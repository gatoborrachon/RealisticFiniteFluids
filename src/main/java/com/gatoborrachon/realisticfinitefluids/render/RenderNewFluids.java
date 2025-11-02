package com.gatoborrachon.realisticfinitefluids.render;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.gatoborrachon.realisticfinitefluids.blocks.BlockNewWater_Flow;
import com.gatoborrachon.realisticfinitefluids.logic.FiniteFluidLogic;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;

public class RenderNewFluids {
    //private final List<NewFluidType> liquids;
    private float alpha;

    public RenderNewFluids() {
        //this.liquids = liquids;
    }

    public List<BakedQuad> renderBlockNewFluid(IBlockState state, 
    	    float h000, float h100, float h010, float h110,
    	    Map<EnumFacing, IBlockState> neighborStates,
    	    int color, int fluidIndex,
    	    TextureAtlasSprite sprite,
    	    EnumFacing sideRequested,
    	    Vec3d flow) {
        
    		//Obtenemos Color, Alpha y coordenadas UV minimas y maximas.
    	    List<BakedQuad> quads = new ArrayList<>();
    	    float r = ((color >> 16) & 255) / 255.0f;
    	    float g = ((color >> 8) & 255) / 255.0f;
    	    float b = (color & 255) / 255.0f;
    	    //this.alpha = ((color >> 24) & 255) / 255.0f;
    	    this.alpha = 1.0f; // alpha fijo porque el color del biome no tiene componente alpha

    	    float u0 = sprite.getMinU();
    	    float v0 = sprite.getMinV();
    	    float u1 = sprite.getMaxU();
    	    float v1 = sprite.getMaxV();   
    	    
            final float baseU0 = sprite.getMinU();
            final float baseV0 = sprite.getMinV();
            final float baseU1 = sprite.getMaxU();
            final float baseV1 = sprite.getMaxV();
    	    
    	    //Centrar la textura de los liquidos vanilla flowing
    		float midU = (u0 + u1) / 2f;
    		float midV = (v0 + v1) / 2f;
    		
    		/*if (sideRequested == EnumFacing.UP) {
        		System.out.println("//////////////");
        		System.out.println("U0: "+u0);
        		System.out.println("U1: "+u1);
        		System.out.println("V0: "+v0);
        		System.out.println("V1: "+v1);

        		System.out.println("midU: "+midU);
        		System.out.println("midV: "+midV);
        		System.out.println("//////////////");
    		}*/
    		
    		float angle = 0;
            boolean hasFlow = false;
	    	if (state.getBlock() instanceof BlockNewWater_Flow) {
	                // flow viene en Vec3d (x,z). si ambas componentes son ~0 => sin flujo efectivo.
	                float fx = (float) flow.x;
	                float fz = (float) flow.z;
	                float mag = (float) Math.sqrt(fx * fx + fz * fz);
	                hasFlow = mag > 1e-4f; // umbral para evitar ruido
	                if (hasFlow) angle = (float) Math.atan2(fx, -fz); // misma convención que usabas

	    		/*if (sideRequested == EnumFacing.UP) {
	        		System.out.println("--------------");
	    			System.out.println("U0: "+u0);
	        		System.out.println("U1: "+u1);
	        		System.out.println("V0: "+v0);
	        		System.out.println("V1: "+v1);
	        		System.out.println("midU: "+midU);
	        		System.out.println("midV: "+midV);
	        		System.out.println("uSpan: "+uSpan);
	        		System.out.println("vSpan: "+vSpan);
	        		System.out.println("--------------");
	    		}*/
	    	}
	    	
    	    //Decidimos si renderizar la cara superior (cuando tenemos bloques del mismo tipo encima: No renderizar, cuando son de distinto tipo: Si renderizar)
    	    IBlockState upState = neighborStates.get(EnumFacing.UP); //Obtener IBlockState vecino superior.
    	    Block upBlock = upState != null ? upState.getBlock() : Blocks.AIR; //Obtener Block vecino superior, si es null entonces usamos Blocks.AIR.
    	    boolean renderTop = true; //Asumimos que renderizamos la parte superior.
	        int topFluidIndex = FiniteFluidLogic.GeneralPurposeLogic.getFluidIndex(upBlock, null, null);
	        if (fluidIndex == topFluidIndex) renderTop = false;
    	    
    	    //TESTS PREVIOS PARA DETERMINAR CUANDO SE RENDERIZABA EL BLOQUE SUPERIOR
    	    /*if (sideRequested == EnumFacing.UP && upState.getBlock() != state.getBlock()) {
    	    System.out.println("               ");
    	    System.out.println("renderTop: "+renderTop);
    	    //System.out.println("sideRequested == null: "+(sideRequested == null));
    	    //System.out.println("sideRequested == EnumFacing.UP: "+(sideRequested == EnumFacing.UP));
    	    //System.out.println("upState.getBlock() != state.getBlock(): "+(upState.getBlock() != state.getBlock()));
    	    }*/
    	    
    	    // =========================
    	    // Cara Superior
    	    // ========================= 
    	    if (renderTop && (sideRequested == null || sideRequested == EnumFacing.UP) && upState.getBlock() != state.getBlock()) {
    	    	//System.out.println(sideRequested);
    	    	//System.out.printf("h00=%.2f, h10=%.2f, h11=%.2f, h01=%.2f%n", h000, h100, h110, h010);
    	    	//System.out.printf("Color ARGB = 0x%08X, r=%.2f g=%.2f b=%.2f%n", color, r, g, b);
    	    	
    	    	//System.out.println("Angulo: "+angle);

                // si NO hay flujo significativo, usa las UVs simples del sprite (still)
                if (!(state.getBlock() instanceof BlockNewWater_Flow)) {
                    // top quad con UVs sin rotación ni offsets
                    UnpackedBakedQuad.Builder topUp = new UnpackedBakedQuad.Builder(DefaultVertexFormats.BLOCK);
                    topUp.setQuadOrientation(EnumFacing.UP);
                    topUp.setTexture(sprite);
                    putVertex(topUp, 0f, h000, 0f, r, g, b, this.alpha, baseU0, baseV0);
                    putVertex(topUp, 0f, h010, 1f, r, g, b, this.alpha, baseU0, baseV1);
                    putVertex(topUp, 1f, h110, 1f, r, g, b, this.alpha, baseU1, baseV1);
                    putVertex(topUp, 1f, h100, 0f, r, g, b, this.alpha, baseU1, baseV0);
                    quads.add(topUp.build());

                    // bottom cap (optional, keep as small offset to avoid z-fighting)
                    UnpackedBakedQuad.Builder topDown = new UnpackedBakedQuad.Builder(DefaultVertexFormats.BLOCK);
                    topDown.setQuadOrientation(EnumFacing.DOWN);
                    topDown.setTexture(sprite);
                    putVertex(topDown, 0f, h000 - 0.001f, 0f, r, g, b, 1.0F, baseU0, baseV0);
                    putVertex(topDown, 1f, h100 - 0.001f, 0f, r, g, b, 1.0F, baseU1, baseV0);
                    putVertex(topDown, 1f, h110 - 0.001f, 1f, r, g, b, 1.0F, baseU1, baseV1);
                    putVertex(topDown, 0f, h010 - 0.001f, 1f, r, g, b, 1.0F, baseU0, baseV1);
                    quads.add(topDown.build());
                } else {
                    // HAY FLUJO: usa método tipo vanilla para calcular las UVs por vértice
                    // No escalamos `baseU0..baseV1`. En su lugar calculamos UVs por vértice en "pixel coords"
                    // inspirado en BlockFluidRenderer:
                    // f21 = sin(angle) * 0.25F
                    // f22 = cos(angle) * 0.25F
                    float f21 = (float) Math.sin(angle) * 0.25f;
                    float f22 = (float) Math.cos(angle) * 0.25f;

                    // compute the 4 UVs using sprite.getInterpolatedU/V (vanilla approach)
                    // center 8.0 (middle of 16x16 tile) then offset by (-f22 - f21)*16 etc
                    float uA = sprite.getInterpolatedU((double) (8.0F + (-f22 - f21) * 16.0F));
                    float vA = sprite.getInterpolatedV((double) (8.0F + (-f22 + f21) * 16.0F));

                    float uB = sprite.getInterpolatedU((double) (8.0F + (-f22 + f21) * 16.0F));
                    float vB = sprite.getInterpolatedV((double) (8.0F + (f22 + f21) * 16.0F));

                    float uC = sprite.getInterpolatedU((double) (8.0F + (f22 + f21) * 16.0F));
                    float vC = sprite.getInterpolatedV((double) (8.0F + (f22 - f21) * 16.0F));

                    float uD = sprite.getInterpolatedU((double) (8.0F + (f22 - f21) * 16.0F));
                    float vD = sprite.getInterpolatedV((double) (8.0F + (-f22 - f21) * 16.0F));

                    // Construimos los quads con esos UVs (igual que vanilla)
                    UnpackedBakedQuad.Builder topUp = new UnpackedBakedQuad.Builder(DefaultVertexFormats.BLOCK);
                    topUp.setQuadOrientation(EnumFacing.UP);
                    topUp.setTexture(sprite);

                    // Nota: el orden de vértices debe coincidir con el que espera el renderer (igual que tu código original)
                    putVertex(topUp, 0f, h000, 0f, r, g, b, this.alpha, uA, vA);
                    putVertex(topUp, 0f, h010, 1f, r, g, b, this.alpha, uB, vB);
                    putVertex(topUp, 1f, h110, 1f, r, g, b, this.alpha, uC, vC);
                    putVertex(topUp, 1f, h100, 0f, r, g, b, this.alpha, uD, vD);
                    quads.add(topUp.build());

                    // bottom face (usamos los mismos u/v pero en orden invertido)
                    UnpackedBakedQuad.Builder topDown = new UnpackedBakedQuad.Builder(DefaultVertexFormats.BLOCK);
                    topDown.setQuadOrientation(EnumFacing.DOWN);
                    topDown.setTexture(sprite);
                    putVertex(topDown, 0f, h000 - 0.001f, 0f, r, g, b, 1.0F, uA, vA);
                    putVertex(topDown, 1f, h100 - 0.001f, 0f, r, g, b, 1.0F, uD, vD);
                    putVertex(topDown, 1f, h110 - 0.001f, 1f, r, g, b, 1.0F, uC, vC);
                    putVertex(topDown, 0f, h010 - 0.001f, 1f, r, g, b, 1.0F, uB, vB);
                    quads.add(topDown.build());
                }
    	        
    	    	return quads; // Si solo querían UP, devuelve aquí
    	        
    	    }
    	    
    	    

    	    
    	    // =========================
    	    // Caras Laterares e Inferior
    	    // ========================= 
    	    EnumFacing[] sides = EnumFacing.values();
    	    for (EnumFacing face : sides) {
    	        if (sideRequested != null && face != sideRequested) continue; // si piden una cara específica, las demas las descartamos
    	        if (face == EnumFacing.UP) continue; // Ya hicimos la cara UP arriba

    	        //Obtenemos el IBlockState y Block del bloque vecino para la respectiva cara a renderizar
    	        IBlockState neighborState = neighborStates.get(face); 
    	        if (neighborState == null) continue;
    	        Block neighborBlock = neighborState.getBlock();

    	        //Suponemos que no vamos a renderizar la respectiva cara solicitada
    	        boolean shouldRender = false;


    	     // Si es aire, sí renderiza
    	     if (neighborBlock == Blocks.AIR) {
    	         shouldRender = true;
    	     } else {
    	         int neighborFluidIndex = FiniteFluidLogic.GeneralPurposeLogic.getFluidIndex(neighborBlock, null, null);

    	    	 if (neighborFluidIndex == fluidIndex) {
    	    	     shouldRender = false; // mismo tipo de fluido --> no renderizar (porque la cara top de uno y otro bloque se fusionan)
    	    	 } else {
    	    	     shouldRender = true;  // diferente tipo --> sí renderizar
    	    	 }
    	     }

    	     //Si no debemos renderizar, vamos con la siguiente cara.
    	        if (!shouldRender) continue;
    	        
    	    	if (state.getBlock() instanceof BlockNewWater_Flow) {
		    		u1 = midU + (u1 - midU) * 0.5f;
		    		v1 = midV + (v1 - midV) * 0.5f;
		    		u0 = midU - (midU - u0) * 0.5f;
		    		v0 = midV - (midV - v0) * 0.5f;
    	    	}

    	        UnpackedBakedQuad.Builder sideBuilder = new UnpackedBakedQuad.Builder(DefaultVertexFormats.BLOCK);
    	        sideBuilder.setQuadOrientation(face);
    	        sideBuilder.setTexture(sprite);
    	        switch (face) {
    	            case NORTH:
    	                putVertex(sideBuilder, 1f, 0f, 0f, r, g, b, this.alpha, u1, v1);
    	                putVertex(sideBuilder, 0f, 0f, 0f, r, g, b, this.alpha, u0, v1);
    	                putVertex(sideBuilder, 0f, h000, 0f, r, g, b, this.alpha, u0, v0);
    	                putVertex(sideBuilder, 1f, h100, 0f, r, g, b, this.alpha, u1, v0);
    	                break;
    	            case SOUTH:
    	                putVertex(sideBuilder, 0f, 0f, 1f, r, g, b, this.alpha, u0, v1);
    	                putVertex(sideBuilder, 1f, 0f, 1f, r, g, b, this.alpha, u1, v1);
    	                putVertex(sideBuilder, 1f, h110, 1f, r, g, b, this.alpha, u1, v0);
    	                putVertex(sideBuilder, 0f, h010, 1f, r, g, b, this.alpha, u0, v0);
    	                break;
    	            case WEST:
    	                putVertex(sideBuilder, 0f, 0f, 0f, r, g, b, this.alpha, u0, v1);
    	                putVertex(sideBuilder, 0f, 0f, 1f, r, g, b, this.alpha, u1, v1);
    	                putVertex(sideBuilder, 0f, h010, 1f, r, g, b, this.alpha, u1, v0);
    	                putVertex(sideBuilder, 0f, h000, 0f, r, g, b, this.alpha, u0, v0);
    	                break;
    	            case EAST:
    	                putVertex(sideBuilder, 1f, 0f, 1f, r, g, b, this.alpha, u0, v1);
    	                putVertex(sideBuilder, 1f, 0f, 0f, r, g, b, this.alpha, u1, v1);
    	                putVertex(sideBuilder, 1f, h100, 0f, r, g, b, this.alpha, u1, v0);
    	                putVertex(sideBuilder, 1f, h110, 1f, r, g, b, this.alpha, u0, v0);
    	                break;
    	            case DOWN:
    	                putVertex(sideBuilder, 0f, 0f, 0f, r, g, b, this.alpha, u0, v0);
    	                putVertex(sideBuilder, 1f, 0f, 0f, r, g, b, this.alpha, u1, v0);
    	                putVertex(sideBuilder, 1f, 0f, 1f, r, g, b, this.alpha, u1, v1);
    	                putVertex(sideBuilder, 0f, 0f, 1f, r, g, b, this.alpha, u0, v1);
    	                break;
    	            default:
    	                continue;
    	        }
    	        quads.add(sideBuilder.build());
    	    }

    	    return quads;
    	}

    
    	//Esta funcion es la que se encarga de añadirle los respectivos parametros de posicion, color, coordenada UV, etc a cada vertice unico (4 por cara) que vamos a renderizar
    	private void putVertex(UnpackedBakedQuad.Builder builder, float x, float y, float z,
    	                       float r, float g, float b, float a,
    	                       float u, float v) {
    	    for (int e = 0; e < builder.getVertexFormat().getElementCount(); e++) {
    	        VertexFormatElement elem = builder.getVertexFormat().getElement(e);
    	        switch (elem.getUsage()) {
    	            case POSITION:
    	                builder.put(e, x, y, z, 1.0f);
    	                break;
    	            case COLOR:
    	                builder.put(e, r, g, b, a);
    	                break;
    	            case UV:
    	                if (elem.getIndex() == 0)
    	                    builder.put(e, u, v, 0f, 1f);
    	                else
    	                    builder.put(e, 0f, 0f, 0f, 1f);
    	                break;
    	            case NORMAL:
    	                builder.put(e, 0f, 1f, 0f, 0f);
    	                break;
    	            default:
    	                builder.put(e);
    	                break;
    	        }
    	    }
    	}
    
}
    