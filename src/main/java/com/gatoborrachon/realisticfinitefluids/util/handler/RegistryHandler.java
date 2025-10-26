package com.gatoborrachon.realisticfinitefluids.util.handler;

import com.gatoborrachon.realisticfinitefluids.RealisticFiniteFluids;
import com.gatoborrachon.realisticfinitefluids.References;
import com.gatoborrachon.realisticfinitefluids.init.ModBlocks;
import com.gatoborrachon.realisticfinitefluids.init.ModItems;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;


//@EventBusSubscriber
@Mod.EventBusSubscriber(modid = References.MODID)
public class RegistryHandler 
{

	@SubscribeEvent
	public static void onItemRegister(RegistryEvent.Register<Item> event)
	{
		event.getRegistry().registerAll(ModItems.ITEMS.toArray(new Item[0]));
	}
	
	@SubscribeEvent
	public static void onBlockRegister(RegistryEvent.Register<Block> event)
	{
		ModBlocks.init();
		event.getRegistry().registerAll(ModBlocks.BLOCKS.toArray(new Block[0]));
	}
	
    /*public static void registerBlocks() {
        for (Block block : ModBlocks.BLOCKS) {
            // Aqu� registras manualmente
            block.setRegistryName(block.getUnlocalizedName().substring(5));
            ForgeRegistries.BLOCKS.register(block);
        }
    }
    
    public static void registerItems() {
        for (Item item : ModItems.ITEMS) {
            item.setRegistryName(item.getUnlocalizedName().substring(5));
            ForgeRegistries.ITEMS.register(item);
        }

        // Registrar �tems de bloques
        for (Block block : ModBlocks.BLOCKS) {
            ItemBlock ib = new ItemBlock(block);
            ib.setRegistryName(block.getRegistryName());
            ForgeRegistries.ITEMS.register(ib);
        }
    }*/
	
	
	@SubscribeEvent
	public static void onModelRegister(ModelRegistryEvent event)
	{
		for (Item item : ModItems.ITEMS)
		{
			RealisticFiniteFluids.proxy.registerItemRenderer(item, 0, "inventory");
		}
		
		for (Block block : ModBlocks.BLOCKS)
		{
			RealisticFiniteFluids.proxy.registerItemRenderer(Item.getItemFromBlock(block), 0, "inventory");
		}
	}
	
	
	public static void initRegistries(FMLInitializationEvent event)
	{
	}
	
	
	
}
