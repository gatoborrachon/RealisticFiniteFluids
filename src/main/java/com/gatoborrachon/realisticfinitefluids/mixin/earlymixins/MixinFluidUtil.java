package com.gatoborrachon.realisticfinitefluids.mixin.earlymixins;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import com.gatoborrachon.realisticfinitefluids.References;
import com.gatoborrachon.realisticfinitefluids.interfaces.IRealisticFiniteFluid;
import com.gatoborrachon.realisticfinitefluids.logic.FiniteFluidLogic;
import com.gatoborrachon.realisticfinitefluids.logic.RealisticFiniteFluidFunctions;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
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
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.wrappers.BlockLiquidWrapper;
import net.minecraftforge.fluids.capability.wrappers.BlockWrapper;
import net.minecraftforge.fluids.capability.wrappers.FluidBlockWrapper;

@Mixin(value = FluidUtil.class, remap = false)
public abstract class MixinFluidUtil {

	//@Shadow private abstract static IFluidHandler getFluidBlockHandler(Fluid fluid, World world, BlockPos pos);

    private static IFluidHandler getFluidBlockHandler(Fluid fluid, World world, BlockPos pos)
    {
        Block block = fluid.getBlock();
        if (block instanceof IFluidBlock)
        {
            return new FluidBlockWrapper((IFluidBlock) block, world, pos);
        }
        else if (block instanceof BlockLiquid)
        {
            return new BlockLiquidWrapper((BlockLiquid) block, world, pos);
        }
        else
        {
            return new BlockWrapper(block, world, pos);
        }
    }
	
	

	@Overwrite(remap = false)
	public static boolean tryPlaceFluid(
			@Nullable EntityPlayer player,
			World world,
			BlockPos pos,
			IFluidHandler fluidSource,
			FluidStack resource) {

		if (world == null || resource == null || pos == null || fluidSource == null) {
			return false;
		}

		Fluid fluid = resource.getFluid();
		if (fluid == null || !fluid.canBePlacedInWorld()) {
			return false;
		}

		// --- RFF Handling ---
		String currentFluid = resource.getFluid().getName();
		Block containedBlock = resource.getFluid().getBlock();
		int index = FiniteFluidLogic.GeneralPurposeLogic.getFluidIndex(currentFluid);
		IBlockState flowingState = FiniteFluidLogic.liquids.get(index).flowingBlock.getDefaultState();

		IBlockState targetBlockState = world.getBlockState(pos);
		Block targetBlock = RealisticFiniteFluidFunctions.getBlock(world, pos, targetBlockState);

		if (RealisticFiniteFluidFunctions.isAirBlock(world, pos, true) || targetBlock instanceof IRealisticFiniteFluid) {

			SoundEvent soundToDisplay = flowingState.getMaterial() == Material.LAVA
					? SoundEvents.ITEM_BUCKET_EMPTY_LAVA
							: SoundEvents.ITEM_BUCKET_EMPTY;

			if (!world.isRemote) {
				if (targetBlock instanceof IRealisticFiniteFluid) {
					IRealisticFiniteFluid realisticFluid = (IRealisticFiniteFluid) targetBlock;

					if (RealisticFiniteFluidFunctions.getFluidRegistry(targetBlock)
							== RealisticFiniteFluidFunctions.getFluidRegistry(containedBlock)) {

						if (realisticFluid.getConceptualVolume(world, pos, targetBlockState) == References.MAXIMUM_CONCEPTUAL_LEVEL) {
							if (!(RealisticFiniteFluidFunctions.getBlock(world, pos.up(), world.getBlockState(pos.up())).hasTileEntity())
									&& targetBlock.isReplaceable(world, pos.up())) {

								RealisticFiniteFluidFunctions.setBlockState(world, pos, pos.up(),
										FiniteFluidLogic.liquids.get(index).flowingBlock.getDefaultState());
							} else
								return false; // No se puede colocar arriba, no hacer nada
						} else {
							// Distribución equitativa
							FiniteFluidLogic.FluidWorldInteraction.distributeFluidEquallyForBuckets(world, pos,
									References.MAXIMUM_CONCEPTUAL_LEVEL, index);
						}

					} else if (targetBlockState.getMaterial() != containedBlock.getDefaultState().getMaterial()) {
						world.playSound(player, pos, SoundEvents.BLOCK_FIRE_EXTINGUISH,
								SoundCategory.BLOCKS, 0.5F,
								2.6F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.8F);

						realisticFluid.setBlockState(world, player.getPosition(), pos,
								FiniteFluidLogic.liquids.get(index).flowingBlock.getDefaultState());
					}
				} else {
					IRealisticFiniteFluid realisticFluid = ((IRealisticFiniteFluid) FiniteFluidLogic.liquids.get(index).flowingBlock);
					realisticFluid.setBlockState(world, player.getPosition(), pos,
							realisticFluid.setVolume(null, null, flowingState, References.MAXIMUM_LEVEL));
				}

				// Drenar correctamente el tanque
				fluidSource.drain(resource, true);
			}

			world.playSound(player.getPosition().getX() + 0.5D, player.getPosition().getY() + 0.5D,
					player.getPosition().getZ() + 0.5D, soundToDisplay,
					SoundCategory.BLOCKS, 1.0F, 1.0F, false);

			if (!player.capabilities.isCreativeMode) {
				EnumHand hand = player.getActiveHand();
				if (hand == null) hand = EnumHand.MAIN_HAND;
				player.setHeldItem(hand, new ItemStack(Items.BUCKET));
			}

			return true;
		}
		// --- END RFF Handling ---

		// --- ORGINAL FORGE CODE ---
		if (fluidSource.drain(resource, false) == null) {
			return false;
		}

		IBlockState destBlockState = world.getBlockState(pos);
		Material destMaterial = destBlockState.getMaterial();
		boolean isDestNonSolid = !destMaterial.isSolid();
		boolean isDestReplaceable = destBlockState.getBlock().isReplaceable(world, pos);
		if (!world.isAirBlock(pos) && !isDestNonSolid && !isDestReplaceable) {
			return false;
		}

		if (world.provider.doesWaterVaporize() && fluid.doesVaporize(resource)) {
			FluidStack result = fluidSource.drain(resource, true);
			if (result != null) {
				result.getFluid().vaporize(player, world, pos, result);
				return true;
			}
		} else {
			//IFluidHandler handler = RealisticFiniteFluidFunctions.getFluidBlockHandler(fluid, world, pos);
			//FluidStack result = RealisticFiniteFluidFunctions.tryFluidTransfer(handler, fluidSource, resource, true);
            IFluidHandler handler = getFluidBlockHandler(fluid, world, pos);
            FluidStack result = FluidUtil.tryFluidTransfer(handler, fluidSource, resource, true);
			if (result != null) {
				SoundEvent soundevent = resource.getFluid().getEmptySound(resource);
				world.playSound(player, pos, soundevent, SoundCategory.BLOCKS, 1.0F, 1.0F);
				return true;
			}
		}
		// --- END ORIGINAL FORGE CODE ---

		return false;
	}

	
	
	

	/*
	@Inject(method = "tryPlaceFluid(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraftforge/fluids/capability/IFluidHandler;Lnet/minecraftforge/fluids/FluidStack;)Z",
			at = @At("TAIL"), cancellable = true)
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
		if (RealisticFiniteFluidFunctions.isAirBlock(world, pos, true) || targetBlock instanceof IRealisticFiniteFluid) {
			SoundEvent soundToDisplay = flowingState.getMaterial() == Material.LAVA ? SoundEvents.ITEM_BUCKET_EMPTY_LAVA : SoundEvents.ITEM_BUCKET_EMPTY;

			if (!world.isRemote) {

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
								RealisticFiniteFluidFunctions.setBlockState(world, pos, pos.up(), FiniteFluidLogic.liquids.get(index).flowingBlock.getDefaultState()); 
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
	}*/


}
