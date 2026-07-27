package online.slavok.frames.plugin

import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

// Permission (simpleframes.command) is enforced by plugin.yml.
class FramesCommand(private val plugin: SimpleFramesPlugin) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.isEmpty()) {
            sender.sendMessage("${ChatColor.GOLD}[SimpleFrames] ${ChatColor.YELLOW}Usage: /simpleframes <doShearsBreak|doLeatherFix> [true|false]")
            return true
        }
        when (args[0].lowercase()) {
            "doshearsbreak" -> toggle(sender, args, "doShearsBreak", plugin.doShearsBreak) { plugin.setDoShearsBreak(it) }
            "doleatherfix" -> toggle(sender, args, "doLeatherFix", plugin.fixWithLeather) { plugin.setFixWithLeather(it) }
            else -> sender.sendMessage("${ChatColor.RED}Unknown option '${args[0]}'. Use doShearsBreak or doLeatherFix.")
        }
        return true
    }

    private fun toggle(sender: CommandSender, args: Array<out String>, name: String, current: Boolean, setter: (Boolean) -> Unit) {
        val value = if (args.size >= 2) {
            val parsed = args[1].toBooleanStrictOrNull()
            if (parsed == null) {
                sender.sendMessage("${ChatColor.RED}Value must be true or false.")
                return
            }
            setter(parsed)
            parsed
        } else {
            current
        }
        show(sender, name, value)
    }

    private fun show(sender: CommandSender, name: String, value: Boolean) {
        val color = if (value) ChatColor.GREEN else ChatColor.RED
        sender.sendMessage("${ChatColor.GOLD}[SimpleFrames] ${ChatColor.AQUA}$name ${ChatColor.GRAY}= $color$value")
    }
}
