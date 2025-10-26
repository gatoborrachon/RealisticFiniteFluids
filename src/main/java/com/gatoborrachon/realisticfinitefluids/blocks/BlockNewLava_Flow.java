package com.gatoborrachon.realisticfinitefluids.blocks;

import com.gatoborrachon.realisticfinitefluids.logic.FiniteFluidLogic;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;

public class BlockNewLava_Flow extends BlockNewWater_Flow {

	public BlockNewLava_Flow(String name, Fluid fluid, Material material) {
		super(name, fluid, material);
        this.setLightLevel(1.0F);
	}
	

    public static void triggerLavaMixEffects(World world, BlockPos pos) {
        // Reproduce el sonido "fizz"
        world.playSound(
            null, // player: null para que lo escuchen todos cerca
            pos.getX() + 0.5, 
            pos.getY() + 0.5, 
            pos.getZ() + 0.5,
            SoundEvents.BLOCK_FIRE_EXTINGUISH,
            SoundCategory.BLOCKS,
            0.5F,
            2.6F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.8F
        );

        // Genera partículas de humo
        for (int i = 0; i < 8; ++i) {
            double x = pos.getX() + Math.random();
            double y = pos.getY() + 1.2D;
            double z = pos.getZ() + Math.random();
            world.spawnParticle(EnumParticleTypes.SMOKE_LARGE, x, y, z, 0.0D, 0.0D, 0.0D);
        }
    }
    
    
    
    @Override
    public boolean interactWithLiquid(World world, BlockPos lavaPos, BlockPos targetPos) {

        Block lavaBlock = world.getBlockState(lavaPos).getBlock();
        Block targetBlock = world.getBlockState(targetPos).getBlock();

        int lavaMeta = lavaBlock.getMetaFromState(world.getBlockState(lavaPos));
        int targetMeta = targetBlock.getMetaFromState(world.getBlockState(targetPos));

        int lavaType = FiniteFluidLogic.GeneralPurposeLogic.getFluidIndex(lavaBlock);     
        int targetType = FiniteFluidLogic.GeneralPurposeLogic.getFluidIndex(targetBlock); 
        
        //a ver si evita un crash aal poner un bloque encima de lava
        if (lavaType < 0 || targetType < 0) return false;

        if (targetBlock == FiniteFluidLogic.liquids.get(targetType).oceanBlock) {
            targetMeta = 15;
        }

        if ("water".equals(FiniteFluidLogic.liquids.get(targetType).name)) {
            // Comparación de altura y nivel para decidir cuál reemplazar
            if (lavaPos.getY() <= targetPos.getY() && lavaMeta < targetMeta) {
                targetPos = lavaPos;
            	//System.out.println("INVERSION ALV");

            }

            if (lavaMeta > 9) {
            	//System.out.println("      ");
            	//System.out.println(world.getBlockState(targetPos).getBlock());
            	//System.out.println("LAVA META +9:"+lavaMeta);
            	//System.out.println("WATER META :"+targetMeta);

                // Condiciones para decidir qué bloque poner
                if (targetMeta < 5) {
                    world.setBlockToAir(targetPos);
                    BlockNewLava_Flow.triggerLavaMixEffects(world, targetPos);
                    return true;
                }

                if (targetMeta < 10) {
                    world.setBlockState(targetPos, Blocks.COBBLESTONE.getDefaultState());
                    BlockNewLava_Flow.triggerLavaMixEffects(world, targetPos);
                    return true;
                }

                if (targetMeta > 9) {
                    world.setBlockState(lavaPos, Blocks.OBSIDIAN.getDefaultState());
                    BlockNewLava_Flow.triggerLavaMixEffects(world, targetPos);
                    return true;
                }
                
                
            } else if (lavaMeta > 5) {
            	//System.out.println("      ");
            	//System.out.println(world.getBlockState(targetPos).getBlock());
            	//System.out.println("LAVA META +5:"+lavaMeta);
            	//System.out.println("WATER META :"+targetMeta);

                if (targetMeta < 5) {
                    world.setBlockToAir(targetPos);
                    BlockNewLava_Flow.triggerLavaMixEffects(world, targetPos);
                    return true;
                }

                if (targetMeta < 10) {
                    world.setBlockState(targetPos, Blocks.COBBLESTONE.getDefaultState());
                    BlockNewLava_Flow.triggerLavaMixEffects(world, targetPos);
                    return true;
                }

                if (targetMeta > 9) {
                    world.setBlockState(targetPos, Blocks.STONE.getDefaultState());
                    BlockNewLava_Flow.triggerLavaMixEffects(world, targetPos);
                    return true;
                }
                
                
            } else {
            	//System.out.println("      ");
            	//System.out.println(world.getBlockState(targetPos).getBlock());
            	//System.out.println("LAVA META -5:"+lavaMeta);
            	//System.out.println("WATER META :"+targetMeta);

                // Condiciones para decidir qué bloque poner
                if (targetMeta < 5) {
                    world.setBlockToAir(targetPos);
                    BlockNewLava_Flow.triggerLavaMixEffects(world, targetPos);
                    return true;
                }

                if (targetMeta < 10) {
                    world.setBlockState(targetPos, Blocks.COBBLESTONE.getDefaultState());
                    //world.setBlockToAir(lavaPos);
                    BlockNewLava_Flow.triggerLavaMixEffects(world, targetPos);
                    return true;
                }

                if (targetMeta > 9) {
                    world.setBlockToAir(lavaPos);
                    world.setBlockState(targetPos, Blocks.STONE.getDefaultState());
                    BlockNewLava_Flow.triggerLavaMixEffects(world, targetPos);
                    return true;
                }
            }
        }

        return false;
    }
    
    @Override
    public boolean shouldSearchOutward()
    {
        return false;
    }

	@Override
	public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
		super.onBlockAdded(worldIn, pos, state);
        FiniteFluidLogic.lavaFunctions.burnArea(worldIn, pos);
	}
    
    
}
