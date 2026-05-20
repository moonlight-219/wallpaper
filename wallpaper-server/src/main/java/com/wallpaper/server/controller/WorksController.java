package com.wallpaper.server.controller;

import com.wallpaper.server.common.ErrorCode;
import com.wallpaper.server.common.Result;
import com.wallpaper.server.dto.CreateWorkRequest;
import com.wallpaper.server.entity.User;
import com.wallpaper.server.entity.UserAction;
import com.wallpaper.server.entity.Wallpaper;
import com.wallpaper.server.entity.Work;
import com.wallpaper.server.repository.UserActionRepository;
import com.wallpaper.server.repository.WallpaperRepository;
import com.wallpaper.server.service.UserService;
import com.wallpaper.server.service.WallpaperService;
import com.wallpaper.server.service.WorksService;
import com.wallpaper.server.service.OssService;
import com.wallpaper.server.util.UserContext;
import com.wallpaper.server.util.XssUtil;
import com.wallpaper.server.vo.PageResult;
import com.wallpaper.server.vo.WallpaperVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/works")
@Validated
public class WorksController {

    @Autowired
    private WorksService worksService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserActionRepository userActionRepository;

    @Autowired
    private WallpaperRepository wallpaperRepository;

    @Autowired
    private OssService ossService;

    @Autowired
    private WallpaperService wallpaperService;

    @PostMapping
    @Transactional
    public Result<Work> create(@Valid @RequestBody CreateWorkRequest request) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(ErrorCode.UNAUTHORIZED);
        }

        Work work = new Work();
        work.setTitle(XssUtil.clean(request.getTitle()));
        work.setType(request.getType());
        work.setCategoryId(request.getCategoryId());
        work.setDescription(XssUtil.clean(request.getDescription()));
        work.setUserId(userId);
        work.setCoverUrl("");
        work.setImageWidth(null);
        work.setImageHeight(null);
        work.setThumbnailUrl(null);
        work.setFileFormat(null);
        work.setFileSize(null);
        work.setWallpaperCount(0);
        work.setLikeCount(0);
        work.setCollectCount(0);
        work.setViewCount(0);
        work.setDownloadCount(0);

        if (UserContext.isAdmin()) {
            work.setStatus(1);
        } else {
            work.setStatus(0);
        }
        work.setCreateTime(java.time.LocalDateTime.now());
        work.setUpdateTime(java.time.LocalDateTime.now());

        Work created = worksService.create(work);

        userService.incrementWorkCount(userId);

        if (request.getWallpapers() != null && !request.getWallpapers().isEmpty()) {
            for (int i = 0; i < request.getWallpapers().size(); i++) {
                CreateWorkRequest.WallpaperInfo wallpaperInfo = request.getWallpapers().get(i);

                Wallpaper wallpaper = new Wallpaper();
                wallpaper.setTitle(work.getTitle());
                wallpaper.setUrl(wallpaperInfo.getUrl());
                wallpaper.setThumbnailUrl(wallpaperInfo.getThumbnailUrl());
                wallpaper.setImageWidth(wallpaperInfo.getImageWidth());
                wallpaper.setImageHeight(wallpaperInfo.getImageHeight());
                wallpaper.setFileFormat(wallpaperInfo.getFileFormat());
                wallpaper.setFileSize(wallpaperInfo.getFileSize());
                wallpaper.setType(work.getType());
                wallpaper.setCategoryId(work.getCategoryId());
                wallpaper.setUserId(userId);
                wallpaper.setWorkId(created.getId());
                wallpaper.setCreateTime(java.time.LocalDateTime.now());
                wallpaper.setUpdateTime(java.time.LocalDateTime.now());

                if (UserContext.isAdmin()) {
                    wallpaper.setIsDel(false);
                } else {
                    wallpaper.setIsDel(true);
                }

                wallpaperService.create(wallpaper);
            }

            int wallpaperCount = request.getWallpapers().size();
            worksService.updateWallpaperCount(created.getId(), wallpaperCount);

            if (wallpaperCount > 0) {
                CreateWorkRequest.WallpaperInfo firstWallpaper = request.getWallpapers().get(0);
                worksService.updateCoverUrl(created.getId(), firstWallpaper.getUrl(),
                        firstWallpaper.getImageWidth(), firstWallpaper.getImageHeight(),
                        firstWallpaper.getThumbnailUrl(), firstWallpaper.getFileFormat(),
                        firstWallpaper.getFileSize());
            }
        }

        return Result.success(created);
    }

    @GetMapping("/{id}")
    public Result<Work> getById(@PathVariable @NotNull Long id) {
        Work work = worksService.findById(id);
        if (work == null) {
            return Result.error(ErrorCode.WORK_NOT_FOUND);
        }
        return Result.success(work);
    }

    @GetMapping
    public Result<PageResult<Work>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer status) {
        Page<Work> workPage;
        if (status != null) {
            workPage = worksService.findByStatus(status, page, pageSize);
        } else {
            workPage = worksService.findAll(page, pageSize);
        }

        return Result.success(PageResult.of(workPage, page, pageSize));
    }

    @GetMapping("/user/{userId}")
    public Result<PageResult<Work>> getUserWorks(
            @PathVariable @NotNull Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer status) {
        Page<Work> workPage;
        if (status != null) {
            workPage = worksService.findByUserIdAndStatus(userId, status, page, pageSize);
        } else {
            workPage = worksService.findByUserId(userId, page, pageSize);
        }

        return Result.success(PageResult.of(workPage, page, pageSize));
    }

    @GetMapping("/category/{categoryId}")
    public Result<PageResult<Work>> getCategoryWorks(
            @PathVariable @NotNull Long categoryId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<Work> workPage = worksService.findByCategoryId(categoryId, page, pageSize);

        return Result.success(PageResult.of(workPage, page, pageSize));
    }

    @GetMapping("/{workId}/wallpapers")
    public Result<List<WallpaperVO>> getWorkWallpapers(
            @PathVariable @NotNull Long workId,
            @RequestParam(required = false) Long userId) {
        Work work = worksService.findById(workId);
        if (work == null) {
            return Result.error(ErrorCode.WORK_NOT_FOUND);
        }

        List<Wallpaper> wallpapers = wallpaperRepository
                .findByWorkIdIn(java.util.Arrays.asList(workId));

        List<Long> userIds = wallpapers.stream()
                .map(Wallpaper::getUserId)
                .filter(u -> u != null)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, User> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            List<User> users = userService.findByIds(userIds);
            userMap = users.stream()
                    .collect(Collectors.toMap(User::getId, u -> u));
        }

        List<WallpaperVO> wallpaperVOs = new ArrayList<>();

        for (Wallpaper wallpaper : wallpapers) {
            WallpaperVO wallpaperVO = new WallpaperVO();
            wallpaperVO.setId(wallpaper.getId());
            wallpaperVO.setTitle(wallpaper.getTitle());
            wallpaperVO.setUrl(wallpaper.getUrl());
            wallpaperVO.setThumbnailUrl(wallpaper.getThumbnailUrl());
            wallpaperVO.setImageWidth(wallpaper.getImageWidth());
            wallpaperVO.setImageHeight(wallpaper.getImageHeight());
            wallpaperVO.setFileFormat(wallpaper.getFileFormat());
            wallpaperVO.setFileSize(wallpaper.getFileSize());
            wallpaperVO.setType(wallpaper.getType());
            wallpaperVO.setViewCount(wallpaper.getViewCount());
            wallpaperVO.setDownloadCount(wallpaper.getDownloadCount());
            wallpaperVO.setLikeCount(wallpaper.getLikeCount());
            wallpaperVO.setCollectCount(wallpaper.getCollectCount());
            wallpaperVO.setCreateTime(wallpaper.getCreateTime());

            if (wallpaper.getUserId() != null) {
                User user = userMap.get(wallpaper.getUserId());
                if (user != null) {
                    WallpaperVO.AuthorInfo authorInfo = new WallpaperVO.AuthorInfo();
                    authorInfo.setId(user.getId());
                    authorInfo.setName(user.getNickname() != null ? user.getNickname() : user.getUsername());
                    authorInfo.setAvatarUrl(user.getAvatar());
                    wallpaperVO.setAuthor(authorInfo);
                }
            }

            if (userId != null) {
                UserAction likeAction = userActionRepository.findByUserAndWallpaperAndAction(userId, wallpaper.getId(),
                        "like");
                wallpaperVO.setIsLiked(likeAction != null);

                UserAction collectAction = userActionRepository.findByUserAndWallpaperAndAction(userId,
                        wallpaper.getId(), "collect");
                wallpaperVO.setIsCollected(collectAction != null);
            } else {
                wallpaperVO.setIsLiked(false);
                wallpaperVO.setIsCollected(false);
            }

            wallpaperVOs.add(wallpaperVO);
        }

        return Result.success(wallpaperVOs);
    }

    @PutMapping("/{id}")
    public Result<Work> update(@PathVariable @NotNull Long id, @Valid @RequestBody CreateWorkRequest request) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(ErrorCode.UNAUTHORIZED);
        }

        Work work = worksService.findById(id);
        if (work == null) {
            return Result.error(ErrorCode.WORK_NOT_FOUND);
        }

        if (!work.getUserId().equals(userId)) {
            return Result.error(ErrorCode.WORK_NO_PERMISSION);
        }

        work.setTitle(XssUtil.clean(request.getTitle()));
        work.setType(request.getType());
        work.setCategoryId(request.getCategoryId());
        work.setDescription(XssUtil.clean(request.getDescription()));

        Work updated = worksService.update(work);
        return Result.success(updated);
    }

    @DeleteMapping("/{id}")
    @Transactional
    public Result<Void> delete(@PathVariable @NotNull Long id) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(ErrorCode.UNAUTHORIZED);
        }

        Work work = worksService.findById(id);
        if (work == null) {
            return Result.error(ErrorCode.WORK_NOT_FOUND);
        }

        if (!work.getUserId().equals(userId)) {
            return Result.error(ErrorCode.WORK_NO_PERMISSION);
        }

        // 软删除：只标记为已删除，不实际删除数据和文件
        work.setIsDel(true);
        work.setUpdateTime(LocalDateTime.now());
        worksService.update(work);

        // 同时软删除关联的壁纸
        List<Wallpaper> wallpapers = wallpaperRepository.findByWorkId(id);
        for (Wallpaper wallpaper : wallpapers) {
            wallpaper.setIsDel(true);
            wallpaper.setUpdateTime(LocalDateTime.now());
            wallpaperRepository.save(wallpaper);
        }

        // 减少用户的作品计数
        userService.decrementWorkCount(userId);

        return Result.success();
    }

    @PostMapping("/{workId}/like")
    @Transactional
    public Result<Void> toggleLike(@PathVariable @NotNull Long workId) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(ErrorCode.UNAUTHORIZED);
        }

        Work work = worksService.findById(workId);
        if (work == null) {
            return Result.error(ErrorCode.WORK_NOT_FOUND);
        }

        UserAction existing = userActionRepository.findByUserAndWorkAndAction(userId, workId, "like");

        if (existing == null) {
            UserAction action = new UserAction();
            action.setUserId(userId);
            action.setWorkId(workId);
            action.setActionType("like");
            action.setCreateTime(java.time.LocalDateTime.now());
            userActionRepository.save(action);

            worksService.incrementLikeCount(workId);
        } else {
            userActionRepository.delete(existing);
            worksService.decrementLikeCount(workId);
        }

        return Result.success();
    }

    @PostMapping("/{workId}/collect")
    @Transactional
    public Result<Void> toggleCollect(@PathVariable @NotNull Long workId) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(ErrorCode.UNAUTHORIZED);
        }

        Work work = worksService.findById(workId);
        if (work == null) {
            return Result.error(ErrorCode.WORK_NOT_FOUND);
        }

        UserAction existing = userActionRepository.findByUserAndWorkAndAction(userId, workId, "collect");

        if (existing == null) {
            UserAction action = new UserAction();
            action.setUserId(userId);
            action.setWorkId(workId);
            action.setActionType("collect");
            action.setCreateTime(java.time.LocalDateTime.now());
            userActionRepository.save(action);

            worksService.incrementCollectCount(workId);
        } else {
            userActionRepository.delete(existing);
            worksService.decrementCollectCount(workId);
        }

        return Result.success();
    }
}
