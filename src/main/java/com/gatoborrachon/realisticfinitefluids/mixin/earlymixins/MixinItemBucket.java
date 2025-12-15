package com.gatoborrachon.realisticfinitefluids.mixin.earlymixins;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import com.gatoborrachon.realisticfinitefluids.References;
import com.gatoborrachon.realisticfinitefluids.interfaces.IRealisticFiniteFluid;
import com.gatoborrachon.realisticfinitefluids.logic.FiniteFluidLogic;
import com.gatoborrachon.realisticfinitefluids.logic.NewFluidType;
import com.gatoborrachon.realisticfinitefluids.logic.RealisticFiniteFluidFunctions;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBucket;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

@Mixin(ItemBucket.class)
public class MixinItemBucket extends Item {

	@Shadow(remap = References.onDev)
	@Final
    public Block field_77876_a; //containedBlock
	
	@Unique
	int localFluidIndex;
	
	@Unique
	NewFluidType fluidType;

	public MixinItemBucket(Block containedBlockIn) {
        this.field_77876_a = containedBlockIn;
        this.localFluidIndex = FiniteFluidLogic.GeneralPurposeLogic.getFluidIndex(containedBlockIn);
        this.fluidType = FiniteFluidLogic.liquids.get(localFluidIndex);
	}

	/*@Override
	public ItemStack onItemUseFinish(ItemStack stack, World worldIn, EntityLivingBase entityLiving) {
		return new ItemStack(Items.BUCKET);
	}*/
	
	//@Override
	@Overwrite(remap = References.onDev) //onItemRightClick
    public ActionResult<ItemStack> func_77659_a(World worldIn, EntityPlayer playerIn, EnumHand handIn)
    {
        boolean flag = this.field_77876_a == Blocks.AIR;
        ItemStack itemstack = playerIn.getHeldItem(handIn);
        RayTraceResult raytraceresult = this.rayTrace(worldIn, playerIn, flag);
        ActionResult<ItemStack> ret = net.minecraftforge.event.ForgeEventFactory.onBucketUse(playerIn, worldIn, itemstack, raytraceresult);
        if (ret != null) return ret;

        if (raytraceresult == null)
        {
            return new ActionResult<ItemStack>(EnumActionResult.PASS, itemstack);
        }
        else if (raytraceresult.typeOfHit != RayTraceResult.Type.BLOCK)
        {
            return new ActionResult<ItemStack>(EnumActionResult.PASS, itemstack);
        }
        else
        {
        	EnumFacing face = raytraceresult.sideHit;
            BlockPos blockpos = raytraceresult.getBlockPos();

            if (!worldIn.isBlockModifiable(playerIn, blockpos))
            {
                return new ActionResult<ItemStack>(EnumActionResult.FAIL, itemstack);
            }
            else if (flag)
            {
                if (!playerIn.canPlayerEdit(blockpos.offset(raytraceresult.sideHit), raytraceresult.sideHit, itemstack))
                {
                    return new ActionResult<ItemStack>(EnumActionResult.FAIL, itemstack);
                }
                else
                {
                    IBlockState iblockstate = worldIn.getBlockState(blockpos);
                    Block initialBlock = iblockstate.getBlock();
                    Material material = iblockstate.getMaterial();

                    if (!(initialBlock instanceof IRealisticFiniteFluid)) 
                        return new ActionResult<ItemStack>(EnumActionResult.FAIL, itemstack);

                    IRealisticFiniteFluid realisticFluid = ((IRealisticFiniteFluid)initialBlock);
                    int level = realisticFluid.getConceptualVolume(worldIn, blockpos, iblockstate); //state.getValue(BlockFiniteFluid.LEVEL)+1; // 0-15 //+1 --> 1-16 LEVEL conceptual

                    //if (level == References.MINIMUM_LEVEL && !(worldIn.getBlockState(blockpos.down()) instanceof IRealisticFiniteFluid)) 
                    //    return new ActionResult<ItemStack>(EnumActionResult.FAIL, itemstack);
                    
                    boolean shouldContinue = false;
                    if (FiniteFluidLogic.bucketRemoveLowFluid) {
                    	shouldContinue = FiniteFluidLogic.FluidWorldInteraction.bucketRemoveFluidEvenLowNEW(worldIn, blockpos, level); //, ((IFluidBlock)blockToCheck).getFluid());
                    } else {
                    	shouldContinue = FiniteFluidLogic.FluidWorldInteraction.bucketRemoveFluidOnlyFullNEW(worldIn, blockpos, level);
                    }

                    if (!shouldContinue)
                        return new ActionResult<ItemStack>(EnumActionResult.FAIL, itemstack);

                    
                    if (!worldIn.isRemote) {

                        
                    	worldIn.scheduleUpdate(blockpos, initialBlock, initialBlock.tickRate(worldIn));
                    	worldIn.setBlockToAir(blockpos);
                        
                        // Ejecutamos tu código extra de verificación de borde oceánico
                        boolean exposedToOceanWater = false;
                        //System.out.println("exposedToOceanWater original " + exposedToOceanWater);

                        for (EnumFacing dir : EnumFacing.VALUES) {
                            BlockPos neighbor = blockpos.offset(dir);
                            IBlockState neighborState = worldIn.getBlockState(neighbor);
                            ////System.out.println("Bloque a explorar " + neighborState.getBlock());

                            if (realisticFluid.isOceanBlock(worldIn, neighbor, neighborState, FiniteFluidLogic.GeneralPurposeLogic.getFluidIndex(neighborState.getBlock()))) {
                                exposedToOceanWater = true;
                                
                                if (exposedToOceanWater
                                    && !(worldIn.getBlockState(blockpos.down()).getBlock() instanceof IRealisticFiniteFluid)
                                    && !(worldIn.getBlockState(blockpos).getBlock() instanceof IRealisticFiniteFluid)) {
                                    FiniteFluidLogic.OceanFluidsLogic.borderOceanCheck(worldIn, blockpos, false);
                                }
                                break;
                            }
                        }
                    }
                    Fluid fluid = ((IRealisticFiniteFluid)initialBlock).getFluid();
                	ItemStack filled = FluidUtil.getFilledBucket(new FluidStack(fluid, Fluid.BUCKET_VOLUME));

                    playerIn.addStat(StatList.getObjectUseStats(this));
                    if (material == Material.WATER) playerIn.playSound(SoundEvents.ITEM_BUCKET_FILL, 1.0F, 1.0F); 
                    else playerIn.playSound(SoundEvents.ITEM_BUCKET_FILL_LAVA, 1.0F, 1.0F);
                	
                    return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, filled);

                	
                    /*if (!playerIn.capabilities.isCreativeMode) {
                        if (emptyBucket.getCount() == 1) {
                            // Caso especial: solo había 1 cubeta, reemplazarla directamente en la mano principal
                        	playerIn.setHeldItem(EnumHand.MAIN_HAND, filled);
                        } else {
                            // Si había más de una, reducir el stack y añadir la nueva cubeta al inventario
                            emptyBucket.shrink(1);
                            if (!playerIn.inventory.addItemStackToInventory(filled)) {
                            	playerIn.dropItem(filled, false);
                            }
                        }
                    } else {
                        // En creativo, simplemente dar la cubeta custom (sin quitar nada)
                    	playerIn.setHeldItem(EnumHand.MAIN_HAND, emptyBucket);
                    }*/
                    
                    
                    /*if (material == Material.WATER && ((Integer)iblockstate.getValue(BlockLiquid.LEVEL)).intValue() == 0)
                    {
                        worldIn.setBlockState(blockpos, Blocks.AIR.getDefaultState(), 11);
                        playerIn.addStat(StatList.getObjectUseStats(this));
                        playerIn.playSound(SoundEvents.ITEM_BUCKET_FILL, 1.0F, 1.0F);
                        return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, this.fillBucket(itemstack, playerIn, Items.WATER_BUCKET));
                    }
                    else if (material == Material.LAVA && ((Integer)iblockstate.getValue(BlockLiquid.LEVEL)).intValue() == 0)
                    {
                        playerIn.playSound(SoundEvents.ITEM_BUCKET_FILL_LAVA, 1.0F, 1.0F);
                        worldIn.setBlockState(blockpos, Blocks.AIR.getDefaultState(), 11);
                        playerIn.addStat(StatList.getObjectUseStats(this));
                        return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, this.fillBucket(itemstack, playerIn, Items.LAVA_BUCKET));
                    }*/
                    
                    
                    //else
                    //{
                        //return new ActionResult<ItemStack>(EnumActionResult.FAIL, itemstack);
                    //}
                }
            }
            else
            {
                boolean flag1 = worldIn.getBlockState(blockpos).getBlock().isReplaceable(worldIn, blockpos);
                BlockPos blockpos1 = flag1 && raytraceresult.sideHit == EnumFacing.UP ? blockpos : blockpos.offset(raytraceresult.sideHit);

                if (!playerIn.canPlayerEdit(blockpos1, raytraceresult.sideHit, itemstack))
                {
                    return new ActionResult<ItemStack>(EnumActionResult.FAIL, itemstack);
                }
                else if (this.tryPlaceContainedLiquid(playerIn, worldIn, blockpos1, face))
                {
                    if (playerIn instanceof EntityPlayerMP)
                    {
                        CriteriaTriggers.PLACED_BLOCK.trigger((EntityPlayerMP)playerIn, blockpos1, itemstack);
                    }

                    playerIn.addStat(StatList.getObjectUseStats(this));
                    return !playerIn.capabilities.isCreativeMode ? new ActionResult<ItemStack>(EnumActionResult.SUCCESS, new ItemStack(Items.BUCKET)) : new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemstack);
                }
                else
                {
                    return new ActionResult<ItemStack>(EnumActionResult.FAIL, itemstack);
                }
            }
        }
    }
 
    
    @Unique
    public boolean tryPlaceContainedLiquid(@Nullable EntityPlayer player, World worldIn, BlockPos posIn, EnumFacing face)
    {
        if (this.field_77876_a == Blocks.AIR)
        {
            return false;
        }
        else
        {
            IBlockState iblockstate = worldIn.getBlockState(posIn);
            Material material = iblockstate.getMaterial();
            boolean flag = !material.isSolid();
            boolean flag1 = iblockstate.getBlock().isReplaceable(worldIn, posIn);

            if (!worldIn.isAirBlock(posIn) && !flag && !flag1)
            {
                return false;
            }
            else
            {
                if (worldIn.provider.doesWaterVaporize() && this.field_77876_a == Blocks.FLOWING_WATER)
                {
                    int l = posIn.getX();
                    int i = posIn.getY();
                    int j = posIn.getZ();
                    worldIn.playSound(player, posIn, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.5F, 2.6F + (worldIn.rand.nextFloat() - worldIn.rand.nextFloat()) * 0.8F);

                    for (int k = 0; k < 8; ++k)
                    {
                        worldIn.spawnParticle(EnumParticleTypes.SMOKE_LARGE, (double)l + Math.random(), (double)i + Math.random(), (double)j + Math.random(), 0.0D, 0.0D, 0.0D);
                    }
                }
                else
                {
                    if (!worldIn.isRemote && (flag || flag1) && !material.isLiquid())
                    {
                        worldIn.destroyBlock(posIn, true);
                    }

                    SoundEvent soundevent = this.field_77876_a == Blocks.FLOWING_LAVA ? SoundEvents.ITEM_BUCKET_EMPTY_LAVA : SoundEvents.ITEM_BUCKET_EMPTY;
                    worldIn.playSound(player, posIn, soundevent, SoundCategory.BLOCKS, 1.0F, 1.0F);
                    RealisticFiniteFluidFunctions.onBucketItemUse(player, worldIn, posIn, face, this.field_77876_a, getFluidIndex(this.field_77876_a));
                    //this.placeFluids(worldIn, posIn, player, face);
                    //worldIn.setBlockState(posIn, this.containedBlock.getDefaultState(), 11);
                }

                return true;
            }
        }
    }
    
    /*@Unique
    public void placeFluids(World worldIn, BlockPos pos, EntityPlayer player, EnumFacing facing) {
		BlockPos targetPos = pos.offset(facing);
		IBlockState targetBlockState = worldIn.getBlockState(targetPos);
		Block targetBlock = targetBlockState.getBlock();
        int fluidIndex = FiniteFluidLogic.GeneralPurposeLogic.getFluidIndex(FiniteFluidLogic.liquids.get(0).flowingBlock);
        IRealisticFiniteFluid realisticFluid = ((IRealisticFiniteFluid)FiniteFluidLogic.liquids.get(FiniteFluidLogic.onFiniteFluidIndex).flowingBlock);

		// Permitir colocar solo si es aire o es tu propio bloque finito
		if (worldIn.isAirBlock(targetPos) || targetBlock instanceof IRealisticFiniteFluid) {
			if (!worldIn.isRemote) {
				//System.out.println(worldIn.getBlockState(targetPos).getMaterial() == Material.WATER);

				if (targetBlock instanceof IRealisticFiniteFluid && worldIn.getBlockState(targetPos).getMaterial() == Material.WATER) {
					realisticFluid = (IRealisticFiniteFluid)targetBlock;
					//System.out.println("VERGA WATER BUCKET");
					if (realisticFluid.getConceptualVolume(worldIn, targetPos, targetBlockState) == References.MAXIMUM_CONCEPTUAL_LEVEL) worldIn.setBlockState(targetPos.up(), FiniteFluidLogic.liquids.get(0).flowingBlock.getDefaultState());
					else
						// Lógica de distribución equitativa
						//distributeFluidEqually(worldIn, targetPos, BlockFiniteFluid.MAXIMUM_CONCEPTUAL_LEVEL); // 15 como nivel completo     
            			FiniteFluidLogic.FluidWorldInteraction.distributeFluidEquallyForBuckets(worldIn, targetPos, References.MAXIMUM_CONCEPTUAL_LEVEL, fluidIndex);
				} else if (targetBlock instanceof IRealisticFiniteFluid && worldIn.getBlockState(targetPos).getMaterial() == Material.LAVA) {
					realisticFluid = (IRealisticFiniteFluid)targetBlock;
					worldIn.playSound(player, pos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.5F, 2.6F + (worldIn.rand.nextFloat() - worldIn.rand.nextFloat()) * 0.8F);
					realisticFluid.setBlockState(worldIn, targetPos, FiniteFluidLogic.liquids.get(0).flowingBlock.getDefaultState());
				} else {
					//distributeFluidEqually(worldIn, targetPos, 15); // 15 como nivel completo                    		
					//worldIn.setBlockState(targetPos, ModBlocks.FINITE_WATER_FLOWING.getDefaultState().withProperty(BlockFiniteFluid.LEVEL, 15));                        	
					realisticFluid.setBlockState(worldIn, targetPos, realisticFluid.setVolume(null, null, FiniteFluidLogic.liquids.get(0).flowingBlock.getDefaultState(), References.MAXIMUM_LEVEL));

				}
				worldIn.playSound(player, pos, SoundEvents.ITEM_BUCKET_EMPTY, SoundCategory.BLOCKS, 1.0F, 1.0F);
			}
			worldIn.playSound(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
					SoundEvents.ITEM_BUCKET_EMPTY, SoundCategory.BLOCKS, 1.0F, 1.0F, false);
		}

    }*/
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    


	/*@Override
	@Overwrite
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
		//System.out.println("[RFF] Erga 1");

		ItemStack stack = playerIn.getHeldItem(handIn);
        boolean isEmptyBucket = this.containedBlock == Blocks.AIR;
		
        //System.out.println("[RFF] Erga 2");

		// Interactuar con IFluidHandler
		RayTraceResult rayTraceResult = this.rayTrace(worldIn, playerIn, false);
		if (rayTraceResult == null || rayTraceResult.typeOfHit != RayTraceResult.Type.BLOCK)
			return new ActionResult<>(EnumActionResult.PASS, stack);

		System.out.println("[RFF] Erga 3");

		BlockPos pos = rayTraceResult.getBlockPos();
		EnumFacing face = rayTraceResult.sideHit;
		pos = pos.offset(face);

		//System.out.println("[RFF] Erga 4");

		if (!playerIn.canPlayerEdit(pos, face, stack)) {
			return new ActionResult<>(EnumActionResult.FAIL, stack);
		}

		//System.out.println("[RFF] Erga 5");

		// ¿Hay un IFluidHandler?
		IFluidHandler handler = FluidUtil.getFluidHandler(worldIn, pos, face);
		if (handler != null) {
			FluidStack fluid = new FluidStack(FluidRegistry.getFluid(getFluidRegistry(this.containedBlock)), Fluid.BUCKET_VOLUME);
			int filled = handler.fill(fluid, true);
			if (filled > 0 && !playerIn.capabilities.isCreativeMode) {
				return new ActionResult<>(EnumActionResult.SUCCESS, new ItemStack(Items.BUCKET));
			}
		}
		
		//System.out.println("[RFF] Erga 6");

		
        if (!worldIn.isBlockModifiable(playerIn, pos))
        {
    		//System.out.println("[RFF] Erga 7");
            return new ActionResult<ItemStack>(EnumActionResult.FAIL, stack);

        }
        else if (isEmptyBucket)
        {
    		//System.out.println("[RFF] Erga 8");

            if (!playerIn.canPlayerEdit(pos.offset(rayTraceResult.sideHit), rayTraceResult.sideHit, stack))
            {
                return new ActionResult<ItemStack>(EnumActionResult.FAIL, stack);
            }
            else
            {
        		RealisticFiniteFluidFunctions.onBucketItemUse(playerIn, worldIn, pos, handIn, face, (float)rayTraceResult.hitVec.x, (float)rayTraceResult.hitVec.y, (float)rayTraceResult.hitVec.z, this.containedBlock, getFluidIndex(this.containedBlock));


                
                
                //else
                //{
                    return new ActionResult<ItemStack>(EnumActionResult.FAIL, stack);
                //}
            }
        }
		
		
		
		else // CUBETA NO VACIA
		{
			boolean flag1 = worldIn.getBlockState(pos).getBlock().isReplaceable(worldIn, pos);
			BlockPos blockpos1 = flag1 && rayTraceResult.sideHit == EnumFacing.UP ? pos : pos.offset(rayTraceResult.sideHit);

			if (!playerIn.canPlayerEdit(blockpos1, rayTraceResult.sideHit, stack))
			{
				return new ActionResult<ItemStack>(EnumActionResult.FAIL, stack);
			}
			else if (this.tryPlaceContainedLiquid(playerIn, worldIn, pos))
			{
				if (playerIn instanceof EntityPlayerMP)
				{
					CriteriaTriggers.PLACED_BLOCK.trigger((EntityPlayerMP)playerIn, blockpos1, stack);
				}

				playerIn.addStat(StatList.getObjectUseStats(this));
				return !playerIn.capabilities.isCreativeMode ? new ActionResult<ItemStack>(EnumActionResult.SUCCESS, new ItemStack(Items.BUCKET)) : new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
			}
			else
			{
				return new ActionResult<ItemStack>(EnumActionResult.FAIL, stack);
			}
		}

		// Si no, coloca el líquido como Vanilla
		//return super.onItemRightClick(worldIn, playerIn, handIn);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	


	
	@Overwrite
	public boolean tryPlaceContainedLiquid(@Nullable EntityPlayer player, World worldIn, BlockPos posIn)
	{
		System.out.println("[RFF] supposed Contained Block: "+this.containedBlock);
		if (this.containedBlock == Blocks.AIR)
		{
			return false;
		} 
		else
		{
			IBlockState iblockstate = worldIn.getBlockState(posIn);
			Material material = iblockstate.getMaterial();
			boolean flag = !material.isSolid();
			boolean flag1 = iblockstate.getBlock().isReplaceable(worldIn, posIn);

			if (!worldIn.isAirBlock(posIn) && !flag && !flag1)
			{
				return false;
			}
			else
			{
				if (worldIn.provider.doesWaterVaporize())
				{
					int l = posIn.getX();
					int i = posIn.getY();
					int j = posIn.getZ();
					worldIn.playSound(player, posIn, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.5F, 2.6F + (worldIn.rand.nextFloat() - worldIn.rand.nextFloat()) * 0.8F);

					for (int k = 0; k < 8; ++k)
					{
						worldIn.spawnParticle(EnumParticleTypes.SMOKE_LARGE, (double)l + Math.random(), (double)i + Math.random(), (double)j + Math.random(), 0.0D, 0.0D, 0.0D);
					}
				}
				else
				{
					if (!worldIn.isRemote && (flag || flag1) && !material.isLiquid())
					{
						worldIn.destroyBlock(posIn, true);
					}

    	            IRealisticFiniteFluid realisticFluid = ((IRealisticFiniteFluid)FiniteFluidLogic.liquids.get(FiniteFluidLogic.onFiniteFluidIndex).flowingBlock);
					SoundEvent soundevent = this.containedBlock == Blocks.FLOWING_LAVA ? SoundEvents.ITEM_BUCKET_EMPTY_LAVA : SoundEvents.ITEM_BUCKET_EMPTY;
					worldIn.playSound(player, posIn, soundevent, SoundCategory.BLOCKS, 1.0F, 1.0F);
					realisticFluid.setBlockState(worldIn, posIn, realisticFluid.setVolume(null, null, this.containedBlock.getDefaultState(), References.MAXIMUM_LEVEL));

				}

				return true;
			}
		}
	}*/

	/*@Override
	//@Overwrite
	public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand,
			EnumFacing facing, float hitX, float hitY, float hitZ) {
		Block actualContainedBlock = null;
		System.out.println("[RFF] actual Bucket Item: "+this.getRegistryName());
		System.out.println("[RFF] supposed Contained Block: "+this.containedBlock);
		if (this == Items.WATER_BUCKET) actualContainedBlock = Blocks.FLOWING_WATER;
		if (this == Items.LAVA_BUCKET) actualContainedBlock = Blocks.FLOWING_LAVA;
		System.out.println("[RFF] actualContainedBlock: "+actualContainedBlock);

		return RealisticFiniteFluidFunctions.onBucketItemUse(player, worldIn, pos, hand, facing, hitX, hitY, hitZ, actualContainedBlock, getFluidIndex(actualContainedBlock));
	}*/
	
	@Unique
	private static int getFluidIndex(Block block) {
		return FiniteFluidLogic.GeneralPurposeLogic.getFluidIndex(block);
	}
	
	@Unique
	private static String getFluidRegistry(Block block) {
		return FiniteFluidLogic.liquids.get(FiniteFluidLogic.GeneralPurposeLogic.getFluidIndex(block)).name;
	}
	
	
}
