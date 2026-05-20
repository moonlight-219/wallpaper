package com.wallpaper.server.service;

import com.wallpaper.server.entity.User;
import com.wallpaper.server.repository.UserRepository;
import com.wallpaper.server.util.PasswordUtil;
import com.wallpaper.server.vo.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public java.util.List<User> findByIds(java.util.List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new java.util.ArrayList<>();
        }
        return userRepository.findAllById(ids);
    }

    public java.util.List<User> findAll() {
        return userRepository.findAll();
    }

    public PageResult<User> findAll(Integer page, Integer pageSize) {
        int p = page != null && page > 0 ? page - 1 : 0;
        int ps = pageSize != null && pageSize > 0 ? pageSize : 10;

        Pageable pageable = PageRequest.of(p, ps, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<User> pageResult = userRepository.findAll(pageable);

        return PageResult.of(pageResult.getContent(), pageResult.getTotalElements(), page, pageSize);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    public User findByWechatOpenId(String wechatOpenId) {
        return userRepository.findByWechatOpenId(wechatOpenId).orElse(null);
    }

    public User create(User user) {
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(PasswordUtil.encode(user.getPassword()));
        }

        if (user.getRole() == null || user.getRole().isEmpty()) {
            user.setRole("user");
        }

        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        return userRepository.save(user);
    }

    public User update(User user) {
        user.setUpdateTime(LocalDateTime.now());
        return userRepository.save(user);
    }

    public void delete(Long id) {
        userRepository.deleteById(id);
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public void incrementWorkCount(Long userId) {
        User user = findById(userId);
        if (user != null) {
            user.setWorkCount((user.getWorkCount() != null ? user.getWorkCount() : 0) + 1);
            userRepository.save(user);
        }
    }

    public void decrementWorkCount(Long userId) {
        User user = findById(userId);
        if (user != null && user.getWorkCount() != null && user.getWorkCount() > 0) {
            user.setWorkCount(user.getWorkCount() - 1);
            userRepository.save(user);
        }
    }
}