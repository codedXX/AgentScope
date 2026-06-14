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
 * description: 路线制定节点
 * date: 2025
 */

public class RouteMakingNode implements NodeAction {

    private final ChatClient chatClient;

    public RouteMakingNode(ChatClient.Builder builder) {
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
        String prompt = (String)state.value("route").orElse("");
        //输出
        String message = (String) state.value("message").orElse("");


        String userMessage = "制定" +
                prompt +
                "驾车路线,并预估费用"+
                "生成json字符串," +
                "格式是{'route':...,'budget':...,'total':....}" +
                "total字段是总费用, 是数字型" +
                "控制在5000个字符";

        System.out.println("======= 路线制定节点 Prompt  ============");
        System.out.println(userMessage);


        // 和大模型交互
        String res = chatClient.prompt()
                .user(userMessage)
                .call()
                .content();

       System.out.println("======= 路线制定节点 输出  =========");
       System.out.println(res);

        Map<String, Object> map = new HashMap<String, Object>();


        map.put("message",res);
        //费用
        map.put("TotalBudget",2);

        // 将响应存储在状态中
        return map;
    }
}
