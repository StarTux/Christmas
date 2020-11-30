package com.cavetale.christmas;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

@Getter @RequiredArgsConstructor
public final class FakeInvHolder implements InventoryHolder {
    private final int index;
    @Setter private Inventory inventory;
}

