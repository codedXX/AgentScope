package com.imooc.managerAgent.controller;

import com.imooc.commons.data.ResponseSchema;
import com.imooc.commons.data.PromptSchema;
import io.agentscope.core.ReActAgent;
import jakarta.annotation.Resource;
import com.imooc.managerAgent.agents.ManagerAgent;
import org.springframework.web.bind.annotation.*;

/**
 * author: Imooc
 * description: 用户和Agent互动的Api 接口
 * date: 2026
 */

@RestController
public class ManagerAgentController {

    @Resource
    private ManagerAgent managerAgent;

    // 用户提交旅游规划的Prompt
    @RequestMapping(
            value = "/trip",
            produces = "application/json;charset=UTF-8",
            method = RequestMethod.POST)
    public ResponseSchema tripPlan(@RequestBody PromptSchema input) {

        ReActAgent manager = managerAgent.getManagerAgent();
        ResponseSchema response = managerAgent.run(input.getPrompt());

        return response;
    }
}
