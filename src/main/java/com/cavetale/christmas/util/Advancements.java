package com.cavetale.christmas.util;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class Advancements {
    private Advancements() { }

    /**
     * Unlock the advancement belonging to the given talent.
     * @param player The player
     * @param talent The talent, or null for the root advancement.
     * @return true if advancements were changed, false otherwise.
     */
    public static boolean give(Player player, JavaPlugin plugin, String name) {
        NamespacedKey key = new NamespacedKey(plugin, name);
        Advancement advancement = Bukkit.getAdvancement(key);
        if (advancement == null) {
            plugin.getLogger().warning("Advancement does not exist: " + key);
            return false;
        }
        AdvancementProgress progress = player.getAdvancementProgress(advancement);
        if (progress.isDone()) return false;
        progress.awardCriteria("impossible");
        plugin.getLogger().warning("Advancement given to " + player.getName() + ": " + key);
        return true;
    }

    public static boolean revoke(Player player, JavaPlugin plugin, String name) {
        NamespacedKey key = new NamespacedKey(plugin, name);
        Advancement advancement = Bukkit.getAdvancement(key);
        if (advancement == null) {
            plugin.getLogger().warning("Advancement does not exist: " + key);
            return false;
        }
        AdvancementProgress progress = player.getAdvancementProgress(advancement);
        if (!progress.isDone()) return false;
        progress.revokeCriteria("impossible");
        plugin.getLogger().warning("Advancement revoked from " + player.getName() + ": " + key);
        return true;
    }
}
