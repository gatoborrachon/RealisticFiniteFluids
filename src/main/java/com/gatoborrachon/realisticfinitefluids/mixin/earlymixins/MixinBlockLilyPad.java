package com.gatoborrachon.realisticfinitefluids.mixin.earlymixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import com.gatoborrachon.realisticfinitefluids.blocks.BlockFiniteFluid;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLilyPad;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(BlockLilyPad.class)
//@Mixin(targets = "net.minecraft.block.BlockLilyPad")
public abstract class MixinBlockLilyPad { //func_176586_d --> canBlockStay
	/**
	 * ESTA FUNCION DEBERIA HACER COMPATIBLE LOS LILIPADS CON MI AGUA, PERO NO SIRVE DEL TODO
	 */
	@Overwrite (remap = false)
	public boolean func_180671_f(World worldIn, BlockPos pos, IBlockState state) { //func_180671_f ..> canBlockStay
	    if (pos.getY() >= 0 && pos.getY() < 256) {
	        IBlockState below = worldIn.getBlockState(pos.down());
	        Block block = below.getBlock();
	        Material material = below.getMaterial();

	        return (block instanceof BlockFiniteFluid && below.getMaterial() == Material.WATER)
	                || material == Material.WATER
	                || material == Material.ICE;
	    } else {
	        return false;
	    }
	}
    
	@Overwrite (remap = false)
	protected boolean func_185514_i(IBlockState state) { //canSustainBush --> func_185514_i
	    Block block = state.getBlock();

	    return (block instanceof BlockFiniteFluid && state.getMaterial() == Material.WATER)
	            || state.getBlock() == Blocks.WATER
	            || state.getMaterial() == Material.ICE;
	}
    

}
