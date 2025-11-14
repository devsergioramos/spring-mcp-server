package com.mcpserver.demo.elicitation

import io.modelcontextprotocol.server.McpSyncServerExchange
import io.modelcontextprotocol.spec.McpSchema
import org.springaicommunity.mcp.annotation.McpTool
import org.springframework.stereotype.Service

@Service
class ElicitationProvider {
    data class Item(
        val id: Int,
        var name: String,
        var price: Double
    )

    val items = mutableListOf(
        Item(1,"Pizza", 10.0),
        Item(2,"Burger", 15.0)
    )

    val presentation = items.joinToString("\n") { item -> "ID: ${item.id} - Nome: ${item.name} - Preço: R$${item.price}" }

    val elicitationItem = McpSchema.ElicitRequest.builder()
        .message("Qual item você deseja alterar o preço? \n $presentation")
        .requestedSchema(
            mapOf(
                "type" to "object",
                "properties" to mapOf(
                    "id" to mapOf(
                        "type" to "number"
                    ),
                    "price" to mapOf(
                        "type" to "string"
                    )
                )))
        .build()

    val elicitationConfirm = McpSchema.ElicitRequest.builder()
        .message("Deseja confirmar alteração?")
        .requestedSchema(
            mapOf(
                "type" to "object",
                "properties" to mapOf(
                    "confirm" to mapOf(
                        "type" to "boolean"
                    )
                )
            )
        )
        .build()

    @McpTool(
        description = """
            Use essa ferramenta para solicitações de alteração de preço de items.
            A ferramenta é capaz de solicitar ao usuario qual item deseja alterar e o preço do item
        """
    )
    fun updateItemPrice(exchange: McpSyncServerExchange): String {
        val elicitationItemResult = exchange.createElicitation(elicitationItem)

        return when (elicitationItemResult.action.name) {
            "ACCEPT" -> {
                val itemId = elicitationItemResult.content["id"] as Int
                val itemPrice =
                    elicitationItemResult.content["price"]?.toString()?.toDoubleOrNull() ?: return "Preço inválido"

                val item = items.find { it.id == itemId }

                if (item != null) {
                    val elicitationConfirmResult = exchange.createElicitation(elicitationConfirm)
                    return when (elicitationConfirmResult.action.name) {
                        "ACCEPT" -> {
                            item.price = itemPrice
                            "Alteração confirmada e realizada. ${item.name} agora custa R$$itemPrice"
                        }
                        "DECLINE" -> "Alteração cancelada pelo usuário"
                        "CANCEL" -> "Operação cancelada"
                        else -> "Ação desconhecida"
                    }
                } else {
                    "Item não encontrado"
                }
            }
            "DECLINE" -> "Alteração cancelada pelo usuário"
            "CANCEL" -> "Operação cancelada"
            else -> "Ação desconhecida"
        }
    }
}