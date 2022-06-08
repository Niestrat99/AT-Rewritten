package org.bukkit.craftbukkit.v1_19_R1.util;

import java.util.HashSet;
import java.util.List;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class LazyPlayerSet extends LazyHashSet<Player> {

    private final MinecraftServer server;

    public LazyPlayerSet(MinecraftServer server) {
        this.server = server;
    }

    @Override
    protected HashSet<Player> makeReference() { // Paper - protected
        if (reference != null) {
            throw new IllegalStateException("Reference already created!");
        }
        List<ServerPlayer> players = this.server.getPlayerList().players;
        // Paper start
        return makePlayerSet(this.server);
    }
    public static HashSet<Player> makePlayerSet(final MinecraftServer server) {
        // Paper end
        List<ServerPlayer> players = server.getPlayerList().players;
        HashSet<Player> reference = new HashSet<Player>(players.size());
        for (ServerPlayer player : players) {
            reference.add(player.getBukkitEntity());
        }
        return reference;
    }
}
