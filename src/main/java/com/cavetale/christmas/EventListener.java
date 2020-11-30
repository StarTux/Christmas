package com.cavetale.christmas;

import com.cavetale.christmas.json.Present;
import com.cavetale.christmas.util.Cal;
import com.cavetale.christmas.util.Text;
import com.cavetale.sidebar.PlayerSidebarEvent;
import com.cavetale.sidebar.Priority;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

@RequiredArgsConstructor
public final class EventListener implements Listener {
    public static final String CHECKMARK = "\u2713";
    private final ChristmasPlugin plugin;

    public EventListener enable() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        return this;
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getInventory().getHolder() instanceof FakeInvHolder)) return;
        FakeInvHolder holder = (FakeInvHolder) event.getInventory().getHolder();
        Player player = (Player) event.getPlayer();
        for (ItemStack item: event.getInventory()) {
            if (item == null || item.getType() == Material.AIR) continue;
            for (ItemStack drop : player.getInventory().addItem(item).values()) {
                player.getWorld().dropItem(player.getEyeLocation(), drop).setPickupDelay(0);
            }
        }
    }

    @EventHandler
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
        if (!(event.getRightClicked() instanceof ArmorStand)) return;
        ArmorStand armorStand = (ArmorStand) event.getRightClicked();
        for (PresentRuntime presentRuntime : plugin.getPresentRuntimes()) {
            if (!presentRuntime.isArmorStand(armorStand)) continue;
            plugin.findPresent(event.getPlayer(), presentRuntime.getIndex());
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        plugin.enter(event.getPlayer());
    }

    @EventHandler
    public void onPlayerSidebar(PlayerSidebarEvent event) {
        Player player = event.getPlayer();
        boolean debug = plugin.isDebug();
        int today = debug
            ? plugin.getProgress(player).getPresentsOpened() + 1
            : Cal.today();
        if (today < 1 || today > 25) return;
        int progress = plugin.getProgress(player).getPresentsOpened();
        if ((!debug && progress >= today) || progress >= 25) {
            event.addLines(plugin, Priority.HIGHEST,
                           Arrays.asList("Xmas " + ChatColor.GREEN + CHECKMARK));
        } else {
            Present present = plugin.getPresentsJson().getPresent(today - 1);
            int gifts = today - progress;
            List<String> lines = new ArrayList<>();
            lines.add("Xmas " + ChatColor.GREEN + "Present #" + today);
            if (debug) lines.add("" + ChatColor.RED + ChatColor.BOLD + "DEBUG " + CHECKMARK);
            lines.addAll(Text.wrapLine("Hint " + ChatColor.GRAY + present.getHint(), 18));
            lines.add("See " + ChatColor.GREEN + "/xmas");
            event.addLines(plugin, Priority.HIGHEST, lines);
        }
    }
}
