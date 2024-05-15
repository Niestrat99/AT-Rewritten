package io.github.niestrat99.advancedteleport.commands.warp;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.api.AdvancedTeleportAPI;
import io.github.niestrat99.advancedteleport.commands.ATCommand;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.GUIConfig;
import io.github.niestrat99.advancedteleport.config.MainConfig;
import io.github.niestrat99.advancedteleport.extensions.ExPermission;
import io.github.niestrat99.advancedteleport.utilities.IconMenu;
import io.github.thatsmusic99.configurationmaster.api.ConfigSection;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public final class WarpsCommand extends ATCommand {

    @Override
    public boolean onCommand(
            @NotNull final CommandSender sender,
            @NotNull final Command command,
            @NotNull final String s,
            @NotNull final String[] args) {
        if (!canProceed(sender)) return true;
        sendWarps(sender);
        return true;
    }

    public static void sendWarps(CommandSender sender) {
        if (GUIConfig.isUsingWarpsGUIMenu()) {
            ConfigSection warps = GUIConfig.getWarpsMenu();
            int minPage = 999;
            int maxPage = 0;
            for (String warp : warps.getKeys(false)) {
                int page = warps.getInteger(warp + ".page");
                if (page < minPage) {
                    minPage = page;
                }
                if (page > maxPage) {
                    maxPage = page;
                }
            }
            int pages = maxPage - minPage + 1;
            IconMenu menu =
                    new IconMenu(
                            CustomMessages.asString("Info.warps"),
                            GUIConfig.getWarpsMenuSlots(),
                            pages,
                            CoreClass.getInstance());

            for (String warpName : warps.getKeys(false)) {
                ConfigSection warp = warps.getConfigSection(warpName);
                if (sender.hasPermission("at.member.warp.*")
                        || sender.hasPermission("at.member.warp." + warpName)
                        || !warp.getBoolean("hideIfNoPermission")) {

                    menu.setIcon(
                            warp.getInteger("page"),
                            warp.getInteger("slot"),
                            new IconMenu.Icon(
                                            new ItemStack(
                                                    Material.valueOf(warp.getString("item")),
                                                    1,
                                                    (byte) warp.getInteger("data-value")))
                                    .withNameAndLore(
                                            warp.getString("name"), warp.getStringList("tooltip"))
                                    .withCommands("warp " + warpName)
                                    .withTexture("texture"));
                }
            }
            // Next page icons will override warps
            for (int i = 0; i < pages; i++) {
                if (i != 0) {
                    ConfigSection lastPage = GUIConfig.getLastPageIcon();
                    menu.setIcon(
                            i,
                            lastPage.getInteger("slot"),
                            new IconMenu.Icon(
                                            new ItemStack(
                                                    Material.valueOf(lastPage.getString("item")),
                                                    1,
                                                    (byte) lastPage.getInteger("data-value")))
                                    .withTexture(lastPage.getString("texture"))
                                    .withNameAndLore(
                                            lastPage.getString("name"),
                                            lastPage.getStringList("tooltip"))
                                    .withHandler(
                                            handler -> {
                                                handler.setWillClose(false);
                                                handler.setWillDestroy(false);
                                                menu.openPreviousPage();
                                            }));
                }
                if (i != pages - 1) {
                    ConfigSection nextPage = GUIConfig.getNextPageIcon();
                    menu.setIcon(
                            i,
                            nextPage.getInteger("slot"),
                            new IconMenu.Icon(
                                            new ItemStack(
                                                    Material.valueOf(nextPage.getString("item")),
                                                    1,
                                                    (byte) nextPage.getInteger("data-value")))
                                    .withTexture(nextPage.getString("texture"))
                                    .withNameAndLore(
                                            nextPage.getString("name"),
                                            nextPage.getStringList("tooltip"))
                                    .withHandler(
                                            handler -> {
                                                handler.setWillClose(false);
                                                handler.setWillDestroy(false);
                                                menu.openNextPage();
                                            }));
                }
            }
            menu.open((Player) sender);

        } else {
            final TextComponent body =
                    (TextComponent)
                            Component.join(
                                    JoinConfiguration.commas(true),
                                    AdvancedTeleportAPI.getWarps().values().stream()
                                            .filter(
                                                    warp ->
                                                            ExPermission.hasPermissionOrStar(
                                                                    sender,
                                                                    "at.member.warp."
                                                                            + warp.getName()
                                                                                    .toLowerCase()))
                                            .map(
                                                    warp ->
                                                            Component.text(warp.getName())
                                                                    .clickEvent(
                                                                            ClickEvent.runCommand(
                                                                                    "/advancedteleport:warp "
                                                                                            + warp
                                                                                                    .getName()))
                                                                    .hoverEvent(
                                                                            CustomMessages
                                                                                    .locationBasedTooltip(
                                                                                            sender,
                                                                                            warp,
                                                                                            "warps")))
                                            .toList());

            if (!body.content().isEmpty() || !body.children().isEmpty()) {
                String text = CustomMessages.config.getString("Info.warps") + "<warps>";
                CustomMessages.sendMessage(sender, CustomMessages.translate(text, Placeholder.component("warps", body)));
            } else CustomMessages.sendMessage(sender, "Error.noWarps");
        }
    }

    @Override
    public boolean getRequiredFeature() {
        return MainConfig.get().USE_WARPS.get();
    }

    @Override
    public @NotNull String getPermission() {
        return "at.member.warps";
    }
}
