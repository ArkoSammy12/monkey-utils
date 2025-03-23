package io.github.arkosammy12.monkeyutils.registrars

import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.tree.LiteralCommandNode
import io.github.arkosammy12.monkeyconfig.base.ConfigElement
import io.github.arkosammy12.monkeyconfig.base.ConfigElementContainer
import io.github.arkosammy12.monkeyconfig.base.ConfigManager
import io.github.arkosammy12.monkeyconfig.base.Section
import io.github.arkosammy12.monkeyutils.commands.CommandVisitor
import io.github.arkosammy12.monkeyutils.commands.DefaultCommandVisitor
import io.github.arkosammy12.monkeyutils.settings.CommandControllable
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text

object DefaultConfigRegistrar : ConfigManagerRegistrar {

    private val configManagers: MutableCollection<ConfigManager> = mutableListOf()

    override fun registerConfigManager(configManager: ConfigManager) {
        this.configManagers.add(configManager)
        CommandRegistrationCallback.EVENT.register { commandDispatcher, _, _ ->
            val commandVisitor: CommandVisitor = DefaultCommandVisitor(configManager, commandDispatcher = commandDispatcher)
            registerCommandLayer(configManager = configManager, commandDispatcher = commandDispatcher, visitor = commandVisitor)
        }
    }

    private fun registerCommandLayer(currentContainer: ConfigElementContainer? = null, parentNode: LiteralCommandNode<ServerCommandSource>? = null, configManager: ConfigManager, visitor: CommandVisitor, commandDispatcher: CommandDispatcher<ServerCommandSource>) {
        val layerElements: Collection<ConfigElement> =  currentContainer?.configElements ?: configManager.configElements
        val currentParentNode: LiteralCommandNode<ServerCommandSource>? = currentContainer?.run {
            if (this !is Section) {
                null
            } else {
                CommandManager
                    .literal(this.name)
                    .requires { source -> source.hasPermissionLevel(4) }.also { argumentBuilder ->
                        val sectionComment: String? = this.comment
                        if (sectionComment != null) {
                            argumentBuilder.executes { ctx ->
                                ctx.source.sendMessage(Text.literal("Description: $sectionComment"))
                                Command.SINGLE_SUCCESS
                            }
                        }
                    }
                    .build()
            }
        }
        if (parentNode == null && currentParentNode != null) {
            visitor.configNode.addChild(currentParentNode)
        } else if (parentNode != null && currentParentNode != null) {
            parentNode.addChild(currentParentNode)
        }
        for (element: ConfigElement in layerElements) {
            if (element !is CommandControllable<*, *>) {
                continue
            }

            element.accept(currentParentNode, visitor)
        }
        for (element: ConfigElement in layerElements) {
            if (element !is ConfigElementContainer) {
                continue
            }
            registerCommandLayer(element, currentParentNode, configManager, visitor, commandDispatcher)
        }
    }

    override fun reloadConfigManagers(onReloadedCallback: ConfigManager.() -> Unit) {
        for (configManager : ConfigManager in this.configManagers) {
            configManager.loadFromFile()
            onReloadedCallback(configManager)
        }
    }

    override fun saveConfigManagers(onSavedCallback: ConfigManager.() -> Unit) {
        for (configManager : ConfigManager in this.configManagers) {
            configManager.saveToFile()
            onSavedCallback(configManager)
        }
    }

    override fun forEachConfigManager(action: ConfigManager.() -> Unit) {
        for (configManager : ConfigManager in this.configManagers) {
            action(configManager)
        }
    }

}