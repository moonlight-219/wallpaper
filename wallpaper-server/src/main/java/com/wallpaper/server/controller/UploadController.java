package com.wallpaper.server.controller;

import com.wallpaper.server.common.Result;
import com.wallpaper.server.model.ImageInfo;
import com.wallpaper.server.service.OssService;
import com.wallpaper.server.util.ImageUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/upload")
public class UploadController {

    @Autowired(required = false)
    private OssService ossService;

    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
            "image/jpeg", "image/png", "image/gif", "image/webp", "image/bmp");

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
            "jpg", "jpeg", "png", "gif", "webp", "bmp");

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    @PostMapping
    public Result<Map<String, Object>> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "type", defaultValue = "1") Integer type) {
        if (file.isEmpty()) {
            return Result.paramError("文件不能为空");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            return Result.paramError("文件大小不能超过10MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            return Result.paramError("只支持图片文件(jpg, png, gif, webp, bmp)");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null && !originalFilename.isEmpty()) {
            String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
            if (!ALLOWED_EXTENSIONS.contains(extension)) {
                return Result.paramError("不支持的文件格式: " + extension);
            }
        }

        if (type != 1 && type != 2 && type != 3) {
            return Result.paramError("type参数错误，只支持1(手机壁纸)、2(平板壁纸)、3(头像)");
        }

        try {
            byte[] fileBytes = file.getBytes();
            if (!ImageUtil.isValidImage(fileBytes)) {
                return Result.paramError("文件内容不是有效的图片");
            }

            Map<String, Object> data = new HashMap<>();

            if (ossService != null) {
                log.info("开始上传图片到OSS，文件名：{}，大小：{} bytes，type：{}", originalFilename, file.getSize(), type);

                if (type == 3) {
                    String url = ossService.uploadFile(file, "avatars");
                    data.put("url", url);
                } else {
                    ImageInfo imageInfo = ossService.uploadImageWithThumbnail(file, type);
                    data.put("url", imageInfo.getOriginalUrl());
                    data.put("thumbnailUrl", imageInfo.getThumbnailUrl());
                    data.put("width", imageInfo.getWidth());
                    data.put("height", imageInfo.getHeight());
                    data.put("format", imageInfo.getFormat());
                    data.put("size", imageInfo.getSize());
                }

                log.info("上传成功");
            } else {
                log.warn("OSS服务未启用，使用模拟数据");
                if (type == 3) {
                    String url = "http://localhost:9999/mock/avatars/" + originalFilename;
                    data.put("url", url);
                } else {
                    ImageInfo imageInfo = createMockImageInfo(file, type);
                    data.put("url", imageInfo.getOriginalUrl());
                    data.put("thumbnailUrl", imageInfo.getThumbnailUrl());
                    data.put("width", imageInfo.getWidth());
                    data.put("height", imageInfo.getHeight());
                    data.put("format", imageInfo.getFormat());
                    data.put("size", imageInfo.getSize());
                }
            }

            return Result.success(data);
        } catch (Exception e) {
            log.error("上传图片失败，文件名：{}，错误信息：{}", originalFilename, e.getMessage(), e);
            return Result.error(10001, "文件上传失败：" + e.getMessage());
        }
    }

    private ImageInfo createMockImageInfo(MultipartFile file, Integer type) throws IOException {
        ImageInfo imageInfo = new ImageInfo();
        String filename = file.getOriginalFilename();
        String extension = filename.substring(filename.lastIndexOf("."));

        String folder = type == 1 ? "phone" : "tablet";
        imageInfo.setOriginalUrl("http://localhost:9999/mock/" + folder + "/" + filename);
        imageInfo.setThumbnailUrl("http://localhost:9999/mock/" + folder + "/thumbnails/" + filename);
        imageInfo.setFormat(extension.substring(1).toUpperCase());
        imageInfo.setSize(file.getSize());

        java.awt.image.BufferedImage image = javax.imageio.ImageIO.read(file.getInputStream());
        if (image != null) {
            imageInfo.setWidth(image.getWidth());
            imageInfo.setHeight(image.getHeight());
        } else {
            imageInfo.setWidth(1920);
            imageInfo.setHeight(1080);
        }

        return imageInfo;
    }
}
