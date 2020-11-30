package com.cavetale.christmas.json;

@SuppressWarnings("VisibilityModifier")
public final class AdvancementJson {
    public Criteria criteria = new Criteria();
    public Display display = new Display();
    public String parent = null;

    public static final class Trigger {
        public String trigger = "minecraft:impossible";
    }

    public static final class Criteria {
        public Trigger impossible = new Trigger();
    }

    public static final class Icon {
        public String item = "minecraft:golden_apple";
        public String nbt = null;
    }

    @SuppressWarnings("MemberName")
    public static final class Display {
        public String title = null;
        public String description = null;
        public boolean show_toast = true;
        public boolean hidden = false;
        public String background = null;
        public Icon icon = new Icon();
        public boolean announce_to_chat = true;
    }
}
