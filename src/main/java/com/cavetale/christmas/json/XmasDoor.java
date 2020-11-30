package com.cavetale.christmas.json;

import com.cavetale.christmas.ChristmasPlugin;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import lombok.Data;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.ItemStack;

@Data
public final class XmasDoor {
    private transient int index; // 1 - 25, number of day
    private transient ArmorStand armorStand;
    private transient int respawnCooldown = 0;
    // Location
    private String world = "spawn";
    private double x;
    private double y;
    private double z;
    private int cx;
    private int cz;
    private int ticks = 0;
    // Rewards
    private List<String> commands = new ArrayList<>();
    private List<String> items = new ArrayList<>();
    private static final ChatColor[] COLORS = {ChatColor.GOLD, ChatColor.GRAY, ChatColor.BLUE, ChatColor.GREEN,
                                               ChatColor.AQUA, ChatColor.RED, ChatColor.LIGHT_PURPLE, ChatColor.YELLOW};

    public void setLocation(Location loc) {
        if (loc == null) throw new NullPointerException("loc cannot be null");
        world = loc.getWorld().getName();
        x = loc.getX();
        y = loc.getY();
        z = loc.getZ();
        cx = (int) Math.floor(x) >> 4;
        cz = (int) Math.floor(z) >> 4;
    }

    public World getWorld() {
        return Bukkit.getWorld(world);
    }

    public Location toLocation(World w) {
        if (w == null) throw new NullPointerException("World not found: " + world);
        return new Location(w, x, y, z, 0.0f, 0.0f);
    }

    public Location toLocation() {
        if (world == null) throw new NullPointerException("world cannot be null");
        World w = Bukkit.getWorld(world);
        return toLocation(w);
    }

    public void tick(ChristmasPlugin plugin) {
        if (armorStand == null) {
            spawnArmorStand(plugin);
        } else {
            tickArmorStand();
        }
    }

    void tickArmorStand() {
        if (!armorStand.isValid()) clearArmorStand();
        float yaw = (float) (System.nanoTime() / 10000000L) * 0.33f;
        Location location = toLocation();
        location.setYaw(yaw);
        armorStand.teleport(location);
        if ((ticks++ % 5) == 0) {
            armorStand.setCustomName("" + COLORS[ThreadLocalRandom.current().nextInt(COLORS.length)] + index);
        }
    }

    public ArmorStand spawnArmorStand(ChristmasPlugin plugin) {
        World w = getWorld();
        if (w == null) return null;
        if (!w.isChunkLoaded(cx, cz)) return null;
        PlayerHead playerHead = plugin.getDoorsJson().getPlayerHead(index - 1);
        ItemStack helmet = playerHead.makeItem(index);
        armorStand = w.spawn(toLocation(w), ArmorStand.class, as -> {
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
