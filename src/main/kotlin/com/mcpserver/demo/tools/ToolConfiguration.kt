package com.mcpserver.demo.tools

import org.springframework.ai.tool.method.MethodToolCallbackProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ToolConfiguration {
    @Bean
    fun methodToolCallbackProvider(methodToolList: List<MethodTool>): MethodToolCallbackProvider =
        MethodToolCallbackProvider
            .builder()
            .toolObjects(*methodToolList.toTypedArray())
            .build()
}