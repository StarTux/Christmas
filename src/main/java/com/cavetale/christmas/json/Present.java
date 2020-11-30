package com.cavetale.christmas.json;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

@Data
public final class Present {
    private String world = "spawn";
    private double x;
    private double y;
    private double z;
    private int cx;
    private int cz;
    private List<String> commands = new ArrayList<>();
    private List<String> items = new ArrayList<>();

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

    public boolean isChunkLoaded(World w) {
        return w.isChunkLoaded(cx, cz);
    }

    public boolean isChunkLoaded() {
        World w = getWorld();
        return w != null
            ? isChunkLoaded(w)
            : false;
    }
}
