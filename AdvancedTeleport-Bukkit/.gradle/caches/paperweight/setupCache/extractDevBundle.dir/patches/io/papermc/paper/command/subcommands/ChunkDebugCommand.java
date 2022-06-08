package io.papermc.paper.command.subcommands;

import io.papermc.paper.command.CommandUtil;
import io.papermc.paper.command.PaperSubcommand;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import io.papermc.paper.util.MCUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.ProtoChunk;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.BLUE;
import static net.kyori.adventure.text.format.NamedTextColor.DARK_AQUA;
import static net.kyori.adventure.text.format.NamedTextColor.GREEN;
import static net.kyori.adventure.text.format.NamedTextColor.RED;

@DefaultQualifier(NonNull.class)
public final class ChunkDebugCommand implements PaperSubcommand {
    @Override
    public boolean execute(final CommandSender sender, final String subCommand, final String[] args) {
        switch (subCommand) {
            case "debug" -> this.doDebug(sender, args);
            case "chunkinfo" -> this.doChunkInfo(sender, args);
            case "holderinfo" -> this.doHolderInfo(sender, args);
        }
        return true;
    }

    @Override
    public List<String> tabComplete(final CommandSender sender, final String subCommand, final String[] args) {
        switch (subCommand) {
            case "debug" -> {
                if (args.length == 1) {
                    return CommandUtil.getListMatchingLast(sender, args, "help", "chunks");
                }
            }
            case "holderinfo" -> {
                List<String> worldNames = new ArrayList<>();
                worldNames.add("*");
                for (org.bukkit.World world : Bukkit.getWorlds()) {
                    worldNames.add(world.getName());
                }
                if (args.length == 1) {
                    return CommandUtil.getListMatchingLast(sender, args, worldNames);
                }
            }
            case "chunkinfo" -> {
                List<String> worldNames = new ArrayList<>();
                worldNames.add("*");
                for (org.bukkit.World world : Bukkit.getWorlds()) {
                    worldNames.add(world.getName());
                }
                if (args.length == 1) {
                    return CommandUtil.getListMatchingLast(sender, args, worldNames);
                }
            }
        }
        return Collections.emptyList();
    }

    private void doChunkInfo(final CommandSender sender, final String[] args) {
        List<org.bukkit.World> worlds;
        if (args.length < 1 || args[0].equals("*")) {
            worlds = Bukkit.getWorlds();
        } else {
            worlds = new ArrayList<>(args.length);
            for (final String arg : args) {
                org.bukkit.@Nullable World world = Bukkit.getWorld(arg);
                if (world == null) {
                    sender.sendMessage(text("World '" + arg + "' is invalid", RED));
                    return;
                }
                worlds.add(world);
            }
        }

        int accumulatedTotal = 0;
        int accumulatedInactive = 0;
        int accumulatedBorder = 0;
        int accumulatedTicking = 0;
        int accumulatedEntityTicking = 0;

        for (final org.bukkit.World bukkitWorld : worlds) {
            final ServerLevel world = ((CraftWorld) bukkitWorld).getHandle();

            int total = 0;
            int inactive = 0;
            int border = 0;
            int ticking = 0;
            int entityTicking = 0;

            for (final ChunkHolder chunk : io.papermc.paper.chunk.system.ChunkSystem.getVisibleChunkHolders(world)) {
                if (chunk.getFullChunkNowUnchecked() == null) {
                    continue;
                }

                ++total;

                ChunkHolder.FullChunkStatus state = chunk.getFullStatus();

                switch (state) {
                    case INACCESSIBLE -> ++inactive;
                    case BORDER -> ++border;
                    case TICKING -> ++ticking;
                    case ENTITY_TICKING -> ++entityTicking;
                }
            }

            accumulatedTotal += total;
            accumulatedInactive += inactive;
            accumulatedBorder += border;
            accumulatedTicking += ticking;
            accumulatedEntityTicking += entityTicking;

            sender.sendMessage(text().append(text("Chunks in ", BLUE), text(bukkitWorld.getName(), GREEN), text(":")));
            sender.sendMessage(text().color(DARK_AQUA).append(
                text("Total: ", BLUE), text(total),
                text(" Inactive: ", BLUE), text(inactive),
                text(" Border: ", BLUE), text(border),
                text(" Ticking: ", BLUE), text(ticking),
                text(" Entity: ", BLUE), text(entityTicking)
            ));
        }
        if (worlds.size() > 1) {
            sender.sendMessage(text().append(text("Chunks in ", BLUE), text("all listed worlds", GREEN), text(":", DARK_AQUA)));
            sender.sendMessage(text().color(DARK_AQUA).append(
                text("Total: ", BLUE), text(accumulatedTotal),
                text(" Inactive: ", BLUE), text(accumulatedInactive),
                text(" Border: ", BLUE), text(accumulatedBorder),
                text(" Ticking: ", BLUE), text(accumulatedTicking),
                text(" Entity: ", BLUE), text(accumulatedEntityTicking)
            ));
        }
    }

    private void doHolderInfo(final CommandSender sender, final String[] args) {
        List<org.bukkit.World> worlds;
        if (args.length < 1 || args[0].equals("*")) {
            worlds = Bukkit.getWorlds();
        } else {
            worlds = new ArrayList<>(args.length);
            for (final String arg : args) {
                org.bukkit.@Nullable World world = Bukkit.getWorld(arg);
                if (world == null) {
                    sender.sendMessage(text("World '" + arg + "' is invalid", RED));
                    return;
                }
                worlds.add(world);
            }
        }

        int accumulatedTotal = 0;
        int accumulatedCanUnload = 0;
        int accumulatedNull = 0;
        int accumulatedReadOnly = 0;
        int accumulatedProtoChunk = 0;
        int accumulatedFullChunk = 0;

        for (final org.bukkit.World bukkitWorld : worlds) {
            final ServerLevel world = ((CraftWorld) bukkitWorld).getHandle();

            int total = 0;
            int canUnload = 0;
            int nullChunks = 0;
            int readOnly = 0;
            int protoChunk = 0;
            int fullChunk = 0;

            for (final ChunkHolder chunk : world.chunkTaskScheduler.chunkHolderManager.getOldChunkHolders()) { // Paper - change updating chunks map
                final ChunkAccess lastChunk = chunk.getAvailableChunkNow();

                ++total;

                if (lastChunk == null) {
                    ++nullChunks;
                } else if (lastChunk instanceof ImposterProtoChunk) {
                    ++readOnly;
                } else if (lastChunk instanceof ProtoChunk) {
                    ++protoChunk;
                } else if (lastChunk instanceof LevelChunk) {
                    ++fullChunk;
                }

                if (chunk.newChunkHolder.isSafeToUnload() == null) {
                    ++canUnload;
                }
            }

            accumulatedTotal += total;
            accumulatedCanUnload += canUnload;
            accumulatedNull += nullChunks;
            accumulatedReadOnly += readOnly;
            accumulatedProtoChunk += protoChunk;
            accumulatedFullChunk += fullChunk;

            sender.sendMessage(text().append(text("Chunks in ", BLUE), text(bukkitWorld.getName(), GREEN), text(":")));
            sender.sendMessage(text().color(DARK_AQUA).append(
                text("Total: ", BLUE), text(total),
                text(" Unloadable: ", BLUE), text(canUnload),
                text(" Null: ", BLUE), text(nullChunks),
                text(" ReadOnly: ", BLUE), text(readOnly),
                text(" Proto: ", BLUE), text(protoChunk),
                text(" Full: ", BLUE), text(fullChunk)
            ));
        }
        if (worlds.size() > 1) {
            sender.sendMessage(text().append(text("Chunks in ", BLUE), text("all listed worlds", GREEN), text(":", DARK_AQUA)));
            sender.sendMessage(text().color(DARK_AQUA).append(
                text("Total: ", BLUE), text(accumulatedTotal),
                text(" Unloadable: ", BLUE), text(accumulatedCanUnload),
                text(" Null: ", BLUE), text(accumulatedNull),
                text(" ReadOnly: ", BLUE), text(accumulatedReadOnly),
                text(" Proto: ", BLUE), text(accumulatedProtoChunk),
                text(" Full: ", BLUE), text(accumulatedFullChunk)
            ));
        }
    }

    private void doDebug(final CommandSender sender, final String[] args) {
        if (args.length < 1) {
            sender.sendMessage(text("Use /paper debug [chunks] help for more information on a specific command", RED));
            return;
        }

        final String debugType = args[0].toLowerCase(Locale.ENGLISH);
        switch (debugType) {
            case "chunks" -> {
                if (args.length >= 2 && args[1].toLowerCase(Locale.ENGLISH).equals("help")) {
                    sender.sendMessage(text("Use /paper debug chunks [world] to dump loaded chunk information to a file", RED));
                    break;
                }
                File file = new File(new File(new File("."), "debug"),
                    "chunks-" + DateTimeFormatter.ofPattern("yyyy-MM-dd_HH.mm.ss").format(LocalDateTime.now()) + ".txt");
                sender.sendMessage(text("Writing chunk information dump to " + file, GREEN));
                try {
                    MCUtil.dumpChunks(file, false);
                    sender.sendMessage(text("Successfully written chunk information!", GREEN));
                } catch (Throwable thr) {
                    MinecraftServer.LOGGER.warn("Failed to dump chunk information to file " + file.toString(), thr);
                    sender.sendMessage(text("Failed to dump chunk information, see console", RED));
                }
            }
            // "help" & default
            default -> sender.sendMessage(text("Use /paper debug [chunks] help for more information on a specific command", RED));
        }
    }

}
