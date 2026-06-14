package com.imooc.controller;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.alibaba.cloud.ai.graph.NodeOutput;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.alibaba.cloud.ai.graph.streaming.OutputType;
import com.alibaba.cloud.ai.graph.streaming.StreamingOutput;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import com.imooc.tools.WeatherTool;

/**
 * author: Imooc
 * description: 通过ReActAgent和大模型Api
 * date: 2025
 */

@RestController
public class ReActAgentController {



    /**
     * author: Imooc
     * description: Agent 流式响应
     * @param :
     * @return java.lang.String
     */
    @GetMapping(value = "/simple/agent",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public void streamChat() throws GraphRunnerException {


        // 创建 DashScope API 实例
        DashScopeApi dashScopeApi = DashScopeApi.builder()
                .apiKey("sk-255506ca196b48f38e686b3e82efac58")
                .build();


        // 创建 ChatModel
        ChatModel chatModel = DashScopeChatModel.builder()
                .dashScopeApi(dashScopeApi)
                .defaultOptions(
                        DashScopeChatOptions.builder()
                        .model("deepseek-r1").build()
                )
                .build();



        //用户输入
        String message = "在咖啡馆里，我也想要杯星巴克，这里的星巴克指的是什么?";


        //工具方法
        ToolCallback weatherTool = FunctionToolCallback
                .builder("getWeather", new WeatherTool())
                .description("查询城市天气")
                .inputType(String.class)
                .build();

        /* **********************
         *
         *
         * SpringAi Alibaba 1.0 还处于和大模型对话阶段 ( ChatClinet )
         *
         * SpringAi Alibaba 1.1
         * 进入到 Agent 自主决策与自主执行 的时代:
         *
         * ReActAgent 除了具备了大脑, 工具使用, 记忆能力，环境感知,
         * 还具备2个能力：
         * 规划能力 ( 复杂任务分解 )，
         * 自主决策能力
         *
         *
         * Agentic AI（智能体式AI）： 多个 ReActAgent 的协调合作。
         * 是一种设计范式，
         * 强调将AI系统构建为具备自主性、适应性和协作能力的智能体集合。
         * 其核心目标是通过多Agent协同解决复杂问题
         *
         *
         *
         *
         *
         * *********************/


        // 创建 agent
        ReactAgent agent = ReactAgent.builder()
                // Agent的名称 (必须)
                .name("weather_agent")
                // Agent的大脑 (语言大模型)
                .model(chatModel)
                // Agent的手脚(调用工具)
                //.tools(weatherTool)
                // Agent的眼睛和耳朵(RAG)
                //.hooks()
                // 系统提示词
                .systemPrompt("You are a helpful assistant")
                // Agent的记忆 (多轮对话)
                //.saver(new MemorySaver())
                .build();


        //阻塞的回答返回
//        AssistantMessage assistantMessage = agent.call(message);
        //获取回答
//        assistantMessage.getText();


        //流式返回

        Flux<NodeOutput> stream = agent.stream(message);

        stream.subscribe(
                output -> {
                    // 检查是否为 StreamingOutput 类型
                    if (output instanceof StreamingOutput streamingOutput) {
                        OutputType type = streamingOutput.getOutputType();

                        // 处理模型推理的流式输出
                        if (type == OutputType.AGENT_MODEL_STREAMING) {
                            // 流式增量内容，逐步显示
                            System.out.print(streamingOutput.message().getText());
                        } else if (type == OutputType.AGENT_MODEL_FINISHED) {
                            // 模型推理完成，可获取完整响应
                            System.out.println("\n模型输出完成");
                        }

                        // 处理工具调用完成（目前不支持 STREAMING）
                        if (type == OutputType.AGENT_TOOL_FINISHED) {
                            System.out.println("工具调用完成: " + output.node());
                        }

                        // 对于 Hook 节点，通常只关注完成事件（如果Hook没有有效输出可以忽略）
                        if (type == OutputType.AGENT_HOOK_FINISHED) {
                            System.out.println("Hook 执行完成: " + output.node());
                        }
                    }
                },
                error -> System.err.println("错误: " + error),
                () -> System.out.println("Agent 执行完成")
        );

    }

}
