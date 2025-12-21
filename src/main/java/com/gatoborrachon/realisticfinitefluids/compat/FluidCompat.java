package com.gatoborrachon.realisticfinitefluids.compat;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.gatoborrachon.realisticfinitefluids.References;
import com.gatoborrachon.realisticfinitefluids.logic.FiniteFluidLogic;
import com.gatoborrachon.realisticfinitefluids.logic.NewFluidType;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.IStateMapper;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.registries.IRegistryDelegate;
import net.minecraftforge.client.model.ModelLoader;

public class FluidCompat {

	public static void loadFiniteFluids(World world) {
	    FluidIndexConfig cfg = FluidIndexConfig.load(world);

	    // A) Cargar fluidos existentes    
	    Iterator<Map.Entry<String, Integer>> it = cfg.storedFluidIndexes.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry<String, Integer> e = it.next();
	        Fluid fluid = FluidRegistry.getFluid(e.getKey());

	        if (fluid == null) {
	            cfg.freeIndexes.add(e.getValue());
	            it.remove(); //ELIMINAMOS DE LOS INDICES ALMACENADOS
	            continue;
	        }
	        
	        if (FiniteFluidLogic.debug) System.out.println("[RFF] LEYENDO Fluido: '" + fluid.getName() + "' con el index: '" + e.getValue() + "'");
	        createFiniteFluid(fluid, e.getValue());
	    }

	    // B) Detectar nuevos fluidos
	    for (Fluid fluid : FluidRegistry.getRegisteredFluids().values()) {
	        if (!cfg.storedFluidIndexes.containsKey(fluid.getName())) {

	            int index = cfg.freeIndexes.isEmpty()
	                    ? cfg.getNextIndex()
	                    : cfg.freeIndexes.remove(0);

	            cfg.storedFluidIndexes.put(fluid.getName(), index);
	            if (FiniteFluidLogic.debug) System.out.println("[RFF] REGISTRANDO Fluido: '"+fluid.getName()+"' con el index: '"+index+"'");
	            //createFiniteFluid(fluid, index);
	        }
	    }

	    cfg.save(world);
	}
	
	
	

	
	
	
	
	
	
	
	
	
	
	public static void createFiniteFluid(Fluid fluid, int indexFromConfig) {
		if (fluid.getName().equals("water")) {
		    addFiniteFluidType(fluid.getName(), Blocks.FLOWING_WATER, Blocks.WATER, -1, indexFromConfig);
		    return;
		}
		
		if (fluid.getName().equals("lava")) {
		    addFiniteFluidType(fluid.getName(), Blocks.FLOWING_LAVA, Blocks.LAVA, -1, indexFromConfig);
		    return;
		}
	

	    // ------------------------------
	    // 1. Obtener el block original
	    // ------------------------------
	    Block fluidBlock = fluid.getBlock();

	    // ------------------------------
	    // 6. Registrar en Liquids y RENDER_ENTRIES
	    // ------------------------------
	    //int gravity = (fluid.getDensity() < 0) ? 1 : -1;
	    int gravity = (fluid.isGaseous()) ? 1 : -1;
	    addFiniteFluidType(fluid.getName(), fluidBlock, fluidBlock, gravity, indexFromConfig);
        //System.out.println("[RFF] FINAL BLOCK: "+fluid.getBlock());
	    //System.out.println("[RFF] Registrado fluido finito para: " + fluid.getName());
	}
	
	
	
	
	
	
	
	
	public static void addFiniteFluidType(String name, Block flowing, Block still, int gravity, int currentIndex) {
		FiniteFluidLogic.liquids.put(
								 currentIndex, 
								 
				new NewFluidType(name,
								 flowing,
								 still,
								 gravity, true)
				);

		////int currentIndex = FiniteFluidLogic.liquids.size() - 1;
		FiniteFluidLogic.fluidIndexMap.put(name, currentIndex);
		FiniteFluidLogic.blockToFluidIndex.put(flowing, currentIndex);
		FiniteFluidLogic.blockToFluidIndex.put(still, currentIndex);
	}
	

	
}
