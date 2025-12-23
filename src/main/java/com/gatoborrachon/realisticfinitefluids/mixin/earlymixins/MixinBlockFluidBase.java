package com.gatoborrachon.realisticfinitefluids.mixin.earlymixins;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.gatoborrachon.realisticfinitefluids.References;
import com.gatoborrachon.realisticfinitefluids.logic.RealisticFiniteFluidFunctions;

import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fluids.BlockFluidBase;
import net.minecraftforge.fluids.Fluid;

@Mixin(BlockFluidBase.class)
public abstract class MixinBlockFluidBase {

    //@Shadow @Final public static PropertyInteger LEVEL; // shadow del campo original
	@Shadow protected int tickRate;

    @Inject(method = "<clinit>", at = @At("RETURN"))
    private static void injectSetLevel(CallbackInfo ci) throws Exception {
        // Quitamos el final y reasignamos
        Field levelField = BlockFluidBase.class.getField("LEVEL");
        Field modifiersFieldLevel = Field.class.getDeclaredField("modifiers");
        modifiersFieldLevel.setAccessible(true);
        modifiersFieldLevel.setInt(levelField, levelField.getModifiers() & ~Modifier.FINAL);

        levelField.set(null, References.LEVEL);
        
        
        
        //For FluidLogged API Compat
        /*Field levelCornersField = BlockFluidBase.class.getField("LEVEL_CORNERS");
        Field modifiersFieldLevelCorners = Field.class.getDeclaredField("modifiers");
        modifiersFieldLevelCorners.setAccessible(true);
        modifiersFieldLevelCorners.setInt(levelCornersField, levelCornersField.getModifiers() & ~Modifier.FINAL);

        levelCornersField.set(null, References.LEVEL_CORNERS);
        */
        
        
    }
    
    
    /*@Shadow @Final @Mutable
    public static PropertyFloat[] LEVEL_CORNERS;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void onClInit(CallbackInfo ci) {
        LEVEL_CORNERS = (PropertyFloat[]) References.LEVEL_CORNERS;
        		
        //		(PropertyFloat[]) new IUnlistedProperty[] {
        //	    new PropertyFloat("level_nw"),  // h00
        //	    new PropertyFloat("level_ne"),  // h10
        //	    new PropertyFloat("level_sw"),  // h01
        //	    new PropertyFloat("level_se")   // h11
        //};
    }*/
    
    
    
    @Inject(
            method = "<init>(Lnet/minecraftforge/fluids/Fluid;Lnet/minecraft/block/material/Material;Lnet/minecraft/block/material/MapColor;)V",
            at = @At("RETURN")
        )
        private void rff$onConstructTail(
                Fluid fluid,
                Material material,
                MapColor mapColor,
                CallbackInfo ci
        ) {
    	
        this.tickRate = fluid.isGaseous() ? 20 : fluid.getViscosity() / 200;
    	
        }
    
    
    @Overwrite(remap = false)
    public Vec3d getFlowVector(IBlockAccess world, BlockPos pos) {
    	return RealisticFiniteFluidFunctions.calculateFlowVector(world, pos, false);
    }

}
