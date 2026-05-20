package com.wallpaper.server.vo;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class DashboardVO {
    private List<TypeDistribution> typeDistribution;
    private List<TopAuthor> topAuthors;
    private Map<String, UploadTrend> uploadTrend;
    private MonthlyCompare monthlyCompare;
    private List<CategoryTrend> categoryTrend;

    @Data
    public static class TypeDistribution {
        private String name;
        private Long value;
    }

    @Data
    public static class TopAuthor {
        private String name;
        private Integer count;
    }

    @Data
    public static class UploadTrend {
        private List<String> dates;
        private List<Integer> values;
    }

    @Data
    public static class MonthlyCompare {
        private List<Integer> current;
        private List<Integer> previous;
    }

    @Data
    public static class CategoryTrend {
        private String name;
        private List<Integer> data;
    }
}
