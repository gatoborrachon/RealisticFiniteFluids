package com.gatoborrachon.realisticfinitefluids.coremod;

import zone.rong.mixinbooter.ILateMixinLoader;

import java.util.ArrayList;
import java.util.List;

import net.minecraftforge.fml.common.Loader;

public class RealisticFiniteFluidsMixinLoader implements ILateMixinLoader {

    @Override
    public List<String> getMixinConfigs() {
        //return Collections.singletonList("mixins.realisticfinitefluids.late.json");
        List<String> configs = new ArrayList<>();

        configs.add("mixins.realisticfinitefluids.late.json");
        if (Loader.isModLoaded("climaticbiomesjbg")) {
        	configs.add("mixins.realisticfinitefluids.climaticbiomesjbg.json");
        } if (Loader.isModLoaded("ic2")) {
        	configs.add("mixins.realisticfinitefluids.ic2.json");
        } if (Loader.isModLoaded("travelersbackpack")) {
        	configs.add("mixins.realisticfinitefluids.travelersbackpack.json");
        }
        /* if (Loader.isModLoaded("MODID")) {
        	configs.add("mixins.realisticfinitefluids.MODID.json");
        }*/
        return configs;
    }
}