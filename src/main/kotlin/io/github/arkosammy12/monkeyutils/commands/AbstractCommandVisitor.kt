package io.github.arkosammy12.monkeyutils.commands

import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.CommandNode
import com.mojang.brigadier.tree.LiteralCommandNode
import io.github.arkosammy12.monkeyconfig.base.ConfigManager
import me.lucko.fabric.api.permissions.v0.Permissions
import net.minecraft.command.CommandRegistryAccess
import net.minecraft.command.DefaultPermissions
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import net.minecraft.util.Formatting

abstract class AbstractCommandVisitor(
    protected val configManager: ConfigManager,
    protected val rootNodeName: String = configManager.fileName,
    protected val commandDispatcher: CommandDispatcher<ServerCommandSource>,
    protected val commandRegistryAccess: CommandRegistryAccess? = null,
    protected val registrationEnvironment: CommandManager.RegistrationEnvironment? = null
) : CommandVisitor {

    final override val configNode: LiteralCommandNode<ServerCommandSource> = CommandManager
        .literal("config")
        .requires(Permissions.require("$rootNodeName.config", 4))
        .build()

    override val onConfigReloadedCallback: (CommandContext<ServerCommandSource>, ConfigManager) -> Int
        get() = get@{ ctx, configManager ->
            if (configManager.loadFromFile()) {
                ctx.source.sendMessage(Text.literal("Config \"${this.rootNodeName}\" reloaded successfully!").formatted(Formatting.GREEN))
            } else {
                ctx.source.sendMessage(Text.literal("Found no existing configuration file \"${this.rootNodeName} to reload from!").formatted(Formatting.RED))
            }
            return@get Command.SINGLE_SUCCESS
        }

    init {

        val reloadNode: LiteralCommandNode<ServerCommandSource> = CommandManager
            .literal("reload")
            .requires(Permissions.require("$rootNodeName.config.reload", 4))
            .executes { ctx -> onConfigReloadedCallback(ctx, configManager) }
            .build()

        // Check if the root node already exists. If it does, use it. Otherwise, create it and add it as a child to the dispatcher root
        val rootNode: CommandNode<ServerCommandSource> = commandDispatcher.root.getChild(rootNodeName) ?: run{
            val node: LiteralCommandNode<ServerCommandSource> = CommandManager
                .literal(rootNodeName)
                .requires { source -> source.permissions.hasPermission(DefaultPermissions.ADMINS) }
                .build()
            commandDispatcher.root.addChild(node)
            node
        }

        rootNode.addChild(configNode)
        configNode.addChild(reloadNode)

    }

}