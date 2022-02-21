package io.github.niestrat99.advancedteleport.api;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.geysermc.cumulus.CustomForm;
import org.geysermc.cumulus.SimpleForm;
import org.geysermc.cumulus.response.CustomFormResponse;
import org.geysermc.cumulus.response.SimpleFormResponse;
import org.geysermc.floodgate.api.FloodgateApi;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ATFloodgatePlayer extends ATPlayer {

    private final UUID floodgateUuid;

    public ATFloodgatePlayer(Player player) {
        super(player);
        floodgateUuid = FloodgateApi.getInstance().getPlayer(player.getUniqueId()).getCorrectUniqueId();
    }

    public void sendTPAForm(boolean here) {
        if (here) {
            sendDropdownForm("tpahere", getVisiblePlayerNames());
        } else {
            sendDropdownForm("tpa", getVisiblePlayerNames());
        }
    }

    public void sendRequestFormTPA(Player sender) {
        SimpleForm form = SimpleForm.builder()
                .title(CustomMessages.getStringRaw("Forms.tpa-received-title"))
                .content(CustomMessages.getString("Forms.tpa-received-description", "{player}", sender.getDisplayName()))
                .button(CustomMessages.getStringRaw("Forms.tpa-received-accept"))
                .button(CustomMessages.getStringRaw("Forms.tpa-received-deny"))
                .build();

        form.setResponseHandler(responseData -> {
            SimpleFormResponse response = form.parseResponse(responseData);
            if (getPlayer() == null) {
                CoreClass.getInstance().getLogger().warning("This player with the UUID " + uuid.toString() + " is null, WHY?");
                return;
            }

            getPlayer().performCommand(response.getClickedButtonId() == 0 ? "advancedteleport:tpayes " + sender.getName()
                    : "advancedteleport:tpano " + sender.getName());
        });

        FloodgateApi.getInstance().sendForm(floodgateUuid, form);
    }

    public void sendRequestFormTPAHere(Player sender) {
        SimpleForm form = SimpleForm.builder()
                .title(CustomMessages.getStringRaw("Forms.tpahere-received-title"))
                .content(CustomMessages.getString("Forms.tpahere-received-description", "{player}", sender.getDisplayName()))
                .button(CustomMessages.getStringRaw("Forms.tpahere-received-accept"))
                .button(CustomMessages.getStringRaw("Forms.tpahere-received-deny"))
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

    public void sendHomeForm() {
        sendDropdownForm("home", getHomes().keySet());
    }

    public void sendSetHomeForm() {
        sendInputForm("sethome");
    }

    public void sendDeleteHomeForm() {
        sendDropdownForm("delhome", getHomes().keySet());
    }

    public void sendSetMainHomeForm() {
        sendInputForm("setmainhome");
    }

    public void sendMoveHomeForm() {
        sendDropdownForm("movehome", getHomes().keySet());
    }

    public void sendWarpForm() {
        sendDropdownForm("warp", AdvancedTeleportAPI.getWarps().keySet());
    }

    public void sendDeleteWarpForm() {
        sendDropdownForm("delwarp", AdvancedTeleportAPI.getWarps().keySet());
    }

    public void sendSetWarpForm() {
        sendInputForm("setwarp");
    }

    public void sendMoveWarpForm() {
        sendDropdownForm("movewarp", AdvancedTeleportAPI.getWarps().keySet());
    }

    public void sendBlockForm() {
        sendDropdownForm("tpblock", getVisiblePlayerNames());
    }

    public void sendUnblockForm() {
        sendDropdownForm("tpunblock", getVisiblePlayerNames());
    }

    public void sendCancelForm() {
        List<TeleportRequest> requests = TeleportRequest.getRequestsByRequester(getPlayer());
        List<String> responders = new ArrayList<>();
        for (TeleportRequest request : requests) {
            responders.add(request.getResponder().getName());
        }
        sendDropdownForm("tpcancel", responders);
    }

    public void sendTpoForm() {
        sendDropdownForm("tpo", getVisiblePlayerNames());
    }

    public void sendTpoHereForm() {
        sendDropdownForm("tpohere", getVisiblePlayerNames());
    }

    private void sendInputForm(String command) {
        CustomForm form = CustomForm.builder()
                .title(CustomMessages.getStringRaw("Forms." + command + "-title"))
                .input(CustomMessages.getStringRaw("Forms." + command + "-description"))
                .build();

        form.setResponseHandler(responseData -> {
            CustomFormResponse response = form.parseResponse(responseData);
            if (getPlayer() == null) {
                CoreClass.getInstance().getLogger().warning("This player with the UUID " + uuid.toString() + " is null, WHY?");
                return;
            }

            String name = response.getInput(0);
            getPlayer().performCommand("advancedteleport:" + command + " " + name);
        });

        FloodgateApi.getInstance().sendForm(floodgateUuid, form);
    }

    private void sendDropdownForm(String command, Collection<String> inputs) {
        String[] items = new String[inputs.size()];
        int index = 0;
        for (String input : inputs) {
            items[index++] = input;
        }

        CustomForm form = CustomForm.builder()
                .title(CustomMessages.getStringRaw("Forms." + command + "-title"))
                .dropdown(CustomMessages.getStringRaw("Forms." + command + "-description"), items)
                .build();

        form.setResponseHandler(responseData -> {
            CustomFormResponse response = form.parseResponse(responseData);
            if (getPlayer() == null) {
                CoreClass.getInstance().getLogger().warning("This player with the UUID " + uuid.toString() + " is null, WHY?");
                return;
            }

            int i = response.getDropdown(0);
            String result = items[i];

            getPlayer().performCommand("advancedteleport:" + command + " " + result);
        });
    }

    private List<String> getVisiblePlayerNames() {
        List<String> players = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player == getPlayer()) continue;
            if (!getPlayer().canSee(player)) continue;
            ATPlayer atPlayer = ATPlayer.getPlayer(player);
            if (atPlayer.hasBlocked(getPlayer()) || hasBlocked(player)) continue;

            players.add(player.getName());
        }
        return players;
    }

    @Nullable
    @Override
    public Player getPlayer() {
        return Bukkit.getPlayer(floodgateUuid);
    }
}
