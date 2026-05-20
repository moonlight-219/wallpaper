package com.wallpaper.server.controller;

import com.wallpaper.server.dto.LoginRequest;
import com.wallpaper.server.entity.Category;
import com.wallpaper.server.entity.User;
import com.wallpaper.server.entity.UserAction;
import com.wallpaper.server.entity.Wallpaper;
import com.wallpaper.server.entity.Work;
import com.wallpaper.server.repository.CategoryRepository;
import com.wallpaper.server.repository.UserActionRepository;
import com.wallpaper.server.repository.UserRepository;
import com.wallpaper.server.repository.WallpaperRepository;
import com.wallpaper.server.repository.WorksRepository;
import com.wallpaper.server.service.UserService;
import com.wallpaper.server.service.WechatService;
import com.wallpaper.server.util.JwtUtil;
import com.wallpaper.server.util.PasswordUtil;
import com.wallpaper.server.util.UserContext;
import com.wallpaper.server.common.Result;
import com.wallpaper.server.common.ErrorCode;
import com.wallpaper.server.vo.CollectRecordVO;
import com.wallpaper.server.vo.DownloadRecordVO;
import com.wallpaper.server.vo.LikeRecordVO;
import com.wallpaper.server.vo.UploadRecordVO;
import com.wallpaper.server.vo.UserStatsVO;
import com.wallpaper.server.vo.PageResult;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.validation.constraints.NotNull;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/user")
@Validated
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private WechatService wechatService;

    @Autowired
    private WallpaperRepository wallpaperRepository;

    @Autowired
    private UserActionRepository userActionRepository;

    @Autowired
    private WorksRepository worksRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        User user;

        if (request.getUsername() != null && !request.getUsername().isEmpty()) {
            user = userService.findByUsername(request.getUsername());
            if (user == null) {
                return Result.error(ErrorCode.USER_NOT_FOUND.getCode(), "用户名不存在");
            }
            if (!PasswordUtil.matches(request.getPassword(), user.getPassword())) {
                return Result.error(ErrorCode.USER_PASSWORD_ERROR.getCode(), "密码错误");
            }
        } else if (request.getWechatOpenId() != null && !request.getWechatOpenId().isEmpty()) {
            String code = request.getWechatOpenId();
            String openId = wechatService.getOpenIdByCode(code);

            if (openId == null || openId.isEmpty()) {
                log.error("微信登录失败，无法获取openId, code: {}", code);
                return Result.error(401, "微信登录失败，请重试");
            }

            log.info("微信登录 - code: {}, openId: {}", code, openId);

            user = userService.findByWechatOpenId(openId);

            if (user == null) {
                log.info("新用户注册 - openId: {}", openId);
                User newUser = new User();
                newUser.setWechatOpenId(openId);
                newUser.setUsername("wx_" + System.currentTimeMillis());

                String userNickname = request.getNickname();
                if (userNickname != null && !userNickname.isEmpty()) {
                    newUser.setNickname(userNickname);
                } else {
                    String defaultNickname = "微信用户" + String.valueOf(System.currentTimeMillis()).substring(8);
                    newUser.setNickname(defaultNickname);
                }

                String userAvatar = request.getAvatar();
                if (userAvatar != null && !userAvatar.isEmpty()) {
                    newUser.setAvatar(userAvatar);
                } else {
                    newUser.setAvatar("");
                }

                newUser.setRole("user");
                newUser.setIsCreator(true);
                newUser.setFollowerCount(0);
                newUser.setWorkCount(0);
                user = userService.create(newUser);
                log.info("新用户创建成功 - userId: {}, openId: {}, nickname: {}", user.getId(), openId, newUser.getNickname());
            }
        } else {
            return Result.paramError("请提供用户名或微信openId");
        }

        String token = jwtUtil.generateToken(
                user.getId(),
                user.getUsername() != null ? user.getUsername() : "user_" + user.getId(),
                user.getRole() != null ? user.getRole() : "user");

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("userId", user.getId());
        data.put("role", user.getRole());
        data.put("isCreator", user.getIsCreator());
        data.put("username", user.getUsername());
        data.put("nickname", user.getNickname());
        data.put("avatar", user.getAvatar());
        data.put("followerCount", user.getFollowerCount());
        data.put("workCount", user.getWorkCount());
        data.put("bio", user.getBio());

        return Result.success(data);
    }

    @PostMapping("/register")
    public Result<Map<String, Object>> register(@RequestBody User user) {
        if (user.getUsername() != null && !user.getUsername().isEmpty()) {
            if (userService.existsByUsername(user.getUsername())) {
                return Result.error(400, "用户名已存在");
            }
        }

        if (user.getWechatOpenId() != null && !user.getWechatOpenId().isEmpty()) {
            User existingUser = userService.findByWechatOpenId(user.getWechatOpenId());
            if (existingUser != null) {
                return Result.error(400, "微信账号已注册");
            }
        }

        User newUser = userService.create(user);

        String token = jwtUtil.generateToken(
                newUser.getId(),
                newUser.getUsername() != null ? newUser.getUsername() : "user_" + newUser.getId(),
                newUser.getRole() != null ? newUser.getRole() : "user");

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("userId", newUser.getId());
        data.put("username", newUser.getUsername());
        data.put("nickname", newUser.getNickname());
        data.put("role", newUser.getRole());

        return Result.success(data);
    }

    @GetMapping("/{id}")
    public Result<UserStatsVO> getUserById(@PathVariable @NotNull Long id) {
        User user = userService.findById(id);
        if (user == null) {
            return Result.userNotFound();
        }

        List<Wallpaper> wallpapers = wallpaperRepository.findPublicByUserId(id);

        UserStatsVO statsVO = new UserStatsVO();
        statsVO.setId(user.getId());
        statsVO.setUsername(user.getUsername());
        statsVO.setNickname(user.getNickname());
        statsVO.setAvatar(user.getAvatar());
        statsVO.setRole(user.getRole());
        statsVO.setIsCreator(user.getIsCreator());
        statsVO.setFollowerCount(user.getFollowerCount());
        statsVO.setWorkCount(wallpapers.size());
        statsVO.setBio(user.getBio());

        int totalLikes = wallpapers.stream()
                .mapToInt(w -> w.getLikeCount() != null ? w.getLikeCount() : 0)
                .sum();

        int totalCollects = wallpapers.stream()
                .mapToInt(w -> w.getCollectCount() != null ? w.getCollectCount() : 0)
                .sum();

        // 只统计用户对壁纸的点赞和收藏数量（不包括作品集）
        Long myLikeCount = userActionRepository.countByUserIdAndActionTypeAndWallpaperIdNotNull(id, "like");
        Long myCollectCount = userActionRepository.countByUserIdAndActionTypeAndWallpaperIdNotNull(id, "collect");

        statsVO.setTotalLikes(totalLikes);
        statsVO.setTotalCollects(totalCollects);
        statsVO.setMyLikeCount(myLikeCount != null ? myLikeCount.intValue() : 0);
        statsVO.setMyCollectCount(myCollectCount != null ? myCollectCount.intValue() : 0);

        return Result.success(statsVO);
    }

    @GetMapping("/{userId}/downloads")
    public Result<PageResult<DownloadRecordVO>> getDownloadRecords(
            @PathVariable @NotNull Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        User user = userService.findById(userId);
        if (user == null) {
            return Result.userNotFound();
        }

        int p = page != null && page > 0 ? page - 1 : 0;
        int ps = pageSize != null && pageSize > 0 ? pageSize : 10;

        Pageable pageable = PageRequest.of(p, ps, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<UserAction> actionPage = userActionRepository.findByUserIdAndActionTypeAndIsDelOrderByCreateTimeDesc(
                userId, "download", false, pageable);

        List<Long> wallpaperIds = actionPage.getContent().stream()
                .map(UserAction::getWallpaperId)
                .filter(w -> w != null)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, Wallpaper> wallpaperMap = new HashMap<>();
        if (!wallpaperIds.isEmpty()) {
            List<Wallpaper> wallpapers = wallpaperRepository.findPublicByIds(wallpaperIds);
            wallpaperMap = wallpapers.stream()
                    .collect(Collectors.toMap(Wallpaper::getId, w -> w));
        }

        List<Long> categoryIds = wallpaperMap.values().stream()
                .map(Wallpaper::getCategoryId)
                .filter(c -> c != null)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, Category> categoryMap;
        if (!categoryIds.isEmpty()) {
            List<Category> categories = categoryRepository.findAllById(categoryIds);
            categoryMap = categories.stream()
                    .collect(Collectors.toMap(Category::getId, c -> c));
        } else {
            categoryMap = new HashMap<>();
        }

        final Map<Long, Category> finalCategoryMap = categoryMap;

        List<DownloadRecordVO> downloadRecords = new ArrayList<>();

        for (UserAction action : actionPage.getContent()) {
            if (action.getWallpaperId() != null) {
                Wallpaper wallpaper = wallpaperMap.get(action.getWallpaperId());
                if (wallpaper != null && !wallpaper.getIsDel()) {
                    DownloadRecordVO record = new DownloadRecordVO();
                    record.setId(action.getId());
                    record.setWallpaperId(wallpaper.getId());
                    record.setTitle(wallpaper.getTitle());
                    record.setUrl(wallpaper.getUrl());
                    record.setType(wallpaper.getType());
                    record.setCategoryId(wallpaper.getCategoryId());

                    if (wallpaper.getCategoryId() != null) {
                        Category category = finalCategoryMap.get(wallpaper.getCategoryId());
                        if (category != null) {
                            record.setCategory(category.getName());
                        }
                    }

                    record.setDownloadTime(action.getCreateTime());
                    downloadRecords.add(record);
                }
            }
        }

        PageResult<DownloadRecordVO> pageResult = new PageResult<>();
        pageResult.setList(downloadRecords);
        pageResult.setTotal(actionPage.getTotalElements());
        pageResult.setPage(page);
        pageResult.setPageSize(pageSize);

        return Result.success(pageResult);
    }

    /**
     * 删除下载记录
     */
    @DeleteMapping("/download-record/{actionId}")
    public Result<Void> deleteDownloadRecord(@PathVariable @NotNull Long actionId) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(ErrorCode.UNAUTHORIZED);
        }

        UserAction action = userActionRepository.findById(actionId).orElse(null);
        if (action == null) {
            return Result.error(404, "下载记录不存在");
        }

        if (!action.getUserId().equals(userId)) {
            return Result.error(403, "无权限删除该记录");
        }

        if (!"download".equals(action.getActionType())) {
            return Result.error(400, "该记录不是下载记录");
        }

        // 软删除：只标记为已删除
        action.setIsDel(true);
        userActionRepository.save(action);

        return Result.success();
    }

    @GetMapping("/{userId}/uploads")
    public Result<UploadRecordVO> getUploadRecords(
            @PathVariable @NotNull Long userId,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        User user = userService.findById(userId);
        if (user == null) {
            return Result.userNotFound();
        }

        int p = page != null && page > 0 ? page - 1 : 0;
        int ps = pageSize != null && pageSize > 0 ? pageSize : 10;

        Pageable pageable = PageRequest.of(p, ps, Sort.by(Sort.Direction.DESC, "createTime"));

        Page<Work> workPage;
        if (type != null) {
            if (status != null) {
                workPage = worksRepository.findByUserIdAndTypeAndStatus(userId, type, status, pageable);
            } else {
                workPage = worksRepository.findByUserIdAndType(userId, type, pageable);
            }
        } else {
            if (status != null) {
                workPage = worksRepository.findByUserIdAndStatus(userId, status, pageable);
            } else {
                workPage = worksRepository.findByUserId(userId, pageable);
            }
        }

        List<Work> activeWorks = workPage.getContent().stream()
                .filter(w -> !w.getIsDel())
                .collect(Collectors.toList());

        List<Long> categoryIds = activeWorks.stream()
                .map(Work::getCategoryId)
                .filter(c -> c != null)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, Category> categoryMap;
        if (!categoryIds.isEmpty()) {
            List<Category> categories = categoryRepository.findAllById(categoryIds);
            categoryMap = categories.stream()
                    .collect(Collectors.toMap(Category::getId, c -> c));
        } else {
            categoryMap = new HashMap<>();
        }

        final Map<Long, Category> finalCategoryMap = categoryMap;

        List<UploadRecordVO.UploadWallpaperVO> workVOs = activeWorks.stream()
                .map(w -> {
                    UploadRecordVO.UploadWallpaperVO vo = new UploadRecordVO.UploadWallpaperVO();
                    vo.setId(w.getId());
                    vo.setUrl(w.getCoverUrl());
                    vo.setThumbnailUrl(w.getThumbnailUrl());
                    vo.setImageWidth(w.getImageWidth());
                    vo.setImageHeight(w.getImageHeight());
                    vo.setFileFormat(w.getFileFormat());
                    vo.setFileSize(w.getFileSize());
                    vo.setCategoryId(w.getCategoryId());

                    if (w.getCategoryId() != null) {
                        Category category = finalCategoryMap.get(w.getCategoryId());
                        if (category != null) {
                            vo.setCategory(category.getName());
                        }
                    }

                    vo.setUploadTime(w.getCreateTime());
                    vo.setStatus(w.getStatus());
                    vo.setType(w.getType());
                    vo.setRejectReason(w.getRejectReason());
                    return vo;
                })
                .collect(Collectors.toList());

        Long totalCount = worksRepository.countByUserId(userId);
        Long approvedCount = worksRepository.countByUserIdAndStatus(userId, 1);
        Long pendingCount = worksRepository.countByUserIdAndStatus(userId, 0);
        Long rejectedCount = worksRepository.countByUserIdAndStatus(userId, 2);

        UploadRecordVO uploadRecordVO = new UploadRecordVO();
        uploadRecordVO.setTotalCount(totalCount.intValue());
        uploadRecordVO.setApprovedCount(approvedCount.intValue());
        uploadRecordVO.setPendingCount(pendingCount.intValue());
        uploadRecordVO.setRejectedCount(rejectedCount.intValue());
        uploadRecordVO.setWallpapers(workVOs);
        uploadRecordVO.setPage(page);
        uploadRecordVO.setPageSize(pageSize);
        uploadRecordVO.setTotal(workPage.getTotalElements());

        return Result.success(uploadRecordVO);
    }

    @GetMapping("/{userId}/likes")
    public Result<PageResult<LikeRecordVO>> getLikeRecords(
            @PathVariable @NotNull Long userId,
            @RequestParam(defaultValue = "wallpaper") String type,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        User user = userService.findById(userId);
        if (user == null) {
            return Result.userNotFound();
        }

        int p = page != null && page > 0 ? page - 1 : 0;
        int ps = pageSize != null && pageSize > 0 ? pageSize : 10;

        Pageable pageable = PageRequest.of(p, ps, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<UserAction> actionPage = userActionRepository.findByUserIdAndActionTypeAndIsDelOrderByCreateTimeDesc(
                userId, "like", false, pageable);

        // 根据类型过滤
        List<UserAction> filteredActions;
        if ("work".equals(type)) {
            filteredActions = actionPage.getContent().stream()
                    .filter(action -> action.getWorkId() != null)
                    .collect(Collectors.toList());
        } else {
            filteredActions = actionPage.getContent().stream()
                    .filter(action -> action.getWallpaperId() != null)
                    .collect(Collectors.toList());
        }

        List<LikeRecordVO> likeRecords = new ArrayList<>();

        if ("work".equals(type)) {
            // 处理作品集
            List<Long> workIds = filteredActions.stream()
                    .map(UserAction::getWorkId)
                    .filter(w -> w != null)
                    .distinct()
                    .collect(Collectors.toList());

            Map<Long, Work> workMap = new HashMap<>();
            if (!workIds.isEmpty()) {
                List<Work> works = worksRepository.findAllById(workIds);
                workMap = works.stream()
                        .collect(Collectors.toMap(Work::getId, w -> w));
            }

            List<Long> categoryIds = workMap.values().stream()
                    .map(Work::getCategoryId)
                    .filter(c -> c != null)
                    .distinct()
                    .collect(Collectors.toList());

            Map<Long, Category> categoryMap;
            if (!categoryIds.isEmpty()) {
                List<Category> categories = categoryRepository.findAllById(categoryIds);
                categoryMap = categories.stream()
                        .collect(Collectors.toMap(Category::getId, c -> c));
            } else {
                categoryMap = new HashMap<>();
            }

            final Map<Long, Category> finalCategoryMap = categoryMap;

            for (UserAction action : filteredActions) {
                if (action.getWorkId() != null) {
                    Work work = workMap.get(action.getWorkId());
                    if (work != null && !work.getIsDel()) {
                        LikeRecordVO record = new LikeRecordVO();
                        record.setId(action.getId());
                        record.setWallpaperId(work.getId());
                        record.setTitle(work.getTitle());
                        record.setUrl(work.getCoverUrl());
                        record.setThumbnailUrl(work.getThumbnailUrl());
                        record.setImageWidth(work.getImageWidth());
                        record.setImageHeight(work.getImageHeight());
                        record.setFileFormat(work.getFileFormat());
                        record.setFileSize(work.getFileSize());
                        record.setType(work.getType());
                        record.setCategoryId(work.getCategoryId());
                        record.setLikeCount(0);
                        record.setCollectCount(0);

                        if (work.getCategoryId() != null) {
                            Category category = finalCategoryMap.get(work.getCategoryId());
                            if (category != null) {
                                record.setCategory(category.getName());
                            }
                        }

                        record.setLikeTime(action.getCreateTime());
                        likeRecords.add(record);
                    }
                }
            }
        } else {
            // 处理壁纸
            List<Long> wallpaperIds = filteredActions.stream()
                    .map(UserAction::getWallpaperId)
                    .filter(w -> w != null)
                    .distinct()
                    .collect(Collectors.toList());

            Map<Long, Wallpaper> wallpaperMap = new HashMap<>();
            if (!wallpaperIds.isEmpty()) {
                List<Wallpaper> wallpapers = wallpaperRepository.findPublicByIds(wallpaperIds);
                wallpaperMap = wallpapers.stream()
                        .collect(Collectors.toMap(Wallpaper::getId, w -> w));
            }

            List<Long> categoryIds = wallpaperMap.values().stream()
                    .map(Wallpaper::getCategoryId)
                    .filter(c -> c != null)
                    .distinct()
                    .collect(Collectors.toList());

            Map<Long, Category> categoryMap;
            if (!categoryIds.isEmpty()) {
                List<Category> categories = categoryRepository.findAllById(categoryIds);
                categoryMap = categories.stream()
                        .collect(Collectors.toMap(Category::getId, c -> c));
            } else {
                categoryMap = new HashMap<>();
            }

            final Map<Long, Category> finalCategoryMap = categoryMap;

            List<Long> authorIds = wallpaperMap.values().stream()
                    .map(Wallpaper::getUserId)
                    .filter(id -> id != null)
                    .distinct()
                    .collect(Collectors.toList());
            Map<Long, User> authorMap = new HashMap<>();
            if (!authorIds.isEmpty()) {
                List<User> authors = userRepository.findAllById(authorIds);
                authorMap = authors.stream()
                        .collect(Collectors.toMap(User::getId, u -> u));
            }

            for (UserAction action : filteredActions) {
                if (action.getWallpaperId() != null) {
                    Wallpaper wallpaper = wallpaperMap.get(action.getWallpaperId());
                    if (wallpaper != null && !wallpaper.getIsDel()) {
                        LikeRecordVO record = new LikeRecordVO();
                        record.setId(action.getId());
                        record.setWallpaperId(wallpaper.getId());
                        record.setTitle(wallpaper.getTitle());
                        record.setUrl(wallpaper.getUrl());
                        record.setThumbnailUrl(wallpaper.getThumbnailUrl());
                        record.setImageWidth(wallpaper.getImageWidth());
                        record.setImageHeight(wallpaper.getImageHeight());
                        record.setFileFormat(wallpaper.getFileFormat());
                        record.setFileSize(wallpaper.getFileSize());
                        record.setType(wallpaper.getType());
                        record.setCategoryId(wallpaper.getCategoryId());
                        record.setLikeCount(wallpaper.getLikeCount());
                        record.setCollectCount(wallpaper.getCollectCount());
                        record.setDownloadCount(wallpaper.getDownloadCount());
                        record.setIsLiked(true);
                        UserAction collectAction = userActionRepository.findByUserAndWallpaperAndAction(userId,
                                wallpaper.getId(), "collect");
                        record.setIsCollected(collectAction != null);

                        if (wallpaper.getCategoryId() != null) {
                            Category category = finalCategoryMap.get(wallpaper.getCategoryId());
                            if (category != null) {
                                record.setCategory(category.getName());
                            }
                        }

                        if (wallpaper.getUserId() != null) {
                            User author = authorMap.get(wallpaper.getUserId());
                            if (author != null) {
                                LikeRecordVO.AuthorInfo authorInfo = new LikeRecordVO.AuthorInfo();
                                authorInfo.setId(author.getId());
                                authorInfo.setName(
                                        author.getNickname() != null ? author.getNickname() : author.getUsername());
                                authorInfo.setAvatarUrl(author.getAvatar());
                                record.setAuthor(authorInfo);
                            }
                        }

                        record.setLikeTime(action.getCreateTime());
                        likeRecords.add(record);
                    }
                }
            }
        }

        // 统计总数
        Long totalCount;
        if ("work".equals(type)) {
            totalCount = userActionRepository.countByUserIdAndActionTypeAndWorkIdNotNull(userId, "like");
        } else {
            totalCount = userActionRepository.countByUserIdAndActionTypeAndWallpaperIdNotNull(userId, "like");
        }

        PageResult<LikeRecordVO> pageResult = new PageResult<>();
        pageResult.setList(likeRecords);
        pageResult.setTotal(totalCount != null ? totalCount : 0L);
        pageResult.setPage(page);
        pageResult.setPageSize(pageSize);

        return Result.success(pageResult);
    }

    @GetMapping("/{userId}/collects")
    public Result<PageResult<CollectRecordVO>> getCollectRecords(
            @PathVariable @NotNull Long userId,
            @RequestParam(defaultValue = "wallpaper") String type,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        User user = userService.findById(userId);
        if (user == null) {
            return Result.userNotFound();
        }

        int p = page != null && page > 0 ? page - 1 : 0;
        int ps = pageSize != null && pageSize > 0 ? pageSize : 10;

        Pageable pageable = PageRequest.of(p, ps, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<UserAction> actionPage = userActionRepository.findByUserIdAndActionTypeAndIsDelOrderByCreateTimeDesc(
                userId, "collect", false, pageable);

        // 根据类型过滤
        List<UserAction> filteredActions;
        if ("work".equals(type)) {
            filteredActions = actionPage.getContent().stream()
                    .filter(action -> action.getWorkId() != null)
                    .collect(Collectors.toList());
        } else {
            filteredActions = actionPage.getContent().stream()
                    .filter(action -> action.getWallpaperId() != null)
                    .collect(Collectors.toList());
        }

        List<CollectRecordVO> collectRecords = new ArrayList<>();

        if ("work".equals(type)) {
            // 处理作品集
            List<Long> workIds = filteredActions.stream()
                    .map(UserAction::getWorkId)
                    .filter(w -> w != null)
                    .distinct()
                    .collect(Collectors.toList());

            Map<Long, Work> workMap = new HashMap<>();
            if (!workIds.isEmpty()) {
                List<Work> works = worksRepository.findAllById(workIds);
                workMap = works.stream()
                        .collect(Collectors.toMap(Work::getId, w -> w));
            }

            List<Long> categoryIds = workMap.values().stream()
                    .map(Work::getCategoryId)
                    .filter(c -> c != null)
                    .distinct()
                    .collect(Collectors.toList());

            Map<Long, Category> categoryMap;
            if (!categoryIds.isEmpty()) {
                List<Category> categories = categoryRepository.findAllById(categoryIds);
                categoryMap = categories.stream()
                        .collect(Collectors.toMap(Category::getId, c -> c));
            } else {
                categoryMap = new HashMap<>();
            }

            final Map<Long, Category> finalCategoryMap = categoryMap;

            for (UserAction action : filteredActions) {
                if (action.getWorkId() != null) {
                    Work work = workMap.get(action.getWorkId());
                    if (work != null && !work.getIsDel()) {
                        CollectRecordVO record = new CollectRecordVO();
                        record.setId(action.getId());
                        record.setWallpaperId(work.getId());
                        record.setTitle(work.getTitle());
                        record.setUrl(work.getCoverUrl());
                        record.setThumbnailUrl(work.getThumbnailUrl());
                        record.setImageWidth(work.getImageWidth());
                        record.setImageHeight(work.getImageHeight());
                        record.setFileFormat(work.getFileFormat());
                        record.setFileSize(work.getFileSize());
                        record.setType(work.getType());
                        record.setCategoryId(work.getCategoryId());
                        record.setLikeCount(0);
                        record.setCollectCount(0);

                        if (work.getCategoryId() != null) {
                            Category category = finalCategoryMap.get(work.getCategoryId());
                            if (category != null) {
                                record.setCategory(category.getName());
                            }
                        }

                        record.setCollectTime(action.getCreateTime());
                        collectRecords.add(record);
                    }
                }
            }
        } else {
            // 处理壁纸
            List<Long> wallpaperIds = filteredActions.stream()
                    .map(UserAction::getWallpaperId)
                    .filter(w -> w != null)
                    .distinct()
                    .collect(Collectors.toList());

            Map<Long, Wallpaper> wallpaperMap = new HashMap<>();
            if (!wallpaperIds.isEmpty()) {
                List<Wallpaper> wallpapers = wallpaperRepository.findPublicByIds(wallpaperIds);
                wallpaperMap = wallpapers.stream()
                        .collect(Collectors.toMap(Wallpaper::getId, w -> w));
            }

            List<Long> categoryIds = wallpaperMap.values().stream()
                    .map(Wallpaper::getCategoryId)
                    .filter(c -> c != null)
                    .distinct()
                    .collect(Collectors.toList());

            Map<Long, Category> categoryMap;
            if (!categoryIds.isEmpty()) {
                List<Category> categories = categoryRepository.findAllById(categoryIds);
                categoryMap = categories.stream()
                        .collect(Collectors.toMap(Category::getId, c -> c));
            } else {
                categoryMap = new HashMap<>();
            }

            final Map<Long, Category> finalCategoryMap = categoryMap;

            List<Long> authorIds = wallpaperMap.values().stream()
                    .map(Wallpaper::getUserId)
                    .filter(id -> id != null)
                    .distinct()
                    .collect(Collectors.toList());
            Map<Long, User> authorMap = new HashMap<>();
            if (!authorIds.isEmpty()) {
                List<User> authors = userRepository.findAllById(authorIds);
                authorMap = authors.stream()
                        .collect(Collectors.toMap(User::getId, u -> u));
            }

            for (UserAction action : filteredActions) {
                if (action.getWallpaperId() != null) {
                    Wallpaper wallpaper = wallpaperMap.get(action.getWallpaperId());
                    if (wallpaper != null && !wallpaper.getIsDel()) {
                        CollectRecordVO record = new CollectRecordVO();
                        record.setId(action.getId());
                        record.setWallpaperId(wallpaper.getId());
                        record.setTitle(wallpaper.getTitle());
                        record.setUrl(wallpaper.getUrl());
                        record.setThumbnailUrl(wallpaper.getThumbnailUrl());
                        record.setImageWidth(wallpaper.getImageWidth());
                        record.setImageHeight(wallpaper.getImageHeight());
                        record.setFileFormat(wallpaper.getFileFormat());
                        record.setFileSize(wallpaper.getFileSize());
                        record.setType(wallpaper.getType());
                        record.setCategoryId(wallpaper.getCategoryId());
                        record.setLikeCount(wallpaper.getLikeCount());
                        record.setCollectCount(wallpaper.getCollectCount());
                        record.setDownloadCount(wallpaper.getDownloadCount());
                        record.setIsCollected(true);
                        UserAction likeAction = userActionRepository.findByUserAndWallpaperAndAction(userId,
                                wallpaper.getId(), "like");
                        record.setIsLiked(likeAction != null);

                        if (wallpaper.getCategoryId() != null) {
                            Category category = finalCategoryMap.get(wallpaper.getCategoryId());
                            if (category != null) {
                                record.setCategory(category.getName());
                            }
                        }

                        if (wallpaper.getUserId() != null) {
                            User author = authorMap.get(wallpaper.getUserId());
                            if (author != null) {
                                CollectRecordVO.AuthorInfo authorInfo = new CollectRecordVO.AuthorInfo();
                                authorInfo.setId(author.getId());
                                authorInfo.setName(
                                        author.getNickname() != null ? author.getNickname() : author.getUsername());
                                authorInfo.setAvatarUrl(author.getAvatar());
                                record.setAuthor(authorInfo);
                            }
                        }

                        record.setCollectTime(action.getCreateTime());
                        collectRecords.add(record);
                    }
                }
            }
        }

        // 统计总数
        Long totalCount;
        if ("work".equals(type)) {
            totalCount = userActionRepository.countByUserIdAndActionTypeAndWorkIdNotNull(userId, "collect");
        } else {
            totalCount = userActionRepository.countByUserIdAndActionTypeAndWallpaperIdNotNull(userId, "collect");
        }

        PageResult<CollectRecordVO> pageResult = new PageResult<>();
        pageResult.setList(collectRecords);
        pageResult.setTotal(totalCount != null ? totalCount : 0L);
        pageResult.setPage(page);
        pageResult.setPageSize(pageSize);

        return Result.success(pageResult);
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/update")
    public Result<Map<String, Object>> updateUserInfo(
            @RequestBody com.wallpaper.server.dto.UpdateUserInfoRequest request) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(ErrorCode.UNAUTHORIZED);
        }

        User user = userService.findById(userId);
        if (user == null) {
            return Result.userNotFound();
        }

        // 更新昵称
        if (request.getNickname() != null && !request.getNickname().trim().isEmpty()) {
            user.setNickname(request.getNickname().trim());
        }

        // 更新头像
        if (request.getAvatar() != null && !request.getAvatar().trim().isEmpty()) {
            user.setAvatar(request.getAvatar().trim());
        }

        // 更新个人简介
        if (request.getBio() != null) {
            user.setBio(request.getBio().trim());
        }

        User updatedUser = userService.update(user);

        Map<String, Object> data = new HashMap<>();
        data.put("id", updatedUser.getId());
        data.put("username", updatedUser.getUsername());
        data.put("nickname", updatedUser.getNickname());
        data.put("avatar", updatedUser.getAvatar());
        data.put("bio", updatedUser.getBio());
        data.put("role", updatedUser.getRole());
        data.put("isCreator", updatedUser.getIsCreator());
        data.put("followerCount", updatedUser.getFollowerCount());
        data.put("workCount", updatedUser.getWorkCount());

        return Result.success(data);
    }
}