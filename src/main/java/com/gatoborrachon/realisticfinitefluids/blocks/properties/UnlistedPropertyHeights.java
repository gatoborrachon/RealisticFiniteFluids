package com.gatoborrachon.realisticfinitefluids.blocks.properties;

import net.minecraftforge.common.property.IUnlistedProperty;

public class UnlistedPropertyHeights implements IUnlistedProperty<Float> {
    private final String name;

    public UnlistedPropertyHeights(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isValid(Float value) {
        return value != null;
    }

    @Override
    public Class<Float> getType() {
        return Float.class;
    }

    @Override
    public String valueToString(Float value) {
        return Float.toString(value);
    }
}
