package com.cavetale.christmas.json;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public final class PresentsJson {
    private transient boolean dirty;
    private List<Present> presents = new ArrayList<>();
    private List<PlayerHead> playerHeads = new ArrayList<>();

    public Present getPresent(int index) {
        if (index < 0) throw new IllegalArgumentException("index=" + index);
        if (index >= presents.size()) throw new IllegalArgumentException("index=" + index + "/" + presents.size());
        return presents.get(index);
    }

    public PlayerHead getPlayerHead(int index) {
        if (index < 0) throw new IllegalArgumentException("index=" + index);
        return playerHeads.get(index % playerHeads.size());
    }
}
