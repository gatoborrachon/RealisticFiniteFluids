package com.gatoborrachon.realisticfinitefluids.init;

import java.util.ArrayList;
import java.util.List;

import com.gatoborrachon.realisticfinitefluids.items.ItemFiniteLavaBucket;
import com.gatoborrachon.realisticfinitefluids.items.ItemFiniteWaterBucket;

import net.minecraft.item.Item;

public class ModItems  {
	public static final List<Item> ITEMS = new ArrayList<Item>();

	public static final Item FINITE_WATER_BUCKET = new ItemFiniteWaterBucket("finite_water_bucket", ModBlocks.FINITE_WATER_FLOWING);
	public static final Item FINITE_LAVA_BUCKET = new ItemFiniteLavaBucket("finite_lava_bucket", ModBlocks.FINITE_LAVA_FLOWING);

}
