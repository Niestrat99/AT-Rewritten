package io.github.niestrat99.advancedteleport.api;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.managers.PluginHookManager;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
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

public final class ATFloodgatePlayer extends ATPlayer {

    @NotNull private final UUID floodgateUuid;

    @Contract(pure = true)
    public ATFloodgatePlayer(@NotNull final Player player) throws IllegalStateException {
        super(player);
        if (!PluginHookManager.get().floodgateEnabled()) {
            throw new IllegalStateException("Floodgate is not enabled.");
        }

        floodgateUuid = FloodgateApi.getInstance().getPlayer(player.getUniqueId()).getCorrectUniqueId();
    }

    public void sendTPAForm(boolean here) {
        if (here) {
            sendDropdownForm("tpahere", getVisiblePlayerNames());
        } else {
            sendDropdownForm("tpa", getVisiblePlayerNames());
        }
    }

    public void sendRequestFormTPA(@NotNull final Player sender) {
        SimpleForm form = SimpleForm.builder()
                .title(CustomMessages.getStringRaw("Forms.tpa-received-title"))
                .content(CustomMessages.getString("Forms.tpa-received-description", "{player}", sender.getDisplayName()))
                .button(CustomMessages.getStringRaw("Forms.tpa-received-accept"))
                .button(CustomMessages.getStringRaw("Forms.tpa-received-deny"))
                .build();

        sendRequest(sender, form);
    }

    public void sendRequestFormTPAHere(@NotNull final Player sender) {
        SimpleForm form = SimpleForm.builder()
                .title(CustomMessages.getStringRaw("Forms.tpahere-received-title"))
                .content(CustomMessages.getString("Forms.tpahere-received-description", "{player}", sender.getDisplayName()))
                .button(CustomMessages.getStringRaw("Forms.tpahere-received-accept"))
                .button(CustomMessages.getStringRaw("Forms.tpahere-received-deny"))
                .build();

        sendRequest(sender, form);
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
        final var responders = TeleportRequest.getRequestsByRequester(getPlayer()).stream()
            .map(request -> request.requester().getName())
            .toList();

        sendDropdownForm("tpcancel", responders);
    }

    public void sendTpoForm() {
        sendDropdownForm("tpo", getVisiblePlayerNames());
    }

    public void sendTpoHereForm() {
        sendDropdownForm("tpohere", getVisiblePlayerNames());
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

        FloodgateApi.getInstance().sendForm(floodgateUuid, form);
    }

    private void sendRequest(@NotNull Player sender, SimpleForm form) {
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
}
