package me.honkling.neopbb.script

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.honkling.neopbb.instance
import org.bukkit.Bukkit
import java.io.File
import java.util.concurrent.CompletableFuture
import kotlin.reflect.KClass
import kotlin.script.experimental.api.*
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvm.baseClassLoader
import kotlin.script.experimental.jvm.jvm
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost
import kotlin.script.experimental.jvmhost.createJvmCompilationConfigurationFromTemplate

fun evaluateScript(
    file: File,
    imports: List<String>,
    vararg symbols: Pair<Pair<String, KClass<*>>, Any>
) = evaluateScript(file.toScriptSource(), imports, *symbols)

fun evaluateScript(
    input: String,
    imports: List<String>,
    vararg symbols: Pair<Pair<String, KClass<*>>, Any>
) = evaluateScript(input.toScriptSource(), imports, *symbols)

fun evaluateScript(
    script: SourceCode,
    imports: List<String>,
    vararg symbols: Pair<Pair<String, KClass<*>>, Any>
): ResultWithDiagnostics<EvaluationResult> {
    val compilationConfiguration = createJvmCompilationConfigurationFromTemplate<ScriptWithMavenDeps> {
        defaultImports("me.honkling.neopbb.profile.profile", "org.bukkit.Bukkit", *imports.toTypedArray())
        providedProperties(*symbols.map { it.first.first to it.first.second }.toTypedArray())
    }

    val evaluationConfiguration = ScriptEvaluationConfiguration({
        jvm {
            baseClassLoader(this::class.java.classLoader)
        }

        providedProperties(*symbols.map { it.first.first to it.second }.toTypedArray())
    })

    return BasicJvmScriptingHost().eval(script, compilationConfiguration, evaluationConfiguration)
}