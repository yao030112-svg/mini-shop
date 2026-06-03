package com.minishop.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minishop.config.WxMiniAppConfig;
import com.minishop.exception.BusinessException;
import com.minishop.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

/**
 * 微信API服务
 */
@Slf4j
@Service
public class WxApiService {

    @Autowired
    private WxMiniAppConfig wxConfig;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 调用微信code2Session接口，获取openid和session_key
     *
     * @param code 微信登录临时凭证
     * @return openid
     */
    public String code2Session(String code) {
        // 构建请求URL
        String url = UriComponentsBuilder.fromHttpUrl(wxConfig.getCode2sessionUrl())
                .queryParam("appid", wxConfig.getAppid())
                .queryParam("secret", wxConfig.getSecret())
                .queryParam("js_code", code)
                .queryParam("grant_type", "authorization_code")
                .build().toUriString();

        log.info("调用微信code2Session接口，code: {}", code);

        try {
            // 发送HTTP GET请求
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                
                // 检查是否有错误
                if (jsonNode.has("errcode")) {
                    int errcode = jsonNode.get("errcode").asInt();
                    String errmsg = jsonNode.get("errmsg").asText();
                    log.error("微信code2Session失败，errcode: {}, errmsg: {}", errcode, errmsg);
                    throw new BusinessException(ErrorCode.UNAUTHORIZED.getCode(), 
                        "微信登录失败: " + errmsg);
                }
                
                // 成功获取openid
                String openid = jsonNode.get("openid").asText();
                log.info("微信登录成功，openid: {}", openid);
                
                return openid;
            } else {
                log.error("微信code2Session接口响应异常");
                throw new BusinessException(ErrorCode.UNAUTHORIZED.getCode(), "微信登录失败");
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("调用微信code2Session接口异常", e);
            throw new BusinessException(ErrorCode.UNAUTHORIZED.getCode(), "微信登录异常: " + e.getMessage());
        }
    }
}
