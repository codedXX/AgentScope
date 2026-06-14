package com.imooc.tripPlannerAgent;

import com.imooc.tripPlannerAgent.agents.TripPlannerAgent;
import io.agentscope.core.ReActAgent;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * author: Imooc
 * description: 启动类
 * date: 2026
 */

@SpringBootApplication(scanBasePackages = {"com.imooc"})
public class TripPlannerAgentApplication {
    public static void main(String[] args) {

        SpringApplication.run(TripPlannerAgentApplication.class, args);

    }
}
