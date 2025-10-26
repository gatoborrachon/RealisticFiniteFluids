package com.gatoborrachon.realisticfinitefluids.blocks.properties;

import net.minecraftforge.common.property.IUnlistedProperty;

public class UnlistedPropertyColor implements IUnlistedProperty<Integer> {

    private final String name;

    public UnlistedPropertyColor(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isValid(Integer value) {
        return value != null;
    }

    @Override
    public Class<Integer> getType() {
        return Integer.class;
    }

    @Override
    public String valueToString(Integer value) {
        return String.format("#%08X", value);
    }
}