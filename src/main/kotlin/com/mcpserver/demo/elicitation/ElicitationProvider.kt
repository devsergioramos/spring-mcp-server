package com.mcpserver.demo.elicitation

import io.modelcontextprotocol.server.McpSyncServerExchange
import io.modelcontextprotocol.spec.McpSchema
import org.springaicommunity.mcp.annotation.McpTool
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class ElicitationProvider {
    data class Item(
        var id: UUID,
        var name: String,
        var price: Double
    )

    val items = mutableListOf(
        Item(UUID.randomUUID(),"Pizza", 10.0),
        Item(UUID.randomUUID(),"Burger", 15.0)
    )

    val presentation = items.map { item -> "${item.id}: ${item.name} - R$${item.price}"}

    val elicitationItem = McpSchema.ElicitRequest.builder()
        .message("Qual item você deseja alterar o preço? \n $presentation")
        .requestedSchema(
            mapOf(
                "type" to "object",
                "properties" to mapOf(
                    "itemId" to mapOf(
                        "type" to "string"
                    ),
                    "itemPrice" to mapOf(
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
        description = "Use esta ferramenta quando o parceiro desejar alterar o preço de um item no seu cardápio."
    )
    fun updateMerchantItemPrice(exchange: McpSyncServerExchange): String {
        val elicitationItemResult = exchange.createElicitation(elicitationItem)

        return when (elicitationItemResult.action.name) {
            "ACCEPT" -> {
                val itemId = elicitationItemResult.content["itemId"] as String
                val itemPrice = elicitationItemResult.content["itemPrice"]?.toString()?.toDoubleOrNull()

                if (itemId.isBlank() || itemPrice == null) {
                    return "Preço inválido"
                }

                val item = items.find { it.id == UUID.fromString(itemId) }

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