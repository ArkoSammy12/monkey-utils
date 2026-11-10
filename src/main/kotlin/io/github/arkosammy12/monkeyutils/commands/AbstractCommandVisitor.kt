package io.github.arkosammy12.monkeyutils.commands

import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.CommandNode
import com.mojang.brigadier.tree.LiteralCommandNode
import io.github.arkosammy12.monkeyconfig.base.ConfigManager
import me.lucko.fabric.api.permissions.v0.Permissions
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.Commands
import net.minecraft.commands.CommandSourceStack
import net.minecraft.network.chat.Component
import net.minecraft.server.permissions.Permissions as VanillaPermissions
import net.minecraft.ChatFormatting
import net.minecraft.server.permissions.PermissionLevel

abstract class AbstractCommandVisitor(
    protected val configManager: ConfigManager,
    protected val rootNodeName: String = configManager.fileName,
    protected val commandDispatcher: CommandDispatcher<CommandSourceStack>,
    protected val commandRegistryAccess: CommandBuildContext? = null,
    protected val registrationEnvironment: Commands.CommandSelection? = null
) : CommandVisitor {

    final override val configNode: LiteralCommandNode<CommandSourceStack> = Commands
        .literal("config")
        .requires(Permissions.require("$rootNodeName.config", PermissionLevel.OWNERS))
        .build()

    override val onConfigReloadedCallback: (CommandContext<CommandSourceStack>, ConfigManager) -> Int
        get() = get@{ ctx, configManager ->
            if (configManager.loadFromFile()) {
                ctx.source.sendSystemMessage(Component.literal("Config \"${this.rootNodeName}\" reloaded successfully!").withStyle(ChatFormatting.GREEN))
            } else {
                ctx.source.sendSystemMessage(Component.literal("Found no existing configuration file \"${this.rootNodeName} to reload from!").withStyle(ChatFormatting.RED))
            }
            return@get Command.SINGLE_SUCCESS
        }

    init {

        val reloadNode: LiteralCommandNode<CommandSourceStack> = Commands
            .literal("reload")
            .requires(Permissions.require("$rootNodeName.config.reload", PermissionLevel.OWNERS))
            .executes { ctx -> onConfigReloadedCallback(ctx, configManager) }
            .build()

        // Check if the root node already exists. If it does, use it. Otherwise, create it and add it as a child to the dispatcher root
        val rootNode: CommandNode<CommandSourceStack> = commandDispatcher.root.getChild(rootNodeName) ?: run{
            val node: LiteralCommandNode<CommandSourceStack> = Commands
                .literal(rootNodeName)
                .requires { source -> source.permissions().hasPermission(VanillaPermissions.COMMANDS_ADMIN) }
                .build()
            commandDispatcher.root.addChild(node)
            node
        }

        rootNode.addChild(configNode)
        configNode.addChild(reloadNode)

    }

}
