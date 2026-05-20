package com.wallpaper.server.repository;

import com.wallpaper.server.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByIsDelFalseOrderBySortOrderAsc();

    List<Category> findByIsDelFalseOrderBySortOrderDesc();

    List<Category> findByIsDelFalseOrderByLikeCountDesc();

    List<Category> findByIsDelFalseAndNameContainingIgnoreCase(String name);

    Page<Category> findByIsDelFalseOrderBySortOrderAsc(Pageable pageable);

    Page<Category> findByIsDelFalseAndNameContainingIgnoreCase(String name, Pageable pageable);

    Long countByIsDelFalse();

    Long countByCreateTimeBetween(LocalDateTime start, LocalDateTime end);
}