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
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;

@RequiredArgsConstructor
public final class EventListener implements Listener {
    public static final String CHECKMARK = "\u2713";
    private final ChristmasPlugin plugin;

    public EventListener enable() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        return this;
    }

    @EventHandler
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
        if (!(event.getRightClicked() instanceof ArmorStand)) return;
        Player player = event.getPlayer();
        ArmorStand armorStand = (ArmorStand) event.getRightClicked();
        for (PresentRuntime presentRuntime : plugin.getPresentRuntimes()) {
            if (!presentRuntime.isArmorStand(armorStand)) continue;
            event.setCancelled(true);
            if (!player.hasPermission("christmas.christmas")) return;
            plugin.findPresent(player, presentRuntime.getIndex());
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
        int today = Cal.today();
        if (today < 1 || today > 25) return;
        int progress = plugin.getProgress(player).getPresentsOpened();
        if (progress >= today || progress >= 25) {
            int hoursLeft = Cal.hoursLeft();
            String hoursLeftString = hoursLeft == 1 ? "1 hour" : hoursLeft + " hours";
            event.addLines(plugin, Priority.HIGHEST,
                           Arrays.asList("Xmas " + ChatColor.GREEN + CHECKMARK,
                                         "Next " + ChatColor.GREEN + hoursLeftString));
        } else {
            Present present = plugin.getPresentsJson().getPresent(progress);
            int gifts = today - progress;
            List<String> lines = new ArrayList<>();
            lines.add("Xmas " + ChatColor.GREEN + "Present #" + (progress + 1));
            lines.addAll(Text.wrapLine("Hint " + ChatColor.GRAY + present.getHint(), 18));
            lines.add("See " + ChatColor.GREEN + "/xmas");
            event.addLines(plugin, Priority.HIGHEST, lines);
        }
    }
}
