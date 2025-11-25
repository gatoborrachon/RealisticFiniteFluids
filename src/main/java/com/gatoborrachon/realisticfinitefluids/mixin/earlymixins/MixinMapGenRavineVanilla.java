package com.gatoborrachon.realisticfinitefluids.mixin.earlymixins;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.gatoborrachon.realisticfinitefluids.blocks.BlockFiniteFluid;
import com.gatoborrachon.realisticfinitefluids.init.ModBlocks;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.MapGenRavine;

@Mixin(MapGenRavine.class)
public abstract class MixinMapGenRavineVanilla {

	/**
	 * ESTE CODIGO SE ENCARGA DE CONVERTIR LA LAVA DEL FONDO DE LA RAVINES POR LAVA FINITA
	 */
    @Shadow(remap = true)
    @Final
    @Mutable
    private static IBlockState field_186135_a; //field_186135_a --> FLOWING_LAVA

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void modifyStaticLavaBlock(CallbackInfo ci) {
    	field_186135_a = ModBlocks.FINITE_LAVA_STILL.getDefaultState().withProperty(BlockFiniteFluid.LEVEL, BlockFiniteFluid.MAXIMUM_CONCEPTUAL_LEVEL); //ModBlocks.INFINITE_LAVA_SOURCE.getDefaultState();
    }
    
    
    @Overwrite (remap = false)
    protected boolean isOceanBlock(ChunkPrimer data, int x, int y, int z, int chunkX, int chunkZ) {
        net.minecraft.block.Block block = data.getBlockState(x, y, z).getBlock();
        return block == ModBlocks.FINITE_WATER_FLOWING || block == ModBlocks.FINITE_WATER_STILL;
    }

}
