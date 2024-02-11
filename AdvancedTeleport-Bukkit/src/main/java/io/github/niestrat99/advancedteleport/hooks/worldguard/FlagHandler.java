package io.github.niestrat99.advancedteleport.hooks.worldguard;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.StringFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.config.CustomMessages;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class FlagHandler {

    private static boolean enabled = false;
    public static StateFlag DANGEROUS_AREA_FLAG = null;
    public static StringFlag DANGEROUS_AREA_MESSAGE = null;

    public static void init() {
        final FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();

        // Create the flags
        try {
            StateFlag dangerousArea = new StateFlag("dangerous-area", false);
            registry.register(dangerousArea);
            DANGEROUS_AREA_FLAG = dangerousArea;

        } catch (FlagConflictException ex) {
            Flag<?> flag = registry.get("dangerous-area");
            if (flag instanceof StateFlag goodFlag) {
                DANGEROUS_AREA_FLAG = goodFlag;
            } else {
                CoreClass.getInstance().getLogger().warning(
                        "Another plugin has registered a conflicting dangerous-area flag! Custom WorldGuard flags for AT will not work.");
            }
        }

        try {
            StringFlag areaMessage = new StringFlag("dangerous-area-message");
            registry.register(areaMessage);
            DANGEROUS_AREA_MESSAGE = areaMessage;
        } catch (FlagConflictException ex) {
            Flag<?> flag = registry.get("dangerous-area-message");
            if (flag instanceof StringFlag goodFlag) {
                DANGEROUS_AREA_MESSAGE = goodFlag;
            } else {
                CoreClass.getInstance().getLogger().warning(
                        "Another plugin has registered a conflicting dangerous-area-message flag! Custom WorldGuard flags for AT will not work.");
            }
        }

        enabled = DANGEROUS_AREA_FLAG != null && DANGEROUS_AREA_MESSAGE != null;
    }

    public static boolean isDangerous(@NotNull Location toLocation, @NotNull Player player) {

        // No WorldGuard? No problem.
        if (!enabled) return false;

        // Get the region set
        final RegionContainer container =
                WorldGuard.getInstance().getPlatform().getRegionContainer();
        final RegionQuery query = container.createQuery();
        final ApplicableRegionSet set = query.getApplicableRegions(BukkitAdapter.adapt(toLocation));

        // Convert the player
        final LocalPlayer wgPlayer = WorldGuardPlugin.inst().wrapPlayer(player);

        // Test the region
        if (!set.testState(wgPlayer, DANGEROUS_AREA_FLAG)) return false;

        // Since it's dangerous, check for the message
        String message = set.queryValue(wgPlayer, DANGEROUS_AREA_MESSAGE);
        if (message != null) {
            CustomMessages.asAudience(player).sendMessage(CustomMessages.translate(message));
        } else {
            CustomMessages.sendMessage(player, "Info.dangerousArea");
        }
        return true;
    }
}
