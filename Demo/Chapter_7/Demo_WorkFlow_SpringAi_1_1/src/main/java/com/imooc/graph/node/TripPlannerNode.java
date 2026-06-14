package com.imooc.graph.node;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import org.springframework.ai.chat.client.ChatClient;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * author: Imooc
 * description: 行程规划节点
 * date: 2025
 */

public class TripPlannerNode implements NodeAction {

    private final ChatClient chatClient;

    public TripPlannerNode(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    /**
     * author: Imooc
     * description: 节点对于全局状态的操作
     * @param state:
     * @return java.util.Map<java.lang.String,java.lang.Object>
     */
    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {

        //根据key获取全局状态中的值
        //费用
        Object object = state.value("TotalBudget");
        //prompt
        String prompt = (String)state.value("trip").orElse("");




        String userMessage = "制定" +
                 prompt +
                "行程规划, 包括住宿,景点,小吃" +
                "生成json字符串," +
                "格式是{'plan':...,'budget':...,'total':....}" +
                "total字段是总费用, 是数字型" +
                "控制在8000个字符";

        System.out.println("======= 行程规划节点 Prompt  ============");
        System.out.println(userMessage);

        // 获取大模型输出
        String res = chatClient.prompt()
                .user(userMessage)
                .call()
                .content();
//
        Map<String, Object> map = new HashMap<String, Object>();

        System.out.println("======= 行程规划节点 输出  =========");
        System.out.println(res);


        map.put("message",res);
        //总费用
        map.put("TotalBudget",1);

        // 将响应存储在状态中
        return map;

    }
}
