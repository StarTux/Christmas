package com.cavetale.christmas.json;

import lombok.Data;

@Data
public final class PlayerProgress {
    private transient boolean dirty;
    private int presentsOpened = 0;
}
