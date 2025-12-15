package com.gatoborrachon.realisticfinitefluids.mixin.earlymixins;

import java.util.function.Function;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import com.gatoborrachon.realisticfinitefluids.render.BakedModelFiniteFluidClassic;

import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelFluid;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.fluids.Fluid;

@Mixin(ModelFluid.class)
public abstract class MixinModelFluid  {
	
	@Shadow
	@Final
    private Fluid fluid;

	
    @Overwrite(remap = false)
    public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter)
    {
		TextureAtlasSprite spriteFlowCompat = bakedTextureGetter.apply(fluid.getFlowing()); //atlas.getAtlasSprite(fluid.getFlowing().toString());
		TextureAtlasSprite spriteStillCompat = bakedTextureGetter.apply(fluid.getStill()); //atlas.getAtlasSprite(fluid.getStill().toString());
		//int index = FiniteFluidLogic.GeneralPurposeLogic.getFluidIndex(this.fluid.getName());
		
        return new BakedModelFiniteFluidClassic(spriteFlowCompat, spriteStillCompat, this.fluid);
    }
    
    
	
}
