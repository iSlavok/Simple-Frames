package online.slavok.frames

import net.fabricmc.api.ModInitializer
//? if >=1.19 {
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
//?} else {
/*import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback*/
//?}
import online.slavok.frames.commands.FrameCommand
import org.slf4j.LoggerFactory

object SimpleFramesMod : ModInitializer {
    @JvmField
    val LOGGER = LoggerFactory.getLogger("simpleframes")

    @JvmField
    val CONFIG = Config()

    override fun onInitialize() {
        LOGGER.info("Simple Frames loaded successfully!")
        CONFIG.load()
        // Command API v2 (1.19+) passes (dispatcher, registryAccess, environment);
        // v1 (1.18 and earlier) passes (dispatcher, dedicated).
        //? if >=1.19 {
        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            FrameCommand().register(dispatcher)
        }
        //?} else {
        /*CommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            FrameCommand().register(dispatcher)
        }*/
        //?}
    }
}
