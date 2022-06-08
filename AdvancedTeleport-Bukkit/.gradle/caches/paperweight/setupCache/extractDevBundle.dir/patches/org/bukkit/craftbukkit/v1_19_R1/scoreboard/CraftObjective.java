package org.bukkit.craftbukkit.v1_19_R1.scoreboard;

import net.minecraft.world.scores.Scoreboard;
import org.apache.commons.lang.Validate;
import org.bukkit.OfflinePlayer;
import org.bukkit.craftbukkit.v1_19_R1.util.CraftChatMessage;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.RenderType;
import org.bukkit.scoreboard.Score;

final class CraftObjective extends CraftScoreboardComponent implements Objective {
    private final net.minecraft.world.scores.Objective objective;
    private final CraftCriteria criteria;

    CraftObjective(CraftScoreboard scoreboard, net.minecraft.world.scores.Objective objective) {
        super(scoreboard);
        this.objective = objective;
        this.criteria = CraftCriteria.getFromNMS(objective);
    }

    net.minecraft.world.scores.Objective getHandle() {
        return this.objective;
    }

    @Override
    public String getName() throws IllegalStateException {
        CraftScoreboard scoreboard = this.checkState();

        return this.objective.getName();
    }

    // Paper start
    @Override
    public net.kyori.adventure.text.Component displayName() throws IllegalStateException {
        CraftScoreboard scoreboard = checkState();
        return io.papermc.paper.adventure.PaperAdventure.asAdventure(objective.getDisplayName());
    }
    @Override
    public void displayName(net.kyori.adventure.text.Component displayName) throws IllegalStateException, IllegalArgumentException {
        if (displayName == null) {
            displayName = net.kyori.adventure.text.Component.empty();
        }
        CraftScoreboard scoreboard = checkState();
        objective.setDisplayName(io.papermc.paper.adventure.PaperAdventure.asVanilla(displayName));
    }
    // Paper end
    @Override
    public String getDisplayName() throws IllegalStateException {
        CraftScoreboard scoreboard = this.checkState();

        return CraftChatMessage.fromComponent(this.objective.getDisplayName());
    }

    @Override
    public void setDisplayName(String displayName) throws IllegalStateException, IllegalArgumentException {
        Validate.notNull(displayName, "Display name cannot be null");
        Validate.isTrue(displayName.length() <= 128, "Display name '" + displayName + "' is longer than the limit of 128 characters");
        CraftScoreboard scoreboard = this.checkState();

        this.objective.setDisplayName(CraftChatMessage.fromString(displayName)[0]); // SPIGOT-4112: not nullable
    }

    @Override
    public String getCriteria() throws IllegalStateException {
        CraftScoreboard scoreboard = this.checkState();

        return criteria.bukkitName;
    }

    @Override
    public Criteria getTrackedCriteria() throws IllegalStateException {
        CraftScoreboard scoreboard = this.checkState();

        return this.criteria;
    }

    @Override
    public boolean isModifiable() throws IllegalStateException {
        CraftScoreboard scoreboard = this.checkState();

        return !criteria.criteria.isReadOnly();
    }

    @Override
    public void setDisplaySlot(DisplaySlot slot) throws IllegalStateException {
        CraftScoreboard scoreboard = this.checkState();
        Scoreboard board = scoreboard.board;
        net.minecraft.world.scores.Objective objective = this.objective;

        for (int i = 0; i < CraftScoreboardTranslations.MAX_DISPLAY_SLOT; i++) {
            if (board.getDisplayObjective(i) == objective) {
                board.setDisplayObjective(i, null);
            }
        }
        if (slot != null) {
            int slotNumber = CraftScoreboardTranslations.fromBukkitSlot(slot);
            board.setDisplayObjective(slotNumber, this.getHandle());
        }
    }

    @Override
    public DisplaySlot getDisplaySlot() throws IllegalStateException {
        CraftScoreboard scoreboard = this.checkState();
        Scoreboard board = scoreboard.board;
        net.minecraft.world.scores.Objective objective = this.objective;

        for (int i = 0; i < CraftScoreboardTranslations.MAX_DISPLAY_SLOT; i++) {
            if (board.getDisplayObjective(i) == objective) {
                return CraftScoreboardTranslations.toBukkitSlot(i);
            }
        }
        return null;
    }

    @Override
    public void setRenderType(RenderType renderType) throws IllegalStateException {
        Validate.notNull(renderType, "RenderType cannot be null");
        CraftScoreboard scoreboard = this.checkState();

        this.objective.setRenderType(CraftScoreboardTranslations.fromBukkitRender(renderType));
    }

    @Override
    public RenderType getRenderType() throws IllegalStateException {
        CraftScoreboard scoreboard = this.checkState();

        return CraftScoreboardTranslations.toBukkitRender(this.objective.getRenderType());
    }

    @Override
    public Score getScore(OfflinePlayer player) throws IllegalArgumentException, IllegalStateException {
        Validate.notNull(player, "Player cannot be null");
        CraftScoreboard scoreboard = this.checkState();

        return new CraftScore(this, player.getName());
    }

    @Override
    public Score getScore(String entry) throws IllegalArgumentException, IllegalStateException {
        Validate.notNull(entry, "Entry cannot be null");
        Validate.isTrue(entry.length() <= Short.MAX_VALUE, "Score '" + entry + "' is longer than the limit of 32767 characters");
        CraftScoreboard scoreboard = this.checkState();

        return new CraftScore(this, entry);
    }

    // Paper start
    @Override
    public Score getScoreFor(org.bukkit.entity.Entity entity) throws IllegalArgumentException, IllegalStateException {
        Validate.notNull(entity, "Entity cannot be null");
        return getScore(((org.bukkit.craftbukkit.v1_19_R1.entity.CraftEntity) entity).getHandle().getScoreboardName());
    }
    // Paper end

    @Override
    public void unregister() throws IllegalStateException {
        CraftScoreboard scoreboard = this.checkState();

        scoreboard.board.removeObjective(objective);
    }

    @Override
    CraftScoreboard checkState() throws IllegalStateException {
        if (getScoreboard().board.getObjective(this.objective.getName()) == null) {
            throw new IllegalStateException("Unregistered scoreboard component");
        }

        return getScoreboard();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + (this.objective != null ? this.objective.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CraftObjective other = (CraftObjective) obj;
        return !(this.objective != other.objective && (this.objective == null || !this.objective.equals(other.objective)));
    }


}
