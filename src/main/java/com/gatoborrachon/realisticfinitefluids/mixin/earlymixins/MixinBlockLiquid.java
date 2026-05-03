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

import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

@Mixin(BlockLiquid.class)
public abstract class MixinBlockLiquid extends Block {

	
	public MixinBlockLiquid(Material materialIn) {
		super(materialIn);
		// TODO Auto-generated constructor stub
	}

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
    
    
    
    @Overwrite(remap = References.onDev) //getFlow
    public Vec3d func_189543_a(IBlockAccess worldIn, BlockPos pos, IBlockState state) {
    	return RealisticFiniteFluidFunctions.calculateFlowVector(worldIn, pos, false);
    }
    
    
    
    @Override
    public EnumPushReaction getPushReaction(IBlockState state) {
        return EnumPushReaction.NORMAL;
    }

	//@Override
	//public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
		// TODO Auto-generated method stub
	//	super.breakBlock(worldIn, pos, state);
	//}

    
	
    
    
    
    
}
