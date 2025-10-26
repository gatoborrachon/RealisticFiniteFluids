package com.gatoborrachon.realisticfinitefluids.blocks.properties;

import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.property.IUnlistedProperty;

public class UnlistedPropertyFlowDirection implements IUnlistedProperty<Vec3d> {

    private final String name;

    public UnlistedPropertyFlowDirection(String name) {
        this.name = name;
    }

	@Override
	public String getName() {
        return name;
	}
    
    @Override
    public boolean isValid(Vec3d value) {
        return value != null;
    }

    @Override
    public String valueToString(Vec3d value) {
        return value.toString();
    }

	@Override
	public Class<Vec3d> getType() {
		return Vec3d.class;
	}


}
