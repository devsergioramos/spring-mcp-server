package com.mcpserver.demo.tools

import org.springaicommunity.mcp.annotation.McpTool
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.format.DateTimeFormatter


@Service
class ToolProvider {
    @McpTool(
        description = "Returns the actual date",
    )
    fun actualDate() = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
}