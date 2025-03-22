package io.github.arkosammy12.monkeyutils

import net.fabricmc.api.ModInitializer
import org.slf4j.LoggerFactory

object MonkeyUtils : ModInitializer {
    private val logger = LoggerFactory.getLogger("monkey-utils")

	override fun onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		logger.info("Hello Fabric world!")
	}
}