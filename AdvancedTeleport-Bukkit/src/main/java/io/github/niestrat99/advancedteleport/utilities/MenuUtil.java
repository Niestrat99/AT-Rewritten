package io.github.niestrat99.advancedteleport.utilities;

import io.github.niestrat99.advancedteleport.config.CustomMessages;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public class MenuUtil {

    public static <T> void sendMenu(@NotNull CommandSender sender,
                                    @NotNull String menuKey,
                                    @NotNull PagedLists<T> list,
                                    @NotNull Function<T, Component> convert,
                                    int page,
                                    @NotNull TagResolver... extraHeaderPlaceholders) {

        // Get the part of the list that we need
        List<T> contents = list.getContentsInPage(page);

        // Handle placeholders because Java is struggling
        List<TagResolver> placeholders = new ArrayList<>(Arrays.asList(extraHeaderPlaceholders));
        placeholders.add(Placeholder.unparsed("current_page", String.valueOf(page)));
        placeholders.add(Placeholder.unparsed("total_pages", String.valueOf(list.getTotalPages())));

        // Send the header
        CustomMessages.sendMessage(sender,
                "Menus." + menuKey,
                placeholders.toArray(new TagResolver[0]));

        // Go through each of the contents and send them
        final var audience = CustomMessages.asAudience(sender);
        contents.forEach(part -> audience.sendMessage(convert.apply(part)));
    }
}
