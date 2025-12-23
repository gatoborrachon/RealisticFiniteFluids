package com.gatoborrachon.realisticfinitefluids.coremod;

import zone.rong.mixinbooter.ILateMixinLoader;

import java.util.ArrayList;
import java.util.List;

import net.minecraftforge.fml.common.Loader;

public class RealisticFiniteFluidsMixinLoader implements ILateMixinLoader {

    @Override
    public List<String> getMixinConfigs() {
        List<String> configs = new ArrayList<>();

        if (Loader.isModLoaded("ic2")) {
        	configs.add("mixins.realisticfinitefluids.ic2.json");
        } if (Loader.isModLoaded("travelersbackpack")) {
        	configs.add("mixins.realisticfinitefluids.travelersbackpack.json");
        } if (Loader.isModLoaded("fluidlogged_api")) {
        	configs.add("mixins.realisticfinitefluids.fluidloggedapi.json");
        }
        
        //configs.add("mixins.realisticfinitefluids.early.registrydebug.json");
        
        
        /* if (Loader.isModLoaded("MODID")) {
        	configs.add("mixins.realisticfinitefluids.MODID.json");
        }*/
        return configs;
    }
}