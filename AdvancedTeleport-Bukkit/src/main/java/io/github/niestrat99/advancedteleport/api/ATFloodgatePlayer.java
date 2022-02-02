package io.github.niestrat99.advancedteleport.api;

import io.github.niestrat99.advancedteleport.CoreClass;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.geysermc.cumulus.CustomForm;
import org.geysermc.cumulus.SimpleForm;
import org.geysermc.cumulus.response.CustomFormResponse;
import org.geysermc.cumulus.response.SimpleFormResponse;
import org.geysermc.floodgate.api.FloodgateApi;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ATFloodgatePlayer extends ATPlayer {

    private final UUID floodgateUuid;

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

        form.setResponseHandler(responseData -> {
            CustomFormResponse response = form.parseResponse(responseData);
            if (getPlayer() == null) {
                CoreClass.getInstance().getLogger().warning("This player with the UUID " + uuid.toString() + " is null, WHY?");
                return;
            }

            int index = response.getDropdown(0);
            String player = players.get(index);

            getPlayer().performCommand("advancedteleport:tpa " + player);
        });

        FloodgateApi.getInstance().sendForm(floodgateUuid, form);
    }

    public void sendRequestFormTPA(Player sender) {
        SimpleForm form = SimpleForm.builder()
                .title("TPA Request")
                .content("The player " + sender.getDisplayName() + " wants to teleport to you!")
                .button("Accept")
                .button("Deny")
                .build();

        form.setResponseHandler(responseData -> {
            SimpleFormResponse response = form.parseResponse(responseData);
            if (getPlayer() == null) {
                CoreClass.getInstance().getLogger().warning("This player with the UUID " + uuid.toString() + " is null, WHY?");
                return;
            }
            getPlayer().performCommand(response.getClickedButtonId() == 0 ? "advancedteleport:tpyes " + sender.getName()
                    : "advancedteleport:tpno " + sender.getName());
        });

        FloodgateApi.getInstance().sendForm(floodgateUuid, form);
    }

    public void sendRequestFormTPAHere(Player sender) {
        SimpleForm form = SimpleForm.builder()
                .title("TPA Request")
                .content("The player " + sender.getDisplayName() + " wants you to teleport to them!")
                .button("Accept")
                .button("Deny")
                .build();

        form.setResponseHandler(responseData -> {
            SimpleFormResponse response = form.parseResponse(responseData);
            if (getPlayer() == null) {
                CoreClass.getInstance().getLogger().warning("This player with the UUID " + uuid.toString() + " is null, WHY?");
                return;
            }
            getPlayer().performCommand(response.getClickedButtonId() == 0 ? "advancedteleport:tpyes " + sender.getName()
                    : "advancedteleport:tpno " + sender.getName());
        });

        FloodgateApi.getInstance().sendForm(floodgateUuid, form);
    }

    @Nullable
    @Override
    public Player getPlayer() {
        return Bukkit.getPlayer(floodgateUuid);
    }
}
