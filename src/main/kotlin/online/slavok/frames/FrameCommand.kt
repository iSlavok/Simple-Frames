package online.slavok.frames

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.BoolArgumentType
import net.minecraft.command.CommandRegistryAccess
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text

object FrameCommand {
    fun register(
        dispatcher: CommandDispatcher<ServerCommandSource>,
        commandRegistryAccess: CommandRegistryAccess,
        registrationEnvironment: CommandManager.RegistrationEnvironment
    ) {
        dispatcher.register(
            CommandManager.literal("simpleframes")
                .then(
                    CommandManager.literal("doShearsBreak").requires { source -> source.hasPermissionLevel(3) }
                        .executes { context -> executeShow(context.source, "doShearsBreak", SimpleFramesMod.CONFIG.doShearsBreak) }
                        .then(
                            CommandManager.argument("value", BoolArgumentType.bool()).executes { context ->
                                executeDoShearsBreak(context.source, BoolArgumentType.getBool(context, "value"))
                            }
                        )
                )
                .then(
                    CommandManager.literal("doLeatherFix").requires { source -> source.hasPermissionLevel(3) }
                        .executes { context -> executeShow(context.source, "doLeatherFix", SimpleFramesMod.CONFIG.fixWithLeather) }
                        .then(
                            CommandManager.argument("value", BoolArgumentType.bool()).executes { context ->
                                executeDoLeatherFix(context.source, BoolArgumentType.getBool(context, "value"))
                            }
                        )
                )
        )
    }

    private fun executeShow(commandSource: ServerCommandSource, name: String, value: Any): Int {
        val strValue: String = if (value is Boolean) {
            if (value) "§6$value" else "§c$value"
        } else {
            value.toString()
        }
        commandSource.player!!.sendMessage(Text.of("§6[§aSimpleFrames§6] §2value of §b$name§2 > $strValue"))
        return 1
    }

    private fun executeDoShearsBreak(commandSource: ServerCommandSource, value: Boolean): Int {
        SimpleFramesMod.CONFIG.doShearsBreak = value
        executeShow(commandSource, "doShearsBreak", SimpleFramesMod.CONFIG.doShearsBreak)
        SimpleFramesMod.CONFIG.dump()
        return 1
    }

    private fun executeDoLeatherFix(commandSource: ServerCommandSource, value: Boolean): Int {
        SimpleFramesMod.CONFIG.fixWithLeather = value
        executeShow(commandSource, "doLeatherFix", SimpleFramesMod.CONFIG.fixWithLeather)
        SimpleFramesMod.CONFIG.dump()
        return 1
    }
}
