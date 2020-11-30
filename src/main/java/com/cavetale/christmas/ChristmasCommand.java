package com.cavetale.christmas;

import com.cavetale.christmas.json.PlayerProgress;
import lombok.RequiredArgsConstructor;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public final class ChristmasCommand implements CommandExecutor {
    private final ChristmasPlugin plugin;

    public ChristmasCommand enable() {
        plugin.getCommand("christmas").setExecutor(this);
        return this;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) return false;
        Player player = (Player) sender;
        PlayerProgress prog = plugin.getProgress(player);
        int today = Cal.today();
        if (today < 1 || today > 25) {
            player.sendMessage(ChatColor.RED + "Christmas is over.");
            return true;
        }
        player.sendMessage(ChatColor.GOLD + "Day of Christmas: " + today);
        player.sendMessage(ChatColor.GOLD + "Presents opened: " + prog.getPresentsOpened());
        return true;
    }
}
