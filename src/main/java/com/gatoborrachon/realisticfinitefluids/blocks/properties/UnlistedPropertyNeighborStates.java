package com.gatoborrachon.realisticfinitefluids.blocks.properties;

import java.util.Map;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.property.IUnlistedProperty;

public class UnlistedPropertyNeighborStates implements IUnlistedProperty<Map<EnumFacing, IBlockState>> {
    @Override
    public String getName() {
        return "neighbor_states";
    }

    @Override
	public boolean isValid(Map<EnumFacing, IBlockState> value) {
        return value != null;
    }

    /*@Override
    public Class<BlockPos> getType() {
        return BlockPos.class;
    }*/

    @Override
	public String valueToString(Map<EnumFacing, IBlockState> value) {
        return value.toString();
    }

	@Override
	public Class<Map<EnumFacing, IBlockState>> getType() {
	    Class<Map<EnumFacing, IBlockState>> clazz = (Class<Map<EnumFacing, IBlockState>>)(Class<?>) Map.class;
		return clazz;
	}

}
