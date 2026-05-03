package com.gatoborrachon.realisticfinitefluids.render;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.gatoborrachon.realisticfinitefluids.References;
import com.gatoborrachon.realisticfinitefluids.logic.FiniteFluidLogic;
import com.gatoborrachon.realisticfinitefluids.logic.RealisticFiniteFluidFunctions;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;

public class RenderNewFluidsClassic {
	//private final List<NewFluidType> liquids;
	//private float alpha;

	/*public RenderNewFluidsClassic() {
		//this.liquids = liquids;
	}*/

	public List<BakedQuad> renderBlockNewGasClassic(IBlockState state, 
			float h000, float h100, float h010, float h110,
			Map<EnumFacing, IBlockState> neighborStates,
			int color, int fluidIndex,
			TextureAtlasSprite spriteFlowing,
			TextureAtlasSprite spriteStill, EnumFacing sideRequested,
			Vec3d flow, boolean isStill, float LEVEL) {

		//Obtenemos Color, Alpha y coordenadas UV minimas y maximas.
		List<BakedQuad> quads = new ArrayList<>();
		float r = ((color >> 16) & 255) / 255.0f;
		float g = ((color >> 8) & 255) / 255.0f;
		float b = (color & 255) / 255.0f;
		float alpha = LEVEL + ( 1.0f - ((float)References.MAXIMUM_LEVEL/10) );
		//System.out.println("[RFF] EXTRA: "+(LEVEL + 1.0f - ((float)References.MAXIMUM_LEVEL/10)));
		//1.0f-(1.0f-LEVEL); // alpha fijo porque el color del biome no tiene componente alpha

		float u0 = 0;
		float v0 = 0;
		float u1 = 0;
		float v1 = 0;   

		float baseU0 = 0;
		float baseV0 = 0;
		float baseU1 = 0;
		float baseV1 = 0;

		if (isStill) {
			u0 = spriteStill.getMinU();
			v0 = spriteStill.getMinV();
			u1 = spriteStill.getMaxU();
			v1 = spriteStill.getMaxV();   

			baseU0 = spriteStill.getMinU();
			baseV0 = spriteStill.getMinV();
			baseU1 = spriteStill.getMaxU();
			baseV1 = spriteStill.getMaxV();
		} else {
			u0 = spriteFlowing.getMinU();
			v0 = spriteFlowing.getMinV();
			u1 = spriteFlowing.getMaxU();
			v1 = spriteFlowing.getMaxV();   

			baseU0 = spriteFlowing.getMinU();
			baseV0 = spriteFlowing.getMinV();
			baseU1 = spriteFlowing.getMaxU();
			baseV1 = spriteFlowing.getMaxV();
		}


		float angle = 0;
		boolean hasFlow = false;
		//if (state.getBlock() instanceof BlockFiniteFluid_Flow) {
		if (!isStill) {
			// flow viene en Vec3d (x,z). si ambas componentes son ~0 => sin flujo efectivo.
			float fx = (float) flow.x;
			float fz = (float) flow.z;
			float mag = (float) Math.sqrt(fx * fx + fz * fz);
			hasFlow = mag > 1e-4f; // umbral para evitar ruido
			if (hasFlow) angle = (float) Math.atan2(fx, -fz); // misma convención que usabas
		}

		//Decidimos si renderizar la cara superior (cuando tenemos bloques del mismo tipo encima: No renderizar, cuando son de distinto tipo: Si renderizar)
		IBlockState upState = neighborStates.get(EnumFacing.UP); //Obtener IBlockState vecino superior.
		Block upBlock = upState != null ? RealisticFiniteFluidFunctions.getBlock(null, null, upState) : Blocks.AIR; //Obtener Block vecino superior, si es null entonces usamos Blocks.AIR.
		boolean renderTop = true; //Asumimos que renderizamos la parte superior.
		int topFluidIndex = FiniteFluidLogic.GeneralPurposeLogic.getFluidIndex(upBlock);
		if (fluidIndex == topFluidIndex) renderTop = false;













		// =========================
		// Cara Superior
		// ========================= 
		if (renderTop && (sideRequested == null || sideRequested == EnumFacing.UP) 
				&& RealisticFiniteFluidFunctions.getBlock(null, null, upState) != RealisticFiniteFluidFunctions.getBlock(null, null, state)) {
			// si NO hay flujo significativo, usa las UVs simples del sprite (still)
			if (isStill) {
				// top quad con UVs sin rotación ni offsets
				UnpackedBakedQuad.Builder topUp = new UnpackedBakedQuad.Builder(DefaultVertexFormats.BLOCK);
				topUp.setQuadOrientation(EnumFacing.UP);
				topUp.setTexture(spriteStill);
				putVertex(topUp, 0f, h000, 0f, r, g, b, alpha, baseU0, baseV0);
				putVertex(topUp, 0f, h010, 1f, r, g, b, alpha, baseU0, baseV1);
				putVertex(topUp, 1f, h110, 1f, r, g, b, alpha, baseU1, baseV1);
				putVertex(topUp, 1f, h100, 0f, r, g, b, alpha, baseU1, baseV0);
				quads.add(topUp.build());

				// bottom cap (optional, keep as small offset to avoid z-fighting)
				/*
				UnpackedBakedQuad.Builder topDown = new UnpackedBakedQuad.Builder(DefaultVertexFormats.BLOCK);
				topDown.setQuadOrientation(EnumFacing.DOWN);
				topDown.setTexture(spriteStill);
				putVertex(topDown, 0f, h000 - 0.001f, 0f, r, g, b, 1.0F, baseU0, baseV0);
				putVertex(topDown, 1f, h100 - 0.001f, 0f, r, g, b, 1.0F, baseU1, baseV0);
				putVertex(topDown, 1f, h110 - 0.001f, 1f, r, g, b, 1.0F, baseU1, baseV1);
				putVertex(topDown, 0f, h010 - 0.001f, 1f, r, g, b, 1.0F, baseU0, baseV1);
				quads.add(topDown.build());
				 */
			} else {
				// HAY FLUJO: usa método tipo vanilla para calcular las UVs por vértice
				// No escalamos `baseU0..baseV1`. En su lugar calculamos UVs por vértice en "pixel coords"
				// inspirado en BlockFluidRenderer:
				float stitchFactor = 0.35f;
				if (RealisticFiniteFluidFunctions.getBlock(null, null, state) instanceof BlockLiquid) stitchFactor = 0.25f;
				float f21 = (float) Math.sin(angle) * stitchFactor;
				float f22 = (float) Math.cos(angle) * stitchFactor;

				// compute the 4 UVs using sprite.getInterpolatedU/V (vanilla approach)
				// center 8.0 (middle of 16x16 tile) then offset by (-f22 - f21)*16 etc
				float uA = spriteFlowing.getInterpolatedU((double) (8.0F + (-f22 - f21) * 16.0F));
				float vA = spriteFlowing.getInterpolatedV((double) (8.0F + (-f22 + f21) * 16.0F));

				float uB = spriteFlowing.getInterpolatedU((double) (8.0F + (-f22 + f21) * 16.0F));
				float vB = spriteFlowing.getInterpolatedV((double) (8.0F + (f22 + f21) * 16.0F));

				float uC = spriteFlowing.getInterpolatedU((double) (8.0F + (f22 + f21) * 16.0F));
				float vC = spriteFlowing.getInterpolatedV((double) (8.0F + (f22 - f21) * 16.0F));

				float uD = spriteFlowing.getInterpolatedU((double) (8.0F + (f22 - f21) * 16.0F));
				float vD = spriteFlowing.getInterpolatedV((double) (8.0F + (-f22 - f21) * 16.0F));

				// Construimos los quads con esos UVs (igual que vanilla)
				UnpackedBakedQuad.Builder topUp = new UnpackedBakedQuad.Builder(DefaultVertexFormats.BLOCK);
				topUp.setQuadOrientation(EnumFacing.UP);
				topUp.setTexture(spriteFlowing);

				// Nota: el orden de vértices debe coincidir con el que espera el renderer (igual que tu código original)
				putVertex(topUp, 0f, h000, 0f, r, g, b, alpha, uA, vA);
				putVertex(topUp, 0f, h010, 1f, r, g, b, alpha, uB, vB);
				putVertex(topUp, 1f, h110, 1f, r, g, b, alpha, uC, vC);
				putVertex(topUp, 1f, h100, 0f, r, g, b, alpha, uD, vD);
				quads.add(topUp.build());

				// bottom face (usamos los mismos u/v pero en orden invertido)
				/*
				UnpackedBakedQuad.Builder topDown = new UnpackedBakedQuad.Builder(DefaultVertexFormats.BLOCK);
				topDown.setQuadOrientation(EnumFacing.DOWN);
				topDown.setTexture(spriteFlowing);
				putVertex(topDown, 0f, h000 - 0.001f, 0f, r, g, b, alpha, uA, vA);
				putVertex(topDown, 1f, h100 - 0.001f, 0f, r, g, b, alpha, uD, vD);
				putVertex(topDown, 1f, h110 - 0.001f, 1f, r, g, b, alpha, uC, vC);
				putVertex(topDown, 0f, h010 - 0.001f, 1f, r, g, b, alpha, uB, vB);
				quads.add(topDown.build());
				 */
			}

			return quads; // Si solo querían UP, devuelve aquí

		}


		//System.out.println("[RFF]");











		// =========================
		// Caras Laterales e Inferior
		// ========================= 
		EnumFacing[] sides = EnumFacing.values();
		//System.out.println("[RFF] sideRequested: "+sideRequested);
		for (EnumFacing face : sides) {
			if (sideRequested != null && face != sideRequested) continue; // si piden una cara específica, las demas las descartamos
			if (face == EnumFacing.UP) continue; // Ya hicimos la cara UP arriba

			//Obtenemos el IBlockState y Block del bloque vecino para la respectiva cara a renderizar
			IBlockState neighborState = neighborStates.get(face); 
			if (neighborState == null) continue;
			Block neighborBlock = RealisticFiniteFluidFunctions.getBlock(null, null, neighborState);
			//if (face == EnumFacing.SOUTH) System.out.println("[RFF] neighborBlock: "+neighborBlock.toString());

			//Suponemos que no vamos a renderizar la respectiva cara solicitada
			boolean shouldRender = false;


			// Si es aire, sí renderiza
			if (neighborBlock == Blocks.AIR) {
				shouldRender = true;
			} else {
				int neighborFluidIndex = FiniteFluidLogic.GeneralPurposeLogic.getFluidIndex(neighborBlock);
				shouldRender = !(neighborFluidIndex == fluidIndex);
				//System.out.println("[RFF] neighborFluidIndex: "+neighborFluidIndex);
				//System.out.println("[RFF] fluidIndex: "+fluidIndex);
			}

			//Si no debemos renderizar, vamos con la siguiente cara.
			if (!shouldRender) continue;

			UnpackedBakedQuad.Builder sideBuilder = new UnpackedBakedQuad.Builder(DefaultVertexFormats.BLOCK);
			sideBuilder.setQuadOrientation(face);
			if (isStill) {
				sideBuilder.setTexture(spriteStill);
			} else {
				sideBuilder.setTexture(spriteFlowing);
			}
			switch (face) {
			case NORTH:
				putVertex(sideBuilder, 1f, 0f, 0f, r, g, b, alpha, u1, v1);
				putVertex(sideBuilder, 0f, 0f, 0f, r, g, b, alpha, u0, v1);
				putVertex(sideBuilder, 0f, h000, 0f, r, g, b, alpha, u0, v0);
				putVertex(sideBuilder, 1f, h100, 0f, r, g, b, alpha, u1, v0);
				break;
			case SOUTH:
				putVertex(sideBuilder, 0f, 0f, 1f, r, g, b, alpha, u0, v1);
				putVertex(sideBuilder, 1f, 0f, 1f, r, g, b, alpha, u1, v1);
				putVertex(sideBuilder, 1f, h110, 1f, r, g, b, alpha, u1, v0);
				putVertex(sideBuilder, 0f, h010, 1f, r, g, b, alpha, u0, v0);
				break;
			case WEST:
				putVertex(sideBuilder, 0f, 0f, 0f, r, g, b, alpha, u0, v1);
				putVertex(sideBuilder, 0f, 0f, 1f, r, g, b, alpha, u1, v1);
				putVertex(sideBuilder, 0f, h010, 1f, r, g, b, alpha, u1, v0);
				putVertex(sideBuilder, 0f, h000, 0f, r, g, b, alpha, u0, v0);
				break;
			case EAST:
				putVertex(sideBuilder, 1f, 0f, 1f, r, g, b, alpha, u0, v1);
				putVertex(sideBuilder, 1f, 0f, 0f, r, g, b, alpha, u1, v1);
				putVertex(sideBuilder, 1f, h100, 0f, r, g, b, alpha, u1, v0);
				putVertex(sideBuilder, 1f, h110, 1f, r, g, b, alpha, u0, v0);
				break;
			case DOWN:
				putVertex(sideBuilder, 0f, 0f, 0f, r, g, b, alpha, u0, v0);
				putVertex(sideBuilder, 1f, 0f, 0f, r, g, b, alpha, u1, v0);
				putVertex(sideBuilder, 1f, 0f, 1f, r, g, b, alpha, u1, v1);
				putVertex(sideBuilder, 0f, 0f, 1f, r, g, b, alpha, u0, v1);
				break;
			default:
				continue;
			}
			quads.add(sideBuilder.build());
		}

		return quads;
	}









	public List<BakedQuad> renderBlockNewFluidClassic(IBlockState state, 
			float h000, float h100, float h010, float h110,
			Map<EnumFacing, IBlockState> neighborStates,
			int color, int fluidIndex,
			TextureAtlasSprite spriteFlowing,
			TextureAtlasSprite spriteStill, EnumFacing sideRequested,
			Vec3d flow, boolean isStill) {

		//isStill = !isStill;

		//Obtenemos Color, Alpha y coordenadas UV minimas y maximas.
		List<BakedQuad> quads = new ArrayList<>();
		float r = ((color >> 16) & 255) / 255.0f;
		float g = ((color >> 8) & 255) / 255.0f;
		float b = (color & 255) / 255.0f;
		//this.alpha = ((color >> 24) & 255) / 255.0f;
		float alpha = 1.0f; // alpha fijo porque el color del biome no tiene componente alpha

		float u0 = 0;
		float v0 = 0;
		float u1 = 0;
		float v1 = 0;   

		float baseU0 = 0;
		float baseV0 = 0;
		float baseU1 = 0;
		float baseV1 = 0;

		if (isStill || !FiniteFluidLogic.dynamicOrStaticTexture) {
			u0 = spriteStill.getMinU();
			v0 = spriteStill.getMinV();
			u1 = spriteStill.getMaxU();
			v1 = spriteStill.getMaxV();   

			baseU0 = spriteStill.getMinU();
			baseV0 = spriteStill.getMinV();
			baseU1 = spriteStill.getMaxU();
			baseV1 = spriteStill.getMaxV();
		} else {
			u0 = spriteFlowing.getMinU();
			v0 = spriteFlowing.getMinV();
			u1 = u0 + (spriteFlowing.getMaxU() - spriteFlowing.getMinU()) * 0.55f;
			v1 = v0 + (spriteFlowing.getMaxV() - spriteFlowing.getMinV()) * 0.55f;

			baseU0 = spriteFlowing.getMinU();
			baseV0 = spriteFlowing.getMinV();
			baseU1 = spriteFlowing.getMaxU();
			baseV1 = spriteFlowing.getMaxV();
		}

		/*if (sideRequested == EnumFacing.UP) {
        		System.out.println("//////////////");
        		System.out.println("U0: "+u0);
        		System.out.println("U1: "+u1);
        		System.out.println("V0: "+v0);
        		System.out.println("V1: "+v1);

        		//System.out.println("midU: "+midU);
        		//System.out.println("midV: "+midV);
        		System.out.println("//////////////");
    		}*/

		float angle = 0;
		boolean hasFlow = false;
		//if (state.getBlock() instanceof BlockFiniteFluid_Flow) {
		if (!isStill) {
			// flow viene en Vec3d (x,z). si ambas componentes son ~0 => sin flujo efectivo.
			float fx = (float) flow.x;
			float fz = (float) flow.z;
			float mag = (float) Math.sqrt(fx * fx + fz * fz);
			hasFlow = mag > 1e-4f; // umbral para evitar ruido
			if (hasFlow) angle = (float) Math.atan2(-fx, fz); // misma convención que usabas

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
		Block upBlock = upState != null ? RealisticFiniteFluidFunctions.getBlock(null, null, upState) : Blocks.AIR; //Obtener Block vecino superior, si es null entonces usamos Blocks.AIR.
		boolean renderTop = true; //Asumimos que renderizamos la parte superior.
		int topFluidIndex = FiniteFluidLogic.GeneralPurposeLogic.getFluidIndex(upBlock);
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
		if (renderTop && (sideRequested == null || sideRequested == EnumFacing.UP) 
				&& RealisticFiniteFluidFunctions.getBlock(null, null, upState) != RealisticFiniteFluidFunctions.getBlock(null, null, state)) {
			//System.out.println(sideRequested);
			//System.out.printf("h00=%.2f, h10=%.2f, h11=%.2f, h01=%.2f%n", h000, h100, h110, h010);
			//System.out.printf("Color ARGB = 0x%08X, r=%.2f g=%.2f b=%.2f%n", color, r, g, b);

			//System.out.println("Angulo: "+angle);

			// si NO hay flujo significativo, usa las UVs simples del sprite (still)
			//if (!(state.getBlock() instanceof BlockFiniteFluid_Flow)) {
			if (isStill || !FiniteFluidLogic.dynamicOrStaticTexture) {
				// top quad con UVs sin rotación ni offsets
				UnpackedBakedQuad.Builder topUp = new UnpackedBakedQuad.Builder(DefaultVertexFormats.BLOCK);
				topUp.setQuadOrientation(EnumFacing.UP);
				topUp.setTexture(spriteStill);
				putVertex(topUp, 0f, h000, 0f, r, g, b, alpha, baseU0, baseV0);
				putVertex(topUp, 0f, h010, 1f, r, g, b, alpha, baseU0, baseV1);
				putVertex(topUp, 1f, h110, 1f, r, g, b, alpha, baseU1, baseV1);
				putVertex(topUp, 1f, h100, 0f, r, g, b, alpha, baseU1, baseV0);
				quads.add(topUp.build());

				// bottom cap (optional, keep as small offset to avoid z-fighting)
				UnpackedBakedQuad.Builder topDown = new UnpackedBakedQuad.Builder(DefaultVertexFormats.BLOCK);
				topDown.setQuadOrientation(EnumFacing.DOWN);
				topDown.setTexture(spriteStill);
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
				float stitchFactor = 0.25f;
				if (RealisticFiniteFluidFunctions.getBlock(null, null, state) instanceof BlockLiquid) stitchFactor = 0.25f;
				float f21 = (float) Math.sin(angle) * stitchFactor;
				float f22 = (float) Math.cos(angle) * stitchFactor;

				// compute the 4 UVs using sprite.getInterpolatedU/V (vanilla approach)
				// center 8.0 (middle of 16x16 tile) then offset by (-f22 - f21)*16 etc
				float uA = spriteFlowing.getInterpolatedU((double) (8.0F + (-f22 - f21) * 16.0F));
				float vA = spriteFlowing.getInterpolatedV((double) (8.0F + (-f22 + f21) * 16.0F));

				float uB = spriteFlowing.getInterpolatedU((double) (8.0F + (-f22 + f21) * 16.0F));
				float vB = spriteFlowing.getInterpolatedV((double) (8.0F + (f22 + f21) * 16.0F));

				float uC = spriteFlowing.getInterpolatedU((double) (8.0F + (f22 + f21) * 16.0F));
				float vC = spriteFlowing.getInterpolatedV((double) (8.0F + (f22 - f21) * 16.0F));

				float uD = spriteFlowing.getInterpolatedU((double) (8.0F + (f22 - f21) * 16.0F));
				float vD = spriteFlowing.getInterpolatedV((double) (8.0F + (-f22 - f21) * 16.0F));

				// Construimos los quads con esos UVs (igual que vanilla)
				UnpackedBakedQuad.Builder topUp = new UnpackedBakedQuad.Builder(DefaultVertexFormats.BLOCK);
				topUp.setQuadOrientation(EnumFacing.UP);
				topUp.setTexture(spriteFlowing);

				// Nota: el orden de vértices debe coincidir con el que espera el renderer (igual que tu código original)
				putVertex(topUp, 0f, h000, 0f, r, g, b, alpha, uA, vA);
				putVertex(topUp, 0f, h010, 1f, r, g, b, alpha, uB, vB);
				putVertex(topUp, 1f, h110, 1f, r, g, b, alpha, uC, vC);
				putVertex(topUp, 1f, h100, 0f, r, g, b, alpha, uD, vD);
				quads.add(topUp.build());

				// bottom face (usamos los mismos u/v pero en orden invertido)
				UnpackedBakedQuad.Builder topDown = new UnpackedBakedQuad.Builder(DefaultVertexFormats.BLOCK);
				topDown.setQuadOrientation(EnumFacing.DOWN);
				topDown.setTexture(spriteFlowing);
				putVertex(topDown, 0f, h000 - 0.001f, 0f, r, g, b, alpha, uA, vA);
				putVertex(topDown, 1f, h100 - 0.001f, 0f, r, g, b, alpha, uD, vD);
				putVertex(topDown, 1f, h110 - 0.001f, 1f, r, g, b, alpha, uC, vC);
				putVertex(topDown, 0f, h010 - 0.001f, 1f, r, g, b, alpha, uB, vB);
				quads.add(topDown.build());
			}

			return quads; // Si solo querían UP, devuelve aquí

		}


		//System.out.println("[RFF]");











		// =========================
		// Caras Laterales e Inferior
		// ========================= 
		EnumFacing[] sides = EnumFacing.values();
		//System.out.println("[RFF] sideRequested: "+sideRequested);
		for (EnumFacing face : sides) {
			if (sideRequested != null && face != sideRequested) continue; // si piden una cara específica, las demas las descartamos
			if (face == EnumFacing.UP) continue; // Ya hicimos la cara UP arriba

			//Obtenemos el IBlockState y Block del bloque vecino para la respectiva cara a renderizar
			IBlockState neighborState = neighborStates.get(face); 
			if (neighborState == null) continue;
			Block neighborBlock = RealisticFiniteFluidFunctions.getBlock(null, null, neighborState);
			//if (face == EnumFacing.SOUTH) System.out.println("[RFF] neighborBlock: "+neighborBlock.toString());

			//Suponemos que no vamos a renderizar la respectiva cara solicitada
			boolean shouldRender = false;


			// Si es aire, sí renderiza
			if (neighborBlock == Blocks.AIR) {
				shouldRender = true;
			} else {
				int neighborFluidIndex = FiniteFluidLogic.GeneralPurposeLogic.getFluidIndex(neighborBlock);
				shouldRender = !(neighborFluidIndex == fluidIndex);
				//System.out.println("[RFF] neighborFluidIndex: "+neighborFluidIndex);
				//System.out.println("[RFF] fluidIndex: "+fluidIndex);
			}

			//Si no debemos renderizar, vamos con la siguiente cara.
			if (!shouldRender) continue;

			//if (state.getBlock() instanceof BlockFiniteFluid_Flow) {
			//LEGACY CODE TO STITCH TEXTURES, because vanilla water/lava needed it
			/*if (!isStill) {
		    		u1 = midU + (u1 - midU) * 0.5f;
		    		v1 = midV + (v1 - midV) * 0.5f;
		    		u0 = midU - (midU - u0) * 0.5f;
		    		v0 = midV - (midV - v0) * 0.5f;
    	    	}*/

			UnpackedBakedQuad.Builder sideBuilder = new UnpackedBakedQuad.Builder(DefaultVertexFormats.BLOCK);
			sideBuilder.setQuadOrientation(face);
			if (isStill || !FiniteFluidLogic.dynamicOrStaticTexture) {
				sideBuilder.setTexture(spriteStill);
			} else {
				sideBuilder.setTexture(spriteFlowing);
			}
			switch (face) {
			case NORTH:
				putVertex(sideBuilder, 1f, 0f, 0f, r, g, b, alpha, u1, v1);
				putVertex(sideBuilder, 0f, 0f, 0f, r, g, b, alpha, u0, v1);
				putVertex(sideBuilder, 0f, h000, 0f, r, g, b, alpha, u0, v0);
				putVertex(sideBuilder, 1f, h100, 0f, r, g, b, alpha, u1, v0);
				break;
			case SOUTH:
				putVertex(sideBuilder, 0f, 0f, 1f, r, g, b, alpha, u0, v1);
				putVertex(sideBuilder, 1f, 0f, 1f, r, g, b, alpha, u1, v1);
				putVertex(sideBuilder, 1f, h110, 1f, r, g, b, alpha, u1, v0);
				putVertex(sideBuilder, 0f, h010, 1f, r, g, b, alpha, u0, v0);
				break;
			case WEST:
				putVertex(sideBuilder, 0f, 0f, 0f, r, g, b, alpha, u0, v1);
				putVertex(sideBuilder, 0f, 0f, 1f, r, g, b, alpha, u1, v1);
				putVertex(sideBuilder, 0f, h010, 1f, r, g, b, alpha, u1, v0);
				putVertex(sideBuilder, 0f, h000, 0f, r, g, b, alpha, u0, v0);
				break;
			case EAST:
				putVertex(sideBuilder, 1f, 0f, 1f, r, g, b, alpha, u0, v1);
				putVertex(sideBuilder, 1f, 0f, 0f, r, g, b, alpha, u1, v1);
				putVertex(sideBuilder, 1f, h100, 0f, r, g, b, alpha, u1, v0);
				putVertex(sideBuilder, 1f, h110, 1f, r, g, b, alpha, u0, v0);
				break;
			case DOWN:
				putVertex(sideBuilder, 0f, 0f, 0f, r, g, b, alpha, u0, v0);
				putVertex(sideBuilder, 1f, 0f, 0f, r, g, b, alpha, u1, v0);
				putVertex(sideBuilder, 1f, 0f, 1f, r, g, b, alpha, u1, v1);
				putVertex(sideBuilder, 0f, 0f, 1f, r, g, b, alpha, u0, v1);
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





	public List<BakedQuad> renderItemFluid(VertexFormat vertexFormat, int color, TextureAtlasSprite spriteStill) {

		List<BakedQuad> quads = new ArrayList<>();

		float r = ((color >> 16) & 255) / 255.0f;
		float g = ((color >> 8) & 255) / 255.0f;
		float b = (color & 255) / 255.0f;
		float alpha = 1.0f;

		float u0 = spriteStill.getMinU();
		float v0 = spriteStill.getMinV();
		float u1 = spriteStill.getMaxU();
		float v1 = spriteStill.getMaxV();

		UnpackedBakedQuad.Builder builder =
				new UnpackedBakedQuad.Builder(vertexFormat);
		//System.out.println(builder.getVertexFormat().);
		builder.setQuadOrientation(EnumFacing.UP);
		builder.setTexture(spriteStill);
		builder.setQuadTint(0);


		// CUADRO COMPLETO 0–1
		putItemVertex(builder, 0f, 0f, 0f, r, g, b, 1, u1, v0);
		putItemVertex(builder, 1f, 0f, 0f, r, g, b, 1, u0, v0);
		putItemVertex(builder, 1f, 1f, 0f, r, g, b, 1, u0, v1);
		putItemVertex(builder, 0f, 1f, 0f, r, g, b, 1, u1, v1);

		quads.add(builder.build());

		return quads;
	}

	private void putItemVertex(UnpackedBakedQuad.Builder builder,
			float x, float y, float z,
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
	
	
	/*


	public List<BakedQuad> renderItemFluid(int color,
			TextureAtlasSprite spriteStill,
			VertexFormat format,
			Optional<TRSRTransformation> transformation) {

		List<BakedQuad> quads = new ArrayList<>();

		float[] x = {0f, 0f, 1f, 1f};
		float[] y = {0f, 1f, 1f, 0f};
		float[] z = {0f, 0f, 0f, 0f};

		UnpackedBakedQuad.Builder builder = new UnpackedBakedQuad.Builder(format);

		builder.setQuadOrientation(EnumFacing.SOUTH);
		builder.setTexture(spriteStill);
		builder.setQuadTint(0);

		IVertexConsumer consumer =
				transformation.isPresent() && !transformation.get().isIdentity()
				? new TRSRTransformer(builder, transformation.get())
						: builder;

				float r = ((color >> 16) & 255) / 255f;
				float g = ((color >> 8) & 255) / 255f;
				float b = (color & 255) / 255f;
				float a = ((color >> 24) & 255) / 255f;

				for (int i = 0; i < 4; i++) {

					float u = spriteStill.getInterpolatedU(x[i] * 16f);
					float v = spriteStill.getInterpolatedV(y[i] * 16f);

					putVertex(consumer, format,
							x[i], y[i], z[i],
							r, g, b, a,
							u, v);
				}

				quads.add(builder.build());
				return quads;
	}

	private void putVertex(IVertexConsumer consumer,
			VertexFormat format,
			float x, float y, float z,
			float r, float g, float b, float a,
			float u, float v) {

		for (int e = 0; e < format.getElementCount(); e++) {

			switch (format.getElement(e).getUsage()) {

			case POSITION:
				consumer.put(e, x, y, z, 1f);
				break;

			case COLOR:
				consumer.put(e, r, g, b, a);
				break;

			case NORMAL:
				consumer.put(e, 0f, 0f, 1f, 0f);
				break;

			case UV:
				if (format.getElement(e).getIndex() == 0)
					consumer.put(e, u, v, 0f, 1f);
				else
					consumer.put(e);
				break;

			default:
				consumer.put(e);
				break;
			}
		}
	}

	 */



}
