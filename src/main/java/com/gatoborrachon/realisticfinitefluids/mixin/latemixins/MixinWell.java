package com.gatoborrachon.realisticfinitefluids.mixin.latemixins;

import com.gatoborrachon.realisticfinitefluids.References;
import com.gatoborrachon.realisticfinitefluids.init.ModBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureVillagePieces;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.Random;

@Mixin(StructureVillagePieces.Well.class)
public abstract class MixinWell extends MixinVillage {

	
	//No recuerdo porque tenia que tener remap=false xdd
	//DEV --> TRUE + Nombre ofuscado
	//OUTSIDE DEV --> FALSE + Nombre ofuscado
	
	//Tal vez, porque alguna mmda pasa que si es true, se remapea en el juego
	//fuera del dev, y pues crashea al estar en una aldea xd
    @Overwrite (remap = References.onDev) // func_74875_a --> addComponentParts
    public boolean func_74875_a(World worldIn, Random randomIn, StructureBoundingBox structureBoundingBoxIn) {
        if (this.field_143015_k < 0) {
            this.field_143015_k = this.func_74889_b(worldIn, structureBoundingBoxIn);
            if (this.field_143015_k < 0) {
                return true;
            }
            this.field_74887_e.offset(0, this.field_143015_k - this.field_74887_e.maxY + 3, 0);
        }

        IBlockState cobble = this.func_175847_a(Blocks.COBBLESTONE.getDefaultState());
        IBlockState fence = this.func_175847_a(Blocks.OAK_FENCE.getDefaultState());
        IBlockState customWater = ModBlocks.INFINITE_WATER_SOURCE.getDefaultState(); 

        this.func_175804_a(worldIn, structureBoundingBoxIn, 1, 0, 1, 4, 12, 4, cobble, customWater, false);

        this.func_175811_a(worldIn, Blocks.AIR.getDefaultState(), 2, 12, 2, structureBoundingBoxIn);
        this.func_175811_a(worldIn, Blocks.AIR.getDefaultState(), 3, 12, 2, structureBoundingBoxIn);
        this.func_175811_a(worldIn, Blocks.AIR.getDefaultState(), 2, 12, 3, structureBoundingBoxIn);
        this.func_175811_a(worldIn, Blocks.AIR.getDefaultState(), 3, 12, 3, structureBoundingBoxIn);

        this.func_175811_a(worldIn, fence, 1, 13, 1, structureBoundingBoxIn);
        this.func_175811_a(worldIn, fence, 1, 14, 1, structureBoundingBoxIn);
        this.func_175811_a(worldIn, fence, 4, 13, 1, structureBoundingBoxIn);
        this.func_175811_a(worldIn, fence, 4, 14, 1, structureBoundingBoxIn);
        this.func_175811_a(worldIn, fence, 1, 13, 4, structureBoundingBoxIn);
        this.func_175811_a(worldIn, fence, 1, 14, 4, structureBoundingBoxIn);
        this.func_175811_a(worldIn, fence, 4, 13, 4, structureBoundingBoxIn);
        this.func_175811_a(worldIn, fence, 4, 14, 4, structureBoundingBoxIn);

        this.func_175804_a(worldIn, structureBoundingBoxIn, 1, 15, 1, 4, 15, 4, cobble, cobble, false);

        for (int i = 0; i <= 5; ++i) {
            for (int j = 0; j <= 5; ++j) {
                if (j == 0 || j == 5 || i == 0 || i == 5) {
                    this.func_175811_a(worldIn, cobble, j, 11, i, structureBoundingBoxIn);
                    this.func_74871_b(worldIn, j, 12, i, structureBoundingBoxIn);
                }
            }
        }
        return true;
    }


}