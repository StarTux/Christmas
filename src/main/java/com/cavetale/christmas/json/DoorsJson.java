package com.cavetale.christmas.json;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public final class DoorsJson {
    private transient boolean dirty;
    private List<XmasDoor> doors = new ArrayList<>();
    private List<PlayerHead> playerHeads = new ArrayList<>();

    public PlayerHead getPlayerHead(int index) {
        return playerHeads.get(index % playerHeads.size());
    }
}
