package com.mcpserver.demo.tools

import org.springframework.ai.tool.annotation.Tool
import org.springframework.stereotype.Service
import java.time.LocalDate


@Service
class ActualDateTool : MethodTool {

    @Tool(
        description = "Returns the actual date",
    )
    fun actualDate() = LocalDate.now()
}