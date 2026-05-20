package com.wallpaper.server.controller;

import com.wallpaper.server.dto.HomeQuery;
import com.wallpaper.server.entity.Category;
import com.wallpaper.server.entity.User;
import com.wallpaper.server.entity.Wallpaper;
import com.wallpaper.server.repository.UserRepository;
import com.wallpaper.server.repository.WallpaperRepository;
import com.wallpaper.server.service.CategoryService;
import com.wallpaper.server.service.UserService;
import com.wallpaper.server.service.WallpaperService;
import com.wallpaper.server.vo.AuthorVO;
import com.wallpaper.server.vo.CategoryVO;
import com.wallpaper.server.vo.HomeVO;
import com.wallpaper.server.vo.WallpaperVO;
import com.wallpaper.server.vo.PageResult;
import com.wallpaper.server.common.Result;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/home")
public class HomeController {
    @Autowired
    private WallpaperService wallpaperService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private WallpaperRepository wallpaperRepository;

    @GetMapping("/data")
    public Result<HomeVO> getHomeData() {
        HomeVO homeVO = new HomeVO();

        homeVO.setCarousel(getHotCategories());
        homeVO.setHotAuthors(getHotAuthors());
        homeVO.setHotAvatars(getHotAvatars());
        homeVO.setHotPcs(getHotPcs());

        return Result.success(homeVO);
    }

    @GetMapping("/daily-recommend")
    public Result<PageResult<WallpaperVO>> getDailyRecommendApi(HomeQuery query) {
        HomeQuery dailyQuery = new HomeQuery();
        dailyQuery.setType(1);

        if (query.getPage() != null) {
            dailyQuery.setPage(query.getPage());
        } else {
            dailyQuery.setPage(1);
        }

        if (query.getPageSize() != null) {
            dailyQuery.setPageSize(query.getPageSize());
        } else {
            dailyQuery.setPageSize(10);
        }

        if (query.getCategoryId() != null) {
            dailyQuery.setCategoryId(query.getCategoryId());
        }

        PageResult<Wallpaper> pageResult = wallpaperService.findByQuery(dailyQuery);
        List<WallpaperVO> voList = pageResult.getList().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return Result.success(
                PageResult.of(voList, pageResult.getTotal(), pageResult.getPage(), pageResult.getPageSize()));
    }

    private List<AuthorVO> getHotAuthors() {
        List<User> users = userRepository.findByIsCreatorTrueOrderByWorkCountDesc();
        return users.stream()
                .limit(5)
                .map(this::convertToAuthorVO)
                .collect(Collectors.toList());
    }

    private AuthorVO convertToAuthorVO(User user) {
        if (user == null) {
            return null;
        }
        AuthorVO vo = new AuthorVO();
        BeanUtils.copyProperties(user, vo);
        vo.setAvatarUrl(user.getAvatar());
        vo.setName(user.getNickname() != null ? user.getNickname() : user.getUsername());
        return vo;
    }

    public List<WallpaperVO> getHotAvatars() {
        return wallpaperService.findHotAvatars().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    public List<WallpaperVO> getHotPcs() {
        return wallpaperService.findHotPcs().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    private WallpaperVO convertToVO(Wallpaper wallpaper) {
        if (wallpaper == null) {
            return null;
        }
        WallpaperVO vo = new WallpaperVO();
        BeanUtils.copyProperties(wallpaper, vo);

        // 设置分类名称
        if (wallpaper.getCategoryId() != null) {
            Category category = categoryService.findById(wallpaper.getCategoryId());
            if (category != null) {
                vo.setCategory(category.getName());
            }
        }

        // 设置作者信息
        if (wallpaper.getUserId() != null) {
            User user = userRepository.findById(wallpaper.getUserId()).orElse(null);
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

    private List<CategoryVO> getHotCategories() {
        List<Category> categories = categoryService.findHotCategories(5);
        return categories.stream()
                .limit(5)
                .map(this::convertToCategoryVO)
                .collect(Collectors.toList());
    }

    private CategoryVO convertToCategoryVO(Category category) {
        if (category == null) {
            return null;
        }
        CategoryVO vo = new CategoryVO();
        BeanUtils.copyProperties(category, vo);
        vo.setWallpaperCount(wallpaperRepository.countPublicVisibleByCategoryId(category.getId()).intValue());
        return vo;
    }
}