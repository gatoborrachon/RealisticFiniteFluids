package com.gatoborrachon.realisticfinitefluids.mixin.latemixins;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.gatoborrachon.realisticfinitefluids.init.ModBlocks;

import jaredbgreat.climaticbiome.generation.chunk.ChunkGenClimaticRealistic;
import net.minecraft.block.state.IBlockState;

@Mixin(ChunkGenClimaticRealistic.class)
public class MixinChunkGenClimaticRealistic {

    @Shadow(remap = true)
    @Final
    @Mutable
    private static IBlockState WATER; // Campo estático final original

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void modifyStaticWaterBlock(CallbackInfo ci) {
        WATER = ModBlocks.INFINITE_WATER_SOURCE.getDefaultState();
    }
}
