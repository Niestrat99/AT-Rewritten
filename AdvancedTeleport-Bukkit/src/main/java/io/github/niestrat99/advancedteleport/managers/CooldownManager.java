package io.github.niestrat99.advancedteleport.managers;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.api.ATPlayer;
import io.github.niestrat99.advancedteleport.config.MainConfig;

import io.github.niestrat99.advancedteleport.utilities.CommandRunner;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.lang.ref.WeakReference;
import java.util.*;

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
                                                + runnable.duration * 1000
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
                        player, atPlayer.getCooldown(command, toWorld), command));
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

    public static class ATRunnable extends BukkitRunnable {
        private final WeakReference<Player> player;
        private final UUID uuid;
        private final long startingTime;
        private final String command;
        private long duration;

        public ATRunnable(Player player, long waitingTime, String command) {
            this.player = new WeakReference<>(player);
            this.uuid = player.getUniqueId();
            this.duration = waitingTime;
            if (MainConfig.get().ADD_COOLDOWN_DURATION_TO_WARM_UP.get()
                    && !player.hasPermission("at.admin.bypass.timer")) {
                this.duration += MainConfig.get().WARM_UPS.valueOf(command).get();
            }
            this.command = command;
            startingTime = System.currentTimeMillis();
            runTaskLater(CoreClass.getInstance());
        }

        public synchronized BukkitTask runTaskLater(Plugin plugin)
                throws IllegalArgumentException, IllegalStateException {
            return super.runTaskLater(plugin, duration * 20);
        }

        @Override
        public void run() {
            List<ATRunnable> list = cooldown.get(getKey(command));
            list.remove(this);

            Player player = this.player.get();
            if (player == null) return;
            CommandRunner.runCommandsOnCooldownExpire(
                    this.command, player, new CommandRunner.Placeholder("duration", this.duration));
        }
    }
}
