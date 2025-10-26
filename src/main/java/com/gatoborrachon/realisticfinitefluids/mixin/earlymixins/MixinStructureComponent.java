package com.gatoborrachon.realisticfinitefluids.mixin.earlymixins;

import java.util.Random;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.block.state.IBlockState;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureComponent;

//@Mixin(targets = "net.minecraft.world.gen.structure.StructureComponent")
@Mixin(StructureComponent.class)
public abstract class MixinStructureComponent {

    @Shadow (remap = true)
    protected StructureBoundingBox field_74887_e; //field_74887_e --> boundingBox

    /*@Shadow
    @Nullable
    private EnumFacing field_74885_f; //field_74885_f --> coordBaseMode*/
    
    @Shadow (remap = true) protected abstract void func_175804_a(World worldIn, StructureBoundingBox box, int x1, int y1, int z1, int x2, int y2, int z2, IBlockState boundaryBlock, IBlockState insideBlock, boolean existingOnly); // func_175804_a --> fillWithBlocks

    @Shadow (remap = true) protected abstract void func_175811_a(World worldIn, IBlockState blockstate, int x, int y, int z, StructureBoundingBox boundingBox); //func_175811_a --> setBlockState

    @Shadow (remap = true) protected abstract void func_74871_b(World worldIn, int x, int y, int z, StructureBoundingBox structureBoundingBoxIn); //func_74871_b --> clearCurrentPositionBlocksUpwards

    @Shadow (remap = true) protected abstract void func_175808_b(World worldIn, IBlockState blockstate, int x, int y, int z, StructureBoundingBox structureBoundingBoxIn); //func_175808_b --> replaceAirAndLiquidDownwards

    
    @Shadow (remap = true) protected abstract boolean func_74875_a(World worldIn, Random randomIn, StructureBoundingBox structureBoundingBoxIn); // func_74875_a --> addComponentParts

    
    // Si necesitas otros campos, también los shadoweas
}
