package com.wallpaper.server.repository;

import com.wallpaper.server.entity.Wallpaper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface WallpaperRepository extends JpaRepository<Wallpaper, Long> {

    /**
     * 前台可见：未删除；无作品集时要求壁纸本身已通过审；有作品集时要求作品集未删且已通过审（status=1）。
     */
    String JPQL_WALLPAPER_PUBLIC = "w.isDel = false AND ((w.workId IS NULL AND w.status = 1) OR EXISTS (SELECT 1 FROM Work _wk WHERE _wk.id = w.workId AND _wk.isDel = false AND _wk.status = 1))";

    Long countByType(Integer type);

    Long countByCreateTimeBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT COUNT(w) FROM Wallpaper w WHERE " + JPQL_WALLPAPER_PUBLIC
            + " AND w.createTime BETWEEN :start AND :end")
    Long countByIsDelFalseAndCreateTimeBetween(@Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(w) FROM Wallpaper w WHERE " + JPQL_WALLPAPER_PUBLIC
            + " AND w.categoryId = :categoryId AND w.createTime BETWEEN :start AND :end")
    Long countByCategoryIdAndIsDelFalseAndCreateTimeBetween(@Param("categoryId") Long categoryId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    List<Wallpaper> findByUserId(Long userId);

    List<Wallpaper> findByUserIdIn(List<Long> userIds);

    List<Wallpaper> findByWorkIdIn(List<Long> workIds);

    List<Wallpaper> findByWorkId(Long workId);

    Page<Wallpaper> findByUserId(Long userId, Pageable pageable);

    List<Wallpaper> findByUserIdAndType(Long userId, Integer type);

    Page<Wallpaper> findByUserIdAndType(Long userId, Integer type, Pageable pageable);

    List<Wallpaper> findByUserIdAndStatus(Long userId, Integer status);

    Page<Wallpaper> findByUserIdAndStatus(Long userId, Integer status, Pageable pageable);

    Page<Wallpaper> findByStatus(Integer status, Pageable pageable);

    List<Wallpaper> findByUserIdAndTypeAndStatus(Long userId, Integer type, Integer status);

    Page<Wallpaper> findByUserIdAndTypeAndStatus(Long userId, Integer type, Integer status, Pageable pageable);

    Long countByUserId(Long userId);

    Long countByUserIdAndStatus(Long userId, Integer status);

    Long countByCategoryId(Long categoryId);

    @Query("SELECT COUNT(w) FROM Wallpaper w WHERE " + JPQL_WALLPAPER_PUBLIC + " AND w.categoryId = :categoryId")
    Long countPublicVisibleByCategoryId(@Param("categoryId") Long categoryId);

    @Query("SELECT w FROM Wallpaper w WHERE w.id = :id AND " + JPQL_WALLPAPER_PUBLIC)
    Optional<Wallpaper> findPublicById(@Param("id") Long id);

    @Query("SELECT w FROM Wallpaper w WHERE w.id IN :ids AND " + JPQL_WALLPAPER_PUBLIC)
    List<Wallpaper> findPublicByIds(@Param("ids") Collection<Long> ids);

    @Query("SELECT w FROM Wallpaper w WHERE w.userId = :userId AND " + JPQL_WALLPAPER_PUBLIC)
    List<Wallpaper> findPublicByUserId(@Param("userId") Long userId);

    @Query("SELECT w FROM Wallpaper w WHERE w.userId IN :userIds AND " + JPQL_WALLPAPER_PUBLIC)
    List<Wallpaper> findPublicByUserIdIn(@Param("userIds") List<Long> userIds);

    @Query("SELECT w FROM Wallpaper w WHERE w.workId IN :workIds AND " + JPQL_WALLPAPER_PUBLIC)
    List<Wallpaper> findPublicByWorkIdIn(@Param("workIds") List<Long> workIds);

    @Query("SELECT w FROM Wallpaper w WHERE w.userId = :userId AND " + JPQL_WALLPAPER_PUBLIC
            + " ORDER BY w.likeCount DESC")
    List<Wallpaper> findTopPublicByUserIdOrderByLikeCount(@Param("userId") Long userId);

    @Query("SELECT w FROM Wallpaper w LEFT JOIN Category c ON w.categoryId = c.id WHERE " + JPQL_WALLPAPER_PUBLIC
            + " AND (w.title LIKE CONCAT('%', :keyword, '%') OR c.name LIKE CONCAT('%', :keyword, '%'))")
    List<Wallpaper> searchByKeyword(@Param("keyword") String keyword);

    @Query("SELECT w FROM Wallpaper w WHERE " + JPQL_WALLPAPER_PUBLIC
            + " AND w.type = :type ORDER BY w.createTime DESC")
    Page<Wallpaper> findByTypeOrderByCreateTimeDesc(@Param("type") Integer type, Pageable pageable);

    @Query("SELECT w FROM Wallpaper w WHERE " + JPQL_WALLPAPER_PUBLIC
            + " AND w.categoryId = :categoryId ORDER BY w.createTime DESC")
    Page<Wallpaper> findByCategoryIdOrderByCreateTimeDesc(@Param("categoryId") Long categoryId,
            Pageable pageable);

    @Query("SELECT w FROM Wallpaper w WHERE " + JPQL_WALLPAPER_PUBLIC
            + " ORDER BY w.createTime DESC")
    Page<Wallpaper> findAllByIsDelFalseOrderByCreateTimeDesc(Pageable pageable);

    @Query("SELECT w FROM Wallpaper w WHERE " + JPQL_WALLPAPER_PUBLIC
            + " ORDER BY w.createTime DESC")
    List<Wallpaper> findAllWithUser();

    @Query("SELECT w FROM Wallpaper w WHERE " + JPQL_WALLPAPER_PUBLIC
            + " ORDER BY w.createTime DESC")
    Page<Wallpaper> findAllWithUser(Pageable pageable);

    @Query("SELECT w FROM Wallpaper w WHERE " + JPQL_WALLPAPER_PUBLIC
            + " AND w.type = :type ORDER BY w.createTime DESC")
    Page<Wallpaper> findByTypeWithUser(@Param("type") Integer type, Pageable pageable);

    @Query("SELECT w FROM Wallpaper w WHERE " + JPQL_WALLPAPER_PUBLIC
            + " AND w.categoryId = :categoryId ORDER BY w.createTime DESC")
    Page<Wallpaper> findByCategoryIdWithUser(@Param("categoryId") Long categoryId, Pageable pageable);

    @Query("SELECT w FROM Wallpaper w WHERE " + JPQL_WALLPAPER_PUBLIC
            + " AND w.categoryId = :categoryId AND w.type = :type ORDER BY w.createTime DESC")
    Page<Wallpaper> findByCategoryIdAndTypeWithUser(@Param("categoryId") Long categoryId,
            @Param("type") Integer type,
            Pageable pageable);

    @Query("SELECT w FROM Wallpaper w WHERE " + JPQL_WALLPAPER_PUBLIC
            + " AND w.categoryId = :categoryId AND w.type = :type ORDER BY w.createTime DESC")
    Page<Wallpaper> findByCategoryIdAndType(@Param("categoryId") Long categoryId,
            @Param("type") Integer type, Pageable pageable);

    @Query("SELECT w FROM Wallpaper w WHERE " + JPQL_WALLPAPER_PUBLIC
            + " AND w.userId = :userId ORDER BY w.createTime DESC")
    Page<Wallpaper> findByUserIdWithUser(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT w FROM Wallpaper w WHERE " + JPQL_WALLPAPER_PUBLIC
            + " AND w.userId = :userId AND w.type = :type ORDER BY w.createTime DESC")
    Page<Wallpaper> findPublicByUserIdAndType(@Param("userId") Long userId, @Param("type") Integer type,
            Pageable pageable);

    @Query("UPDATE Wallpaper w SET w.likeCount = w.likeCount + 1 WHERE w.id = :id")
    @org.springframework.data.jpa.repository.Modifying
    int incrementLikeCount(@Param("id") Long id);

    @Query("UPDATE Wallpaper w SET w.likeCount = GREATEST(0, w.likeCount - 1), w.version = w.version + 1 WHERE w.id = :id")
    @org.springframework.data.jpa.repository.Modifying
    int decrementLikeCount(@Param("id") Long id);

    @Query("UPDATE Wallpaper w SET w.collectCount = w.collectCount + 1, w.version = w.version + 1 WHERE w.id = :id")
    @org.springframework.data.jpa.repository.Modifying
    int incrementCollectCount(@Param("id") Long id);

    @Query("UPDATE Wallpaper w SET w.collectCount = GREATEST(0, w.collectCount - 1), w.version = w.version + 1 WHERE w.id = :id")
    @org.springframework.data.jpa.repository.Modifying
    int decrementCollectCount(@Param("id") Long id);

    @Query("UPDATE Wallpaper w SET w.downloadCount = w.downloadCount + 1, w.version = w.version + 1 WHERE w.id = :id")
    @org.springframework.data.jpa.repository.Modifying
    int incrementDownloadCount(@Param("id") Long id);

    @Query("UPDATE Wallpaper w SET w.isDel = true, w.version = w.version + 1 WHERE w.id = :id")
    @org.springframework.data.jpa.repository.Modifying
    int softDelete(@Param("id") Long id);

    @Query("SELECT w FROM Wallpaper w WHERE " + JPQL_WALLPAPER_PUBLIC
            + " AND w.categoryId IN (SELECT c.id FROM Category c WHERE c.name = :categoryName) ORDER BY w.createTime DESC")
    Page<Wallpaper> findByCategoryNameWithUser(@Param("categoryName") String categoryName, Pageable pageable);

    @Query("SELECT w FROM Wallpaper w WHERE " + JPQL_WALLPAPER_PUBLIC
            + " AND w.categoryId IN (SELECT c.id FROM Category c WHERE c.name = :categoryName) AND w.type = :type ORDER BY w.createTime DESC")
    Page<Wallpaper> findByCategoryNameAndTypeWithUser(@Param("categoryName") String categoryName,
            @Param("type") Integer type, Pageable pageable);

    @Query("SELECT w FROM Wallpaper w WHERE " + JPQL_WALLPAPER_PUBLIC
            + " AND w.categoryId IN (SELECT c.id FROM Category c WHERE c.name LIKE CONCAT('%', :categoryName, '%')) ORDER BY w.createTime DESC")
    Page<Wallpaper> findByCategoryNameLikeWithUser(@Param("categoryName") String categoryName, Pageable pageable);

    @Query("SELECT w FROM Wallpaper w WHERE " + JPQL_WALLPAPER_PUBLIC
            + " AND w.categoryId IN (SELECT c.id FROM Category c WHERE c.name LIKE CONCAT('%', :categoryName, '%')) AND w.type = :type ORDER BY w.createTime DESC")
    Page<Wallpaper> findByCategoryNameLikeAndTypeWithUser(@Param("categoryName") String categoryName,
            @Param("type") Integer type, Pageable pageable);
}
