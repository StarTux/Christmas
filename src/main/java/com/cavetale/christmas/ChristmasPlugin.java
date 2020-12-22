package com.cavetale.christmas;

import com.cavetale.christmas.json.Item;
import com.cavetale.christmas.json.PlayerProgress;
import com.cavetale.christmas.json.PlayersJson;
import com.cavetale.christmas.json.Present;
import com.cavetale.christmas.json.PresentsJson;
import com.cavetale.christmas.util.Advancements;
import com.cavetale.christmas.util.Cal;
import com.cavetale.core.util.Json;
import com.cavetale.mytems.Mytems;
import com.destroystokyo.paper.Title;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
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
    @Setter private boolean debug;

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
        Gui.onEnable(this);
        for (Player player : Bukkit.getOnlinePlayers()) {
            enter(player);
        }
    }

    @Override
    public void onDisable() {
        Gui.onDisable(this);
        if (presentsJson.isDirty()) savePresents();
        if (playersJson.isDirty()) savePlayers();
        clearPresents();
    }

    public void enter(Player player) {
        PlayerProgress playerProgress = getProgress(player);
        for (int i = 1; i <= 25; i += 1) {
            if (playerProgress.getPresentsOpened() < i) {
                Advancements.revoke(player, this, "present" + i);
            } else {
                Advancements.give(player, this, "present" + i);
            }
        }
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
            Json.save(new File(playersFolder, fn), playerProgress, true);
        }
    }

    public void importPresentsFile() {
        presentsJson = Json.load(new File(getDataFolder(), "presents.json"), PresentsJson.class, PresentsJson::new);
        while (presentsJson.getPresents().size() < 25) presentsJson.getPresents().add(new Present());
        if (presentsJson.getPresentItems().isEmpty()) {
            presentsJson.getPresentItems().add(Item.dummy());
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
        if (presentsJson.isDirty()) savePresents();
        if (playersJson.isDirty()) savePlayers();
        int today = debug ? 25 : Cal.today();
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
        if (debug) getLogger().info("findPresent(" + player.getName() + ", " + index + ")");
        PlayerProgress playerProgress = getProgress(player);
        int today = Cal.today();
        if (!debug && today < index) {
            player.sendMessage(ChatColor.RED + "It's not the " + st(index) + " yet!");
            return;
        }
        if (playerProgress.getPresentsOpened() >= index) {
            player.sendMessage(ChatColor.RED + "You already opened this present!");
            return;
        }
        if (playerProgress.getPresentsOpened() < index - 1) {
            player.sendMessage(ChatColor.RED + "Open present " + (playerProgress.getPresentsOpened() + 1) + " first.");
            return;
        }
        givePresent(player, index);
    }

    public void givePresent(Player player, int index) {
        Gui gui = new Gui(this);
        gui.title(ChatColor.BLUE + "Advent Present " + index);
        gui.size(27);
        List<Item> items = presentsJson.getPresents().get(index - 1).getItems();
        List<ItemStack> itemStacks = new ArrayList<>();
        List<Integer> indexes = new ArrayList<>();
        for (int i = 0; i < 27; i += 1) indexes.add(i);
        Collections.sort(indexes, (a, b) -> Integer.compare(Math.abs(a - 13), Math.abs(b - 13)));
        for (Item item : items) {
            itemStacks.add(item.toItemStack());
        }
        ItemStack christmasTokenItem = Mytems.CHRISTMAS_TOKEN.getMytem().getItem();
        christmasTokenItem.setAmount(((index - 1) % 3) + 1);
        itemStacks.add(christmasTokenItem);
        itemStacks.add(Mytems.KITTY_COIN.getMytem().getItem());
        for (int i = 0; i < itemStacks.size(); i += 1) {
            gui.setItem(indexes.get(i), itemStacks.get(i));
        }
        gui.setEditable(true);
        gui.onClose(event -> {
                getLogger().info(player.getName() + " received present #" + index);
                PlayerProgress playerProgress = getProgress(player);
                playerProgress.setPresentsOpened(index);
                playersJson.markDirty(playerProgress);
                Advancements.give(player, this, "present" + index);
                player.sendTitle(new Title("", ChatColor.GREEN + "Present #" + index + " Found!", 5, 20, 5));
                for (ItemStack item: event.getInventory()) {
                    if (item == null || item.getAmount() <= 0) continue;
                    for (ItemStack drop : player.getInventory().addItem(item).values()) {
                        player.getWorld().dropItem(player.getEyeLocation(), drop).setPickupDelay(0);
                    }
                }
                event.getInventory().clear();
            });
        gui.open(player);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, SoundCategory.MASTER, 0.25f, 2.0f);
    }
}
