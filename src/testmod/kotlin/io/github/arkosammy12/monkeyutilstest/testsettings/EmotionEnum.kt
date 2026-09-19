package io.github.arkosammy12.monkeyutilstest.testsettings

import net.minecraft.util.StringRepresentable

enum class EmotionEnum(val string: String) : StringRepresentable {
    HAPPY("HAPPY"),
    SAD("SAD"),
    ANGRY("ANGRY"),
    WORRIED("WORRIED");

    override fun getSerializedName(): String? = this.string

}