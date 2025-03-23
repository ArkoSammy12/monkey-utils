package io.github.arkosammy12.monkeyutils.settings

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import io.github.arkosammy12.monkeyconfig.builders.EnumSettingBuilder
import io.github.arkosammy12.monkeyconfig.settings.EnumSetting
import io.github.arkosammy12.monkeyconfig.util.ElementPath
import net.minecraft.command.CommandSource
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.util.StringIdentifiable
import java.util.concurrent.CompletableFuture

open class CommandControllableEnumSetting<E>(
    settingBuilder: EnumSettingBuilder<E>
) : EnumSetting<E>(settingBuilder), CommandControllable<E, StringArgumentType> where E : Enum<E>, E : StringIdentifiable {

    override val argumentType: StringArgumentType
        get() = StringArgumentType.word()

    override val commandPath: ElementPath
        get() = this.path

    @Throws(IllegalArgumentException::class)
    override fun getArgumentValue(ctx: CommandContext<out ServerCommandSource>, argumentName: String): E {
        val string: String = StringArgumentType.getString(ctx, argumentName)
        for (enumValue: E in this.value.default::class.java.enumConstants) {
            if (enumValue.asString() == string) {
                return enumValue
            }
        }
        throw IllegalArgumentException("Enum constant of type \"${this.value.default::class.java.simpleName}\" not found for name \"$string\"")
    }

    override fun getSuggestions(ctx: CommandContext<ServerCommandSource>, suggestionsBuilder: SuggestionsBuilder): CompletableFuture<Suggestions> {
        return CommandSource.suggestMatching(
            this.value.default::class.java.enumConstants
                .map { value -> value.asString() }
                .toMutableList(),
            suggestionsBuilder
        )
    }

}