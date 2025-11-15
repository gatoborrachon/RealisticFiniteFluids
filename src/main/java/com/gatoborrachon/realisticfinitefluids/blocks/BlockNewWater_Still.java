package com.gatoborrachon.realisticfinitefluids.blocks;


import java.util.Random;

import com.gatoborrachon.realisticfinitefluids.init.ModBlocks;
import com.gatoborrachon.realisticfinitefluids.init.ModConfig;
import com.gatoborrachon.realisticfinitefluids.init.ModItems;
import com.gatoborrachon.realisticfinitefluids.logic.FiniteFluidLogic;
import com.gatoborrachon.realisticfinitefluids.logic.NewFluidType;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

public class BlockNewWater_Still extends BlockFiniteFluid
{
	public BlockNewWater_Still(String name, Fluid fluid, Material material)
    {
        super(name, fluid, material);
		setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
		ModBlocks.BLOCKS.add(this);
		ModItems.ITEMS.add(new ItemBlock(this).setRegistryName(this.getRegistryName()));
        this.setDefaultState(this.blockState.getBaseState().withProperty(LEVEL, Integer.valueOf(15)));

        this.setTickRandomly(FiniteFluidLogic.shouldTickRandomly);
    }


    /**
     * Called whenever the block is added into the world. Args: world, x, y, z
     */
	@Override
	public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
        if (!world.isRemote)
        {        	

        	FiniteFluidLogic.GeneralPurposeLogic.setCurrentFluidIndex(this);
        	//int currentFluidIndex = FiniteFluidLogic.GeneralPurposeLogic.getFluidIndex(this);
        	NewFluidType fluid = FiniteFluidLogic.liquids.get(FiniteFluidLogic.onFiniteFluidIndex);

            if (world.getBlockState(new BlockPos(pos.getX(), pos.getY() - 1 * FiniteFluidLogic.GeneralPurposeLogic.getFluidGravity(), pos.getZ())) == ((NewFluidType)FiniteFluidLogic.liquids.get(FiniteFluidLogic.onFiniteFluidIndex)).oceanBlock & !FiniteFluidLogic.GeneralPurposeLogic.hasSideBorder(world, pos, ((NewFluidType)FiniteFluidLogic.liquids.get(FiniteFluidLogic.onFiniteFluidIndex)).oceanBlock))
            {
                if (FiniteFluidLogic.GeneralPurposeLogic.getCalc() < FiniteFluidLogic.GeneralPurposeLogic.getMaxCalc() / 2)
                {
                	BlockPos belowBlock = new BlockPos(pos.getX(), pos.getY() - 1 * FiniteFluidLogic.GeneralPurposeLogic.getFluidGravity(), pos.getZ());
                	if (world.getBlockState(pos).getMaterial() == world.getBlockState(belowBlock).getMaterial()) {
                    Random random = new Random();
                    
                    if (random.nextInt(60) == 0)
                    {
                    	FiniteFluidLogic.InfiniteWaterSource.borderOceanCheck(world, belowBlock, 1);
                    }
                	} else if (world.getBlockState(pos).getMaterial() != world.getBlockState(belowBlock).getMaterial()) {
                    	FiniteFluidLogic.InfiniteWaterSource.borderOceanCheck(world, belowBlock, 1);                		
                	}
                }

                FiniteFluidLogic.GeneralPurposeLogic.tryGrab(world, pos, pos, 0, fluid);
            }
        }
		super.onBlockAdded(world, pos, state);

	}



    /**
     * Ticks the block if it's been scheduled
     */
    @Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {

    	if (!world.isRemote)
        {
        	//Freeze blocks, if the block freezes, stop all the logic
    		if (ModConfig.waterCanFreeze && BlockFiniteFluid.tryFreezeWater(world, pos, state, rand)) return;

        	
    		//En mi mente enferma penso en que, este bloque deberia quedarse asi si ya no tiene a donde moverse o si tiene 0 de agua
    		//Si un bloque intenta meterle agua (tryGrab) pues revive y se convierte en flow y vuelve a ejecutar sus tareas dde flow (ecualizacion hirozntal)
            int newLevel = BlockFiniteFluid.getVolume(world, pos, world.getBlockState(pos)); //world.getBlockState(pos).getValue(LEVEL);
            FiniteFluidLogic.GeneralPurposeLogic.setCurrentFluidIndex(this);

            if (FiniteFluidLogic.GeneralPurposeLogic.canMove(world, pos, newLevel))
            {
            	FiniteFluidLogic.GeneralPurposeLogic.setCurrentFluidIndex(this);
            	//int currentFluidIndex = FiniteFluidLogic.GeneralPurposeLogic.getFluidIndex(this);
            	Block flowingBlock = ((NewFluidType) FiniteFluidLogic.liquids.get(FiniteFluidLogic.onFiniteFluidIndex)).flowingBlock;

            	BlockFiniteFluid.setBlockState(world, pos, BlockFiniteFluid.setVolume(null, null, flowingBlock.getDefaultState(), newLevel));
            	//IBlockState newState = flowingBlock.getDefaultState().withProperty(BlockFiniteFluid.LEVEL, newLevel);
            	//world.setBlockState(pos, newState, 3);
            	
            	//world.setBlock(var2, var3, var4, ((NewFluidType)FiniteFluidLogic.liquids.get(FiniteFluidLogic.onWaterType)).flow, var6, 3);
            }
            else
            {
                if (shouldEvap(world, pos))
                {
                    if (rand.nextInt(FiniteFluidLogic.evaporationChance) == 0)
                    {
                    	world.setBlockToAir(pos);
                        return;
                    }

                    if (FiniteFluidLogic.enableEvaporation)
                    {
                    	world.scheduleUpdate(pos, this, this.tickRate(world));
                    }
                }
            	
                FiniteFluidLogic.GeneralPurposeLogic.setCurrentFluidIndex(this);
                BlockPos below = pos.down();
                IBlockState stateBelow = world.getBlockState(below);
                
                if (FiniteFluidLogic.GeneralPurposeLogic.checkForNeighborLiquid(world, pos)){
                    return;
                    //Aca yo controlo lo de interaccion de still con water xd
                }  else if (stateBelow.getBlock() == ModBlocks.INFINITE_WATER_SOURCE && BlockFiniteFluid.getVolume(world, pos, world.getBlockState(pos)) < 2) {
                    // Este bloque es "absorbido" por el océano
                    world.setBlockToAir(pos);  // O reemplaza por aire
                }
            }
            
        }
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
