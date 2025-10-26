package com.gatoborrachon.realisticfinitefluids.mixin.earlymixins;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.gatoborrachon.realisticfinitefluids.init.ModBlocks;
import com.gatoborrachon.realisticfinitefluids.init.ModItems;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;

@Mixin(value = FluidUtil.class, remap = false)
public abstract class MixinFluidUtil {
	
	@Inject(method = "tryPlaceFluid(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraftforge/fluids/capability/IFluidHandler;Lnet/minecraftforge/fluids/FluidStack;)Z",
    //@Inject(method = "tryPlaceFluid(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraftforge/fluids/IFluidHandler;Lnet/minecraftforge/fluids/FluidStack;)Z",
            at = @At("HEAD"), cancellable = true)
    private static void injectTryPlaceFluid(
            @Nullable EntityPlayer player, World world, BlockPos pos,
            IFluidHandler fluidSource, FluidStack resource,
            CallbackInfoReturnable<Boolean> cir) {

        if (resource != null && resource.getFluid() == FluidRegistry.WATER) {
            // coloca tu bloque personalizado
            world.setBlockState(pos, ModBlocks.FINITE_WATER_FLOWING.getDefaultState(), 3);
            // consume el líquido del tanque (si quieres hacerlo aquí)
            fluidSource.drain(resource, true);
            cir.setReturnValue(true);
        }
        
        if (resource != null && resource.getFluid() == FluidRegistry.LAVA) {
            // coloca tu bloque personalizado
            world.setBlockState(pos, ModBlocks.FINITE_LAVA_FLOWING.getDefaultState(), 3);
            // consume el líquido del tanque (si quieres hacerlo aquí)
            fluidSource.drain(resource, true);
            cir.setReturnValue(true);
        }
    }
	
	//Este codigo maneja cualquier interaccion entre un tanque de fluido (que tiene agua o lava) para ddar mis cubetas de fluido finito
    @Redirect(
            method = "getFilledBucket",
            at = @At(
                value = "NEW",
                target = "net/minecraft/item/ItemStack",
                ordinal = 0 // Para WATER_BUCKET
            ),
            remap = false
        )
        private static ItemStack redirectWaterBucket(Item itemIn) {
            if (itemIn == Items.WATER_BUCKET) {
                return new ItemStack(ModItems.FINITE_WATER_BUCKET);
            }
            return new ItemStack(itemIn);
        }

        @Redirect(
            method = "getFilledBucket",
            at = @At(
                value = "NEW",
                target = "net/minecraft/item/ItemStack",
                ordinal = 1 // Para LAVA_BUCKET
            ),
            remap = false
        )
        private static ItemStack redirectLavaBucket(Item itemIn) {
            if (itemIn == Items.LAVA_BUCKET) {
                return new ItemStack(ModItems.FINITE_LAVA_BUCKET);
            }
            return new ItemStack(itemIn);
        }
}
