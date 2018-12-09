package com.cavetale.christmas;

import com.cavetale.dirty.Dirty;
import com.cavetale.mirage.PlayerUseEntityEvent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public final class ChristmasPlugin extends JavaPlugin implements Listener {
    // Files
    PlayersJson playersJson;
    DoorsJson doorsJson;
    // Commands
    private AdminCommand adminCommand = new AdminCommand(this);

    // --- Data

    static final class PlayersJson {
        transient boolean dirty;
        Map<UUID, PlayerProgress> players = new HashMap<>();
    }

    static final class DoorsJson {
        transient boolean dirty;
        List<XmasDoor> doors = new ArrayList<>();
        List<PlayerHead> playerHeads = new ArrayList<>();
    }

    // --- JavaPlugin

    @Override
    public void onEnable() {
        saveResource("doors.json", false);
        importDoorsFile();
        importPlayersFile();
        getCommand("xmasadm").setExecutor(this.adminCommand);
        Bukkit.getScheduler().runTaskTimer(this, this::onTick, 1L, 1L);
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        for (XmasDoor door: this.doorsJson.doors) door.clearMirage();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) return false;
        Player player = (Player)sender;
        PlayerProgress prog = getProgress(player);
        int today = Cal.today();
        if (today < 1 || today > 25) {
            player.sendMessage(ChatColor.RED + "Christmas is over.");
            return true;
        }
        player.sendMessage(ChatColor.GOLD + "Day of Christmas: " + today);
        player.sendMessage(ChatColor.GOLD + "Presents opened: " + prog.doorsOpened);
        return true;
    }

    // --- Persistence

    void importPlayersFile() {
        this.playersJson = loadJsonFile("players.json", PlayersJson.class, PlayersJson::new);
    }

    void savePlayers() {
        this.playersJson.dirty = false;
        saveJsonFile("players.json", this.playersJson, true);
    }

    void importDoorsFile() {
        this.doorsJson = loadJsonFile("doors.json", DoorsJson.class, DoorsJson::new);
        while (this.doorsJson.doors.size() < 25) this.doorsJson.doors.add(new XmasDoor());
        for (int i = 0; i < this.doorsJson.doors.size(); i += 1) {
            this.doorsJson.doors.get(i).setIndex(i + 1);
        }
        if (this.doorsJson.playerHeads.isEmpty()) this.doorsJson.playerHeads.add(new PlayerHead("dca29a3a-76d3-4979-88a2-2da034b99212", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmNlZjlhYTE0ZTg4NDc3M2VhYzEzNGE0ZWU4OTcyMDYzZjQ2NmRlNjc4MzYzY2Y3YjFhMjFhODViNyJ9fX0="));
    }

    void saveDoors() {
        this.doorsJson.dirty = false;
        saveJsonFile("doors.json", this.doorsJson, true);
    }

    <T> T loadJsonFile(String fn, Class<T> clazz, Supplier<T> dfl) {
        File file = new File(getDataFolder(), fn);
        if (!file.exists()) return dfl.get();
        try (FileReader fr = new FileReader(file)) {
            return new Gson().fromJson(fr, clazz);
        } catch (Exception e) {
            e.printStackTrace();
            return dfl.get();
        }
    }

    void saveJsonFile(String fn, Object obj, boolean pretty) {
        File dir = getDataFolder();
        dir.mkdirs();
        File file = new File(dir, fn);
        try (FileWriter fw = new FileWriter(file)) {
            Gson gson = pretty ? new GsonBuilder().setPrettyPrinting().create() : new Gson();
            gson.toJson(obj, fw);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    PlayerProgress getProgress(Player player) {
        final UUID uuid = player.getUniqueId();
        PlayerProgress result = this.playersJson.players.get(uuid);
        if (result == null) {
            result = new PlayerProgress();
            this.playersJson.players.put(uuid, result);
            this.playersJson.dirty = true;
        }
        return result;
    }

    // --- Ticking

    void onTick() {
        int today = Cal.today();
        if (this.doorsJson.dirty) saveDoors();
        if (this.playersJson.dirty) savePlayers();
        for (int i = 0; i < today; i += 1) {
            this.doorsJson.doors.get(i).tick(this);
        }
    }

    int adventOf(int day) {
        switch (day) {
        case 2: return 1;
        case 9: return 2;
        case 16: return 3;
        case 23: return 4;
        default: return -1;
        }
    }

    @Getter @RequiredArgsConstructor
    static class FakeInvHolder implements InventoryHolder {
        final int index;
        @Setter private Inventory inventory;
    }

    String st(int i) {
        switch (i) {
        case 1: return "First";
        case 2: return "Second";
        case 3: return "Third";
        case 4: return "Fourth";
        default: return "" + i;
        }
    }

    void findDoor(Player player, int index) {
        PlayerProgress prog = getProgress(player);
        if (prog.doorsOpened >= index) {
            player.sendMessage(ChatColor.RED + "You already opened this door!");
            return;
        }
        if (prog.doorsOpened < index - 1) {
            player.sendMessage(ChatColor.RED + "Open present " + (prog.doorsOpened + 1) + " first.");
            return;
        }
        prog.doorsOpened = index;
        this.playersJson.dirty = true;
        giveDoor(player, index);
        getServer().dispatchCommand(getServer().getConsoleSender(), "minecraft:advancement grant " + player.getName() + " until christmas:present" + index);
    }

    void giveDoor(Player player, int index) {
        String invTitle = ChatColor.BLUE + "Advent Present " + index;
        FakeInvHolder holder = new FakeInvHolder(index);
        Inventory inv = Bukkit.getServer().createInventory(holder, 9, invTitle);
        holder.inventory = inv;
        List<String> items = this.doorsJson.doors.get(index - 1).getItems();
        int invIndex = (9 - items.size()) / 2;
        for (String s: items) {
            inv.setItem(invIndex++, Dirty.deserializeItem(s));
        }
        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getInventory().getHolder() instanceof FakeInvHolder)) return;
        FakeInvHolder holder = (FakeInvHolder)event.getInventory().getHolder();
        Player player = (Player)event.getPlayer();
        for (ItemStack item: event.getInventory()) {
            if (item == null || item.getType() == Material.AIR) continue;
            player.getWorld().dropItem(player.getEyeLocation(), item).setPickupDelay(0);
        }
        player.sendTitle(ChatColor.GOLD + "Present " + holder.index, ChatColor.GOLD + "Found Advent Present " + holder.index);
        player.playSound(player.getEyeLocation(), Sound.ENTITY_PLAYER_LEVELUP, SoundCategory.MASTER, 1.0f, 1.0f);
    }

    @EventHandler
    public void onPlayerUseEntity(PlayerUseEntityEvent event) {
        int id = event.getEntityId();
        for (XmasDoor door: this.doorsJson.doors) {
            if (door.isId(id)) {
                findDoor(event.getPlayer(), door.getIndex());
                return;
            }
        }
    }
}
