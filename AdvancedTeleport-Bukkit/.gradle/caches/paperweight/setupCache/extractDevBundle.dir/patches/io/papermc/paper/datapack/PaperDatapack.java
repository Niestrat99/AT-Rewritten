package io.papermc.paper.datapack;

import io.papermc.paper.event.server.ServerResourcesReloadedEvent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.repository.Pack;
import java.util.List;
import java.util.stream.Collectors;

public class PaperDatapack implements Datapack {
    private final String name;
    private final Compatibility compatibility;
    private final boolean enabled;

    PaperDatapack(Pack loader, boolean enabled) {
        this.name = loader.getId();
        this.compatibility = Compatibility.valueOf(loader.getCompatibility().name());
        this.enabled = enabled;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Compatibility getCompatibility() {
        return compatibility;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (enabled == this.enabled) {
            return;
        }

        MinecraftServer server = MinecraftServer.getServer();
        List<String> enabledKeys = server.getPackRepository().getSelectedPacks().stream().map(Pack::getId).collect(Collectors.toList());
        if (enabled) {
            enabledKeys.add(this.name);
        } else {
            enabledKeys.remove(this.name);
        }
        server.reloadResources(enabledKeys, ServerResourcesReloadedEvent.Cause.PLUGIN);
    }
}
