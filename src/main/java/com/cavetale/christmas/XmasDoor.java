package com.cavetale.christmas;

import com.cavetale.mirage.DataVar;
import com.cavetale.mirage.EntityFlag;
import com.cavetale.mirage.Mirage;
import com.cavetale.mirage.MirageData;
import com.cavetale.mirage.MirageType;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import lombok.Data;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

/**
 * Gsonable
 */
@Data
final class XmasDoor {
    private transient int index; // 1 - 25, number of day
    private transient Mirage mirage;
    private transient int respawnCooldown = 0;
    // Location
    private String world = "spawn";
    private double x, y, z;
    private int cx, cz;
    private int ticks = 0;
    // Rewards
    private List<String> commands = new ArrayList<>();
    private List<String> items = new ArrayList<>();

    void setLocation(Location loc) {
        if (loc == null) throw new NullPointerException("loc cannot be null");
        this.world = loc.getWorld().getName();
        this.x = loc.getX();
        this.y = loc.getY();
        this.z = loc.getZ();
        this.cx = (int)Math.floor(x) >> 4;
        this.cz = (int)Math.floor(z) >> 4;
    }

    Location toLocation() {
        if (this.world == null) throw new NullPointerException("world cannot be null");
        World w = Bukkit.getWorld(this.world);
        if (w == null) throw new NullPointerException("World not found: " + this.world);
        return new Location(w, this.x, this.y, this.z, 0.0f, 0.0f);
    }

    void tick(ChristmasPlugin plugin) {
        if (this.mirage == null) {
            // Create skull
            List<PlayerHead> skulls = plugin.doorsJson.playerHeads;
            PlayerHead skull = skulls.get((this.index - 1) % skulls.size());
            ItemStack playerHead = skull.makeItem(this.index);
            // Put on head
            this.mirage = new Mirage(plugin);
            MirageData mirageData = new MirageData();
            mirageData.type = MirageType.MOB;
            mirageData.debugName = "xmas" + this.index;
            mirageData.entityType = EntityType.ARMOR_STAND;
            mirageData.location = MirageData.Location.fromBukkitLocation(toLocation().add(0, -1.35, 0));
            this.mirage.setup(mirageData);
            this.mirage.setEquipment(EquipmentSlot.HEAD, playerHead);
            this.mirage.setMetadata(DataVar.ENTITY_FLAGS, (byte)EntityFlag.ENTITY_INVISIBLE.bitMask);
            this.mirage.setMetadata(DataVar.ENTITY_CUSTOM_NAME_VISIBLE, true);
        } else {
            this.mirage.updateObserverList();
            if (this.mirage.getObservers().isEmpty()) return;
            float yaw = (float)(System.nanoTime() / 10000000L) * 0.33f;
            this.mirage.look(360.0f - yaw % 360.0f, 0.0f);
            if ((this.ticks++ % 5) == 0) {
                final ChatColor[] cs = {ChatColor.GOLD, ChatColor.GRAY, ChatColor.BLUE, ChatColor.GREEN, ChatColor.AQUA, ChatColor.RED, ChatColor.LIGHT_PURPLE, ChatColor.YELLOW};
                this.mirage.setCustomName("" + cs[ThreadLocalRandom.current().nextInt(cs.length)] + this.index);
            }
        }
    }

    void clearMirage() {
        if (this.mirage == null) return;
        this.mirage.removeAllObservers();
        this.mirage = null;
    }

    boolean isId(int id) {
        return this.mirage != null && this.mirage.getEntityId() == id;
    }
}
