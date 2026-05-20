package com.wallpaper.server.service;

import com.wallpaper.server.entity.Wallpaper;
import com.wallpaper.server.repository.UserActionRepository;
import com.wallpaper.server.repository.WallpaperRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DataValidationService {
    private static final Logger logger = LoggerFactory.getLogger(DataValidationService.class);

    @Autowired
    private UserActionRepository userActionRepository;

    @Autowired
    private WallpaperRepository wallpaperRepository;

    @Scheduled(fixedRate = 3600000)
    public void validateWallpaperStats() {
        logger.info("开始校验壁纸统计数据...");

        int pageSize = 1000;
        int page = 0;
        int totalCorrected = 0;
        int totalChecked = 0;

        Page<Wallpaper> wallpaperPage;
        do {
            Pageable pageable = PageRequest.of(page, pageSize);
            wallpaperPage = wallpaperRepository.findAll(pageable);

            int correctedCount = 0;

            for (Wallpaper wallpaper : wallpaperPage.getContent()) {
                Long wallpaperId = wallpaper.getId();

                Long actualLikeCount = userActionRepository.countByWallpaperIdAndActionType(wallpaperId, "like");
                Long actualCollectCount = userActionRepository.countByWallpaperIdAndActionType(wallpaperId, "collect");

                Integer storedLikeCount = wallpaper.getLikeCount() != null ? wallpaper.getLikeCount() : 0;
                Integer storedCollectCount = wallpaper.getCollectCount() != null ? wallpaper.getCollectCount() : 0;

                boolean needsCorrection = false;

                if (!actualLikeCount.equals(storedLikeCount.longValue())) {
                    logger.warn("壁纸ID: {} 点赞数不一致 - 实际: {}, 存储: {}", wallpaperId, actualLikeCount, storedLikeCount);
                    wallpaper.setLikeCount(actualLikeCount.intValue());
                    needsCorrection = true;
                }

                if (!actualCollectCount.equals(storedCollectCount.longValue())) {
                    logger.warn("壁纸ID: {} 收藏数不一致 - 实际: {}, 存储: {}", wallpaperId, actualCollectCount,
                            storedCollectCount);
                    wallpaper.setCollectCount(actualCollectCount.intValue());
                    needsCorrection = true;
                }

                if (needsCorrection) {
                    wallpaperRepository.save(wallpaper);
                    correctedCount++;
                }
            }

            totalChecked += wallpaperPage.getContent().size();
            totalCorrected += correctedCount;
            page++;

            logger.info("已检查第 {} 页数据，本页检查 {} 条，修正 {} 条", page, wallpaperPage.getContent().size(), correctedCount);

        } while (wallpaperPage.hasNext());

        logger.info("壁纸统计数据校验完成，共检查 {} 条记录，修正 {} 条", totalChecked, totalCorrected);
    }
}
