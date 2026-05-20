package com.wallpaper.server.repository;

import com.wallpaper.server.entity.Work;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorksRepository extends JpaRepository<Work, Long> {

        List<Work> findByUserId(Long userId);

        Page<Work> findByUserId(Long userId, Pageable pageable);

        List<Work> findByUserIdAndStatus(Long userId, Integer status);

        Page<Work> findByUserIdAndStatus(Long userId, Integer status, Pageable pageable);

        Page<Work> findByUserIdAndType(Long userId, Integer type, Pageable pageable);

        Page<Work> findByUserIdAndTypeAndStatus(Long userId, Integer type, Integer status, Pageable pageable);

        Page<Work> findByStatus(Integer status, Pageable pageable);

        List<Work> findByCategoryId(Long categoryId);

        Page<Work> findByCategoryId(Long categoryId, Pageable pageable);

        @Query("SELECT w FROM Work w WHERE w.isDel = false AND w.userId = :userId ORDER BY w.likeCount DESC")
        Page<Work> findTopLikedByUserId(@Param("userId") Long userId, Pageable pageable);

        @Query("SELECT w FROM Work w WHERE w.isDel = false AND w.userId = :userId ORDER BY w.collectCount DESC")
        Page<Work> findTopCollectedByUserId(@Param("userId") Long userId, Pageable pageable);

        Long countByUserId(Long userId);

        Long countByUserIdAndStatus(Long userId, Integer status);

        @Query("SELECT w FROM Work w WHERE w.isDel = false")
        Page<Work> findAll(Pageable pageable);

        @Query("SELECT w FROM Work w WHERE w.isDel = false AND w.status = :status")
        Page<Work> findByStatusOrderByCreateTime(@Param("status") Integer status, Pageable pageable);

        @Query("UPDATE Work w SET w.likeCount = w.likeCount + 1 WHERE w.id = :id")
        @org.springframework.data.jpa.repository.Modifying
        int incrementLikeCount(@Param("id") Long id);

        @Query("UPDATE Work w SET w.likeCount = GREATEST(0, w.likeCount - 1), w.version = w.version + 1 WHERE w.id = :id")
        @org.springframework.data.jpa.repository.Modifying
        int decrementLikeCount(@Param("id") Long id);

        @Query("UPDATE Work w SET w.collectCount = w.collectCount + 1, w.version = w.version + 1 WHERE w.id = :id")
        @org.springframework.data.jpa.repository.Modifying
        int incrementCollectCount(@Param("id") Long id);

        @Query("UPDATE Work w SET w.collectCount = GREATEST(0, w.collectCount - 1), w.version = w.version + 1 WHERE w.id = :id")
        @org.springframework.data.jpa.repository.Modifying
        int decrementCollectCount(@Param("id") Long id);

        @Query("UPDATE Work w SET w.viewCount = w.viewCount + 1, w.version = w.version + 1 WHERE w.id = :id")
        @org.springframework.data.jpa.repository.Modifying
        int incrementViewCount(@Param("id") Long id);

        @Query("UPDATE Work w SET w.downloadCount = w.downloadCount + 1, w.version = w.version + 1 WHERE w.id = :id")
        @org.springframework.data.jpa.repository.Modifying
        int incrementDownloadCount(@Param("id") Long id);

        @Query("SELECT w FROM Work w WHERE w.isDel = false AND w.status = 1 AND w.type = :type")
        Page<Work> findByStatusAndType(@Param("type") Integer type, Pageable pageable);

        @Query("SELECT w FROM Work w WHERE w.isDel = false AND w.status = :status AND w.type = :type")
        Page<Work> findByStatusAndType(@Param("status") Integer status, @Param("type") Integer type, Pageable pageable);

        @Query("SELECT w FROM Work w WHERE w.isDel = false AND w.status = 1 AND w.categoryId = :categoryId")
        Page<Work> findByStatusAndCategoryId(@Param("categoryId") Long categoryId, Pageable pageable);

        @Query("SELECT w FROM Work w WHERE w.isDel = false AND w.status = :status AND w.categoryId = :categoryId")
        Page<Work> findByStatusAndCategoryId(@Param("status") Integer status, @Param("categoryId") Long categoryId,
                        Pageable pageable);

        @Query("SELECT w FROM Work w WHERE w.isDel = false AND w.status = 1 AND w.type = :type AND w.categoryId = :categoryId")
        Page<Work> findByStatusAndTypeAndCategoryId(@Param("type") Integer type, @Param("categoryId") Long categoryId,
                        Pageable pageable);

        @Query("SELECT w FROM Work w WHERE w.isDel = false AND w.status = :status AND w.type = :type AND w.categoryId = :categoryId")
        Page<Work> findByStatusAndTypeAndCategoryId(@Param("status") Integer status, @Param("type") Integer type,
                        @Param("categoryId") Long categoryId, Pageable pageable);

        @Query("SELECT w FROM Work w INNER JOIN User u ON w.userId = u.id WHERE w.isDel = false AND w.status = 1 AND (u.username LIKE CONCAT('%', :keyword, '%') OR u.nickname LIKE CONCAT('%', :keyword, '%'))")
        Page<Work> findByStatusAndAuthorKeyword(@Param("keyword") String keyword, Pageable pageable);

        @Query("SELECT w FROM Work w INNER JOIN User u ON w.userId = u.id WHERE w.isDel = false AND w.status = :status AND (u.username LIKE CONCAT('%', :keyword, '%') OR u.nickname LIKE CONCAT('%', :keyword, '%'))")
        Page<Work> findByStatusAndAuthorKeyword(@Param("status") Integer status, @Param("keyword") String keyword,
                        Pageable pageable);

        @Query("SELECT w FROM Work w INNER JOIN User u ON w.userId = u.id WHERE w.isDel = false AND w.status = 1 AND w.type = :type AND (u.username LIKE CONCAT('%', :keyword, '%') OR u.nickname LIKE CONCAT('%', :keyword, '%'))")
        Page<Work> findByStatusAndTypeAndAuthorKeyword(@Param("type") Integer type, @Param("keyword") String keyword,
                        Pageable pageable);

        @Query("SELECT w FROM Work w INNER JOIN User u ON w.userId = u.id WHERE w.isDel = false AND w.status = :status AND w.type = :type AND (u.username LIKE CONCAT('%', :keyword, '%') OR u.nickname LIKE CONCAT('%', :keyword, '%'))")
        Page<Work> findByStatusAndTypeAndAuthorKeyword(@Param("status") Integer status, @Param("type") Integer type,
                        @Param("keyword") String keyword, Pageable pageable);

        @Query("SELECT w FROM Work w INNER JOIN User u ON w.userId = u.id WHERE w.isDel = false AND w.status = 1 AND w.categoryId = :categoryId AND (u.username LIKE CONCAT('%', :keyword, '%') OR u.nickname LIKE CONCAT('%', :keyword, '%'))")
        Page<Work> findByStatusAndCategoryIdAndAuthorKeyword(@Param("categoryId") Long categoryId,
                        @Param("keyword") String keyword, Pageable pageable);

        @Query("SELECT w FROM Work w INNER JOIN User u ON w.userId = u.id WHERE w.isDel = false AND w.status = :status AND w.categoryId = :categoryId AND (u.username LIKE CONCAT('%', :keyword, '%') OR u.nickname LIKE CONCAT('%', :keyword, '%'))")
        Page<Work> findByStatusAndCategoryIdAndAuthorKeyword(@Param("status") Integer status,
                        @Param("categoryId") Long categoryId, @Param("keyword") String keyword, Pageable pageable);

        @Query("SELECT w FROM Work w INNER JOIN User u ON w.userId = u.id WHERE w.isDel = false AND w.status = 1 AND w.type = :type AND w.categoryId = :categoryId AND (u.username LIKE CONCAT('%', :keyword, '%') OR u.nickname LIKE CONCAT('%', :keyword, '%'))")
        Page<Work> findByStatusAndTypeAndCategoryIdAndAuthorKeyword(@Param("type") Integer type,
                        @Param("categoryId") Long categoryId, @Param("keyword") String keyword, Pageable pageable);

        @Query("SELECT w FROM Work w INNER JOIN User u ON w.userId = u.id WHERE w.isDel = false AND w.status = :status AND w.type = :type AND w.categoryId = :categoryId AND (u.username LIKE CONCAT('%', :keyword, '%') OR u.nickname LIKE CONCAT('%', :keyword, '%'))")
        Page<Work> findByStatusAndTypeAndCategoryIdAndAuthorKeyword(@Param("status") Integer status,
                        @Param("type") Integer type, @Param("categoryId") Long categoryId,
                        @Param("keyword") String keyword, Pageable pageable);

        @Query("SELECT w FROM Work w INNER JOIN User u ON w.userId = u.id WHERE w.isDel = false AND (u.username LIKE CONCAT('%', :keyword, '%') OR u.nickname LIKE CONCAT('%', :keyword, '%'))")
        Page<Work> findByAuthorKeyword(@Param("keyword") String keyword, Pageable pageable);

        @Query("SELECT w FROM Work w INNER JOIN User u ON w.userId = u.id WHERE w.isDel = false AND w.categoryId = :categoryId AND (u.username LIKE CONCAT('%', :keyword, '%') OR u.nickname LIKE CONCAT('%', :keyword, '%'))")
        Page<Work> findByCategoryIdAndAuthorKeyword(@Param("categoryId") Long categoryId,
                        @Param("keyword") String keyword, Pageable pageable);

        @Query("SELECT new com.wallpaper.server.vo.UserWorkCount(w.userId, COUNT(w)) FROM Work w WHERE w.isDel = false GROUP BY w.userId ORDER BY COUNT(w) DESC")
        List<com.wallpaper.server.vo.UserWorkCount> findTopAuthorsByWorkCount();
}
