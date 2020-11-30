package com.cavetale.christmas.json;

import java.util.Base64;
import lombok.Data;
import lombok.NonNull;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * (De)serializable with Json.
 */
@Data
public final class Item {
    private String material; // for readability
    private int amount; // for readability
    private String serialized;

    public Item() { }

    public Item(@NonNull final ItemStack item) {
        material = item.getType().name().toLowerCase();
        amount = item.getAmount();
        serialized = serialize(item);
    }

    public static Item dummy() {
        return new Item(new ItemStack(Material.STONE));
    }

    public static String serialize(final ItemStack itemStack) {
        if (itemStack == null) return null;
        byte[] bytes = itemStack.serializeAsBytes();
        return Base64.getEncoder().encodeToString(bytes);
    }

    public static ItemStack deserialize(final String string) {
        if (string == null) return null;
        byte[] bytes = Base64.getDecoder().decode(string);
        return ItemStack.deserializeBytes(bytes);
    }

    public ItemStack toItemStack() {
        return deserialize(serialized);
    }

    @Override
    public String toString() {
        return amount + "x" + material;
    }
}
