package org.bukkit.craftbukkit.v1_19_R1.inventory;

import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import org.apache.commons.lang.Validate;
import org.bukkit.craftbukkit.v1_19_R1.util.CraftChatMessage;

public class CraftMerchantCustom extends CraftMerchant {

    @Deprecated // Paper - Adventure
    public CraftMerchantCustom(String title) {
        super(new MinecraftMerchant(title));
        this.getMerchant().craftMerchant = this;
    }
    // Paper start
    public CraftMerchantCustom(net.kyori.adventure.text.Component title) {
        super(new MinecraftMerchant(title));
        getMerchant().craftMerchant = this;
    }
    // Paper end

    @Override
    public String toString() {
        return "CraftMerchantCustom";
    }

    @Override
    public MinecraftMerchant getMerchant() {
        return (MinecraftMerchant) super.getMerchant();
    }

    public static class MinecraftMerchant implements Merchant {

        private final Component title;
        private final MerchantOffers trades = new MerchantOffers();
        private Player tradingPlayer;
        protected CraftMerchant craftMerchant;

        @Deprecated // Paper - Adventure
        public MinecraftMerchant(String title) {
            Validate.notNull(title, "Title cannot be null");
            this.title = CraftChatMessage.fromString(title)[0];
        }
        // Paper start
        public MinecraftMerchant(net.kyori.adventure.text.Component title) {
            Validate.notNull(title, "Title cannot be null");
            this.title = io.papermc.paper.adventure.PaperAdventure.asVanilla(title);
        }
        // Paper end

        @Override
        public CraftMerchant getCraftMerchant() {
            return this.craftMerchant;
        }

        @Override
        public void setTradingPlayer(Player customer) {
            this.tradingPlayer = customer;
        }

        @Override
        public Player getTradingPlayer() {
            return this.tradingPlayer;
        }

        @Override
        public MerchantOffers getOffers() {
            return this.trades;
        }

        // Paper start
        @Override
        public void processTrade(MerchantOffer merchantRecipe, @javax.annotation.Nullable io.papermc.paper.event.player.PlayerPurchaseEvent event) { // The MerchantRecipe passed in here is the one set by the PlayerPurchaseEvent
            /** Based on {@link net.minecraft.world.entity.npc.AbstractVillager#processTrade(MerchantOffer, io.papermc.paper.event.player.PlayerPurchaseEvent)} */
            if (getTradingPlayer() instanceof net.minecraft.server.level.ServerPlayer) {
                if (event == null || event.willIncreaseTradeUses()) {
                    merchantRecipe.increaseUses();
                }
                if (event == null || event.isRewardingExp()) {
                    this.tradingPlayer.level.addFreshEntity(new net.minecraft.world.entity.ExperienceOrb(tradingPlayer.level, tradingPlayer.getX(), tradingPlayer.getY(), tradingPlayer.getZ(), merchantRecipe.getXp(), org.bukkit.entity.ExperienceOrb.SpawnReason.VILLAGER_TRADE, this.tradingPlayer, null));
                }
            }
            this.notifyTrade(merchantRecipe);
        }
        // Paper end
        @Override
        public void notifyTrade(MerchantOffer offer) {
            // increase recipe's uses
            // offer.increaseUses(); // Paper - handled above in processTrade
        }

        @Override
        public void notifyTradeUpdated(ItemStack stack) {
        }

        public Component getScoreboardDisplayName() {
            return this.title;
        }

        @Override
        public int getVillagerXp() {
            return 0; // xp
        }

        @Override
        public void overrideXp(int experience) {
        }

        @Override
        public boolean showProgressBar() {
            return false; // is-regular-villager flag (hides some gui elements: xp bar, name suffix)
        }

        @Override
        public SoundEvent getNotifyTradeSound() {
            return SoundEvents.VILLAGER_YES;
        }

        @Override
        public void overrideOffers(MerchantOffers offers) {
        }

        @Override
        public boolean isClientSide() {
            return false;
        }
    }
}
