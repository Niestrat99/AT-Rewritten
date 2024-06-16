package io.github.niestrat99.advancedteleport.managers;

import io.github.niestrat99.advancedteleport.api.ATSign;
import io.github.niestrat99.advancedteleport.api.AdvancedTeleportAPI;
import io.github.niestrat99.advancedteleport.api.signs.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public class SignManager {

    private static SignManager instance;
    private final @NotNull HashMap<String, ATSign> signs;

    public SignManager() {
        instance = this;

        this.signs = new HashMap<>();
        AdvancedTeleportAPI.registerSign("warps", new WarpsSign());
        AdvancedTeleportAPI.registerSign("warp", new WarpSign());
        AdvancedTeleportAPI.registerSign("home", new HomeSign());
        AdvancedTeleportAPI.registerSign("homes", new HomesSign());
        AdvancedTeleportAPI.registerSign("bed", new BedSign());
        AdvancedTeleportAPI.registerSign("spawn", new SpawnSign());
        AdvancedTeleportAPI.registerSign("randomtp", new RandomTPSign());
    }

    public static SignManager get() {
        return instance;
    }

    public void register(final @NotNull String name, final @NotNull ATSign sign) {
        this.signs.put(name, sign);
    }

    public @Nullable ATSign getSign(final @NotNull String name) {
        return this.signs.get(name);
    }

    public @Nullable ATSign getSignByFlatDisplayName(final @NotNull TextComponent component) {
        for (var sign : this.signs.values()) {
            CoreClass.debug("Display name for " + sign + ": " + sign.getDisplayName());
	    CoreClass.debug("Display name for " + sign + " matches using equals: " + sign.getDisplayName().equals(component));
	    CoreClass.debug("Display name for " + sign + " matches using content equals: " + sign.getDisplayName().content().equals(component.content());
	    if (!sign.getDisplayName().equals(component)) continue;
            return sign;
        }

        return null;
    }

    public @Nullable ATSign getSignByDisplayName(final @NotNull Component component) {

        // uggggghh
        for (var sign : this.signs.values()) {
            if (!hardEquals(sign.getDisplayName(), component)) continue;
            return sign;
        }

        return null;
    }

    private boolean hardEquals(final @NotNull Component c1, final @NotNull Component c2) {
        if (c1 == c2) return true;
        if (!(c1 instanceof TextComponent tc1 && c2 instanceof TextComponent tc2)) return false;
        if (!tc1.equals(tc2)) return false;
        return tc1.style().equals(tc2.style());
    }
}
