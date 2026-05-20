package com.wallpaper.server.service;

import com.wallpaper.server.entity.Work;
import com.wallpaper.server.repository.WorksRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class WorksService {

    @Autowired
    private WorksRepository worksRepository;

    public Work findById(Long id) {
        return worksRepository.findById(id).orElse(null);
    }

    public List<Work> findAll() {
        return worksRepository.findAll();
    }

    public Page<Work> findAll(int page, int pageSize) {
        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "createTime"));
        return worksRepository.findAll(pageable);
    }

    public Page<Work> findByUserId(Long userId, int page, int pageSize) {
        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "createTime"));
        return worksRepository.findByUserId(userId, pageable);
    }

    public Page<Work> findByUserIdAndStatus(Long userId, Integer status, int page, int pageSize) {
        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "createTime"));
        return worksRepository.findByUserIdAndStatus(userId, status, pageable);
    }

    public Page<Work> findByStatus(Integer status, int page, int pageSize) {
        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "createTime"));
        return worksRepository.findByStatusOrderByCreateTime(status, pageable);
    }

    public List<Work> findByCategoryId(Long categoryId) {
        return worksRepository.findByCategoryId(categoryId);
    }

    public Page<Work> findByCategoryId(Long categoryId, int page, int pageSize) {
        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "createTime"));
        return worksRepository.findByCategoryId(categoryId, pageable);
    }

    @Transactional
    public Work create(Work work) {
        return worksRepository.save(work);
    }

    @Transactional
    public Work update(Work work) {
        return worksRepository.save(work);
    }

    @Transactional
    public void delete(Long id) {
        worksRepository.deleteById(id);
    }

    @Transactional
    public void incrementLikeCount(Long workId) {
        worksRepository.incrementLikeCount(workId);
    }

    @Transactional
    public void decrementLikeCount(Long workId) {
        worksRepository.decrementLikeCount(workId);
    }

    @Transactional
    public void incrementCollectCount(Long workId) {
        worksRepository.incrementCollectCount(workId);
    }

    @Transactional
    public void decrementCollectCount(Long workId) {
        worksRepository.decrementCollectCount(workId);
    }

    @Transactional
    public void incrementViewCount(Long workId) {
        worksRepository.incrementViewCount(workId);
    }

    @Transactional
    public void incrementDownloadCount(Long workId) {
        worksRepository.incrementDownloadCount(workId);
    }

    @Transactional
    public void updateWallpaperCount(Long workId, int count) {
        Work work = findById(workId);
        if (work != null) {
            work.setWallpaperCount(count);
            worksRepository.save(work);
        }
    }

    @Transactional
    public void updateCoverUrl(Long workId, String coverUrl, Integer imageWidth, Integer imageHeight,
            String thumbnailUrl, String fileFormat, Long fileSize) {
        Work work = findById(workId);
        if (work != null) {
            work.setCoverUrl(coverUrl);
            work.setImageWidth(imageWidth);
            work.setImageHeight(imageHeight);
            work.setThumbnailUrl(thumbnailUrl);
            work.setFileFormat(fileFormat);
            work.setFileSize(fileSize);
            worksRepository.save(work);
        }
    }
}
