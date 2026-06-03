package com.minishop.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 微信小程序配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "wx.miniapp")
public class WxMiniAppConfig {

    /**
     * 小程序AppID
     */
    private String appid;

    /**
     * 小程序AppSecret
     */
    private String secret;

    /**
     * 微信登录code2session接口地址
     */
    private String code2sessionUrl;
}
