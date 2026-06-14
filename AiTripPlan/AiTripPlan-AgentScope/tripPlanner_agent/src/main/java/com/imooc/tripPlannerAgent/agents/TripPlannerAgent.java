package com.imooc.tripPlannerAgent.agents;

import com.imooc.commons.data.ResponseSchema;
import com.imooc.commons.utils.PromptUtils;
import com.imooc.commons.utils.ToolUtils;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.agent.StreamOptions;
import io.agentscope.core.message.Msg;
import io.agentscope.core.model.DashScopeChatModel;
import io.agentscope.core.model.ExecutionConfig;
import io.agentscope.core.tool.Toolkit;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import com.imooc.commons.utils.AgentUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Duration;

/**
 * author: Imooc
 * description: 行程规划Agent
 * date: 2026
 */

@Component
@Slf4j
public class TripPlannerAgent {

    @Resource
    private AgentUtils agentUtils;

    //景点推荐SubAgent
    @Resource
    private SuggestSightAgent suggestSightAgent;

    @Bean
    public ReActAgent getTripPlannerAgent() throws URISyntaxException, IOException {


        ToolUtils toolUtils = new ToolUtils();
        //将智能体(子Agent)作为工具
        Toolkit toolkit = toolUtils.getToolkit(suggestSightAgent.getSuggestSightAgent());
        // 打印所有工具信息
        toolUtils.getTools();



        /* **********************
         *
         * 1.
         * AgentScope框架自带了注册中心： AgentScopeA2aServer
         *
         * 2.
         * AgentScope框架将智能体卡片注册到注册中心,有2种方案：
         * a. 通过SpringBoot, 以Bean的形式自动注入
         * b. 手动写入注册中心, 主要针对于AgentScopeA2aServer
         *
         *
         * *********************/




        //行程规划Agent
        return agentUtils.getReActAgentBuilder(
                "TripPlannerAgent",
                "擅长处理旅游行程规划"
        )
                .sysPrompt(
                    """
                    你是一个旅游行程规划助手。
                    制定包括旅游景点，小吃，住宿这些方面，
                    并且费用性价比高的旅游行程。
                    """)
                //挂载工具包
                .toolkit(toolkit)
                .build();




        //=========== 手动写入注册中心，项目不用种方式 START ====

//        //行程规划Agent 智能体卡片
//        ConfigurableAgentCard agentCard =  new ConfigurableAgentCard.Builder()
//                .name("TripPlannerAgent")
//                .description("行程规划Agent")
//                .build();
//
//        //将智能体卡片写入到AgentScope自带的注册中心
//        AgentScopeA2aServer.builder(builder)
//                .agentCard(agentCard)
//                .deploymentProperties(
//                       new DeploymentProperties(
//                               "localhost",
//                               8080)
//                )
//                .build();

        //还需要AgentScopeA2aServer启动


        //======== 手动写入注册中心，项目不用种方式 END ====


    }


}
