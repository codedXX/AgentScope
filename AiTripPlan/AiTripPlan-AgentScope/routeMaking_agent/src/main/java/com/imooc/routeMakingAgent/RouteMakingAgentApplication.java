package com.imooc.routeMakingAgent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * author: Imooc
 * description: 启动类
 * date: 2026
 */

@SpringBootApplication(scanBasePackages = {"com.imooc"})
public class RouteMakingAgentApplication {
    public static void main(String[] args) {

        SpringApplication.run(RouteMakingAgentApplication.class, args);
    }
}
