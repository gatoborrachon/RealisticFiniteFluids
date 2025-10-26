package com.gatoborrachon.realisticfinitefluids.mixin.earlymixins;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.block.state.IBlockState;
import net.minecraft.world.gen.structure.StructureOceanMonumentPieces;


//@Mixin(targets = "net.minecraft.world.gen.structure.StructureOceanMonumentPieces$Piece")
@Mixin(StructureOceanMonumentPieces.Piece.class)
public class MixinOceanMonumentPiece {
    
    @Shadow (remap = true) @Final @Mutable 
    private static IBlockState field_175822_f; //field_175822_f --> WATER

    static {
        // Reemplaza el bloque agua por tu bloque custom al cargar la clase
    	field_175822_f = com.gatoborrachon.realisticfinitefluids.init.ModBlocks.INFINITE_WATER_SOURCE.getDefaultState();
    }
}
