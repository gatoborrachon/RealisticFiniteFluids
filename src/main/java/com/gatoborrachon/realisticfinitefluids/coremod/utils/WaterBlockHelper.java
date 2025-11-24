package com.gatoborrachon.realisticfinitefluids.coremod.utils;

import com.gatoborrachon.realisticfinitefluids.blocks.BlockFiniteFluid;
import com.gatoborrachon.realisticfinitefluids.init.ModBlocks;

public class WaterBlockHelper {
    public static net.minecraft.block.Block getCustomWaterBlock() {
        return ModBlocks.FINITE_WATER_STILL.getDefaultState().withProperty(BlockFiniteFluid.LEVEL, BlockFiniteFluid.MAXIMUM_CONCEPTUAL_LEVEL).getBlock(); //ModBlocks.INFINITE_WATER_SOURCE;
    }
    public static net.minecraft.block.state.IBlockState getCustomWater() {
        return ModBlocks.FINITE_WATER_STILL.getDefaultState().withProperty(BlockFiniteFluid.LEVEL, BlockFiniteFluid.MAXIMUM_CONCEPTUAL_LEVEL); //ModBlocks.INFINITE_WATER_SOURCE.getDefaultState();
    }
}