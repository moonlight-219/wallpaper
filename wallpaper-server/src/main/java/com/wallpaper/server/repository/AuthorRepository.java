package com.wallpaper.server.repository;

import com.wallpaper.server.entity.Author;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface AuthorRepository extends JpaRepository<Author, Long> {
    List<Author> findByIsDelFalseOrderByFollowerCountDesc();

    @Query("SELECT a.id as id, a.name as nickname, COUNT(w.id) as workCount " +
            "FROM Author a LEFT JOIN Wallpaper w ON a.id = w.userId AND w.isDel = false "
            + "AND ((w.workId IS NULL AND w.status = 1) OR EXISTS (SELECT 1 FROM Work _wk WHERE _wk.id = w.workId AND _wk.isDel = false AND _wk.status = 1)) "
            + "WHERE a.isDel = false " +
            "GROUP BY a.id, a.name " +
            "ORDER BY workCount DESC")
    List<Map<String, Object>> findTopAuthorsByWorkCount(Pageable pageable);
}