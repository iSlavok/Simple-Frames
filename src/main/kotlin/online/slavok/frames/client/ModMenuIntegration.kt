package online.slavok.frames.client

// Whole file is client-only and pulls in ModMenu/YACL, which have no 1.18.2 build.
// Stonecutter comments the body out there, leaving an empty (valid) Kotlin file.
//? if >=1.19 {
import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi

/**
 * ModMenu entrypoint: hands ModMenu the factory that builds our YACL screen.
 * Only ever classloaded on a client running ModMenu, so a dedicated server never
 * touches it (and never needs YACL).
 */
class ModMenuIntegration : ModMenuApi {
    override fun getModConfigScreenFactory(): ConfigScreenFactory<*> =
        ConfigScreenFactory { parent -> ConfigScreen.build(parent) }
}
//?}
