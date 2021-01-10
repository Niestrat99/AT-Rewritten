package io.github.niestrat99.advancedteleport.events;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.config.NewConfig;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class CooldownManager {

    private static HashMap<String, List<ATRunnable>> cooldown = new HashMap<>();

    public static class ATRunnable extends BukkitRunnable {
        private UUID uuid;
        private long ms;
        private long startingTime;
        private String command;

        public ATRunnable(UUID uuid, long waitingTime, String command) {
            this.uuid = uuid;
            ms = waitingTime;
            if (NewConfig.getInstance().ADD_COOLDOWN_DURATION_TO_WARM_UP.get() && !Bukkit.getPlayer(uuid).hasPermission("at.admin.bypass.timer")) {
                ms += NewConfig.getInstance().WARM_UPS.valueOf(command).get();
            }
            this.command = getKey(command);
            startingTime = System.currentTimeMillis();
            runTaskLater(CoreClass.getInstance());
        }

        public synchronized BukkitTask runTaskLater(Plugin plugin) throws IllegalArgumentException, IllegalStateException {
            return super.runTaskLater(plugin, ms * 20);
        }

        @Override
        public void run() {
            List<ATRunnable> list = cooldown.get(command);
            list.remove(this);
        }
    }

    public static int secondsLeftOnCooldown(String command, Player player) {
        if (player.hasPermission("at.admin.bypass.cooldown")) return 0;
        List<ATRunnable> list = cooldown.get(getKey(command));
        for (ATRunnable runnable : list) {
            if (runnable.uuid == player.getUniqueId()) {
                return (int) Math.ceil((runnable.startingTime + runnable.ms * 1000 - System.currentTimeMillis()) / 1000);
            }
        }
        return 0;
    }

    public static void addToCooldown(String command, Player player) {
        List<ATRunnable> list = cooldown.get(getKey(command));
        list.add(new ATRunnable(player.getUniqueId(), NewConfig.getInstance().COOLDOWNS.valueOf(command).get(), command));
        cooldown.put(getKey(command), list);
    }

    private static String getKey(String command) {
        return NewConfig.getInstance().APPLY_COOLDOWN_TO_ALL_COMMANDS.get() ? "all" : command;
    }

    public static void init() {
        cooldown.clear();
        if (NewConfig.getInstance().APPLY_COOLDOWN_TO_ALL_COMMANDS.get()) {
            cooldown.put("all", new ArrayList<>());
        } else {
            for (String command : Arrays.asList("tpa", "tpahere", "tpr", "warp", "spawn", "home", "back")) {
                cooldown.put(command, new ArrayList<>());
            }
        }
    }
}
