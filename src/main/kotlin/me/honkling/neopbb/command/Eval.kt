@file:Command("eval", permission = "pbb.eval")

package me.honkling.neopbb.command

import me.honkling.commando.spigot.command.Command
import me.honkling.neopbb.instance
import me.honkling.neopbb.lib.mm
import me.honkling.neopbb.script.evaluateScript
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import java.util.*
import java.util.concurrent.Future
import javax.script.ScriptEngineManager
import kotlin.concurrent.thread
import kotlin.reflect.KClass
import kotlin.script.experimental.api.ResultValue
import kotlin.script.experimental.api.ScriptDiagnostic
import kotlin.script.experimental.api.valueOrThrow
import kotlin.script.experimental.jvm.util.isError

private val adminUniqueId = UUID.fromString("ac94d512-c072-41db-984f-e1203783d362")
private val imports = mutableListOf<String>()

private fun import(sender: CommandSender, import: String) {
    if (sender !is ConsoleCommandSender && (sender !is Player || sender.uniqueId != adminUniqueId))
        return

    sender.sendMessage("<success>Imported <good2>$import</good2>")
    imports += import
}

private fun eval(sender: CommandSender, input: String) {
    if (sender !is ConsoleCommandSender && (sender !is Player || sender.uniqueId != adminUniqueId))
        return

    val syncInput = "org.bukkit.Bukkit.getScheduler().callSyncMethod(instance) { $input }"
    sender.sendMessage("<gray>Evaluating...".mm)
    val symbols = mutableListOf<Pair<Pair<String, KClass<*>>, Any>>(
        "instance" to JavaPlugin::class to instance
    )

    if (sender is Player)
        symbols += "player" to Player::class to sender

    thread {
        val result = evaluateScript(syncInput, imports, *symbols.toTypedArray())

        if (result.isError()) {
            val errors = result.reports.filter { it.severity >= ScriptDiagnostic.Severity.WARNING }
            val displayResult = if (errors.size == 1) "result" else "results"
            sender.sendMessage("<failure>Failed with <bad2>${errors.size} $displayResult</bad2>:".mm)

            for (error in errors) {
                val start = error.location?.start
                val location = if (start != null) " (${start.line}:${start.col})" else ""
                sender.sendMessage("<bad><bad2>${error.severity.name}$location: ${error.message}".mm)
            }

            return@thread
        }

        val displayValue = when (val value = result.valueOrThrow().returnValue) {
            is ResultValue.Value -> {
                val trueValue = value.value

                if (trueValue is Future<*>)
                    trueValue.get().toString()
                else trueValue?.toString()
            }
            is ResultValue.Unit -> ""
            is ResultValue.Error -> "exception: ${value.error.message}"
            is ResultValue.NotEvaluated -> "not evaluated"
        }
        sender.sendMessage("<success>Done! Result:\n<good2>$displayValue</good2>".mm)
    }
}