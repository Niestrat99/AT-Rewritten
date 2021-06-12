package io.github.niestrat99.advancedteleport.listeners;

import io.github.niestrat99.advancedteleport.api.ATPlayer;
import io.github.niestrat99.advancedteleport.api.Warp;
import io.github.niestrat99.advancedteleport.commands.home.HomeCommand;
import io.github.niestrat99.advancedteleport.commands.spawn.SpawnCommand;
import io.github.niestrat99.advancedteleport.commands.teleport.Tpr;
import io.github.niestrat99.advancedteleport.commands.warp.WarpCommand;
import io.github.niestrat99.advancedteleport.commands.warp.WarpsCommand;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.NewConfig;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
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

public class AtSigns implements Listener {

    private final HashMap<String, ATSign> signRegistry;

    public AtSigns() {
        signRegistry = new HashMap<>();
        signRegistry.put("warps", new ATSign("Warps", NewConfig.get().USE_WARPS.get()) {
            @Override
            public void onInteract(Sign sign, Player player) {
                WarpsCommand.sendWarps(player);
            }

            @Override
            public boolean canCreate(Sign sign, Player player) {
                return true;
            }
        });

        signRegistry.put("warp", new ATSign("Warp", NewConfig.get().USE_WARPS.get()) {
            @Override
            public void onInteract(Sign sign, Player player) {
                if (Warp.getWarps().containsKey(sign.getLine(1))) {
                    WarpCommand.warp(Warp.getWarps().get(sign.getLine(1)), player);
                }
            }

            @Override
            public boolean canCreate(Sign sign, Player player) {
                if (sign.getLine(1).isEmpty()) {
                    CustomMessages.sendMessage(player, "Error.noWarpInput");
                    return false;
                } else {
                    if (Warp.getWarps().containsKey(sign.getLine(1))){
                        String warpName = sign.getLine(1);
                        sign.setLine(0, ChatColor.BLUE + "" + ChatColor.BOLD + "[Warp]");
                        sign.setLine(1, warpName);
                        CustomMessages.sendMessage(player, "Info.createdWarpSign");
                        return true;
                    } else {
                        CustomMessages.sendMessage(player, "Error.noSuchWarp");
                        return false;
                    }
                }
            }
        });
        signRegistry.put("home", new ATSign("Home", NewConfig.get().USE_HOMES.get()) {
            @Override
            public void onInteract(Sign sign, Player player) {

            }

            @Override
            public boolean canCreate(Sign sign, Player player) {
                return false;
            }
        });

        signRegistry.put("homes", new ATSign("Homes", NewConfig.get().USE_HOMES.get()) {
            @Override
            public void onInteract(Sign sign, Player player) {

            }

            @Override
            public boolean canCreate(Sign sign, Player player) {
                return false;
            }
        }); 

        signRegistry.put("bed", new ATSign("Bed", NewConfig.get().USE_HOMES.get()) {
            @Override
            public void onInteract(Sign sign, Player player) {
                ATPlayer atPlayer = ATPlayer.getPlayer(player);
                if (atPlayer.getBedSpawn() != null) {
                    HomeCommand.teleport(player, atPlayer.getBedSpawn());
                }
            }

            @Override
            public boolean canCreate(Sign sign, Player player) {
                return true;
            }
        });

        signRegistry.put("spawn", new ATSign("Spawn", NewConfig.get().USE_SPAWN.get()) {
            @Override
            public void onInteract(Sign sign, Player player) {
                String world = player.getWorld().getName();
                if (!sign.getLine(1).isEmpty()) {
                    world = sign.getLine(1);
                }
                SpawnCommand.spawn(player, world);
            }

            @Override
            public boolean canCreate(Sign sign, Player player) {
                return true;
            }
        });

        signRegistry.put("randomtp", new ATSign("RandomTP", NewConfig.get().USE_RANDOMTP.get()) {
            @Override
            public void onInteract(Sign sign, Player player) {
                if (!sign.getLine(1).isEmpty()) {
                    World otherWorld = Bukkit.getWorld(sign.getLine(1));
                    if (otherWorld != null) {
                        Tpr.randomTeleport(player, otherWorld);
                    } else {
                        CustomMessages.sendMessage(player, "Error.noSuchWorld");
                    }
                } else {
                    Tpr.randomTeleport(player, player.getWorld());
                }
            }

            @Override
            public boolean canCreate(Sign sign, Player player) {
                if (!sign.getLine(1).isEmpty()) {
                    World otherWorld = Bukkit.getWorld(sign.getLine(1));
                    if (otherWorld == null) {
                        CustomMessages.sendMessage(player, "Error.noSuchWorld");
                        return false;
                    }
                }
                return true;
            }
        });
    }

    @EventHandler
    public void onSignInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Player player = event.getPlayer();
            Block clickedBlock = event.getClickedBlock();
            BlockState blockState = clickedBlock.getState();
            if (blockState instanceof Sign) {
                Sign sign = (Sign) blockState;
                String command = ChatColor.stripColor(sign.getLine(0));
                if (command.length() > 2) {
                    command = command.substring(1, command.length() - 1).toLowerCase();
                }
                if (signRegistry.containsKey(command)) {
                    ATSign atSign = signRegistry.get(command);
                    if (atSign.isEnabled()) {
                        if (player.hasPermission(atSign.getRequiredPermission())) {
                            atSign.onInteract(sign, player);
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onSignPlace(SignChangeEvent event){
        Block placeBlock = event.getBlock();
        BlockState state = placeBlock.getState();
        Player player = event.getPlayer();
        if (state instanceof Sign) {
            Sign sign = (Sign) state;
            String command = ChatColor.stripColor(event.getLine(0));
            if (command.length() > 2) {
                command = command.substring(1, command.length() - 1).toLowerCase();
            }
            if (signRegistry.containsKey(command)) {
                ATSign atSign = signRegistry.get(command);
                if (atSign.isEnabled()) {
                    if (player.hasPermission(atSign.getAdminPermission())) {
                        sign.setLine(1, event.getLine(1));
                        if (atSign.canCreate(sign, player)) {
                            event.setLine(0, ChatColor.BLUE + "" + ChatColor.BOLD + "[" + atSign.getName() + "]");
                        }
                    } else {
                        CustomMessages.sendMessage(player, "Error.noPermissionSign");
                        event.setCancelled(true);
                    }
                }
            }
        }
    }
}
