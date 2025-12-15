package com.gatoborrachon.realisticfinitefluids.mixin.earlymixins;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import com.gatoborrachon.realisticfinitefluids.References;
import com.gatoborrachon.realisticfinitefluids.interfaces.IRealisticFiniteFluid;
import com.gatoborrachon.realisticfinitefluids.logic.FiniteFluidLogic;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDynamicLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BlockFluidRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;

@Mixin(BlockFluidRenderer.class)
public class MixinBlockFluidRenderer {
	   
    @Shadow(remap = References.onDev) @Final private BlockColors field_187500_a; //blockColors
    @Shadow(remap = References.onDev) @Final private TextureAtlasSprite[] field_178272_a; //atlasSpritesLava
    @Shadow(remap = References.onDev) @Final private TextureAtlasSprite[] field_178271_b; //atlasSpritesWater
    @Shadow(remap = References.onDev) @Final private TextureAtlasSprite field_187501_d; //atlasSpriteWaterOverlay
	
	@Overwrite(remap = References.onDev) //renderFluid
    public boolean func_178270_a(IBlockAccess world, IBlockState state, BlockPos pos, BufferBuilder bufferBuilderIn)
    {
        // ¿Este fluido es lava?
        boolean isLava = state.getMaterial() == Material.LAVA;

        // Texturas a usar:
        // [0] = still
        // [1] = flowing
        TextureAtlasSprite[] fluidSprites = isLava
                ? this.field_178272_a
                : this.field_178271_b;

        // Color del bloque (bioma, tinte, etc.)
        int blockColor = this.field_187500_a.colorMultiplier(
        		state, world, pos, 0
        );

        // Componentes RGB normalizados
        float colorR = (float)(blockColor >> 16 & 255) / 255.0F;
        float colorG = (float)(blockColor >> 8  & 255) / 255.0F;
        float colorB = (float)(blockColor       & 255) / 255.0F;

        Vec3d flow = ((IRealisticFiniteFluid)state.getBlock()).calculateFlowVector(world, pos);       
        
        int fluidIndex = FiniteFluidLogic.GeneralPurposeLogic.getFluidIndex(state.getBlock());
        
        boolean isFlowing = state.getBlock() instanceof BlockDynamicLiquid;

        
        
		float angle = 0;
        boolean hasFlow = false;
    	if (isFlowing) {
                // flow viene en Vec3d (x,z). si ambas componentes son ~0 => sin flujo efectivo.
                float fx = (float) flow.x;
                float fz = (float) flow.z;
                float mag = (float) Math.sqrt(fx * fx + fz * fz);
                hasFlow = mag > 1e-4f; // umbral para evitar ruido
                if (hasFlow) angle = (float) Math.atan2(-fx, fz); // misma convención que usabas
    	}
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
        // =============================
        // DECIDE WHICH FACES TO RENDER
        // =============================
        boolean renderTop = state.shouldSideBeRendered(world, pos, EnumFacing.UP);
        boolean renderBottom = state.shouldSideBeRendered(world, pos, EnumFacing.DOWN);
                
    	EnumFacing[] horizontals = EnumFacing.HORIZONTALS; // [SOUTH, WEST, NORTH, EAST]
    	boolean[] renderSide = new boolean[horizontals.length];

    	for (int i = 0; i < horizontals.length; i++) {
    	    EnumFacing face = horizontals[i];
    	    Block neighborBlock = world.getBlockState(pos.offset(face)).getBlock();
	        int neighborFluidIndex = FiniteFluidLogic.GeneralPurposeLogic.getFluidIndex(neighborBlock); 
	        
    	    renderSide[i] = neighborFluidIndex != fluidIndex && state.shouldSideBeRendered(world, pos, face);
    	}
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	

        // =============================
        // AVOID RENDER IF NOT NECESSARY
        // =============================
    	if (!renderTop && !renderBottom
                && !renderSide[0] && !renderSide[1]
                && !renderSide[2] && !renderSide[3])
        {
            return false;
        }
        
        
        
        
        
        
        
        
        
        
        
        // ============================
        // BASE CODE TO START RENDERING
        // ============================
        else
        {
            // ¿Se renderizó al menos una cara?
            boolean renderedAnything = false;

            // Altura del fluido en las cuatro esquinas
            float heightNW = FiniteFluidLogic.GeneralPurposeLogic.getHeight(world, pos, -1, -1); //h00; //this.getFluidHeight(world, pos, fluidMaterial);
            float heightSW = FiniteFluidLogic.GeneralPurposeLogic.getHeight(world, pos, -1, 1); //h01; //this.getFluidHeight(world, pos.south(), fluidMaterial);
            float heightSE = FiniteFluidLogic.GeneralPurposeLogic.getHeight(world, pos, 1, 1); //h11; //this.getFluidHeight(world, pos.east().south(), fluidMaterial);
            float heightNE = FiniteFluidLogic.GeneralPurposeLogic.getHeight(world, pos, 1, -1); //h10; //this.getFluidHeight(world, pos.east(), fluidMaterial);

            // Coordenadas base del bloque
            double x = pos.getX();
            double y = pos.getY();
            double z = pos.getZ();

            // Epsilon para evitar z-fighting
            float epsilon = 0.001F;
            
         // Coordenadas y UVs base            
            final float baseU0 = isFlowing ? fluidSprites[1].getMinU() : fluidSprites[0].getMinU();
            final float baseV0 = isFlowing ? fluidSprites[1].getMinV() : fluidSprites[0].getMinV();
            final float baseU1 = isFlowing ? fluidSprites[1].getMaxU() : fluidSprites[0].getMaxU();
            final float baseV1 = isFlowing ? fluidSprites[1].getMaxV() : fluidSprites[0].getMaxV();

            // Calcular UVs tipo vanilla si hay flujo
            float uA, uB, uC, uD;
            float vA, vB, vC, vD;

            if (isFlowing) {
                // Magnitud > 0 --> flujo efectivo, usamos UVs tipo vanilla
                float sinAngle = (float) Math.sin(angle) * 0.25f;
                float cosAngle = (float) Math.cos(angle) * 0.25f;

                uA = fluidSprites[1].getInterpolatedU((double) (8.0F + (-cosAngle - sinAngle) * 16.0F));
                vA = fluidSprites[1].getInterpolatedV((double) (8.0F + (-cosAngle + sinAngle) * 16.0F));

                uB = fluidSprites[1].getInterpolatedU((double) (8.0F + (-cosAngle + sinAngle) * 16.0F));
                vB = fluidSprites[1].getInterpolatedV((double) (8.0F + ( cosAngle + sinAngle) * 16.0F));

                uC = fluidSprites[1].getInterpolatedU((double) (8.0F + ( cosAngle + sinAngle) * 16.0F));
                vC = fluidSprites[1].getInterpolatedV((double) (8.0F + ( cosAngle - sinAngle) * 16.0F));

                uD = fluidSprites[1].getInterpolatedU((double) (8.0F + ( cosAngle - sinAngle) * 16.0F));
                vD = fluidSprites[1].getInterpolatedV((double) (8.0F + (-cosAngle - sinAngle) * 16.0F));
            } else {
                // Sin flujo --> usamos UVs simples del sprite
                uA = baseU0; vA = baseV0;
                uB = baseU0; vB = baseV1;
                uC = baseU1; vC = baseV1;
                uD = baseU1; vD = baseV0;
            }

            // Lightmap (vanilla toma luz del bloque superior para top face)
            int packedLightTop = state.getPackedLightmapCoords(world, pos);
            int lightSkyTop = packedLightTop >> 16 & 65535;
            int lightBlockTop = packedLightTop & 65535;



            
            
            
            
            // =======================
            //        TOP FACE
            // =======================
            if (renderTop) {
                // BufferBuilder top face
            	bufferBuilderIn.pos(x + 0.0D, y + heightNW, z + 0.0D).color(colorR, colorG, colorB, 1.0F).tex(uA, vA).lightmap(lightSkyTop, lightBlockTop).endVertex();
            	bufferBuilderIn.pos(x + 0.0D, y + heightSW, z + 1.0D).color(colorR, colorG, colorB, 1.0F).tex(uB, vB).lightmap(lightSkyTop, lightBlockTop).endVertex();
            	bufferBuilderIn.pos(x + 1.0D, y + heightSE, z + 1.0D).color(colorR, colorG, colorB, 1.0F).tex(uC, vC).lightmap(lightSkyTop, lightBlockTop).endVertex();
            	bufferBuilderIn.pos(x + 1.0D, y + heightNE, z + 0.0D).color(colorR, colorG, colorB, 1.0F).tex(uD, vD).lightmap(lightSkyTop, lightBlockTop).endVertex();

                // Bottom face (para vista desde abajo)
            	bufferBuilderIn.pos(x + 0.0D, y + heightNW - epsilon, z + 0.0D).color(colorR, colorG, colorB, 1.0F).tex(uA, vA).lightmap(lightSkyTop, lightBlockTop).endVertex();
            	bufferBuilderIn.pos(x + 1.0D, y + heightNE - epsilon, z + 0.0D).color(colorR, colorG, colorB, 1.0F).tex(uD, vD).lightmap(lightSkyTop, lightBlockTop).endVertex();
            	bufferBuilderIn.pos(x + 1.0D, y + heightSE - epsilon, z + 1.0D).color(colorR, colorG, colorB, 1.0F).tex(uC, vC).lightmap(lightSkyTop, lightBlockTop).endVertex();
            	bufferBuilderIn.pos(x + 0.0D, y + heightSW - epsilon, z + 1.0D).color(colorR, colorG, colorB, 1.0F).tex(uB, vB).lightmap(lightSkyTop, lightBlockTop).endVertex();
                renderedAnything = true;
            }
            
            
            
            
            
            
            
            
            
            
            
            

            /* =======================
             *      BOTTOM FACE
             * ======================= */
            if (renderBottom)
            {
                TextureAtlasSprite bottomSprite = fluidSprites[0];

                float minU = bottomSprite.getMinU();
                float maxU = bottomSprite.getMaxU();
                float minV = bottomSprite.getMinV();
                float maxV = bottomSprite.getMaxV();

                int light = state.getPackedLightmapCoords(
                        world, pos.down()
                );
                int sky = light >> 16 & 65535;
                int block = light & 65535;

                bufferBuilderIn.pos(x,     y, z + 1).color(0.5F, 0.5F, 0.5F, 1).tex(minU, maxV).lightmap(sky, block).endVertex();
                bufferBuilderIn.pos(x,     y, z    ).color(0.5F, 0.5F, 0.5F, 1).tex(minU, minV).lightmap(sky, block).endVertex();
                bufferBuilderIn.pos(x + 1, y, z    ).color(0.5F, 0.5F, 0.5F, 1).tex(maxU, minV).lightmap(sky, block).endVertex();
                bufferBuilderIn.pos(x + 1, y, z + 1).color(0.5F, 0.5F, 0.5F, 1).tex(maxU, maxV).lightmap(sky, block).endVertex();

                renderedAnything = true;
            }
            
            
            
            
            
            
            
            
            
            
            
            
     
            
            

            /* =======================
             *      SIDE FACES
             * ======================= */
            for (int sideIndex = 0; sideIndex < 4; sideIndex++) {
                if (!renderSide[sideIndex]) continue; // EVITA RENDERIZAR CARAS ENTRE EL MISMO FLUIDO
                //if (renderSide[sideIndex]) continue; // EVITA RENDERIZAR CARAS ENTRE EL FLUIOD Y OTRAS COSAS
                
                // Offset del vecino según la dirección de la cara
                int offsetX = 0;
                int offsetZ = 0;
                
                if (sideIndex == 0) offsetZ = 1; // SOUTH 
                else if (sideIndex == 1) offsetX = -1; // WEST
                else if (sideIndex == 2) offsetZ = -1; // NORTH
                else if (sideIndex == 3) offsetX = 1; // EAST

                BlockPos neighborPos = pos.add(offsetX, 0, offsetZ);
                TextureAtlasSprite sideSprite = isFlowing ? fluidSprites[1] : fluidSprites[0]; // sprite base

                // Verificar overlay (si es agua y el vecino tiene cara sólida)
                if (!isLava) {
                    IBlockState neighborState = world.getBlockState(neighborPos);
                    if (neighborState.getBlockFaceShape(world, neighborPos, EnumFacing.values()[sideIndex + 2].getOpposite()) == BlockFaceShape.SOLID) {
                        sideSprite = field_187501_d;
                    }
                }

                // Altura superior e inferior de los vértices de esta cara
                float yTopStart, yTopEnd;
                double xStart, xEnd, zStart, zEnd;

                if (sideIndex == 0) { // SOUTH
                    yTopStart = heightSE;
                    yTopEnd   = heightSW;
                    xStart = x + 1.0D;
                    xEnd = x;
                    zStart = z + 1.0D - 0.001D;
                    zEnd = z + 1.0D - 0.001D;
                } else if (sideIndex == 1) { // WEST
                    yTopStart = heightSW;
                    yTopEnd   = heightNW;
                    xStart = x + 0.001D;
                    xEnd = x + 0.001D;
                    zStart = z + 1.0D;
                    zEnd = z;
                } else if (sideIndex == 2) { // NORTH
                    yTopStart = heightNW;
                    yTopEnd   = heightNE;
                    xStart = x;
                    xEnd = x + 1.0D;
                    zStart = z + 0.001D;
                    zEnd = z + 0.001D;
                } else { // EAST
                    yTopStart = heightNE;
                    yTopEnd   = heightSE;
                    xStart = x + 1.0D - 0.001D;
                    xEnd = x + 1.0D - 0.001D;
                    zStart = z;
                    zEnd = z + 1.0D;
                }

                renderedAnything = true; // marca que al menos un lado se renderiza

                // Calcular UVs
                float uStart = sideSprite.getInterpolatedU(0.0D);
                float uEnd   = sideSprite.getInterpolatedU(8.0D);
                float vStart = sideSprite.getInterpolatedV((1.0F - yTopStart) * 16.0F * 0.5F);
                float vEnd   = sideSprite.getInterpolatedV((1.0F - yTopEnd)   * 16.0F * 0.5F);
                float vBottom = sideSprite.getInterpolatedV(8.0D);

                // Luz del bloque vecino
                int packedLight = state.getPackedLightmapCoords(world, neighborPos);
                int lightHigh = (packedLight >> 16) & 65535;
                int lightLow  = packedLight & 65535;

                // Ajuste de color según la cara
                float sideColorFactor = (sideIndex < 2) ? 0.8F : 0.6F;
                float red   = colorR * sideColorFactor;
                float green = colorG * sideColorFactor;
                float blue  = colorB * sideColorFactor;

                // Construir quad lateral superior
                bufferBuilderIn.pos(xStart, yTopStart + y, zStart).color(red, green, blue, 1.0F).tex(uStart, vStart).lightmap(lightHigh, lightLow).endVertex();
                bufferBuilderIn.pos(xEnd,   yTopEnd   + y, zEnd).color(red, green, blue, 1.0F).tex(uEnd,   vEnd).lightmap(lightHigh, lightLow).endVertex();
                bufferBuilderIn.pos(xEnd,   y, zEnd).color(red, green, blue, 1.0F).tex(uEnd,   vBottom).lightmap(lightHigh, lightLow).endVertex();
                bufferBuilderIn.pos(xStart, y, zStart).color(red, green, blue, 1.0F).tex(uStart, vBottom).lightmap(lightHigh, lightLow).endVertex();

                // Si no es overlay, renderizar también el mismo quad invertido para evitar z-fighting
                if (sideSprite != field_187501_d) {
                    bufferBuilderIn.pos(xStart, y, zStart).color(red, green, blue, 1.0F).tex(uStart, vBottom).lightmap(lightHigh, lightLow).endVertex();
                    bufferBuilderIn.pos(xEnd,   y, zEnd).color(red, green, blue, 1.0F).tex(uEnd,   vBottom).lightmap(lightHigh, lightLow).endVertex();
                    bufferBuilderIn.pos(xEnd,   yTopEnd + y, zEnd).color(red, green, blue, 1.0F).tex(uEnd,   vEnd).lightmap(lightHigh, lightLow).endVertex();
                    bufferBuilderIn.pos(xStart, yTopStart + y, zStart).color(red, green, blue, 1.0F).tex(uStart, vStart).lightmap(lightHigh, lightLow).endVertex();
                }
            }

            
            
            
            return renderedAnything;
        }
    }

}
