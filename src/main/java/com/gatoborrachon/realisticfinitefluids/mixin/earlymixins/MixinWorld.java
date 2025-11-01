package com.gatoborrachon.realisticfinitefluids.mixin.earlymixins;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.gatoborrachon.realisticfinitefluids.blocks.BlockFiniteFluid;
import com.gatoborrachon.realisticfinitefluids.init.ModBlocks;

@Mixin(World.class)
public class MixinWorld {

	/**
	 * Funcion para congelar el agua de antemano al generar un mundo (o al recargarlo? ni idea, es codigo vanilla asi que yo creo que no hay problema)
	 */
    @Inject(method = "canBlockFreezeBody", at = @At("HEAD"), cancellable = true, remap = false)
    private void injectFiniteWaterFreeze(BlockPos pos, boolean noWaterAdj, CallbackInfoReturnable<Boolean> cir) {
        World world = (World) (Object) this;
        Biome biome = world.getBiome(pos);
        float temp = biome.getTemperature(pos);

        if (temp >= 0.15F) {
            return;
        }

        if (pos.getY() < 0 || pos.getY() >= 256) {
            return;
        }

        if (world.getLightFor(EnumSkyBlock.BLOCK, pos) >= 10) {
            return;
        }

        IBlockState state = world.getBlockState(pos);
        Block block = state.getBlock();

        // Considera tus bloques como equivalentes a WATER / FLOWING_WATER
        boolean isFiniteWater = block == ModBlocks.FINITE_WATER_STILL || block == ModBlocks.FINITE_WATER_FLOWING || block == ModBlocks.INFINITE_WATER_SOURCE;
        //boolean isVanillaWater = block == Blocks.WATER || block == Blocks.FLOWING_WATER;

        if ((isFiniteWater/* || isVanillaWater*/)
                && state.getValue(BlockFiniteFluid.LEVEL) >= 12) { //deberia ser >=15 para que solo los bloques completos se congelen, pero bueno, vamos a darle un mayor margen

            if (!noWaterAdj) {
                cir.setReturnValue(true);
                return;
            }

            boolean flag =
                    isWaterLike(world, pos.west()) &&
                    isWaterLike(world, pos.east()) &&
                    isWaterLike(world, pos.north()) &&
                    isWaterLike(world, pos.south());

            if (!flag) {
                cir.setReturnValue(true);
            }
        }
    }

    private boolean isWaterLike(World world, BlockPos pos) {
        Block block = world.getBlockState(pos).getBlock();
        return //block == Blocks.WATER ||
               //block == Blocks.FLOWING_WATER ||
               block == ModBlocks.FINITE_WATER_STILL ||
               block == ModBlocks.FINITE_WATER_FLOWING ||
               block == ModBlocks.INFINITE_WATER_SOURCE;
    }
}
