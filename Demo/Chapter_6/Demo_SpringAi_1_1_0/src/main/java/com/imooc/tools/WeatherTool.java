package com.imooc.tools;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;

import java.util.function.Function;


/**
 * author: Imooc
 * description: Agent工具定义
 * date: 2026
 */

// 定义天气查询工具
//将 BiFunction 转换为工具
public class WeatherTool implements Function<String, String> {

    @Override
    public String apply(String s) {
        return null;
    }
}