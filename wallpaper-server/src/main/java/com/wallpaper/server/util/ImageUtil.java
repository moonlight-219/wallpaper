package com.wallpaper.server.util;

import lombok.extern.slf4j.Slf4j;
import org.imgscalr.Scalr;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;

@Slf4j
public class ImageUtil {

    public static final int THUMBNAIL_SMALL = 300;
    public static final int THUMBNAIL_MEDIUM = 600;
    public static final int THUMBNAIL_LARGE = 1200;

    public static String detectImageFormat(byte[] imageBytes) {
        try (ImageInputStream iis = ImageIO.createImageInputStream(new ByteArrayInputStream(imageBytes))) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
            if (readers.hasNext()) {
                ImageReader reader = readers.next();
                String format = reader.getFormatName();
                reader.dispose();
                return format.toUpperCase();
            }
        } catch (Exception e) {
            log.warn("无法检测图片格式: {}", e.getMessage());
        }
        return "UNKNOWN";
    }

    public static BufferedImage readImage(byte[] imageBytes) throws IOException {
        BufferedImage image = null;

        try {
            image = ImageIO.read(new ByteArrayInputStream(imageBytes));

            if (image != null) {
                log.info("成功读取图片，尺寸: {}x{}, 类型: {}", image.getWidth(), image.getHeight(), image.getType());
                return convertToRGB(image);
            }

            try (ImageInputStream iis = ImageIO.createImageInputStream(new ByteArrayInputStream(imageBytes))) {
                Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);

                if (readers.hasNext()) {
                    ImageReader reader = readers.next();
                    try {
                        reader.setInput(iis);
                        image = reader.read(0);
                        log.info("使用ImageReader成功读取图片，格式: {}", reader.getFormatName());
                        return convertToRGB(image);
                    } finally {
                        reader.dispose();
                    }
                }
            }

            throw new IOException("无法读取图片，可能是不支持的格式或文件已损坏");

        } catch (Exception e) {
            log.error("读取图片失败: {}", e.getMessage(), e);
            throw new IOException("读取图片失败: " + e.getMessage(), e);
        }
    }

    private static BufferedImage convertToRGB(BufferedImage image) {
        if (image == null) {
            return null;
        }

        int type = image.getType();
        log.info("图片类型: {}, 色彩空间: {}", type, image.getColorModel().getColorSpace());

        if (type == BufferedImage.TYPE_INT_RGB || type == BufferedImage.TYPE_INT_ARGB) {
            log.info("图片已经是RGB格式，无需转换");
            return image;
        }

        log.info("转换图片为RGB格式...");
        BufferedImage rgbImage = new BufferedImage(
                image.getWidth(),
                image.getHeight(),
                BufferedImage.TYPE_INT_RGB);

        ColorConvertOp op = new ColorConvertOp(null);
        op.filter(image, rgbImage);

        log.info("图片转换完成");
        return rgbImage;
    }

    public static boolean isValidImage(byte[] imageBytes) {
        try {
            BufferedImage image = readImage(imageBytes);
            return image != null && image.getWidth() > 0 && image.getHeight() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    public static int[] getImageDimensions(byte[] imageBytes) throws IOException {
        BufferedImage image = readImage(imageBytes);
        if (image == null) {
            throw new IOException("无法读取图片尺寸");
        }
        return new int[] { image.getWidth(), image.getHeight() };
    }

    public static byte[] convertToWebp(byte[] imageBytes, float quality) throws IOException {
        BufferedImage image = readImage(imageBytes);
        if (image == null) {
            throw new IOException("无法读取图片");
        }

        log.info("开始转换为WebP格式，质量: {}", quality);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageWriter writer = ImageIO.getImageWritersByFormatName("webp").next();
            ImageOutputStream ios = ImageIO.createImageOutputStream(baos);
            writer.setOutput(ios);

            javax.imageio.ImageWriteParam param = writer.getDefaultWriteParam();
            if (param.canWriteCompressed()) {
                param.setCompressionMode(javax.imageio.ImageWriteParam.MODE_EXPLICIT);
                param.setCompressionQuality(quality);
            }

            writer.write(null, new javax.imageio.IIOImage(image, null, null), param);
            writer.dispose();
            ios.close();

            byte[] webpBytes = baos.toByteArray();
            log.info("WebP转换完成，原始大小: {} bytes, WebP大小: {} bytes, 压缩率: {}%",
                    imageBytes.length, webpBytes.length,
                    String.format("%.2f", (1 - (double) webpBytes.length / imageBytes.length) * 100));

            return webpBytes;
        } catch (Exception e) {
            log.error("WebP转换失败: {}", e.getMessage(), e);
            throw new IOException("WebP转换失败: " + e.getMessage(), e);
        }
    }

    public static byte[] createThumbnail(byte[] imageBytes, int targetSize, float quality) throws IOException {
        BufferedImage originalImage = readImage(imageBytes);
        if (originalImage == null) {
            throw new IOException("无法读取图片");
        }

        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();

        if (originalWidth <= targetSize && originalHeight <= targetSize) {
            log.info("图片尺寸 {}x{} 小于目标尺寸 {}，无需生成缩略图", originalWidth, originalHeight, targetSize);
            return convertToWebp(imageBytes, quality);
        }

        BufferedImage thumbnail = Scalr.resize(originalImage, Scalr.Method.QUALITY, Scalr.Mode.AUTOMATIC,
                targetSize, targetSize, Scalr.OP_ANTIALIAS);

        log.info("生成缩略图: {}x{} -> {}x{}", originalWidth, originalHeight, thumbnail.getWidth(), thumbnail.getHeight());

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageWriter writer = ImageIO.getImageWritersByFormatName("webp").next();
            ImageOutputStream ios = ImageIO.createImageOutputStream(baos);
            writer.setOutput(ios);

            javax.imageio.ImageWriteParam param = writer.getDefaultWriteParam();
            if (param.canWriteCompressed()) {
                param.setCompressionMode(javax.imageio.ImageWriteParam.MODE_EXPLICIT);
                param.setCompressionQuality(quality);
            }

            writer.write(null, new javax.imageio.IIOImage(thumbnail, null, null), param);
            writer.dispose();
            ios.close();

            byte[] thumbnailBytes = baos.toByteArray();
            log.info("缩略图生成完成，大小: {} bytes", thumbnailBytes.length);

            return thumbnailBytes;
        } catch (Exception e) {
            log.error("生成缩略图失败: {}", e.getMessage(), e);
            throw new IOException("生成缩略图失败: " + e.getMessage(), e);
        }
    }

    public static boolean isWebpSupported() {
        return ImageIO.getImageWritersByFormatName("webp").hasNext();
    }
}
