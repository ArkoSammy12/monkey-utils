package io.github.arkosammy12.monkeyutils.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.tree.ArgumentCommandNode
import com.mojang.brigadier.tree.LiteralCommandNode
import io.github.arkosammy12.monkeyconfig.base.ConfigManager
import io.github.arkosammy12.monkeyutils.settings.CommandControllable
import me.lucko.fabric.api.permissions.v0.Permissions
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.Commands
import net.minecraft.commands.CommandSourceStack

class DefaultCommandVisitor(
    configManager: ConfigManager,
    rootNodeName: String = configManager.fileName,
    commandDispatcher: CommandDispatcher<CommandSourceStack>,
    commandRegistryAccess: CommandBuildContext? = null,
    registrationEnvironment: Commands.CommandSelection? = null
) : AbstractCommandVisitor(configManager, rootNodeName, commandDispatcher, commandRegistryAccess, registrationEnvironment) {

    override fun <V : Any, A : ArgumentType<*>> visit(parentNode: LiteralCommandNode<CommandSourceStack>?, commandControllableSetting: CommandControllable<V, A>) {
        val currentParentNode: LiteralCommandNode<CommandSourceStack> = parentNode ?: this.configNode
        val settingName: String = commandControllableSetting.commandPath.asList.last()
        val settingNode: LiteralCommandNode<CommandSourceStack> = Commands
            .literal(settingName)
            .requires(Permissions.require("$rootNodeName.config.${commandControllableSetting.commandPath.string}", 4))
            .executes { ctx -> commandControllableSetting.onValueGetCallback(ctx, this.configManager) }
            .build()

        val setterNode: ArgumentCommandNode<CommandSourceStack, out Any> = Commands
            .argument(settingName, commandControllableSetting.argumentType)
            .requires(Permissions.require("$rootNodeName.config.${commandControllableSetting.commandPath.string}", 4))
            .suggests { ctx, suggestionBuilder -> commandControllableSetting.getSuggestions(ctx, suggestionBuilder) }
            .executes { ctx -> commandControllableSetting.onValueSetCallback(ctx, this.configManager) }
            .build()

        currentParentNode.addChild(settingNode)
        settingNode.addChild(setterNode)
    }

}