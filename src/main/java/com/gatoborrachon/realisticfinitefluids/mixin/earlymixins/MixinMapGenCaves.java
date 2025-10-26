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
import net.minecraft.world.gen.MapGenCaves;

@Mixin(MapGenCaves.class)
public class MixinMapGenCaves {
	/**
	 * ESTE CODIGO SE ENCARGA DE CONVERTIR LA LAVA DEBAJO DEL MUNDO POR LAVA FINITA
	 */
    @Shadow(remap = true)
    @Final
    @Mutable
    private static IBlockState field_186126_a; //field_186126_a --> BLK_LAVA

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void modifyStaticLavaBlock(CallbackInfo ci) {
    	field_186126_a = ModBlocks.INFINITE_LAVA_SOURCE.getDefaultState();
    }
    
    /**
     * ESTE CODIGO SE ENCARGA DE  PARCHEAR LOS TECHOS/PAREDES DE LAS CUEVAS QUE SE GENERAN DEBAJO DEL MAR, PARA EVITAR QUE TENGAN FUGAS
     * DE AGUA FINITA
     */
    @Inject(method = "digBlock", at = @At("HEAD"), cancellable = true, remap = false) //No hay SRG porque el metoddo lo añade forge,
    private void injectCustomGravelAndSandLogic(ChunkPrimer data, int x, int y, int z, int chunkX, int chunkZ,
                                                boolean foundTop, IBlockState state, IBlockState up,
                                                CallbackInfo ci) {
    	//if (y>62) return;
    	Material actualBlockMaterial = state.getMaterial();
        if (actualBlockMaterial == Material.ROCK || actualBlockMaterial == Material.SAND || actualBlockMaterial == Material.GROUND/*state.getBlock() == Blocks.GRAVEL || state.getBlock() == Blocks.SAND || state.getBlock() == Blocks.STONE || state.getBlock() == Blocks.DIRT*/) {
            boolean nearWaterOrGravel = up.getMaterial() == Material.WATER || up.getMaterial() == Material.SAND /*.getMaterial() == Material.SAND*/
                || isWaterSafe(data, x + 1, y, z)
                || isWaterSafe(data, x - 1, y, z)
                || isWaterSafe(data, x, y, z + 1)
                || isWaterSafe(data, x, y, z - 1);

            if (nearWaterOrGravel) {
                data.setBlockState(x, y, z, Blocks.STONE.getDefaultState());

                if (y > 1 && (data.getBlockState(x, y - 1, z).getBlock() == Blocks.AIR)) {
                    data.setBlockState(x, y - 1, z, Blocks.STONE.getDefaultState());
                } else if (y > 1 && (data.getBlockState(x, y-1, z).getMaterial() == Material.SAND || data.getBlockState(x, y, z).getMaterial() == Material.SAND)) {
                    data.setBlockState(x, y + 1, z, Blocks.STONE.getDefaultState());                	
                }

                ci.cancel();
            }
        }
    }

    private boolean isWaterSafe(ChunkPrimer data, int x, int y, int z) {
        return x >= 0 && x < 16 && z >= 0 && z < 16 && y >= 0 && y < 256
            && (data.getBlockState(x, y, z).getMaterial() == Material.WATER  || data.getBlockState(x, y, z).getMaterial() == Material.SAND);
    }
    
    /*@Inject(method = "digBlock", at = @At("HEAD"), cancellable = true)
    private void injectCustomGravelLogic(ChunkPrimer data, int x, int y, int z, int chunkX, int chunkZ,
                                         boolean foundTop, IBlockState state, IBlockState up,
                                         CallbackInfo ci) {
        // Si el bloque actual es grava y arriba hay agua
        if (state.getBlock() == Blocks.GRAVEL && up.getMaterial() == Material.WATER) {
            if (y > 1) { // Evitar index out of bounds
                IBlockState below = data.getBlockState(x, y - 1, z);
                if (below.getBlock() == Blocks.AIR) {
                    // Colocamos bloque de ORO como soporte
                    data.setBlockState(x, y - 1, z, Blocks.GOLD_BLOCK.getDefaultState());
                }
            }
            ci.cancel(); // Cancelamos para no ejecutar la lógica vanilla
        }
    }*/
    
}