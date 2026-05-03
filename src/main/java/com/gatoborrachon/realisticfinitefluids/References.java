package com.gatoborrachon.realisticfinitefluids;

import java.util.Map;

import com.gatoborrachon.realisticfinitefluids.blocks.properties.UnlistedPropertyBoolean;
import com.gatoborrachon.realisticfinitefluids.blocks.properties.UnlistedPropertyColor;
import com.gatoborrachon.realisticfinitefluids.blocks.properties.UnlistedPropertyFlowDirection;
import com.gatoborrachon.realisticfinitefluids.blocks.properties.UnlistedPropertyNeighborStates;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.common.property.PropertyFloat;

public class References {
	
    public static final String MODID = "realisticfinitefluids";
    public static final String NAME = "Realistic Finite Fluids";
    public static final String VERSION = "2.1.4";
	public static final String CLIENT_PROXY_CLASS = "com.gatoborrachon.realisticfinitefluids.proxy.ClientProxy";
	public static final String COMMON_PROXY_CLASS = "com.gatoborrachon.realisticfinitefluids.proxy.CommonProxy";
	
	public static final boolean onDev = false;
	public static final boolean debugBlockFluidClassic = false;
	//public static Block DEBUG_BLOCK = Block.REGISTRY.getObject(new ResourceLocation("diamond_block")); //Blocks.DIAMOND_BLOCK
	public static final Block DEBUG_BLOCK;
    
	public static final int FIXER_VERSION = 2;
	
    //public static final PropertyInteger LEVEL = PropertyInteger.create("level", BlockFiniteFluid.MINIMUM_LEVEL, BlockFiniteFluid.MAXIMUM_REAL_LEVEL); //MEJOR NO TOCAMOS ESTO, SE VA ALV EL REGISTRO DE BLOQUES
	
	static {
		DEBUG_BLOCK = new Block(Material.IRON) {}.setRegistryName("debug_block").setTranslationKey("debug_block");
	}
	
	
	
	


	
	
	
	
	
	
	
    public static final double EPS = 1.0 / 1024.0; // pequeña histéresis para evitar parpadeos en calculos de renderizado
    /**
     * Minimum literal level for the finite fluid blocks (0). The minimum conceptual level is 1.
     */
    public static final int MINIMUM_LEVEL = 0;
    public static final int MINIMUM_CONCEPTUAL_LEVEL = MINIMUM_LEVEL+1;
    
    /**
     * Maximum literal level for the finite fluid blocks (7). The maximum conceptual level is 8.
     */
    public static final int MAXIMUM_LEVEL = 7; //ESTE ES EL MAESTRO ALV
    private static final int MAXIMUM_REAL_LEVEL = 15; //ESTE SOLO SIRVE COMO TOPE VERDADER
    public static final int MAXIMUM_CONCEPTUAL_LEVEL = MAXIMUM_LEVEL+1;

    /**
     * Quartiles
     */
    public static final int Q1_LOW = MINIMUM_LEVEL + (MAXIMUM_LEVEL - MINIMUM_LEVEL) / 4;
    public static final int Q1_HIGH = Q1_LOW + 1;
    
    public static final int Q2_LOW = MINIMUM_LEVEL + (MAXIMUM_LEVEL - MINIMUM_LEVEL) / 2;
    public static final int Q2_HIGH = Q2_LOW +1;

    public static final int Q3_LOW = MINIMUM_LEVEL + 3 * (MAXIMUM_LEVEL - MINIMUM_LEVEL) / 4;
    public static final int Q3_HIGH = Q3_LOW + 1;
    
    
    
    
    
    
    
    
    
    public static final PropertyInteger LEVEL = PropertyInteger.create("level", MINIMUM_LEVEL, MAXIMUM_REAL_LEVEL); //MEJOR NO TOCAMOS ESTO, SE VA ALV EL REGISTRO DE BLOQUES
    public static final IUnlistedProperty<Map<EnumFacing, IBlockState>> NEIGHBOR_STATES = new UnlistedPropertyNeighborStates();
    //public static final PropertyFloat[] LEVEL_CORNERS = new PropertyFloat[4];
    
    public static final IUnlistedProperty<Float>[] LEVEL_CORNERS = new IUnlistedProperty[] {
    new PropertyFloat("level_nw"),  // h00
    new PropertyFloat("level_ne"),  // h10
    new PropertyFloat("level_sw"),  // h01
    new PropertyFloat("level_se")   // h11
    };
    
	public static final IUnlistedProperty<Integer> FLUID_COLOR = new UnlistedPropertyColor("fluid_color");
	public static final IUnlistedProperty<Vec3d> FLOW_DIRECTION = new UnlistedPropertyFlowDirection("flow_direction");

	public static final UnlistedPropertyBoolean IS_STILL = new UnlistedPropertyBoolean("is_still");
	public static final UnlistedPropertyBoolean IS_GASEOUS = new UnlistedPropertyBoolean("is_gaseous");
    
    
}
