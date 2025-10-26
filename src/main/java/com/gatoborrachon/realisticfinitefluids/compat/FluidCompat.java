package com.gatoborrachon.realisticfinitefluids.compat;

import com.gatoborrachon.realisticfinitefluids.init.ModBlocks;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.IFluidBlock;

public class FluidCompat {
    public static boolean isWaterLike(Block block) {
    	if (block instanceof IFluidBlock) {
            return ((IFluidBlock)block).getFluid() == FluidRegistry.WATER;
    	} else {
    		return block == Blocks.WATER || block == Blocks.FLOWING_WATER;
    	}
    }
    
	public static boolean isLavaLike(Block block) {
    	if (block instanceof IFluidBlock) {
            return ((IFluidBlock)block).getFluid() == FluidRegistry.LAVA;
    	} else {
    		return block == Blocks.LAVA || block == Blocks.FLOWING_LAVA;
    	}
    }
	
	public static Block redirectWaterFlowing() {
	    return ModBlocks.FINITE_WATER_FLOWING;
	}

	public static Block redirectWaterStill() {
	    return ModBlocks.FINITE_WATER_STILL;
	}
	
	public static Block redirectLavaFlowing() {
	    return ModBlocks.FINITE_LAVA_FLOWING;
	}

	public static Block redirectLavaStill() {
	    return ModBlocks.FINITE_LAVA_STILL;
	}	
	
}
