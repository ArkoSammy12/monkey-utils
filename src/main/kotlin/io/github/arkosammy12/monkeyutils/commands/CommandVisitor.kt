package io.github.arkosammy12.monkeyutils.commands

import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import io.github.arkosammy12.monkeyconfig.base.ConfigManager
import io.github.arkosammy12.monkeyutils.settings.CommandControllable
import net.minecraft.commands.CommandSourceStack

interface CommandVisitor {

    val configNode: LiteralCommandNode<CommandSourceStack>

    val onConfigReloadedCallback: (CommandContext<CommandSourceStack>, ConfigManager) -> Int

    fun <V : Any, A : ArgumentType<*>> visit(parentNode: LiteralCommandNode<CommandSourceStack>?, commandControllableSetting: CommandControllable<V, A>)

}