package io.github.niestrat99.advancedteleport.events;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.config.Config;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

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
            if (Config.isApplyingTimerToCooldown() && !Bukkit.getPlayer(uuid).hasPermission("at.admin.bypass.timer")) {
                ms += Config.getTeleportTimer(command);
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
        list.add(new ATRunnable(player.getUniqueId(), Config.getCooldown(command), command));
        cooldown.put(getKey(command), list);
    }

    private static String getKey(String command) {
        return Config.isCooldownGlobal() ? "all" : command;
    }

    public static void init() {
        cooldown.clear();
        if (Config.isCooldownGlobal()) {
            cooldown.put("all", new ArrayList<>());
        } else {
            for (String command : Config.config.getConfigurationSection("cooldowns").getKeys(false)) {
                switch (command) {
                    case "default":
                    case "apply-globally":
                        continue;
                    default:
                        cooldown.put(command, new ArrayList<>());
                }
            }
        }
    }
}
