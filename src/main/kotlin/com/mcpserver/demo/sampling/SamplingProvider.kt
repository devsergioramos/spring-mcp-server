package com.mcpserver.demo.sampling

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.modelcontextprotocol.server.McpSyncServerExchange
import io.modelcontextprotocol.spec.McpSchema
import org.springaicommunity.mcp.annotation.McpTool
import org.springaicommunity.mcp.annotation.McpToolParam
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Service
import java.util.UUID
import java.text.NumberFormat
import java.util.Locale

@Service
class SamplingProvider(
    private val objectMapper: ObjectMapper
) {
    data class Product(
        val id: UUID,
        val name: String,
        val category: String,
        val price: Double,
    )

    private fun formatProductsForHuman(products: List<Product>): String {
        val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))

        return buildString {
            appendLine("📦 Lista de Produtos Disponíveis:\n")
            products.forEachIndexed { index, product ->
                appendLine("${index + 1}. ${product.name}")
                appendLine("   Categoria: ${product.category}")
                appendLine("   Preço: ${currencyFormatter.format(product.price)}")
                appendLine()
            }
            appendLine("Total: ${products.size} produto(s)")
        }
    }

    private fun loadProducts(): List<Product> {
        val resource = ClassPathResource("data/products.json")
        return objectMapper.readValue(resource.inputStream)
    }

    private val systemPrompt = """
        Você é um assistente de filtragem de dados de alta precisão
        
        Regras de Processamento
        1. Interpretação do Filtro: Analise o filtro para identificar os critérios exatos.
            - Palavras-chave: Termos que devem constar no campo name.   
            - Categorias: Termos que devem constar no campo category.
            - Preços: Condições lógicas baseadas no campo price.
            - Combinações: O filtro pode combinar múltiplos critérios.
        2. Filtragem de Dados: Itere sobre a lista_produtos e 
            -selecione apenas os itens que satisfazem todos os critérios identificados no filtro.
        
        Formato da Resposta
        Sua resposta deve ser exclusivamente em texto natural, legível para humanos.
        - Se produtos forem encontrados: 
        Inicie com uma breve confirmação. Liste os produtos de forma clara usando marcadores (*). 
        Para cada produto, inclua seu nome (name) e seu preço (price), 
        formatando o preço adequadamente para a moeda Brasileira (ex: R$ 549,90).
        - Se nenhum produto for encontrado: Informe ao usuário de forma clara e 
        amigável que a busca não retornou resultados para aquele filtro específico.
        
        Lista de Produtos: {productList}
    """

    @McpTool(
        description = "Use essa ferramenta para listar os produtos"
    )
    fun listItems(
        @McpToolParam(
            required = false,
            description = """
                Filtro em linguagem natural 
                descrevendo os critérios de 
                busca desejados pelo usuário.
            """)
        filter: String? = null,
        exchange: McpSyncServerExchange
    ): String {
        val products = loadProducts()

        if (filter == null) {
            return formatProductsForHuman(products)
        }

        val createSamplingRequest = McpSchema.CreateMessageRequest.builder()
            .messages(listOf(
                McpSchema.SamplingMessage(
                    McpSchema.Role.USER,
                    McpSchema.TextContent(filter)
                )
            )).systemPrompt(
                systemPrompt.replace("{productList}", products.toString())
            )
            .build()

        val samplingResponse = exchange.createMessage(createSamplingRequest)

        val result = samplingResponse.content as McpSchema.TextContent

        return result.text
    }
}