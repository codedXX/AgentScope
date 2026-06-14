package com.imooc.graph.conf;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.alibaba.cloud.ai.graph.*;
import com.alibaba.cloud.ai.graph.action.AsyncNodeAction;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.alibaba.cloud.ai.graph.state.strategy.AppendStrategy;
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy;
import com.imooc.graph.edges.TotalBudgetEdge;
import com.imooc.graph.node.*;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * author: Imooc
 * description: 工作流配置类
 * date: 2025
 */

/* **********************
 *
 * 工作流涉及的对象：
 *
 * 1. OverAllState : 全局状态对象，任何节点都能够读取和修改
 * 全局状态是贯穿整个工作流
 *
 * 2. StateGraph: 工作流框架对象，
 * 工作流框架可以添加节点以及节点之间的箭头连线
 *
 * 3. CompiledGraph: 工作流的编译对象
 *
 *
 * *********************/

@Configuration
public class GraphConfiguration {

    //将链式工作流配置方法设置为Bean
    @Bean(name="parallelGraph")
    //配置链式工作流
    public StateGraph parallelGraph() throws GraphStateException {

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

        ChatClient.Builder chatClient = ChatClient.builder(chatModel);



        /* **********************
         *
         * SpringAi Alibaba 1.1
         * 通过 KeyStrategyFactory 设置状态(State)合并策略
         * 设置完成后，无需手动将合并策略注册到全局状态 OverAllState中
         *
         *
         * *********************/


        //设置全局状态的合并策略
        KeyStrategyFactory keyStrategyFactory =()-> {

            /* **********************
             *
             * 全局状态OverAllState以Key-Value的形式存放上下文数据
             *
             * KeyStrategy不只是指代key所对应的Value，还包括Value的更新策略：
             * 有2种更新策略：
             * 1. AppendStrategy：在旧值的基础上追加新值
             * 2. ReplaceStrategy：将新值覆盖旧值
             *
             * 更新策略是限定了节点对于OverAllState的Key所对应Value的操作
             *
             * *********************/
            Map<String, KeyStrategy> map = new HashMap<String, KeyStrategy>();
            //添加key以及Key所对应Value的更新策略

            //更新策略为覆盖
            //费用
            map.put("TotalBudget", new ReplaceStrategy());
            //prompt
            map.put("route", new ReplaceStrategy());
            map.put("trip", new ReplaceStrategy());
            //输出
            map.put("message", new AppendStrategy());

            return map;
        };


        /* **********************
         *
         * StateGraph的创建需要3个参数：
         * 1. 工作流框架的名称
         * 2. 状态(State)合并策略 (必须传入)
         * 3. Json框架：采用的是Gson，因为已经自动创建出来，所以不需要传入这个参数
         *
         * *********************/
        StateGraph stateGraph =  new StateGraph(
                "parallel",
                //状态(State)合并策略
                keyStrategyFactory);

        /* **********************
         *
         * 添加节点,按道理是应该添加一个节点对象Node,
         * 采用
         * addNode(String,Node),
         *
         * 但是，
         * addNode(String,Node)是被
         * addNode(String id, AsyncNodeActionWithConfig actionWithConfig)
         * 所调用，
         *
         * addNode(String id, AsyncNodeActionWithConfig actionWithConfig)
         * 又被
         * addNode(String id, AsyncNodeAction action)
         * 所调用
         *
         * 所以，
         * 我们直接调用addNode(String id, AsyncNodeAction action)就可以了
         *
         * AsyncNodeAction的实例生成是采用
         * AsyncNodeAction.node_async()
         *
         * AsyncNodeAction：基于异步的节点操作
         *
         * node_async()需要传入NodeAction
         * NodeAction：是一个函数式接口
         *
         *
         * *********************/



        //添加节点, 需要传入2个参数（1.节点id，2.AsyncNodeAction）

        //路线制定节点
        stateGraph.addNode("RouteMakingNode",AsyncNodeAction.node_async(new RouteMakingNode(chatClient)));
        //行程规划节点
        stateGraph.addNode("TripPlannerNode",AsyncNodeAction.node_async(new TripPlannerNode(chatClient)));
        //费用统筹节点
        stateGraph.addNode("BudgetNode",AsyncNodeAction.node_async(new BudgetNode()));
        //汇总节点
        stateGraph.addNode("AggregationNode",AsyncNodeAction.node_async(new AggregationNode()));
        //任务分发节点
        stateGraph.addNode("TaskAssignmentNode",AsyncNodeAction.node_async(new TaskAssignmentNode()));

        //添加节点间的箭头连线

        //工作流开始
        stateGraph.addEdge(stateGraph.START,"TaskAssignmentNode");

        //工作流结束
        stateGraph.addEdge("AggregationNode",stateGraph.END);

        //任务分发节点 分发任务到 路线制定节点
        stateGraph.addEdge("TaskAssignmentNode","RouteMakingNode");
        //任务分发节点 分发任务到 行程规划节点
        stateGraph.addEdge("TaskAssignmentNode","TripPlannerNode");


        //路线制定节点 汇总 费用统筹节点
        stateGraph.addEdge("RouteMakingNode","BudgetNode");
        //行程规划节点 汇总 费用统筹节点
        stateGraph.addEdge("TripPlannerNode","BudgetNode");



        //费用统筹节点进行费用判断
        stateGraph
                .addConditionalEdges(
                        "BudgetNode",
                        //对行程规划费用进行条件判断
                        new TotalBudgetEdge(),
                        //费用统筹节点可能会跳转到两个节点，这第三个参数就是这两个节点的集合
                        Map.of(
                                //行程规划费用超支, 会返回行程规划节点
                                "back","TripPlannerNode",
                                //不超支, 汇总到汇总节点
                                "continue","AggregationNode"
                        )
                );



        //以PlantUML格式打印工作流图形
        GraphRepresentation graph =
        stateGraph.getGraph(GraphRepresentation.Type.PLANTUML);

        System.out.println("#############工作流图形化展示 #############");
        System.out.println("############# 将下面内容复制到 parallel.puml #############");

        System.out.println(graph.content());

        System.out.println("将 circle start<<input>> as __START__");
        System.out.println("改为 circle start as __START__");
        System.out.println("##########################");


        return stateGraph;

    }
}
