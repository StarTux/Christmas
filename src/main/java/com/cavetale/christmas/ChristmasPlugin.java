package com.cavetale.christmas;

import com.cavetale.christmas.json.DoorsJson;
import com.cavetale.christmas.json.PlayerHead;
import com.cavetale.christmas.json.PlayerProgress;
import com.cavetale.christmas.json.PlayersJson;
import com.cavetale.christmas.json.XmasDoor;
import com.cavetale.dirty.Dirty;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.List;
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
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public final class ChristmasPlugin extends JavaPlugin implements Listener {
    PlayersJson playersJson;
    DoorsJson doorsJson;
    private AdminCommand adminCommand = new AdminCommand(this);

    @Override
    public void onEnable() {
        saveResource("doors.json", false);
        importDoorsFile();
        importPlayersFile();
        getCommand("xmasadm").setExecutor(adminCommand);
        Bukkit.getScheduler().runTaskTimer(this, this::onTick, 1L, 1L);
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        for (XmasDoor door : doorsJson.getDoors()) {
            door.clearArmorStand();
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) return false;
        Player player = (Player) sender;
        PlayerProgress prog = getProgress(player);
        int today = Cal.today();
        if (today < 1 || today > 25) {
            player.sendMessage(ChatColor.RED + "Christmas is over.");
            return true;
        }
        player.sendMessage(ChatColor.GOLD + "Day of Christmas: " + today);
        player.sendMessage(ChatColor.GOLD + "Presents opened: " + prog.getDoorsOpened());
        return true;
    }

    // --- Persistence

    public void importPlayersFile() {
        playersJson = loadJsonFile("players.json", PlayersJson.class, PlayersJson::new);
    }

    public void savePlayers() {
        playersJson.setDirty(false);
        saveJsonFile("players.json", playersJson, true);
    }

    public void importDoorsFile() {
        if (doorsJson != null) {
            for (XmasDoor door: doorsJson.getDoors()) {
                door.clearArmorStand();
            }
        }
        doorsJson = loadJsonFile("doors.json", DoorsJson.class, DoorsJson::new);
        while (doorsJson.getDoors().size() < 25) doorsJson.getDoors().add(new XmasDoor());
        for (int i = 0; i < doorsJson.getDoors().size(); i += 1) {
            doorsJson.getDoors().get(i).setIndex(i + 1);
        }
        if (doorsJson.getPlayerHeads().isEmpty()) {
            @SuppressWarnings("LineLength")
            PlayerHead playerHead = new PlayerHead("dca29a3a-76d3-4979-88a2-2da034b99212",
                                                   "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmNlZjlhYTE0ZTg4NDc3M2VhYzEzNGE0ZWU4OTcyMDYzZjQ2NmRlNjc4MzYzY2Y3YjFhMjFhODViNyJ9fX0=");
            doorsJson.getPlayerHeads().add(playerHead);
        }
    }

    public void saveDoors() {
        doorsJson.setDirty(false);
        saveJsonFile("doors.json", doorsJson, true);
    }

    public <T> T loadJsonFile(String fn, Class<T> clazz, Supplier<T> dfl) {
        File file = new File(getDataFolder(), fn);
        if (!file.exists()) return dfl.get();
        try (FileReader fr = new FileReader(file)) {
            return new Gson().fromJson(fr, clazz);
        } catch (Exception e) {
            e.printStackTrace();
            return dfl.get();
        }
    }

    public  void saveJsonFile(String fn, Object obj, boolean pretty) {
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

    public PlayerProgress getProgress(Player player) {
        final UUID uuid = player.getUniqueId();
        PlayerProgress result = playersJson.getPlayers().get(uuid);
        if (result == null) {
            result = new PlayerProgress();
            playersJson.getPlayers().put(uuid, result);
            playersJson.setDirty(true);
        }
        return result;
    }

    // --- Ticking

    void onTick() {
        int today = Cal.today();
        if (doorsJson.isDirty()) saveDoors();
        if (playersJson.isDirty()) savePlayers();
        for (int i = 0; i < today; i += 1) {
            doorsJson.getDoors().get(i).tick(this);
        }
    }

    @Getter @RequiredArgsConstructor
    static class FakeInvHolder implements InventoryHolder {
        final int index;
        @Setter private Inventory inventory;
    }

    public static String st(int i) {
        switch (i) {
        case 1: return "First";
        case 2: return "Second";
        case 3: return "Third";
        case 4: return "Fourth";
        default: return "" + i;
        }
    }

    public void findDoor(Player player, int index) {
        PlayerProgress prog = getProgress(player);
        if (prog.getDoorsOpened() >= index) {
            player.sendMessage(ChatColor.RED + "You already opened this door!");
            return;
        }
        if (prog.getDoorsOpened() < index - 1) {
            player.sendMessage(ChatColor.RED + "Open present " + (prog.getDoorsOpened() + 1) + " first.");
            return;
        }
        prog.setDoorsOpened(index);
        playersJson.setDirty(true);
        giveDoor(player, index);
        getServer().dispatchCommand(getServer().getConsoleSender(), "minecraft:advancement grant " + player.getName() + " until christmas:present" + index);
    }

    public void giveDoor(Player player, int index) {
        String invTitle = ChatColor.BLUE + "Advent Present " + index;
        FakeInvHolder holder = new FakeInvHolder(index);
        Inventory inv = Bukkit.getServer().createInventory(holder, 9, invTitle);
        holder.inventory = inv;
        List<String> items = doorsJson.getDoors().get(index - 1).getItems();
        int invIndex = (9 - items.size()) / 2;
        for (String s: items) {
            inv.setItem(invIndex++, Dirty.deserializeItem(s));
        }
        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getInventory().getHolder() instanceof FakeInvHolder)) return;
        FakeInvHolder holder = (FakeInvHolder) event.getInventory().getHolder();
        Player player = (Player) event.getPlayer();
        for (ItemStack item: event.getInventory()) {
            if (item == null || item.getType() == Material.AIR) continue;
            player.getWorld().dropItem(player.getEyeLocation(), item).setPickupDelay(0);
        }
        player.sendTitle(ChatColor.GOLD + "Present " + holder.index, ChatColor.GOLD + "Found Advent Present " + holder.index);
        player.playSound(player.getEyeLocation(), Sound.ENTITY_PLAYER_LEVELUP, SoundCategory.MASTER, 1.0f, 1.0f);
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof ArmorStand)) return;
        ArmorStand armorStand = (ArmorStand) event.getRightClicked();
        for (XmasDoor door : doorsJson.getDoors()) {
            if (!door.isArmorStand(armorStand)) continue;
            findDoor(event.getPlayer(), door.getIndex());
            return;
        }
    }
}
