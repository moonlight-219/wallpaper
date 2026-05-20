package com.wallpaper.server.controller;

import com.wallpaper.server.dto.UploadWallpaperRequest;
import com.wallpaper.server.dto.UploadWallpaperToWorkRequest;
import com.wallpaper.server.dto.WallpaperQuery;
import com.wallpaper.server.entity.Category;
import com.wallpaper.server.entity.Report;
import com.wallpaper.server.entity.User;
import com.wallpaper.server.entity.UserAction;
import com.wallpaper.server.entity.Wallpaper;
import com.wallpaper.server.entity.Work;
import com.wallpaper.server.repository.ReportRepository;
import com.wallpaper.server.repository.UserActionRepository;
import com.wallpaper.server.repository.WallpaperRepository;
import com.wallpaper.server.repository.WorksRepository;
import com.wallpaper.server.service.UserService;
import com.wallpaper.server.service.WallpaperService;
import com.wallpaper.server.service.WorksService;
import com.wallpaper.server.service.CategoryService;
import com.wallpaper.server.service.OssService;
import com.wallpaper.server.util.UserContext;
import com.wallpaper.server.util.XssUtil;
import com.wallpaper.server.vo.WallpaperDetailVO;
import com.wallpaper.server.vo.WallpaperVO;
import com.wallpaper.server.vo.PageResult;
import com.wallpaper.server.common.Result;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/wallpaper")
@Validated
public class WallpaperController {
    @Autowired
    private WallpaperService wallpaperService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserActionRepository userActionRepository;

    @Autowired
    private WorksRepository worksRepository;

    @Autowired
    private OssService ossService;

    @Autowired
    private WallpaperRepository wallpaperRepository;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private WorksService worksService;

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/list")
    public Result<PageResult<WallpaperVO>> list(WallpaperQuery query) {
        PageResult<WallpaperVO> wallpapers = wallpaperService.findPageResultVO(query);
        return Result.success(wallpapers);
    }

    @GetMapping("/{id}")
    public Result<WallpaperVO> getById(@PathVariable @NotNull Long id) {
        WallpaperVO wallpaper = wallpaperService.findById(id);
        if (wallpaper == null) {
            return Result.wallpaperNotFound();
        }
        return Result.success(wallpaper);
    }

    @GetMapping("/detail/{id}")
    public Result<WallpaperDetailVO> getDetail(@PathVariable Long id, @RequestParam(required = false) Long userId) {
        Wallpaper wallpaper = wallpaperService.findPublicByIdEntity(id);
        if (wallpaper == null) {
            return Result.wallpaperNotFound();
        }

        WallpaperDetailVO detailVO = new WallpaperDetailVO();
        BeanUtils.copyProperties(wallpaper, detailVO);

        // 设置分类名称
        if (wallpaper.getCategoryId() != null) {
            Category category = categoryService.findById(wallpaper.getCategoryId());
            if (category != null) {
                detailVO.setCategory(category.getName());
            }
        }

        if (wallpaper.getUserId() != null) {
            User user = userService.findById(wallpaper.getUserId());
            if (user != null) {
                WallpaperDetailVO.AuthorInfo authorInfo = new WallpaperDetailVO.AuthorInfo();
                authorInfo.setId(user.getId());
                authorInfo.setName(user.getNickname() != null ? user.getNickname() : user.getUsername());
                authorInfo.setAvatarUrl(user.getAvatar());
                detailVO.setAuthor(authorInfo);
            }
        }

        if (userId != null) {
            UserAction likeAction = userActionRepository.findByUserAndWallpaperAndAction(userId, id, "like");
            detailVO.setIsLiked(likeAction != null);

            UserAction collectAction = userActionRepository.findByUserAndWallpaperAndAction(userId, id, "collect");
            detailVO.setIsCollected(collectAction != null);
        } else {
            detailVO.setIsLiked(false);
            detailVO.setIsCollected(false);
        }

        return Result.success(detailVO);
    }

    @GetMapping("/search")
    public Result<List<WallpaperVO>> search(@RequestParam @NotBlank String keyword) {
        WallpaperQuery query = new WallpaperQuery();
        query.setKeyword(keyword);
        List<WallpaperVO> wallpapers = wallpaperService.findAll(query);
        return Result.success(wallpapers);
    }

    @PostMapping("/upload")
    public Result<Void> upload(@Valid @RequestBody UploadWallpaperRequest request) {
        User currentUser = UserContext.getCurrentUser();
        if (currentUser == null) {
            return Result.error(401, "未登录");
        }
        if (!Boolean.TRUE.equals(currentUser.getIsCreator())) {
            return Result.userNotCreator();
        }

        Wallpaper wallpaper = new Wallpaper();
        wallpaper.setUrl(request.getUrl());
        wallpaper.setThumbnailUrl(request.getThumbnailUrl());
        wallpaper.setImageWidth(request.getImageWidth());
        wallpaper.setImageHeight(request.getImageHeight());
        wallpaper.setFileFormat(request.getFileFormat());
        wallpaper.setFileSize(request.getFileSize());
        wallpaper.setType(request.getType());
        wallpaper.setTitle(XssUtil.clean(request.getTitle()));
        wallpaper.setUserId(currentUser.getId());
        wallpaper.setCategoryId(request.getCategoryId());
        wallpaper.setDescription(XssUtil.clean(request.getDescription()));
        wallpaper.setStatus(0);
        wallpaper.setCreateTime(LocalDateTime.now());
        wallpaper.setUpdateTime(LocalDateTime.now());

        wallpaperService.create(wallpaper);
        return Result.success();
    }

    @PostMapping("/upload-to-work")
    @Transactional
    public Result<Long> uploadToWork(@Valid @RequestBody UploadWallpaperToWorkRequest request) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "未登录");
        }

        Work work = worksService.findById(request.getWorkId());
        if (work == null) {
            return Result.error(8001, "作品集不存在");
        }

        if (!work.getUserId().equals(userId)) {
            return Result.error(8003, "无权上传到此作品集");
        }

        Wallpaper wallpaper = new Wallpaper();
        wallpaper.setUrl(request.getUrl());
        wallpaper.setThumbnailUrl(request.getThumbnailUrl());
        wallpaper.setImageWidth(request.getImageWidth());
        wallpaper.setImageHeight(request.getImageHeight());
        wallpaper.setFileFormat(request.getFileFormat());
        wallpaper.setFileSize(request.getFileSize());
        wallpaper.setType(work.getType());
        wallpaper.setCategoryId(work.getCategoryId());
        wallpaper.setUserId(userId);
        wallpaper.setWorkId(request.getWorkId());
        wallpaper.setStatus(0);
        wallpaper.setCreateTime(LocalDateTime.now());
        wallpaper.setUpdateTime(LocalDateTime.now());

        Wallpaper created = wallpaperService.create(wallpaper);

        int currentCount = work.getWallpaperCount();
        work.setWallpaperCount(currentCount + 1);

        if (currentCount == 0) {
            work.setCoverUrl(request.getUrl());
        }

        worksService.update(work);

        return Result.success(created.getId());
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Wallpaper wallpaper) {
        User currentUser = UserContext.getCurrentUser();
        if (currentUser == null) {
            return Result.error(401, "未登录");
        }

        Wallpaper existingWallpaper = wallpaperService.findByIdEntity(id);
        if (existingWallpaper == null) {
            return Result.wallpaperNotFound();
        }

        if (!existingWallpaper.getUserId().equals(currentUser.getId()) && !"admin".equals(currentUser.getRole())) {
            return Result.error(403, "无权限修改该壁纸");
        }

        wallpaper.setId(id);
        wallpaperService.update(wallpaper);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Transactional
    public Result<Void> delete(@PathVariable @NotNull Long id) {
        User currentUser = UserContext.getCurrentUser();
        if (currentUser == null) {
            return Result.error(401, "未登录");
        }

        Wallpaper existingWallpaper = wallpaperService.findByIdEntity(id);
        if (existingWallpaper == null) {
            return Result.wallpaperNotFound();
        }

        if (!existingWallpaper.getUserId().equals(currentUser.getId()) && !"admin".equals(currentUser.getRole())) {
            return Result.error(403, "无权限删除该壁纸");
        }

        // 软删除：只标记为已删除，不实际删除数据和文件
        existingWallpaper.setIsDel(true);
        existingWallpaper.setUpdateTime(LocalDateTime.now());
        wallpaperRepository.save(existingWallpaper);

        return Result.success();
    }

    @PostMapping("/like/{id}")
    @Transactional
    public Result<Void> like(@PathVariable @NotNull Long id) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "未登录");
        }

        Wallpaper wallpaper = wallpaperService.findByIdEntity(id);
        if (wallpaper == null || wallpaper.getIsDel()) {
            return Result.wallpaperNotFound();
        }

        UserAction existing = userActionRepository.findByUserAndWallpaperAndAction(userId, id, "like");

        if (existing == null) {
            UserAction action = new UserAction();
            action.setUserId(userId);
            action.setWallpaperId(id);
            action.setActionType("like");
            action.setCreateTime(java.time.LocalDateTime.now());
            userActionRepository.save(action);

            wallpaperRepository.incrementLikeCount(id);
        } else {
            userActionRepository.delete(existing);
            wallpaperRepository.decrementLikeCount(id);
        }

        return Result.success();
    }

    @PostMapping("/collect/{id}")
    @Transactional
    public Result<Void> collect(@PathVariable @NotNull Long id) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "未登录");
        }

        Wallpaper wallpaper = wallpaperService.findByIdEntity(id);
        if (wallpaper == null || wallpaper.getIsDel()) {
            return Result.wallpaperNotFound();
        }

        UserAction existing = userActionRepository.findByUserAndWallpaperAndAction(userId, id, "collect");

        if (existing == null) {
            UserAction action = new UserAction();
            action.setUserId(userId);
            action.setWallpaperId(id);
            action.setActionType("collect");
            action.setCreateTime(java.time.LocalDateTime.now());
            userActionRepository.save(action);

            wallpaperRepository.incrementCollectCount(id);
        } else {
            userActionRepository.delete(existing);
            wallpaperRepository.decrementCollectCount(id);
        }

        return Result.success();
    }

    @PostMapping("/download/{id}")
    @Transactional
    public Result<Void> download(@PathVariable @NotNull Long id) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "未登录");
        }

        Wallpaper wallpaper = wallpaperService.findByIdEntity(id);
        if (wallpaper == null || wallpaper.getIsDel()) {
            return Result.wallpaperNotFound();
        }

        UserAction existing = userActionRepository.findByUserAndWallpaperAndAction(userId, id, "download");

        if (existing == null) {
            // 首次下载，增加计数并记录
            wallpaperRepository.incrementDownloadCount(id);

            UserAction downloadAction = new UserAction();
            downloadAction.setUserId(userId);
            downloadAction.setWallpaperId(id);
            downloadAction.setActionType("download");
            downloadAction.setCreateTime(LocalDateTime.now());
            userActionRepository.save(downloadAction);
        }
        // 如果已下载过，不做任何操作（下载计数不重复增加）

        return Result.success();
    }

    @PostMapping("/report/{id}")
    @Transactional
    public Result<Void> report(@PathVariable @NotNull Long id, @RequestParam @NotBlank String reason) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "未登录");
        }

        Wallpaper wallpaper = wallpaperService.findByIdEntity(id);
        if (wallpaper == null || wallpaper.getIsDel()) {
            return Result.wallpaperNotFound();
        }

        Report report = new Report();
        report.setUserId(userId);
        report.setWallpaperId(id);
        report.setReportType("wallpaper");
        report.setReason(reason);
        report.setCreateTime(LocalDateTime.now());
        reportRepository.save(report);

        return Result.success();
    }
}