package io.github.niestrat99.advancedteleport.listeners;

import io.github.niestrat99.advancedteleport.api.ATSign;
import io.github.niestrat99.advancedteleport.api.signs.BedSign;
import io.github.niestrat99.advancedteleport.api.signs.HomeSign;
import io.github.niestrat99.advancedteleport.api.signs.HomesSign;
import io.github.niestrat99.advancedteleport.api.signs.RandomTPSign;
import io.github.niestrat99.advancedteleport.api.signs.SpawnSign;
import io.github.niestrat99.advancedteleport.api.signs.WarpSign;
import io.github.niestrat99.advancedteleport.api.signs.WarpsSign;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashMap;

public class SignInteractListener implements Listener {

    private final HashMap<String, ATSign> signRegistry;

    public SignInteractListener() {
        signRegistry = new HashMap<>();

        // Register each sign
        signRegistry.put("warps", new WarpsSign());
        signRegistry.put("warp", new WarpSign());
        signRegistry.put("home", new HomeSign());
        signRegistry.put("homes", new HomesSign());
        signRegistry.put("bed", new BedSign());
        signRegistry.put("spawn", new SpawnSign());
        signRegistry.put("randomtp", new RandomTPSign());
    }

    @EventHandler
    public void onSignInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Player player = event.getPlayer();
        Block clickedBlock = event.getClickedBlock();
        BlockState blockState = clickedBlock.getState();
        if (!(blockState instanceof Sign sign)) return;
        String command = ChatColor.stripColor(sign.getLine(0));
        if (command.length() > 2) {
            command = command.substring(1, command.length() - 1).toLowerCase();
        }
        if (!signRegistry.containsKey(command)) return;
        ATSign atSign = signRegistry.get(command);
        if (!atSign.isEnabled()) return;
        if (!player.hasPermission(atSign.getRequiredPermission())) return;
        atSign.onInteract(sign, player);
    }

    @EventHandler
    public void onSignPlace(SignChangeEvent event) {
        Block placeBlock = event.getBlock();
        BlockState state = placeBlock.getState();
        Player player = event.getPlayer();
        // Make sure it's a sign
        if (!(state instanceof Sign sign)) return;
        String command = ChatColor.stripColor(event.getLine(0));
        if (command.length() > 2) {
            command = command.substring(1, command.length() - 1).toLowerCase();
        }
        if (!signRegistry.containsKey(command)) return;
        ATSign atSign = signRegistry.get(command);
        if (!atSign.isEnabled()) return;
        if (player.hasPermission(atSign.getAdminPermission())) {
            sign.setLine(1, event.getLine(1));
            if (!atSign.canCreate(sign, player)) return;
            event.setLine(0, ChatColor.BLUE + "" + ChatColor.BOLD + "[" + atSign.getName() + "]");
        } else {
            CustomMessages.sendMessage(player, "Error.noPermissionSign");
            event.setCancelled(true);
        }
    }
}
