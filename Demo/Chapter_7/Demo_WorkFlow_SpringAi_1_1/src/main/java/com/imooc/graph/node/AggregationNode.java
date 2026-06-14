package com.imooc.graph.node;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;

import java.util.Map;

/**
 * author: Imooc
 * description: 汇总节点
 * date: 2026
 */

public class AggregationNode implements NodeAction {
    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {

        String message = state.value("message").toString();

        String result = "汇总节点输出: " + message;
        return Map.of("message", result);
    }
}
