package io.papermc.paper.adventure;

import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.minecraft.network.protocol.game.ClientboundBossEventPacket;
import net.minecraft.world.BossEvent;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class VanillaBossBarListener implements BossBar.Listener {
    private final Consumer<Function<BossEvent, ClientboundBossEventPacket>> action;

    public VanillaBossBarListener(final Consumer<Function<BossEvent, ClientboundBossEventPacket>> action) {
        this.action = action;
    }

    @Override
    public void bossBarNameChanged(final @NonNull BossBar bar, final @NonNull Component oldName, final @NonNull Component newName) {
        this.action.accept(ClientboundBossEventPacket::createUpdateNamePacket);
    }

    @Override
    public void bossBarProgressChanged(final @NonNull BossBar bar, final float oldProgress, final float newProgress) {
        this.action.accept(ClientboundBossEventPacket::createUpdateProgressPacket);
    }

    @Override
    public void bossBarColorChanged(final @NonNull BossBar bar, final BossBar.@NonNull Color oldColor, final BossBar.@NonNull Color newColor) {
        this.action.accept(ClientboundBossEventPacket::createUpdateStylePacket);
    }

    @Override
    public void bossBarOverlayChanged(final @NonNull BossBar bar, final BossBar.@NonNull Overlay oldOverlay, final BossBar.@NonNull Overlay newOverlay) {
        this.action.accept(ClientboundBossEventPacket::createUpdateStylePacket);
    }

    @Override
    public void bossBarFlagsChanged(final @NonNull BossBar bar, final @NonNull Set<BossBar.Flag> flagsAdded, final @NonNull Set<BossBar.Flag> flagsRemoved) {
        this.action.accept(ClientboundBossEventPacket::createUpdatePropertiesPacket);
    }
}
