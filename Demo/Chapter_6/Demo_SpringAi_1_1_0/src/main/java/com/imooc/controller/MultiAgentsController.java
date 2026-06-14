package com.imooc.controller;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.agent.AgentTool;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.flow.agent.LlmRoutingAgent;
import com.alibaba.cloud.ai.graph.agent.flow.agent.ParallelAgent;
import com.alibaba.cloud.ai.graph.agent.flow.agent.SequentialAgent;
import com.alibaba.cloud.ai.graph.agent.flow.agent.SupervisorAgent;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

/**
 * author: Imooc
 * description: 多Agent协同工作流
 * date: 2026
 */

@RestController
public class MultiAgentsController {

    @GetMapping(value = "/agent/workflow",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public void multiAgent() throws GraphRunnerException {

        // 创建 DashScope API 实例
        DashScopeApi dashScopeApi = DashScopeApi.builder()
                .apiKey("sk-255506ca196b48f38e686b3e82efac58")
                .build();


        // 创建 ChatModel
        ChatModel chatModel = DashScopeChatModel.builder()
                .dashScopeApi(dashScopeApi)
                .defaultOptions(
                        DashScopeChatOptions.builder()
                                .model("qwen3-32b").build()
                )
                .build();



        /* **********************
         *
         * 旅游规划Agent团队协同合作逻辑：
         *
         * 1. 路线制定agent 和 行程规划agent 都嵌入了 费用统筹agent
         * 2. 路线制定agent 业务执行 和 行程规划agent 业务执行 没有业务依赖关系，是并行执行的业务
         * 3. 主管agent 负责业务分发的角色，将业务分发给相应的Agent
         *
         *
         * 通过工作流Agent ( FlowAgent )将执行业务的Agent (ReActAgent) 按照流程逻辑串联起来
         *
         *
         * *********************/


        // 费用统筹agent
        ReactAgent budgetAgent =
                ReactAgent.builder()
                        .name("budgetAgent")
                        .description("你是负责预估费用")
                        .model(chatModel)
                        .tools()
                        .build();



        // 路线制定agent
        ReactAgent routeMakingAgent =
                ReactAgent.builder()
                        .name("routeMakingAgent")
                        .description("负责制定驾车路线")
                        .instruction(
                                "擅长驾车路线的规划," +
                                "包括了路线距离,高速费用,油费" +
                                "返回性价比最优的驾车路线, 及预估费用," +
                                "以表格形式展示"
                        )
                        .model(chatModel)
                        // 将一个ReActAgent作为工具添加到另外一个ReActAgent
                        .tools(AgentTool.getFunctionToolCallback(budgetAgent))
                        .returnReasoningContents(true)
                        //输出
                        .outputKey("route_making")
                        .build();


        // 行程规划agent
        ReactAgent tripPlannerAgent =
                ReactAgent.builder()
                        .name("tripPlanAgent")
                        .description("负责规划旅游行程")
                        .instruction(
                                "擅长旅游行程的规划," +
                                "包括了景点,本地小吃,住宿" +
                                "返回性价比最优的行程规划, 及预估费用," +
                                "以表格形式展示"
                        )
                        .model(chatModel)
                        // 将一个ReActAgent作为工具添加到另外一个ReActAgent
                        .tools(AgentTool.getFunctionToolCallback(budgetAgent))
                        //输出
                        .outputKey("trip_plan")
                        .build();



        //并行Agent
        // ParallelAgent是工作流Agent，不是ReAct架构的Agent
        //工作流Agent是将多个Agent按照流程逻辑组织起来
        //ParallelAgent是多个Agent按照并行执行的流程逻辑进行协同
        ParallelAgent parallelAgent =  ParallelAgent.builder()
                .name("parallelAgent")
                .description("并行执行多个任务, 以表格形式进行总结")
                .subAgents(List.of(tripPlannerAgent,routeMakingAgent))
                //输出
                .mergeOutputKey("merged_results")
                .build();


        // 主管agent
        //LlmRoutingAgent是工作流Agent，不是ReAct架构的Agent
        //LlmRoutingAgent是让语言大模型动态的分发任务 (起到路由的作用)
//        LlmRoutingAgent managerAgent = LlmRoutingAgent.builder()
        SupervisorAgent managerAgent = SupervisorAgent .builder()
                .name("managerAgent")
                .description("智能路由到合适的Agent去执行")
                .instruction(
                        """
                        ## 可用的子Agent及其职责
                                                
                        ### routeMakingAgent
                        - **功能**: 擅长驾车路线制定
                      
                                                
                        ### tripPlanAgent
                        - **功能**: 擅长住宿,景点的行程规划
                      
                        """
                )
                .model(chatModel)
                .subAgents(List.of(tripPlannerAgent,routeMakingAgent))
                .build();


        //工作流
        //SequentialAgent是工作流Agent，不是ReAct架构的Agent
        //SequentialAgent将多个Agent按照顺序的流程逻辑进行协同
        //将路由Agent(llmRoutingAgent)以及并行Agent(parallelAgent)按照顺序流程逻辑串联起来
        SequentialAgent sequentialAgent = SequentialAgent.builder()
                .name("sequentialAgent")
                .description(
                        "旅游规划工作流：" +
                        "路由选择合适Agent执行 ->" +
                        "并行执行路线制定和行程规划 -> " +
                        "以表格形式总结"
                )
                .subAgents(List.of(managerAgent,parallelAgent))
                .build();


        // 启动工作流
        Optional<OverAllState> res = sequentialAgent.invoke(
                "帮我规划深圳到惠州性价比高的驾车路线, " +
                "以及制定1天1夜的旅游行程"
        );

        //获取结果
        if (res.isPresent()) {
            OverAllState state = res.get();

            //打印出路线制定agent的输出
            System.out.println("======= 驾车路线 ============");
            System.out.println(state.value("route_making"));
            //打印出行程规划agent的输出
            System.out.println("======= 旅游行程 ============");
            System.out.println(state.value("trip_plan"));
            //打印出总结
            System.out.println("======= 表格总结 ============");
            System.out.println(state.value("merged_results"));



        }else {
            System.out.println("没有结果: "+res);
        }

    }
}
