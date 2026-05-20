package com.wallpaper.server.service;

import com.wallpaper.server.repository.WallpaperRepository;
import com.wallpaper.server.repository.UserRepository;
import com.wallpaper.server.repository.CategoryRepository;
import com.wallpaper.server.repository.WorksRepository;
import com.wallpaper.server.entity.User;
import com.wallpaper.server.vo.DashboardStatsVO;
import com.wallpaper.server.vo.DashboardVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    @Autowired
    private WallpaperRepository wallpaperRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private WorksRepository worksRepository;

    public DashboardStatsVO getStats() {
        DashboardStatsVO stats = new DashboardStatsVO();

        LocalDateTime now = LocalDateTime.now();

        LocalDateTime thisMonthStart = now.toLocalDate().withDayOfMonth(1).atStartOfDay();
        LocalDateTime thisMonthEnd = now.toLocalDate().atTime(23, 59, 59);

        LocalDateTime lastMonthStart = now.minusMonths(1).toLocalDate().withDayOfMonth(1).atStartOfDay();
        LocalDateTime lastMonthEnd = now.toLocalDate().withDayOfMonth(1).atStartOfDay().minusSeconds(1);

        Long wallpaperCount = wallpaperRepository.count();
        Long authorCount = userRepository.countByIsCreatorTrue();
        Long categoryCount = categoryRepository.countByIsDelFalse();

        Long thisMonthWallpaperCount = wallpaperRepository.countByIsDelFalseAndCreateTimeBetween(thisMonthStart,
                thisMonthEnd);
        Long thisMonthAuthorCount = userRepository.countByCreateTimeBetween(thisMonthStart, thisMonthEnd);
        Long thisMonthCategoryCount = categoryRepository.countByCreateTimeBetween(thisMonthStart, thisMonthEnd);

        Long lastMonthWallpaperCount = wallpaperRepository.countByIsDelFalseAndCreateTimeBetween(lastMonthStart,
                lastMonthEnd);
        Long lastMonthAuthorCount = userRepository.countByCreateTimeBetween(lastMonthStart, lastMonthEnd);
        Long lastMonthCategoryCount = categoryRepository.countByCreateTimeBetween(lastMonthStart, lastMonthEnd);

        stats.setWallpapers(wallpaperCount);
        stats.setAuthors(authorCount);
        stats.setCategories(categoryCount);

        double wallpaperGrowth = 0.0;
        if (lastMonthWallpaperCount != null && lastMonthWallpaperCount > 0) {
            wallpaperGrowth = ((thisMonthWallpaperCount != null ? thisMonthWallpaperCount : 0)
                    - lastMonthWallpaperCount)
                    / lastMonthWallpaperCount.doubleValue() * 100;
        } else if (thisMonthWallpaperCount != null && thisMonthWallpaperCount > 0) {
            wallpaperGrowth = 100.0;
        }

        double authorGrowth = 0.0;
        if (lastMonthAuthorCount != null && lastMonthAuthorCount > 0) {
            authorGrowth = ((thisMonthAuthorCount != null ? thisMonthAuthorCount : 0) - lastMonthAuthorCount)
                    / lastMonthAuthorCount.doubleValue() * 100;
        } else if (thisMonthAuthorCount != null && thisMonthAuthorCount > 0) {
            authorGrowth = 100.0;
        }

        double categoryGrowth = 0.0;
        if (lastMonthCategoryCount != null && lastMonthCategoryCount > 0) {
            categoryGrowth = ((thisMonthCategoryCount != null ? thisMonthCategoryCount : 0) - lastMonthCategoryCount)
                    / lastMonthCategoryCount.doubleValue() * 100;
        } else if (thisMonthCategoryCount != null && thisMonthCategoryCount > 0) {
            categoryGrowth = 100.0;
        }

        stats.setWallpaperGrowth(wallpaperGrowth);
        stats.setAuthorGrowth(authorGrowth);
        stats.setCategoryGrowth(categoryGrowth);

        return stats;
    }

    public DashboardVO getDashboardData() {
        DashboardVO dashboard = new DashboardVO();

        dashboard.setTypeDistribution(getTypeDistribution());
        dashboard.setTopAuthors(getTopAuthors());
        dashboard.setUploadTrend(getUploadTrend());
        dashboard.setMonthlyCompare(getMonthlyCompare());
        dashboard.setCategoryTrend(getCategoryTrend());

        return dashboard;
    }

    private List<DashboardVO.TypeDistribution> getTypeDistribution() {
        List<DashboardVO.TypeDistribution> distribution = new ArrayList<>();

        Long phoneCount = wallpaperRepository.countByType(1);
        Long tabletCount = wallpaperRepository.countByType(2);
        Long avatarCount = wallpaperRepository.countByType(3);

        DashboardVO.TypeDistribution phone = new DashboardVO.TypeDistribution();
        phone.setName("手机壁纸");
        phone.setValue(phoneCount);
        distribution.add(phone);

        DashboardVO.TypeDistribution tablet = new DashboardVO.TypeDistribution();
        tablet.setName("平板壁纸");
        tablet.setValue(tabletCount);
        distribution.add(tablet);

        DashboardVO.TypeDistribution avatar = new DashboardVO.TypeDistribution();
        avatar.setName("头像");
        avatar.setValue(avatarCount);
        distribution.add(avatar);

        return distribution;
    }

    private List<DashboardVO.TopAuthor> getTopAuthors() {
        List<com.wallpaper.server.vo.UserWorkCount> userWorkCounts = worksRepository.findTopAuthorsByWorkCount();

        return userWorkCounts.stream()
                .limit(10)
                .map(userWorkCount -> {
                    User user = userRepository.findById(userWorkCount.getUserId()).orElse(null);
                    if (user == null) {
                        return null;
                    }

                    DashboardVO.TopAuthor author = new DashboardVO.TopAuthor();
                    author.setName(user.getNickname() != null ? user.getNickname() : user.getUsername());
                    author.setCount(userWorkCount.getCount() != null ? userWorkCount.getCount().intValue() : 0);
                    return author;
                })
                .filter(author -> author != null)
                .collect(Collectors.toList());
    }

    private Map<String, DashboardVO.UploadTrend> getUploadTrend() {
        Map<String, DashboardVO.UploadTrend> trend = new HashMap<>();

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        DashboardVO.UploadTrend weekTrend = new DashboardVO.UploadTrend();
        weekTrend.setDates(getWeekDates(now, formatter));
        weekTrend.setValues(getWeekUploadCounts(now));
        trend.put("week", weekTrend);

        DashboardVO.UploadTrend monthTrend = new DashboardVO.UploadTrend();
        monthTrend.setDates(getMonthDates(now, formatter));
        monthTrend.setValues(getMonthUploadCounts(now));
        trend.put("month", monthTrend);

        DashboardVO.UploadTrend yearTrend = new DashboardVO.UploadTrend();
        yearTrend.setDates(getYearDates(now, formatter));
        yearTrend.setValues(getYearUploadCounts(now));
        trend.put("year", yearTrend);

        return trend;
    }

    private List<String> getWeekDates(LocalDateTime now, DateTimeFormatter formatter) {
        List<String> dates = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            dates.add(now.minusDays(i).format(formatter));
        }
        return dates;
    }

    private List<Integer> getWeekUploadCounts(LocalDateTime now) {
        List<Integer> counts = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDateTime day = now.minusDays(i);
            LocalDateTime startOfDay = day.toLocalDate().atStartOfDay();
            LocalDateTime endOfDay = day.toLocalDate().atTime(23, 59, 59);
            Long count = wallpaperRepository.countByIsDelFalseAndCreateTimeBetween(startOfDay, endOfDay);
            counts.add(count != null ? count.intValue() : 0);
        }
        return counts;
    }

    private List<String> getMonthDates(LocalDateTime now, DateTimeFormatter formatter) {
        List<String> dates = new ArrayList<>();
        for (int i = 29; i >= 0; i--) {
            dates.add(now.minusDays(i).format(formatter));
        }
        return dates;
    }

    private List<Integer> getMonthUploadCounts(LocalDateTime now) {
        List<Integer> counts = new ArrayList<>();
        for (int i = 29; i >= 0; i--) {
            LocalDateTime day = now.minusDays(i);
            LocalDateTime startOfDay = day.toLocalDate().atStartOfDay();
            LocalDateTime endOfDay = day.toLocalDate().atTime(23, 59, 59);
            Long count = wallpaperRepository.countByIsDelFalseAndCreateTimeBetween(startOfDay, endOfDay);
            counts.add(count != null ? count.intValue() : 0);
        }
        return counts;
    }

    private List<String> getYearDates(LocalDateTime now, DateTimeFormatter formatter) {
        List<String> dates = new ArrayList<>();
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("yyyy-MM");
        for (int i = 11; i >= 0; i--) {
            dates.add(now.minusMonths(i).format(monthFormatter));
        }
        return dates;
    }

    private List<Integer> getYearUploadCounts(LocalDateTime now) {
        List<Integer> counts = new ArrayList<>();
        for (int i = 11; i >= 0; i--) {
            LocalDateTime monthStart = now.minusMonths(i).toLocalDate().atStartOfDay();
            LocalDateTime monthEnd = now.minusMonths(i).plusMonths(1).toLocalDate().atStartOfDay().minusSeconds(1);
            Long count = wallpaperRepository.countByIsDelFalseAndCreateTimeBetween(monthStart, monthEnd);
            counts.add(count != null ? count.intValue() : 0);
        }
        return counts;
    }

    private DashboardVO.MonthlyCompare getMonthlyCompare() {
        DashboardVO.MonthlyCompare compare = new DashboardVO.MonthlyCompare();

        LocalDateTime now = LocalDateTime.now();

        List<Integer> currentMonth = new ArrayList<>();
        List<Integer> previousMonth = new ArrayList<>();

        for (int i = 0; i < 12; i++) {
            LocalDateTime monthStart = now.minusMonths(11 - i).toLocalDate().atStartOfDay();
            LocalDateTime monthEnd = now.minusMonths(11 - i).plusMonths(1).toLocalDate().atStartOfDay().minusSeconds(1);
            Long count = wallpaperRepository.countByIsDelFalseAndCreateTimeBetween(monthStart, monthEnd);
            currentMonth.add(count != null ? count.intValue() : 0);

            LocalDateTime prevMonthStart = now.minusMonths(12 - i).toLocalDate().atStartOfDay();
            LocalDateTime prevMonthEnd = now.minusMonths(11 - i).toLocalDate().atStartOfDay().minusSeconds(1);
            Long prevCount = wallpaperRepository.countByIsDelFalseAndCreateTimeBetween(prevMonthStart, prevMonthEnd);
            previousMonth.add(prevCount != null ? prevCount.intValue() : 0);
        }

        compare.setCurrent(currentMonth);
        compare.setPrevious(previousMonth);

        return compare;
    }

    private List<DashboardVO.CategoryTrend> getCategoryTrend() {
        List<DashboardVO.CategoryTrend> trends = new ArrayList<>();

        LocalDateTime now = LocalDateTime.now();

        List<com.wallpaper.server.entity.Category> categories = categoryRepository
                .findByIsDelFalseOrderBySortOrderAsc();
        for (com.wallpaper.server.entity.Category category : categories) {
            DashboardVO.CategoryTrend trend = new DashboardVO.CategoryTrend();
            trend.setName(category.getName());
            trend.setData(getCategoryMonthlyData(now, category.getId()));
            trends.add(trend);
        }

        return trends;
    }

    private List<Integer> getCategoryMonthlyData(LocalDateTime now, Long categoryId) {
        List<Integer> data = new ArrayList<>();
        for (int i = 11; i >= 0; i--) {
            LocalDateTime monthStart = now.minusMonths(i).toLocalDate().atStartOfDay();
            LocalDateTime monthEnd = now.minusMonths(i).plusMonths(1).toLocalDate().atStartOfDay().minusSeconds(1);
            Long count = wallpaperRepository.countByCategoryIdAndIsDelFalseAndCreateTimeBetween(categoryId, monthStart,
                    monthEnd);
            data.add(count != null ? count.intValue() : 0);
        }
        return data;
    }
}
