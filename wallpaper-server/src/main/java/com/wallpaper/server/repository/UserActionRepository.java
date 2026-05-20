package com.wallpaper.server.repository;

import com.wallpaper.server.entity.UserAction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserActionRepository extends JpaRepository<UserAction, Long> {
        List<UserAction> findByUserId(Long userId);

        List<UserAction> findByUserIdAndActionType(Long userId, String actionType);

        List<UserAction> findByUserIdAndActionTypeOrderByCreateTimeDesc(Long userId, String actionType);

        Page<UserAction> findByUserIdAndActionTypeAndIsDelOrderByCreateTimeDesc(Long userId, String actionType,
                        Boolean isDel, Pageable pageable);

        @Query("SELECT ua FROM UserAction ua WHERE ua.userId = :userId AND ua.wallpaperId = :wallpaperId AND ua.actionType = :actionType AND ua.isDel = false")
        UserAction findByUserAndWallpaperAndAction(@Param("userId") Long userId,
                        @Param("wallpaperId") Long wallpaperId,
                        @Param("actionType") String actionType);

        @Query("SELECT ua FROM UserAction ua WHERE ua.userId = :userId AND ua.workId = :workId AND ua.actionType = :actionType AND ua.isDel = false")
        UserAction findByUserAndWorkAndAction(@Param("userId") Long userId,
                        @Param("workId") Long workId,
                        @Param("actionType") String actionType);

        @Query("SELECT ua FROM UserAction ua WHERE ua.userId = :userId AND ua.categoryId = :categoryId AND ua.actionType = :actionType AND ua.isDel = false")
        UserAction findByUserAndCategoryAndAction(@Param("userId") Long userId,
                        @Param("categoryId") Long categoryId,
                        @Param("actionType") String actionType);

        @Query("SELECT COUNT(ua) FROM UserAction ua WHERE ua.userId = :userId AND ua.actionType = :actionType")
        Long countByUserIdAndActionType(@Param("userId") Long userId, @Param("actionType") String actionType);

        @Query("SELECT COUNT(ua) FROM UserAction ua WHERE ua.userId = :userId AND ua.actionType = :actionType AND ua.wallpaperId IS NOT NULL AND ua.isDel = false")
        Long countByUserIdAndActionTypeAndWallpaperIdNotNull(@Param("userId") Long userId, @Param("actionType") String actionType);

        @Query("SELECT COUNT(ua) FROM UserAction ua WHERE ua.userId = :userId AND ua.actionType = :actionType AND ua.workId IS NOT NULL AND ua.isDel = false")
        Long countByUserIdAndActionTypeAndWorkIdNotNull(@Param("userId") Long userId, @Param("actionType") String actionType);

        @Query("SELECT COUNT(ua) FROM UserAction ua WHERE ua.wallpaperId = :wallpaperId AND ua.actionType = :actionType AND ua.isDel = false")
        Long countByWallpaperIdAndActionType(@Param("wallpaperId") Long wallpaperId,
                        @Param("actionType") String actionType);
}