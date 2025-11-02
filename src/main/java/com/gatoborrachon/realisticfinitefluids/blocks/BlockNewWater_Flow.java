package com.gatoborrachon.realisticfinitefluids.blocks;

import java.util.Random;

import com.gatoborrachon.realisticfinitefluids.init.ModBlocks;
import com.gatoborrachon.realisticfinitefluids.init.ModConfig;
import com.gatoborrachon.realisticfinitefluids.init.ModItems;
import com.gatoborrachon.realisticfinitefluids.logic.FiniteFluidLogic;
import com.gatoborrachon.realisticfinitefluids.logic.NewFluidType;

import git.jbredwards.fluidlogged_api.api.util.FluidState;
import git.jbredwards.fluidlogged_api.api.util.FluidloggedUtils;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;

public class BlockNewWater_Flow extends BlockFiniteFluid 
{
	public BlockNewWater_Flow(String name, Fluid fluid, Material material)
    {
        super(name, fluid, material);
		setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
		ModBlocks.BLOCKS.add(this);
		ModItems.ITEMS.add(new ItemBlock(this).setRegistryName(this.getRegistryName()));
        this.setDefaultState(this.blockState.getBaseState().withProperty(LEVEL, Integer.valueOf(15)));

        this.setTickRandomly(FiniteFluidLogic.shouldTickRandomly);
    }


    /**
     * Ticks the block if it's been scheduled
     */
    @Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
        if (!world.isRemote)
        {
        	FiniteFluidLogic.GeneralPurposeLogic.setCurrentFluidIndex(this);

            IBlockState currentaState = world.getBlockState(pos);
            Block currentBlock = currentaState.getBlock();
            Block downBlock = world.getBlockState(pos.down()).getBlock();
            
            //Aca yo controlo lo de interaccion de flowing con ocean xd
            //Avoid too much block updates over oceanic liquid
            /*System.out.println("DOWN BLOCK "+downBlock);
            System.out.println("CURRENT BLOCK"+currentBlock);
            System.out.println("WORLD "+world);
            System.out.println("POS.DOWN"+pos.down());
            System.out.println("Fluid index "+FiniteFluidLogic.GeneralPurposeLogic.getFluidIndex(currentBlock, world, pos.down()));
            */
            //System.out.println("Ocean Block"+FiniteFluidLogic.liquids.get(FiniteFluidLogic.GeneralPurposeLogic.getFluidIndex(currentBlock, world, pos.down())).oceanBlock);
            int temporalIndex = FiniteFluidLogic.GeneralPurposeLogic.getFluidIndex(currentBlock, world, pos.down());
            if (temporalIndex != -1 
            		&& downBlock == FiniteFluidLogic.liquids.get(temporalIndex).oceanBlock 
            		&& currentaState.getValue(BlockFiniteFluid.LEVEL) < 4) { //8
            	int newValue = world.getBlockState(pos).getValue(BlockFiniteFluid.LEVEL)/2; //3
            	world.setBlockState(pos, currentaState.withProperty(BlockFiniteFluid.LEVEL, newValue));
            }
            
            //Wake nearby ocean blocks
            FiniteFluidLogic.InfiniteWaterSource.wakeOcean(world, pos);

            //Control how many calculations are done. If we are above the limit, just schedule for the next tick
            if (FiniteFluidLogic.GeneralPurposeLogic.getCalc() > FiniteFluidLogic.GeneralPurposeLogic.getMaxCalc())
            {

            	world.scheduleUpdate(pos, this, this.tickRate(world));
            }
            //If we are below the calculation limit:
            else
            {
                Block belowBlock1 = world.getBlockState(new BlockPos(pos.getX(), pos.getY() - 1 * FiniteFluidLogic.GeneralPurposeLogic.getFluidGravity(), pos.getZ())).getBlock();

                if (belowBlock1 != Blocks.AIR & !FiniteFluidLogic.GeneralPurposeLogic.hasSideBorder(world, pos, Blocks.AIR))
                {
                    if ((float)FiniteFluidLogic.GeneralPurposeLogic.getCalc() > (float)FiniteFluidLogic.GeneralPurposeLogic.getMaxCalc() * 0.65F && world.getBlockState(pos).getValue(LEVEL) < 4 & (!FiniteFluidLogic.GeneralPurposeLogic.isRealisticFluid(belowBlock1) || world.getBlockState(new BlockPos(pos.getX(), pos.getY() - 1 * FiniteFluidLogic.GeneralPurposeLogic.getFluidGravity(), pos.getZ())).getValue(LEVEL) < 15))
                    {
                    	world.scheduleUpdate(pos, this, this.tickRate(world));
                        return;
                    }

                    if ((float)FiniteFluidLogic.GeneralPurposeLogic.getCalc() > (float)FiniteFluidLogic.GeneralPurposeLogic.getMaxCalc() * 0.5F)
                    {
                        EntityPlayer var7 = world.getClosestPlayer((double)pos.getX(), (double)pos.getY(), (double)pos.getZ(), FiniteFluidLogic.GeneralPurposeLogic.getPlayerDistanceToCalc(), true);

                        if (var7 == null)
                        {
                        	world.scheduleUpdate(pos, this, this.tickRate(world));
                            return;
                        }
                    }
                }

                FiniteFluidLogic.GeneralPurposeLogic.addCalc();


                if (world.getBlockState(new BlockPos(pos.getX(), pos.getY() - 1 * FiniteFluidLogic.GeneralPurposeLogic.getFluidGravity(), pos.getZ())).getBlock() == Blocks.DIAMOND_BLOCK)
                {
                    IBlockState currentState = world.getBlockState(pos);
                    IBlockState newState = currentState.withProperty(BlockFiniteFluid.LEVEL, 15);
                    if (currentState.getValue(BlockFiniteFluid.LEVEL) < 15) {
                    world.setBlockState(pos, newState, 3);
                    }
                    world.setBlockState(pos.up(), newState, 3);
                    
                	FiniteFluidLogic.GeneralPurposeLogic.tryMove(world, pos.up());
                    world.scheduleUpdate(pos, this, this.tickRate(world));
                }
                else if (!FiniteFluidLogic.GeneralPurposeLogic.checkForNeighborLiquid(world, pos))
                {
                	int newLevel = 0;
                	//FluidLogged API Compat
                	IBlockState newCurrentBlockState = world.getBlockState(pos);
                	if (newCurrentBlockState.getBlock() instanceof BlockFiniteFluid) {
                		newLevel = world.getBlockState(pos).getValue(LEVEL);
                	} else {
                    	FluidState fluidState = FluidloggedUtils.getFluidState(world, pos);
                    	if (fluidState.getBlock() instanceof BlockFiniteFluid)
                    		newLevel = fluidState.getState().getValue(LEVEL);
                	}
                	//////
                		
                    if (FiniteFluidLogic.GeneralPurposeLogic.tryMove(world, pos))
                    {
                    	world.scheduleUpdate(pos, this, this.tickRate(world));
                    }
                    else
                    {
                    	FiniteFluidLogic.GeneralPurposeLogic.setCurrentFluidIndex(this);
                    	//int currentFluidIndex = FiniteFluidLogic.GeneralPurposeLogic.getFluidIndex(this);

                    	Block stillBlock = ((NewFluidType) FiniteFluidLogic.liquids.get(FiniteFluidLogic.onFiniteFluidIndex)).stillBlock;
                    	IBlockState newState = stillBlock.getDefaultState().withProperty(BlockFiniteFluid.LEVEL, newLevel);
                    	//FluidLogged API Compat
                    	Block newBlockToCheck = FluidloggedUtils.getFluidState(world, pos).getBlock();
                    	if (newCurrentBlockState.getBlock() instanceof BlockFiniteFluid) {
                        	world.setBlockState(pos, newState, 3);
                    	} else if (newBlockToCheck instanceof BlockFiniteFluid) {
                    	    //FluidState newFluidState = new FluidState(((BlockFiniteFluid)newBlockToCheck).getFluid(), newState);
                    	    //FluidState newFluidState = FluidloggedUtils.
                    	    
                    	    FluidState newFluidState = FluidState.of(newState);

                    	    
                	        FluidloggedUtils.setFluidState(world, pos, newState, newFluidState, false);

                    	}
                    	////////
                    	
                    	BlockPos belowBlock = new BlockPos(pos.getX(), pos.getY() - 1 * FiniteFluidLogic.GeneralPurposeLogic.getFluidGravity(), pos.getZ());
                    	if (world.getBlockState(pos).getMaterial() == world.getBlockState(belowBlock).getMaterial()) {
                            Random random = new Random();
                            
                            if (random.nextInt(60) == 0) {
                            	FiniteFluidLogic.InfiniteWaterSource.borderOceanCheck(world, belowBlock, 1);
                            }
                    	} else if (world.getBlockState(pos).getMaterial() != world.getBlockState(belowBlock).getMaterial()) {
                        	FiniteFluidLogic.InfiniteWaterSource.borderOceanCheck(world, belowBlock, 1);                		
                    	}
                    }
                }
            }
        }
		super.updateTick(world, pos, state, rand);
    }
    
    
	@Override
    public void onEntityCollidedWithBlock(World worldIn, BlockPos pos, IBlockState state, Entity entityIn) {
		if (entityIn instanceof EntityPlayer && ((EntityPlayer)entityIn).isCreative() && !ModConfig.flowingWaterShouldMoveCreativePlayer /*&& state.getMaterial() == Material.WATER*/) return;
		
		//Vec3d flow = calculateFlowVector(worldIn, pos);
        Vec3d flow = Vec3d.ZERO;
		
        double strength = 0.014D;
        entityIn.motionX += -flow.x * strength;
        entityIn.motionY += -flow.y * strength;
        entityIn.motionZ += -flow.z * strength;
    }
	

    @Override
    public boolean interactWithLiquid(World world, BlockPos waterPos, BlockPos targetPos)
    {
        IBlockState waterState = world.getBlockState(waterPos);
        IBlockState targetState = world.getBlockState(targetPos);
        
        if (targetState.getMaterial() != Material.LAVA) return false;
        
        Block waterBlock = waterState.getBlock();
        Block targetBlock = targetState.getBlock();

        int waterMeta = waterBlock.getMetaFromState(waterState);
        int targetMeta = targetBlock.getMetaFromState(targetState);

        int waterType = FiniteFluidLogic.GeneralPurposeLogic.getFluidIndex(waterBlock, world, waterPos);     
        int targetType = FiniteFluidLogic.GeneralPurposeLogic.getFluidIndex(targetBlock, world, targetPos);
        
        if (waterType < 0 || targetType < 0) return false;
        
        if (targetBlock instanceof BlockFiniteFluid) {
            if (waterMeta > 9) {
            	
                if (targetMeta < 5) {
                    //world.setBlockToAir(waterPos);
                    world.setBlockState(targetPos, Blocks.COBBLESTONE.getDefaultState());
                    BlockNewLava_Flow.triggerLavaMixEffects(world, targetPos);
                    return true;
                }

                if (targetMeta < 10) {
                    world.setBlockState(targetPos, Blocks.STONE.getDefaultState());
                    //world.setBlockToAir(waterPos);
                    BlockNewLava_Flow.triggerLavaMixEffects(world, targetPos);
                    return true;
                }

                if (targetMeta > 9) {
                    world.setBlockToAir(waterPos);
                    world.setBlockState(targetPos, Blocks.OBSIDIAN.getDefaultState());
                    BlockNewLava_Flow.triggerLavaMixEffects(world, targetPos);
                    return true;
                }

            } else if (waterMeta > 5) {
                if (targetMeta < 6) {
                    world.setBlockState(targetPos, Blocks.STONE.getDefaultState());
                    //world.setBlockToAir(waterPos);
                    BlockNewLava_Flow.triggerLavaMixEffects(world, targetPos);
                    return true;
                }

                if (targetMeta > 5) {
                    //world.setBlockToAir(waterPos);
                    world.setBlockState(targetPos, Blocks.COBBLESTONE.getDefaultState());
                    BlockNewLava_Flow.triggerLavaMixEffects(world, targetPos);
                    return true;
                }
                if (targetMeta > 10) {
                    world.setBlockToAir(waterPos);
                    BlockNewLava_Flow.triggerLavaMixEffects(world, targetPos);
                    return true;
                }

            } else {
                world.setBlockToAir(waterPos);
                BlockNewLava_Flow.triggerLavaMixEffects(world, targetPos);
                return true;
            }

            
        } else {
        	//vanilla water?
        }
        

    	
        return false;    
        }


}
