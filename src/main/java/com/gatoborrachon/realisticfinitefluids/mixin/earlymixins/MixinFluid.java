package com.gatoborrachon.realisticfinitefluids.mixin.earlymixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.gatoborrachon.realisticfinitefluids.init.ModBlocks;

import net.minecraft.block.Block;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

@Mixin(value = Fluid.class, remap = false)
public abstract class MixinFluid {

	//Casi a prueba de todo, IC2 FluidCells les vale amdre esto, y puede que muchos mas mods :'v
    @Inject(method = "getBlock", at = @At("HEAD"), cancellable = true)
    private void injectGetBlock(CallbackInfoReturnable<Block> cir) {
        Fluid self = (Fluid)(Object)this;
        if (self == FluidRegistry.WATER) {
            cir.setReturnValue(ModBlocks.FINITE_WATER_FLOWING); 
        }
        if (self == FluidRegistry.LAVA) {
            cir.setReturnValue(ModBlocks.FINITE_LAVA_FLOWING); 
        }
    }
}
