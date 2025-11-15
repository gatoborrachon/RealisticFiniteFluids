package com.gatoborrachon.realisticfinitefluids.init;

import java.util.ArrayList;
import java.util.List;

import com.gatoborrachon.realisticfinitefluids.blocks.BlockNewInfiniteLavaSource;
import com.gatoborrachon.realisticfinitefluids.blocks.BlockNewInfiniteSource;
import com.gatoborrachon.realisticfinitefluids.blocks.BlockNewLava_Flow;
import com.gatoborrachon.realisticfinitefluids.blocks.BlockNewLava_Still;
import com.gatoborrachon.realisticfinitefluids.blocks.BlockNewWater_Flow;
import com.gatoborrachon.realisticfinitefluids.blocks.BlockNewWater_Still;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraftforge.fluids.FluidRegistry;

public class ModBlocks {

    public static final List<Block> BLOCKS = new ArrayList<>();

    public static Block FINITE_WATER_FLOWING;
    public static Block FINITE_WATER_STILL;
    public static Block INFINITE_WATER_SOURCE;
    public static Block FINITE_LAVA_FLOWING;
    public static Block FINITE_LAVA_STILL;
    public static Block INFINITE_LAVA_SOURCE;

    public static void init() {
        FINITE_WATER_FLOWING = new BlockNewWater_Flow("finite_water_flowing", FluidRegistry.WATER, Material.WATER);
        FINITE_WATER_STILL   = new BlockNewWater_Still("finite_water_still", FluidRegistry.WATER, Material.WATER);
        INFINITE_WATER_SOURCE = new BlockNewInfiniteSource("infinite_water_source", FluidRegistry.WATER, Material.WATER);

        FINITE_LAVA_FLOWING  = new BlockNewLava_Flow("finite_lava_flowing", FluidRegistry.LAVA, Material.LAVA);
        FINITE_LAVA_STILL    = new BlockNewLava_Still("finite_lava_still", FluidRegistry.LAVA, Material.LAVA);
        INFINITE_LAVA_SOURCE = new BlockNewInfiniteLavaSource("infinite_lava_source", FluidRegistry.LAVA, Material.LAVA);

    }
}
