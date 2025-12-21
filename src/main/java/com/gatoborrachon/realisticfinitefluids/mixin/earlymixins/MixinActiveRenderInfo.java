package com.gatoborrachon.realisticfinitefluids.mixin.earlymixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import com.gatoborrachon.realisticfinitefluids.References;
import com.gatoborrachon.realisticfinitefluids.interfaces.IRealisticFiniteFluid;
import com.gatoborrachon.realisticfinitefluids.logic.RealisticFiniteFluidFunctions;

import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

@Mixin(ActiveRenderInfo.class)
public class MixinActiveRenderInfo {

	@Overwrite(remap = References.onDev) //getBlockStateAtEntityViewpoint
    public static IBlockState func_186703_a(World worldIn, Entity entityIn, float p_186703_2_)
    {
        Vec3d vec3d = ActiveRenderInfo.projectViewFromEntity(entityIn, (double)p_186703_2_);
        BlockPos blockpos = new BlockPos(vec3d);
        IBlockState iblockstate = worldIn.getBlockState(blockpos);

        if (iblockstate.getMaterial().isLiquid())
        {
            float f = 0.0F;
            //TODO Confirmar que no me beneficio de usar IRealisticFiniteFluid
            if (RealisticFiniteFluidFunctions.getBlock(worldIn, blockpos, iblockstate) instanceof BlockLiquid) // BlockLiquid)
            {
                f = 1F - ((float)((IRealisticFiniteFluid)RealisticFiniteFluidFunctions.getBlock(worldIn, blockpos, iblockstate)).getConceptualVolume(worldIn, blockpos, iblockstate)/(float)References.MAXIMUM_CONCEPTUAL_LEVEL); //BlockLiquid.getLiquidHeightPercent(((Integer)iblockstate.getValue(BlockLiquid.LEVEL)).intValue()) - 0.11111111F;
            }

            float f1 = (float)(blockpos.getY() + 1) - f;

            if (vec3d.y >= (double)f1)
            {
                iblockstate = worldIn.getBlockState(blockpos.up());
            }
        }

        return RealisticFiniteFluidFunctions.getBlock(worldIn, blockpos, iblockstate).getStateAtViewpoint(iblockstate, worldIn, blockpos, vec3d);
    }
}
