package com.wallpaper.server.service;

import com.wallpaper.server.entity.Category;
import com.wallpaper.server.repository.CategoryRepository;
import com.wallpaper.server.repository.WallpaperRepository;
import com.wallpaper.server.vo.CategoryVO;
import com.wallpaper.server.vo.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CategoryService {
    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private WallpaperRepository wallpaperRepository;

    private static final String CACHE_NAME = "categories";

    @Cacheable(value = CACHE_NAME, key = "'all'")
    public List<Category> findAll() {
        return categoryRepository.findByIsDelFalseOrderBySortOrderAsc();
    }

    public PageResult<Category> findAll(Integer page, Integer pageSize) {
        int p = page != null && page > 0 ? page - 1 : 0;
        int ps = pageSize != null && pageSize > 0 ? pageSize : 10;

        Pageable pageable = PageRequest.of(p, ps, Sort.by(Sort.Direction.ASC, "sortOrder"));
        Page<Category> pageResult = categoryRepository.findByIsDelFalseOrderBySortOrderAsc(pageable);

        return PageResult.of(pageResult.getContent(), pageResult.getTotalElements(), page, pageSize);
    }

    @Cacheable(value = CACHE_NAME, key = "'page:' + #page + ':' + #pageSize")
    public PageResult<CategoryVO> findAllWithWallpaperCount(Integer page, Integer pageSize) {
        int p = page != null && page > 0 ? page - 1 : 0;
        int ps = pageSize != null && pageSize > 0 ? pageSize : 10;

        Pageable pageable = PageRequest.of(p, ps, Sort.by(Sort.Direction.ASC, "sortOrder"));
        Page<Category> pageResult = categoryRepository.findByIsDelFalseOrderBySortOrderAsc(pageable);

        List<Category> categories = pageResult.getContent();

        Map<Long, Long> wallpaperCountMap = categories.stream()
                .collect(Collectors.toMap(
                        Category::getId,
                        category -> wallpaperRepository.countPublicVisibleByCategoryId(category.getId())));

        List<CategoryVO> categoryVOs = categories.stream()
                .map(category -> {
                    CategoryVO vo = new CategoryVO();
                    vo.setId(category.getId());
                    vo.setName(category.getName());
                    vo.setAlias(category.getAlias());
                    vo.setIconPath(category.getIconPath());
                    vo.setCoverUrl(category.getCoverUrl());
                    vo.setLikeCount(category.getLikeCount());
                    vo.setSortOrder(category.getSortOrder());
                    vo.setWallpaperCount(wallpaperCountMap.get(category.getId()).intValue());
                    return vo;
                })
                .collect(Collectors.toList());

        return PageResult.of(categoryVOs, pageResult.getTotalElements(), page, pageSize);
    }

    public List<Category> searchByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return findAll();
        }
        return categoryRepository.findByIsDelFalseAndNameContainingIgnoreCase(name.trim());
    }

    public PageResult<Category> searchByName(String name, Integer page, Integer pageSize) {
        if (name == null || name.trim().isEmpty()) {
            return findAll(page, pageSize);
        }

        int p = page != null && page > 0 ? page - 1 : 0;
        int ps = pageSize != null && pageSize > 0 ? pageSize : 10;

        Pageable pageable = PageRequest.of(p, ps, Sort.by(Sort.Direction.ASC, "sortOrder"));
        Page<Category> pageResult = categoryRepository.findByIsDelFalseAndNameContainingIgnoreCase(name.trim(),
                pageable);

        return PageResult.of(pageResult.getContent(), pageResult.getTotalElements(), page, pageSize);
    }

    @Cacheable(value = CACHE_NAME, key = "'hot:' + #limit")
    public List<Category> findHotCategories(Integer limit) {
        List<Category> categories = categoryRepository.findByIsDelFalseOrderBySortOrderDesc();
        if (limit == null || limit <= 0) {
            limit = 5;
        }
        return categories.stream().limit(limit).collect(java.util.stream.Collectors.toList());
    }

    @Cacheable(value = CACHE_NAME, key = "#id")
    public Category findById(Long id) {
        return categoryRepository.findById(id).orElse(null);
    }

    public List<Category> findByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new java.util.ArrayList<>();
        }
        return categoryRepository.findAllById(ids);
    }

    @CacheEvict(value = CACHE_NAME, allEntries = true)
    public Category create(Category category) {
        category.setCreateTime(LocalDateTime.now());
        category.setUpdateTime(LocalDateTime.now());
        return categoryRepository.save(category);
    }

    @CacheEvict(value = CACHE_NAME, allEntries = true)
    public Category update(Category category) {
        category.setUpdateTime(LocalDateTime.now());
        return categoryRepository.save(category);
    }

    @CacheEvict(value = CACHE_NAME, allEntries = true)
    public void delete(Long id) {
        categoryRepository.deleteById(id);
    }

    @CacheEvict(value = CACHE_NAME, key = "#categoryId")
    public void incrementLikeCount(Long categoryId) {
        Category category = categoryRepository.findById(categoryId).orElse(null);
        if (category != null) {
            category.setLikeCount(category.getLikeCount() + 1);
            categoryRepository.save(category);
        }
    }

    @CacheEvict(value = CACHE_NAME, key = "#categoryId")
    public void decrementLikeCount(Long categoryId) {
        Category category = categoryRepository.findById(categoryId).orElse(null);
        if (category != null && category.getLikeCount() > 0) {
            category.setLikeCount(category.getLikeCount() - 1);
            categoryRepository.save(category);
        }
    }
}