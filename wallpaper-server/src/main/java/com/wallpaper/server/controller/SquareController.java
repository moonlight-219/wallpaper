package com.wallpaper.server.controller;

import com.wallpaper.server.entity.Category;
import com.wallpaper.server.entity.User;
import com.wallpaper.server.entity.UserAction;
import com.wallpaper.server.entity.Wallpaper;
import com.wallpaper.server.entity.Work;
import com.wallpaper.server.repository.CategoryRepository;
import com.wallpaper.server.repository.UserActionRepository;
import com.wallpaper.server.repository.WallpaperRepository;
import com.wallpaper.server.repository.WorksRepository;
import com.wallpaper.server.service.UserService;
import com.wallpaper.server.service.WorksService;
import com.wallpaper.server.util.UserContext;
import com.wallpaper.server.vo.PageResult;
import com.wallpaper.server.vo.WallpaperVO;
import com.wallpaper.server.vo.WorkVO;
import com.wallpaper.server.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/square")
@Validated
public class SquareController {
    @Autowired
    private UserActionRepository userActionRepository;

    @Autowired
    private WorksService worksService;

    @Autowired
    private WorksRepository worksRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private WallpaperRepository wallpaperRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @GetMapping("/works")
    public Result<PageResult<WorkVO>> getWorks(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer sortType,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long userId) {

        int p = page != null && page > 0 ? page - 1 : 0;
        int ps = pageSize != null && pageSize > 0 ? pageSize : 10;

        Integer queryStatus = status;

        Sort sort;
        if (sortType != null && sortType == 1) {
            sort = Sort.by(Sort.Direction.DESC, "likeCount");
        } else if (sortType != null && sortType == 2) {
            sort = Sort.by(Sort.Direction.DESC, "collectCount");
        } else {
            // 默认排序：如果查询已发布作品（status=1），按发布时间排序；否则按创建时间排序
            if (queryStatus != null && queryStatus == 1) {
                sort = Sort.by(Sort.Direction.DESC, "publishTime").and(Sort.by(Sort.Direction.DESC, "createTime"));
            } else {
                sort = Sort.by(Sort.Direction.DESC, "createTime");
            }
        }

        Pageable pageable = PageRequest.of(p, ps, sort);

        org.springframework.data.domain.Page<Work> workPage;

        if (queryStatus != null && categoryId != null && keyword != null && !keyword.trim().isEmpty()) {
            workPage = worksRepository.findByStatusAndCategoryIdAndAuthorKeyword(queryStatus, categoryId,
                    keyword.trim(), pageable);
        } else if (queryStatus != null && categoryId != null) {
            workPage = worksRepository.findByStatusAndCategoryId(queryStatus, categoryId, pageable);
        } else if (queryStatus != null && keyword != null && !keyword.trim().isEmpty()) {
            workPage = worksRepository.findByStatusAndAuthorKeyword(queryStatus, keyword.trim(), pageable);
        } else if (categoryId != null && keyword != null && !keyword.trim().isEmpty()) {
            workPage = worksRepository.findByCategoryIdAndAuthorKeyword(categoryId, keyword.trim(), pageable);
        } else if (categoryId != null) {
            workPage = worksRepository.findByCategoryId(categoryId, pageable);
        } else if (keyword != null && !keyword.trim().isEmpty()) {
            workPage = worksRepository.findByAuthorKeyword(keyword.trim(), pageable);
        } else if (queryStatus != null) {
            workPage = worksRepository.findByStatusOrderByCreateTime(queryStatus, pageable);
        } else {
            workPage = worksRepository.findAll(pageable);
        }

        List<Long> userIds = workPage.getContent().stream()
                .map(Work::getUserId)
                .filter(u -> u != null)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, User> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            List<User> users = userService.findByIds(userIds);
            userMap = users.stream()
                    .collect(Collectors.toMap(User::getId, u -> u));
        }

        List<Long> categoryIds = workPage.getContent().stream()
                .map(Work::getCategoryId)
                .filter(c -> c != null)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, Category> categoryMap = new HashMap<>();
        if (!categoryIds.isEmpty()) {
            List<Category> categories = categoryRepository.findAllById(categoryIds);
            categoryMap = categories.stream()
                    .collect(Collectors.toMap(Category::getId, c -> c));
        }

        List<Long> workIds = workPage.getContent().stream()
                .map(Work::getId)
                .collect(Collectors.toList());

        List<Wallpaper> allWallpapers = wallpaperRepository.findByWorkIdIn(workIds);
        Map<Long, List<Wallpaper>> wallpapersByWorkId = allWallpapers.stream()
                .collect(Collectors.groupingBy(Wallpaper::getWorkId));

        final Map<Long, Category> finalCategoryMap = categoryMap;
        final Map<Long, User> finalUserMap = userMap;

        List<WorkVO> works = new ArrayList<>();

        for (Work work : workPage.getContent()) {
            WorkVO workVO = new WorkVO();
            workVO.setId(work.getId());
            workVO.setTitle(work.getTitle());
            workVO.setCoverUrl(work.getCoverUrl());
            workVO.setImageWidth(work.getImageWidth());
            workVO.setImageHeight(work.getImageHeight());
            workVO.setThumbnailUrl(work.getThumbnailUrl());
            workVO.setFileFormat(work.getFileFormat());
            workVO.setFileSize(work.getFileSize());
            workVO.setType(work.getType());
            workVO.setImageCount(work.getWallpaperCount());

            if (work.getUserId() != null) {
                User user = finalUserMap.get(work.getUserId());
                if (user != null) {
                    WorkVO.AuthorInfo authorInfo = new WorkVO.AuthorInfo();
                    authorInfo.setId(user.getId());
                    authorInfo.setName(user.getNickname() != null ? user.getNickname() : user.getUsername());
                    authorInfo.setAvatar(user.getAvatar());
                    workVO.setAuthor(authorInfo);
                }
            }

            workVO.setLikes(work.getLikeCount());
            workVO.setCollects(work.getCollectCount());
            workVO.setViews(work.getViewCount());
            // 使用发布时间，如果没有则使用创建时间
            workVO.setPublishTime(work.getPublishTime() != null ? work.getPublishTime() : work.getCreateTime());
            workVO.setStatus(work.getStatus());

            if (userId != null) {
                UserAction likeAction = userActionRepository.findByUserAndWorkAndAction(userId, work.getId(), "like");
                workVO.setIsLiked(likeAction != null);

                UserAction collectAction = userActionRepository.findByUserAndWorkAndAction(userId, work.getId(),
                        "collect");
                workVO.setIsCollected(collectAction != null);
            } else {
                workVO.setIsLiked(false);
                workVO.setIsCollected(false);
            }

            List<Wallpaper> workWallpapers = wallpapersByWorkId.getOrDefault(work.getId(), new ArrayList<>());
            List<WallpaperVO> wallpaperVOs = new ArrayList<>();

            for (Wallpaper wallpaper : workWallpapers) {
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

                if (wallpaper.getCategoryId() != null) {
                    Category category = finalCategoryMap.get(wallpaper.getCategoryId());
                    if (category != null) {
                        wallpaperVO.setCategory(category.getName());
                    }
                }

                wallpaperVO.setViewCount(wallpaper.getViewCount());
                wallpaperVO.setDownloadCount(wallpaper.getDownloadCount());
                wallpaperVO.setLikeCount(wallpaper.getLikeCount());
                wallpaperVO.setCollectCount(wallpaper.getCollectCount());
                wallpaperVO.setCreateTime(wallpaper.getCreateTime());

                if (wallpaper.getUserId() != null) {
                    User user = finalUserMap.get(wallpaper.getUserId());
                    if (user != null) {
                        WallpaperVO.AuthorInfo authorInfo = new WallpaperVO.AuthorInfo();
                        authorInfo.setId(user.getId());
                        authorInfo.setName(user.getNickname() != null ? user.getNickname() : user.getUsername());
                        authorInfo.setAvatarUrl(user.getAvatar());
                        wallpaperVO.setAuthor(authorInfo);
                    }
                }

                wallpaperVOs.add(wallpaperVO);
            }

            workVO.setWallpapers(wallpaperVOs);
            works.add(workVO);
        }

        return Result.success(PageResult.of(works, workPage.getTotalElements(), page, pageSize));
    }
}
