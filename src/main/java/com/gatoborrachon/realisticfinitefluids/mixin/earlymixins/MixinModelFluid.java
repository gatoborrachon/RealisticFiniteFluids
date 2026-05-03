package com.gatoborrachon.realisticfinitefluids.mixin.earlymixins;

import java.util.Optional;
import java.util.function.Function;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.At;

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

	@Inject(
			method = "bake",
			at = @At("HEAD"),
			cancellable = true,
			remap = false
			)
	private void rff$bake(
			IModelState state,
			VertexFormat format,
			Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter,
			CallbackInfoReturnable<IBakedModel> cir
			) {
		TextureAtlasSprite spriteFlow = bakedTextureGetter.apply(fluid.getFlowing());
		TextureAtlasSprite spriteStill = bakedTextureGetter.apply(fluid.getStill());

		cir.setReturnValue(
				new BakedModelFiniteFluidClassic(
						//state.apply(Optional.empty()),
						format, 
						state,
						spriteFlow,
						spriteStill,
						this.fluid
						)
				);
	}
	
	/*
	@Overwrite(remap = false)
    public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter)
    {
		TextureAtlasSprite spriteFlowCompat = bakedTextureGetter.apply(fluid.getFlowing()); //atlas.getAtlasSprite(fluid.getFlowing().toString());
		TextureAtlasSprite spriteStillCompat = bakedTextureGetter.apply(fluid.getStill()); //atlas.getAtlasSprite(fluid.getStill().toString());
		//int index = FiniteFluidLogic.GeneralPurposeLogic.getFluidIndex(this.fluid.getName());
		
        return new BakedModelFiniteFluidClassic(spriteFlowCompat, spriteStillCompat, this.fluid);
    }
	*/
	
}
