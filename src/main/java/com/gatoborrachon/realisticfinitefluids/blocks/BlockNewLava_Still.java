package com.gatoborrachon.realisticfinitefluids.blocks;

import com.gatoborrachon.realisticfinitefluids.logic.FiniteFluidLogic;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;

public class BlockNewLava_Still extends BlockNewWater_Still {

	public BlockNewLava_Still(String name, Fluid fluid, Material material) {
		super(name, fluid, material);
        this.setLightLevel(1.0F);
	}
    
	@Override
	public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
		super.onBlockAdded(worldIn, pos, state);
        FiniteFluidLogic.lavaFunctions.burnArea(worldIn, pos);
	}
	
	
	@Override
	public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
	    super.neighborChanged(state, worldIn, pos, blockIn, fromPos);
        for (EnumFacing dir : EnumFacing.VALUES) {
            BlockPos neighborAir = pos.offset(dir);
            IBlockState neighborAirState = worldIn.getBlockState(neighborAir);
            if (neighborAirState.getMaterial() == Material.WATER) {
        	    FiniteFluidLogic.GeneralPurposeLogic.checkForNeighborLiquid(worldIn, pos);
                //break;
            }
        }
	}


	@Override
	public boolean shouldEvap(World world, BlockPos pos) {
		return false;
	}
	
	
}
