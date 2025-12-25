package com.gatoborrachon.realisticfinitefluids.render;

import java.util.Collection;
import java.util.function.Function;

import javax.annotation.Nullable;

import com.gatoborrachon.realisticfinitefluids.logic.FiniteFluidLogic;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelFluid;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.FMLLog;

@SuppressWarnings("unused")
public class IModelFiniteFluidClassic implements IModel {
    TextureMap atlas = Minecraft.getMinecraft().getTextureMapBlocks();
    private Fluid fluid;

    public IModelFiniteFluidClassic(Fluid fluid) {
        this.fluid = fluid;
    }

    @Override
    public Collection<ResourceLocation> getTextures() {
        return fluid.getOverlay() != null
                ? ImmutableSet.of(fluid.getStill(), fluid.getFlowing(), fluid.getOverlay())
                : ImmutableSet.of(fluid.getStill(), fluid.getFlowing());
    }

    @Override
    public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
    		TextureAtlasSprite spriteFlowCompat = bakedTextureGetter.apply(fluid.getFlowing()); //atlas.getAtlasSprite(fluid.getFlowing().toString());
    		TextureAtlasSprite spriteStillCompat = bakedTextureGetter.apply(fluid.getStill()); //atlas.getAtlasSprite(fluid.getStill().toString());
    		//int index = FiniteFluidLogic.GeneralPurposeLogic.getFluidIndex(this.fluid.getName());
    		
    		return new BakedModelFiniteFluidClassic(state, spriteFlowCompat, spriteStillCompat, this.fluid);
    }

    @Override
    public IModel process(ImmutableMap<String, String> customData) {
        if(!customData.containsKey("fluid")) return this;

        String fluidStr = customData.get("fluid");
        JsonElement e = new JsonParser().parse(fluidStr);
        String fluidName = e.getAsString();
        if(!FluidRegistry.isFluidRegistered(fluidName))
        {
            FMLLog.log.fatal("fluid '{}' not found", fluidName);
            return this;
        }
        
        return new IModelFiniteFluidClassic(FluidRegistry.getFluid(fluidName));
    	
    }
}
