package com.gatoborrachon.realisticfinitefluids.render;

import com.gatoborrachon.realisticfinitefluids.References;
import com.gatoborrachon.realisticfinitefluids.interfaces.IRealisticFiniteFluid;
import com.gatoborrachon.realisticfinitefluids.logic.FiniteFluidLogic;
import com.gatoborrachon.realisticfinitefluids.logic.RealisticFiniteFluidFunctions;
import com.google.common.collect.ImmutableMap;

import net.minecraft.block.state.IBlockState;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.model.PerspectiveMapWrapper;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fluids.Fluid;

import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;

import org.apache.commons.lang3.tuple.Pair;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class BakedModelFiniteFluidClassic implements IBakedModel {

	private final RenderNewFluidsClassic renderer;
    private final TextureAtlasSprite spriteFlowing;
    private final TextureAtlasSprite spriteStill;
    private final Fluid fluid;
    private boolean isStill;
    protected final ImmutableMap<TransformType, TRSRTransformation> transforms;
    

    public BakedModelFiniteFluidClassic(IModelState state, TextureAtlasSprite spriteFlowing, TextureAtlasSprite spriteStill, Fluid fluid) {
        this.renderer = new RenderNewFluidsClassic();
		this.spriteFlowing = spriteFlowing;
        this.spriteStill = spriteStill;
        this.fluid = fluid;
        this.transforms = PerspectiveMapWrapper.getTransforms(state);
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
        if (state == null || side == null) return Collections.emptyList();
    	//if (state == null) return Collections.emptyList();
        
        if (!(state instanceof IExtendedBlockState) && !(RealisticFiniteFluidFunctions.getBlock(null, null, state) instanceof IRealisticFiniteFluid)) return Collections.emptyList();

        IExtendedBlockState ext = (IExtendedBlockState) state;
        IRealisticFiniteFluid block = ((IRealisticFiniteFluid)RealisticFiniteFluidFunctions.getBlock(null, null, state));
        int fluidIndex = FiniteFluidLogic.GeneralPurposeLogic.getFluidIndex(fluid.getName());

        Float h00 = ext.getValue(block.getCornerLevel(0));
        Float h10 = ext.getValue(block.getCornerLevel(1));
        Float h01 = ext.getValue(block.getCornerLevel(2));
        Float h11 = ext.getValue(block.getCornerLevel(3));
        
        Map<EnumFacing, IBlockState> neighborStates = ext.getValue(block.getNeighborStates());
        
        int color = 0xFFFFFFFF; // blanco por defecto
        Integer colorFluid = fluid.getColor();
        if (colorFluid != null) {
            color = colorFluid;
        }
        
        
        Vec3d flow = ext.getValue(block.getFlowDirectionProperty());
        

        if (h00 == null || h10 == null || h01 == null || h11 == null || neighborStates == null) {
            return Collections.emptyList();
        }
        
        isStill = ext.getValue(block.getIsStill());
        //System.out.println(isStill);
        
        boolean isGaseous = ext.getValue(block.getIsGaseous());
        float LEVEL = ((float)state.getValue(References.LEVEL))/10.0f; //ext.getValue(References.LEVEL);
        //System.out.println(LEVEL);

        return isGaseous 
        ? renderer.renderBlockNewGasClassic(state, 1f, 1f, 1f, 1f, neighborStates, color, fluidIndex, spriteFlowing, spriteStill, side, flow, isStill, LEVEL)
        : renderer.renderBlockNewFluidClassic(state, h00, h10, h01, h11, neighborStates, color, fluidIndex, spriteFlowing, spriteStill, side, flow, isStill);

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
        return isStill ? spriteStill : spriteFlowing;
    }

    /*@Override
    public ItemCameraTransforms getItemCameraTransforms() {
        return ItemCameraTransforms.DEFAULT;
    }*/

    @Override
    public ItemOverrideList getOverrides() {
        return ItemOverrideList.NONE;
    }
    
    @Override
    public Pair<? extends IBakedModel, Matrix4f> handlePerspective(TransformType type) {
        return PerspectiveMapWrapper.handlePerspective(this, transforms, type);
    }
    
}
