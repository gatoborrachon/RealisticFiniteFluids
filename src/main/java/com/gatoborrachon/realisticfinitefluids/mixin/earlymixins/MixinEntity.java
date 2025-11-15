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
                        fluidSurface = pos.getY() + (level / 16.0D);
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
    
    
    
    //@Shadow (remap = true) public boolean field_70171_ac; // field_70171_ac --> inWater
    //@Shadow (remap = true) protected boolean field_70148_d; // field_70148_d --> firstUpdate
    //@Shadow (remap = true) public float field_70143_R; // fallDistance --> field_70143_R
    //@Shadow (remap = true) public abstract void func_71061_d_(); // doWaterSplashEffect --> func_71061_d_
    //@Shadow (remap = true) public abstract void func_70066_B(); // extinguish --> func_70066_B
    //@Shadow (remap = true) public abstract AxisAlignedBB func_174813_aQ(); // getEntityBoundingBox --> func_174813_aQ
    //@Shadow (remap = true) public abstract Entity func_184187_bx(); // getRidingEntity --> func_184187_bx

    
    /**
     * Returns if this entity is in water and will end up adding the waters velocity to the entity
     */
    /*public boolean originalHandleWaterMovement()
    {
        if (this.func_184187_bx() instanceof EntityBoat)
        {
            this.field_70171_ac = false;
        }
        else if (this.field_70170_p.handleMaterialAcceleration(this.func_174813_aQ().grow(0.0D, -0.4000000059604645D, 0.0D).shrink(0.001D), Material.WATER, (Entity)(Object)this))
        {
            if (!this.field_70171_ac && !this.field_70148_d)
            {
                this.func_71061_d_();
            }

            this.field_70143_R = 0.0F;
            this.field_70171_ac = true;
            this.func_70066_B();
        }
        else
        {
            this.field_70171_ac = false;
        }

        return this.field_70171_ac;
    }
    
    @Inject(method = "func_70072_I", at = @At("HEAD"), cancellable = true, remap = true) // func_70072_I --> handleWaterMovement
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

                    // Si es agua vanilla y estamos en un bloque de agua normal --> usar código original
                    if (block instanceof BlockLiquid && !(block instanceof BlockFiniteFluid)) {
                        // Ejecutar lógica original de HandleWaterMovement
                        cir.setReturnValue(this.originalHandleWaterMovement());
                        cir.cancel();
                        return;
                    }

                    // Lógica custom para tu BlockFiniteFluid
                    double fluidSurface = pos.getY() + 1.0D;
                    if (block instanceof BlockFiniteFluid) {
                        int level = state.getValue(BlockFiniteFluid.LEVEL) + 1;
                        fluidSurface = pos.getY() + (level / 16.0D);
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

        // Aquí chequeamos el bloque de abajo para decidir si aplicar la aceleración
        BlockPos below = new BlockPos(bb.minX, bb.minY - 1, bb.minZ);
        IBlockState stateBelow = field_70170_p.getBlockState(below);
        Block blockBelow = stateBelow.getBlock();

        if (!(blockBelow instanceof BlockFiniteFluid || stateBelow.getMaterial() == Material.WATER)) {
            // No hay agua abajo --> fallback a lógica original
            cir.setReturnValue(this.originalHandleWaterMovement());
            cir.cancel();
            return;
        }

        // Si sí hay agua abajo --> aplicar tu lógica custom
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
    }*/
    
    
    
    
    
    ///////////////////////////////////////////////////////////////////////////////////////////

	
	
	
	
    /*@	@Shadow public double posX;
    @Shadow public double posY;
    @Shadow public double posZ;
    @Shadow public abstract float getEyeHeight();
    @Shadow public World world;

    @Inject(
        method = "isInsideOfMaterial",
        at = @At("HEAD"),
        cancellable = true
    )
    private void injectFiniteFluidOverlay(Material materialIn, CallbackInfoReturnable<Boolean> cir) {
        Entity self = (Entity)(Object)this;

        double eyeY = this.posY + this.getEyeHeight();
        BlockPos blockPos = new BlockPos(this.posX, eyeY, this.posZ);
        IBlockState state = this.world.getBlockState(blockPos);

        if (state.getBlock() instanceof BlockFiniteFluid) {
            Boolean result = ((BlockFiniteFluid) state.getBlock())
                                .isEntityInsideMaterialForOverlay(this.world, blockPos, state, self, eyeY, materialIn);
            if (result != null) {
                cir.setReturnValue(result);
            }
        }
    }	
	
	
	

	
	
	
	
	
	/////////////////////////////////
    //@Shadow public World world;
    @Shadow public boolean inWater;
    @Shadow protected boolean firstUpdate;
    @Shadow public float fallDistance;
    @Shadow public abstract void doWaterSplashEffect();
    @Shadow public abstract void extinguish();
    @Shadow public abstract AxisAlignedBB getEntityBoundingBox();
    //@Shadow public double posY;
    @Shadow public double motionX, motionY, motionZ;
    @Shadow public abstract Entity getRidingEntity();

    /**
     * Returns if this entity is in water and will end up adding the waters velocity to the entity
     */
    /*public boolean originalHandleWaterMovement()
    {
        if (this.getRidingEntity() instanceof EntityBoat)
        {
            this.inWater = false;
        }
        else if (this.world.handleMaterialAcceleration(this.getEntityBoundingBox().grow(0.0D, -0.4000000059604645D, 0.0D).shrink(0.001D), Material.WATER, (Entity)(Object)this))
        {
            if (!this.inWater && !this.firstUpdate)
            {
                this.doWaterSplashEffect();
            }

            this.fallDistance = 0.0F;
            this.inWater = true;
            this.extinguish();
        }
        else
        {
            this.inWater = false;
        }

        return this.inWater;
    }
    
    @Inject(method = "handleWaterMovement", at = @At("HEAD"), cancellable = true)
    private void injectHandleWaterMovement(CallbackInfoReturnable<Boolean> cir) {
        AxisAlignedBB bb = this.getEntityBoundingBox();
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
                    IBlockState state = world.getBlockState(pos);
                    Block block = state.getBlock();

                    // Si es agua vanilla y estamos en un bloque de agua normal --> usar código original
                    if (block instanceof BlockLiquid && !(block instanceof BlockFiniteFluid)) {
                        // Ejecutar lógica original de HandleWaterMovement
                        cir.setReturnValue(this.originalHandleWaterMovement());
                        cir.cancel();
                        return;
                    }

                    // Lógica custom para tu BlockFiniteFluid
                    double fluidSurface = pos.getY() + 1.0D;
                    if (block instanceof BlockFiniteFluid) {
                        int level = state.getValue(BlockFiniteFluid.LEVEL) + 1;
                        fluidSurface = pos.getY() + (level / 16.0D);
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

        // Aquí chequeamos el bloque de abajo para decidir si aplicar la aceleración
        BlockPos below = new BlockPos(bb.minX, bb.minY - 1, bb.minZ);
        IBlockState stateBelow = world.getBlockState(below);
        Block blockBelow = stateBelow.getBlock();

        if (!(blockBelow instanceof BlockFiniteFluid || stateBelow.getMaterial() == Material.WATER)) {
            // No hay agua abajo --> fallback a lógica original
            cir.setReturnValue(this.originalHandleWaterMovement());
            cir.cancel();
            return;
        }

        // Si sí hay agua abajo --> aplicar tu lógica custom
        if (stillInWater) {
            if (!this.inWater && !this.firstUpdate) this.doWaterSplashEffect();
            this.fallDistance = 0.0F;
            this.inWater = true;
            this.extinguish();
            cir.setReturnValue(true);
        } else {
            this.inWater = false;
            cir.setReturnValue(false);
        }

        cir.cancel();
    }*/
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    /*@Inject(method = "handleWaterMovement", at = @At("HEAD"), cancellable = true)
    private void injectHandleWaterMovement(CallbackInfoReturnable<Boolean> cir) {
        AxisAlignedBB bb = this.getEntityBoundingBox();
        boolean stillInWater = false;
        Block actualBlock = null;

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
                    IBlockState state = world.getBlockState(pos);
                    Block block = state.getBlock();
                    actualBlock = block;
                    
                    double fluidSurface = pos.getY() + 1.0D;

                    if (block instanceof BlockFiniteFluid) {
                        int level = state.getValue(BlockFiniteFluid.LEVEL) + 1;
                        fluidSurface = pos.getY() + (level / 16.0D);
                    } else if (block instanceof BlockLiquid) {
                        int level = state.getValue(BlockLiquid.LEVEL);
                        fluidSurface = pos.getY() + 1.0D - BlockLiquid.getLiquidHeightPercent(level);
                    } else {
                        continue;
                    }

                    //*if (block instanceof BlockFiniteFluid && (bb.minY+0.2D) < fluidSurface) { //le añado un poco de altura para intentar acercarme al fenotipo vanilla
                        stillInWater = true;
                        break;
                    }
                    
                    if (block instanceof BlockLiquid && bb.minY < fluidSurface) {
                        stillInWater = true;
                        break;
                    } /*
                    
                    if ((bb.minY+0.2D) < fluidSurface) {
                        stillInWater = true;
                        break;
                    }
                    
                    
                }
                if (stillInWater) break;
            }
            if (stillInWater) break;
        }*/

        /*if (actualBlock != null && actualBlock instanceof BlockLiquid) {
            if (this.getRidingEntity() instanceof EntityBoat)
            {
                this.inWater = false;
                cir.setReturnValue(false);
            }
            else if (this.world.handleMaterialAcceleration(this.getEntityBoundingBox().grow(0.0D, -0.4000000059604645D, 0.0D).shrink(0.001D), Material.WATER, (Entity)(Object)this))
            {
                if (!this.inWater && !this.firstUpdate)
                {
                    this.doWaterSplashEffect();
                }

                this.fallDistance = 0.0F;
                this.inWater = true;
                this.extinguish();
                cir.setReturnValue(true);
            }
            else
            {
                this.inWater = false;
                cir.setReturnValue(false);
            }
        }//*
        
        if (stillInWater) {
            if (!this.inWater && !this.firstUpdate) {
                this.doWaterSplashEffect();
            }
            this.fallDistance = 0.0F;
            this.inWater = true;
            this.extinguish();
            cir.setReturnValue(true);
        } else {
            this.inWater = false;
            cir.setReturnValue(false);
        }
        
        
        

        
        cir.cancel();
    }*/

    
    
    
    
    
    
    
    
    /*@Shadow public World world;
    @Shadow protected boolean inWater;
    @Shadow protected boolean firstUpdate;
    @Shadow protected float fallDistance;
    @Shadow protected abstract void doWaterSplashEffect();
    @Shadow protected abstract void extinguish();
    @Shadow public abstract AxisAlignedBB getEntityBoundingBox();
    @Shadow public double posY;
    @Shadow public double motionX, motionY, motionZ;

    @Inject(method = "handleWaterMovement", at = @At("HEAD"), cancellable = true)
    private void injectHandleWaterMovement(CallbackInfoReturnable<Boolean> cir) {
        AxisAlignedBB bb = this.getEntityBoundingBox();

        boolean inWaterLocal = false;

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
                    IBlockState state = world.getBlockState(pos);
                    Block block = state.getBlock();

                    if (block instanceof BlockFiniteFluid) {
                        int level = state.getValue(BlockFiniteFluid.LEVEL) + 1;
                        float height = level / 16.0F;
                        double fluidSurface = pos.getY() + height;

                        if ((bb.minY+0.1D) < fluidSurface) { // +0.1D para bajar un poco al Player, intentando semejar el agua vanilla
                            inWaterLocal = true;
                            break;
                        }
                    }
                }
                if (inWaterLocal) break;
            }
            if (inWaterLocal) break;
        }

        if (inWaterLocal) {
            if (!this.inWater && !this.firstUpdate) {
                this.doWaterSplashEffect();
            }
            this.fallDistance = 0.0F;
            this.inWater = true;
            this.extinguish();
            cir.setReturnValue(true);
        } else {
            this.inWater = false;
            cir.setReturnValue(false);
        }

        cir.cancel();
    }*/
    
    
    
    /*@Shadow public World world;
    @Shadow public boolean inWater;
    @Shadow protected boolean firstUpdate;
    @Shadow public float fallDistance;
    @Shadow public abstract void doWaterSplashEffect();
    @Shadow public abstract void extinguish();
    @Shadow public abstract AxisAlignedBB getEntityBoundingBox();
    @Shadow public double posY;
    @Shadow public double motionX, motionY, motionZ;

    @Inject(method = "handleWaterMovement", at = @At("HEAD"), cancellable = true)
    private void injectHandleWaterMovement(CallbackInfoReturnable<Boolean> cir) {
        AxisAlignedBB bb = this.getEntityBoundingBox();
        BlockPos pos = new BlockPos(bb.minX, bb.minY, bb.minZ);

        IBlockState state = world.getBlockState(pos);
        Block block = state.getBlock();

        // Si es tu bloque de fluido finito
        if (block instanceof BlockFiniteFluid) {
            int level = state.getValue(BlockFiniteFluid.LEVEL) + 1;
            float height = level / 16.0F; // 0.0625F - 1.0F
            double fluidSurface = pos.getY() + height;

            if ((this.posY+0.1D) < fluidSurface) { // +0.1D para bajar un poco al Player, intentando semejar el agua vanilla
                if (!this.inWater && !this.firstUpdate) {
                    this.doWaterSplashEffect();
                }
                this.fallDistance = 0.0F;
                this.inWater = true;
                this.extinguish();

                // lo importante: retornamos true para que el player pueda nadar
                cir.setReturnValue(true);
                cir.cancel();
            } else {
                this.inWater = false;
                cir.setReturnValue(false);
                cir.cancel();
            }
        }
    }*/
	

    
}


