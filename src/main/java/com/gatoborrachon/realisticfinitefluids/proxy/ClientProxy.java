package com.gatoborrachon.realisticfinitefluids.proxy;

import com.gatoborrachon.realisticfinitefluids.References;
import com.gatoborrachon.realisticfinitefluids.events.FluidModelEventHandler;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy  
{
	public void registerItemRenderer(Item item, int meta, String id)
	{
		ModelLoader.setCustomModelResourceLocation(item, meta, new ModelResourceLocation(item.getRegistryName(), id));
	}
	
    @Override
    public void preInit(FMLPreInitializationEvent e) {
        OBJLoader.INSTANCE.addDomain(References.MODID);
        // Asegúrate de que se cargue la clase
        Class<?> dummy = FluidModelEventHandler.class;
        super.preInit(e);
    }
    
    
}