package io.papermc.paper.command.subcommands;

import io.papermc.paper.command.PaperSubcommand;
import java.util.ArrayDeque;
import java.util.Deque;
import io.papermc.paper.util.MCUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ThreadedLevelLightEngine;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.BLUE;
import static net.kyori.adventure.text.format.NamedTextColor.DARK_AQUA;
import static net.kyori.adventure.text.format.NamedTextColor.GREEN;
import static net.kyori.adventure.text.format.NamedTextColor.RED;

@DefaultQualifier(NonNull.class)
public final class FixLightCommand implements PaperSubcommand {
    @Override
    public boolean execute(final CommandSender sender, final String subCommand, final String[] args) {
        this.doFixLight(sender, args);
        return true;
    }

    private void doFixLight(final CommandSender sender, final String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(text("Only players can use this command", RED));
            return;
        }
        @Nullable Runnable post = null;
        int radius = 2;
        if (args.length > 0) {
            try {
                final int parsed = Integer.parseInt(args[0]);
                if (parsed < 0) {
                    sender.sendMessage(text("Radius cannot be negative!", RED));
                    return;
                }
                final int maxRadius = 32;
                radius = Math.min(maxRadius, parsed);
                if (radius != parsed) {
                    post = () -> sender.sendMessage(text("Radius '" + parsed + "' was not in the required range [0, " + maxRadius + "], it was lowered to the maximum (" + maxRadius + " chunks).", RED));
                }
            } catch (final Exception e) {
                sender.sendMessage(text("'" + args[0] + "' is not a valid number.", RED));
                return;
            }
        }

        CraftPlayer player = (CraftPlayer) sender;
        ServerPlayer handle = player.getHandle();
        ServerLevel world = (ServerLevel) handle.level;
        ThreadedLevelLightEngine lightengine = world.getChunkSource().getLightEngine();
        this.starlightFixLight(handle, world, lightengine, radius, post);
    }

    private void starlightFixLight(
        final ServerPlayer sender,
        final ServerLevel world,
        final ThreadedLevelLightEngine lightengine,
        final int radius,
        final @Nullable Runnable done
    ) {
        final long start = System.nanoTime();
        final java.util.LinkedHashSet<ChunkPos> chunks = new java.util.LinkedHashSet<>(MCUtil.getSpiralOutChunks(sender.blockPosition(), radius)); // getChunkCoordinates is actually just bad mappings, this function rets position as blockpos

        final int[] pending = new int[1];
        for (java.util.Iterator<ChunkPos> iterator = chunks.iterator(); iterator.hasNext(); ) {
            final ChunkPos chunkPos = iterator.next();

            final @Nullable ChunkAccess chunk = (ChunkAccess) world.getChunkSource().getChunkForLighting(chunkPos.x, chunkPos.z);
            if (chunk == null || !chunk.isLightCorrect() || !chunk.getStatus().isOrAfter(net.minecraft.world.level.chunk.ChunkStatus.LIGHT)) {
                // cannot relight this chunk
                iterator.remove();
                continue;
            }

            ++pending[0];
        }

        final int[] relitChunks = new int[1];
        lightengine.relight(chunks,
            (final ChunkPos chunkPos) -> {
                ++relitChunks[0];
                sender.getBukkitEntity().sendMessage(text().color(DARK_AQUA).append(
                    text("Relit chunk ", BLUE), text(chunkPos.toString()),
                    text(", progress: ", BLUE), text((int) (Math.round(100.0 * (double) (relitChunks[0]) / (double) pending[0])) + "%")
                ));
            },
            (final int totalRelit) -> {
                final long end = System.nanoTime();
                final long diff = Math.round(1.0e-6 * (end - start));
                sender.getBukkitEntity().sendMessage(text().color(DARK_AQUA).append(
                    text("Relit ", BLUE), text(totalRelit),
                    text(" chunks. Took ", BLUE), text(diff + "ms")
                ));
                if (done != null) {
                    done.run();
                }
            }
        );
        sender.getBukkitEntity().sendMessage(text().color(BLUE).append(text("Relighting "), text(pending[0], DARK_AQUA), text(" chunks")));
    }
}
