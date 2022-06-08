package org.bukkit.craftbukkit.v1_19_R1.scoreboard;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import java.util.Collection;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.craftbukkit.v1_19_R1.util.CraftChatMessage;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.RenderType;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Team;

public final class CraftScoreboard implements org.bukkit.scoreboard.Scoreboard {
    final Scoreboard board;
    boolean registeredGlobally = false; // Paper

    CraftScoreboard(Scoreboard board) {
        this.board = board;
    }

    @Override
    public CraftObjective registerNewObjective(String name, String criteria) throws IllegalArgumentException {
        return this.registerNewObjective(name, criteria, name);
    }
    // Paper start
    @Override
    public CraftObjective registerNewObjective(String name, String criteria, net.kyori.adventure.text.Component displayName) {
        return registerNewObjective(name, CraftCriteria.getFromBukkit(criteria), displayName, RenderType.INTEGER);
    }
    @Override
    public CraftObjective registerNewObjective(String name, String criteria, net.kyori.adventure.text.Component displayName, RenderType renderType) {
        return registerNewObjective(name, CraftCriteria.getFromBukkit(criteria), displayName, renderType);
    }
    @Override
    public CraftObjective registerNewObjective(String name, Criteria criteria, net.kyori.adventure.text.Component displayName) throws IllegalArgumentException {
        return registerNewObjective(name, criteria, displayName, RenderType.INTEGER);
    }
    @Override
    public CraftObjective registerNewObjective(String name, Criteria criteria, net.kyori.adventure.text.Component displayName, RenderType renderType) throws IllegalArgumentException {
        if (displayName == null) {
            displayName = net.kyori.adventure.text.Component.empty();
        }
        Validate.notNull(name, "Objective name cannot be null");
        Validate.notNull(criteria, "Criteria cannot be null");
        Validate.notNull(displayName, "Display name cannot be null");
        Validate.notNull(renderType, "RenderType cannot be null");
        Validate.isTrue(name.length() <= Short.MAX_VALUE, "The name '" + name + "' is longer than the limit of 32767 characters");
        Validate.isTrue(board.getObjective(name) == null, "An objective of name '" + name + "' already exists");
        // Paper start - the block comment from the old registerNewObjective didnt cause a conflict when rebasing, so this block wasn't added to the adventure registerNewObjective
        if (((CraftCriteria) criteria).criteria != net.minecraft.world.scores.criteria.ObjectiveCriteria.DUMMY && !registeredGlobally) {
            net.minecraft.server.MinecraftServer.getServer().server.getScoreboardManager().registerScoreboardForVanilla(this);
            registeredGlobally = true;
        }
        // Paper end
        net.minecraft.world.scores.Objective objective = board.addObjective(name, ((CraftCriteria) criteria).criteria, io.papermc.paper.adventure.PaperAdventure.asVanilla(displayName), CraftScoreboardTranslations.fromBukkitRender(renderType));
        return new CraftObjective(this, objective);
    }
    // Paper end

    @Override
    public CraftObjective registerNewObjective(String name, String criteria, String displayName) throws IllegalArgumentException {
        return this.registerNewObjective(name, CraftCriteria.getFromBukkit(criteria), displayName, RenderType.INTEGER);
    }

    @Override
    public CraftObjective registerNewObjective(String name, String criteria, String displayName, RenderType renderType) throws IllegalArgumentException {
        return this.registerNewObjective(name, CraftCriteria.getFromBukkit(criteria), displayName, renderType);
    }

    @Override
    public CraftObjective registerNewObjective(String name, Criteria criteria, String displayName) throws IllegalArgumentException {
        return this.registerNewObjective(name, criteria, displayName, RenderType.INTEGER);
    }

    @Override
    public CraftObjective registerNewObjective(String name, Criteria criteria, String displayName, RenderType renderType) throws IllegalArgumentException {
        return registerNewObjective(name, criteria, net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().deserialize(displayName), renderType); // Paper
    }

    @Override
    public Objective getObjective(String name) throws IllegalArgumentException {
        Validate.notNull(name, "Name cannot be null");
        net.minecraft.world.scores.Objective nms = this.board.getObjective(name);
        return nms == null ? null : new CraftObjective(this, nms);
    }

    @Override
    public ImmutableSet<Objective> getObjectivesByCriteria(String criteria) throws IllegalArgumentException {
        Validate.notNull(criteria, "Criteria cannot be null");

        ImmutableSet.Builder<Objective> objectives = ImmutableSet.builder();
        for (net.minecraft.world.scores.Objective netObjective : (Collection<net.minecraft.world.scores.Objective>) this.board.getObjectives()) {
            CraftObjective objective = new CraftObjective(this, netObjective);
            if (objective.getCriteria().equals(criteria)) {
                objectives.add(objective);
            }
        }
        return objectives.build();
    }

    @Override
    public ImmutableSet<Objective> getObjectivesByCriteria(Criteria criteria) throws IllegalArgumentException {
        Validate.notNull(criteria, "Criteria cannot be null");

        ImmutableSet.Builder<Objective> objectives = ImmutableSet.builder();
        for (net.minecraft.world.scores.Objective netObjective : this.board.getObjectives()) {
            CraftObjective objective = new CraftObjective(this, netObjective);
            if (objective.getTrackedCriteria().equals(criteria)) {
                objectives.add(objective);
            }
        }

        return objectives.build();
    }

    @Override
    public ImmutableSet<Objective> getObjectives() {
        return ImmutableSet.copyOf(Iterables.transform((Collection<net.minecraft.world.scores.Objective>) this.board.getObjectives(), new Function<net.minecraft.world.scores.Objective, Objective>() {

            @Override
            public Objective apply(net.minecraft.world.scores.Objective input) {
                return new CraftObjective(CraftScoreboard.this, input);
            }
        }));
    }

    @Override
    public Objective getObjective(DisplaySlot slot) throws IllegalArgumentException {
        Validate.notNull(slot, "Display slot cannot be null");
        net.minecraft.world.scores.Objective objective = this.board.getDisplayObjective(CraftScoreboardTranslations.fromBukkitSlot(slot));
        if (objective == null) {
            return null;
        }
        return new CraftObjective(this, objective);
    }

    @Override
    public ImmutableSet<Score> getScores(OfflinePlayer player) throws IllegalArgumentException {
        Validate.notNull(player, "OfflinePlayer cannot be null");

        return this.getScores(player.getName());
    }

    @Override
    public ImmutableSet<Score> getScores(String entry) throws IllegalArgumentException {
        Validate.notNull(entry, "Entry cannot be null");

        ImmutableSet.Builder<Score> scores = ImmutableSet.builder();
        for (net.minecraft.world.scores.Objective objective : (Collection<net.minecraft.world.scores.Objective>) this.board.getObjectives()) {
            scores.add(new CraftScore(new CraftObjective(this, objective), entry));
        }
        return scores.build();
    }

    @Override
    public void resetScores(OfflinePlayer player) throws IllegalArgumentException {
        Validate.notNull(player, "OfflinePlayer cannot be null");

        this.resetScores(player.getName());
    }

    @Override
    public void resetScores(String entry) throws IllegalArgumentException {
        Validate.notNull(entry, "Entry cannot be null");

        for (net.minecraft.world.scores.Objective objective : (Collection<net.minecraft.world.scores.Objective>) this.board.getObjectives()) {
            this.board.resetPlayerScore(entry, objective);
        }
    }

    @Override
    public Team getPlayerTeam(OfflinePlayer player) throws IllegalArgumentException {
        Validate.notNull(player, "OfflinePlayer cannot be null");

        PlayerTeam team = this.board.getPlayersTeam(player.getName());
        return team == null ? null : new CraftTeam(this, team);
    }

    @Override
    public Team getEntryTeam(String entry) throws IllegalArgumentException {
        Validate.notNull(entry, "Entry cannot be null");

        PlayerTeam team = this.board.getPlayersTeam(entry);
        return team == null ? null : new CraftTeam(this, team);
    }

    @Override
    public Team getTeam(String teamName) throws IllegalArgumentException {
        Validate.notNull(teamName, "Team name cannot be null");

        PlayerTeam team = this.board.getPlayerTeam(teamName);
        return team == null ? null : new CraftTeam(this, team);
    }

    @Override
    public ImmutableSet<Team> getTeams() {
        return ImmutableSet.copyOf(Iterables.transform((Collection<PlayerTeam>) this.board.getPlayerTeams(), new Function<PlayerTeam, Team>() {

            @Override
            public Team apply(PlayerTeam input) {
                return new CraftTeam(CraftScoreboard.this, input);
            }
        }));
    }

    @Override
    public Team registerNewTeam(String name) throws IllegalArgumentException {
        Validate.notNull(name, "Team name cannot be null");
        Validate.isTrue(name.length() <= Short.MAX_VALUE, "Team name '" + name + "' is longer than the limit of 32767 characters");
        Validate.isTrue(this.board.getPlayerTeam(name) == null, "Team name '" + name + "' is already in use");

        return new CraftTeam(this, this.board.addPlayerTeam(name));
    }

    @Override
    public ImmutableSet<OfflinePlayer> getPlayers() {
        ImmutableSet.Builder<OfflinePlayer> players = ImmutableSet.builder();
        for (Object playerName : this.board.getTrackedPlayers()) {
            players.add(Bukkit.getOfflinePlayer(playerName.toString()));
        }
        return players.build();
    }

    @Override
    public ImmutableSet<String> getEntries() {
        ImmutableSet.Builder<String> entries = ImmutableSet.builder();
        for (Object entry : this.board.getTrackedPlayers()) {
            entries.add(entry.toString());
        }
        return entries.build();
    }

    @Override
    public void clearSlot(DisplaySlot slot) throws IllegalArgumentException {
        Validate.notNull(slot, "Slot cannot be null");
        this.board.setDisplayObjective(CraftScoreboardTranslations.fromBukkitSlot(slot), null);
    }

    // CraftBukkit method
    public Scoreboard getHandle() {
        return this.board;
    }
    // Paper start
    @Override
    public ImmutableSet<Score> getScoresFor(org.bukkit.entity.Entity entity) throws IllegalArgumentException {
        Validate.notNull(entity, "Entity cannot be null");
        return this.getScores(((org.bukkit.craftbukkit.v1_19_R1.entity.CraftEntity) entity).getHandle().getScoreboardName());
    }

    @Override
    public void resetScoresFor(org.bukkit.entity.Entity entity) throws IllegalArgumentException {
        Validate.notNull(entity, "Entity cannot be null");
        this.resetScores(((org.bukkit.craftbukkit.v1_19_R1.entity.CraftEntity) entity).getHandle().getScoreboardName());
    }

    @Override
    public Team getEntityTeam(org.bukkit.entity.Entity entity) throws IllegalArgumentException {
        Validate.notNull(entity, "Entity cannot be null");
        return this.getEntryTeam(((org.bukkit.craftbukkit.v1_19_R1.entity.CraftEntity) entity).getHandle().getScoreboardName());
    }
    // Paper end
}
