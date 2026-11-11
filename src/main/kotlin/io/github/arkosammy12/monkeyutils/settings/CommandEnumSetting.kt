package io.github.arkosammy12.monkeyutils.settings

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import io.github.arkosammy12.monkeyconfig.builders.EnumSettingBuilder
import io.github.arkosammy12.monkeyconfig.settings.EnumSetting
import io.github.arkosammy12.monkeyconfig.util.ElementPath
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.commands.CommandSourceStack
import net.minecraft.util.StringRepresentable
import java.util.concurrent.CompletableFuture

open class CommandEnumSetting<E>(
    settingBuilder: EnumSettingBuilder<E>
) : EnumSetting<E>(settingBuilder), CommandControllable<E, StringArgumentType> where E : Enum<E>, E : StringRepresentable {

    override val argumentType: StringArgumentType
        get() = StringArgumentType.word()

    override val commandPath: ElementPath
        get() = this.path

    @Throws(IllegalArgumentException::class)
    override fun getArgumentValue(ctx: CommandContext<out CommandSourceStack>, argumentName: String): E {
        val string: String = StringArgumentType.getString(ctx, argumentName)
        for (enumValue: E in this.value.default::class.java.enumConstants) {
            if (enumValue.getSerializedName() == string) {
                return enumValue
            }
        }
        throw IllegalArgumentException("Enum constant of type \"${this.value.default::class.java.simpleName}\" not found for name \"$string\"")
    }

    override fun getSuggestions(ctx: CommandContext<CommandSourceStack>, suggestionsBuilder: SuggestionsBuilder): CompletableFuture<Suggestions> {
        return SharedSuggestionProvider.suggest(
            this.value.default::class.java.enumConstants
                .map { value -> value.getSerializedName() }
                .toMutableList(),
            suggestionsBuilder
        )
    }

}
