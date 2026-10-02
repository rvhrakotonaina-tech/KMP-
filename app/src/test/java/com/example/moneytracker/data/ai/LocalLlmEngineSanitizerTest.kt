package com.example.moneytracker.data.ai

import org.junit.Assert.assertEquals
import org.junit.Test

class LocalLlmEngineSanitizerTest {

    @Test
    fun testEnglishSentence_preservesTextExactly() {
        val input = "Prioritize important purchases."
        val output = LocalLlmEngine.sanitizeResponse(input)
        assertEquals("Prioritize important purchases.", output)
    }

    @Test
    fun testFrenchSentence_preservesAccentedCharactersExactly() {
        val input = "Prioriser l'acquisition et le déploiement des produits importants."
        val output = LocalLlmEngine.sanitizeResponse(input)
        assertEquals("Prioriser l'acquisition et le déploiement des produits importants.", output)
    }

    @Test
    fun testFrenchFullAccentPreservation() {
        val input = "é è ê ë à â ç î ï ô ù û ü œ æ"
        val output = LocalLlmEngine.sanitizeResponse(input)
        assertEquals("é è ê ë à â ç î ï ô ù û ü œ æ", output)
    }

    @Test
    fun testNumberedList_noDuplicatedOrInjectedAccents() {
        val input = "1. Prioriser l'acquisition et le déploiement des produits importants comme le smartphone ou les articles de vêtement."
        val output = LocalLlmEngine.sanitizeResponse(input)
        assertEquals(
            "1. Prioriser l'acquisition et le déploiement des produits importants comme le smartphone ou les articles de vêtement.",
            output
        )
    }

    @Test
    fun testLiteralNewlineEscapeSequences_convertedToRealNewlinesWithoutCorruptingUnicode() {
        val input = "Conseil 1: Économiser sur les sorties.\\nConseil 2: Réduire les dépenses de vêtement."
        val expected = "Conseil 1: Économiser sur les sorties.\nConseil 2: Réduire les dépenses de vêtement."
        val output = LocalLlmEngine.sanitizeResponse(input)
        assertEquals(expected, output)
    }

    @Test
    fun testQwenSpecialTokens_strippedWithoutCorruptingUnicode() {
        val input = "<|im_start|>assistant\nPrioriser l'acquisition et le déploiement des produits importants.<|im_end|>"
        val output = LocalLlmEngine.sanitizeResponse(input)
        assertEquals("Prioriser l'acquisition et le déploiement des produits importants.", output)
    }

    @Test
    fun testMarkdownFormatting_cleanedWithoutCorruptingUnicode() {
        val input = "**Important**: Prioriser l'acquisition et le `déploiement` des produits."
        val output = LocalLlmEngine.sanitizeResponse(input)
        assertEquals("Important: Prioriser l'acquisition et le déploiement des produits.", output)
    }

    @Test
    fun testFullPipelineEndToEndSimulation() {
        // Raw model output containing Markdown, literal \n, special token and French accented characters
        val rawModelOutput = "**Conseils budgétaires**:\\n• Prioriser l'acquisition et le déploiement des produits importants.\\n• Éviter les dépenses superflues.<|im_end|>"
        val sanitized = LocalLlmEngine.sanitizeResponse(rawModelOutput)

        // ViewModel processing logic:
        val lines = sanitized.lines().filter { it.isNotBlank() }
            .map { it.trim().removePrefix("•").trim().removePrefix("-").trim() }
        val insights = lines.take(3)

        assertEquals("Conseils budgétaires:", insights[0])
        assertEquals("Prioriser l'acquisition et le déploiement des produits importants.", insights[1])
        assertEquals("Éviter les dépenses superflues.", insights[2])
    }

    @Test
    fun testMojibakeRecovery_coutsAndConserve() {
        val input = "Réduire les coÃ»ts et garder l'argent conservÃ©."
        val output = LocalLlmEngine.sanitizeResponse(input)
        assertEquals("Réduire les coûts et garder l'argent conservé.", output)
    }

    @Test
    fun testUserRequirementSentence_mojibakeDecodedAndAccentsPreserved() {
        val input = "Prioriser l'essentiel et les coÃ»ts importants.\nL'argent est mieux conservÃ© lorsqu'il est nÃ©cessairement investi."
        val output = LocalLlmEngine.sanitizeResponse(input)
        val expected = "Prioriser l'essentiel et les coûts importants.\nL'argent est mieux conservé lorsqu'il est nécessairement investi."
        assertEquals(expected, output)
    }

    @Test
    fun testUserRequirementSentence_alreadyValidUnicodePreserved() {
        val input = "Prioriser l'essentiel et les coûts importants.\nL'argent est mieux conservé lorsqu'il est nécessairement investi."
        val output = LocalLlmEngine.sanitizeResponse(input)
        assertEquals(input, output)
    }

    @Test
    fun testAllFrenchAccents_mojibakeDecoded() {
        // UTF-8 bytes for: é è ê ë à â î ï ô ù û ü ç œ
        // Decoded via single-byte charset: Ã© Ã¨ Ãª Ã« Ã  Ã¢ Ã® Ã¯ Ã´ Ã¹ Ã» Ã¼ Ã§ Å“
        val input = "Ã© Ã¨ Ãª Ã« Ã\u00A0 Ã¢ Ã® Ã¯ Ã´ Ã¹ Ã» Ã¼ Ã§ Å“"
        val output = LocalLlmEngine.sanitizeResponse(input)
        assertEquals("é è ê ë à â î ï ô ù û ü ç œ", output)
    }

    @Test
    fun testLiteralSlashN_convertedSafelyWithoutAlteringLegitimateText() {
        val input = "Conseil 1: Prioriser les coûts./nConseil 2: Épargner régulièrement./n• Point 3"
        val expected = "Conseil 1: Prioriser les coûts.\nConseil 2: Épargner régulièrement.\n• Point 3"
        val output = LocalLlmEngine.sanitizeResponse(input)
        assertEquals(expected, output)
    }

    @Test
    fun testLiteralSlashN_doesNotAlterLegitimateText() {
        // URLs or math notation (e.g. 1/n) or normal words should not be altered
        val input = "Voir https://example.com/net ou ratio 1/n pour vos calculs."
        val output = LocalLlmEngine.sanitizeResponse(input)
        assertEquals("Voir https://example.com/net ou ratio 1/n pour vos calculs.", output)
    }

    @Test
    fun testMixedContentWithEmoji_mojibakeDecodedEmojiPreserved() {
        val input = "• Réduire les coÃ»ts 💡 et épargner 💰"
        val output = LocalLlmEngine.sanitizeResponse(input)
        assertEquals("• Réduire les coûts 💡 et épargner 💰", output)
    }
}

