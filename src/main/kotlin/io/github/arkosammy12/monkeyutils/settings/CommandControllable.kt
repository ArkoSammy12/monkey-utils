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
import net.minecraft.commands.CommandSourceStack
import net.minecraft.network.chat.Component
import net.minecraft.ChatFormatting
import java.util.concurrent.CompletableFuture

interface CommandControllable<out V : Any, A : ArgumentType<*>> {

    val argumentType: A

    val commandPath: ElementPath

    fun getArgumentValue(ctx: CommandContext<out CommandSourceStack>, argumentName: String): V

    fun getSuggestions(ctx: CommandContext<CommandSourceStack>, suggestionsBuilder: SuggestionsBuilder) : CompletableFuture<Suggestions> =
        this.argumentType.listSuggestions(ctx, suggestionsBuilder)

    val onValueSetCallback: (CommandContext<out CommandSourceStack>, ConfigManager) -> Int
        get() = get@{ ctx, configManager ->
            try {
                val setting: Setting<V, *> = configManager.getSetting<V, Setting<V, *>>(this.commandPath) ?: return@get Command.SINGLE_SUCCESS.also {
                    ctx.source.sendSystemMessage(Component.literal("Config setting \"${this.commandPath.asList.last()}\" was not found!").withStyle(ChatFormatting.RED))
                }
                val newValue: V = this.getArgumentValue(ctx, this.commandPath.asList.last())
                setting.value.raw = newValue
                ctx.source.sendSystemMessage(Component.literal("${this.commandPath.asList.last()} has been set to : ${setting.value.raw}"))
                return@get Command.SINGLE_SUCCESS
            } catch (e: Exception) {
                ctx.source.sendSystemMessage(Component.literal("Error attempting to set value for ${this.commandPath.asList.last()}: ${e.message}"))
                return@get Command.SINGLE_SUCCESS
            }
            return@get Command.SINGLE_SUCCESS
        }

    val onValueGetCallback: (CommandContext<out CommandSourceStack>, ConfigManager) -> Int
        get() = get@{ ctx, configManager ->
            val setting: Setting<V, *> = configManager.getSetting<V, Setting<V, *>>(this.commandPath) ?: return@get Command.SINGLE_SUCCESS.also {
                    ctx.source.sendSystemMessage(Component.literal("Config setting \"${this.commandPath.asList.last()}\" was not found!").withStyle(ChatFormatting.RED))
            }
            val settingComment: String? = if (this is Setting<*, *>) this.comment else null
            val currentValue: V = setting.value.raw
            if (settingComment != null) {
                ctx.source.sendSystemMessage(Component.literal("Description: $settingComment"))
            }
            ctx.source.sendSystemMessage(Component.literal("${this.commandPath.asList.last()} currently set to: $currentValue"))
            return@get Command.SINGLE_SUCCESS
        }

    fun accept(parentNode: LiteralCommandNode<CommandSourceStack>? = null, visitor: CommandVisitor) {
        visitor.visit(parentNode, this)
    }

}
