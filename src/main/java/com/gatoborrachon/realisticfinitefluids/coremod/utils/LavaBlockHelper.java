package com.gatoborrachon.realisticfinitefluids.coremod.utils;

import com.gatoborrachon.realisticfinitefluids.init.ModBlocks;
import net.minecraft.block.state.IBlockState;

public class LavaBlockHelper {
    public static IBlockState getCustomLava() {
        return ModBlocks.INFINITE_LAVA_SOURCE.getDefaultState();
    }
    
    //NOTA --> SI USAS BLOCKS, EL JUEGO ACTIVA TICKS Y CRASHEA, SI USAS IBLOCKSTATE, TODO CHIDO (EN MAYOR PARTE, A VECES NO, A VECES DEBES USAR CHUNK.SETBLOCKSTATE)
    public static net.minecraft.block.Block getCustomLavaBlock() {
        return ModBlocks.INFINITE_LAVA_SOURCE; 
    }
}
