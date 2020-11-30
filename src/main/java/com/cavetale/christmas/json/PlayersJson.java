package com.cavetale.christmas.json;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.Data;

/**
 * Not really saved as Json.
 * Players are now stored in players/<UUID>.json
 */
@Data
public final class PlayersJson {
    private transient boolean dirty;
    private Map<UUID, PlayerProgress> players = new HashMap<>();

    public void markDirty(PlayerProgress playerProgress) {
        playerProgress.setDirty(true);
        dirty = true;
    }
}
