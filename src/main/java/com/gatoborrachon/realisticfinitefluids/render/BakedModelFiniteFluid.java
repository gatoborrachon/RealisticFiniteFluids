package com.gatoborrachon.realisticfinitefluids.render;

import com.gatoborrachon.realisticfinitefluids.blocks.BlockFiniteFluid;
import com.gatoborrachon.realisticfinitefluids.blocks.BlockNewWater;
import com.gatoborrachon.realisticfinitefluids.render.RenderNewFluids;
import net.minecraft.block.state.IBlockState;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.property.IExtendedBlockState;

import javax.annotation.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class BakedModelFiniteFluid implements IBakedModel {

    private final RenderNewFluids renderer;
    private final TextureAtlasSprite sprite;
    private final int fluidIndex;

    public BakedModelFiniteFluid(RenderNewFluids renderer, TextureAtlasSprite sprite, int fluidIndex) {
        this.renderer = renderer;
        this.sprite = sprite;
        this.fluidIndex = fluidIndex;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
        if (state == null || side == null) return Collections.emptyList();
    	//if (state == null) return Collections.emptyList();

    	
        if (!(state instanceof IExtendedBlockState)) return Collections.emptyList();

        IExtendedBlockState ext = (IExtendedBlockState) state;

        Float h00 = ext.getValue(BlockFiniteFluid.HEIGHT_NW);
        Float h10 = ext.getValue(BlockFiniteFluid.HEIGHT_NE);
        Float h01 = ext.getValue(BlockFiniteFluid.HEIGHT_SW);
        Float h11 = ext.getValue(BlockFiniteFluid.HEIGHT_SE);
        
        Map<EnumFacing, IBlockState> neighborStates = ext.getValue(BlockFiniteFluid.NEIGHBOR_STATES);
        
        int color = 0xFFFFFFFF; // blanco por defecto
        Integer colorProp = ext.getValue(BlockFiniteFluid.FLUID_COLOR);
        if (colorProp != null) {
            color = colorProp;
        }
        
        
        Vec3d flow = new Vec3d(0,0,0);
        if (state.getBlock() instanceof BlockNewWater) {
        	flow = ext.getValue(BlockNewWater.FLOW_DIRECTION);
        }
        

        if (h00 == null || h10 == null || h01 == null || h11 == null || neighborStates == null) {
            return Collections.emptyList();
        }


        return renderer.renderBlockNewFluid(state, h00, h10, h01, h11, neighborStates, color, fluidIndex, sprite, side, flow);

    }

    @Override
    public boolean isAmbientOcclusion() {
        return true;
    }

    @Override
    public boolean isGui3d() {
        return false;
    }

    @Override
    public boolean isBuiltInRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return sprite;
    }

    @Override
    public ItemCameraTransforms getItemCameraTransforms() {
        return ItemCameraTransforms.DEFAULT;
    }

    @Override
    public net.minecraft.client.renderer.block.model.ItemOverrideList getOverrides() {
        return net.minecraft.client.renderer.block.model.ItemOverrideList.NONE;
    }
}
