/**
 * Automatically generated file, changes will be lost.
 */
package org.bukkit.craftbukkit.v1_19_R1.block.impl;

public final class CraftCandle extends org.bukkit.craftbukkit.v1_19_R1.block.data.CraftBlockData implements org.bukkit.block.data.type.Candle, org.bukkit.block.data.Lightable, org.bukkit.block.data.Waterlogged {

    public CraftCandle() {
        super();
    }

    public CraftCandle(net.minecraft.world.level.block.state.BlockState state) {
        super(state);
    }

    // org.bukkit.craftbukkit.v1_19_R1.block.data.type.CraftCandle

    private static final net.minecraft.world.level.block.state.properties.IntegerProperty CANDLES = getInteger(net.minecraft.world.level.block.CandleBlock.class, "candles");

    @Override
    public int getCandles() {
        return get(CraftCandle.CANDLES);
    }

    @Override
    public void setCandles(int candles) {
        set(CraftCandle.CANDLES, candles);
    }

    @Override
    public int getMaximumCandles() {
        return getMax(CraftCandle.CANDLES);
    }
    // Paper start
    @Override
    public int getMinimumCandles() {
        return getMin(CraftCandle.CANDLES);
    }
    // Paper end

    // org.bukkit.craftbukkit.v1_19_R1.block.data.CraftLightable

    private static final net.minecraft.world.level.block.state.properties.BooleanProperty LIT = getBoolean(net.minecraft.world.level.block.CandleBlock.class, "lit");

    @Override
    public boolean isLit() {
        return get(CraftCandle.LIT);
    }

    @Override
    public void setLit(boolean lit) {
        set(CraftCandle.LIT, lit);
    }

    // org.bukkit.craftbukkit.v1_19_R1.block.data.CraftWaterlogged

    private static final net.minecraft.world.level.block.state.properties.BooleanProperty WATERLOGGED = getBoolean(net.minecraft.world.level.block.CandleBlock.class, "waterlogged");

    @Override
    public boolean isWaterlogged() {
        return get(CraftCandle.WATERLOGGED);
    }

    @Override
    public void setWaterlogged(boolean waterlogged) {
        set(CraftCandle.WATERLOGGED, waterlogged);
    }
}
