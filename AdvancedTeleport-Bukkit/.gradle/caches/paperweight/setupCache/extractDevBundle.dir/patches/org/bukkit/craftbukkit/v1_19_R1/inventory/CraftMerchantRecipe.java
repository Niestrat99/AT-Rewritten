package org.bukkit.craftbukkit.v1_19_R1.inventory;

import com.google.common.base.Preconditions;
import java.util.List;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;

public class CraftMerchantRecipe extends MerchantRecipe {

    private final net.minecraft.world.item.trading.MerchantOffer handle;

    public CraftMerchantRecipe(net.minecraft.world.item.trading.MerchantOffer merchantRecipe) {
        super(CraftItemStack.asBukkitCopy(merchantRecipe.result), 0);
        this.handle = merchantRecipe;
        addIngredient(CraftItemStack.asBukkitCopy(merchantRecipe.baseCostA));
        addIngredient(CraftItemStack.asBukkitCopy(merchantRecipe.costB));
    }

    @Deprecated
    public CraftMerchantRecipe(ItemStack result, int uses, int maxUses, boolean experienceReward, int experience, float priceMultiplier) {
        // Paper start - add ignoreDiscounts param
        this(result, uses, maxUses, experienceReward, experience, priceMultiplier, 0, 0, false);
    }
    public CraftMerchantRecipe(ItemStack result, int uses, int maxUses, boolean experienceReward, int experience, float priceMultiplier, boolean ignoreDiscounts) {
        this(result, uses, maxUses, experienceReward, experience, priceMultiplier, 0, 0, ignoreDiscounts);
    }

    public CraftMerchantRecipe(ItemStack result, int uses, int maxUses, boolean experienceReward, int experience, float priceMultiplier, int demand, int specialPrice) {
        this(result, uses, maxUses, experienceReward, experience, priceMultiplier, demand, specialPrice, false);
    }
    public CraftMerchantRecipe(ItemStack result, int uses, int maxUses, boolean experienceReward, int experience, float priceMultiplier, int demand, int specialPrice, boolean ignoreDiscounts) {
        super(result, uses, maxUses, experienceReward, experience, priceMultiplier, demand, specialPrice, ignoreDiscounts);
        // Paper end
        this.handle = new net.minecraft.world.item.trading.MerchantOffer(
                net.minecraft.world.item.ItemStack.EMPTY,
                net.minecraft.world.item.ItemStack.EMPTY,
                CraftItemStack.asNMSCopy(result),
                uses,
                maxUses,
                experience,
                priceMultiplier,
                demand,
                ignoreDiscounts, // Paper - add ignoreDiscounts param
                this
        );
        this.setSpecialPrice(specialPrice);
        this.setExperienceReward(experienceReward);
    }

    @Override
    public int getSpecialPrice() {
        return this.handle.getSpecialPriceDiff();
    }

    @Override
    public void setSpecialPrice(int specialPrice) {
        handle.specialPriceDiff = specialPrice;
    }

    @Override
    public int getDemand() {
        return handle.demand;
    }

    @Override
    public void setDemand(int demand) {
        handle.demand = demand;
    }

    @Override
    public int getUses() {
        return handle.uses;
    }

    @Override
    public void setUses(int uses) {
        handle.uses = uses;
    }

    @Override
    public int getMaxUses() {
        return handle.maxUses;
    }

    @Override
    public void setMaxUses(int maxUses) {
        handle.maxUses = maxUses;
    }

    @Override
    public boolean hasExperienceReward() {
        return handle.rewardExp;
    }

    @Override
    public void setExperienceReward(boolean flag) {
        handle.rewardExp = flag;
    }

    @Override
    public int getVillagerExperience() {
        return handle.xp;
    }

    @Override
    public void setVillagerExperience(int villagerExperience) {
        handle.xp = villagerExperience;
    }

    @Override
    public float getPriceMultiplier() {
        return handle.priceMultiplier;
    }

    @Override
    public void setPriceMultiplier(float priceMultiplier) {
        handle.priceMultiplier = priceMultiplier;
    }

    // Paper start
    @Override
    public boolean shouldIgnoreDiscounts() {
        return this.handle.ignoreDiscounts;
    }

    @Override
    public void setIgnoreDiscounts(boolean ignoreDiscounts) {
        this.handle.ignoreDiscounts = ignoreDiscounts;
    }
    // Paper end

    public net.minecraft.world.item.trading.MerchantOffer toMinecraft() {
        List<ItemStack> ingredients = getIngredients();
        Preconditions.checkState(!ingredients.isEmpty(), "No offered ingredients");
        handle.baseCostA = CraftItemStack.asNMSCopy(ingredients.get(0));
        if (ingredients.size() > 1) {
            handle.costB = CraftItemStack.asNMSCopy(ingredients.get(1));
        }
        return this.handle;
    }

    public static CraftMerchantRecipe fromBukkit(MerchantRecipe recipe) {
        if (recipe instanceof CraftMerchantRecipe) {
            return (CraftMerchantRecipe) recipe;
        } else {
            CraftMerchantRecipe craft = new CraftMerchantRecipe(recipe.getResult(), recipe.getUses(), recipe.getMaxUses(), recipe.hasExperienceReward(), recipe.getVillagerExperience(), recipe.getPriceMultiplier(), recipe.getDemand(), recipe.getSpecialPrice(), recipe.shouldIgnoreDiscounts()); // Paper - shouldIgnoreDiscounts
            craft.setIngredients(recipe.getIngredients());

            return craft;
        }
    }
}
