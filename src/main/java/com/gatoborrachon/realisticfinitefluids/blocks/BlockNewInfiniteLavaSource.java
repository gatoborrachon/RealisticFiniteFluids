package com.gatoborrachon.realisticfinitefluids.blocks;

import net.minecraft.block.material.Material;
import net.minecraftforge.fluids.Fluid;

public class BlockNewInfiniteLavaSource extends BlockNewInfiniteSource {

	public BlockNewInfiniteLavaSource(String name, Fluid fluid, Material material) {
		super(name, fluid, material);
        this.setLightLevel(1.0F);
	}

}
