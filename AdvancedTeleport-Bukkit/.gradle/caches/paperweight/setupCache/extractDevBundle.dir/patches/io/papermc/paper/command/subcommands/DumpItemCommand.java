package io.papermc.paper.command.subcommands;

import io.papermc.paper.adventure.PaperAdventure;
import io.papermc.paper.command.PaperSubcommand;
import java.util.Objects;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_19_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.GRAY;
import static net.kyori.adventure.text.format.NamedTextColor.YELLOW;
import static net.kyori.adventure.text.format.TextDecoration.ITALIC;

@DefaultQualifier(NonNull.class)
public final class DumpItemCommand implements PaperSubcommand {
    @Override
    public boolean execute(final CommandSender sender, final String subCommand, final String[] args) {
        this.doDumpItem(sender);
        return true;
    }

    private void doDumpItem(final CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command");
            return;
        }
        final ItemStack itemStack = CraftItemStack.asNMSCopy(((CraftPlayer) sender).getItemInHand());
        final @Nullable CompoundTag tag = itemStack.getTag();
        final @Nullable Component nbtComponent = tag == null ? null : PaperAdventure.asAdventure(net.minecraft.nbt.NbtUtils.toPrettyComponent(tag));
        final String itemId = Objects.requireNonNull(((CraftWorld) ((CraftPlayer) sender).getWorld()).getHandle().registryAccess()
            .registryOrThrow(Registry.ITEM_REGISTRY).getKey(itemStack.getItem())).toString();
        final Component message = text()
            .append(text(itemId, YELLOW))
            .apply(b -> {
                if (nbtComponent != null) {
                    b.append(nbtComponent);
                }
            })
            .build();
        Bukkit.getConsoleSender().sendMessage(message);
        sender.sendMessage(message);
        sender.sendMessage(text().content("    Click to copy item to clipboard")
            .color(GRAY)
            .decorate(ITALIC)
            .clickEvent(ClickEvent.copyToClipboard(tag == null ? itemId : (itemId + tag))));
    }
}
