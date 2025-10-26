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

@Mixin(StructureVillagePieces.Field2.class)
public abstract class MixinField2 extends MixinVillage {
    
    @Shadow (remap = true) Block field_82675_b; // field_82675_b --> cropTypeA
    @Shadow (remap = true) Block field_82676_c; // field_82676_c --> cropTypeB
    
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

        this.func_175804_a(worldIn, structureBoundingBoxIn, 0, 1, 0, 6, 4, 8, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
        this.func_175804_a(worldIn, structureBoundingBoxIn, 1, 0, 1, 2, 0, 7, Blocks.FARMLAND.getDefaultState(), Blocks.FARMLAND.getDefaultState(), false);
        this.func_175804_a(worldIn, structureBoundingBoxIn, 4, 0, 1, 5, 0, 7, Blocks.FARMLAND.getDefaultState(), Blocks.FARMLAND.getDefaultState(), false);
        this.func_175804_a(worldIn, structureBoundingBoxIn, 0, 0, 0, 0, 0, 8, log, log, false);
        this.func_175804_a(worldIn, structureBoundingBoxIn, 6, 0, 0, 6, 0, 8, log, log, false);
        this.func_175804_a(worldIn, structureBoundingBoxIn, 1, 0, 0, 5, 0, 0, log, log, false);
        this.func_175804_a(worldIn, structureBoundingBoxIn, 1, 0, 8, 5, 0, 8, log, log, false);

        // Cambiamos el agua por el bloque custom
        this.func_175804_a(worldIn, structureBoundingBoxIn, 3, 0, 1, 3, 0, 7, customWater, customWater, false);

        for (int i = 1; i <= 7; ++i) {
            int j = ((BlockCrops) this.field_82675_b).getMaxAge();
            int k = j / 3;
            this.func_175811_a(worldIn, this.field_82675_b.getStateFromMeta(MathHelper.getInt(randomIn, k, j)), 1, 1, i, structureBoundingBoxIn);
            this.func_175811_a(worldIn, this.field_82675_b.getStateFromMeta(MathHelper.getInt(randomIn, k, j)), 2, 1, i, structureBoundingBoxIn);
            int l = ((BlockCrops) this.field_82676_c).getMaxAge();
            int i1 = l / 3;
            this.func_175811_a(worldIn, this.field_82676_c.getStateFromMeta(MathHelper.getInt(randomIn, i1, l)), 4, 1, i, structureBoundingBoxIn);
            this.func_175811_a(worldIn, this.field_82676_c.getStateFromMeta(MathHelper.getInt(randomIn, i1, l)), 5, 1, i, structureBoundingBoxIn);
        }

        for (int j1 = 0; j1 < 9; ++j1) {
            for (int k1 = 0; k1 < 7; ++k1) {
                this.func_74871_b(worldIn, k1, 4, j1, structureBoundingBoxIn);
                this.func_175808_b(worldIn, Blocks.DIRT.getDefaultState(), k1, -1, j1, structureBoundingBoxIn);
            }
        }

        return true;
    }


}
