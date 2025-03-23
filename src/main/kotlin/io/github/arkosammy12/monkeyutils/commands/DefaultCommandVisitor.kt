package io.github.arkosammy12.monkeyutils.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.tree.ArgumentCommandNode
import com.mojang.brigadier.tree.LiteralCommandNode
import io.github.arkosammy12.monkeyconfig.base.ConfigManager
import io.github.arkosammy12.monkeyutils.settings.CommandControllable
import net.minecraft.command.CommandRegistryAccess
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource

class DefaultCommandVisitor(
    configManager: ConfigManager,
    rootNodeName: String = configManager.fileName,
    commandDispatcher: CommandDispatcher<ServerCommandSource>,
    commandRegistryAccess: CommandRegistryAccess? = null,
    registrationEnvironment: CommandManager.RegistrationEnvironment? = null
) : AbstractCommandVisitor(configManager, rootNodeName, commandDispatcher, commandRegistryAccess, registrationEnvironment) {

    override fun <V : Any, A : ArgumentType<*>> visit(parentNode: LiteralCommandNode<ServerCommandSource>?, commandControllableSetting: CommandControllable<V, A>) {
        val currentParentNode: LiteralCommandNode<ServerCommandSource> = parentNode ?: this.configNode
        val settingName: String = commandControllableSetting.commandPath.asList.last()
        val settingNode: LiteralCommandNode<ServerCommandSource> = CommandManager
            .literal(settingName)
            .requires { source -> source.hasPermissionLevel(4) }
            .executes { ctx -> commandControllableSetting.onValueGetCallback(ctx, this.configManager) }
            .build()

        val setterNode: ArgumentCommandNode<ServerCommandSource, out Any> = CommandManager
            .argument(settingName, commandControllableSetting.argumentType)
            .requires { source -> source.hasPermissionLevel(4) }
            .suggests { ctx, suggestionBuilder -> commandControllableSetting.getSuggestions(ctx, suggestionBuilder) }
            .executes { ctx -> commandControllableSetting.onValueSetCallback(ctx, this.configManager) }
            .build()

        currentParentNode.addChild(settingNode)
        settingNode.addChild(setterNode)
    }

}