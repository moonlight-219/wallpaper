package com.wallpaper.server.controller;

import com.wallpaper.server.entity.Category;
import com.wallpaper.server.entity.UserAction;
import com.wallpaper.server.entity.Wallpaper;
import com.wallpaper.server.repository.UserActionRepository;
import com.wallpaper.server.repository.WallpaperRepository;
import com.wallpaper.server.service.CategoryService;
import com.wallpaper.server.service.WallpaperService;
import com.wallpaper.server.util.UserContext;
import com.wallpaper.server.common.Result;
import com.wallpaper.server.vo.CategoryDetailVO;
import com.wallpaper.server.vo.CategoryVO;
import com.wallpaper.server.vo.WallpaperVO;
import com.wallpaper.server.vo.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/category")
@Validated
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    @Autowired
    private UserActionRepository userActionRepository;

    @Autowired
    private WallpaperService wallpaperService;

    @Autowired
    private WallpaperRepository wallpaperRepository;

    @GetMapping("/list")
    public Result<PageResult<CategoryVO>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResult<CategoryVO> categories = categoryService.findAllWithWallpaperCount(page, pageSize);
        return Result.success(categories);
    }

    @GetMapping
    public Result<PageResult<CategoryVO>> list(
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResult<CategoryVO> categories;
        if (name != null && !name.trim().isEmpty()) {
            // 搜索功能暂时不包含壁纸数量，保持原有逻辑
            PageResult<Category> categoryPage = categoryService.searchByName(name, page, pageSize);
            List<CategoryVO> categoryVOs = categoryPage.getList().stream()
                    .map(category -> {
                        CategoryVO vo = new CategoryVO();
                        vo.setId(category.getId());
                        vo.setName(category.getName());
                        vo.setAlias(category.getAlias());
                        vo.setIconPath(category.getIconPath());
                        vo.setCoverUrl(category.getCoverUrl());
                        vo.setLikeCount(category.getLikeCount());
                        vo.setSortOrder(category.getSortOrder());
                        vo.setWallpaperCount(0); // 搜索时暂不统计壁纸数量
                        return vo;
                    })
                    .collect(Collectors.toList());
            categories = PageResult.of(categoryVOs, categoryPage.getTotal(), categoryPage.getPage(),
                    categoryPage.getPageSize());
        } else {
            categories = categoryService.findAllWithWallpaperCount(page, pageSize);
        }
        return Result.success(categories);
    }

    @GetMapping("/hot")
    public Result<List<Category>> getHotCategories(@RequestParam(defaultValue = "5") Integer limit) {
        List<Category> categories = categoryService.findHotCategories(limit);
        return Result.success(categories);
    }

    @GetMapping("/{id}")
    public Result<Category> getById(@PathVariable @NotNull Long id) {
        Category category = categoryService.findById(id);
        if (category == null) {
            return Result.categoryNotFound();
        }
        return Result.success(category);
    }

    @PostMapping
    public Result<Category> create(@Valid @RequestBody Category category) {
        Category newCategory = categoryService.create(category);
        return Result.success(newCategory);
    }

    @PutMapping("/{id}")
    public Result<Category> update(@PathVariable @NotNull Long id, @RequestBody Category category) {
        category.setId(id);
        Category updatedCategory = categoryService.update(category);
        return Result.success(updatedCategory);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable @NotNull Long id) {
        categoryService.delete(id);
        return Result.success();
    }

    @GetMapping("/{id}/detail")
    public Result<CategoryDetailVO> getCategoryDetail(
            @PathVariable @NotNull Long id,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Category category = categoryService.findById(id);
        if (category == null) {
            return Result.categoryNotFound();
        }

        PageResult<WallpaperVO> wallpaperPage = wallpaperService.findByCategoryId(id, type, page, pageSize);
        Long wallpaperCount = wallpaperRepository.countPublicVisibleByCategoryId(id);

        CategoryDetailVO detailVO = new CategoryDetailVO();
        detailVO.setId(category.getId());
        detailVO.setName(category.getName());
        detailVO.setAlias(category.getAlias());
        detailVO.setIconPath(category.getIconPath());
        detailVO.setCoverUrl(category.getCoverUrl());
        detailVO.setLikeCount(category.getLikeCount());
        detailVO.setSortOrder(category.getSortOrder());
        detailVO.setWallpaperCount(wallpaperCount);
        detailVO.setWallpapers(wallpaperPage.getList());
        detailVO.setTotal(wallpaperPage.getTotal());
        detailVO.setPage(wallpaperPage.getPage());
        detailVO.setPageSize(wallpaperPage.getPageSize());

        return Result.success(detailVO);
    }

    @PostMapping("/like/{id}")
    @Transactional
    public Result<Void> like(@PathVariable @NotNull Long id) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "未登录");
        }

        Category category = categoryService.findById(id);
        if (category == null || category.getIsDel()) {
            return Result.categoryNotFound();
        }

        UserAction existing = userActionRepository.findByUserAndCategoryAndAction(userId, id, "like");

        if (existing == null) {
            UserAction action = new UserAction();
            action.setUserId(userId);
            action.setCategoryId(id);
            action.setActionType("like");
            action.setCreateTime(java.time.LocalDateTime.now());
            userActionRepository.save(action);

            categoryService.incrementLikeCount(id);
        } else {
            userActionRepository.delete(existing);
            categoryService.decrementLikeCount(id);
        }

        return Result.success();
    }
}