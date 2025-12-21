package com.gatoborrachon.realisticfinitefluids.mixin.earlymixins;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.gatoborrachon.realisticfinitefluids.References;
import com.gatoborrachon.realisticfinitefluids.logic.RealisticFiniteFluidFunctions;

import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;

@Mixin(BlockLiquid.class)
public abstract class MixinBlockLiquid {

	
	private static String getCorrectLEVELField() {
	    Boolean isDev = (Boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment");
	    return (isDev != null && isDev) ? "LEVEL" : "field_176367_b";
	}
	
    //@Shadow(remap = References.onDev) @Final 
    //public static PropertyInteger field_176367_b; // LEVEL

    @Inject(method = "<clinit>", at = @At("RETURN"))
    private static void injectSetLevel(CallbackInfo ci) throws Exception {
        // Quitamos el final y reasignamos
        Field levelField = BlockLiquid.class.getField(getCorrectLEVELField());
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(levelField, levelField.getModifiers() & ~Modifier.FINAL);

        levelField.set(null, References.LEVEL); // reasignamos a tu LEVEL
    }
    
    
    
    @Overwrite//(remap = References.onDev) //getFlow
    public Vec3d getFlow(IBlockAccess worldIn, BlockPos pos, IBlockState state) {
    	return RealisticFiniteFluidFunctions.calculateFlowVector(worldIn, pos, false);
    }
    
    
}
