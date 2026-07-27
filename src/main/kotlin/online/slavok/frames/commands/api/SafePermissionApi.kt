//? if <1.22 {
package online.slavok.frames.commands.api

import me.lucko.fabric.api.permissions.v0.Permissions
import net.minecraft.server.command.ServerCommandSource

// Excluded on 26+: fabric-permissions-api is not available there and the >=1.22
// branch of Permission gates on the vanilla command permission instead.
object SafePermissionApi {
    fun check(source: ServerCommandSource, permission: String, defaultRequiredLevel: Int): Boolean {
        // Query the permission provider (LuckPerms, etc.); fall back to the vanilla op
        // level ourselves. Permissions.check(source, permission, int) does that fallback
        // internally but references CommandSource#hasPermissionLevel with an intermediary
        // that no longer exists on newer versions (NoSuchMethodError on player join) —
        // calling hasPermissionLevel from our own per-version-compiled code is remapped
        // correctly.
        return Permissions.getPermissionValue(source, permission)
            .orElseGet { source.hasPermissionLevel(defaultRequiredLevel) }
    }
}
//?}
