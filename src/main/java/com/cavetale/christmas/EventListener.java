package com.cavetale.christmas;

import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;

@RequiredArgsConstructor
public final class EventListener implements Listener {
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
            player.getWorld().dropItem(player.getEyeLocation(), item).setPickupDelay(0);
        }
        player.sendTitle(ChatColor.GOLD + "Present " + holder.getIndex(),
                         ChatColor.GOLD + "Found Advent Present " + holder.getIndex());
        player.playSound(player.getEyeLocation(), Sound.ENTITY_PLAYER_LEVELUP, SoundCategory.MASTER, 1.0f, 1.0f);
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof ArmorStand)) return;
        ArmorStand armorStand = (ArmorStand) event.getRightClicked();
        for (PresentRuntime presentRuntime : plugin.getPresentRuntimes()) {
            if (!presentRuntime.isArmorStand(armorStand)) continue;
            plugin.findPresent(event.getPlayer(), presentRuntime.getIndex());
            return;
        }
    }
}
