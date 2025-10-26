package com.gatoborrachon.realisticfinitefluids.blocks;

import java.util.EnumMap;
import java.util.Map;
import java.util.Random;

import com.gatoborrachon.realisticfinitefluids.blocks.properties.UnlistedPropertyColor;
import com.gatoborrachon.realisticfinitefluids.blocks.properties.UnlistedPropertyFlowDirection;
import com.gatoborrachon.realisticfinitefluids.blocks.properties.UnlistedPropertyHeights;
import com.gatoborrachon.realisticfinitefluids.blocks.properties.UnlistedPropertyNeighborStates;
import com.gatoborrachon.realisticfinitefluids.init.ModConfig;
import com.gatoborrachon.realisticfinitefluids.logic.FiniteFluidLogic;

import net.minecraft.block.Block;
import net.minecraft.block.BlockSnow;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockFiniteFluid extends Block implements IFluidBlock {

	private Fluid fluid;
	private Material fluidMaterial;
    private static final double EPS = 1.0 / 1024.0; // pequeña histéresis para evitar parpadeos en calculos de renderizado

    /**
     * The main class for Finite Fluids blocks, has all the base elements that will be required, other classes only add the tick functions and some interactions
     */
	public BlockFiniteFluid(String name, Fluid fluid, Material material) {
		super(material);
        this.fluid = fluid;
    	this.fluidMaterial = material;
    	
		setUnlocalizedName(name);
		setRegistryName(name);
        this.setDefaultState(this.blockState.getBaseState().withProperty(LEVEL, Integer.valueOf(0)));
	}
	
    // =========================
    // Properties Handling
    // ========================= 
    public static final PropertyInteger LEVEL = PropertyInteger.create("level", 0, 15); //MEJOR NO TOCAMOS ESTO, SE VA ALV EL REGISTRO DE BLOQUES
    public static final IUnlistedProperty<Map<EnumFacing, IBlockState>> NEIGHBOR_STATES = new UnlistedPropertyNeighborStates();
    public static final IUnlistedProperty<Float> HEIGHT_NW = new UnlistedPropertyHeights("height_nw");  // h00
    public static final IUnlistedProperty<Float> HEIGHT_NE = new UnlistedPropertyHeights("height_ne");  // h10
    public static final IUnlistedProperty<Float> HEIGHT_SW = new UnlistedPropertyHeights("height_sw");  // h01
    public static final IUnlistedProperty<Float> HEIGHT_SE = new UnlistedPropertyHeights("height_se");	// h11
    public static final IUnlistedProperty<Integer> FLUID_COLOR = new UnlistedPropertyColor("fluid_color");
    public static final IUnlistedProperty<Vec3d> FLOW_DIRECTION = new UnlistedPropertyFlowDirection("flow_direction");

    
    @Override
    protected BlockStateContainer createBlockState() {
        return new ExtendedBlockState(this,
        	new IProperty[] { LEVEL }, //Listed Properties
            new IUnlistedProperty<?>[] { //Unlisted Properties
        		NEIGHBOR_STATES,
                HEIGHT_NW,
                HEIGHT_NE,
                HEIGHT_SW,
                HEIGHT_SE,
                FLUID_COLOR,
                FLOW_DIRECTION
            }
        );
    }
    
    @Override
    public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
        if (state instanceof IExtendedBlockState) {
            IExtendedBlockState extendedState = (IExtendedBlockState) state;

            float h00 = FiniteFluidLogic.GeneralPurposeLogic.getHeight(world, pos, -1, -1);
            float h10 = FiniteFluidLogic.GeneralPurposeLogic.getHeight(world, pos, 1, -1);
            float h01 = FiniteFluidLogic.GeneralPurposeLogic.getHeight(world, pos, -1, 1);
            float h11 = FiniteFluidLogic.GeneralPurposeLogic.getHeight(world, pos, 1, 1);
            
            Map<EnumFacing, IBlockState> neighborStates = new EnumMap<>(EnumFacing.class);
            for (EnumFacing face : EnumFacing.values()) {
                BlockPos neighborPos = pos.offset(face);
                IBlockState neighborState = world.getBlockState(neighborPos);
                neighborStates.put(face, neighborState);
            }
            
            int color = Minecraft.getMinecraft().getBlockColors().colorMultiplier(state, world, pos, 0);
            
            Vec3d flowDirection = (state.getBlock() instanceof BlockNewWater_Flow) ? calculateFlowVector(world, pos) : null;
            
            extendedState = extendedState
               	.withProperty(NEIGHBOR_STATES, neighborStates)
                .withProperty(HEIGHT_NW, h00)
                .withProperty(HEIGHT_NE, h10)
                .withProperty(HEIGHT_SW, h01)
                .withProperty(HEIGHT_SE, h11)
                .withProperty(FLUID_COLOR, color)
                .withProperty(FLOW_DIRECTION, flowDirection);
            return extendedState; 
        }
        return state;
    }
    
    
    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(LEVEL, meta);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(LEVEL);
    }
    

    
    // =========================
    // Special RealisticFiniteFluid Functions
    // ========================= 
    
    //Public functions to interact with Fluids
    public boolean allowTransfer()
    {
        return true;
    }

    /**
     * Sets the specified LEVEL from the passed stated to the block on the current pos
     * @param world the current World
     * @param pos the Position of the block
     * @param state the IBlockState with the right LEVEL value
     */
    public static void setVolume(World world, BlockPos pos, IBlockState state)
    {
    	world.setBlockState(pos, state);
    }

    /**
     * Gets the LEVEL value the block on the current pos
     * @param world the current World
     * @param pos the Position of the block
     * @return the LEVEL value in 0 to 15 scale.
     */
    public static int getVolume(World world, BlockPos pos)
    {
        return world.getBlockState(pos).getValue(LEVEL);
    }

    /**
     * Absorbs all the fluid contained on the current position and eliminates the block
     * @param world the current World
     * @param posthe Position of the block
     * @return all the LEVELs contained on this block
     */
    public static int drain(World world, BlockPos pos)
    {
    	world.setBlockToAir(pos);
        return world.getBlockState(pos).getValue(LEVEL);
    }
    
    
    
    
    
    /**
     * Gets the LEVEL value the block on the current pos. IN A SCALE 1-16
     * @param world the current World
     * @param pos the Position of the block
     * @return the LEVEL value IN A SCALE 1-16
     */
    public static int getConceptualVolume(World world, BlockPos pos)
    {
        return world.getBlockState(pos).getValue(LEVEL)+1;
    }

    /**
     * Absorbs all the fluid contained on the current position and eliminates the block  IN A SCALE 1-16
     * @param world the current World
     * @param posthe Position of the block
     * @return all the LEVELs (IN A SCALE 1-16) contained on this block
     */
    public static int conceptualDrain(World world, BlockPos pos)
    {
    	world.setBlockToAir(pos);
        return world.getBlockState(pos).getValue(LEVEL)+1;
    }
    
    /**
     * Sets the specified LEVEL from the passed stated to the block on the current pos IN A SCALE 1-16
     * @param world the current World
     * @param pos the Position of the block
     * @param state the IBlockState with the right LEVEL value
     */
    public static void setConceptualVolume(World world, BlockPos pos, IBlockState state, int LEVEL)
    {
    	world.setBlockState(pos, state.withProperty(BlockFiniteFluid.LEVEL, LEVEL));
    }
    
    
    
    
    
    /**
     * Special function to interact specifically with the Overlay of water depending on the eye height of the player. 
     * Avoids overriding isEntityInsideMaterial and making any fluid below the eye of the player not of Material.WATER
     */
	public Boolean isEntityInsideMaterialForOverlay(IBlockAccess world, BlockPos pos, IBlockState state, 
			Entity entity, double eyeY, Material material) {
	    if (!(state.getBlock() instanceof BlockFiniteFluid)) return null; // null = no se mete en overlay
	    if (material != Material.WATER) return null; // solo aplica para agua finita

	    // Altura del agua según LEVEL (0-15)
	    int level = state.getValue(LEVEL) + 1;
	    float fluidHeight = level / 16.0F;
	    boolean inside = (pos.getY() + fluidHeight) > eyeY;

	    return inside;
	}
	
	/**
	 * Try to freeze the current fluid block.
	 * @param world the current World
	 * @param pos the current BlockPos
	 * @param state the current IBlockState
	 * @param rand a random value
	 * @return if this block was turned into ice
	 */
	public static boolean tryFreezeWater(World world, BlockPos pos, IBlockState state, Random rand) {
		Material blockUpMaterial = world.getBlockState(pos.up()).getMaterial();
	    // congela solo si es agua
		if (!(world.getBlockState(pos).getMaterial() == Material.WATER)) {
	        return false;
	    }

	    // Solo aplica si puede ver el cielo
	    /*if (!world.canBlockSeeSky(pos)) {
	        return false;
	    }*/
		
		//EN VEZ DE INTS, USO FLOATS AHORA
		/*if (blockUpMaterial == Material.SNOW) {
			world.setBlockState(pos.up(), Blocks.AIR.getDefaultState(), 2);
		} else {
		    // 25% chance por tick, para evitar congelación masiva instantánea
			if (!world.isRaining()) {
			    if (rand.nextInt(FiniteFluidLogic.evaporationChance/5) != 0) {//rand.nextInt(1) != 0) {
			        return false;
			    }
			} else {
			    if (rand.nextInt(FiniteFluidLogic.evaporationChance/25) != 0) {//rand.nextInt(1) != 0) {
			        return false;
			    }
			}
			
		}*/
		
		if (blockUpMaterial == Material.SNOW) {
		    world.setBlockState(pos.up(), Blocks.AIR.getDefaultState(), 2);
		} else {
		    float chance = 1f; // default 100% chance
		    if (!world.isRaining()) {
		        chance = 5f / (float) FiniteFluidLogic.evaporationChance; // 25% de probabilidad por tick
		    } else {
		        chance = 25f / (float) FiniteFluidLogic.evaporationChance; // ajustado si llueve
		    }

		    // nextFloat devuelve valor entre 0 y 1
		    if (rand.nextFloat() >= chance) {
		        return false;
		    }
		}
		

	    if (world.getBlockState(pos.up()).getMaterial() != Material.AIR) {
	    	return false;
	    }
	    
	    //Solo si el bloque no tiene agua encima
	    if (world.getBlockState(pos.up()) instanceof BlockFiniteFluid) {
	    	return false;
	    }

	    //Que el bioma sea de nieve
	    if (world.getBiome(pos).getTemperature(pos) >= 0.15F) { 
	    	return false; 
	    }

	    // Altura válida
	    if (pos.getY() < 0 || pos.getY() >= 256) {
	        return false;
	    }

	    // Checar que no haya calor cerca (igual que vanilla)
	    if (isNearHotBlock(world, pos)) {
	        return false;
	    }

	    if (state.getBlock() instanceof BlockFiniteFluid) {
	        int level = state.getValue(BlockFiniteFluid.LEVEL);

	        if (level == 15) {
	            // Nivel máximo, congelar en hielo
	            world.setBlockState(pos, Blocks.ICE.getDefaultState(), 2);
	            return true;
	        } else if (level == 0) {
	            // Nivel 0, desaparece o capa mínima
	            world.setBlockState(pos, Blocks.SNOW_LAYER.getDefaultState()
	                .withProperty(BlockSnow.LAYERS, 1), 2);
	            return true;
	        } else {
	            // Mapeo LEVEL 1-14 a capas 1-7
	            int snowLayers = (level + 1) / 2; // Nivel par e impar van al mismo layer
	            world.setBlockState(pos, Blocks.SNOW_LAYER.getDefaultState()
	                .withProperty(BlockSnow.LAYERS, snowLayers), 2);
	            return true;
	        }
	    }

	    return false;
	}

	/**
	 * Whether this block is near a heat source or not
	 * Requires more compat
	 * 
	 * @param world the current World
	 * @param pos the position of the block to affect
	 * @return if it is near a heat source
	 */
	private static boolean isNearHotBlock(World world, BlockPos pos) {
	    for (EnumFacing facing : EnumFacing.values()) {
	        BlockPos checkPos = pos.offset(facing);
	        Material mat = world.getBlockState(checkPos).getMaterial();

	        if (mat == Material.FIRE || mat == Material.LAVA) {
	            return true;
	        }

	        //TODO --> Hacerlo compatible con mas bloques, usar algun mapa de bloques calientes dado por: TAN, PrimalCore, Heat and Climate, etc.
	        Block block = world.getBlockState(checkPos).getBlock();
	        if (block == Blocks.LIT_PUMPKIN || block == Blocks.TORCH) {
	            return true;
	        }
	    }
	    return false;
	}
	
	
	/**
	 * Whether this block should evaporate
	 * Only matters for still fluids
	 * @param world the current World
	 * @param pos the current BlockPos
	 * @return if this finite fluid block can evaporate
	 */
    public boolean shouldEvap(World world, BlockPos pos)
    {
        if (!FiniteFluidLogic.enableEvaporation)
        {
            return false;
        }
        else if (world.getBlockState(pos).getValue(BlockFiniteFluid.LEVEL) > 0)
        {
            return false;
        }
        else if (world.getBiome(pos).getTemperature(pos) < 0.15F) 
        { 
    	    return false; 
    	}
        else if (world.isRainingAt(pos))
        {
        	return false;
        }
        else
        {
            Block evaporableBlock = world.getBlockState(pos.down()).getBlock();
            return evaporableBlock == Blocks.GRASS ? true : (evaporableBlock == Blocks.DIRT ? true : (evaporableBlock == Blocks.SAND ? true : (evaporableBlock == Blocks.GRAVEL ? true : world.getTopSolidOrLiquidBlock(pos) == pos)));
        }
    }
    
	/**
	 * Whether this block interacts with other FiniteFluids
	 * @param world the current World
	 * @param pos1 current Block Position
	 * @param pos2 second BlockPosition
	 * @return if this can interact with the second Position
	 */
    public boolean interactWithLiquid(World world, BlockPos pos1, BlockPos pos2)
    {
        return false;
    }
	

	/**
	 * Used inside logic of fluid movement.
	 * Only true for water, false for lava
	 * @return
	 */
    public boolean shouldSearchOutward()
    {
        return true;
    }

    public boolean calcAvg(World world, BlockPos posFrom, BlockPos posTo) //shouldEqualize queda mejor
    {
        int levelFrom = world.getBlockState(posFrom).getValue(LEVEL);
        int levelTo = world.getBlockState(posTo).getValue(LEVEL);

        if (levelFrom - 1 > levelTo)
        {
            return true;
        }
        else
        {
            float var10 = FiniteFluidLogic.GeneralPurposeLogic.calculateNeighborWaterLevel(world, posFrom, posTo);            
            float var11 = FiniteFluidLogic.GeneralPurposeLogic.calculateNeighborWaterLevel(world, posTo, posFrom);
            return (var10 + 1.0F != (float)levelFrom || var11 + 1.0F != (float)levelTo) && var10 - 0.8F > var11;
        }
    }
    
    /**
     * Gets the actual flow vector based on adyacent finite fluid blocks
     * @param world The actual world
     * @param pos The current position of the block to get its flow vector
     * @return The Vec3d Flow Vector
     */
	public Vec3d calculateFlowVector(IBlockAccess world, BlockPos pos) {
    	Vec3d flow = new Vec3d(0,0,0);
        for(EnumFacing dir : EnumFacing.Plane.HORIZONTAL) {
            BlockPos neighbor = pos.offset(dir);
            if (!(world.getBlockState(neighbor).getBlock() instanceof BlockFiniteFluid)) continue;
            
            int levelNeighbor = FiniteFluidLogic.GeneralPurposeLogic.getFluidLevel(world, neighbor);
            int levelCurrent = FiniteFluidLogic.GeneralPurposeLogic.getFluidLevel(world, pos);
            int diff = levelNeighbor - levelCurrent;
            
            flow = flow.addVector(
                dir.getFrontOffsetX() * diff, 
                0, 
                dir.getFrontOffsetZ() * diff
            );
            
    		if (flow.lengthVector() > 0) 
    			flow = flow.normalize();
        }
		return flow;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
    
    // =========================
    // Vanilla Overriddes
    // =========================    
    @Override
    @SideOnly(Side.CLIENT)
	public Vec3d getFogColor(World world, BlockPos pos, IBlockState state, Entity entity, Vec3d originalColor,
			float partialTicks) {
        int level = state.getValue(LEVEL)+1;
        Vec3d cam = ActiveRenderInfo.projectViewFromEntity(entity, partialTicks);
        double surfaceY = pos.getY() + (level / 16.0D);

        // Solo pintamos niebla si la cámara está DENTRO del fluido
        return (cam.y < surfaceY - EPS) ? super.getFogColor(world, pos, state, entity, originalColor, partialTicks) : originalColor;
	}
	


    @Override
    @SideOnly(Side.CLIENT)
    public IBlockState getStateAtViewpoint(IBlockState state, IBlockAccess world, BlockPos pos, Vec3d viewpoint) {
        // Si la cámara está por debajo de la “superficie” (LEVEL/16), seguimos “dentro” del fluido.
        int level = state.getValue(LEVEL) + 1;     // 1..16
        double surfaceY = pos.getY() + (level / 16.0D);

        if (viewpoint.y < surfaceY - EPS) {
            return state; // seguimos “dentro” del fluido
        }
        return Blocks.AIR.getDefaultState(); // por encima: aire (vanilla hará el FOV normal)
    }
	

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        int level = state.getValue(LEVEL)+1; // PropertyInteger LEVEL conceptual = 1-16
        float height = level / 16.0F;
        return new AxisAlignedBB(0.0F, 0.0F, 0.0F, 1.0F, height, 1.0F);
    }
  
    
    @Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
    	return EnumBlockRenderType.MODEL;// FiniteFluidLogic.id2;
	}
    
    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getBlockLayer() {
        //return BlockRenderLayer.TRANSLUCENT;
        return this.blockMaterial == Material.WATER ? BlockRenderLayer.TRANSLUCENT : BlockRenderLayer.SOLID;
    }
    
    @Override
    public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
        return layer == BlockRenderLayer.TRANSLUCENT;
    }
    
    //NO SE PARA QUE ERA ESTO
	@Override
	public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
	    int var5 = super.getLightValue(state, world, pos);  // llamada a super para evitar recursiÃ³n
	    int var6 = super.getLightValue(state, world, new BlockPos(pos.getX(), pos.getY() + 1, pos.getZ()));
	    return var5 <= var6 ? var6 : var5;
	}
	
    @SideOnly(Side.CLIENT)
    @Override
    public int getPackedLightmapCoords(IBlockState state, IBlockAccess source, BlockPos pos)
    {
        int i = source.getCombinedLight(pos, 0);
        int j = source.getCombinedLight(pos.up(), 0);
        int k = i & 255;
        int l = j & 255;
        int i1 = i >> 16 & 255;
        int j1 = j >> 16 & 255;
        return (k > l ? k : l) | (i1 > j1 ? i1 : j1) << 16;
    }
	
	@Override
	public int getLightOpacity(IBlockState state, IBlockAccess world, BlockPos pos) {
	    return ModConfig.waterLightOpacity; //Vanilla uses 3, i use 1
	}
	
	
	@SideOnly(Side.CLIENT)
	@Override
	public void randomDisplayTick(IBlockState stateIn, World worldIn, BlockPos pos, Random rand) {
	    double d0 = (double)pos.getX();
	    double d1 = (double)pos.getY();
	    double d2 = (double)pos.getZ();

	    if (this.fluidMaterial == Material.WATER) {
	        boolean isFlowing = stateIn.getBlock() instanceof BlockNewWater_Flow; // tu clase para agua en movimiento
	        boolean isStill = stateIn.getBlock() instanceof BlockNewWater_Still || stateIn.getBlock() instanceof BlockNewInfiniteSource;

	        if (isFlowing) {
	            // Agua en movimiento --> sonido ambiente ocasional
	            if (rand.nextInt(64) == 0) {
	                worldIn.playSound(d0 + 0.5D, d1 + 0.5D, d2 + 0.5D,
	                    SoundEvents.BLOCK_WATER_AMBIENT,
	                    SoundCategory.BLOCKS,
	                    rand.nextFloat() * 0.25F + 0.75F,
	                    rand.nextFloat() + 0.5F,
	                    false);
	            }
	        } else if (isStill) {
	            // Agua quieta --> partículas de suspensión ocasionales
	            if (rand.nextInt(10) == 0) {
	                worldIn.spawnParticle(EnumParticleTypes.SUSPENDED,
	                    d0 + (double)rand.nextFloat(),
	                    d1 + (double)rand.nextFloat(),
	                    d2 + (double)rand.nextFloat(),
	                    0.0D, 0.0D, 0.0D);
	            }
	        }
	    }

	    if (this.fluidMaterial == Material.LAVA
	            && worldIn.getBlockState(pos.up()).getMaterial() == Material.AIR
	            && !worldIn.getBlockState(pos.up()).isOpaqueCube()) {
	        if (rand.nextInt(100) == 0) {
	            double d8 = d0 + (double)rand.nextFloat();
	            double d4 = d1 + stateIn.getBoundingBox(worldIn, pos).maxY;
	            double d6 = d2 + (double)rand.nextFloat();
	            worldIn.spawnParticle(EnumParticleTypes.LAVA, d8, d4, d6, 0.0D, 0.0D, 0.0D);
	            worldIn.playSound(d8, d4, d6,
	                SoundEvents.BLOCK_LAVA_POP,
	                SoundCategory.BLOCKS,
	                0.2F + rand.nextFloat() * 0.2F,
	                0.9F + rand.nextFloat() * 0.15F,
	                false);
	        }

	        if (rand.nextInt(200) == 0) {
	            worldIn.playSound(d0, d1, d2,
	                SoundEvents.BLOCK_LAVA_AMBIENT,
	                SoundCategory.BLOCKS,
	                0.2F + rand.nextFloat() * 0.2F,
	                0.9F + rand.nextFloat() * 0.15F,
	                false);
	        }
	    }

	    if (rand.nextInt(10) == 0 && worldIn.getBlockState(pos.down()).isTopSolid()) {
	        Material material = worldIn.getBlockState(pos.down(2)).getMaterial();

	        if (!material.blocksMovement() && !material.isLiquid()) {
	            double d3 = d0 + (double)rand.nextFloat();
	            double d5 = d1 - 1.05D;
	            double d7 = d2 + (double)rand.nextFloat();

	            if (this.fluidMaterial == Material.WATER) {
	                worldIn.spawnParticle(EnumParticleTypes.DRIP_WATER, d3, d5, d7, 0.0D, 0.0D, 0.0D);
	            } else {
	                worldIn.spawnParticle(EnumParticleTypes.DRIP_LAVA, d3, d5, d7, 0.0D, 0.0D, 0.0D);
	            }
	        }
	    }
	}
	

    @Override
	public int tickRate(World worldIn) {

        if (this.fluidMaterial == Material.WATER)
    	{
            return FiniteFluidLogic.waterTick;
        }
        else if (this.fluidMaterial == Material.LAVA)
        {
            return worldIn.provider.isNether() ? FiniteFluidLogic.lavaTick/3 : FiniteFluidLogic.lavaTick;
        }
        else
        {
            return super.tickRate(worldIn);
        }
	}



	@Override
	public boolean isReplaceable(IBlockAccess worldIn, BlockPos pos) {
		return true; // worldIn.getBlockState(pos).getMaterial() == Material.WATER;
	}
	
	
    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
        return face == EnumFacing.UP ? BlockFaceShape.SOLID : BlockFaceShape.UNDEFINED;
    }
    

    @Override
	public int quantityDropped(IBlockState state, int fortune, Random random) {
        return 0;
	}

	/**
     * Returns whether this block is collideable based on the arguments passed in Args: blockMetaData, unknownFlag
     */
    @Override
	public boolean canCollideCheck(IBlockState state, boolean hitIfLiquid) {
        return hitIfLiquid;
	}


    @Override
	public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        return null;
	}
    
    @Override
	public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
    	worldIn.scheduleUpdate(pos, this, this.tickRate(worldIn));
    }

    
    
    /**
     * If this block doesn't render as an ordinary block it will return False (examples: signs, buttons, stairs, etc)
     */
    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }
    
    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }
    
    @Override
	public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
    	worldIn.scheduleUpdate(pos, this, this.tickRate(worldIn));
	}
    
    /**
     * Used to prevent updates on chunk generation
     */
    @Override
    public boolean requiresUpdates()
    {
        return false;
    }


	
	
	
	
	
	
	
	
	
	
	
    
    
    
	//FORGE FLUID FUNCTIONS
 // IMPORTANTE: ajusta el prefijo de las llamadas a tus helpers si no están en FiniteFluidLogic.FluidWorldInteraction

    @Override
    public Fluid getFluid() {
        return this.fluid;
    }

    /** Convierte mB -> niveles conceptuales (0..16). 0 significa "no alcanza a colocar nada". */
    private static int mbToConceptual(int mb) {
        if (mb <= 0) return 0;
        int c = (int) Math.floor(mb * 16.0 / 1000.0);
        return Math.max(0, Math.min(16, c));
    }

    /** Convierte niveles conceptuales (0..16) -> mB aproximados representados por esos niveles */
    private static int conceptualToMB(int conceptual) {
        if (conceptual <= 0) return 0;
        return (int) Math.floor(conceptual * 1000.0 / 16.0);
    }

    /**
     * place:
     * - Devuelve la cantidad de mB *usados* del FluidStack para conseguir el nivel colocado.
     * - Si doPlace==false se simula (no modifica mundo).
     * - Si ya hay un BlockFiniteFluid del mismo tipo en pos, sumamos niveles hasta 16.
     */
    @Override
    public int place(World world, BlockPos pos, FluidStack fluidStack, boolean doPlace) {
        if (fluidStack == null || !fluidStack.getFluid().equals(this.fluid)) return 0;

        int amount = fluidStack.amount;
        int addConceptual = mbToConceptual(amount); // 0..16

        if (addConceptual <= 0) return 0; // no alcanza a crear/elevar nivel

        //IBlockState currentState = world.getBlockState(pos);
        int currentConceptual = BlockFiniteFluid.getConceptualVolume(world, pos); // 0..16
        // Si hay otro tipo de bloque fluyente distinto, preferimos colocarlo solo si está vacío/puede reemplazar: dejamos esa decisión al llamador.
        // Sumamos niveles (cap 16)
        int newTotalConceptual = Math.min(16, currentConceptual + addConceptual);
        int usedConceptual = newTotalConceptual - currentConceptual;
        if (usedConceptual <= 0) return 0;

        int usedMB = conceptualToMB(usedConceptual);

        if (doPlace) {
            if (newTotalConceptual <= 0) {
                world.setBlockToAir(pos);
            } else {
                // Guardamos LEVEL como propiedad 0..15 (conceptual-1)
                int levelProp = newTotalConceptual - 1;
                world.setBlockState(pos, this.getDefaultState().withProperty(BlockFiniteFluid.LEVEL, levelProp));
            }
            // Notificar vecinos si lo consideras necesario:
            world.neighborChanged(pos, this, pos);
            FiniteFluidLogic.FluidWorldInteraction.activateOcean(world, pos);
        }

        return usedMB; // cantidad de mB *usada*
    }

    /**
     * drain:
     * - Intentamos dar un bucket completo si es posible (colectando vecinos) — igual que bucketCollect.
     * - Si no hay para bucket completo, devolvemos la cantidad extraíble del bloque (en mB).
     * - Si doDrain==true aplicamos los cambios al mundo (usamos tus helpers que modifican el mundo).
     */
    @Override
    public FluidStack drain(World world, BlockPos pos, boolean doDrain) {
        IBlockState state = world.getBlockState(pos);
        if (!(state.getBlock() instanceof BlockFiniteFluid)) return null;

        BlockFiniteFluid bf = (BlockFiniteFluid) state.getBlock();
        Fluid blockFluid = bf.getFluid();
        if (blockFluid == null) return null;

        int centerConcept = BlockFiniteFluid.getConceptualVolume(world, pos); // 0..16
        if (centerConcept <= 0) return null;

        // 1) Si el bloque central ya está full (16) -> bucket completo
        if (centerConcept >= 16) {
            if (doDrain) {
                world.setBlockToAir(pos);
                FiniteFluidLogic.FluidWorldInteraction.activateOcean(world, pos);
                world.neighborChanged(pos, bf, pos);
            }
            return new FluidStack(blockFluid, 1000);
        }

        // 2) Calculamos total disponible alrededor (simulación, sin modificar)
        int total = centerConcept;
        BlockPos[] laterals = {pos.north(), pos.south(), pos.east(), pos.west()};
        BlockPos[] diagonals = {
            pos.north().east(), pos.north().west(),
            pos.south().east(), pos.south().west()
        };
        BlockPos below = pos.down();

        for (BlockPos p : laterals) {
            IBlockState s = world.getBlockState(p);
            if (s.getBlock() instanceof BlockFiniteFluid) {
                BlockFiniteFluid nb = (BlockFiniteFluid) s.getBlock();
                if (nb.getFluid() == blockFluid) total += BlockFiniteFluid.getConceptualVolume(world, p);
            }
        }
        for (BlockPos p : diagonals) {
            IBlockState s = world.getBlockState(p);
            if (s.getBlock() instanceof BlockFiniteFluid) {
                BlockFiniteFluid nb = (BlockFiniteFluid) s.getBlock();
                if (nb.getFluid() == blockFluid) total += BlockFiniteFluid.getConceptualVolume(world, p);
            }
        }
        {
            IBlockState s = world.getBlockState(below);
            if (s.getBlock() instanceof BlockFiniteFluid) {
                BlockFiniteFluid nb = (BlockFiniteFluid) s.getBlock();
                if (nb.getFluid() == blockFluid) total += BlockFiniteFluid.getConceptualVolume(world, below);
            }
        }

        // 3) Si hay al menos 16 niveles conceptuales disponibles -> podemos devolver 1000mB
        if (total >= 16) {
            if (doDrain) {
                // Usamos tu helper que consume vecinos y central para formar cubeta completa.
                // bucketRemoveFluidOnlyFullCollect hace la extracción real.
                FiniteFluidLogic.FluidWorldInteraction.bucketRemoveFluidOnlyFullCollect(world, pos, centerConcept, blockFluid);
                // observe: tu helper ya hace world.setBlockToAir/setBlockState según convenga
            }
            return new FluidStack(blockFluid, 1000);
        }

        // 4) No alcanza para cubeta entera: devolver lo que hay en el bloque (o extraer lo máximo disponible)
        if (doDrain) {
            // Usamos el helper que extrae suavemente hasta 16 niveles (pero si no hay tanto, extrae lo disponible).
            int collectedLevels = FiniteFluidLogic.FluidWorldInteraction.bucketRemoveFluidEvenLowCollect(world, pos, centerConcept, 16, blockFluid);
            if (collectedLevels <= 0) return null;
            int mb = conceptualToMB(collectedLevels);
            return new FluidStack(blockFluid, mb);
        } else {
            // Simulación: sólo devolvemos lo que hay en el bloque central (sin tocar vecinos)
            int mbCenter = conceptualToMB(centerConcept);
            return new FluidStack(blockFluid, mbCenter);
        }
    }

    /**
     * canDrain:
     * - true si hay algo para drenar en la posición o en vecinos inmediatos (compatibilidad con bombas que intentan formar cubeta).
     */
    @Override
    public boolean canDrain(World world, BlockPos pos) {
        IBlockState s = world.getBlockState(pos);
        if (!(s.getBlock() instanceof BlockFiniteFluid)) return false;

        BlockFiniteFluid bf = (BlockFiniteFluid) s.getBlock();
        Fluid f = bf.getFluid();
        if (f == null) return false;

        int center = BlockFiniteFluid.getConceptualVolume(world, pos);
        if (center > 0) return true; // hay fluidos en el propio bloque

        // si no, chequeamos laterales/diagonales/abajo para ver si hay algo que pueda agregarse
        BlockPos[] checks = {
            pos.north(), pos.south(), pos.east(), pos.west(),
            pos.north().east(), pos.north().west(), pos.south().east(), pos.south().west(),
            pos.down()
        };
        for (BlockPos p : checks) {
            IBlockState n = world.getBlockState(p);
            if (n.getBlock() instanceof BlockFiniteFluid) {
                BlockFiniteFluid nb = (BlockFiniteFluid) n.getBlock();
                if (nb.getFluid() == f && BlockFiniteFluid.getConceptualVolume(world, p) > 0) return true;
            }
        }
        return false;
    }

    /**
     * getFilledPercentage:
     * - devuelve entre 0.0 (vacío / no es BlockFiniteFluid) y 1.0 (bloque conceptualmente lleno, 16/16).
     * - usamos getLevelForBlock (conceptual 1..16) y lo normalizamos a 16.
     */
    @Override
    public float getFilledPercentage(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        if (!(state.getBlock() instanceof BlockFiniteFluid)) return 0f;
        int conceptual = BlockFiniteFluid.getConceptualVolume(world, pos); // 0..16
        if (conceptual <= 0) return 0f;
        return conceptual / 16f; // 1/16 == 0.0625 ... 16/16 == 1.0
    }    

}
