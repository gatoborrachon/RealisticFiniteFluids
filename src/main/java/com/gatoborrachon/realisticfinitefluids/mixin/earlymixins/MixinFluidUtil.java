package com.gatoborrachon.realisticfinitefluids.mixin.earlymixins;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.gatoborrachon.realisticfinitefluids.References;
import com.gatoborrachon.realisticfinitefluids.interfaces.IRealisticFiniteFluid;
import com.gatoborrachon.realisticfinitefluids.logic.FiniteFluidLogic;
import com.gatoborrachon.realisticfinitefluids.logic.RealisticFiniteFluidFunctions;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;

@Mixin(value = FluidUtil.class, remap = false)
public abstract class MixinFluidUtil {

	@Inject(method = "tryPlaceFluid(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraftforge/fluids/capability/IFluidHandler;Lnet/minecraftforge/fluids/FluidStack;)Z",
			//@Inject(method = "tryPlaceFluid(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraftforge/fluids/IFluidHandler;Lnet/minecraftforge/fluids/FluidStack;)Z",
			at = @At("TAIL"), cancellable = true) //at = @At("HEAD")
	private static void injectTryPlaceFluid(
			@Nullable EntityPlayer player, World world, BlockPos pos,
			IFluidHandler fluidSource, FluidStack resource,
			CallbackInfoReturnable<Boolean> cir) {

		//TERCER ITERACION
		String currentFluid = resource.getFluid().getName();
		Block containedBlock = resource.getFluid().getBlock();
		int index = FiniteFluidLogic.GeneralPurposeLogic.getFluidIndex(currentFluid);
		IBlockState flowingState = FiniteFluidLogic.liquids.get(index).flowingBlock.getDefaultState();

		IBlockState targetBlockState = world.getBlockState(pos);
		Block targetBlock = RealisticFiniteFluidFunctions.getBlock(world, pos, targetBlockState);
		//int fluidIndex = FiniteFluidLogic.GeneralPurposeLogic.getFluidIndex(ModBlocks.FINITE_LAVA_FLOWING);

		// Permitir colocar solo si es aire o es tu propio bloque finito
		if (world.isAirBlock(pos) || targetBlock instanceof IRealisticFiniteFluid) {
			SoundEvent soundToDisplay = flowingState.getMaterial() == Material.LAVA ? SoundEvents.ITEM_BUCKET_EMPTY_LAVA : SoundEvents.ITEM_BUCKET_EMPTY;

			if (!world.isRemote) {
				/*if (targetBlock instanceof IRealisticFiniteFluid) {
    	            IRealisticFiniteFluid realisticFluid = ((IRealisticFiniteFluid)targetBlock);

            		if (realisticFluid.getConceptualVolume(world, pos, targetBlockState) == References.MAXIMUM_CONCEPTUAL_LEVEL) world.setBlockState(pos.up(), flowingState);

            		else
                    // Lógica de distribución equitativa
                    FiniteFluidLogic.FluidWorldInteraction.distributeFluidEquallyForBuckets(world, pos, References.MAXIMUM_CONCEPTUAL_LEVEL, index); // 15 como nivel completo                    		
            	} else if (targetBlock instanceof IRealisticFiniteFluid && flowingState.getMaterial() == Material.LAVA && world.getBlockState(pos).getMaterial() == Material.WATER) {
                    world.playSound(player, player.getPosition(), SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.5F, 2.6F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.8F);
                    world.setBlockState(pos, flowingState);
            	} else {
    	            IRealisticFiniteFluid realisticFluid = ((IRealisticFiniteFluid)FiniteFluidLogic.liquids.get(FiniteFluidLogic.onFiniteFluidIndex).flowingBlock);                     	
            		realisticFluid.setBlockState(world, pos, realisticFluid.setVolume(null, null, flowingState, References.MAXIMUM_LEVEL));

            	}*/

				if (targetBlock instanceof IRealisticFiniteFluid) {
					IRealisticFiniteFluid realisticFluid = (IRealisticFiniteFluid)targetBlock;
					//System.out.println("[RFF] getFluidRegistry(targetBlock): "+RealisticFiniteFluidFunctions.getFluidRegistry(targetBlock));

					//System.out.println("[RFF] this.containedBlock: "+containedBlock);
					//System.out.println("[RFF] getFluidRegistry(this.containedBlock): "+RealisticFiniteFluidFunctions.getFluidRegistry(containedBlock));

					if (RealisticFiniteFluidFunctions.getFluidRegistry(targetBlock) == RealisticFiniteFluidFunctions.getFluidRegistry(containedBlock)) {
						if (realisticFluid.getConceptualVolume(world, pos, targetBlockState) == References.MAXIMUM_CONCEPTUAL_LEVEL) {
							//world.setBlockState(pos.up(), FiniteFluidLogic.liquids.get(index).flowingBlock.getDefaultState()); 
							//return;
							
							if (!(RealisticFiniteFluidFunctions.getBlock(world, pos.up(), world.getBlockState(pos.up())).hasTileEntity()) && targetBlock.isReplaceable(world, pos.up())) {
								world.setBlockState(pos.up(), FiniteFluidLogic.liquids.get(index).flowingBlock.getDefaultState()); 
								//return;
							} else 
								return; //Because, if the current block is full and you can't place above it, then you should not place the fluid no matter what
						}
						else { // Lógica de distribución equitativa
							FiniteFluidLogic.FluidWorldInteraction.distributeFluidEquallyForBuckets(world, pos, References.MAXIMUM_CONCEPTUAL_LEVEL, index);
							//return;
						}
					} else if (targetBlockState.getMaterial() != containedBlock.getDefaultState().getMaterial()) { //have not same fluidRegistry --> 
						world.playSound(player, pos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.5F, 2.6F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.8F);
						realisticFluid.setBlockState(world, player.getPosition(), pos, FiniteFluidLogic.liquids.get(index).flowingBlock.getDefaultState());
						//return;
					}
				} else {
					IRealisticFiniteFluid realisticFluid = ((IRealisticFiniteFluid)FiniteFluidLogic.liquids.get(index).flowingBlock);                     	
					realisticFluid.setBlockState(world, player.getPosition(), pos, realisticFluid.setVolume(null, null, flowingState, References.MAXIMUM_LEVEL));
					//return;
				}



				world.playSound(player, player.getPosition(), soundToDisplay, SoundCategory.BLOCKS, 1.0F, 1.0F);
			}
			world.playSound(player.getPosition().getX() + 0.5D, player.getPosition().getY() + 0.5D, player.getPosition().getZ() + 0.5D,
					soundToDisplay, SoundCategory.BLOCKS, 1.0F, 1.0F, false);

			if (!player.capabilities.isCreativeMode) {
				EnumHand hand = player.getActiveHand();
				if (hand == null) hand = EnumHand.MAIN_HAND;
				player.setHeldItem(hand, new ItemStack(Items.BUCKET));
			}
		}
	}


}
