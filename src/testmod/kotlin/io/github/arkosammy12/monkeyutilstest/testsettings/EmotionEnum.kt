package io.github.arkosammy12.monkeyutilstest.testsettings

import net.minecraft.util.StringIdentifiable

enum class EmotionEnum(val string: String) : StringIdentifiable {
    HAPPY("HAPPY"),
    SAD("SAD"),
    ANGRY("ANGRY"),
    WORRIED("WORRIED");

    override fun asString(): String? = this.string

}