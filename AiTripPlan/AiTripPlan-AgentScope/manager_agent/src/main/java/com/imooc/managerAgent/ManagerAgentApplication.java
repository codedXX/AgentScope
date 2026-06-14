package com.imooc.managerAgent;

import com.alibaba.nacos.api.exception.NacosException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * author: Imooc
 * description: 启动类
 * date: 2026
 */

@SpringBootApplication(scanBasePackages = {"com.imooc"})
public class ManagerAgentApplication {
    public static void main(String[] args) throws NacosException
    {

        SpringApplication.run(ManagerAgentApplication.class, args);
    }
}
