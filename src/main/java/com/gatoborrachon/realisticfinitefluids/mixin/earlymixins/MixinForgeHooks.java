package com.gatoborrachon.realisticfinitefluids.mixin.earlymixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.gatoborrachon.realisticfinitefluids.blocks.BlockNewLava;
import com.gatoborrachon.realisticfinitefluids.blocks.BlockNewLava_Still;
import com.gatoborrachon.realisticfinitefluids.blocks.BlockNewWater;
import com.gatoborrachon.realisticfinitefluids.blocks.BlockNewWater_Still;

import net.minecraft.block.Block;
import net.minecraftforge.common.ForgeModContainer;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

@Mixin(value = ForgeModContainer.class, remap = false)
public abstract class MixinForgeHooks {

	//Entiendo que esto no ahce nada en realidad
    @Inject(method = "lookupFluidForBlock", at = @At("HEAD"), cancellable = true)
    private static void redirectFluidLookup(Block block, CallbackInfoReturnable<Fluid> cir) {
        if (block instanceof BlockNewLava || block instanceof BlockNewLava_Still) {
            cir.setReturnValue(FluidRegistry.LAVA);
        }
        if (block instanceof BlockNewWater || block instanceof BlockNewWater_Still) {
            cir.setReturnValue(FluidRegistry.WATER);
        }
    }
}
