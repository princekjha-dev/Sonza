package com.sonza.app.core.db

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import java.io.File
import java.util.Properties

/**
 * Desktop driver: JdbcSqliteDriver writing to the per-user app data dir.
 *
 * Path conventions:
 *  Windows: %LOCALAPPDATA%\Sonza\sonza.db
 *  macOS:   ~/Library/Application Support/Sonza/sonza.db
 *  Linux:   ~/.local/share/Sonza/sonza.db
 *
 * Falls back to the user's home directory if the OS-specific path can't
 * be resolved. The schema is materialised on first connection — Desktop
 * starts with an empty DB; no Room migration runs here.
 */
actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        val dbFile = resolveDatabaseFile()
        dbFile.parentFile?.mkdirs()
        val driver = JdbcSqliteDriver(
            url = "jdbc:sqlite:${dbFile.absolutePath}",
            properties = Properties(),
        )
        // SQLDelight needs explicit schema creation on first run for JDBC
        // — AndroidSqliteDriver handles this automatically, JDBC does not.
        if (!dbFile.exists() || dbFile.length() == 0L) {
            SonzaDatabase.Schema.create(driver)
        }
        return driver
    }

    private fun resolveDatabaseFile(): File {
        val osName = System.getProperty("os.name").orEmpty().lowercase()
        val appDir = when {
            osName.contains("win") -> {
                val localAppData = System.getenv("LOCALAPPDATA")
                    ?: System.getProperty("user.home") + "\\AppData\\Local"
                File(localAppData, "Sonza")
            }
            osName.contains("mac") -> {
                val home = System.getProperty("user.home")
                File("$home/Library/Application Support/Sonza")
            }
            else -> {
                val xdgData = System.getenv("XDG_DATA_HOME")
                    ?: (System.getProperty("user.home") + "/.local/share")
                File("$xdgData/Sonza")
            }
        }
        return File(appDir, "sonza.db")
    }
}
