package com.gatoborrachon.realisticfinitefluids.mixin.earlymixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.biome.BiomeDecorator;
import net.minecraft.world.gen.feature.WorldGenLiquids;

@Mixin(BiomeDecorator.class)
public abstract class MixinBiomeDecorator {

    // Redirect a la creación de WorldGenLiquids con agua
    @Redirect(
        method = "func_150513_a", //func_150513_a --> genDecorations
        at = @At(
            value = "NEW",
            target = "net/minecraft/world/gen/feature/WorldGenLiquids"
        ), remap = true
    )
    
    /**
     * NOTA --> Al parecer, creo que cuando vanilla espera un FLOWING_WATER o FLOWING_LAVA y le meto mis bloques, crashea por problemas
     * de que las cosas hacen tick antes de tiempo,
     * 
     * No se si en alguna parte del codigo se obligue a el agua flowing a hacer tick, y cuando obligan a mis bloques a hacer tick
     * todo se va al carajo.
     * 
     * Considerar ver en que codigo se fuerza esta ejecucion de ticks, o si mejor reemplazo la clase WorldGenLiquids por una custom
     * que no use Block, sino IBlockState (estos nunca crashean, creo xd)
     * 
     * Aunque comprendo que usen FLOWING_WATER o LAVA, pues tan pronto aparece ese bloque, debe fluir, y si ponen un bloque WATER
     * o LAVA a secas, supongo que esta se queda ahi quieta como mensa
     * 
     * 
     */
    private WorldGenLiquids redirectWorldGenLiquidsForWaterOrLava(Block blockIn) {
        if (blockIn == Blocks.FLOWING_WATER) {
            return new WorldGenLiquids(Blocks.AIR); // tu agua
        } else if (blockIn == Blocks.FLOWING_LAVA) {
            return new WorldGenLiquids(Blocks.AIR); // tu lava
        }
        return new WorldGenLiquids(blockIn); // cualquier otro bloque
    }
}
