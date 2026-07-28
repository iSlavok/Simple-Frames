package online.slavok.frames.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.BoolArgumentType
//? if >=1.22 {
/*import net.minecraft.commands.Commands as CommandManager
import net.minecraft.commands.Commands.literal
import net.minecraft.commands.Commands.argument
import net.minecraft.commands.CommandSourceStack as ServerCommandSource
import net.minecraft.network.chat.Component as Text
import net.minecraft.ChatFormatting as Formatting*/
//?} else {
import net.minecraft.server.command.CommandManager.argument
import net.minecraft.server.command.CommandManager.literal
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import net.minecraft.util.Formatting
//?}
import online.slavok.frames.SimpleFramesMod
import online.slavok.frames.colored
import online.slavok.frames.literalText
import online.slavok.frames.commands.api.Permission

// ServerCommandSource feedback signature changed across versions: pre-1.20 takes a
// Text directly, 1.20+ takes a Supplier<Text>, 26+ renamed it to sendSuccess.
private fun ServerCommandSource.feedback(text: Text, broadcastToOps: Boolean) {
    //? if >=1.22 {
    /*sendSuccess({ text }, broadcastToOps)*/
    //?} elif >=1.20 {
    sendFeedback({ text }, broadcastToOps)
    //?} else {
    /*sendFeedback(text, broadcastToOps)*/
    //?}
}

class FrameCommand {
    fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
        dispatcher.register(
            literal("simpleframes")
                .requires(Permission.require("simpleframes.command", 3))
                .then(
                    toggle(
                        "doShearsBreak",
                        { SimpleFramesMod.CONFIG.doShearsBreak },
                        { value -> SimpleFramesMod.CONFIG.doShearsBreak = value }
                    )
                )
                .then(
                    toggle(
                        "doLeatherFix",
                        { SimpleFramesMod.CONFIG.fixWithLeather },
                        { value -> SimpleFramesMod.CONFIG.fixWithLeather = value }
                    )
                )
                .then(
                    toggle(
                        "enableWax",
                        { SimpleFramesMod.CONFIG.enableWax },
                        { value -> SimpleFramesMod.CONFIG.enableWax = value }
                    )
                )
                .then(
                    toggle(
                        "waxFullLock",
                        { SimpleFramesMod.CONFIG.waxFullLock },
                        { value -> SimpleFramesMod.CONFIG.waxFullLock = value }
                    )
                )
                .then(
                    toggle(
                        "doAxeBreak",
                        { SimpleFramesMod.CONFIG.doAxeBreak },
                        { value -> SimpleFramesMod.CONFIG.doAxeBreak = value }
                    )
                )
        )
    }

    private fun toggle(name: String, getter: () -> Boolean, setter: (Boolean) -> Unit) =
        literal(name)
            .executes { context ->
                show(context.source, name, getter())
            }
            .then(
                argument("value", BoolArgumentType.bool())
                    .executes { context ->
                        setter(BoolArgumentType.getBool(context, "value"))
                        SimpleFramesMod.CONFIG.dump()
                        show(context.source, name, getter())
                    }
            )

    private fun show(source: ServerCommandSource, name: String, value: Boolean): Int {
        source.feedback(
            literalText("[SimpleFrames] ").colored(Formatting.GOLD)
                .append(literalText("$name = ").colored(Formatting.AQUA))
                .append(literalText(value.toString()).colored(if (value) Formatting.GREEN else Formatting.RED)),
            false
        )
        return 1
    }
}
