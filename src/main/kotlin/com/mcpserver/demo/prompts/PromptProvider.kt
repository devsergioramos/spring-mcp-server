package com.mcpserver.demo.prompts

import io.modelcontextprotocol.spec.McpSchema
import io.modelcontextprotocol.spec.McpSchema.Role
import org.springaicommunity.mcp.annotation.McpPrompt
import org.springframework.stereotype.Service
import org.springframework.ai.util.ResourceUtils

@Service
class PromptProvider {
    private companion object {
        const val CODE_REFACTOR_PROMPT_PATH = "prompts/code-refactor-prompt.txt"
        const val UNIT_TEST_PROMPT_PATH = "prompts/unit-test-prompt.txt"
    }

    @McpPrompt(name = "codeRefactor", description = "Code Refactoring Prompt")
    fun codeRefactorPrompt(): McpSchema.GetPromptResult {
        val content = ResourceUtils.getText(CODE_REFACTOR_PROMPT_PATH)

        val prompt = McpSchema.PromptMessage(
            Role.USER,
            McpSchema.TextContent(content)
        )

        return McpSchema.GetPromptResult(
            "Code Refactoring Prompt",
            listOf(prompt),
        )
    }

    @McpPrompt(name = "unitTest", description = "Unit Test Prompt")
    fun writeUnitTestPrompt(): McpSchema.GetPromptResult {
        val content = ResourceUtils.getText(UNIT_TEST_PROMPT_PATH)

        val prompt = McpSchema.PromptMessage(
            Role.USER,
            McpSchema.TextContent(content)
        )

        return McpSchema.GetPromptResult(
            "Write Unit Test Prompt",
            listOf(prompt)
        )
    }
}