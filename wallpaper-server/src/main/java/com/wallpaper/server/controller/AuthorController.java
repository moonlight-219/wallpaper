package com.wallpaper.server.controller;

import com.wallpaper.server.entity.User;
import com.wallpaper.server.entity.Wallpaper;
import com.wallpaper.server.repository.WallpaperRepository;
import com.wallpaper.server.service.UserService;
import com.wallpaper.server.service.WallpaperService;
import com.wallpaper.server.vo.AuthorDetailVO;
import com.wallpaper.server.vo.AuthorPreviewVO;
import com.wallpaper.server.vo.WallpaperVO;
import com.wallpaper.server.vo.PageResult;
import com.wallpaper.server.common.Result;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/author")
@Validated
public class AuthorController {
        @Autowired
        private UserService userService;

        @Autowired
        private WallpaperService wallpaperService;

        @Autowired
        private WallpaperRepository wallpaperRepository;

        @GetMapping("/list")
        public Result<PageResult<AuthorPreviewVO>> list(
                        @RequestParam(defaultValue = "1") Integer page,
                        @RequestParam(defaultValue = "10") Integer pageSize) {
                com.wallpaper.server.vo.PageResult<User> userPage = userService.findAll(page, pageSize);
                List<User> users = userPage.getList();

                List<Long> userIds = users.stream()
                                .map(User::getId)
                                .collect(Collectors.toList());

                List<Wallpaper> allWallpapers = wallpaperRepository.findPublicByUserIdIn(userIds);
                java.util.Map<Long, List<Wallpaper>> wallpapersByUserId = allWallpapers.stream()
                                .collect(Collectors.groupingBy(Wallpaper::getUserId));

                List<AuthorPreviewVO> previewVOs = users.stream()
                                .map(user -> {
                                        AuthorPreviewVO previewVO = new AuthorPreviewVO();
                                        BeanUtils.copyProperties(user, previewVO);
                                        previewVO.setAvatarUrl(user.getAvatar());
                                        previewVO.setName(user.getNickname() != null ? user.getNickname()
                                                        : user.getUsername());
                                        previewVO.setIsCreator(user.getIsCreator());

                                        List<Wallpaper> userWallpapers = wallpapersByUserId.getOrDefault(user.getId(),
                                                        new java.util.ArrayList<>());

                                        previewVO.setWorkCount(userWallpapers.size());

                                        List<WallpaperVO> wallpaperVOs = userWallpapers.stream()
                                                        .sorted((a, b) -> b.getCreateTime()
                                                                        .compareTo(a.getCreateTime()))
                                                        .limit(3)
                                                        .map(w -> {
                                                                WallpaperVO vo = new WallpaperVO();
                                                                BeanUtils.copyProperties(w, vo);
                                                                return vo;
                                                        })
                                                        .collect(Collectors.toList());

                                        previewVO.setHotWallpapers(wallpaperVOs);

                                        int totalLikes = userWallpapers.stream()
                                                        .mapToInt(w -> w.getLikeCount() != null ? w.getLikeCount() : 0)
                                                        .sum();

                                        int totalCollects = userWallpapers.stream()
                                                        .mapToInt(w -> w.getCollectCount() != null ? w.getCollectCount()
                                                                        : 0)
                                                        .sum();

                                        previewVO.setTotalLikes(totalLikes);
                                        previewVO.setTotalCollects(totalCollects);

                                        return previewVO;
                                })
                                .filter(previewVO -> previewVO.getHotWallpapers() != null
                                                && !previewVO.getHotWallpapers().isEmpty())
                                .collect(Collectors.toList());

                PageResult<AuthorPreviewVO> pageResult = new PageResult<>();
                pageResult.setList(previewVOs);
                pageResult.setTotal((long) previewVOs.size());
                pageResult.setPage(page);
                pageResult.setPageSize(pageSize);

                return Result.success(pageResult);
        }

        @GetMapping("/detail/{id}")
        public Result<AuthorDetailVO> getDetail(@PathVariable @NotNull Long id,
                        @RequestParam(defaultValue = "1") Integer page,
                        @RequestParam(defaultValue = "10") Integer pageSize,
                        @RequestParam(required = false) String type) {
                User user = userService.findById(id);
                if (user == null) {
                        return Result.error(8001, "用户不存在");
                }

                AuthorDetailVO detailVO = new AuthorDetailVO();
                BeanUtils.copyProperties(user, detailVO);
                detailVO.setAvatarUrl(user.getAvatar());
                detailVO.setName(user.getNickname() != null ? user.getNickname() : user.getUsername());

                List<Wallpaper> wallpapers = wallpaperService.findByUserId(id, type, page, pageSize);
                List<WallpaperVO> wallpaperVOs = wallpapers.stream()
                                .map(w -> {
                                        WallpaperVO vo = new WallpaperVO();
                                        BeanUtils.copyProperties(w, vo);
                                        return vo;
                                })
                                .collect(Collectors.toList());

                detailVO.setWallpapers(wallpaperVOs);

                // 统计用户所有壁纸的点赞和收藏总数（不限于当前页）
                List<Wallpaper> allUserWallpapers = wallpaperRepository.findPublicByUserId(id);
                int totalLikes = allUserWallpapers.stream()
                                .mapToInt(w -> w.getLikeCount() != null ? w.getLikeCount() : 0)
                                .sum();

                int totalCollects = allUserWallpapers.stream()
                                .mapToInt(w -> w.getCollectCount() != null ? w.getCollectCount() : 0)
                                .sum();

                detailVO.setTotalLikes(totalLikes);
                detailVO.setTotalCollects(totalCollects);
                detailVO.setWorkCount(allUserWallpapers.size());

                return Result.success(detailVO);
        }

        /**
         * 获取作者基本信息（不包含作品列表）
         */
        @GetMapping("/{id}")
        public Result<AuthorDetailVO> getById(@PathVariable @NotNull Long id) {
                User user = userService.findById(id);
                if (user == null) {
                        return Result.error(8001, "用户不存在");
                }

                AuthorDetailVO detailVO = new AuthorDetailVO();
                BeanUtils.copyProperties(user, detailVO);
                detailVO.setAvatarUrl(user.getAvatar());
                detailVO.setName(user.getNickname() != null ? user.getNickname() : user.getUsername());

                // 统计用户所有壁纸的点赞和收藏总数
                List<Wallpaper> allUserWallpapers = wallpaperRepository.findPublicByUserId(id);
                int totalLikes = allUserWallpapers.stream()
                                .mapToInt(w -> w.getLikeCount() != null ? w.getLikeCount() : 0)
                                .sum();

                int totalCollects = allUserWallpapers.stream()
                                .mapToInt(w -> w.getCollectCount() != null ? w.getCollectCount() : 0)
                                .sum();

                detailVO.setTotalLikes(totalLikes);
                detailVO.setTotalCollects(totalCollects);
                detailVO.setWorkCount(allUserWallpapers.size());
                detailVO.setWallpapers(new ArrayList<>()); // 空列表

                return Result.success(detailVO);
        }
}
