package com.gatoborrachon.realisticfinitefluids.proxy;

import net.minecraft.item.Item;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class CommonProxy 
{
	public void registerItemRenderer(Item item, int meta, String id) {}
	
	public void registerModel(Item item, int metadata) {}

	public void preInit(FMLPreInitializationEvent e) {
	}

}
