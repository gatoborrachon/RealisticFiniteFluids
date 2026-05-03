package com.gatoborrachon.realisticfinitefluids.mixin.earlymixins;

import org.spongepowered.asm.mixin.Mixin;

import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.gatoborrachon.realisticfinitefluids.References;
import com.gatoborrachon.realisticfinitefluids.interfaces.IRealisticFiniteFluid;
import com.gatoborrachon.realisticfinitefluids.logic.FiniteFluidLogic;
import com.gatoborrachon.realisticfinitefluids.logic.RealisticFiniteFluidFunctions;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.PotionTypes;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGlassBottle;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionUtils;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

@Mixin(ItemGlassBottle.class)
public class MixinItemGlassBottle extends Item {

	@Inject(method = "func_77659_a", at = @At("HEAD"), cancellable = true, remap = References.onDev) //onItemRightClick --> func_77659_a
	private void injectFiniteWater(World world, EntityPlayer player, EnumHand hand,
	        CallbackInfoReturnable<ActionResult<ItemStack>> cir) {

	    ItemStack stack = player.getHeldItem(hand);
	    RayTraceResult ray = this.rayTrace(world, player, true);

	    if (ray == null || ray.typeOfHit != RayTraceResult.Type.BLOCK)
	        return; // no canceles, deja vanilla

	    BlockPos pos = ray.getBlockPos();
	    IBlockState state = world.getBlockState(pos);
	    Block block = RealisticFiniteFluidFunctions.getBlock(world, pos, state);

	    if (!(block instanceof IRealisticFiniteFluid))
	        return; // deja vanilla

	    int volume = RealisticFiniteFluidFunctions.getConceptualVolume(world, pos, state);
	    int required = 3;

	    if (volume < required) {
	        // No hay suficiente agua --> NO permitir botella
	        cir.setReturnValue(new ActionResult<>(EnumActionResult.FAIL, stack));
	        cir.cancel();
	        return;
	    }

	    if (!world.isRemote) {
	    	
	    	
	    	
	        //TODO ESTA MIERDA NO JALA JAJA
            boolean exposedToOceanWater = false;
            //System.out.println("exposedToOceanWater original " + exposedToOceanWater);

            for (EnumFacing dir : EnumFacing.VALUES) {
                BlockPos neighbor = pos.offset(dir);
                IBlockState neighborState = world.getBlockState(neighbor);
                ////System.out.println("Bloque a explorar " + neighborState.getBlock());

                if (RealisticFiniteFluidFunctions.isOceanBlock(world, neighbor, neighborState, FiniteFluidLogic.GeneralPurposeLogic.getFluidIndex(RealisticFiniteFluidFunctions.getBlock(world, neighbor, neighborState)))) {
                    exposedToOceanWater = true;
                    
                    if (exposedToOceanWater
                        && !(RealisticFiniteFluidFunctions.getBlock(world, pos.down(), world.getBlockState(pos.down())) instanceof IRealisticFiniteFluid)
                        && !(RealisticFiniteFluidFunctions.getBlock(world, pos, world.getBlockState(pos)) instanceof IRealisticFiniteFluid)) {
                        FiniteFluidLogic.OceanFluidsLogic.borderOceanCheck(world, pos, false);
                    }
                    break;
                }
            }
            
            
            
            

	        if (volume == required)
	            RealisticFiniteFluidFunctions.setBlockToAir(world, pos);
	        else
	            RealisticFiniteFluidFunctions.setBlockState(
	                world,
	                player.getPosition(),
	                pos,
	                RealisticFiniteFluidFunctions.setConceptualVolume(world, pos, state, volume - required)
	            );
	        
	        
	        

            
            
            

	        world.playSound(null, player.posX, player.posY, player.posZ,
	                SoundEvents.ITEM_BOTTLE_FILL,
	                SoundCategory.NEUTRAL, 1.0F, 1.0F);
	    }

	    ItemStack waterBottle =
	        PotionUtils.addPotionToItemStack(
	            new ItemStack(Items.POTIONITEM),
	            PotionTypes.WATER);

	    cir.setReturnValue(new ActionResult<>(
	        EnumActionResult.SUCCESS,
	        turnBottleIntoItem(stack, player, waterBottle)));

	    cir.cancel();
	}
	
	
	
    protected ItemStack turnBottleIntoItem(ItemStack emptyBottle, EntityPlayer player, ItemStack waterBottle) {
    	emptyBottle.shrink(1);
        player.addStat(StatList.getObjectUseStats((ItemGlassBottle)(Object)this));

        if (emptyBottle.isEmpty()) return waterBottle;
        else {
            if (!player.inventory.addItemStackToInventory(waterBottle)) player.dropItem(waterBottle, false);
            return emptyBottle;
        }
    }
}
