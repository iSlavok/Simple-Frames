package online.slavok.frames.plugin

import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.util.StringUtil

// Permission (simpleframes.command) is enforced by plugin.yml.
class FramesCommand(private val plugin: SimpleFramesPlugin) : CommandExecutor, TabCompleter {

    companion object {
        val OPTIONS = listOf("doShearsBreak", "doLeatherFix", "enableWax", "waxFullLock", "doAxeBreak")
        private val BOOLEAN_OPTIONS = listOf("true", "false")
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.isEmpty()) {
            sender.sendMessage("${ChatColor.GOLD}[SimpleFrames] ${ChatColor.YELLOW}Usage: /simpleframes <${OPTIONS.joinToString("|")}> [true|false]")
            return true
        }
        when (args[0].lowercase()) {
            "doshearsbreak" -> toggle(sender, args, "doShearsBreak", plugin.doShearsBreak) { plugin.setDoShearsBreak(it) }
            "doleatherfix" -> toggle(sender, args, "doLeatherFix", plugin.fixWithLeather) { plugin.setFixWithLeather(it) }
            "enablewax" -> toggle(sender, args, "enableWax", plugin.enableWax) { plugin.setEnableWax(it) }
            "waxfulllock" -> toggle(sender, args, "waxFullLock", plugin.waxFullLock) { plugin.setWaxFullLock(it) }
            "doaxebreak" -> toggle(sender, args, "doAxeBreak", plugin.doAxeBreak) { plugin.setDoAxeBreak(it) }
            else -> sender.sendMessage("${ChatColor.RED}Unknown option '${args[0]}'. Use ${OPTIONS.dropLast(1).joinToString(", ")} or ${OPTIONS.last()}.")
        }
        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>,
    ): MutableList<String> {
        val result = mutableListOf<String>()
        return when (args.size) {
            1 -> StringUtil.copyPartialMatches(args[0], OPTIONS, result).apply { sort() }
            2 -> StringUtil.copyPartialMatches(args[1], BOOLEAN_OPTIONS, result).apply { sort() }
            else -> result
        }
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
