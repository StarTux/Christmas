package com.cavetale.christmas;

import com.cavetale.dirty.Dirty;
import com.google.gson.Gson;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@RequiredArgsConstructor
final class AdminCommand implements CommandExecutor {
    private final ChristmasPlugin plugin;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 0) return false;
        return onCommand(sender, args[0], Arrays.copyOfRange(args, 1, args.length));
    }

    private boolean onCommand(CommandSender sender, String cmd, String[] args) {
        final Player player = sender instanceof Player ? (Player)sender : null;
        switch (cmd) {
        case "setdoor": {
            if (args.length != 1) return false;
            int index = Integer.parseInt(args[0]);
            this.plugin.doorsJson.doors.get(index - 1).setLocation(player.getLocation());
            this.plugin.doorsJson.doors.get(index - 1).setNpc(null);
            this.plugin.doorsJson.dirty = true;
            player.sendMessage("Set door #" + index + " to current location.");
            return true;
        }
        case "additem": {
            if (args.length != 1) return false;
            int index = Integer.parseInt(args[0]);
            ItemStack item = player.getInventory().getItemInMainHand();
            if (item == null || item.getType() == Material.AIR) throw new IllegalArgumentException("holding air");
            Gson gson = new Gson();
            String json = gson.toJson(Dirty.serializeItem(item));
            this.plugin.doorsJson.doors.get(index - 1).getItems().add(json);
            this.plugin.doorsJson.dirty = true;
            player.sendMessage("Item added to door #" + index + ": " + json);
            return true;
        }
        case "open": {
            if (args.length != 1) return false;
            int index = Integer.parseInt(args[0]);
            this.plugin.giveDoor(player, index);
            return true;
        }
        case "reload": {
            this.plugin.importPlayersFile();
            this.plugin.importDoorsFile();
            sender.sendMessage("Players and doors reloaded.");
            return true;
        }
        default:
            return false;
        }
    }
}
