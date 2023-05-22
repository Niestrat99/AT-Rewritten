package io.github.niestrat99.advancedteleport.managers;

import io.github.niestrat99.advancedteleport.api.ATPlayer;
import io.github.niestrat99.advancedteleport.config.MainConfig;
import io.github.niestrat99.advancedteleport.folia.RunnableManager;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class CooldownManager {

    private static final HashMap<String, List<ATRunnable>> cooldown = new HashMap<>();

    public static int secondsLeftOnCooldown(String command, Player player) {
        if (player.hasPermission("at.admin.bypass.cooldown")) return 0;
        List<ATRunnable> list = cooldown.get(getKey(command));
        if (list == null) return 0;
        for (ATRunnable runnable : list) {
            if (runnable.uuid.toString().equals(player.getUniqueId().toString())) {
                return (int)
                        Math.ceil(
                                (runnable.startingTime
                                                + runnable.ms * 1000
                                                - System.currentTimeMillis())
                                        / 1000.0);
            }
        }
        return 0;
    }

    public static void addToCooldown(String command, Player player, World toWorld) {
        List<ATRunnable> list = cooldown.get(getKey(command));
        ATPlayer atPlayer = ATPlayer.getPlayer(player);
        list.add(
                new ATRunnable(
                        player.getUniqueId(), atPlayer.getCooldown(command, toWorld), command));
        cooldown.put(getKey(command), list);
    }

    private static String getKey(String command) {
        return MainConfig.get().APPLY_COOLDOWN_TO_ALL_COMMANDS.get() ? "all" : command;
    }

    public static void init() {
        cooldown.clear();
        if (MainConfig.get().APPLY_COOLDOWN_TO_ALL_COMMANDS.get()) {
            cooldown.put("all", new ArrayList<>());
        } else {
            for (String command :
                    Arrays.asList("tpa", "tpahere", "tpr", "warp", "spawn", "home", "back")) {
                cooldown.put(command, new ArrayList<>());
            }
        }
    }

    public static class ATRunnable implements Runnable {
        private final UUID uuid;
        private final long startingTime;
        private final String command;
        private long ms;

        public ATRunnable(UUID uuid, long waitingTime, String command) {
            this.uuid = uuid;
            ms = waitingTime;
            if (MainConfig.get().ADD_COOLDOWN_DURATION_TO_WARM_UP.get()
                    && !Bukkit.getPlayer(uuid).hasPermission("at.admin.bypass.timer")) {
                ms += MainConfig.get().WARM_UPS.valueOf(command).get();
            }
            this.command = getKey(command);
            startingTime = System.currentTimeMillis();
            RunnableManager.setupRunnerDelayed(t -> this.run(), ms * 20);
        }

        @Override
        public void run() {
            List<ATRunnable> list = cooldown.get(command);
            list.remove(this);
        }
    }
}
