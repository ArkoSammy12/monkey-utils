package io.github.arkosammy12.monkeyutils.settings

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import com.mojang.brigadier.tree.LiteralCommandNode
import io.github.arkosammy12.monkeyconfig.base.ConfigManager
import io.github.arkosammy12.monkeyconfig.base.Setting
import io.github.arkosammy12.monkeyconfig.base.getSetting
import io.github.arkosammy12.monkeyconfig.util.ElementPath
import io.github.arkosammy12.monkeyutils.commands.CommandVisitor
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import java.util.concurrent.CompletableFuture

interface CommandControllable<out V : Any, A : ArgumentType<*>> {

    val argumentType: A

    val commandPath: ElementPath

    fun getArgumentValue(ctx: CommandContext<out ServerCommandSource>, argumentName: String): V

    fun getSuggestions(ctx: CommandContext<ServerCommandSource>, suggestionsBuilder: SuggestionsBuilder) : CompletableFuture<Suggestions> =
        this.argumentType.listSuggestions(ctx, suggestionsBuilder)

    val onValueSetCallback: (CommandContext<out ServerCommandSource>, ConfigManager) -> Int
        get() = get@{ ctx, configManager ->
            try {
                val setting: Setting<V, *> = configManager.getSetting<V, Setting<V, *>>(this.commandPath) ?: return@get Command.SINGLE_SUCCESS.also {
                    ctx.source.sendMessage(Text.literal("Config setting \"${this.commandPath.asList.last()}\" was not found!").formatted(Formatting.RED))
                }
                val newValue: V = this.getArgumentValue(ctx, this.commandPath.asList.last())
                setting.value.raw = newValue
                ctx.source.sendMessage(Text.literal("${this.commandPath.asList.last()} has been set to : ${setting.value}"))
                return@get Command.SINGLE_SUCCESS
            } catch (e: Exception) {
                ctx.source.sendMessage(Text.literal("Error attempting to set value for ${this.commandPath.asList.last()}: ${e.message}"))
                return@get Command.SINGLE_SUCCESS
            }
            return@get Command.SINGLE_SUCCESS
        }

    val onValueGetCallback: (CommandContext<out ServerCommandSource>, ConfigManager) -> Int
        get() = get@{ ctx, configManager ->
            val setting: Setting<V, *> = configManager.getSetting<V, Setting<V, *>>(this.commandPath) ?: return@get Command.SINGLE_SUCCESS.also {
                    ctx.source.sendMessage(Text.literal("Config setting \"${this.commandPath.asList.last()}\" was not found!").formatted(Formatting.RED))
            }
            val settingComment: String? = if (this is Setting<*, *>) this.comment else null
            val currentValue: V = setting.value.raw
            if (settingComment != null) {
                ctx.source.sendMessage(Text.literal("Description: $settingComment"))
            }
            ctx.source.sendMessage(Text.literal("${this.commandPath.asList.last()} currently set to: $currentValue"))
            return@get Command.SINGLE_SUCCESS
        }

    fun accept(parentNode: LiteralCommandNode<ServerCommandSource>? = null, visitor: CommandVisitor) {
        visitor.visit(parentNode, this)
    }

}