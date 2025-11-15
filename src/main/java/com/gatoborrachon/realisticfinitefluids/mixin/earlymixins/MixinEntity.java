package com.gatoborrachon.realisticfinitefluids.mixin.earlymixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.gatoborrachon.realisticfinitefluids.blocks.BlockFiniteFluid;
import com.gatoborrachon.realisticfinitefluids.coremod.utils.IEntityExtended;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

@Mixin(Entity.class)
public abstract class MixinEntity implements IEntityExtended {

	/**
	 * Para arreglar el Overlay (que solo se ejecute cuando la funcion isEntityInsideMaterialFromOverlay da la info necesaria)
	 * 
	 * Para contrlar las interacciones de agua vanilla, y tambien las del agua finita, para controlar la altura a la que el jugador se eleva en el agua dependiendo el LEVEL
	 */
	// === Campos básicos ===
    @Shadow(remap = true) public double field_70165_t; // posX
    @Shadow(remap = true) public double field_70163_u; // posY
    @Shadow(remap = true) public double field_70161_v; // posZ
    @Shadow(remap = true) public World field_70170_p;  // world

    @Shadow(remap = true) public boolean field_70171_ac; // inWater
    @Shadow(remap = true) protected boolean field_70148_d; // firstUpdate
    @Shadow(remap = true) public float field_70143_R; // fallDistance

    // === Métodos vanilla ===
    @Shadow(remap = true) public abstract float func_70047_e(); // getEyeHeight
    @Shadow(remap = true) public abstract void func_71061_d_(); // doWaterSplashEffect
    @Shadow(remap = true) public abstract void func_70066_B(); // extinguish
    @Shadow(remap = true) public abstract AxisAlignedBB func_174813_aQ(); // getEntityBoundingBox
    @Shadow(remap = true) public abstract Entity func_184187_bx(); // getRidingEntity

    ///////////////////////////////////////
    /**
     * Copia de isInsideOfMaterial que no tiene mi patch, perfecta para el render
     */
    @Override
    public boolean isInsideOfMaterialForRender(Material materialIn) {
        Entity self = (Entity) (Object) this; // casteo de mixin
        double d0 = self.posY + (double)self.getEyeHeight();
        BlockPos blockpos = new BlockPos(self.posX, d0, self.posZ);
        IBlockState iblockstate = self.world.getBlockState(blockpos);

        Boolean result = iblockstate.getBlock().isEntityInsideMaterial(
                self.world, blockpos, iblockstate, self, d0, materialIn, true);
        if (result != null) return result;

        if (iblockstate.getMaterial() == materialIn) {
            return net.minecraftforge.common.ForgeHooks.isInsideOfMaterial(materialIn, self, blockpos);
        } else {
            return false;
        }
    }
    
    
    ////////////////////////////////////////
    
    // === Overlay check (isInsideOfMaterial) ===
    @Inject(
        method = "func_70055_a", // isInsideOfMaterial
        at = @At("HEAD"),
        cancellable = true,
        remap = true
    )
    private void injectFiniteFluidOverlay(Material materialIn, CallbackInfoReturnable<Boolean> cir) {
        Entity self = (Entity)(Object)this;

        double eyeY = this.field_70163_u + this.func_70047_e();
        BlockPos blockPos = new BlockPos(this.field_70165_t, eyeY, this.field_70161_v);
        IBlockState state = this.field_70170_p.getBlockState(blockPos);

        if (state.getBlock() instanceof BlockFiniteFluid && state.getMaterial() == Material.WATER) {
            Boolean result = ((BlockFiniteFluid) state.getBlock())
                                .isEntityInsideMaterialForOverlay(this.field_70170_p, blockPos, state, self, eyeY, materialIn);
            if (result != null) {
                cir.setReturnValue(result);
            }
        }
    }
    
    
    
    
    
    
    
    ///////////////////////

    // === Método vanilla original (para fallback en agua normal) ===
    public boolean originalHandleWaterMovement() {
        if (this.func_184187_bx() instanceof EntityBoat) {
            this.field_70171_ac = false;
        } else if (this.field_70170_p.handleMaterialAcceleration(
                       this.func_174813_aQ().grow(0.0D, -0.4000000059604645D, 0.0D).shrink(0.001D),
                       Material.WATER, (Entity)(Object)this)) {

            if (!this.field_70171_ac && !this.field_70148_d) {
                this.func_71061_d_(); // splash
            }

            this.field_70143_R = 0.0F;
            this.field_70171_ac = true;
            this.func_70066_B(); // extinguish
        } else {
            this.field_70171_ac = false;
        }

        return this.field_70171_ac;
    }
    
    
    
    

    // === Custom handleWaterMovement ===
    @Inject(
        method = "func_70072_I", // handleWaterMovement
        at = @At("HEAD"),
        cancellable = true,
        remap = true
    )
    private void injectHandleWaterMovement(CallbackInfoReturnable<Boolean> cir) {
        AxisAlignedBB bb = this.func_174813_aQ();
        boolean stillInWater = false;

        int minX = MathHelper.floor(bb.minX);
        int maxX = MathHelper.ceil(bb.maxX);
        int minY = MathHelper.floor(bb.minY);
        int maxY = MathHelper.ceil(bb.maxY);
        int minZ = MathHelper.floor(bb.minZ);
        int maxZ = MathHelper.ceil(bb.maxZ);

        for (int x = minX; x < maxX; x++) {
            for (int y = minY; y < maxY; y++) {
                for (int z = minZ; z < maxZ; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    IBlockState state = field_70170_p.getBlockState(pos);
                    Block block = state.getBlock();

                    // Agua vanilla --> usar el método original
                    if (block instanceof BlockLiquid && !(block instanceof BlockFiniteFluid)) {
                        cir.setReturnValue(this.originalHandleWaterMovement());
                        cir.cancel();
                        return;
                    }

                    // Lógica custom para agua finita
                    double fluidSurface = pos.getY() + 1.0D;
                    if (block instanceof BlockFiniteFluid) {
                        int level = BlockFiniteFluid.getConceptualVolume(null, null, state);//state.getValue(BlockFiniteFluid.LEVEL) + 1;
                        fluidSurface = pos.getY() + (level / (double)BlockFiniteFluid.MAXIMUM_CONCEPTUAL_LEVEL);
                    } else {
                        continue;
                    }

                    if ((bb.minY + 0.2D) < fluidSurface) {
                        stillInWater = true;
                        break;
                    }
                }
                if (stillInWater) break;
            }
            if (stillInWater) break;
        }

        // Chequear bloque de abajo
        BlockPos below = new BlockPos(bb.minX, bb.minY - 1, bb.minZ);
        //BlockPos above = new BlockPos(bb.minX, bb.minY + 1, bb.minZ);
        IBlockState stateBelow = field_70170_p.getBlockState(below);
        //IBlockState stateAbove = field_70170_p.getBlockState(above);
        Block blockBelow = stateBelow.getBlock();
        //Block blockAbove = stateAbove.getBlock();

        if (!(blockBelow instanceof BlockFiniteFluid || stateBelow.getMaterial() == Material.WATER)) {
            cir.setReturnValue(this.originalHandleWaterMovement());
            cir.cancel();
            return;
        }

        // Si está en agua --> aplicar lógica custom
        if (stillInWater) {
            if (!this.field_70171_ac && !this.field_70148_d) this.func_71061_d_();
            this.field_70143_R = 0.0F;
            this.field_70171_ac = true;
            this.func_70066_B();
            cir.setReturnValue(true);
        } else {
            this.field_70171_ac = false;
            cir.setReturnValue(false);
        }

        cir.cancel();
    }
}


