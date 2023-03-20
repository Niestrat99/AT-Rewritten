package io.github.niestrat99.advancedteleport.hooks.particles;

import dev.esophose.playerparticles.PlayerParticles;
import dev.esophose.playerparticles.api.PlayerParticlesAPI;
import dev.esophose.playerparticles.particles.PPlayer;
import dev.esophose.playerparticles.particles.ParticleEffect;
import dev.esophose.playerparticles.particles.ParticlePair;
import dev.esophose.playerparticles.particles.data.ColorTransition;
import dev.esophose.playerparticles.particles.data.NoteColor;
import dev.esophose.playerparticles.particles.data.OrdinaryColor;
import dev.esophose.playerparticles.particles.data.Vibration;
import dev.esophose.playerparticles.styles.ParticleStyle;
import io.github.niestrat99.advancedteleport.config.MainConfig;
import io.github.niestrat99.advancedteleport.hooks.ParticlesPlugin;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public final class PlayerParticlesHook extends ParticlesPlugin<PlayerParticles, Void> {

    // For configuration parsing, we'll use just id for presets, or particle,
    private PlayerParticlesAPI api;

    public PlayerParticlesHook() {
        super("PlayerParticles");
    }

    @Override
    public boolean pluginUsable() {

        // If particles are enabled and the plugin is enabled
        if (!super.pluginUsable()) return false;

        // Get the plugin itself and ensure the API is the one we want.
        return plugin().map(plugin -> {
            this.api = PlayerParticlesAPI.getInstance();
            return true;
        }).orElse(false);
    }

    @Override
    public void applyParticles(
            @NotNull final Player player,
            @NotNull final String command
    ) {
        getPairStream(player, command)
                .forEach(pair -> api.addActivePlayerParticle(player, pair));
    }

    @Override
    public void removeParticles(
            @NotNull final Player player,
            @NotNull final String command
    ) {

        // Get the existing particles in place, but if it doesn't already exist? Meh
        String rawParticle = MainConfig.get().WAITING_PARTICLES.valueOf(command).get();
        if (rawParticle.isEmpty()) return;

        // Get the raw pairs of particles and parse them
        String[] rawPairs = rawParticle.split(";");
        for (String rawPair : rawPairs) {

            // Individual pieces
            String[] parts = rawPair.split(",");
            int id = -1;

            // Go through each of the active particles active on the player
            // If any comes from AT, remove it
            for (ParticlePair newPair : api.getActivePlayerParticles(player)) {
                if (newPair == null) continue;
                if (!newPair.getEffect().getInternalName().equals(parts[0])) continue;
                if (!newPair.getStyle().getInternalName().equals(parts[1])) continue;
                if (!getRawDataFromPair(newPair).equals(parts[2])) continue;
                id = newPair.getId();
                break;
            }

            // If there's no ID to remove, stop there
            if (id == -1) continue;
            api.removeActivePlayerParticle(player, id);
        }
    }

    @Override
    @Nullable
    public String getParticle(@NotNull final Player player) {

        // Get the player particles
        Collection<ParticlePair> particlePairs = api.getActivePlayerParticles(player);

        // Create the list of strings to build this up
        List<String> particleRawList = new ArrayList<>();

        // For each particle...
        for (ParticlePair pair : particlePairs) {
            ParticleEffect effect = pair.getEffect();
            ParticleStyle style = pair.getStyle();
            particleRawList.add(effect.getInternalName() + "," + style.getInternalName() + "," + getRawDataFromPair(pair));
        }

        // If it's empty, return null
        if (particleRawList.isEmpty()) return null;

        // Get all particles joined together with a semi-colon
        return String.join(";", particleRawList);
    }

    private String getRawDataFromPair(@NotNull final ParticlePair pair) {

        // If it's a block or falling dust, use the name of the block material
        if (pair.getEffect() == ParticleEffect.BLOCK || pair.getEffect() == ParticleEffect.FALLING_DUST)
            return pair.getBlockMaterial().name();

        // If it's an item, get the name of the item material
        if (pair.getEffect() == ParticleEffect.ITEM) return pair.getItemMaterial().name();

        // If the effect is colourable, get the colour of that
        if (pair.getEffect().hasProperty(ParticleEffect.ParticleProperty.COLORABLE)) {
            if (pair.getEffect() == ParticleEffect.NOTE) {
                return parseColour(pair.getNoteColor(), NoteColor.RAINBOW, NoteColor.RANDOM,
                        String.valueOf(pair.getNoteColor().getNote()));
            } else {
                return parseColour(pair.getColor(), OrdinaryColor.RAINBOW, OrdinaryColor.RANDOM,
                        pair.getColor().getRed() + " " + pair.getColor().getGreen() + " " + pair.getColor().getBlue());
            }
        }

        // If the effect transitions between colours, get the values of that
        if (pair.getEffect().hasProperty(ParticleEffect.ParticleProperty.COLORABLE_TRANSITION)) {
            String start = parseColour(pair.getColorTransition().getStartColor(), OrdinaryColor.RAINBOW, OrdinaryColor.RANDOM,
                    pair.getColor().getRed() + " " + pair.getColor().getGreen() + " " + pair.getColor().getBlue());
            String end = parseColour(pair.getColorTransition().getEndColor(), OrdinaryColor.RAINBOW, OrdinaryColor.RANDOM,
                    pair.getColor().getRed() + " " + pair.getColor().getGreen() + " " + pair.getColor().getBlue());

            return start + ">" + end;
        } else if (pair.getEffect().hasProperty(ParticleEffect.ParticleProperty.VIBRATION)) {
            return String.valueOf(pair.getVibration().getDuration());
        } else {
            return "null";
        }
    }

    private @Nullable ParticlePair getPairFromData(
        @NotNull final Player player,
        @NotNull final String data
    ) {
        String[] parts = data.split(",");

        // Get the effect
        ParticleEffect effect = ParticleEffect.fromInternalName(parts[0]);
        if (effect == null) return null;

        // Get the style
        ParticleStyle style = ParticleStyle.fromInternalName(parts[1]);
        if (style == null) return null;

        // If it's an RGB thing
        String[] rgb = parts[2].split(" ");

        // Get the data items
        Material itemMaterial = get(effect == ParticleEffect.ITEM, () -> Material.getMaterial(data));
        Material blockMaterial = get(effect == ParticleEffect.BLOCK
                || effect == ParticleEffect.FALLING_DUST, () -> Material.getMaterial(data));
        OrdinaryColor ordinaryColor = getNote(data, effect, OrdinaryColor.RAINBOW, OrdinaryColor.RANDOM,
                () -> new OrdinaryColor(Integer.parseInt(rgb[0]), Integer.parseInt(rgb[1]), Integer.parseInt(rgb[2])));

        NoteColor noteColor = getNote(data, effect, NoteColor.RAINBOW, NoteColor.RANDOM,
                () -> new NoteColor(Integer.parseInt(rgb[0])));

        Vibration vibration = get(effect.hasProperty(ParticleEffect.ParticleProperty.VIBRATION), () -> new Vibration(Integer.parseInt(data)));
        ColorTransition colorTransition = get(effect.hasProperty(ParticleEffect.ParticleProperty.COLORABLE_TRANSITION), () -> {
           String[] startAndEnd = data.split(">");
           String[] startRaw = startAndEnd[0].split(" ");
           String[] endRaw = startAndEnd[1].split(" ");
           OrdinaryColor start = new OrdinaryColor(Integer.parseInt(startRaw[0]), Integer.parseInt(startRaw[1]), Integer.parseInt(startRaw[2]));
           OrdinaryColor end = new OrdinaryColor(Integer.parseInt(endRaw[0]), Integer.parseInt(endRaw[1]), Integer.parseInt(endRaw[2]));

           return new ColorTransition(start, end);
        });

        PPlayer pPlayer = api.getPPlayer(player);
        if (pPlayer == null) return null;
        // Get the particle
        return new ParticlePair(player.getUniqueId(), pPlayer.getNextActiveParticleId(), effect, style,
                itemMaterial, blockMaterial, ordinaryColor, noteColor, colorTransition, vibration);
    }

    private <T> @Nullable T get(
        final boolean condition,
        @NotNull Supplier<T> supplier
    ) {
        if (condition) supplier.get();
        return null;
    }

    private <T> T getNote(
        @NotNull final String data,
        @NotNull final ParticleEffect effect,
        @NotNull final T rainbow,
        @NotNull final T random,
        @NotNull final Supplier<T> base
    ) {
        return get(effect.hasProperty(ParticleEffect.ParticleProperty.COLORABLE)
                && effect != ParticleEffect.NOTE, () -> data.equals("rainbow") ? rainbow
                : (data.equals("random") ? random : base.get()));
    }

    private <T> @NotNull String parseColour(
        @NotNull final T colour,
        @NotNull final T rainbow,
        @NotNull final T random,
        @NotNull final String base
    ) {
        if (colour == rainbow) {
            return "rainbow";
        } else if (colour == random) {
            return "random";
        } else {
            return base;
        }
    }

    private @NotNull Stream<ParticlePair> getPairStream(
        @NotNull final Player player,
        @NotNull final String command
    ) {
        final var rawParticle = MainConfig.get().WAITING_PARTICLES.valueOf(command).get();
        if (rawParticle.isEmpty()) return Stream.empty();

        return Arrays.stream(rawParticle.split(";"))
            .map(rawPair -> getPairFromData(player, rawPair))
            .filter(Objects::nonNull);
    }
}
