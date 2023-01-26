package io.github.niestrat99.advancedteleport.config;

import io.github.thatsmusic99.configurationmaster.api.ConfigSection;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public final class GUI extends ATConfig {

    private static GUI instance;

    public GUI() throws IOException {
        super("guis.yml");
        instance = this;
    }

    @Override
    public void loadDefaults() {
        //Warps Menu
        addDefault("warps.slots", 27);
        addDefault("warps.gui-enabled", false);
        // Last Page icon
        addDefault("warps.icons.last-page.name", "&bLast Page");
        addDefault("warps.icons.last-page.item", "ARROW");
        addDefault("warps.icons.last-page.tooltip", new ArrayList<>());
        addDefault("warps.icons.last-page.slot", 18);
        // Next Page icon
        addDefault("warps.icons.next-page.name", "&bNext Page");
        addDefault("warps.icons.next-page.item", "ARROW");
        addDefault("warps.icons.next-page.tooltip", new ArrayList<>());
        addDefault("warps.icons.next-page.slot", 26);
        makeSectionLenient("warps.warps");
        //Warps Menu Example
        addComment("warps.warps.warpName1", "This is an example icon in the GUI.\n" +
                "The name - warpName1 - is the warp name used in /warp, e.g. /warp warpName1.");
        addExample("warps.warps.warpName1.name", "&aWarp Name 1" , "The display name for the warp's item in the GUI.");
        addExample("warps.warps.warpName1.item", "DIAMOND_SWORD", "The item the warp is represented by in the GUI.\n" +
                "If you are on a legacy version (< v1.13), you can specify extra data by adding a data-value option, which takes an integer:\n" +
                "data-value: 1\n" +
                "If you are using a player head item (SKULL_ITEM and data-value: 3 or PLAYER_HEAD in new versions), you can specify a texture:\n" +
                "texture: eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjQxNDQ5MDk3YjRiNzlhOWY2Y2FmNjM0NDQxOGYyMDM0ZGU0YmI5NzFmZWI3YThlNGFhY2JmYjkwNWFjZGNlZiJ9fX0=\n");
        addExample("warps.warps.warpName1.tooltip", Collections.singletonList("&7Teleports you to warpName1"));
        addExample("warps.warps.warpName1.slot", 11, "The slot (starting from 0) that the warp should appear in.");
        addExample("warps.warps.warpName1.page", 0, "The page (starting from 0) that the warp should appear in.");
        addExample("warps.warps.warpName1.hide-if-no-permission", false, "Whether the warp should be hidden from the user or not if they don't have permission to it.");

        addExample("warps.warps.warpName2.name", "&7Warp Name 2");
        addExample("warps.warps.warpName2.item", "IRON_PICKAXE");
        addExample("warps.warps.warpName2.tooltip", Collections.singletonList("&7Teleports you to warpName2"));
        addExample("warps.warps.warpName2.slot", 13);
        addExample("warps.warps.warpName2.page", 0);
        addExample("warps.warps.warpName2.hide-if-no-permission", true);
    }

    public static GUI get() {
        return instance;
    }

    public static ConfigSection getWarpsMenu() { return get().getConfigSection("warps.warps"); }
    public static int getWarpsMenuSlots() { return get().getInteger("warps.slots"); }
    public static boolean isUsingWarpsGUIMenu() { return get().getBoolean("warps.gui-enabled"); }
    public static ConfigSection getLastPageIcon() { return get().getConfigSection("warps.icons.last-page"); }
    public static ConfigSection getNextPageIcon() { return get().getConfigSection("warps.icons.next-page"); }


}
