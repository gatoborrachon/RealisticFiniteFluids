package com.gatoborrachon.realisticfinitefluids.mixin.earlymixins;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.gatoborrachon.realisticfinitefluids.init.ModBlocks;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.MapGenRavine;

@Mixin(MapGenRavine.class)
public abstract class MixinMapGenRavine {

    @Shadow(remap = true)
    @Final
    @Mutable
    private static IBlockState field_186135_a; //field_186135_a --> FLOWING_LAVA

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void modifyStaticLavaBlock(CallbackInfo ci) {
    	field_186135_a = ModBlocks.INFINITE_LAVA_SOURCE.getDefaultState();
    }
    
    @Inject(method = "digBlock", at = @At("HEAD"), cancellable = true, remap = false)
    private void injectCustomGravelAndSandLogic(ChunkPrimer data, int x, int y, int z, int chunkX, int chunkZ,
                                                boolean foundTop, CallbackInfo ci) {

        IBlockState state = data.getBlockState(x, y, z);
        IBlockState up = (y + 1 < 256) ? data.getBlockState(x, y + 1, z) : null;

        if (state.getMaterial() == Material.ROCK || state.getMaterial() == Material.SAND || state.getMaterial() == Material.GROUND/*state.getBlock() == Blocks.GRAVEL || state.getBlock() == Blocks.SAND || state.getBlock() == Blocks.STONE*/) {
            boolean nearWaterOrSand = false;
            if (up != null) {
                nearWaterOrSand = up.getMaterial() == Material.WATER || up.getMaterial() == Material.SAND
                    || isWaterSafe(data, x + 1, y, z)
                    || isWaterSafe(data, x - 1, y, z)
                    || isWaterSafe(data, x, y, z + 1)
                    || isWaterSafe(data, x, y, z - 1);
            }

            if (nearWaterOrSand) {
                data.setBlockState(x, y, z, Blocks.STONE.getDefaultState());

                if (y > 1 && data.getBlockState(x, y - 1, z).getBlock() == Blocks.AIR) {
                    data.setBlockState(x, y - 1, z, Blocks.STONE.getDefaultState());
                } else if (y > 1 && (data.getBlockState(x, y - 1, z).getMaterial() == Material.SAND || state.getMaterial() == Material.SAND)) {
                    data.setBlockState(x, y + 1, z, Blocks.STONE.getDefaultState());
                }

                ci.cancel();
            }
        }
    }

    private boolean isWaterSafe(ChunkPrimer data, int x, int y, int z) {
        return x >= 0 && x < 16 && z >= 0 && z < 16 && y >= 0 && y < 256
            && (data.getBlockState(x, y, z).getMaterial() == Material.WATER || data.getBlockState(x, y, z).getMaterial() == Material.SAND);
    }
}
