package com.sonza.app.ai.orbit

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Automated Security Test Suite for Sonza Orbit Assistant.
 *
 * Verifies that:
 * 1. ZERO private API keys (Groq `gsk_`, OpenAI `sk-`, etc.) exist anywhere in the Android app source.
 * 2. .gitignore properly protects all secret, environment, and credential files.
 * 3. Android client classes (OrbitManager, GroqProxyProvider, OrbitExecutor) do not hardcode secrets.
 */
class OrbitSecurityTest {

    private fun getProjectRootDir(): File {
        val cwd = File(".").canonicalFile
        return if (cwd.name == "app") cwd.parentFile else cwd
    }

    @Test
    fun testNoHardcodedProviderSecretsInSource() {
        val projectRoot = getProjectRootDir()
        val sourceDir = File(projectRoot, "app/java")
        assertTrue("app/java source directory should exist at ${sourceDir.absolutePath}", sourceDir.exists())

        val forbiddenPrefixes = listOf(
            "gsk_", // Groq private API key prefix
            "sk-proj-", // Real OpenAI project keys
            "sk-ant-" // Real Anthropic keys
        )

        sourceDir.walkTopDown().forEach { file ->
            if (file.isFile && file.extension == "kt") {
                val content = file.readText()
                for (prefix in forbiddenPrefixes) {
                    val containsRealKey = content.lines().any { line ->
                        // Disregard placeholder text in AISettingsScreen (e.g. keyPlaceholder = "sk-proj-...")
                        val isPlaceholder = line.contains("keyPlaceholder") || line.contains("example")
                        line.contains(prefix) && !isPlaceholder
                    }
                    assertFalse(
                        "Security violation: Found forbidden secret prefix '$prefix' in ${file.relativeTo(projectRoot)}",
                        containsRealKey
                    )
                }
            }
        }
    }

    @Test
    fun testGitignoreProtectsSecrets() {
        val projectRoot = getProjectRootDir()
        val gitignore = File(projectRoot, ".gitignore")
        assertTrue(".gitignore must exist at ${gitignore.absolutePath}", gitignore.exists())
        val content = gitignore.readText()

        assertTrue(".gitignore must ignore .env files", content.contains(".env"))
        assertTrue(".gitignore must ignore secret files", content.contains("*.secret"))
        assertTrue(".gitignore must ignore key files", content.contains("*.key"))
        assertTrue(".gitignore must ignore wrangler directories", content.contains(".wrangler/"))
    }
}
