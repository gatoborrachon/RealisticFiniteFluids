package com.gatoborrachon.realisticfinitefluids.mixin.earlymixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import com.gatoborrachon.realisticfinitefluids.References;
import com.gatoborrachon.realisticfinitefluids.compat.FluidCompat;
import com.gatoborrachon.realisticfinitefluids.render.IModelFiniteFluidClassic;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelFluid;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

@Mixin(ModelFluid.FluidLoader.class)
public abstract class MixinModelFluid$FluidLoader  {

	@Overwrite(remap = false)
	public IModel loadModel(ResourceLocation modelLocation) {		
		Fluid fluid = FluidCompat.ModelResourceLocationToFluidMap.get(modelLocation.toString());
		if (fluid != null)
			return new IModelFiniteFluidClassic(fluid);
		else
			return new IModelFiniteFluidClassic(FluidRegistry.WATER); // ModelFluid.WATER;
	}

	
	@Overwrite(remap = false)
	public boolean accepts(ResourceLocation modelLocation){
		return (
				!modelLocation.getNamespace().equals(ForgeVersion.MOD_ID)
				) 
				&& 
				(
						modelLocation.getPath().equals("fluids") ||
						modelLocation.getPath().startsWith("fluid") ||
						modelLocation.getPath().contains("fluid") ||
						modelLocation.getPath().equals("models/block/fluids") ||
						modelLocation.getPath().equals("models/item/fluids") 
						//|| modelLocation.getPath().startsWith("finite")
				)
				
				
				||


				modelLocation.getNamespace().equals(References.MODID) //realisticfinitefluids

				;
	}

}
