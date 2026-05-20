package com.wallpaper.server.service;

import com.wallpaper.server.dto.HomeQuery;
import com.wallpaper.server.dto.WallpaperQuery;
import com.wallpaper.server.entity.Category;
import com.wallpaper.server.entity.User;
import com.wallpaper.server.entity.Wallpaper;
import com.wallpaper.server.repository.UserRepository;
import com.wallpaper.server.repository.WallpaperRepository;
import com.wallpaper.server.service.CategoryService;
import com.wallpaper.server.vo.WallpaperVO;
import com.wallpaper.server.vo.PageResult;
import com.wallpaper.server.entity.UserAction;
import com.wallpaper.server.repository.UserActionRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class WallpaperService {
    @Autowired
    private WallpaperRepository wallpaperRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private UserActionRepository userActionRepository;

    private static final String CACHE_NAME = "wallpapers";

    public List<WallpaperVO> findAll(WallpaperQuery query) {
        return convertToVOWithUserBatch(findAllEntities(query), query.getUserId());
    }

    public PageResult<WallpaperVO> findPageResultVO(WallpaperQuery query) {
        PageResult<Wallpaper> pageResult = findPageResult(query);
        List<WallpaperVO> voList = convertToVOWithUserBatch(pageResult.getList(), query.getUserId());

        return PageResult.of(voList, pageResult.getTotal(), pageResult.getPage(), pageResult.getPageSize());
    }

    public List<Wallpaper> findAllEntities(WallpaperQuery query) {
        int page = query.getPage() != null && query.getPage() > 0 ? query.getPage() - 1 : 0;
        int pageSize = query.getPageSize() != null && query.getPageSize() > 0 ? query.getPageSize() : 10;

        Sort sort = Sort.by(Sort.Direction.DESC, "createTime");
        Pageable pageable = PageRequest.of(page, pageSize, sort);

        Page<Wallpaper> pageResult;

        if (query.getCategoryId() != null && query.getType() != null) {
            pageResult = wallpaperRepository.findByCategoryIdAndTypeWithUser(query.getCategoryId(), query.getType(),
                    pageable);
        } else if (query.getCategoryId() != null) {
            pageResult = wallpaperRepository.findByCategoryIdWithUser(query.getCategoryId(), pageable);
        } else if (query.getType() != null) {
            pageResult = wallpaperRepository.findByTypeWithUser(query.getType(), pageable);
        } else if (query.getUserId() != null) {
            pageResult = wallpaperRepository.findByUserIdWithUser(query.getUserId(), pageable);
        } else if (query.getKeyword() != null && !query.getKeyword().isEmpty()) {
            List<Wallpaper> searchResults = wallpaperRepository.searchByKeyword(query.getKeyword());
            Map<Long, User> userMap = batchGetUsers(searchResults);
            return searchResults.subList(
                    Math.min(page * pageSize, searchResults.size()),
                    Math.min((page + 1) * pageSize, searchResults.size()));
        } else {
            pageResult = wallpaperRepository.findAllWithUser(pageable);
        }

        return pageResult.getContent();
    }

    public PageResult<Wallpaper> findPageResult(WallpaperQuery query) {
        int page = query.getPage() != null && query.getPage() > 0 ? query.getPage() - 1 : 0;
        int pageSize = query.getPageSize() != null && query.getPageSize() > 0 ? query.getPageSize() : 10;

        Sort sort = Sort.by(Sort.Direction.DESC, "createTime");
        Pageable pageable = PageRequest.of(page, pageSize, sort);

        Page<Wallpaper> pageResult;

        if (query.getKeyword() != null && !query.getKeyword().isEmpty()) {
            if (query.getType() != null) {
                pageResult = wallpaperRepository.findByCategoryNameLikeAndTypeWithUser(query.getKeyword(),
                        query.getType(),
                        pageable);
            } else {
                pageResult = wallpaperRepository.findByCategoryNameLikeWithUser(query.getKeyword(), pageable);
            }
        } else if (query.getCategoryId() != null && query.getType() != null) {
            pageResult = wallpaperRepository.findByCategoryIdAndTypeWithUser(query.getCategoryId(), query.getType(),
                    pageable);
        } else if (query.getCategoryId() != null) {
            pageResult = wallpaperRepository.findByCategoryIdWithUser(query.getCategoryId(), pageable);
        } else if (query.getType() != null) {
            pageResult = wallpaperRepository.findByTypeWithUser(query.getType(), pageable);
        } else if (query.getUserId() != null) {
            pageResult = wallpaperRepository.findByUserIdWithUser(query.getUserId(), pageable);
        } else {
            pageResult = wallpaperRepository.findAllWithUser(pageable);
        }

        return PageResult.of(pageResult.getContent(), pageResult.getTotalElements(), query.getPage(), pageSize);
    }

    private Map<Long, User> batchGetUsers(List<Wallpaper> wallpapers) {
        List<Long> userIds = wallpapers.stream()
                .map(Wallpaper::getUserId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());

        if (userIds.isEmpty()) {
            return new HashMap<>();
        }

        List<User> users = userRepository.findByIdIn(userIds);
        Map<Long, User> userMap = new HashMap<>();
        for (User user : users) {
            userMap.put(user.getId(), user);
        }
        return userMap;
    }

    public PageResult<Wallpaper> findByQuery(HomeQuery query) {
        int page = query.getPage() != null && query.getPage() > 0 ? query.getPage() - 1 : 0;
        int pageSize = query.getPageSize() != null && query.getPageSize() > 0 ? query.getPageSize() : 10;
        int limit = query.getLimit() != null && query.getLimit() > 0 ? query.getLimit() : pageSize;

        Sort sort = Sort.by(Sort.Direction.DESC, "createTime");
        if ("collect".equals(query.getSortBy())) {
            sort = Sort.by(Sort.Direction.DESC, "collectCount");
        } else if ("download".equals(query.getSortBy())) {
            sort = Sort.by(Sort.Direction.DESC, "downloadCount");
        }

        Pageable pageable = PageRequest.of(page, limit, sort);

        Page<Wallpaper> pageResult;
        if (query.getType() != null) {
            pageResult = wallpaperRepository.findByTypeWithUser(query.getType(), pageable);
        } else {
            pageResult = wallpaperRepository.findAllWithUser(pageable);
        }

        return PageResult.of(pageResult.getContent(), pageResult.getTotalElements(), query.getPage(), limit);
    }

    @Cacheable(value = CACHE_NAME, key = "'hotAvatars'")
    public List<Wallpaper> findHotAvatars() {
        Pageable pageable = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "likeCount"));
        return wallpaperRepository.findByTypeWithUser(3, pageable).getContent();
    }

    @Cacheable(value = CACHE_NAME, key = "'hotPcs'")
    public List<Wallpaper> findHotPcs() {
        Pageable pageable = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "likeCount"));
        return wallpaperRepository.findByTypeWithUser(2, pageable).getContent();
    }

    @Cacheable(value = CACHE_NAME, key = "#id")
    public WallpaperVO findById(Long id) {
        return convertToVO(wallpaperRepository.findPublicById(id).orElse(null));
    }

    public Wallpaper findPublicByIdEntity(Long id) {
        return wallpaperRepository.findPublicById(id).orElse(null);
    }

    public Wallpaper findByIdEntity(Long id) {
        return wallpaperRepository.findById(id).orElse(null);
    }

    @CacheEvict(value = CACHE_NAME, allEntries = true)
    public Wallpaper create(Wallpaper wallpaper) {
        if (wallpaper.getUserId() != null) {
            User user = userService.findById(wallpaper.getUserId());
            if (user == null) {
                throw new IllegalArgumentException("用户不存在");
            }
        }

        if (wallpaper.getCategoryId() != null) {
            Category category = categoryService.findById(wallpaper.getCategoryId());
            if (category == null) {
                throw new IllegalArgumentException("分类不存在");
            }
        }

        wallpaper.setCreateTime(LocalDateTime.now());
        wallpaper.setUpdateTime(LocalDateTime.now());
        return wallpaperRepository.save(wallpaper);
    }

    @CacheEvict(value = CACHE_NAME, key = "#wallpaper.id")
    public Wallpaper update(Wallpaper wallpaper) {
        wallpaper.setUpdateTime(LocalDateTime.now());
        return wallpaperRepository.save(wallpaper);
    }

    @CacheEvict(value = CACHE_NAME, key = "#id")
    public void delete(Long id) {
        wallpaperRepository.softDelete(id);
    }

    public PageResult<WallpaperVO> findByCategoryId(Long categoryId, String type, Integer page, Integer pageSize) {
        int p = page != null && page > 0 ? page - 1 : 0;
        int ps = pageSize != null && pageSize > 0 ? pageSize : 10;

        Pageable pageable = PageRequest.of(p, ps, Sort.by(Sort.Direction.DESC, "createTime"));

        org.springframework.data.domain.Page<Wallpaper> pageResult;
        if (type != null && !type.isEmpty()) {
            Integer typeInt = Integer.parseInt(type);
            pageResult = wallpaperRepository.findByCategoryIdAndType(categoryId, typeInt, pageable);
        } else {
            pageResult = wallpaperRepository.findByCategoryIdOrderByCreateTimeDesc(categoryId, pageable);
        }

        Category category = categoryService.findById(categoryId);
        String categoryName = category != null ? category.getName() : null;

        List<WallpaperVO> wallpaperVOs = pageResult.getContent().stream()
                .map(w -> {
                    WallpaperVO vo = convertToVOWithUser(w);
                    if (vo != null && categoryName != null) {
                        vo.setCategory(categoryName);
                    }
                    return vo;
                })
                .collect(Collectors.toList());

        return PageResult.of(wallpaperVOs, pageResult.getTotalElements(), page, pageSize);
    }

    public List<Wallpaper> findByUserId(Long userId, String type, Integer page, Integer pageSize) {
        int p = page != null && page > 0 ? page - 1 : 0;
        int ps = pageSize != null && pageSize > 0 ? pageSize : 10;

        Pageable pageable = PageRequest.of(p, ps, Sort.by(Sort.Direction.DESC, "createTime"));

        Page<Wallpaper> pageResult;
        if (type != null && !type.isEmpty()) {
            Integer typeInt = Integer.parseInt(type);
            pageResult = wallpaperRepository.findPublicByUserIdAndType(userId, typeInt, pageable);
        } else {
            pageResult = wallpaperRepository.findByUserIdWithUser(userId, pageable);
        }

        return pageResult.getContent();
    }

    private WallpaperVO convertToVO(Wallpaper wallpaper) {
        if (wallpaper == null) {
            return null;
        }
        WallpaperVO vo = new WallpaperVO();
        BeanUtils.copyProperties(wallpaper, vo);
        return vo;
    }

    private List<WallpaperVO> convertToVOWithUser(List<Wallpaper> wallpapers) {
        return wallpapers.stream()
                .map(this::convertToVOWithUser)
                .collect(Collectors.toList());
    }

    private WallpaperVO convertToVOWithUser(Wallpaper wallpaper) {
        if (wallpaper == null) {
            return null;
        }
        WallpaperVO vo = new WallpaperVO();
        BeanUtils.copyProperties(wallpaper, vo);

        if (wallpaper.getUserId() != null) {
            User user = userService.findById(wallpaper.getUserId());
            if (user != null) {
                WallpaperVO.AuthorInfo authorInfo = new WallpaperVO.AuthorInfo();
                authorInfo.setId(user.getId());
                authorInfo.setName(user.getNickname() != null ? user.getNickname() : user.getUsername());
                authorInfo.setAvatarUrl(user.getAvatar());
                vo.setAuthor(authorInfo);
            }
        }

        return vo;
    }

    private List<WallpaperVO> convertToVOWithUserBatch(List<Wallpaper> wallpapers, Long currentUserId) {
        Map<Long, User> userMap = batchGetUsers(wallpapers);

        List<Long> categoryIds = wallpapers.stream()
                .map(Wallpaper::getCategoryId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, Category> categoryMap;
        if (!categoryIds.isEmpty()) {
            List<Category> categories = categoryService.findByIds(categoryIds);
            categoryMap = categories.stream()
                    .collect(Collectors.toMap(Category::getId, c -> c));
        } else {
            categoryMap = new HashMap<>();
        }

        Map<Long, Boolean> likedMap = new HashMap<>();
        Map<Long, Boolean> collectedMap = new HashMap<>();
        if (currentUserId != null) {
            List<Long> wallpaperIds = wallpapers.stream().map(Wallpaper::getId).collect(Collectors.toList());
            for (Long wid : wallpaperIds) {
                UserAction likeAction = userActionRepository.findByUserAndWallpaperAndAction(currentUserId, wid,
                        "like");
                likedMap.put(wid, likeAction != null);
                UserAction collectAction = userActionRepository.findByUserAndWallpaperAndAction(currentUserId, wid,
                        "collect");
                collectedMap.put(wid, collectAction != null);
            }
        }

        final Map<Long, Boolean> finalLikedMap = likedMap;
        final Map<Long, Boolean> finalCollectedMap = collectedMap;

        return wallpapers.stream()
                .map(w -> {
                    WallpaperVO vo = new WallpaperVO();
                    BeanUtils.copyProperties(w, vo);

                    if (w.getUserId() != null) {
                        User user = userMap.get(w.getUserId());
                        if (user != null) {
                            WallpaperVO.AuthorInfo authorInfo = new WallpaperVO.AuthorInfo();
                            authorInfo.setId(user.getId());
                            authorInfo.setName(user.getNickname() != null ? user.getNickname() : user.getUsername());
                            authorInfo.setAvatarUrl(user.getAvatar());
                            vo.setAuthor(authorInfo);
                        }
                    }

                    if (w.getCategoryId() != null) {
                        Category category = categoryMap.get(w.getCategoryId());
                        if (category != null) {
                            vo.setCategory(category.getName());
                        }
                    }

                    if (currentUserId != null) {
                        vo.setIsLiked(finalLikedMap.getOrDefault(w.getId(), false));
                        vo.setIsCollected(finalCollectedMap.getOrDefault(w.getId(), false));
                    }

                    return vo;
                })
                .collect(Collectors.toList());
    }

    private List<WallpaperVO> convertToVO(List<Wallpaper> wallpapers) {
        return wallpapers.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }
}