package com.cavetale.christmas.json;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import java.util.UUID;
import lombok.Value;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

@Value
public final class PlayerHead {
    public final String id;
    public final String texture;

    public ItemStack makeItem(int index) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        String name = "Xmas Present (" + index + ")";
        meta.setDisplayNameComponent(new BaseComponent[] {new TextComponent(name)});
        UUID uuid = UUID.fromString(id);
        PlayerProfile profile = Bukkit.createProfile(uuid);
        ProfileProperty property = new ProfileProperty("textures", texture, (String) null);
        profile.setProperty(property);
        meta.setPlayerProfile(profile);
        item.setItemMeta(meta);
        return item;
    }
}
