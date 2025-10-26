package com.gatoborrachon.realisticfinitefluids.mixin.earlymixins;

import java.util.Random;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.material.Material;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenMinable;

@Mixin(WorldGenMinable.class)
public class MixinWorldGenMinable {
	/**
	 * EVITAR QUE LAS MASAS DE GRAVA MINABLES APAREZCAN EN MEDIO DEL OCEANO, PARA EVITAR QUE LAS CUEVAS SE INUNDEN POR QUE AHORA EXISTE UNA PARED
	 * DE GRAVA QUE AL FINAL SE VA A CAER
	 * 
	 * @param worldIn
	 * @param rand
	 * @param position
	 * @param cir
	 */
    @Inject(method = "func_180709_b", at = @At("HEAD"), cancellable = true, remap = true) //func_180709_b --> generate
    private void cancelIfNearWater(World worldIn, Random rand, BlockPos position, CallbackInfoReturnable<Boolean> cir) {
        if (isNearWater(worldIn, position)) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }

    private boolean isNearWater(World world, BlockPos pos) {
        int radius = 1; // reduje el radio para no tocar otros chunks
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    BlockPos checkPos = pos.add(dx, dy, dz);
                    // Evitar forzar carga de chunks
                    if (!world.isBlockLoaded(checkPos)) continue;

                    Material mat = world.getBlockState(checkPos).getMaterial();
                    if (mat == Material.WATER) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}

