package com.wallpaper.server.controller;

import com.wallpaper.server.dto.AuditRequest;
import com.wallpaper.server.entity.User;
import com.wallpaper.server.entity.Wallpaper;
import com.wallpaper.server.entity.Work;
import com.wallpaper.server.repository.UserRepository;
import com.wallpaper.server.repository.WallpaperRepository;
import com.wallpaper.server.repository.WorksRepository;
import com.wallpaper.server.common.Result;
import com.wallpaper.server.vo.PageResult;
import com.wallpaper.server.vo.UserVO;
import com.wallpaper.server.vo.DashboardStatsVO;
import com.wallpaper.server.vo.DashboardVO;
import com.wallpaper.server.service.DashboardService;
import com.wallpaper.server.service.WallpaperService;
import com.wallpaper.server.service.OssService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin")
@Validated
public class AdminController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WallpaperRepository wallpaperRepository;

    @Autowired
    private WorksRepository worksRepository;

    @Autowired
    private OssService ossService;

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/stats")
    public Result<DashboardStatsVO> getStats() {
        DashboardStatsVO stats = dashboardService.getStats();
        return Result.success(stats);
    }

    @GetMapping("/dashboard")
    public Result<DashboardVO> getDashboard() {
        DashboardVO dashboard = dashboardService.getDashboardData();
        return Result.success(dashboard);
    }

    @PutMapping("/user/{id}/creator-status")
    public Result<Void> updateCreatorStatus(@PathVariable @NotNull Long id, @RequestBody User user) {
        User existingUser = userRepository.findById(id).orElse(null);
        if (existingUser == null) {
            return Result.userNotFound();
        }

        existingUser.setIsCreator(user.getIsCreator());
        userRepository.save(existingUser);
        return Result.success();
    }

    @GetMapping("/users")
    public Result<PageResult<UserVO>> getAllUsers(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword) {
        int p = page != null && page > 0 ? page - 1 : 0;
        int ps = pageSize != null && pageSize > 0 ? pageSize : 10;

        Pageable pageable = PageRequest.of(p, ps, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<User> pageResult;

        if (keyword != null && !keyword.trim().isEmpty()) {
            pageResult = userRepository.findByUsernameOrNicknameContainingIgnoreCase(keyword.trim(), pageable);
        } else {
            pageResult = userRepository.findAll(pageable);
        }

        List<User> users = pageResult.getContent();
        List<Long> userIds = users.stream().map(User::getId).collect(Collectors.toList());

        List<Wallpaper> wallpapers = wallpaperRepository.findByUserIdIn(userIds);

        Map<Long, List<Wallpaper>> wallpapersByUserId = wallpapers.stream()
                .collect(Collectors.groupingBy(Wallpaper::getUserId));

        List<UserVO> userVOs = users.stream()
                .map(user -> {
                    UserVO userVO = new UserVO();
                    BeanUtils.copyProperties(user, userVO);

                    List<Wallpaper> userWallpapers = wallpapersByUserId.get(user.getId());
                    if (userWallpapers != null) {
                        int totalLikes = userWallpapers.stream()
                                .mapToInt(w -> w.getLikeCount() != null ? w.getLikeCount() : 0)
                                .sum();
                        int totalCollects = userWallpapers.stream()
                                .mapToInt(w -> w.getCollectCount() != null ? w.getCollectCount() : 0)
                                .sum();
                        userVO.setTotalLikes(totalLikes);
                        userVO.setTotalCollects(totalCollects);
                    } else {
                        userVO.setTotalLikes(0);
                        userVO.setTotalCollects(0);
                    }

                    return userVO;
                })
                .collect(Collectors.toList());

        return Result.success(PageResult.of(userVOs, pageResult.getTotalElements(), page, pageSize));
    }

    @GetMapping("/wallpapers")
    public Result<PageResult<com.wallpaper.server.vo.WallpaperVO>> getWallpapers(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer status) {
        int p = page != null && page > 0 ? page - 1 : 0;
        int ps = pageSize != null && pageSize > 0 ? pageSize : 10;

        Pageable pageable = PageRequest.of(p, ps, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<Wallpaper> pageResult;

        if (status != null) {
            pageResult = wallpaperRepository.findByStatus(status, pageable);
        } else {
            pageResult = wallpaperRepository.findAll(pageable);
        }

        List<com.wallpaper.server.vo.WallpaperVO> wallpaperVOs = pageResult.getContent().stream()
                .map(wallpaper -> {
                    com.wallpaper.server.vo.WallpaperVO wallpaperVO = new com.wallpaper.server.vo.WallpaperVO();
                    BeanUtils.copyProperties(wallpaper, wallpaperVO);
                    return wallpaperVO;
                })
                .collect(Collectors.toList());

        return Result.success(PageResult.of(wallpaperVOs, pageResult.getTotalElements(), page, pageSize));
    }

    @PutMapping("/wallpaper/{id}/audit")
    public Result<Void> auditWallpaper(@PathVariable @NotNull Long id, @Valid @RequestBody AuditRequest request) {
        Wallpaper wallpaper = wallpaperRepository.findById(id).orElse(null);
        if (wallpaper == null) {
            return Result.error(8001, "壁纸不存在");
        }

        if (request.getStatus() != 1 && request.getStatus() != 2) {
            return Result.error(400, "审核状态只能是1（通过）或2（拒绝）");
        }

        wallpaper.setStatus(request.getStatus());
        wallpaperRepository.save(wallpaper);
        return Result.success();
    }

    @PutMapping("/work/{id}/audit")
    @Transactional
    public Result<Void> auditWork(@PathVariable @NotNull Long id, @Valid @RequestBody AuditRequest request) {
        Work work = worksRepository.findById(id).orElse(null);
        if (work == null) {
            return Result.error(8001, "作品集不存在");
        }

        if (request.getStatus() != 1 && request.getStatus() != 2) {
            return Result.error(400, "审核状态只能是1（通过）或2（拒绝）");
        }

        if (request.getStatus() == 2) {
            if (request.getReason() == null || request.getReason().trim().isEmpty()) {
                return Result.error(400, "拒绝审核必须填写原因");
            }

            List<Wallpaper> wallpapers = wallpaperRepository.findByWorkId(id);
            for (Wallpaper wallpaper : wallpapers) {
                if (wallpaper.getUrl() != null) {
                    ossService.deleteFile(wallpaper.getUrl());
                }
                if (wallpaper.getThumbnailUrl() != null) {
                    ossService.deleteFile(wallpaper.getThumbnailUrl());
                }
                wallpaperRepository.delete(wallpaper);
            }
            work.setStatus(2);
            work.setRejectReason(request.getReason());
            worksRepository.save(work);
        } else {
            work.setStatus(1);
            work.setRejectReason(null);
            // 设置发布时间为审核通过的时间
            work.setPublishTime(java.time.LocalDateTime.now());
            worksRepository.save(work);

            List<Wallpaper> wallpapers = wallpaperRepository.findByWorkId(id);
            for (Wallpaper wallpaper : wallpapers) {
                wallpaper.setIsDel(false);
                wallpaperRepository.save(wallpaper);
            }
        }

        return Result.success();
    }

    @GetMapping("/work/{id}/reject-reason")
    public Result<String> getWorkRejectReason(@PathVariable @NotNull Long id) {
        Work work = worksRepository.findById(id).orElse(null);
        if (work == null) {
            return Result.error(8001, "作品集不存在");
        }

        if (work.getStatus() != 2) {
            return Result.error(400, "该作品集未被拒绝");
        }

        return Result.success(work.getRejectReason());
    }
}