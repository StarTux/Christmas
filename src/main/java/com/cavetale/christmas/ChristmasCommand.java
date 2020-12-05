package com.cavetale.christmas;

import com.cavetale.christmas.json.PlayerProgress;
import com.cavetale.christmas.json.Present;
import com.cavetale.christmas.util.Cal;
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
        if (today < 1) {
            player.sendMessage(ChatColor.RED + "Christmas hasn't started yet.");
            return true;
        }
        if (today > 25) {
            player.sendMessage(ChatColor.RED + "Christmas is over.");
            return true;
        }
        player.sendMessage(ChatColor.GRAY + "Day of Christmas: " + ChatColor.GOLD + today);
        player.sendMessage(ChatColor.GRAY + "Presents opened: " + ChatColor.GOLD + prog.getPresentsOpened());
        if (today > prog.getPresentsOpened() && prog.getPresentsOpened() < 25) {
            Present present = plugin.getPresentsJson().getPresent(prog.getPresentsOpened());
            player.sendMessage(ChatColor.GRAY + "Next: " + ChatColor.GOLD + "Present #" + today);
            player.sendMessage(ChatColor.GRAY + "Hint #" + today  + ": " + ChatColor.GOLD + present.getHint());
        }
        return true;
    }
}
