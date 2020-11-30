package com.cavetale.christmas;

import com.cavetale.christmas.json.PlayerHead;
import com.cavetale.christmas.json.PlayerProgress;
import com.cavetale.christmas.json.PlayersJson;
import com.cavetale.christmas.json.Present;
import com.cavetale.christmas.json.PresentsJson;
import com.cavetale.core.util.Json;
import com.cavetale.dirty.Dirty;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public final class ChristmasPlugin extends JavaPlugin {
    private PlayersJson playersJson;
    private PresentsJson presentsJson;
    private AdminCommand adminCommand;
    private ChristmasCommand christmasCommand;
    private EventListener eventListener;
    private final List<PresentRuntime> presentRuntimes = new ArrayList<>();
    private File playersFolder;

    @Override
    public void onEnable() {
        saveResource("presents.json", false);
        playersFolder = new File(getDataFolder(), "players");
        playersFolder.mkdirs();
        importPresentsFile();
        importPlayerFiles();
        adminCommand = new AdminCommand(this).enable();
        christmasCommand = new ChristmasCommand(this).enable();
        eventListener = new EventListener(this).enable();
        Bukkit.getScheduler().runTaskTimer(this, this::onTick, 1L, 1L);
        resetPresents();
    }

    @Override
    public void onDisable() {
        clearPresents();
    }

    public void createPresents() {
        for (int i = 1; i <= 25; i += 1) {
            presentRuntimes.add(new PresentRuntime(this, i));
        }
    }

    public void clearPresents() {
        for (PresentRuntime presentRuntime : presentRuntimes) {
            presentRuntime.clearArmorStand();
        }
        presentRuntimes.clear();
    }

    public void resetPresents() {
        clearPresents();
        createPresents();
    }

    public void importPlayerFiles() {
        playersJson = new PlayersJson();
        for (File file : playersFolder.listFiles()) {
            String name = file.getName();
            if (!name.endsWith(".json")) continue;
            name = name.substring(0, name.length() - 5);
            UUID uuid;
            try {
                uuid = UUID.fromString(name);
            } catch (IllegalArgumentException iae) {
                getLogger().warning("Invalid file name: " + file);
                continue;
            }
            PlayerProgress playerProgress = Json.load(file, PlayerProgress.class, PlayerProgress::new);
            playersJson.getPlayers().put(uuid, playerProgress);
        }
    }

    public void savePlayers() {
        playersJson.setDirty(false);
        for (Map.Entry<UUID, PlayerProgress> entry : playersJson.getPlayers().entrySet()) {
            PlayerProgress playerProgress = entry.getValue();
            if (!playerProgress.isDirty()) continue;
            UUID uuid = entry.getKey();
            String fn = "" + uuid + ".json";
            Json.save(new File(playersFolder, fn), playersJson, true);
        }
    }

    public void importPresentsFile() {
        presentsJson = Json.load(new File(getDataFolder(), "presents.json"), PresentsJson.class, PresentsJson::new);
        while (presentsJson.getPresents().size() < 25) presentsJson.getPresents().add(new Present());
        if (presentsJson.getPlayerHeads().isEmpty()) {
            @SuppressWarnings("LineLength")
            PlayerHead playerHead = new PlayerHead("dca29a3a-76d3-4979-88a2-2da034b99212",
                                                   "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmNlZjlhYTE0ZTg4NDc3M2VhYzEzNGE0ZWU4OTcyMDYzZjQ2NmRlNjc4MzYzY2Y3YjFhMjFhODViNyJ9fX0=");
            presentsJson.getPlayerHeads().add(playerHead);
        }
    }

    public void savePresents() {
        presentsJson.setDirty(false);
        Json.save(new File(getDataFolder(), "presents.json"), presentsJson, true);
    }

    public PlayerProgress getProgress(Player player) {
        return playersJson.getPlayers().computeIfAbsent(player.getUniqueId(), u -> new PlayerProgress());
    }

    void onTick() {
        int today = Cal.today();
        if (presentsJson.isDirty()) savePresents();
        if (playersJson.isDirty()) savePlayers();
        for (int i = 0; i < today; i += 1) {
            presentRuntimes.get(i).tick();
        }
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

    public void findPresent(Player player, int index) {
        PlayerProgress playerProgress = getProgress(player);
        if (playerProgress.getPresentsOpened() >= index) {
            player.sendMessage(ChatColor.RED + "You already opened this present!");
            return;
        }
        if (playerProgress.getPresentsOpened() < index - 1) {
            player.sendMessage(ChatColor.RED + "Open present " + (playerProgress.getPresentsOpened() + 1) + " first.");
            return;
        }
        playerProgress.setPresentsOpened(index);
        playersJson.markDirty(playerProgress);
        givePresent(player, index);
        getServer().dispatchCommand(getServer().getConsoleSender(), "minecraft:advancement grant " + player.getName() + " until christmas:present" + index);
    }

    public void givePresent(Player player, int index) {
        String invTitle = ChatColor.BLUE + "Advent Present " + index;
        FakeInvHolder holder = new FakeInvHolder(index);
        Inventory inv = Bukkit.getServer().createInventory(holder, 9, invTitle);
        holder.setInventory(inv);
        List<String> items = presentsJson.getPresents().get(index - 1).getItems();
        int invIndex = (9 - items.size()) / 2;
        for (String s: items) {
            inv.setItem(invIndex++, Dirty.deserializeItem(s));
        }
        player.openInventory(inv);
    }
}
