package com.gatoborrachon.realisticfinitefluids;


import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import zone.rong.mixinbooter.IEarlyMixinLoader;

import java.util.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.gatoborrachon.realisticfinitefluids.init.EarlyConfig;

@IFMLLoadingPlugin.Name("RealisticFiniteFluidsCore")
@IFMLLoadingPlugin.MCVersion("1.12.2")
@IFMLLoadingPlugin.SortingIndex(Integer.MIN_VALUE)
public class RealisticFiniteFluidsMixinCoremod implements IFMLLoadingPlugin, IEarlyMixinLoader {

    private static final Logger LOGGER = LogManager.getLogger("RealisticFiniteFluids");

    
    @Override
    public List<String> getMixinConfigs() {
        String sealMode = EarlyConfig.readBarrierMode();
        String replaceMode = EarlyConfig.readReplacerMode();
        List<String> configs = new ArrayList<>();

        configs.add("mixins.realisticfinitefluids.early.json");
        //LOGGER.info("MODO SELECCIONADO AL FINAL: {}", mode);
        if ("optimized".equalsIgnoreCase(sealMode)) {
        	configs.add("mixins.realisticfinitefluids.early.optimized.json");
        } else if ("unoptimized".equalsIgnoreCase(sealMode)) {
        	configs.add("mixins.realisticfinitefluids.early.unoptimized.json");
        }
        //LOGGER.info("MODO SELECCIONADO AL FINAL: {}", replaceMode);
        if ("true".equalsIgnoreCase(replaceMode)) {
        	configs.add("mixins.realisticfinitefluids.early.fluidreplacer.json");
        }
        return configs;
    }
    
    

    @Override
    public boolean shouldMixinConfigQueue(String mixinConfig) {
        return true;
    }

    @Override public String[] getASMTransformerClass() {
        String universalCompatEnabled = EarlyConfig.readUniversalCompat();

        //LOGGER.info("MODO SELECCIONADO AL FINAL: {}", universalCompatEnabled);
        if ("true".equalsIgnoreCase(universalCompatEnabled)) {
            return new String[] { "com.gatoborrachon.realisticfinitefluids.coremod.ChunkHellTransformer",
					  			  "com.gatoborrachon.realisticfinitefluids.coremod.BlocksGlobalTransformer",
					  			  "com.gatoborrachon.realisticfinitefluids.coremod.BiomeTransformer" };
        };
        
        return new String[] { "com.gatoborrachon.realisticfinitefluids.coremod.ChunkHellTransformer",
        					  "com.gatoborrachon.realisticfinitefluids.coremod.BiomeTransformer" };
        
    }
    
    @Override public String getModContainerClass() { return null; }
    @Override public String getSetupClass() { return null; }
    @Override public String getAccessTransformerClass() { return null; }

    @Override
    public void injectData(Map<String, Object> data) {
        // Aquí puedes modificar cosas del classloader si lo necesitas
    }
}