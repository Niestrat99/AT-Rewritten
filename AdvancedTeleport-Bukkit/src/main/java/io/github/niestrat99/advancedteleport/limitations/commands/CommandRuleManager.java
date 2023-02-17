package io.github.niestrat99.advancedteleport.limitations.commands;

import io.github.niestrat99.advancedteleport.config.MainConfig;
import io.github.niestrat99.advancedteleport.limitations.commands.list.IgnoreRule;
import io.github.niestrat99.advancedteleport.limitations.commands.list.OverrideRule;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public final class CommandRuleManager {

    private final HashMap<String, List<CommandRule>> rules;

    public CommandRuleManager() {
        rules = new HashMap<>();
        for (String command : Arrays.asList("tpa", "tpahere", "tpr", "warp", "spawn", "home", "back")) {
            addCommand(command, MainConfig.get().COMMAND_RULES.valueOf(command).get());
        }
    }

    private void addCommand(
        @NotNull final String command,
        @NotNull final String rulesRaw
    ) {
        String[] rules = rulesRaw.split(";");
        List<CommandRule> ruleList = new ArrayList<>();
        for (String rule : rules) {
            if (rule.startsWith("override")) {
                ruleList.add(new OverrideRule(rule.replaceFirst("override", "")));
            } else if (rule.startsWith("ignore")) {
                ruleList.add(new IgnoreRule(rule.replaceFirst("ignore", "")));
            }
        }
        this.rules.put(command, ruleList);
    }

    public int canTeleport(
        @NotNull final Player player,
        @NotNull final Location toLoc,
        @NotNull final String command
    ) {
        final var commandRules = this.rules.get(command);
        if (commandRules == null) return 0;
        for (CommandRule rule : commandRules) {
            if (rule instanceof IgnoreRule) {
                if (!rule.canTeleport(player, toLoc)) return -1;
            } else if (rule instanceof OverrideRule) {
                if (rule.canTeleport(player, toLoc)) return 1;
            }
        }
        return 0;
    }
}
