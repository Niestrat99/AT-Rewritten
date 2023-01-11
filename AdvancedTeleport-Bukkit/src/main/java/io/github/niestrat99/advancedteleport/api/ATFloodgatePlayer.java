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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Represents a bedrock-connected player.
 */
public class ATFloodgatePlayer extends ATPlayer {

    private final @NotNull UUID floodgateUuid;

    protected ATFloodgatePlayer(Player player) {
        super(player);
        floodgateUuid = FloodgateApi.getInstance().getPlayer(player.getUniqueId()).getCorrectUniqueId();
    }

    /**
     * Sends a Bedrock TPA form.
     *
     * @param here true if the form is for /tpahere, false if it is for /tpa.
     */
    public void sendTPAForm(boolean here) {
        if (here) {
            sendDropdownForm("tpahere", getVisiblePlayerNames());
        } else {
            sendDropdownForm("tpa", getVisiblePlayerNames());
        }
    }

    /**
     * Send the request form for accepting and denying a /tpa request.
     *
     * @param sender the player that requested the teleportation.
     */
    public void sendRequestFormTPA(@NotNull Player sender) {

        // Set up the form
        SimpleForm form = SimpleForm.builder()
                .title(CustomMessages.getStringRaw("Forms.tpa-received-title"))
                .content(CustomMessages.getString("Forms.tpa-received-description", "{player}", sender.getDisplayName()))
                .button(CustomMessages.getStringRaw("Forms.tpa-received-accept"))
                .button(CustomMessages.getStringRaw("Forms.tpa-received-deny"))
                .build();

        form.setResponseHandler(responseData -> {

            // Get the response
            SimpleFormResponse response = form.parseResponse(responseData);
            if (getPlayer() == null) {
                CoreClass.getInstance().getLogger().warning("This player with the UUID " + uuid.toString() + " is null, WHY?");
                return;
            }

            getPlayer().performCommand(response.getClickedButtonId() == 0 ? "advancedteleport:tpayes " + sender.getName()
                    : "advancedteleport:tpano " + sender.getName());
        });

        // Send the form
        FloodgateApi.getInstance().sendForm(floodgateUuid, form);
    }

    /**
     * Send the request form for accepting and denying a /tpahere request.
     *
     * @param sender the player that requested the teleportation.
     */
    public void sendRequestFormTPAHere(@NotNull Player sender) {

        // Set up the form
        SimpleForm form = SimpleForm.builder()
                .title(CustomMessages.getStringRaw("Forms.tpahere-received-title"))
                .content(CustomMessages.getString("Forms.tpahere-received-description", "{player}", sender.getDisplayName()))
                .button(CustomMessages.getStringRaw("Forms.tpahere-received-accept"))
                .button(CustomMessages.getStringRaw("Forms.tpahere-received-deny"))
                .build();

        form.setResponseHandler(responseData -> {

            // Get the response
            SimpleFormResponse response = form.parseResponse(responseData);
            if (getPlayer() == null) {
                CoreClass.getInstance().getLogger().warning("This player with the UUID " + uuid.toString() + " is null, WHY?");
                return;
            }
            getPlayer().performCommand(response.getClickedButtonId() == 0 ? "advancedteleport:tpyes " + sender.getName()
                    : "advancedteleport:tpno " + sender.getName());
        });

        // Send the form
        FloodgateApi.getInstance().sendForm(floodgateUuid, form);
    }

    /**
     * Sends the form for /home.
     */
    public void sendHomeForm() {
        sendDropdownForm("home", getHomes().keySet());
    }

    /**
     * Sends the form for /sethome.
     */
    public void sendSetHomeForm() {
        sendInputForm("sethome");
    }

    /**
     * Sends the form for /delhome.
     */
    public void sendDeleteHomeForm() {
        sendDropdownForm("delhome", getHomes().keySet());
    }

    /**
     * Sends the form for /setmainhome.
     */
    public void sendSetMainHomeForm() {
        sendInputForm("setmainhome");
    }

    /**
     * Sends the form for /movehome.
     */
    public void sendMoveHomeForm() {
        sendDropdownForm("movehome", getHomes().keySet());
    }

    /**
     * Sends the form for /warp.
     */
    public void sendWarpForm() {
        sendDropdownForm("warp", AdvancedTeleportAPI.getWarps().keySet());
    }

    /**
     * Sends the form for /delwarp.
     */
    public void sendDeleteWarpForm() {
        sendDropdownForm("delwarp", AdvancedTeleportAPI.getWarps().keySet());
    }

    /**
     * Sends the form for /setwarp.
     */
    public void sendSetWarpForm() {
        sendInputForm("setwarp");
    }

    /**
     * Sends the form for /movewarp.
     */
    public void sendMoveWarpForm() {
        sendDropdownForm("movewarp", AdvancedTeleportAPI.getWarps().keySet());
    }

    /**
     * Sends the form for /tpblock.
     */
    public void sendBlockForm() {
        sendDropdownForm("tpblock", getVisiblePlayerNames());
    }

    /**
     * Sends the form for /tpunblock.
     */
    public void sendUnblockForm() {
        sendDropdownForm("tpunblock", getVisiblePlayerNames());
    }

    /**
     * Sends the form for /tpcancel.
     */
    public void sendCancelForm() {

        // Builds the list of teleport requests that can be cancelled
        List<TeleportRequest> requests = TeleportRequest.getRequestsByRequester(getPlayer());
        List<String> responders = new ArrayList<>();
        for (TeleportRequest request : requests) {
            responders.add(request.getResponder().getName());
        }

        // Sends the dropdown menu form
        sendDropdownForm("tpcancel", responders);
    }

    /**
     * Sends the form for /tpo.
     */
    public void sendTpoForm() {
        sendDropdownForm("tpo", getVisiblePlayerNames());
    }

    /**
     * Sends the form for /tpohere.
     */
    public void sendTpoHereForm() {
        sendDropdownForm("tpohere", getVisiblePlayerNames());
    }

    private void sendInputForm(@NotNull String command) {

        // Builds the form
        CustomForm form = CustomForm.builder()
                .title(CustomMessages.getStringRaw("Forms." + command + "-title"))
                .input(CustomMessages.getStringRaw("Forms." + command + "-description"))
                .build();

        form.setResponseHandler(responseData -> {

            // Gets the response
            CustomFormResponse response = form.parseResponse(responseData);
            if (getPlayer() == null) {
                CoreClass.getInstance().getLogger().warning("This player with the UUID " + uuid.toString() + " is null, WHY?");
                return;
            }

            // Gets the chosen button
            String name = response.getInput(0);
            getPlayer().performCommand("advancedteleport:" + command + " " + name);
        });

        // Sends the form
        FloodgateApi.getInstance().sendForm(floodgateUuid, form);
    }

    private void sendDropdownForm(@NotNull String command, @NotNull Collection<String> inputs) {

        // Builds the items to be put in the dropdown
        String[] items = new String[inputs.size()];
        int index = 0;
        for (String input : inputs) {
            items[index++] = input;
        }

        // Builds the form
        CustomForm form = CustomForm.builder()
                .title(CustomMessages.getStringRaw("Forms." + command + "-title"))
                .dropdown(CustomMessages.getStringRaw("Forms." + command + "-description"), items)
                .build();

        form.setResponseHandler(responseData -> {

            // Gets the response
            CustomFormResponse response = form.parseResponse(responseData);
            if (getPlayer() == null) {
                CoreClass.getInstance().getLogger().warning("This player with the UUID " + uuid.toString() + " is null, WHY?");
                return;
            }

            // Gets the chosen item
            int i = response.getDropdown(0);
            String result = items[i];

            getPlayer().performCommand("advancedteleport:" + command + " " + result);
        });

        // Sends the form
        FloodgateApi.getInstance().sendForm(floodgateUuid, form);
    }

    @NotNull
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
