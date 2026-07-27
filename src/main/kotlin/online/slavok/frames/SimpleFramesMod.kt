package online.slavok.frames

import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.network.packet.Packet
import net.minecraft.server.network.ServerPlayerEntity
import org.slf4j.LoggerFactory

object SimpleFramesMod : ModInitializer {
    @JvmField
    val LOGGER = LoggerFactory.getLogger("simpleframes")

    @JvmField
    val CONFIG = Config()

    override fun onInitialize() {
        LOGGER.info("Simple Frames loaded successfully!")
        CONFIG.load()
        CommandRegistrationCallback.EVENT.register(FrameCommand::register)
    }

    @JvmStatic
    fun sendPackets(player: ServerPlayerEntity, packet: Packet<*>) {
        player.networkHandler.sendPacket(packet)
    }
}
