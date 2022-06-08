package org.bukkit.craftbukkit.v1_19_R1.command;

import com.google.common.base.Joiner;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.tree.CommandNode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.world.entity.vehicle.MinecartCommandBlock;
import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.ProxiedCommandSender;
import org.bukkit.command.RemoteConsoleCommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.craftbukkit.v1_19_R1.CraftServer;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftMinecartCommand;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.entity.minecart.CommandMinecart;

public final class VanillaCommandWrapper extends BukkitCommand {

    private final Commands dispatcher;
    public final CommandNode<CommandSourceStack> vanillaCommand;

    public VanillaCommandWrapper(Commands dispatcher, CommandNode<CommandSourceStack> vanillaCommand) {
        super(vanillaCommand.getName(), "A Mojang provided command.", vanillaCommand.getUsageText(), Collections.EMPTY_LIST);
        this.dispatcher = dispatcher;
        this.vanillaCommand = vanillaCommand;
        this.setPermission(VanillaCommandWrapper.getPermission(vanillaCommand));
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (!testPermission(sender)) return true;

        CommandSourceStack icommandlistener = VanillaCommandWrapper.getListener(sender);
        this.dispatcher.performPrefixedCommand(icommandlistener, this.toDispatcher(args, getName()), this.toDispatcher(args, commandLabel));
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args, Location location) throws IllegalArgumentException {
        Validate.notNull(sender, "Sender cannot be null");
        Validate.notNull(args, "Arguments cannot be null");
        Validate.notNull(alias, "Alias cannot be null");

        CommandSourceStack icommandlistener = VanillaCommandWrapper.getListener(sender);
        ParseResults<CommandSourceStack> parsed = this.dispatcher.getDispatcher().parse(this.toDispatcher(args, getName()), icommandlistener);

        List<String> results = new ArrayList<>();
        this.dispatcher.getDispatcher().getCompletionSuggestions(parsed).thenAccept((suggestions) -> {
            suggestions.getList().forEach((s) -> results.add(s.getText()));
        });

        return results;
    }

    public static CommandSourceStack getListener(CommandSender sender) {
        // Paper start - account for other entity command senders
        if (sender instanceof org.bukkit.craftbukkit.v1_19_R1.entity.CraftEntity craftEntity) {
            return craftEntity.getHandle().createCommandSourceStack();
        // Paper end
        }
        if (sender instanceof BlockCommandSender) {
            return ((CraftBlockCommandSender) sender).getWrapper();
        }
        if (sender instanceof CommandMinecart) {
            return ((MinecartCommandBlock) ((CraftMinecartCommand) sender).getHandle()).getCommandBlock().createCommandSourceStack();
        }
        if (sender instanceof RemoteConsoleCommandSender) {
            return ((DedicatedServer) MinecraftServer.getServer()).rconConsoleSource.createCommandSourceStack();
        }
        if (sender instanceof ConsoleCommandSender) {
            return ((CraftServer) sender.getServer()).getServer().createCommandSourceStack();
        }
        if (sender instanceof ProxiedCommandSender) {
            return ((ProxiedNativeCommandSender) sender).getHandle();
        }
        // Paper start
        if (sender instanceof io.papermc.paper.commands.FeedbackForwardingSender feedback) {
            return feedback.asVanilla();
        }
        // Paper end

        throw new IllegalArgumentException("Cannot make " + sender + " a vanilla command listener");
    }

    public static String getPermission(CommandNode<CommandSourceStack> vanillaCommand) {
        // Paper start
        final String commandName;
        if (vanillaCommand.getRedirect() == null) {
            commandName = vanillaCommand.getName();
        } else {
            commandName = vanillaCommand.getRedirect().getName();
        }
        return "minecraft.command." + stripDefaultNamespace(commandName);
    }

    private static String stripDefaultNamespace(final String maybeNamespaced) {
        final String prefix = "minecraft:";
        if (maybeNamespaced.startsWith(prefix)) {
            return maybeNamespaced.substring(prefix.length());
        }
        return maybeNamespaced;
        // Paper end
    }

    private String toDispatcher(String[] args, String name) {
        return name + ((args.length > 0) ? " " + Joiner.on(' ').join(args) : "");
    }
}
