package com.imooc.managerAgent.agents;

import com.imooc.commons.data.ResponseSchema;
import com.imooc.commons.utils.LangFuseUtils;
import com.imooc.managerAgent.tool.RemoteAgentTool;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.agent.Event;
import io.agentscope.core.hook.ActingChunkEvent;
import io.agentscope.core.hook.Hook;
import io.agentscope.core.hook.HookEvent;
import io.agentscope.core.model.StructuredOutputReminder;
import io.agentscope.core.model.ToolSchema;
import io.agentscope.core.plan.PlanNotebook;
import io.agentscope.core.tool.Toolkit;
import com.imooc.managerAgent.hook.planHook;
import com.imooc.managerAgent.plan.TripPlan;
import io.agentscope.core.tracing.TracerRegistry;
import io.agentscope.core.tracing.telemetry.TelemetryTracer;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import com.imooc.commons.utils.AgentUtils;
import com.imooc.commons.utils.ToolUtils;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;

/**
 * author: Imooc
 * description: 主管Agent
 * date: 2026
 */

@Component
@Slf4j
public class ManagerAgent {

    @Resource
    private AgentUtils agentUtils;

    @Resource
    private LangFuseUtils langFuseUtils;

    private ReActAgent agent;

    /**
     * author: Imooc
     * description: Agent 创建
     * @param :
     * @return null
     */
    public ReActAgent getManagerAgent() {


        //PlanNotebook
        TripPlan plan = new TripPlan();
        //Toolkit
        ToolUtils toolUtils = new ToolUtils();
        //将远程Agent封装为工具的封装注册到工具包
        Toolkit toolkit = toolUtils.getToolkit(new RemoteAgentTool());

        // 打印所有工具信息
        toolUtils.getTools();

        //计划对象
        PlanNotebook planNotebook = plan.getPlan();


        agent = agentUtils.getReActAgentBuilder(
                "ManagerAgent",
                "负责用户需求的解决方案和执行计划制定, 以及任务分发"
        )
                .sysPrompt("""
                    你是一个旅游管理主管。
                    当用户要求你规划旅游行程时，
                    请先创建一个详细的计划，
                    以及执行计划步骤,
                    并对每个计划步骤,要列出擅长执行这个步骤任务的Agent
                    然后按计划逐步执行。
                    """)
                /* **********************
                 *
                 * ReActAgent 能自主分解复杂任务, 并且会自动生成计划步骤：
                 * 1. .enablePlan()
                 *    1.1 但enablePlan方法不需要传递任何参数，也就是说无法对智能体的计划做自定义的设置
                 * 2. .planNotebook()
                 *
                 * .enablePlan() 内部调用了 PlanNotebook的Builder 构造方法
                 * 是采用默认的 PlanNotebook 的属性
                 *
                 * .planNotebook() 它是传入 PlanNotebook的 实例,
                 * 可以对 PlanNotebook 进行自定义
                 *
                 *
                 * PlanNotebook对象 是Agent能自主分解任务和步骤执行的核心
                 *
                 * PlanNotebook整个流程：
                 * 1. 复杂任务分解
                 * 2. 生成执行步骤
                 * 3. 状态跟踪
                 * 4. 动态调整
                 * 5. 任务完成
                 *
                 * PlanNotebook对象：自主规划 (PlanAct) + 自主决策 (ReAct)
                 *
                 *
                 *
                 *
                 * *********************/

                //自定义配置执行计划
                .planNotebook(planNotebook)
                //拦截器
                .hook(new planHook(planNotebook))
                //工具包
                .toolkit(toolkit)
                //结构化输出
                .structuredOutputReminder(StructuredOutputReminder.PROMPT)
                .build();


        return agent;


    }

    /**
     * author: Imooc
     * description: Agent 运行
     * @param :
     * @return void
     */
    public ResponseSchema run(String prompt) {
//        String prompt = """
//        帮我制定2026年元旦，
//        深圳到惠州3日游自驾游计划，
//        请包含吃住行，天气，酒店，餐饮美食。
//
//        你可以调用以下Agent处理子任务：
//        - routeMaking Agent: 擅长处理自驾游路线制定
//        - tripPlanner Agent: 擅长处理景点行程规划
//
//        - 每个子任务要注明调用的Agent
//        """;

        Flux<Event> stream = agentUtils.streamResponse(agent,prompt);

        //把响应打印出来
        ResponseSchema result =
                stream
                //阻塞直到结束
                .blockLast()
                .getMessage()
                .getStructuredData(ResponseSchema.class);

        return result;

    }

}
