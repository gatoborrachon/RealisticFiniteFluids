package com.gatoborrachon.realisticfinitefluids.mixin.latemixins;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import com.gatoborrachon.realisticfinitefluids.References;

import git.jbredwards.fluidlogged_api.api.util.FluidloggedUtils;
import git.jbredwards.fluidlogged_api.mod.asm.iface.IConfigFluidBox;
import git.jbredwards.fluidlogged_api.mod.asm.iface.IWaterHeight;
import git.jbredwards.fluidlogged_api.mod.common.config.FluidloggedAPIConfig;
import git.jbredwards.fluidlogged_api.mod.common.fluid.handler.FluidCollisionHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fluids.Fluid;

@Mixin(FluidCollisionHandler.class)
public class MixinFluidCollisionHandler {
	
    @Nonnull
    @Shadow
    @Final
    public static ThreadLocal<IWaterHeight> cacheHeight;
	
    @Unique
    private static double applyQolOffset(final double fluidHeight) {
        final double qolOffset = 0.015; // move the level to check down slightly, so things like lava next to soul sand don't light players on fire
        return (int)fluidHeight == fluidHeight ? fluidHeight : fluidHeight - qolOffset;
    }
    
    @Unique
    private static boolean isYWithinFluid(@Nullable final Fluid fluid, @Nonnull final BlockPos pos, final double minY, final double maxY, final double fluidHeight, final boolean checkCache) {
        final boolean gas = fluid != null && fluid.isLighterThanAir();
        final boolean isWithin = gas ? maxY > pos.getY() + 1 - fluidHeight && minY < pos.getY() + 1 : minY < pos.getY() + fluidHeight && maxY > pos.getY();

        if(!isWithin) return false;
        else if(checkCache && FluidloggedAPIConfig.ignoreLowFluidCollision) {
            @Nullable final IWaterHeight waterHeight = cacheHeight.get();
            if(waterHeight != null) {
                @Nullable final IConfigFluidBox.HeightBox box = waterHeight.getBox();
                final double height = Math.min(pos.getY() + fluidHeight - minY, 1);

                if(box == null) waterHeight.setBox(new IConfigFluidBox.HeightBox(gas ? 1 - height : 0, gas ? 1 : height));
                else if(box.min != 0 || box.max != 1) waterHeight.setBox(new IConfigFluidBox.HeightBox(gas ? Math.min(box.min, 1 - height) : 0, gas ? 1 : Math.max(box.max, height)));
            }
        }

        return true;
    }
	
	@Overwrite
    //@SuppressWarnings("UnnecessaryLocalVariable")
    static boolean isPointWithinFluid(@Nonnull final BlockPos pos, final double xIn, final double minY, final double maxY, final double zIn, @Nonnull final IExtendedBlockState state, final boolean checkCache) {
		//if (!(RealisticFiniteFluidFunctions.getBlock(state) instanceof IRealisticFiniteFluid)) return false;
		
		//IRealisticFiniteFluid block = ((IRealisticFiniteFluid)RealisticFiniteFluidFunctions.getBlock(state));
		@Nonnull final float[][] corners = new float[2][2];
        corners[0][0] = state.getValue(References.LEVEL_CORNERS[0]); //state.getValue(References.LEVEL_CORNERS[0]);
        corners[0][1] = state.getValue(References.LEVEL_CORNERS[1]); //state.getValue(References.LEVEL_CORNERS[1]);
        corners[1][1] = state.getValue(References.LEVEL_CORNERS[2]); //state.getValue(References.LEVEL_CORNERS[2]);
        corners[1][0] = state.getValue(References.LEVEL_CORNERS[3]); //state.getValue(References.LEVEL_CORNERS[3]);

        // unit position of the point, relative to the fluid pos
        final double x = MathHelper.clamp(xIn, pos.getX(), pos.getX() + 1) - pos.getX();
        final double z = MathHelper.clamp(zIn, pos.getZ(), pos.getZ() + 1) - pos.getZ();

        // gets the exact slope height of the fluid at the (x, z) point
        final double x_weight_0 = 1 - x, x_weight_1 = x;
        final double z_weight_0 = 1 - z, z_weight_1 = z;
        final double fluidHeightAtPoint
                = corners[0][0] * x_weight_0 * z_weight_0
                + corners[0][1] * x_weight_0 * z_weight_1
                + corners[1][1] * x_weight_1 * z_weight_1
                + corners[1][0] * x_weight_1 * z_weight_0;
        return isYWithinFluid(FluidloggedUtils.getFluidFromState(state), pos, minY, maxY, applyQolOffset(fluidHeightAtPoint), checkCache);
    }

}
