package com.cavetale.christmas;

import com.cavetale.npc.NPC;
import com.cavetale.npc.NPCPlugin;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import lombok.Data;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

/**
 * Gsonable
 */
@Data
final class XmasDoor {
    private transient int index; // 1 - 25, number of day
    private transient NPC npc;
    private transient int respawnCooldown = 0;
    // Location
    private String world = "spawn";
    private double x, y, z;
    private float pitch, yaw;
    private int cx, cz;
    // Rewards
    private List<String> commands = new ArrayList<>();
    private List<String> items = new ArrayList<>();

    void setLocation(Location loc) {
        if (loc == null) throw new NullPointerException("loc cannot be null");
        this.world = loc.getWorld().getName();
        this.x = loc.getX();
        this.y = loc.getY();
        this.z = loc.getZ();
        this.pitch = loc.getPitch();
        this.yaw = loc.getYaw();
        this.cx = (int)Math.floor(x) >> 4;
        this.cz = (int)Math.floor(z) >> 4;
    }

    Location toLocation() {
        if (this.world == null) throw new NullPointerException("world cannot be null");
        World w = Bukkit.getWorld(this.world);
        if (w == null) throw new NullPointerException("World not found: " + this.world);
        return new Location(w, this.x, this.y, this.z, this.yaw, this.pitch);
    }

    void tick(ChristmasPlugin plugin) {
        if (npc == null || !npc.isValid()) {
            if (respawnCooldown > 0) {
                respawnCooldown -= 1;
                return;
            }
            World w = Bukkit.getWorld(this.world);
            if (w == null || !w.isChunkLoaded(this.cx, this.cz)) return;
            // Create skull
            List<PlayerHead> skulls = plugin.doorsJson.playerHeads;
            PlayerHead skull = skulls.get((this.index - 1) % skulls.size());
            ItemStack playerHead = skull.makeItem(this.index);
            // Put on head
            final NPCPlugin npcPlugin = NPCPlugin.getInstance();
            this.npc = new NPC(npcPlugin, NPC.Type.MOB, this.toLocation().add(0.0, -1.5, 0.0), EntityType.ARMOR_STAND);
            this.npc.setEquipment(EquipmentSlot.HEAD, playerHead);
            this.npc.setFlag(NPC.DataVar.ENTITY_FLAGS, NPC.EntityFlag.ENTITY_INVISIBLE, true);
            this.npc.setData(NPC.DataVar.ENTITY_CUSTOM_NAME_VISIBLE, true);
            this.npc.setLifespan(2L);
            this.npc.setDelegate(new NPC.Delegate() {
                    @Override public void onTick(NPC n) { }
                    @Override public boolean onInteract(NPC n, Player p, boolean r) {
                        plugin.findDoor(p, XmasDoor.this.index);
                        return true;
                    }
                });
            npcPlugin.enableNPC(this.npc);
        } else {
            this.npc.setLifespan(this.npc.getLifespan() + 1L);
            Location loc = this.npc.getLocation();
            loc.setYaw(loc.getYaw() + 5.0f);
            this.npc.setLocation(loc);
            this.npc.setHeadYaw(loc.getYaw());
            final ChatColor[] cs = {ChatColor.GOLD, ChatColor.GRAY, ChatColor.BLUE, ChatColor.GREEN, ChatColor.AQUA, ChatColor.RED, ChatColor.LIGHT_PURPLE, ChatColor.YELLOW};
            this.npc.updateCustomName("" + cs[ThreadLocalRandom.current().nextInt(cs.length)] + this.index);
        }
    }
}
