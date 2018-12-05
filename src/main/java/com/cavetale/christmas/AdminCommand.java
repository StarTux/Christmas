package com.cavetale.christmas;

import com.cavetale.dirty.Dirty;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
        case "adv": {
            try {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                File dir = this.plugin.getServer().getWorlds().get(0).getWorldFolder();
                dir = new File(dir, "datapacks");
                dir = new File(dir, "christmas");
                dir.mkdirs();
                File file = new File(dir, "pack.mcmeta");
                try (FileWriter fw = new FileWriter(file)) {
                    gson.toJson(new PackJson(), fw);
                }
                dir = new File(dir, "data");
                dir = new File(dir, "christmas");
                dir = new File(dir, "advancements");
                dir.mkdirs();
                AdvancementJson root = new AdvancementJson();
                root.display.title = "Christmas";
                root.display.description = "Collect Daily Christmas Presents around Spawn";
                root.display.background = "minecraft:textures/block/snow.png";
                root.display.announce_to_chat = false;
                root.criteria.impossible.trigger = "minecraft:location";
                file = new File(dir, "root.json");
                try (FileWriter fw = new FileWriter(file)) {
                    gson.toJson(root, fw);
                }
                file = new File(dir, "root.json");
                for (XmasDoor door: this.plugin.doorsJson.doors) {
                    int index = door.getIndex();
                    AdvancementJson adv = new AdvancementJson();
                    ItemStack item;
                    if (!door.getItems().isEmpty()) {
                        item = Dirty.deserializeItem(door.getItems().get(0));
                    } else {
                        item = new ItemStack(Material.GOLDEN_APPLE, index);
                    }
                    if (item.getType() == Material.PLAYER_HEAD) item = new ItemStack(Material.GOLDEN_APPLE, index);
                    adv.display.icon.item = "minecraft:" + item.getType().name().toLowerCase();
                    Gson gson2 = new GsonBuilder().create();
                    adv.display.icon.nbt = gson2.toJson(Dirty.serializeItem(item));
                    adv.display.title = "Present " + door.getIndex();
                    adv.display.description = "Open Present " + door.getIndex() + ".";
                    if (index == 1) {
                        adv.parent = "christmas:root";
                    } else {
                        adv.parent = "christmas:present" + (index - 1);
                    }
                    file = new File(dir, "present" + index + ".json");
                    try (FileWriter fw = new FileWriter(file)) {
                        gson.toJson(adv, fw);
                    }
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
            this.plugin.getServer().reloadData();
            sender.sendMessage("Advancements generated");
            return true;
        }
        default:
            return false;
        }
    }

    static final class AdvancementJson {
        static final class Trigger {
            String trigger = "minecraft:impossible";
        }
        static final class Criteria {
            Trigger impossible = new Trigger();
        }
        static final class Icon {
            String item = "minecraft:golden_apple";
            String nbt = null;
        }
        static final class Display {
            String title = null;
            String description = null;
            boolean show_toast = true;
            boolean hidden = false;
            String background = null;
            Icon icon = new Icon();
            boolean announce_to_chat = true;
        }
        Criteria criteria = new Criteria();
        Display display = new Display();
        String parent = null;
    }

    static final class PackJson {
        static final class Pack {
            String description = "Christmas Event Advancements";
            int pack_format = 1;
        }
        Pack pack = new Pack();
    }
}
