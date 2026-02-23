package com.troodon.d2.editor

import com.intellij.codeInsight.completion.CompletionType
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class D2StylePropertyCompletionProviderTest : BasePlatformTestCase() {

    fun testCompletionInsideStyleBlock() {
        myFixture.configureByText("test.d2", """
            node: {
              style: {
                <caret>
              }
            }
        """.trimIndent())

        myFixture.complete(CompletionType.BASIC)
        val lookupStrings = myFixture.lookupElementStrings

        assertNotNull(lookupStrings)
        assertTrue(lookupStrings!!.contains("opacity"))
        assertTrue(lookupStrings.contains("stroke"))
        assertTrue(lookupStrings.contains("fill"))
        assertTrue(lookupStrings.contains("fill-pattern"))
        assertTrue(lookupStrings.contains("stroke-width"))
        assertTrue(lookupStrings.contains("font-size"))
        assertTrue(lookupStrings.contains("bold"))
        assertTrue(lookupStrings.contains("animated"))
        assertTrue(lookupStrings.contains("3d"))
        assertTrue(lookupStrings.contains("text-transform"))
    }

    fun testNoStyleCompletionOutsideStyleBlock() {
        myFixture.configureByText("test.d2", """
            node: {
              <caret>
            }
        """.trimIndent())

        myFixture.complete(CompletionType.BASIC)
        val lookupStrings = myFixture.lookupElementStrings

        if (lookupStrings != null) {
            // Should not suggest style-specific properties outside style block
            assertFalse(lookupStrings.contains("opacity"))
            assertFalse(lookupStrings.contains("stroke-width"))
            assertFalse(lookupStrings.contains("fill-pattern"))
        }
    }

    fun testExcludeDefinedStyleProperties() {
        myFixture.configureByText("test.d2", """
            node: {
              style: {
                fill: red
                opacity: 0.5
                <caret>
              }
            }
        """.trimIndent())

        myFixture.complete(CompletionType.BASIC)
        val lookupStrings = myFixture.lookupElementStrings

        assertNotNull(lookupStrings)
        // Already defined should be excluded
        assertFalse(lookupStrings!!.contains("fill"))
        assertFalse(lookupStrings.contains("opacity"))
        // Others should still be present
        assertTrue(lookupStrings.contains("stroke"))
        assertTrue(lookupStrings.contains("bold"))
    }
}
