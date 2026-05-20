package com.wallpaper.server.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Service
public class WechatService {

    @Value("${wechat.miniprogram.app-id:}")
    private String appId;

    @Value("${wechat.miniprogram.app-secret:}")
    private String appSecret;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String getOpenIdByCode(String code) {
        if (appId == null || appId.isEmpty() || appSecret == null || appSecret.isEmpty()) {
            log.warn("微信小程序 appId 或 appSecret 未配置，使用 code 作为标识");
            return code;
        }

        String url = String.format(
            "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
            appId, appSecret, code
        );

        try {
            log.info("调用微信API获取openId, code: {}", code);
            String response = restTemplate.getForObject(url, String.class);
            log.info("微信API响应: {}", response);

            Map<String, Object> result = objectMapper.readValue(response, Map.class);

            if (result.containsKey("openid")) {
                return (String) result.get("openid");
            } else {
                log.error("获取openId失败: {}", response);
                return null;
            }
        } catch (Exception e) {
            log.error("调用微信API异常", e);
            return null;
        }
    }
}
