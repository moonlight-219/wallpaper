package com.wallpaper.server.service;

import com.wallpaper.server.entity.Author;
import com.wallpaper.server.repository.AuthorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuthorService {
    @Autowired
    private AuthorRepository authorRepository;

    public List<Author> findAll() {
        return authorRepository.findByIsDelFalseOrderByFollowerCountDesc();
    }

    public Author findById(Long id) {
        return authorRepository.findById(id).orElse(null);
    }

    public Author create(Author author) {
        author.setCreateTime(LocalDateTime.now());
        author.setUpdateTime(LocalDateTime.now());
        return authorRepository.save(author);
    }

    public Author update(Author author) {
        author.setUpdateTime(LocalDateTime.now());
        return authorRepository.save(author);
    }

    public void delete(Long id) {
        authorRepository.deleteById(id);
    }
}