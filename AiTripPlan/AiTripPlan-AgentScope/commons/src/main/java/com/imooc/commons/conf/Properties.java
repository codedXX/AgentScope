package com.imooc.commons.conf;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.DefaultPropertySourceFactory;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Optional;

/**
 * author: Imooc
 * description: 配置类
 * date: 2026
 */

@Configuration
@Getter
public class Properties {

    //大模型名称
    @Value("${agent.model_name}")
    private String modelName;

    //阿里云DashScope Key
    @Value("${agent.alibaba_dashscope_key}")
    private String alibabaDashscopeKey;

    //百度地图MCP服务端地址
    @Value("${mcp.baidu_map_addr}")
    private String baiduMapAddr;

    //LangFuse 服务地址
    @Value("${spring.ai.observation.langfuse.endpoint}")
    private String endpoint;

    //LangFuse 私钥
    @Value("${spring.ai.observation.langfuse.secret-key}")
    private String secretKey;

    //LangFuse 公钥
    @Value("${spring.ai.observation.langfuse.public-key}")
    private String publicKey;
}



