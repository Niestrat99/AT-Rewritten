package io.github.niestrat99.advancedteleport.api;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.geysermc.cumulus.CustomForm;
import org.geysermc.cumulus.SimpleForm;
import org.geysermc.floodgate.api.FloodgateApi;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ATFloodgatePlayer extends ATPlayer {

    private UUID floodgateUuid;

    public ATFloodgatePlayer(Player player) {
        super(player);
        floodgateUuid = FloodgateApi.getInstance().getPlayer(player.getUniqueId()).getCorrectUniqueId();
    }

    public void sendTPAForm() {
        List<String> players = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player == getPlayer()) continue;
            if (!getPlayer().canSee(player)) continue;
            ATPlayer atPlayer = ATPlayer.getPlayer(player);
            if (atPlayer.hasBlocked(getPlayer()) || hasBlocked(player)) continue;

            players.add(player.getName());
        }

        CustomForm form = CustomForm.builder()
                .title("TPA Request")
                .dropdown("Select a player to TPA to.", players.toArray(new String[0]))
                .build();

        FloodgateApi.getInstance().sendForm(floodgateUuid, form);
    }

    public void sendRequestFormTPA(Player sender) {
        SimpleForm form = SimpleForm.builder()
                .title("TPA Request")
                .content("The player " + sender.getDisplayName() + " wants to teleport to you!")
                .button("Accept")
                .button("Deny")
                .build();

        FloodgateApi.getInstance().sendForm(floodgateUuid, form);
    }

    public void sendRequestFormTPAHere(Player sender) {
        SimpleForm form = SimpleForm.builder()
                .title("TPA Request")
                .content("The player " + sender.getDisplayName() + " wants you to teleport to them!")
                .button("Accept")
                .button("Deny")
                .build();

        FloodgateApi.getInstance().sendForm(floodgateUuid, form);
    }

    @Nullable
    @Override
    public Player getPlayer() {
        return Bukkit.getPlayer(floodgateUuid);
    }
}
