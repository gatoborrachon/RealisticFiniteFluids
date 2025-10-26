package com.gatoborrachon.realisticfinitefluids.blocks;

import java.util.EnumMap;
import java.util.Map;
import java.util.Random;

import com.gatoborrachon.realisticfinitefluids.blocks.properties.UnlistedPropertyFlowDirection;
import com.gatoborrachon.realisticfinitefluids.init.ModBlocks;
import com.gatoborrachon.realisticfinitefluids.init.ModConfig;
import com.gatoborrachon.realisticfinitefluids.init.ModItems;
import com.gatoborrachon.realisticfinitefluids.logic.FiniteFluidLogic;
import com.gatoborrachon.realisticfinitefluids.logic.NewFluidType;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fluids.Fluid;

public class BlockNewWater extends BlockFiniteFluid 
{
	public BlockNewWater(String name, Fluid fluid, Material material)
    {
        super(name, fluid, material);
		setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
		ModBlocks.BLOCKS.add(this);
		ModItems.ITEMS.add(new ItemBlock(this).setRegistryName(this.getRegistryName()));
        this.setDefaultState(this.blockState.getBaseState().withProperty(LEVEL, Integer.valueOf(15)));

        if (FiniteFluidLogic.shouldTickRandomly)
        {
            this.setTickRandomly(true);
        }
    }


    // =========================
    // Properties Handling
    // ========================= 
    public static final IUnlistedProperty<Vec3d> FLOW_DIRECTION = new UnlistedPropertyFlowDirection("flow_direction");
    
    @Override
    protected BlockStateContainer createBlockState() {
        return new ExtendedBlockState(this,
        	new IProperty[] { LEVEL }, //Listed Properties
            new IUnlistedProperty<?>[] { //Unlisted Properties
        		NEIGHBOR_STATES,
                HEIGHT_NW,
                HEIGHT_NE,
                HEIGHT_SW,
                HEIGHT_SE,
                FLUID_COLOR,
                FLOW_DIRECTION
            }
        );
    }
    
    @Override
    public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
        if (state instanceof IExtendedBlockState) {
            IExtendedBlockState extendedState = (IExtendedBlockState) state;

            float h00 = FiniteFluidLogic.GeneralPurposeLogic.getHeight(world, pos, -1, -1);
            float h10 = FiniteFluidLogic.GeneralPurposeLogic.getHeight(world, pos, 1, -1);
            float h01 = FiniteFluidLogic.GeneralPurposeLogic.getHeight(world, pos, -1, 1);
            float h11 = FiniteFluidLogic.GeneralPurposeLogic.getHeight(world, pos, 1, 1);
            
            Map<EnumFacing, IBlockState> neighborStates = new EnumMap<>(EnumFacing.class);
            for (EnumFacing face : EnumFacing.values()) {
                BlockPos neighborPos = pos.offset(face);
                IBlockState neighborState = world.getBlockState(neighborPos);
                neighborStates.put(face, neighborState);
            }
            
            int color = Minecraft.getMinecraft().getBlockColors().colorMultiplier(state, world, pos, 0);

            Vec3d flowDirection = calculateFlowVector(world, pos);
            
            extendedState = extendedState
               	.withProperty(NEIGHBOR_STATES, neighborStates)
                .withProperty(HEIGHT_NW, h00)
                .withProperty(HEIGHT_NE, h10)
                .withProperty(HEIGHT_SW, h01)
                .withProperty(HEIGHT_SE, h11)
                .withProperty(FLUID_COLOR, color)
                .withProperty(FLOW_DIRECTION, flowDirection);
            return extendedState; 
        }
        return state;
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
            if (downBlock == FiniteFluidLogic.liquids.get(FiniteFluidLogic.GeneralPurposeLogic.getFluidIndex(currentBlock)).oceanBlock && currentaState.getValue(BlockFiniteFluid.LEVEL) < 4) { //8
            	int newValue = world.getBlockState(pos).getValue(BlockFiniteFluid.LEVEL)/2; //3
            	world.setBlockState(pos, currentaState.withProperty(BlockFiniteFluid.LEVEL, newValue));
            }
            
            FiniteFluidLogic.InfiniteWaterSource.wakeOcean(world, pos);

            if (FiniteFluidLogic.GeneralPurposeLogic.getCalc() > FiniteFluidLogic.GeneralPurposeLogic.getMaxCalc())
            {

            	world.scheduleUpdate(pos, this, this.tickRate(world));
            }
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
                    int newLevel = world.getBlockState(pos).getValue(LEVEL);
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
                    	world.setBlockState(pos, newState, 3);
                    	
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
                }
            }
        }
		super.updateTick(world, pos, state, rand);
    }
    
    
	@Override
    public void onEntityCollidedWithBlock(World worldIn, BlockPos pos, IBlockState state, Entity entityIn) {
		if (entityIn instanceof EntityPlayer && ((EntityPlayer)entityIn).isCreative() && !ModConfig.flowingWaterShouldMoveCreativePlayer /*&& state.getMaterial() == Material.WATER*/) return;
		
		Vec3d flow = calculateFlowVector(worldIn, pos);
        
        double strength = 0.014D;
        entityIn.motionX += -flow.x * strength;
        entityIn.motionY += -flow.y * strength;
        entityIn.motionZ += -flow.z * strength;
    }
	
	
	public Vec3d calculateFlowVector(IBlockAccess world, BlockPos pos) {
    	Vec3d flow = new Vec3d(0,0,0);
        for(EnumFacing dir : EnumFacing.Plane.HORIZONTAL) {
            BlockPos neighbor = pos.offset(dir);
            if (!(world.getBlockState(neighbor).getBlock() instanceof BlockFiniteFluid)) continue;
            
            int levelNeighbor = FiniteFluidLogic.GeneralPurposeLogic.getFluidLevel(world, neighbor);
            int levelCurrent = FiniteFluidLogic.GeneralPurposeLogic.getFluidLevel(world, pos);
            int diff = levelNeighbor - levelCurrent;
            
            flow = flow.addVector(
                dir.getFrontOffsetX() * diff, 
                0, 
                dir.getFrontOffsetZ() * diff
            );
            
    		if (flow.lengthVector() > 0) 
    			flow = flow.normalize();
        }
		return flow;
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

        int waterType = FiniteFluidLogic.GeneralPurposeLogic.getFluidIndex(waterBlock);     
        int targetType = FiniteFluidLogic.GeneralPurposeLogic.getFluidIndex(targetBlock);
        
        if (waterType < 0 || targetType < 0) return false;
        
        if (targetBlock instanceof BlockFiniteFluid) {
            if (waterMeta > 9) {
            	
                if (targetMeta < 5) {
                    //world.setBlockToAir(waterPos);
                    world.setBlockState(targetPos, Blocks.COBBLESTONE.getDefaultState());
                    BlockNewLava.triggerLavaMixEffects(world, targetPos);
                    return true;
                }

                if (targetMeta < 10) {
                    world.setBlockState(targetPos, Blocks.STONE.getDefaultState());
                    //world.setBlockToAir(waterPos);
                    BlockNewLava.triggerLavaMixEffects(world, targetPos);
                    return true;
                }

                if (targetMeta > 9) {
                    world.setBlockToAir(waterPos);
                    world.setBlockState(targetPos, Blocks.OBSIDIAN.getDefaultState());
                    BlockNewLava.triggerLavaMixEffects(world, targetPos);
                    return true;
                }

            } else if (waterMeta > 5) {
                if (targetMeta < 6) {
                    world.setBlockState(targetPos, Blocks.STONE.getDefaultState());
                    //world.setBlockToAir(waterPos);
                    BlockNewLava.triggerLavaMixEffects(world, targetPos);
                    return true;
                }

                if (targetMeta > 5) {
                    //world.setBlockToAir(waterPos);
                    world.setBlockState(targetPos, Blocks.COBBLESTONE.getDefaultState());
                    BlockNewLava.triggerLavaMixEffects(world, targetPos);
                    return true;
                }
                if (targetMeta > 10) {
                    world.setBlockToAir(waterPos);
                    BlockNewLava.triggerLavaMixEffects(world, targetPos);
                    return true;
                }

            } else {
                world.setBlockToAir(waterPos);
                BlockNewLava.triggerLavaMixEffects(world, targetPos);
                return true;
            }

            
        } else {
        	//vanilla water?
        }
        

    	
        return false;    
        }


}
