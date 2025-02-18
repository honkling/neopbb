package me.honkling.neopbb.script

import java.io.File
import java.net.URLClassLoader
import kotlin.io.path.Path
import kotlin.io.path.extension
import kotlin.io.path.listDirectoryEntries
import kotlin.script.experimental.annotations.KotlinScript
import kotlin.script.experimental.api.ScriptCompilationConfiguration
import kotlin.script.experimental.api.defaultImports
import kotlin.script.experimental.jvm.dependenciesFromCurrentContext
import kotlin.script.experimental.jvm.jvm
import kotlin.script.experimental.jvm.updateClasspath

@KotlinScript(
    fileExtension = "kts",
    compilationConfiguration = ScriptWithMavenDepsConfiguration::class
)
abstract class ScriptWithMavenDeps

class ScriptWithMavenDepsConfiguration : ScriptCompilationConfiguration({
    jvm {
        defaultImports("java.util.*")
        dependenciesFromCurrentContext(wholeClasspath = true)
        updateClasspath(
            // Libraries from class loader
            (javaClass.classLoader as URLClassLoader).urLs.map { File(it.path) } +
            // Plugins
            Path("plugins").listDirectoryEntries().filter { it.extension == "jar" }.map { it.toFile() }
        )
    }
})