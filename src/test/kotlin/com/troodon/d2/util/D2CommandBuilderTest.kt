package com.troodon.d2.util

import org.junit.Assert.assertEquals
import org.junit.Test

class D2CommandBuilderTest {

    @Test
    fun `buildCommand without WSL returns plain command`() {
        val result = D2CommandBuilder.buildCommand("/usr/bin/d2", listOf("--version"), false)
        assertEquals(listOf("/usr/bin/d2", "--version"), result)
    }

    @Test
    fun `buildCommand with WSL no distro`() {
        val result = D2CommandBuilder.buildCommand("d2", listOf("--version"), true)
        assertEquals(listOf("wsl.exe", "-e", "d2", "--version"), result)
    }

    @Test
    fun `buildCommand with WSL and distro`() {
        val result = D2CommandBuilder.buildCommand("d2", listOf("--version"), true, "Arch")
        assertEquals(listOf("wsl.exe", "-d", "Arch", "-e", "d2", "--version"), result)
    }

    @Test
    fun `convertToWslPath with drive letter`() {
        assertEquals("/mnt/c/Users/foo/file.d2", D2CommandBuilder.convertToWslPath("C:\\Users\\foo\\file.d2"))
    }

    @Test
    fun `convertToWslPath with lowercase drive letter`() {
        assertEquals("/mnt/d/work/test.d2", D2CommandBuilder.convertToWslPath("d:\\work\\test.d2"))
    }

    @Test
    fun `convertToWslPath with forward slashes`() {
        assertEquals("/mnt/c/Users/foo/file.d2", D2CommandBuilder.convertToWslPath("C:/Users/foo/file.d2"))
    }

    @Test
    fun `convertToWslPath with UNC wsl$ path`() {
        assertEquals("/home/foo/file.d2", D2CommandBuilder.convertToWslPath("\\\\wsl\$\\Ubuntu\\home\\foo\\file.d2"))
    }

    @Test
    fun `convertToWslPath with UNC wsl localhost path`() {
        assertEquals("/home/foo/file.d2", D2CommandBuilder.convertToWslPath("\\\\wsl.localhost\\Ubuntu\\home\\foo\\file.d2"))
    }

    @Test
    fun `convertToWslPath with unix path passes through`() {
        assertEquals("/mnt/c/test", D2CommandBuilder.convertToWslPath("/mnt/c/test"))
    }

    @Test
    fun `buildD2RenderCommand with WSL converts output path`() {
        val result = D2CommandBuilder.buildD2RenderCommand(
            "d2", "--sketch", "C:\\tmp\\output.svg", true, "Ubuntu"
        )
        assertEquals(
            listOf("wsl.exe", "-d", "Ubuntu", "-e", "d2", "--sketch", "-", "/mnt/c/tmp/output.svg"),
            result
        )
    }

    @Test
    fun `buildD2RenderCommand without WSL keeps paths`() {
        val result = D2CommandBuilder.buildD2RenderCommand(
            "/usr/bin/d2", "--sketch", "/tmp/output.svg", false
        )
        assertEquals(listOf("/usr/bin/d2", "--sketch", "-", "/tmp/output.svg"), result)
    }

    @Test
    fun `buildD2FmtCommand with WSL converts file path`() {
        val result = D2CommandBuilder.buildD2FmtCommand(
            "d2", "C:\\tmp\\file.d2", true
        )
        assertEquals(listOf("wsl.exe", "-e", "d2", "fmt", "/mnt/c/tmp/file.d2"), result)
    }

    @Test
    fun `buildD2VersionCommand with WSL`() {
        val result = D2CommandBuilder.buildD2VersionCommand("d2", true, "Debian")
        assertEquals(listOf("wsl.exe", "-d", "Debian", "-e", "d2", "--version"), result)
    }
}
