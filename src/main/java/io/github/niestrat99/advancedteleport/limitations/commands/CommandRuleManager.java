package io.github.niestrat99.advancedteleport.limitations.commands;

import io.github.niestrat99.advancedteleport.config.NewConfig;
import io.github.niestrat99.advancedteleport.limitations.commands.list.IgnoreRule;
import io.github.niestrat99.advancedteleport.limitations.commands.list.OverrideRule;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class CommandRuleManager {

    private HashMap<String, List<CommandRule>> rules;

    public CommandRuleManager() {
        rules = new HashMap<>();
        for (String command : Arrays.asList("tpa", "tpahere", "tpr", "warp", "spawn", "home", "back")) {
            addCommand(command, NewConfig.getInstance().COMMAND_RULES.valueOf(command).get());
        }
    }

    private void addCommand(String command, String rulesRaw) {
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

    public int canTeleport(Player player, Location toLoc, String command) {
        List<CommandRule> rules = this.rules.get(command);
        for (CommandRule rule : rules) {
            if (rule instanceof IgnoreRule) {
                if (!rule.canTeleport(player, toLoc)) return -1;
            } else if (rule instanceof OverrideRule) {
                if (rule.canTeleport(player, toLoc)) return 1;
            }
        }
        return 0;
    }
}
