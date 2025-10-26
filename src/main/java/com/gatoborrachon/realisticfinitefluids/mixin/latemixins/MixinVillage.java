package com.gatoborrachon.realisticfinitefluids.mixin.latemixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.gatoborrachon.realisticfinitefluids.References;
import com.gatoborrachon.realisticfinitefluids.mixin.earlymixins.MixinStructureComponent;

import net.minecraft.block.state.IBlockState;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureVillagePieces;

//@Mixin(targets = "net.minecraft.world.gen.structure.StructureVillagePieces$Village")
@Mixin(StructureVillagePieces.Village.class)
public abstract class MixinVillage extends MixinStructureComponent {

	@Shadow (remap = References.onDev)
	//protected int averageGroundLvl; 
	protected int field_143015_k; //field_143015_k --> averageGroundLvl
	
	
	/*
	     boolean isDev = (Boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment");
    
    if (isDev != null && isDev) {
    	@Shadow (remap = false)
    	protected int field_143015_k; //field_143015_k --> averageGroundLvl
    } else {
    	@Shadow (remap = false)
    	protected int averageGroundLvl; 
    }
    
	 */
	
	
	
	
    @Shadow (remap = true) protected abstract IBlockState func_175847_a(IBlockState state); //func_175847_a --> getBiomeSpecificBlockState

    //@Shadow (remap = true) protected abstract int func_74889_b(World worldIn, StructureBoundingBox structureBoundingBoxIn); // func_74889_b --> getAverageGroundLevel
    @Shadow  (remap = true) protected abstract int func_74889_b(World worldIn, StructureBoundingBox structureBoundingBoxIn); // func_74889_b --> getAverageGroundLevel


    
}
