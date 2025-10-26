package com.gatoborrachon.realisticfinitefluids.mixin.latemixins;

import com.gatoborrachon.realisticfinitefluids.References;
import com.gatoborrachon.realisticfinitefluids.init.ModBlocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureVillagePieces;

import java.util.Random;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;


//NOTA --> AHORA NO FUNCIONA LA IC2 FLUID CELL POR ALGUN MOTIVO :'V

	
@Mixin(StructureVillagePieces.Field1.class)
public abstract class MixinField1 extends MixinVillage {

    @Shadow (remap = true) Block field_82679_b; // field_82679_b --> cropTypeA
    @Shadow (remap = true) Block field_82680_c; // field_82680_c --> cropTypeB
    @Shadow (remap = true) Block field_82678_d; // field_82678_d --> cropTypeC
    @Shadow (remap = true) Block field_82681_h; // field_82681_h --> cropTypeD
	
    @Overwrite (remap = References.onDev) //func_74875_a --> addComponentParts
    public boolean func_74875_a(World worldIn, Random randomIn, StructureBoundingBox structureBoundingBoxIn) {
        if (this.field_143015_k < 0) {
            this.field_143015_k = this.func_74889_b(worldIn, structureBoundingBoxIn);
            if (this.field_143015_k < 0) {
                return true;
            }
            this.field_74887_e.offset(0, this.field_143015_k - this.field_74887_e.maxY + 4 - 1, 0);
        }
        


	
        

        IBlockState log = this.func_175847_a(Blocks.LOG.getDefaultState());
        IBlockState customWater = ModBlocks.INFINITE_WATER_SOURCE.getDefaultState();

        this.func_175804_a(worldIn, structureBoundingBoxIn, 0, 1, 0, 12, 4, 8, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
        this.func_175804_a(worldIn, structureBoundingBoxIn, 1, 0, 1, 2, 0, 7, Blocks.FARMLAND.getDefaultState(), Blocks.FARMLAND.getDefaultState(), false);
        this.func_175804_a(worldIn, structureBoundingBoxIn, 4, 0, 1, 5, 0, 7, Blocks.FARMLAND.getDefaultState(), Blocks.FARMLAND.getDefaultState(), false);
        this.func_175804_a(worldIn, structureBoundingBoxIn, 7, 0, 1, 8, 0, 7, Blocks.FARMLAND.getDefaultState(), Blocks.FARMLAND.getDefaultState(), false);
        this.func_175804_a(worldIn, structureBoundingBoxIn, 10, 0, 1, 11, 0, 7, Blocks.FARMLAND.getDefaultState(), Blocks.FARMLAND.getDefaultState(), false);
        this.func_175804_a(worldIn, structureBoundingBoxIn, 0, 0, 0, 0, 0, 8, log, log, false);
        this.func_175804_a(worldIn, structureBoundingBoxIn, 6, 0, 0, 6, 0, 8, log, log, false);
        this.func_175804_a(worldIn, structureBoundingBoxIn, 12, 0, 0, 12, 0, 8, log, log, false);
        this.func_175804_a(worldIn, structureBoundingBoxIn, 1, 0, 0, 11, 0, 0, log, log, false);
        this.func_175804_a(worldIn, structureBoundingBoxIn, 1, 0, 8, 11, 0, 8, log, log, false);

        // Aquí cambiamos el agua vanilla por tu bloque personalizado
        this.func_175804_a(worldIn, structureBoundingBoxIn, 3, 0, 1, 3, 0, 7, customWater, customWater, false);
        this.func_175804_a(worldIn, structureBoundingBoxIn, 9, 0, 1, 9, 0, 7, customWater, customWater, false);

        for (int i = 1; i <= 7; ++i) {
            int j = ((BlockCrops) this.field_82679_b).getMaxAge();
            int k = j / 3;
            this.func_175811_a(worldIn, this.field_82679_b.getStateFromMeta(MathHelper.getInt(randomIn, k, j)), 1, 1, i, structureBoundingBoxIn);
            this.func_175811_a(worldIn, this.field_82679_b.getStateFromMeta(MathHelper.getInt(randomIn, k, j)), 2, 1, i, structureBoundingBoxIn);
            int l = ((BlockCrops) this.field_82680_c).getMaxAge();
            int i1 = l / 3;
            this.func_175811_a(worldIn, this.field_82680_c.getStateFromMeta(MathHelper.getInt(randomIn, i1, l)), 4, 1, i, structureBoundingBoxIn);
            this.func_175811_a(worldIn, this.field_82680_c.getStateFromMeta(MathHelper.getInt(randomIn, i1, l)), 5, 1, i, structureBoundingBoxIn);
            int j1 = ((BlockCrops) this.field_82678_d).getMaxAge();
            int k1 = j1 / 3;
            this.func_175811_a(worldIn, this.field_82678_d.getStateFromMeta(MathHelper.getInt(randomIn, k1, j1)), 7, 1, i, structureBoundingBoxIn);
            this.func_175811_a(worldIn, this.field_82678_d.getStateFromMeta(MathHelper.getInt(randomIn, k1, j1)), 8, 1, i, structureBoundingBoxIn);
            int l1 = ((BlockCrops) this.field_82681_h).getMaxAge();
            int i2 = l1 / 3;
            this.func_175811_a(worldIn, this.field_82681_h.getStateFromMeta(MathHelper.getInt(randomIn, i2, l1)), 10, 1, i, structureBoundingBoxIn);
            this.func_175811_a(worldIn, this.field_82681_h.getStateFromMeta(MathHelper.getInt(randomIn, i2, l1)), 11, 1, i, structureBoundingBoxIn);
        }

        for (int j2 = 0; j2 < 9; ++j2) {
            for (int k2 = 0; k2 < 13; ++k2) {
                this.func_74871_b(worldIn, k2, 4, j2, structureBoundingBoxIn);
                this.func_175808_b(worldIn, Blocks.DIRT.getDefaultState(), k2, -1, j2, structureBoundingBoxIn);
            }
        }

        return true;
    }

}
