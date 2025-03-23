package io.github.arkosammy12.monkeyutilstest

import io.github.arkosammy12.monkeyconfig.builders.tomlConfigManager
import io.github.arkosammy12.monkeyutils.registrars.DefaultConfigRegistrar
import io.github.arkosammy12.monkeyutils.settings.CommandBooleanSetting
import io.github.arkosammy12.monkeyutils.settings.CommandNumberSetting
import io.github.arkosammy12.monkeyutils.settings.CommandStringSetting
import net.fabricmc.api.ModInitializer
import net.fabricmc.loader.api.FabricLoader
import org.slf4j.LoggerFactory

object KMonkeyUtilsTest : ModInitializer {

    const val MOD_ID: String = "monkey-utils-test"
    private val LOGGER = LoggerFactory.getLogger(MOD_ID)

    val configManager = tomlConfigManager(MOD_ID, FabricLoader.getInstance().configDir.resolve("$MOD_ID.toml")) {
        logger = LOGGER
        booleanSetting("monkeyStyle", true) {
            comment = "This is true by default!"
            implementation = ::CommandBooleanSetting
        }

        section("preferences") {
            comment = "These settings are for preferences."
            numberSetting<Int>("breakDaysLimit", 0) {
                comment = "How many days I can take a break before getting back in the grind. Default is 0. Minimum is 0 and maximum is 10."
                minValue = 0
                maxValue = 10
                implementation = ::CommandNumberSetting
            }
            section("whitelist") {
                stringSetting("prompt", "You need to be whitelisted!") {
                    comment = "The prompt shown to users who are not whitelisted."
                    implementation = ::CommandStringSetting
                }
                booleanSetting("enabled", true) {
                    comment = "Default is true"
                    implementation = ::CommandBooleanSetting
                }
                stringListSetting("list", listOf("ArkoSammy12")) {}
            }
        }
    }

    override fun onInitialize() {

        DefaultConfigRegistrar.registerConfigManager(configManager)
        DefaultConfigRegistrar.saveConfigManagers()

    }

}


