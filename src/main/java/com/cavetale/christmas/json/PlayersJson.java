package com.cavetale.christmas.json;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.Data;

@Data
public final class PlayersJson {
    private transient boolean dirty;
    private Map<UUID, PlayerProgress> players = new HashMap<>();
}
