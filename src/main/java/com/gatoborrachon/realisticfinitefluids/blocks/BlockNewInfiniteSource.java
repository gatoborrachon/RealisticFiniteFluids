package com.gatoborrachon.realisticfinitefluids.blocks;

import java.util.Random;

import com.gatoborrachon.realisticfinitefluids.init.ModBlocks;
import com.gatoborrachon.realisticfinitefluids.init.ModConfig;
import com.gatoborrachon.realisticfinitefluids.init.ModItems;
import com.gatoborrachon.realisticfinitefluids.logic.FiniteFluidLogic;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

public class BlockNewInfiniteSource extends BlockFiniteFluid //Former OceanBlock
{
	public BlockNewInfiniteSource(String name, Fluid fluid, Material material)
    {
        super(name, fluid, material);
		setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
		ModBlocks.BLOCKS.add(this);
		ModItems.ITEMS.add(new ItemBlock(this).setRegistryName(this.getRegistryName()));
        this.setDefaultState(this.blockState.getBaseState().withProperty(LEVEL, Integer.valueOf(15)));
        //setResistance(6000F);

        //ESTO NO ESTABA ACA, NO SE QUE PUEDA PROVOCAR
        this.setTickRandomly(FiniteFluidLogic.shouldTickRandomly);



    }
    
    @Override
    public boolean allowTransfer()
    {
        return false;
    }

    @Override
	public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
    	//Empty, don't know why
    	//if (worldIn.isPopulating(worldIn.getChunkFromBlockCoords(pos).x, worldIn.getChunkFromBlockCoords(pos).z)) return;

	}


	/**
     * Ticks the block if it's been scheduled
     */
    @Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
    	
    	if (world.isRemote) return;
    	
        if (FiniteFluidLogic.GeneralPurposeLogic.getCalc() > FiniteFluidLogic.GeneralPurposeLogic.getMaxCalc())
        {
        	world.scheduleUpdate(pos, this, this.tickRate(world));
        }
        else
        {
            if (!world.isAirBlock(pos.down()) && (float)FiniteFluidLogic.GeneralPurposeLogic.getCalc() > (float)FiniteFluidLogic.GeneralPurposeLogic.getMaxCalc() * 0.55F)
            {
                EntityPlayer player = world.getClosestPlayer(pos.getX(), pos.getY(), pos.getZ(), 16.0D, true); //16 --> Maxima distancia del jugador para calcular el movimiento del agua

                if (player == null)
                {
                	world.scheduleUpdate(pos, this, this.tickRate(world));
                    return;
                }
            }

            FiniteFluidLogic.GeneralPurposeLogic.addCalc();
            FiniteFluidLogic.GeneralPurposeLogic.setCurrentFluidIndex(this, world, pos);

            if (FiniteFluidLogic.InfiniteWaterSource.tryOceanMove(world, pos))
            {
            	world.scheduleUpdate(pos, this, this.tickRate(world));
            }
        }		super.updateTick(world, pos, state, rand);
        
	}

	@Override
	public void randomTick(World worldIn, BlockPos pos, IBlockState state, Random random) {
    	//Freeze blocks, if the block freezes, stop all the logic
		Block blockToCheck = worldIn.getBlockState(pos).getBlock();
		if (!(blockToCheck instanceof BlockFiniteFluid)) return;
		if (((BlockFiniteFluid)blockToCheck).getFluid() == FluidRegistry.WATER && ModConfig.waterCanFreeze && BlockFiniteFluid.tryFreezeWater(worldIn, pos, state, random)) return;
		//super.randomTick(worldIn, pos, state, random);
	}


    
    

}
