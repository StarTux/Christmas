package com.cavetale.christmas;

import com.cavetale.christmas.json.AdvancementJson;
import com.cavetale.christmas.json.Item;
import com.cavetale.christmas.json.PackJson;
import com.cavetale.christmas.json.Present;
import com.cavetale.core.command.CommandNode;
import com.cavetale.core.command.CommandWarn;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@RequiredArgsConstructor
final class AdminCommand implements TabExecutor {
    private final ChristmasPlugin plugin;
    private CommandNode rootNode;

    public AdminCommand enable() {
        rootNode = new CommandNode("xmasadm");
        rootNode.addChild("setlocation").arguments("<index>")
            .description("Set present location")
            .playerCaller(this::setlocation);
        rootNode.addChild("items").arguments("<index>")
            .description("Set present reward items")
            .playerCaller(this::items);
        rootNode.addChild("hint").arguments("<index>")
            .description("Set present hint")
            .playerCaller(this::hint);
        rootNode.addChild("presentitems").denyTabCompletion()
            .description("Set present (floating skull) items")
            .playerCaller(this::presentitems);
        rootNode.addChild("open").arguments("<index>")
            .description("Open a present (debug)")
            .playerCaller(this::open);
        rootNode.addChild("tp").arguments("<index>")
            .description("Teleport to present")
            .playerCaller(this::tp);
        rootNode.addChild("reload").denyTabCompletion()
            .description("Reload configuration files")
            .senderCaller(this::reload);
        rootNode.addChild("adv").denyTabCompletion()
            .description("Write advancement files")
            .senderCaller(this::adv);
        rootNode.addChild("debug").denyTabCompletion()
            .description("Toggle debug mode")
            .senderCaller(this::debug);
        plugin.getCommand("xmasadm").setExecutor(this);
        return this;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
        return rootNode.call(sender, command, alias, args);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return rootNode.complete(sender, command, alias, args);
    }

    public static int parseIndex(String arg) {
        int index;
        try {
            index = Integer.parseInt(arg);
        } catch (NumberFormatException nfe) {
            throw new CommandWarn("Invalid index: " + arg);
        }
        if (index < 1 || index > 25) {
            throw new CommandWarn("Invalid index: " + index);
        }
        return index;
    }

    boolean setlocation(Player player, String[] args) {
        if (args.length != 1) return false;
        int index = parseIndex(args[0]);
        plugin.getPresentsJson().getPresents().get(index - 1).setLocation(player.getLocation());
        plugin.getPresentRuntimes().get(index - 1).clearArmorStand();
        plugin.getPresentsJson().setDirty(true);
        player.sendMessage("Set present #" + index + " to current location.");
        return true;
    }

    boolean items(Player player, String[] args) {
        if (args.length != 1) return false;
        int index = parseIndex(args[0]);
        Gui gui = new Gui(plugin);
        gui.setEditable(true);
        gui.size(6 * 9);
        gui.title("Present #" + index);
        gui.onClose(event -> {
                List<Item> items = new ArrayList<>();
                for (ItemStack itemStack : gui.getInventory()) {
                    if (itemStack == null) continue;
                    items.add(new Item(itemStack));
                }
                plugin.getPresentsJson().getPresent(index).setItems(items);
                plugin.getPresentsJson().setDirty(true);
                player.sendMessage(ChatColor.BLUE + "Present #" + index + " now has " + items.size() + " items.");
            });
        player.sendMessage(ChatColor.YELLOW + "Put the items in the chest");
        gui.open(player);
        return true;
    }

    boolean presentitems(Player player, String[] args) {
        if (args.length != 0) return false;
        Gui gui = new Gui(plugin);
        gui.setEditable(true);
        gui.size(6 * 9);
        gui.title("Present Items");
        gui.onClose(event -> {
                List<Item> items = new ArrayList<>();
                for (ItemStack itemStack : gui.getInventory()) {
                    if (itemStack == null) continue;
                    items.add(new Item(itemStack));
                }
                plugin.getPresentsJson().setPresentItems(items);
                plugin.getPresentsJson().setDirty(true);
                plugin.resetPresents();
                player.sendMessage(ChatColor.BLUE + "Present Heads updated!");
            });
        player.sendMessage(ChatColor.YELLOW + "Put the items in the chest");
        gui.open(player);
        return true;
    }

    boolean open(Player player, String[] args) {
        if (args.length != 1) return false;
        int index = parseIndex(args[0]);
        plugin.givePresent(player, index);
        return true;
    }

    boolean tp(Player player, String[] args) {
        if (args.length != 1) return false;
        int index = parseIndex(args[0]);
        Location loc = plugin.getPresentsJson().getPresents().get(index - 1).toLocation();
        player.sendMessage("Location of present " + index + ": " + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ());
        player.teleport(loc);
        return true;
    }

    boolean reload(CommandSender sender, String[] args) {
        plugin.importPlayerFiles();
        plugin.importPresentsFile();
        sender.sendMessage("Players and presents reloaded.");
        return true;
    }

    boolean adv(CommandSender sender, String[] args) {
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            File dir = plugin.getServer().getWorlds().get(0).getWorldFolder();
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
            int i = 1;
            for (Present present: plugin.getPresentsJson().getPresents()) {
                int index = i++;
                AdvancementJson adv = new AdvancementJson();
                ItemStack item;
                if (!present.getItems().isEmpty()) {
                    item = present.getItems().get(0).toItemStack();
                } else {
                    item = new ItemStack(Material.GOLDEN_APPLE);
                }
                if (item.getType() == Material.PLAYER_HEAD) item = new ItemStack(Material.GOLDEN_APPLE, index);
                adv.display.icon.item = "minecraft:" + item.getType().name().toLowerCase();
                Gson gson2 = new GsonBuilder().create();
                adv.display.title = "Present " + index;
                adv.display.description = "Open Present " + index + ".";
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
        plugin.getServer().reloadData();
        sender.sendMessage("Advancements generated");
        return true;
    }

    boolean debug(CommandSender sender, String[] args) {
        if (args.length != 0) return false;
        boolean debug = !plugin.isDebug();
        plugin.setDebug(debug);
        sender.sendMessage("Debug = " + debug);
        if (!debug) plugin.resetPresents();
        return true;
    }

    boolean hint(CommandSender sender, String[] args) {
        if (args.length < 2) return false;
        int index = parseIndex(args[0]);
        String hint = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        plugin.getPresentsJson().getPresent(index - 1).setHint(hint);
        plugin.getPresentsJson().setDirty(true);
        sender.sendMessage("Hint #" + index + " updated: '" + hint + "'");
        return true;
    }
}
