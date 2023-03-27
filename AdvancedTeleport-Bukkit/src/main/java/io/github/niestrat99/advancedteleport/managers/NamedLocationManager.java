package io.github.niestrat99.advancedteleport.managers;

import com.google.common.collect.ImmutableMap;
import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.api.Warp;
import io.github.niestrat99.advancedteleport.api.spawn.Spawn;
import io.github.niestrat99.advancedteleport.config.MainConfig;
import io.github.niestrat99.advancedteleport.sql.MetadataSQLManager;
import io.github.niestrat99.advancedteleport.sql.SpawnSQLManager;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

/**
 * Used to manage the internal registration of warps and spawns during runtime.
 * <br>
 * Shouldn't be accessed to bypass the API, this behaviour can change spontaneously and does not reflect updates in the database.
 */
@ApiStatus.Internal
public class NamedLocationManager {

    @NotNull private final HashMap<String, Warp> warps;
    @NotNull private final HashMap<String, Spawn> spawns;
    @NotNull private final HashMap<String, Spawn> worldMirrors;
    @Nullable private Spawn mainSpawn;
    private static NamedLocationManager instance;

    public NamedLocationManager() {
        instance = this;

        this.warps = new HashMap<>();
        this.spawns = new HashMap<>();
        this.worldMirrors = new HashMap<>();

        CoreClass.debug("Set up the NamedLocationManager. Ready to accept warps and spawnpoints.");
    }

    public static NamedLocationManager get() {
        return instance;
    }

    public void loadSpawnData() {

        // Go through each registered spawn in the SQL database
        SpawnSQLManager.get().getSpawns().thenAcceptAsync(spawns -> {

            // Add to the spawns list
            spawns.forEach(this::registerSpawn);

            // Then load mirrored spawns
            MetadataSQLManager.get().loadMirroredSpawns();

            // Get the main spawn
            MetadataSQLManager.get().loadMainSpawn().whenComplete((result, err) -> {
                this.mainSpawn = result;

                if (err != null) {
                    CoreClass.getInstance().getLogger().warning("Failed to get the main spawn!");
                    err.printStackTrace();
                }
            });
        }, CoreClass.sync);
    }

    @Contract(pure = true)
    public void registerWarp(@NotNull Warp warp) {
        this.warps.put(warp.getName(), warp);
    }

    @Contract(pure = true)
    public boolean isWarpSet(@NotNull String name) {
        return this.warps.containsKey(name);
    }

    @Contract(pure = true)
    public void removeWarp(@NotNull Warp warp) {
        this.warps.remove(warp.getName());
    }

    public ImmutableMap<String, Warp> getWarps() {
        return ImmutableMap.copyOf(this.warps);
    }

    @Contract(pure = true)
    public void registerSpawn(@NotNull Spawn spawn) {
        this.spawns.put(spawn.getName(), spawn);
        CoreClass.debug("Registered spawn " + spawn.getName());
    }

    @Contract(pure = true)
    public @Nullable Spawn getSpawn(@NotNull String name) {
        return this.spawns.get(name);
    }

    @Contract(pure = true)
    public @Nullable Spawn getSpawn(
            @NotNull String name,
            @Nullable Player teleportingPlayer
    ) {

        // If the spawn isn't registered by that name, return null
        if (!this.spawns.containsKey(name)) return null;

        // Get the destination spawn
        Spawn spawn = this.spawns.get(name);

        // If there's no player, just throw this spawn
        if (teleportingPlayer == null) return spawn;

        // While the player can't access the spawn, go to the next spawn
        while (!spawn.canAccess(teleportingPlayer) && spawn.getMirroringSpawn() != null) {
            spawn = spawn.getMirroringSpawn();
        }

        // Return the spawn if it can be reached
        if (spawn.canAccess(teleportingPlayer)) return spawn;

        // Return nothing
        return null;
    }

    @Contract(pure = true)
    public @NotNull Spawn getSpawn(
            @NotNull World world,
            @Nullable Player teleportingPlayer
    ) {

        // If there's no spawns registered under the world's name, try using the main spawn.
        if (!this.spawns.containsKey(world.getName()) && !this.worldMirrors.containsKey(world.getName())) {
            return getUndeclaredSpawn(world);
        }

        // Get the destination spawn
        Spawn spawn = this.spawns.getOrDefault(world.getName(), this.worldMirrors.get(world.getName()));

        // If there's no player, just throw this spawn
        if (teleportingPlayer == null) return spawn;

        // While the player can't access the spawn, go to the next spawn
        while (!spawn.canAccess(teleportingPlayer) && spawn.getMirroringSpawn() != null && spawn.getMirroringSpawn() != spawn) {
            spawn = spawn.getMirroringSpawn();
        }

        // If the spawn cannot be reached, return the undeclared spawn
        if (!spawn.canAccess(teleportingPlayer)) return getUndeclaredSpawn(world);

        // Return the spawn
        return spawn;
    }

    @Contract(pure = true)
    private Spawn getUndeclaredSpawn(@NotNull World world) {

        // If there's a main spawn, just return it
        if (this.mainSpawn != null) return mainSpawn;

        // If the main spawn isn't there though, get the spawn location of the world
        if (MainConfig.get().USE_OVERWORLD.get()) {

            // If the dimension is in the overworld, just send them there
            if (world.getEnvironment() == World.Environment.NORMAL)
                return new Spawn(world.getName(), world.getSpawnLocation());

            // Remove the end/nether suffix
            String overworldName = world.getName().replaceAll("(_nether|_the_end)$", "");

            // If the world isn't loaded/existent, just relocate to the original world's spawn
            World overworld = Bukkit.getWorld(overworldName);
            if (overworld == null)
                return new Spawn(world.getName(), world.getSpawnLocation());

            // Otherwise, teleport to the overworld
            return this.spawns.getOrDefault(overworld.getName(), new Spawn(overworld.getName(), overworld.getSpawnLocation()));
        } else {

            // Return a new spawn object, but don't register it for now
            return new Spawn(world.getName(), world.getSpawnLocation());
        }
    }

    @Contract
    public void addMirroredSpawn(@NotNull String from, @Nullable Spawn spawn) {
        if (spawn != null) {
            this.worldMirrors.put(from, spawn);
            CoreClass.debug("Added world mirror " + from + " to redirect to " + spawn.getName() + ".");
        } else {
            this.worldMirrors.remove(from);
        }
    }

    @Contract(pure = true)
    public void setMainSpawn(@Nullable Spawn spawn) {
        this.mainSpawn = spawn;
    }

    @Contract(pure = true)
    public void removeSpawn(@NotNull Spawn spawn) {
        this.spawns.remove(spawn.getName());
    }

    @Contract(pure = true)
    public boolean isSpawnSet(@NotNull String id) {
        return this.spawns.containsKey(id);
    }

    @Contract(pure = true)
    public @NotNull ImmutableMap<String, Spawn> getSpawns() {
        return ImmutableMap.copyOf(this.spawns);
    }

    public @Nullable Spawn getMainSpawn() {
        return mainSpawn;
    }
}
