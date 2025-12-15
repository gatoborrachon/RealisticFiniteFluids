package com.gatoborrachon.realisticfinitefluids.mixin.earlymixins;

import java.util.Map;
import java.util.Map.Entry;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.block.statemap.IStateMapper;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.registries.IRegistryDelegate;

@Mixin(ModelLoader.class)
public class MixinModelLoader {

	@Shadow
	@Final
    private static Map<IRegistryDelegate<Block>, IStateMapper> customStateMappers;
	
	@Overwrite
	public static void onRegisterAllBlocks(BlockModelShapes shapes)
	{
		for (Entry<IRegistryDelegate<Block>, IStateMapper> e : customStateMappers.entrySet())
		{
			System.out.println("[RFF - ModelLoader] Block: "+e.getKey().get().toString());
			System.out.println("[RFF - ModelLoader] IStateMapper: "+e.getValue());
			//System.out.println("[RFF - ModelLoader] e.getValue().toString(): "+e.getValue().toString());
			System.out.println(" ");

			shapes.registerBlockWithStateMapper(e.getKey().get(), e.getValue());
		}
	}

}
