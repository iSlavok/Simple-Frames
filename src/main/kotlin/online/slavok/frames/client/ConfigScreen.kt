package online.slavok.frames.client

// Client-only: builds the YACL config screen bound to SimpleFramesMod.CONFIG. YACL
// has no 1.18.2 build, so Stonecutter comments the whole file out there.
//? if >=1.19 {
import dev.isxander.yacl3.api.ConfigCategory
import dev.isxander.yacl3.api.Option
import dev.isxander.yacl3.api.OptionGroup
import dev.isxander.yacl3.api.YetAnotherConfigLib
import dev.isxander.yacl3.api.controller.EnumControllerBuilder
import dev.isxander.yacl3.api.controller.StringControllerBuilder
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder
//? if >=1.22 {
/*import net.minecraft.client.gui.screens.Screen*/
//?} else {
import net.minecraft.client.gui.screen.Screen
//?}
import online.slavok.frames.ClickMode
import online.slavok.frames.SimpleFramesMod.CONFIG
import online.slavok.frames.literalText

/** Builds the Simple Frames config screen. Values bind straight to [CONFIG]; on save
 *  the config is written to disk. Mixins read [CONFIG] live, so edits apply at once. */
object ConfigScreen {

    fun build(parent: Screen?): Screen =
        YetAnotherConfigLib.createBuilder()
            .title(literalText("Simple Frames"))
            .category(
                ConfigCategory.createBuilder()
                    .name(literalText("Interactions"))
                    .group(
                        OptionGroup.createBuilder()
                            .name(literalText("Trigger button"))
                            .option(clickOption("Shears", ClickMode.LEFT, { CONFIG.shearsButton }, { CONFIG.shearsButton = it }))
                            .option(clickOption("Leather", ClickMode.LEFT, { CONFIG.leatherButton }, { CONFIG.leatherButton = it }))
                            .option(clickOption("Honeycomb", ClickMode.BOTH, { CONFIG.honeycombButton }, { CONFIG.honeycombButton = it }))
                            .option(clickOption("Axe", ClickMode.BOTH, { CONFIG.axeButton }, { CONFIG.axeButton = it }))
                            .build()
                    )
                    .build()
            )
            .category(
                ConfigCategory.createBuilder()
                    .name(literalText("Wax"))
                    .option(boolOption("Enable wax", true, { CONFIG.enableWax }, { CONFIG.enableWax = it }))
                    .option(boolOption("Waxed frame fully invulnerable", false, { CONFIG.waxFullLock }, { CONFIG.waxFullLock = it }))
                    .option(boolOption("Axe loses durability un-waxing", true, { CONFIG.doAxeBreak }, { CONFIG.doAxeBreak = it }))
                    .build()
            )
            .category(
                ConfigCategory.createBuilder()
                    .name(literalText("Consumption"))
                    .option(boolOption("Shears take damage / break", true, { CONFIG.doShearsBreak }, { CONFIG.doShearsBreak = it }))
                    .option(boolOption("Restore invisible frames with leather", true, { CONFIG.fixWithLeather }, { CONFIG.fixWithLeather = it }))
                    .option(boolOption("Consume leather", true, { CONFIG.doLeatherConsume }, { CONFIG.doLeatherConsume = it }))
                    .option(boolOption("Consume honeycomb", true, { CONFIG.doHoneycombConsume }, { CONFIG.doHoneycombConsume = it }))
                    .build()
            )
            .category(
                ConfigCategory.createBuilder()
                    .name(literalText("Item names"))
                    .option(stringOption("Invisible frame name", "Invisible Item Frame", { CONFIG.invisibleFrameName }, { CONFIG.invisibleFrameName = it }))
                    .option(stringOption("Invisible glow frame name", "Invisible Glow Item Frame", { CONFIG.invisibleGlowFrameName }, { CONFIG.invisibleGlowFrameName = it }))
                    .build()
            )
            .save { CONFIG.dump() }
            .build()
            .generateScreen(parent)

    private fun boolOption(name: String, def: Boolean, get: () -> Boolean, set: (Boolean) -> Unit): Option<Boolean> =
        Option.createBuilder<Boolean>()
            .name(literalText(name))
            .binding(def, { get() }, { set(it) })
            .controller { TickBoxControllerBuilder.create(it) }
            .build()

    private fun stringOption(name: String, def: String, get: () -> String, set: (String) -> Unit): Option<String> =
        Option.createBuilder<String>()
            .name(literalText(name))
            .binding(def, { get() }, { set(it) })
            .controller { StringControllerBuilder.create(it) }
            .build()

    private fun clickOption(name: String, def: ClickMode, get: () -> ClickMode, set: (ClickMode) -> Unit): Option<ClickMode> =
        Option.createBuilder<ClickMode>()
            .name(literalText(name))
            .binding(def, { get() }, { set(it) })
            // YACL renamed the enum value formatter: valueFormatter (3.1.x, on 1.19.4)
            // -> formatValue (3.6+, everything newer).
            .controller {
                //? if >=1.20 {
                EnumControllerBuilder.create(it).enumClass(ClickMode::class.java).formatValue { v -> literalText(v.name) }
                //?} else {
                /*EnumControllerBuilder.create(it).enumClass(ClickMode::class.java).valueFormatter { v -> literalText(v.name) }*/
                //?}
            }
            .build()
}
//?}
