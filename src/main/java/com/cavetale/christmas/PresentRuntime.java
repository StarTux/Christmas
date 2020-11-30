package com.cavetale.christmas;

import com.cavetale.christmas.json.PlayerHead;
import com.cavetale.christmas.json.Present;
import java.util.concurrent.ThreadLocalRandom;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.ItemStack;

@RequiredArgsConstructor @Getter
public final class PresentRuntime {
    private static final ChatColor[] COLORS = {ChatColor.GOLD, ChatColor.GRAY, ChatColor.BLUE, ChatColor.GREEN,
                                               ChatColor.AQUA, ChatColor.RED, ChatColor.LIGHT_PURPLE, ChatColor.YELLOW};
    private final ChristmasPlugin plugin;
    private final int index; // 1 - 25, number of day
    private ArmorStand armorStand;
    private int respawnCooldown = 0;
    private int ticks = 0;

    public void tick() {
        if (armorStand == null) {
            spawnArmorStand();
        } else {
            tickArmorStand();
        }
    }

    public Present getPresent() {
        return plugin.getPresentsJson().getPresent(index - 1);
    }

    public void tickArmorStand() {
        Present present = getPresent();
        if (!armorStand.isValid()) clearArmorStand();
        float yaw = (float) (System.nanoTime() / 10000000L) * 0.33f;
        Location location = present.toLocation();
        location.setYaw(yaw);
        armorStand.teleport(location);
        if ((ticks++ % 5) == 0) {
            armorStand.setCustomName("" + COLORS[ThreadLocalRandom.current().nextInt(COLORS.length)] + index);
        }
    }

    public ArmorStand spawnArmorStand() {
        Present present = getPresent();
        World w = present.getWorld();
        if (w == null) return null;
        if (!present.isChunkLoaded(w)) return null;
        PlayerHead playerHead = plugin.getPresentsJson().getPlayerHead(index - 1);
        ItemStack helmet = playerHead.makeItem(index);
        armorStand = w.spawn(present.toLocation(w), ArmorStand.class, as -> {
                as.setPersistent(false);
                as.setVisible(false);
                as.setMarker(true);
                as.setHelmet(helmet);
                as.setCustomNameVisible(true);
            });
        return armorStand;
    }

    public void clearArmorStand() {
        if (armorStand == null) return;
        armorStand.remove();
        armorStand = null;
    }

    public boolean isArmorStand(ArmorStand other) {
        return armorStand != null && armorStand.equals(other);
    }
}
