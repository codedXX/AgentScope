package com.imooc.graph.controller;

import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.alibaba.cloud.ai.graph.streaming.StreamingOutput;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * author: Imooc
 * description: 工作流控制器
 * date: 2025
 */

@RestController
public class WorkFlowController {

    /* **********************
     *
     * CompiledGraph提供了2种运行工作流的方式：
     *
     * 1. invoke(): 以阻塞的方式运行工作流，工作流的结果是一次性返回
     *
     * 2. stream(); 以非阻塞，流式的方式运行工作流，
     * 执行一个节点，就返回这个节点完成的结果~
     *
     * *********************/



    //工作流编译后对象
    private final CompiledGraph compiledGraph;


    /* **********************
     *
     * @Qualifier注入指定名称的Bean
     *
     * *********************/


    public WorkFlowController(@Qualifier("parallelGraph") StateGraph graph) throws GraphStateException {

        //工作流编译
        this.compiledGraph = graph.compile();

    }

    @GetMapping(value = "/graph/workflow",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public void graphWorkflow() {

        // prompt
        Map<String, Object> prompts = Map.of(
                "route", "深圳到惠州",
                "trip","惠州1天1夜"
        );

        //输出
        Optional<OverAllState> res = this.compiledGraph.invoke(prompts);

        //获取结果
        if (res.isPresent()) {
            OverAllState state = res.get();
            //打印出总结
            System.out.println(state.value("message"));



        }else {
            System.out.println("没有结果: "+res);
        }
    }



}
