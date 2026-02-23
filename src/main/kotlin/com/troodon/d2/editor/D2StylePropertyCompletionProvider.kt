package com.troodon.d2.editor

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.util.ProcessingContext

class D2StylePropertyCompletionProvider : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(
        parameters: CompletionParameters,
        context: ProcessingContext,
        result: CompletionResultSet
    ) {
        val styleBlockInfo = getStyleBlockInfo(parameters) ?: return
        val definedProperties = getDefinedProperties(styleBlockInfo)
        STYLE_PROPERTIES.forEach { property ->
            if (!definedProperties.contains(property)) {
                result.addElement(LookupElementBuilder.create(property))
            }
        }
    }

    private data class StyleBlockInfo(val content: String)

    private fun getStyleBlockInfo(parameters: CompletionParameters): StyleBlockInfo? {
        val text = parameters.originalFile.text
        val offset = parameters.offset
        val textBeforeCursor = text.substring(0, offset)

        // Find the last "style" block pattern before cursor
        val stylePattern = Regex("""style\s*[:{]\s*\{?""")
        val matches = stylePattern.findAll(textBeforeCursor)
        val lastMatch = matches.lastOrNull() ?: return null

        val afterStyleMatch = textBeforeCursor.substring(lastMatch.range.last + 1)

        // Count braces to ensure we're still inside the style block
        var openBraces = 0
        // Account for the opening brace in the style pattern
        if (lastMatch.value.contains("{")) {
            // If pattern matched "style: {" or "style {", we start with one open brace
            val braceCount = lastMatch.value.count { it == '{' }
            openBraces = braceCount - 1 // -1 because we need net open braces after the block opener
        }

        afterStyleMatch.forEach { char ->
            when (char) {
                '{' -> openBraces++
                '}' -> openBraces--
            }
        }

        // We're inside if braces are still open (>= 0 means not closed)
        if (openBraces >= 0) {
            return StyleBlockInfo(afterStyleMatch)
        }

        return null
    }

    private fun getDefinedProperties(styleBlockInfo: StyleBlockInfo): Set<String> {
        val definedProperties = mutableSetOf<String>()
        val propertyPattern = Regex("""^\s*([\w-]+)\s*:""", RegexOption.MULTILINE)
        propertyPattern.findAll(styleBlockInfo.content).forEach { match ->
            val propertyName = match.groupValues[1]
            if (STYLE_PROPERTIES.contains(propertyName)) {
                definedProperties.add(propertyName)
            }
        }
        return definedProperties
    }

    companion object {
        val STYLE_PROPERTIES = listOf(
            "opacity",
            "stroke",
            "fill",
            "fill-pattern",
            "stroke-width",
            "stroke-dash",
            "border-radius",
            "shadow",
            "3d",
            "multiple",
            "double-border",
            "font",
            "font-size",
            "font-color",
            "animated",
            "bold",
            "italic",
            "underline",
            "text-transform"
        )
    }
}
