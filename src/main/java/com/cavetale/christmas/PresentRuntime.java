package com.cavetale.christmas;

import com.cavetale.christmas.json.Present;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;

@RequiredArgsConstructor @Getter
public final class PresentRuntime {
    private final ChristmasPlugin plugin;
    private final int index; // 1 - 25, number of day
    private ArmorStand armorStand;
    private int respawnCooldown = 0;
    private int ticks = 0;
    private double yaw = 0;

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
        if (!armorStand.isValid()) {
            clearArmorStand();
            return;
        }
        if (ticks++ % 2 == 0) {
            EulerAngle headPose = armorStand.getHeadPose();
            yaw += 0.04;
            if (yaw > 1.0) yaw -= 2.0;
            headPose = headPose.setY(Math.sin(yaw * Math.PI));
            armorStand.setHeadPose(headPose);
        }
    }

    public ArmorStand spawnArmorStand() {
        Present present = getPresent();
        World w = present.getWorld();
        if (w == null) return null;
        if (!present.isChunkLoaded(w)) return null;
        ItemStack helmet = plugin.getPresentsJson().getPresentItem(index - 1).toItemStack();
        armorStand = w.spawn(present.toLocation(w), ArmorStand.class, as -> {
                as.setPersistent(false);
                as.setVisible(false);
                as.setHelmet(helmet);
                as.setSmall(true);
            });
        if (plugin.isDebug()) {
            plugin.getLogger().info("Spawned Present #" + index
                                    + " at " + present.toLocationString());
        }
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
