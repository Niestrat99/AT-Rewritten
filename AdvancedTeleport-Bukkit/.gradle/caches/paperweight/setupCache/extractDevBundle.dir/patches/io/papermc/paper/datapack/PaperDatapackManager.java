package io.papermc.paper.datapack;

import java.util.Collection;
import java.util.stream.Collectors;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;

public class PaperDatapackManager implements DatapackManager {
    private final PackRepository repository;

    public PaperDatapackManager(PackRepository repository) {
        this.repository = repository;
    }

    @Override
    public Collection<Datapack> getPacks() {
        Collection<Pack> enabledPacks = repository.getSelectedPacks();
        return repository.getAvailablePacks().stream().map(loader -> new PaperDatapack(loader, enabledPacks.contains(loader))).collect(Collectors.toList());
    }

    @Override
    public Collection<Datapack> getEnabledPacks() {
        return repository.getSelectedPacks().stream().map(loader -> new PaperDatapack(loader, true)).collect(Collectors.toList());
    }
}
