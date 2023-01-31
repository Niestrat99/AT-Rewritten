package io.github.niestrat99.advancedteleport.api;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.managers.PluginHookManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.geysermc.cumulus.CustomForm;
import org.geysermc.cumulus.SimpleForm;
import org.geysermc.cumulus.response.CustomFormResponse;
import org.geysermc.cumulus.response.SimpleFormResponse;
import org.geysermc.floodgate.api.FloodgateApi;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Represents a bedrock-connected player.
 */
public final class ATFloodgatePlayer extends ATPlayer {

    private final @NotNull UUID floodgateUuid;

    @Contract(pure = true)
    ATFloodgatePlayer(@NotNull final Player player) throws IllegalStateException {
        super(player);
        if (!PluginHookManager.get().floodgateEnabled()) {
            throw new IllegalStateException("Floodgate is not enabled.");
        }

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

    private void sendDropdownForm(
        @NotNull String command,
        @NotNull Collection<String> inputs
    ) {

        // Builds the items to be put in the dropdown
        String[] items = new String[inputs.size()];
        int index = 0;
        for (String input : inputs) {
            items[index++] = input;
        }

        // Builds the form
        CustomForm form = CustomForm.builder()
            .title(CustomMessages.asString("Forms." + command + "-title"))
            .dropdown(CustomMessages.asString("Forms." + command + "-description"), items)
            .build();

        form.setResponseHandler(responseData -> {

            // Gets the response
            CustomFormResponse response = form.parseResponse(responseData);
            if (getPlayer() == null) {
                CoreClass.getInstance().getLogger().warning("This player with the UUID " + uuid + " is null, WHY?");
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

    @Contract(pure = true)
    private @NotNull List<String> getVisiblePlayerNames() {
        return Bukkit.getOnlinePlayers().stream()
            .filter(player -> player != getPlayer())
            .filter(player -> getPlayer().canSee(player))
            .filter(player -> {
                final var atPlayer = ATPlayer.getPlayer(player);
                return !atPlayer.hasBlocked(getPlayer()) && this.hasBlocked(atPlayer.uuid());
            }).map(Player::getName).toList();
    }

    @Override
    public @Nullable Player getPlayer() {
        return Bukkit.getPlayer(floodgateUuid);
    }

    /**
     * Send the request form for accepting and denying a /tpa request.
     *
     * @param sender the player that requested the teleportation.
     */
    public void sendRequestFormTPA(@NotNull final Player sender) {

        // Set up the form
        SimpleForm form = SimpleForm.builder()
            .title(CustomMessages.asString("Forms.tpa-received-title"))
            .content(CustomMessages.asString("Forms.tpa-received-description", "player", (Supplier<Component>) sender::displayName))
            .button(CustomMessages.asString("Forms.tpa-received-accept"))
            .button(CustomMessages.asString("Forms.tpa-received-deny"))
            .build();

        sendRequest(sender, form);
    }

    private void sendRequest(
        @NotNull Player sender,
        SimpleForm form
    ) {
        form.setResponseHandler(responseData -> {
            SimpleFormResponse response = form.parseResponse(responseData);
            if (getPlayer() == null) {
                CoreClass.getInstance().getLogger().warning("This player with the UUID " + uuid + " is null, WHY?");
                return;
            }
            getPlayer().performCommand(response.getClickedButtonId() == 0 ? "advancedteleport:tpyes " + sender.getName()
                                                                          : "advancedteleport:tpno " + sender.getName());
        });

        FloodgateApi.getInstance().sendForm(floodgateUuid, form);
    }

    /**
     * Send the request form for accepting and denying a /tpahere request.
     *
     * @param sender the player that requested the teleportation.
     */
    public void sendRequestFormTPAHere(@NotNull final Player sender) {

        // Set up the form
        SimpleForm form = SimpleForm.builder()
            .title(CustomMessages.asString("Forms.tpahere-received-title"))
            .content(CustomMessages.asString("Forms.tpahere-received-description", "player", (Supplier<Component>) sender::displayName))
            .button(CustomMessages.asString("Forms.tpahere-received-accept"))
            .button(CustomMessages.asString("Forms.tpahere-received-deny"))
            .build();

        // Send the form
        sendRequest(sender, form);
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

    private void sendInputForm(@NotNull String command) {

        // Builds the form
        CustomForm form = CustomForm.builder()
            .title(CustomMessages.asString("Forms." + command + "-title"))
            .input(CustomMessages.asString("Forms." + command + "-description"))
            .build();

        form.setResponseHandler(responseData -> {

            // Gets the response
            CustomFormResponse response = form.parseResponse(responseData);
            if (getPlayer() == null) {
                CoreClass.getInstance().getLogger().warning("This player with the UUID " + uuid + " is null, WHY?");
                return;
            }

            // Gets the chosen button
            String name = response.getInput(0);
            getPlayer().performCommand("advancedteleport:" + command + " " + name);
        });

        // Sends the form
        FloodgateApi.getInstance().sendForm(floodgateUuid, form);
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
        final var responders = TeleportRequest.getRequestsByRequester(getPlayer()).stream()
            .map(request -> request.requester().getName())
            .toList();

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
}
