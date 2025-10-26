package com.gatoborrachon.realisticfinitefluids.mixin.earlymixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import com.gatoborrachon.realisticfinitefluids.init.ModBlocks;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.biome.BiomeSwamp;

@Mixin(BiomeSwamp.class)
public abstract class MixinBiomeSwamp {

    // Variante MCP (dev). Requiere refmap funcionando
    @ModifyArg(
        method = "func_180622_a", // genTerrainBlocks
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/chunk/ChunkPrimer;setBlockState(IIILnet/minecraft/block/state/IBlockState;)V"
        ),
        index = 3,
        remap = true,
        require = 0 // no rompas si no se encuentra este
    )
    private IBlockState rff$replaceWater_mcp(IBlockState state) {
        return (state.getBlock() == Blocks.WATER)
                ? ModBlocks.INFINITE_WATER_SOURCE.getDefaultState()
                : state;
    }

    // Variante SRG (runtime). Útil si tu refmap no carga fuera de dev
    @ModifyArg(
        method = "func_180622_a", // genTerrainBlocks
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/chunk/ChunkPrimer;func_177855_a(IIILnet/minecraft/block/state/IBlockState;)V" // setBlockState SRG
        ),
        index = 3,
        remap = false,
        require = 0
    )
    private IBlockState rff$replaceWater_srg(IBlockState state) {
        return (state.getBlock() == Blocks.WATER)
                ? ModBlocks.INFINITE_WATER_SOURCE.getDefaultState()
                : state;
    }
    
    /*@Redirect(
        method = "func_180622_a", //func_180622_a --> genTerrainBlocks
        at = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/world/chunk/ChunkPrimer;setBlockState(IIILnet/minecraft/block/state/IBlockState;)V"
            ),
            remap = true
        )
        private void redirectWaterSet(ChunkPrimer primer, int x, int y, int z, IBlockState state) {
            if (state.getBlock() == Blocks.WATER) {
                primer.setBlockState(x, y, z, ModBlocks.INFINITE_WATER_SOURCE.setLightOpacity(ModConfig.waterOpacity).getDefaultState());
            } else {
                primer.setBlockState(x, y, z, state);
            }
        }*/
}
