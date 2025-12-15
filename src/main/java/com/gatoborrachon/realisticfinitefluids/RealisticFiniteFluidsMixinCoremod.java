package com.gatoborrachon.realisticfinitefluids;


import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import zone.rong.mixinbooter.IEarlyMixinLoader;

import java.util.*;

//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;

@IFMLLoadingPlugin.Name("RealisticFiniteFluidsCore")
@IFMLLoadingPlugin.MCVersion("1.12.2")
@IFMLLoadingPlugin.SortingIndex(Integer.MIN_VALUE)
public class RealisticFiniteFluidsMixinCoremod implements IFMLLoadingPlugin, IEarlyMixinLoader {

    //private static final Logger LOGGER = LogManager.getLogger("RealisticFiniteFluids");

    
    @Override
    public List<String> getMixinConfigs() {
        List<String> configs = new ArrayList<>();

        configs.add("mixins.realisticfinitefluids.early.json");
    	configs.add("mixins.realisticfinitefluids.early.basefluidsmixins.json");
    	
    	//configs.add("mixins.realisticfinitefluids.early.registrydebug.json");

        return configs;
    }
    
    

    @Override
    public boolean shouldMixinConfigQueue(String mixinConfig) {
        return true;
    }

    @Override public String[] getASMTransformerClass() {
        return new String[] { };
    }
    
    @Override public String getModContainerClass() { return null; }
    @Override public String getSetupClass() { return null; }
    @Override public String getAccessTransformerClass() { return null; }

    @Override
    public void injectData(Map<String, Object> data) {
    	
    }
}