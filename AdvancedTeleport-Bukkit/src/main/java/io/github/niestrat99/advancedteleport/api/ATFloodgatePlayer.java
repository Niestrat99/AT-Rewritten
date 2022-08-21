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

import java.util.*;

public class ATFloodgatePlayer extends ATPlayer {

    private final UUID floodgateUuid;

    public ATFloodgatePlayer(Player player) {
        super(player);
        floodgateUuid = FloodgateApi.getInstance().getPlayer(player.getUniqueId()).getCorrectUniqueId();
    }

    public void sendTPAForm(boolean here) {
        List<String> players = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player == getPlayer()) continue;
            if (!getPlayer().canSee(player)) continue;
            ATPlayer atPlayer = ATPlayer.getPlayer(player);
            if (atPlayer.hasBlocked(getPlayer()) || hasBlocked(player)) continue;

            players.add(player.getName());
        }

        if (here) {
            sendDropdownForm("tpahere", "TPAHere Request", "Select a player to send a TPAHere request to.", players);
        } else {
            sendDropdownForm("tpa", "TPA Request", "Select a player to send a TPA request to.", players);
        }
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

    public void sendHomeForm() {
        sendDropdownForm("home", "Homes", "Select a home to teleport to.", getHomes().keySet());
    }

    public void sendSetHomeForm() {
        sendInputForm("sethome", "Set Home", "Enter a home name.");
    }

    public void sendDeleteHomeForm() {
        sendDropdownForm("delhome", "Delete Home", "Select the home to delete.", getHomes().keySet());
    }

    public void sendSetMainHomeForm() {
        sendInputForm("setmainhome", "Set Main Home", "Enter an existing home name or a new one.");
    }

    public void sendMoveHomeForm() {
        sendDropdownForm("movehome", "Move Home", "Choose the home to be moved.", getHomes().keySet());
    }

    public void sendWarpForm() {
        sendDropdownForm("warp", "Warps", "Select a warp to teleport to.", Warp.getWarps().keySet());
    }

    public void sendDeleteWarpForm() {
        sendDropdownForm("delwarp", "Delete Warp", "Select a warp to delete.", Warp.getWarps().keySet());
    }

    public void sendSetWarpForm() {
        sendInputForm("setwarp", "Set Warp", "Enter a warp name.");
    }

    public void sendMoveWarpForm() {
        sendDropdownForm("movewarp", "Move Warp", "Select a warp to move.", Warp.getWarps().keySet());
    }

    private void sendInputForm(String command, String title, String prompt) {
        CustomForm form = CustomForm.builder().title(title).input(prompt).build();

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

    private void sendDropdownForm(String command, String title, String prompt, Collection<String> inputs) {
        String[] items = new String[inputs.size()];
        int index = 0;
        for (String input : inputs) {
            items[index++] = input;
        }

        CustomForm form = CustomForm.builder().title(title).dropdown(prompt, items).build();

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

        FloodgateApi.getInstance().sendForm(floodgateUuid, form);
    }

    @Nullable
    @Override
    public Player getPlayer() {
        return Bukkit.getPlayer(floodgateUuid);
    }
}
