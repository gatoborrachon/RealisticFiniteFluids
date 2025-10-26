package com.gatoborrachon.realisticfinitefluids.logic;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;

public class NewFluidType {
    public final Block flowingBlock;
    public final Block stillBlock;
    public final Block oceanBlock;
    public final int gravity; // 1 o -1
    public final boolean flowsOverHalfBlocks;
    public final String name;
    //public final Item bucketItem;

    public NewFluidType(String name, Block flowing, Block still, Block ocean, int gravity, /*Item bucket,*/ boolean flowsOverHalfBlocks) {
		this.name = name;
        this.flowingBlock = flowing;
        this.stillBlock = still;
        this.oceanBlock = ocean;
        this.gravity = gravity;
        //this.bucketItem = bucket;
        this.flowsOverHalfBlocks = flowsOverHalfBlocks;
    }

    public boolean isFluid(Block block) {
        return block == flowingBlock || block == stillBlock || block == oceanBlock;
    }

    public boolean isFluid(IBlockState state) {
        return isFluid(state.getBlock());
    }
    
    /*public Block getFluid(Block block){
    	return block == this.flowingBlock ? true : block == this.;
    }*/
    
    
}
