package com.wallpaper.server.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Data
@Slf4j
@Configuration
@ConfigurationProperties(prefix = "aliyun.oss")
public class OssConfig {
    private String endpoint;
    private String accessKeyId;
    private String accessKeySecret;
    private String bucketName;
    private String urlPrefix;

    @Bean
    public OSS ossClient() {
        if (accessKeyId == null || accessKeyId.isEmpty() ||
                accessKeySecret == null || accessKeySecret.isEmpty() ||
                endpoint == null || endpoint.isEmpty()) {
            log.warn("OSS配置不完整，OSS服务将不可用。请配置 OSS_ACCESS_KEY_ID 和 OSS_ACCESS_KEY_SECRET 环境变量");
            return null;
        }
        log.info("初始化OSS客户端，Endpoint: {}, Bucket: {}", endpoint, bucketName);
        return new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
    }
}
