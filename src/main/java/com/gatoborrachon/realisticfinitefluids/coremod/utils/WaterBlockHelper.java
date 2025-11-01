package com.gatoborrachon.realisticfinitefluids.coremod.utils;

import com.gatoborrachon.realisticfinitefluids.init.ModBlocks;

public class WaterBlockHelper {
    public static net.minecraft.block.Block getCustomWaterBlock() {
        return ModBlocks.INFINITE_WATER_SOURCE;
    }
    public static net.minecraft.block.state.IBlockState getCustomWater() {
        return ModBlocks.INFINITE_WATER_SOURCE.getDefaultState();
    }
}