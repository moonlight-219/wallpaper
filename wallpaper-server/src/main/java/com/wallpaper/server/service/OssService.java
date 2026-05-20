package com.wallpaper.server.service;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.ObjectMetadata;
import com.wallpaper.server.model.ImageInfo;
import com.wallpaper.server.util.ImageUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Slf4j
@Service
public class OssService {

    @Autowired(required = false)
    private OSS ossClient;

    @Autowired
    private com.wallpaper.server.config.OssConfig ossConfig;

    @javax.annotation.PostConstruct
    public void init() {
        if (ossClient != null) {
            log.info("OssService初始化成功");
            log.info("OSS配置 - Endpoint: {}, Bucket: {}, URL前缀: {}",
                    ossConfig.getEndpoint(), ossConfig.getBucketName(), ossConfig.getUrlPrefix());
        } else {
            log.warn("OssService初始化：OSS客户端未配置，文件上传功能将不可用");
        }
    }

    private boolean isOssAvailable() {
        return ossClient != null;
    }

    public String uploadFile(MultipartFile file, String folder) throws IOException {
        if (!isOssAvailable()) {
            throw new IOException("OSS服务未配置");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            originalFilename = "unknown";
        }

        String extension = "";
        int dotIndex = originalFilename.lastIndexOf(".");
        if (dotIndex > 0) {
            extension = originalFilename.substring(dotIndex);
        }

        String fileName = UUID.randomUUID().toString() + extension;
        String objectKey = folder + "/" + fileName;

        try (InputStream inputStream = file.getInputStream()) {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());
            metadata.setContentDisposition("inline");

            ossClient.putObject(ossConfig.getBucketName(), objectKey, inputStream, metadata);
        }

        return ossConfig.getUrlPrefix() + "/" + objectKey + "?x-oss-disposition=inline";
    }

    public ImageInfo uploadImageWithThumbnail(MultipartFile file, Integer type) throws IOException {
        if (!isOssAvailable()) {
            throw new IOException("OSS服务未配置");
        }

        log.info("开始处理图片，文件名：{}，大小：{} bytes，ContentType：{}，type：{}",
                file.getOriginalFilename(), file.getSize(), file.getContentType(), type);

        if (!ImageUtil.isWebpSupported()) {
            log.warn("WebP格式不支持，将使用原始格式");
        }

        byte[] fileBytes = file.getBytes();
        byte[] uploadBytes = fileBytes;
        String extension = ".webp";
        String contentType = "image/webp";

        if (ImageUtil.isWebpSupported()) {
            try {
                uploadBytes = ImageUtil.convertToWebp(fileBytes, 0.85f);
                log.info("图片已转换为WebP格式");
            } catch (Exception e) {
                log.warn("WebP转换失败，使用原始格式：{}", e.getMessage());
                String originalFilename = file.getOriginalFilename();
                if (originalFilename != null && !originalFilename.isEmpty()) {
                    int dotIndex = originalFilename.lastIndexOf(".");
                    if (dotIndex > 0) {
                        extension = originalFilename.substring(dotIndex);
                        contentType = file.getContentType();
                    }
                }
                uploadBytes = fileBytes;
            }
        } else {
            String originalFilename = file.getOriginalFilename();
            if (originalFilename != null && !originalFilename.isEmpty()) {
                int dotIndex = originalFilename.lastIndexOf(".");
                if (dotIndex > 0) {
                    extension = originalFilename.substring(dotIndex);
                    contentType = file.getContentType();
                }
            }
            uploadBytes = fileBytes;
        }

        String fileName = UUID.randomUUID().toString() + extension;

        String folder;
        if (type == 1) {
            folder = "phone";
        } else if (type == 2) {
            folder = "tablet";
        } else {
            throw new IOException("type参数错误，只支持1(手机壁纸)、2(平板壁纸)");
        }

        String originalObjectKey = folder + "/" + fileName;

        try (InputStream inputStream = new ByteArrayInputStream(uploadBytes)) {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(uploadBytes.length);
            metadata.setContentType(contentType);
            metadata.setContentDisposition("inline");

            ossClient.putObject(ossConfig.getBucketName(), originalObjectKey, inputStream, metadata);
            log.info("原始图片上传成功：{}", originalObjectKey);
        }

        ImageInfo imageInfo = new ImageInfo();
        imageInfo.setOriginalUrl(ossConfig.getUrlPrefix() + "/" + originalObjectKey + "?x-oss-disposition=inline");
        imageInfo.setFormat("WEBP");
        imageInfo.setSize((long) uploadBytes.length);

        try {
            BufferedImage originalImage = ImageUtil.readImage(fileBytes);
            if (originalImage != null) {
                imageInfo.setWidth(originalImage.getWidth());
                imageInfo.setHeight(originalImage.getHeight());
                log.info("图片尺寸：{}x{}", originalImage.getWidth(), originalImage.getHeight());
            } else {
                imageInfo.setWidth(1920);
                imageInfo.setHeight(1080);
            }
        } catch (Exception e) {
            log.warn("读取图片尺寸失败：{}", e.getMessage());
            imageInfo.setWidth(1920);
            imageInfo.setHeight(1080);
        }

        if (ImageUtil.isWebpSupported()) {
            try {
                byte[] thumbnailSmall = ImageUtil.createThumbnail(fileBytes, ImageUtil.THUMBNAIL_SMALL, 0.8f);
                String smallKey = folder + "/thumbnails/" + fileName.replace(extension, "_300" + extension);
                uploadToOss(smallKey, thumbnailSmall, contentType);
                imageInfo.setThumbnailSmallUrl(ossConfig.getUrlPrefix() + "/" + smallKey + "?x-oss-disposition=inline");
                log.info("小尺寸缩略图上传成功：{}", smallKey);
            } catch (Exception e) {
                log.warn("生成小尺寸缩略图失败：{}", e.getMessage());
            }

            try {
                byte[] thumbnailMedium = ImageUtil.createThumbnail(fileBytes, ImageUtil.THUMBNAIL_MEDIUM, 0.85f);
                String mediumKey = folder + "/thumbnails/" + fileName.replace(extension, "_600" + extension);
                uploadToOss(mediumKey, thumbnailMedium, contentType);
                imageInfo.setThumbnailMediumUrl(ossConfig.getUrlPrefix() + "/" + mediumKey + "?x-oss-disposition=inline");
                log.info("中尺寸缩略图上传成功：{}", mediumKey);
            } catch (Exception e) {
                log.warn("生成中尺寸缩略图失败：{}", e.getMessage());
            }

            try {
                byte[] thumbnailLarge = ImageUtil.createThumbnail(fileBytes, ImageUtil.THUMBNAIL_LARGE, 0.9f);
                String largeKey = folder + "/thumbnails/" + fileName.replace(extension, "_1200" + extension);
                uploadToOss(largeKey, thumbnailLarge, contentType);
                imageInfo.setThumbnailLargeUrl(ossConfig.getUrlPrefix() + "/" + largeKey + "?x-oss-disposition=inline");
                log.info("大尺寸缩略图上传成功：{}", largeKey);
            } catch (Exception e) {
                log.warn("生成大尺寸缩略图失败：{}", e.getMessage());
            }
        }

        if (imageInfo.getThumbnailSmallUrl() != null) {
            imageInfo.setThumbnailUrl(imageInfo.getThumbnailSmallUrl());
        } else {
            imageInfo.setThumbnailUrl(ossConfig.getUrlPrefix() + "/" + originalObjectKey
                    + "?x-oss-process=image/resize,w_300,h_300&x-oss-disposition=inline");
        }

        log.info("图片上传完成，原图URL：{}，缩略图URL：{}，格式：{}，大小：{} bytes",
                imageInfo.getOriginalUrl(), imageInfo.getThumbnailUrl(), imageInfo.getFormat(), imageInfo.getSize());

        return imageInfo;
    }

    private void uploadToOss(String objectKey, byte[] data, String contentType) throws IOException {
        try (InputStream inputStream = new ByteArrayInputStream(data)) {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(data.length);
            metadata.setContentType(contentType);
            metadata.setContentDisposition("inline");

            ossClient.putObject(ossConfig.getBucketName(), objectKey, inputStream, metadata);
        }
    }

    public void deleteFile(String fileUrl) {
        if (!isOssAvailable()) {
            log.warn("OSS服务未配置，无法删除文件");
            return;
        }

        if (fileUrl == null || fileUrl.isEmpty()) {
            return;
        }

        try {
            String objectKey = fileUrl.substring(ossConfig.getUrlPrefix().length() + 1);
            ossClient.deleteObject(ossConfig.getBucketName(), objectKey);
            log.info("删除OSS文件成功: {}", objectKey);
        } catch (Exception e) {
            log.error("删除OSS文件失败: {}", fileUrl, e);
        }
    }
}
