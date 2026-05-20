package com.wallpaper.server.repository;

import com.wallpaper.server.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    Optional<User> findByWechatOpenId(String wechatOpenId);

    boolean existsByUsername(String username);

    List<User> findByIsCreatorTrueOrderByWorkCountDesc();

    Page<User> findByIsCreatorTrue(Pageable pageable);

    List<User> findByIdIn(List<Long> ids);

    Long countByIsCreatorTrue();

    @Query("SELECT u FROM User u WHERE (u.username LIKE CONCAT('%', :keyword, '%') OR u.nickname LIKE CONCAT('%', :keyword, '%')) AND u.isDel = false")
    Page<User> findByUsernameOrNicknameContainingIgnoreCase(@Param("keyword") String keyword, Pageable pageable);

    Long countByCreateTimeBetween(LocalDateTime start, LocalDateTime end);
}