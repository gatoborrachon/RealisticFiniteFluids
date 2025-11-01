package com.gatoborrachon.realisticfinitefluids.mixin.earlymixins;

import java.lang.reflect.Field;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.gatoborrachon.realisticfinitefluids.init.ModBlocks;
import com.gatoborrachon.realisticfinitefluids.init.ModConfig;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.gen.ChunkGeneratorOverworld;
import net.minecraft.world.gen.feature.WorldGenLakes;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

@Mixin(ChunkGeneratorOverworld.class)
public class MixinChunkGeneratorOverworld {

	/**
	 * esta funcion cambia el agua y la lava de esas pequeñas lagunas en el mundo por mi agua/lava finitas
	 * @param blockIn
	 * @return
	 */
    @Redirect(
        method = "func_185931_b", //func_185931_b --> populate
        at = @At(
            value = "NEW",
            target = "net/minecraft/world/gen/feature/WorldGenLakes"
        ), remap = true
    )
    private WorldGenLakes redirectLakeGen(Block blockIn) {
        // Si es agua vanilla, la cambiamos por agua finita
        if (blockIn == Blocks.WATER) {
            return new WorldGenLakes(ModBlocks.INFINITE_WATER_SOURCE.setLightOpacity(ModConfig.waterLightOpacity));
        }

        // También interceptamos LAVA aquí para cambiarla por lava finita
        if (blockIn == Blocks.LAVA) {
            return new WorldGenLakes(ModBlocks.INFINITE_LAVA_SOURCE);
        }

        // En cualquier otro caso, regresamos el bloque original
        return new WorldGenLakes(blockIn);
    }

    /**
     * Esta funcion cambia el agua de oceanos y rios del Overworld por mi agua finita
     */
        @Inject(method = "<init>", at = @At("RETURN"))
        private void onConstructed(World worldIn, long seed, boolean generateStructures, String settingsJson, CallbackInfo ci) {
            try {
                //Field oceanBlockField = ChunkGeneratorOverworld.class.getDeclaredField("field_186001_t"); //field_186001_t --> oceanBlock
            	Field oceanBlockField = ObfuscationReflectionHelper.findField(ChunkGeneratorOverworld.class, "field_186001_t");
            	oceanBlockField.setAccessible(true);
                oceanBlockField.set(this, ModBlocks.INFINITE_WATER_SOURCE.setLightOpacity(ModConfig.waterLightOpacity).getDefaultState());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
}
