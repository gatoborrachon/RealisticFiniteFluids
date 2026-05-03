package com.gatoborrachon.realisticfinitefluids.mixin.earlymixins;

import java.lang.reflect.Method;
import java.util.EnumMap;
import java.util.Map;
import java.util.Random;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.gatoborrachon.realisticfinitefluids.References;
import com.gatoborrachon.realisticfinitefluids.blocks.properties.UnlistedPropertyBoolean;
import com.gatoborrachon.realisticfinitefluids.interfaces.IRealisticFiniteFluid;
import com.gatoborrachon.realisticfinitefluids.logic.FiniteFluidLogic;
import com.gatoborrachon.realisticfinitefluids.logic.FiniteFluidLogic.FiniteFluidsLogic;
import com.gatoborrachon.realisticfinitefluids.logic.FiniteFluidLogic.FluidWorldInteraction;

import com.gatoborrachon.realisticfinitefluids.logic.RealisticFiniteFluidFunctions;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDynamicLiquid;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.PropertyFloat;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mixin(BlockDynamicLiquid.class)
public abstract class MixinBlockDynamicLiquid extends BlockLiquid implements IRealisticFiniteFluid, IFluidBlock {

	@Inject(method = "<init>", at = @At("RETURN"))
	private void onConstructed(Material material, CallbackInfo ci) {
		if (References.debugBlockFluidClassic) {
			Class<?> clazz = this.getClass();

			if (clazz != BlockDynamicLiquid.class) {
				System.out.println("[RFF] Nueva clase que extiende BlockDynamicLiquid: " + clazz.getName());

				// Ver métodos overrideados
				for (Method m : clazz.getDeclaredMethods()) {
					try {
						Method parent = BlockDynamicLiquid.class.getMethod(m.getName(), m.getParameterTypes());
						if (!m.equals(parent)) {
							System.out.println("[RFF] Método overrideado: " + m.getName());
						}
					} catch (NoSuchMethodException e) {
						System.out.println("[RFF] Método NUEVO agregado: " + m.getName());
					}
				}
			}
		}
	}
	
	

	
    
    

@Inject(
	    method = "<init>",
	    at = @At("RETURN")
	)
	private void rff$onConstruct(Material material, CallbackInfo ci) {
	    this.setDefaultState(this.blockState.getBaseState().withProperty(LEVEL, MAXIMUM_LEVEL));
	    if (this.material == Material.LAVA) this.setLightLevel(1.0F);
		this.fluidMaterial = material;
		//this.setTickRandomly(false);

		//BlockDynamicLiquid Ticks by default
	    //this.setTickRandomly(FiniteFluidLogic.shouldTickRandomly);
	    //this.setDefaultFluidState(FluidState.of(this.getFluid()).withLevel(MAXIMUM_LEVEL));
	}
    
    

	
	
	
	
	
	
	
	/*public MixinBlockDynamicLiquid(Fluid fluid, Material material) {
		this(fluid, material, material.getMaterialMapColor());
	}*/


	public MixinBlockDynamicLiquid(Material material) {
		super(material);
		this.fluid = material == Material.WATER ? FluidRegistry.WATER : FluidRegistry.LAVA;
		this.fluidMaterial = material;

		this.fluidName = fluid.getName();
		this.density = fluid.getDensity();
		this.temperature = fluid.getTemperature();
		//this.maxScaledLight = fluid.luminosity;
		this.densityDir = fluid.getDensity() > 0 ? -1 : 1;

		this.setDefaultState(this.blockState.getBaseState().withProperty(LEVEL, Integer.valueOf(MAXIMUM_LEVEL)));
		if (this.material == Material.LAVA) this.setLightLevel(1.0F);
		this.setTickRandomly(FiniteFluidLogic.shouldTickRandomly);
		
        //System.out.println("[RFF] BlockFluidClassic '"+ this.getRegistryName() + "' with fluid '"+fluid.getName()+ "' Added");
        //FluidCompat.fluidBlocksList.add(fluid);
	}
	
	
	
	
	
	




	@Unique private Fluid fluid;
	@Unique private Material fluidMaterial;
	@Unique private String fluidName;
	@Unique private Object density;
	@Unique private Object temperature;
	@Unique private Object densityDir;
	//@Shadow private Material material;

	/**
	 * pequeña histéresis para evitar parpadeos en calculos de renderizado
	 */
	@Unique private static final double EPS = References.EPS;
    
    /**
     * Minimum literal level for the finite fluid blocks (0). The minimum conceptual level is 1.
     */
	@Unique private static final int MINIMUM_LEVEL = References.MINIMUM_LEVEL;
	@Unique private static final int MINIMUM_CONCEPTUAL_LEVEL = References.MINIMUM_CONCEPTUAL_LEVEL;
    
    /**
     * Maximum literal level for the finite fluid blocks (7). The maximum conceptual level is 8.
     */
	@Unique private static final int MAXIMUM_LEVEL = References.MAXIMUM_LEVEL; //ESTE ES EL MAESTRO ALV
	@Unique private static final int MAXIMUM_CONCEPTUAL_LEVEL = References.MAXIMUM_CONCEPTUAL_LEVEL;

    /**
     * Quartiles
     */
	@Unique private static final int Q1_LOW = References.Q1_LOW;
	@Unique private static final int Q1_HIGH = References.Q1_HIGH;
    
	@Unique private static final int Q2_LOW = References.Q2_LOW;
	@Unique private static final int Q2_HIGH = References.Q2_HIGH;

	@Unique private static final int Q3_LOW = References.Q3_LOW;
	@Unique private static final int Q3_HIGH = References.Q3_HIGH;


    // =========================
    // Properties Handling
    // ========================= 
	//@Unique private static final PropertyInteger LEVEL = References.LEVEL; //MEJOR NO TOCAMOS ESTO, SE VA ALV EL REGISTRO DE BLOQUES
    @Unique private static final IUnlistedProperty<Map<EnumFacing, IBlockState>> NEIGHBOR_STATES = References.NEIGHBOR_STATES;
    @Unique private static final IUnlistedProperty<Float>[] LEVEL_CORNERS = References.LEVEL_CORNERS;
    ////@Unique private static final PropertyFloat[] LEVEL_CORNERS_EXP = References.LEVEL_CORNERS_EXP;
    @Unique private static final IUnlistedProperty<Integer> FLUID_COLOR = References.FLUID_COLOR;
	@Unique private static final IUnlistedProperty<Vec3d> FLOW_DIRECTION = References.FLOW_DIRECTION;
	@Unique private static final UnlistedPropertyBoolean IS_STILL = References.IS_STILL;
	@Unique private static final UnlistedPropertyBoolean IS_GASEOUS = References.IS_GASEOUS;

	@Unique
	@Override
    public IUnlistedProperty<Vec3d> getFlowDirectionProperty() {
    	return FLOW_DIRECTION;
    }
	@Unique
	@Override
	public IUnlistedProperty<Map<EnumFacing, IBlockState>> getNeighborStates() {
		return NEIGHBOR_STATES;
	}
	@Unique
	@Override
	public IUnlistedProperty<Integer> getFluidColor() {
		return FLUID_COLOR;
	}
	@Unique
	@Override
	public UnlistedPropertyBoolean getIsStill() {
		return IS_STILL;
	}
	@Unique
	@Override
	public PropertyFloat getCornerLevel(int index) {
		return (PropertyFloat) LEVEL_CORNERS[index];
	}
	@Unique
	@Override
	public UnlistedPropertyBoolean getIsGaseous() {
		return IS_GASEOUS;
	}

	@Unique
	@Override
	protected BlockStateContainer createBlockState() {
		return new ExtendedBlockState(this,
				new IProperty[] { LEVEL }, //Listed Properties
				new IUnlistedProperty<?>[] { //Unlisted Properties
					//ACTIVAR PARA FLUIDLOGGED API
					NEIGHBOR_STATES,
					LEVEL_CORNERS[0],
					LEVEL_CORNERS[1],
					LEVEL_CORNERS[2],
					LEVEL_CORNERS[3],
					FLUID_COLOR,
					FLOW_DIRECTION,
					IS_STILL,
					IS_GASEOUS
				}
			   );
	}
	
    /*static
    {
        ImmutableList.Builder<IUnlistedProperty<?>> builder = ImmutableList.builder();
        builder.add(FLOW_DIRECTION);
        for(int i = 0; i < 4; i++)
        {
            LEVEL_CORNERS[i] = new PropertyFloat("level_corner_" + i, 0f, 1f);
            builder.add(LEVEL_CORNERS[i]);

            SIDE_OVERLAYS[i] = new UnlistedPropertyBool("side_overlay_" + i);
            builder.add(SIDE_OVERLAYS[i]);
        }
        FLUID_RENDER_PROPS = builder.build();
    }
	
    @Override
    @Nonnull
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer.Builder(this)
                .add(LEVEL)
                .add(FLUID_RENDER_PROPS.toArray(new IUnlistedProperty<?>[0]))
                .build();
    }*/

	//ACTIVAR PARA FLUIDLOGGED API
	@Unique
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

			Vec3d flowDirection = (RealisticFiniteFluidFunctions.getBlock(world, pos, state) instanceof IRealisticFiniteFluid) ? calculateFlowVector(world, pos, true) : new Vec3d(0,0,0);
			//Vec3d flowDirection = (state.getBlock() instanceof BlockNewWater_Flow) ? getFlowVector(world, pos) : null;
			//Vec3d flowDirection = new Vec3d(0,0,0);

			boolean isStill = false; //FiniteFluidLogic.GeneralPurposeLogic.canMoveForRender(world, pos, this.getVolume(world, pos, state));

			boolean isGaseous = false;

			extendedState = extendedState
					.withProperty(NEIGHBOR_STATES, neighborStates)
					.withProperty(LEVEL_CORNERS[0], h00)
					.withProperty(LEVEL_CORNERS[1], h10)
					.withProperty(LEVEL_CORNERS[2], h01)
					.withProperty(LEVEL_CORNERS[3], h11)
					.withProperty(FLUID_COLOR, color)
					.withProperty(FLOW_DIRECTION, flowDirection)
					.withProperty(IS_STILL, isStill)
					.withProperty(IS_GASEOUS, isGaseous);
			return extendedState; 
		}
		return state;
	}


	@Unique
	@Override
	public IBlockState getStateFromMeta(int meta) {
		return ((Block)(Object)this).getDefaultState().withProperty(LEVEL, meta);
	}

	@Unique
	@Override
	public int getMetaFromState(IBlockState state) {
		//return state.getValue(LEVEL);
		return getVolume(null, null, state);
	}



	// =========================
	// Special RealisticFiniteFluid Functions
	// ========================= 
	/**
	 * Gets the LEVEL value the block on the current pos
	 * @param world the current World
	 * @param pos the Position of the block
	 * @return the LEVEL value in 0 to 15 scale.
	 */
	@Unique
	@Override
	public int getVolume(@Nullable IBlockAccess world, @Nullable BlockPos pos, @Nullable IBlockState state) {
		return RealisticFiniteFluidFunctions.getVolume(world, pos, state);
	}


	/**
	 * Gets the LEVEL value the block on the current pos. IN A SCALE 1-16
	 * @param world the current World
	 * @param pos the Position of the block
	 * @return the LEVEL value IN A SCALE 1-16
	 */
	@Unique
	@Override
	public int getConceptualVolume(@Nullable IBlockAccess world, @Nullable BlockPos pos, @Nullable IBlockState state) {
		return RealisticFiniteFluidFunctions.getConceptualVolume(world, pos, state);
	}

	/**
	 * Sets the specified LEVEL from the passed stated to the block on the current pos
	 * @param world the current World
	 * @param pos the Position of the block
	 * @param state the IBlockState with the right LEVEL value
	 */
	@Unique
	@Override
	public IBlockState setVolume(@Nullable World world, @Nullable BlockPos pos, @Nullable IBlockState state, int level) {
		return RealisticFiniteFluidFunctions.setVolume(world, pos, state, level);
	}

	@Unique
	@Override
	public IBlockState setConceptualVolume(@Nullable World world, @Nullable BlockPos pos, @Nullable IBlockState state, int level) {
		return RealisticFiniteFluidFunctions.setConceptualVolume(world, pos, state, level);
	}


	/**
	 * Unified function to setBlockState. Intented for compat with Fluidlogged API
	 */
	@Unique
	@Override
	public void setBlockState(World world, BlockPos sourcePos, BlockPos destPos, IBlockState state) {
		RealisticFiniteFluidFunctions.setBlockState(world, sourcePos, destPos, state);
	}
	
	/**
	 * Unified function to setBlockToAir. Intented for compat with Fluidlogged API
	 */
	@Unique
	@Override
	public void setBlockToAir(World world, BlockPos destPos) {
		RealisticFiniteFluidFunctions.setBlockToAir(world, destPos);
	}

	/**
	 * Unified function to setBlockState. Intented for compat with Fluidlogged API
	 */
	/*public static void setBlockState(World world, BlockPos pos, IBlockState state, int flag) {
        	world.setBlockState(pos, state, flag);
        }*/









	/**
	 * Special function to interact specifically with the Overlay of water depending on the eye height of the player. 
	 * Avoids overriding isEntityInsideMaterial and making any fluid below the eye of the player not of Material.WATER
	 */
	@Unique
	@Override
	public Boolean isEntityInsideMaterialForOverlay(IBlockAccess world, BlockPos pos, IBlockState state, 
			Entity entity, double eyeY, Material material) {
		return RealisticFiniteFluidFunctions.isEntityInsideMaterialForOverlay(world, pos, state, entity, eyeY, material);
	}

	/**
	 * Try to freeze the current fluid block.
	 * @param world the current World
	 * @param pos the current BlockPos
	 * @param state the current IBlockState
	 * @param rand a random value
	 * @return if this block was turned into ice
	 */
	@Unique
	@Override
	public boolean tryFreezeWater(World world, BlockPos pos, IBlockState state, Random rand) {
		return RealisticFiniteFluidFunctions.tryFreezeWater(world, pos, state, rand);
	}

	/**
	 * Whether this block is near a heat source or not
	 * Requires more compat
	 * 
	 * @param world the current World
	 * @param pos the position of the block to affect
	 * @return if it is near a heat source
	 */
	@Unique
	@Override
	public boolean isNearHotBlock(World world, BlockPos pos) {
		return RealisticFiniteFluidFunctions.isNearHotBlock(world, pos);
	}


	/**
	 * Whether this block should evaporate
	 * Only matters for still fluids
	 * @param world the current World
	 * @param pos the current BlockPos
	 * @return if this finite fluid block can evaporate
	 */
	@Unique
	@Override
	public boolean shouldEvap(World world, BlockPos pos, Random rand) {
		return RealisticFiniteFluidFunctions.shouldEvap(world, pos, rand);
	}

	/**
	 * Whether this block interacts with other FiniteFluids
	 * @param world the current World
	 * @param pos1 current Block Position
	 * @param pos2 second BlockPosition
	 * @return if this can interact with the second Position
	 */
	@Unique
	@Override
	public boolean interactWithLiquid(World world, BlockPos currentPos, BlockPos targetPos) {
		return RealisticFiniteFluidFunctions.interactWithLiquid(world, currentPos, targetPos);
	}


	/**
	 * Used inside logic of fluid movement.
	 * Only true for water, false for lava
	 * @return
	 */
	@Unique
	@Override
	public boolean shouldSearchOutward(Material material) {
		return RealisticFiniteFluidFunctions.shouldSearchOutward(material);
	}

	@Unique
	@Override
	public boolean shouldFlowToNeighbor(IBlockAccess world, BlockPos posFrom, BlockPos posTo) {
		return RealisticFiniteFluidFunctions.shouldFlowToNeighbor(world, posFrom, posTo);
	}

	/**
	 * Gets the actual flow vector based on adyacent finite fluid blocks
	 * @param world The actual world
	 * @param pos The current position of the block to get its flow vector
	 * @return The Vec3d Flow Vector
	 */
	@Unique
	@Override
	public Vec3d calculateFlowVector(IBlockAccess world, BlockPos pos, boolean fluidRequest) {
		return RealisticFiniteFluidFunctions.calculateFlowVector(world, pos, fluidRequest);
	}


	@Unique
	@Override
	public BlockPos getPositionOnGravityDirection(BlockPos originalPos) {
		return RealisticFiniteFluidFunctions.getPositionOnGravityDirection(originalPos);
	}

	/**
	 * To check if the given block is Oceanic (it should have 1 level more of the MAXIMUM_LEVEL for other normal blocks (Flowing and Still). This only works if we are not using all the values on LEVEL)
	 * @param world
	 * @param pos
	 * @param state
	 * @param fluidType The current fluidType to search
	 * @return
	 */
	@Unique
	@Override
	public boolean isOceanBlock(@Nullable IBlockAccess world, @Nullable BlockPos pos, @Nullable IBlockState state, int fluidType) {
		return RealisticFiniteFluidFunctions.isOceanBlock(world, pos, state, fluidType);
	}


















	// =========================
	// Vanilla Overriddes
	// =========================    

	@Override
	public void randomTick(World worldIn, BlockPos pos, IBlockState state, Random random) {
		/*
		//Freeze blocks, if the block freezes, stop all the logic
		Block blockToCheck = worldIn.getBlockState(pos).getBlock();
		if (!(blockToCheck instanceof IRealisticFiniteFluid)) return;
		if (((IRealisticFiniteFluid)blockToCheck).getFluid() == FluidRegistry.WATER && ModConfig.waterCanFreeze && this.tryFreezeWater(worldIn, pos, state, random)) return;
		//super.randomTick(worldIn, pos, state, random);
		*/
	}

	@Unique
	@Override
	@SideOnly(Side.CLIENT)
	public Vec3d getFogColor(World world, BlockPos pos, IBlockState state, Entity entity, Vec3d originalColor,
			float partialTicks) {
		return RealisticFiniteFluidFunctions.getFogColor(world, pos, state, entity, originalColor, partialTicks);
	}
	




	@Unique
	@Override
	@SideOnly(Side.CLIENT)
	public IBlockState getStateAtViewpoint(IBlockState state, IBlockAccess world, BlockPos pos, Vec3d viewpoint) {
		return RealisticFiniteFluidFunctions.getStateAtViewpoint(state, world, pos, viewpoint);
	}


	@Unique
	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
		int level = getConceptualVolume(worldIn, pos, state); //state.getValue(LEVEL)+1; // PropertyInteger LEVEL conceptual = 1-16
		float height = level / (float)MAXIMUM_CONCEPTUAL_LEVEL;
		return new AxisAlignedBB(0.0F, 0.0F, 0.0F, 1.0F, height, 1.0F);
	}


	/*@Unique
	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.MODEL;// FiniteFluidLogic.id2;
	}*/

	@Unique
	@Override
	@SideOnly(Side.CLIENT)
	public BlockRenderLayer getRenderLayer() {
		//return BlockRenderLayer.TRANSLUCENT;
		return this.fluidMaterial == Material.WATER ? BlockRenderLayer.TRANSLUCENT : BlockRenderLayer.SOLID;
	}



	@Unique
	@Override
	public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
		return layer == BlockRenderLayer.TRANSLUCENT;
	}

	//NO SE PARA QUE ERA ESTO
	@Unique
	@Override
	public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
		int var5 = super.getLightValue(state, world, pos);  // llamada a super para evitar recursiÃ³n
		int var6 = super.getLightValue(state, world, new BlockPos(pos.getX(), pos.getY() + 1, pos.getZ()));
		return var5 <= var6 ? var6 : var5;
	}

	@Unique
	@Override
	@SideOnly(Side.CLIENT)
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

	@Unique
	@Override
	public int getLightOpacity(IBlockState state, IBlockAccess world, BlockPos pos) {
		return FiniteFluidLogic.waterLightOpacity; //Vanilla uses 3, i use 1
	}

	@Unique
	@Override
	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(IBlockState stateIn, World worldIn, BlockPos pos, Random rand) {
		double d0 = (double)pos.getX();
		double d1 = (double)pos.getY();
		double d2 = (double)pos.getZ();

		if (this.fluidMaterial == Material.WATER) {
			boolean isFlowing = RealisticFiniteFluidFunctions.getBlock(worldIn, pos, stateIn) instanceof BlockDynamicLiquid; // || stateIn.getBlock() instanceof BlockNewInfiniteSource;
			boolean isStill = !isFlowing; //stateIn.getBlock() instanceof BlockFiniteFluid_Flow; // tu clase para agua en movimiento
			////System.out.println("[RFF - Vanilla Flow] isStill: "+isStill);
			////System.out.println("[RFF - Vanilla Flow] IExtendedBlockState: "+(stateIn instanceof IExtendedBlockState));
			////System.out.println("[RFF - Vanilla Flow] IS_STILL property: "+((IExtendedBlockState)stateIn).getValue(IS_STILL));
			////System.out.println("[RFF - Vanilla Flow] ");

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


	/*@Unique
	@Override
	public int tickRate(World worldIn) {
		if (this.fluid == FluidRegistry.WATER) 
			return FiniteFluidLogic.waterTick;

		else if (this.fluid == FluidRegistry.LAVA) 
			return worldIn.provider.isNether() ? FiniteFluidLogic.lavaTick/3 : FiniteFluidLogic.lavaTick;

		else
			return fluid.getViscosity() / 200;
	}*/



	@Unique
	@Override
	public boolean isReplaceable(IBlockAccess worldIn, BlockPos pos) {
		return true; // worldIn.getBlockState(pos).getMaterial() == Material.WATER;
	}


	@Unique
	@Override
	public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
		return face == EnumFacing.UP ? BlockFaceShape.SOLID : BlockFaceShape.UNDEFINED;
	}


	@Unique
	@Override
	public int quantityDropped(IBlockState state, int fortune, Random random) {
		return 0;
	}

	/**
	 * Returns whether this block is collideable based on the arguments passed in Args: blockMetaData, unknownFlag
	 */
	//@Unique
	@Override
	//@Overwrite
	public boolean canCollideCheck(IBlockState state, boolean hitIfLiquid) {
		return hitIfLiquid;
	}


	@Unique
	@Override
	public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
		return null;
	}

	@Unique
	@Override
	//@Overwrite
	public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
		if (isOceanBlock(worldIn, pos, null, FiniteFluidLogic.GeneralPurposeLogic.getFluidIndex(RealisticFiniteFluidFunctions.getBlock(worldIn, pos, worldIn.getBlockState(pos))))) 
			return;

		if (worldIn.getBlockState(pos).getMaterial() == Material.LAVA) {
			for (EnumFacing dir : EnumFacing.VALUES) {
				BlockPos neighborAir = pos.offset(dir);
				IBlockState neighborAirState = worldIn.getBlockState(neighborAir);
				if (neighborAirState.getMaterial() == Material.WATER) {
					FluidWorldInteraction.interactWithNeighborLiquid(worldIn, pos);
					//break;
				}
			}
		}

		//worldIn.scheduleUpdate(pos, RealisticFiniteFluidFunctions.returnCorrectBlock(worldIn, pos), this.tickRate(worldIn));
		worldIn.scheduleUpdate(pos, ((Block)(Object)this), this.tickRate(worldIn));
		//worldIn.scheduleUpdate(pos, this, this.tickRate(worldIn));
		//worldIn.scheduleUpdate(pos, ((Block)(Object)this), this.tickRate(worldIn));
	}



	/**
	 * If this block doesn't render as an ordinary block it will return False (examples: signs, buttons, stairs, etc)
	 */
	@Unique
	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}

	@Unique
	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	//@Unique
	//@Override
	@Overwrite(remap = References.onDev) //onBlockAdded
	public void func_176213_c(World worldIn, BlockPos pos, IBlockState state) {
		if (this.fluidMaterial == Material.LAVA) FiniteFluidLogic.lavaFunctions.burnArea(worldIn, pos);
		//worldIn.scheduleUpdate(pos, RealisticFiniteFluidFunctions.returnCorrectBlock(worldIn, pos), this.tickRate(worldIn));
		worldIn.scheduleUpdate(pos, ((Block)(Object)this), this.tickRate(worldIn));
		//worldIn.scheduleUpdate(pos, this, this.tickRate(worldIn));
	}

	/**
	 * Used to prevent updates on chunk generation
	 */
	@Unique
	@Override
	public boolean requiresUpdates()
	{
		return false;
	}
	
	@Unique
	@Override
    public int tickRate(World worldIn)
    {
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
            return 0;
        }
    }











	/**
	 * Ticks the block if it's been scheduled
	 */
	@Overwrite(remap = References.onDev) //updateTick
	public void func_180650_b(World world, BlockPos pos, IBlockState state, Random rand) {
		if (!world.isRemote)
		{
			//System.out.println("VERGA 2: "+pos);


            //System.out.println("MAX CALC "+FiniteFluidLogic.maxCalc);

			int newLevel = this.getVolume(world, pos, world.getBlockState(pos)); //world.getBlockState(pos).getValue(LEVEL);

			
			
			
			
			
			
			
			//ELSE --> WE ARE FLOWING BLOCK
			//BlockFiniteFluid.setVolume(world, pos, state.withProperty(IS_STILL, false), newLevel);

			//SETEAR EL TIPO DE FLUIDO ACTUAL EN LOS CALCULOS DE LA LOGICA
			FiniteFluidLogic.GeneralPurposeLogic.setCurrentFluidIndex(((Block)(Object)this));

			//DETERMIANR TANTO EL BLOQUE ACTUAL COMO EL BLOQUE DE ABAJO
			IBlockState currentaState = world.getBlockState(pos);
			BlockPos posBelow = getPositionOnGravityDirection(pos);
			Block belowBlock = RealisticFiniteFluidFunctions.getBlock(world, posBelow, world.getBlockState(posBelow));
			//BlockPos belowBlock = new BlockPos(pos.getX(), pos.getY() - 1 * FiniteFluidLogic.GeneralPurposeLogic.getFluidGravity(), pos.getZ());


			//Aca yo controlo lo de interaccion de flowing con ocean xd
			//Avoid too much block updates over oceanic liquid
			if (isOceanBlock(world, pos.down(), null, FiniteFluidLogic.onFiniteFluidIndex) //downBlock == FiniteFluidLogic.liquids.get(FiniteFluidLogic.GeneralPurposeLogic.getFluidIndex(currentBlock)).oceanBlock 
					&& getVolume(world, pos, currentaState) < Q1_HIGH) { //8
				int newValue = getVolume(world, pos, world.getBlockState(pos))/2; //3
				//world.setBlockState(pos, currentaState.withProperty(BlockFiniteFluid.LEVEL, newValue));
				setBlockState(world, pos, pos, setVolume(world, pos, currentaState, newValue));
				//System.out.println("VERGA?");
			}

			//Despertar bloques oceanicos (para evitar dejarlos sin actualizar, y que se vean raros)
			FiniteFluidLogic.OceanFluidsLogic.wakeOcean(world, pos);

			//REVISAR SI ACTUALMENTE PODEMOS EJECUTAR CALCULOS Y NO SOBRECARGAR EL CPU
			if (FiniteFluidLogic.GeneralPurposeLogic.getCalc() > FiniteFluidLogic.GeneralPurposeLogic.getMaxCalc())
			{
				//world.scheduleUpdate(pos, RealisticFiniteFluidFunctions.returnCorrectBlock(world, pos), this.tickRate(world));
				world.scheduleUpdate(pos, ((Block)(Object)this), this.tickRate(world));
				//world.scheduleUpdate(pos, this, this.tickRate(world));
			}

			//SI SI PODEMOS HACER CALCULOS -->
			else
			{
				//Obtenemos el bloque inmediatamente abajo (dependiendo la gravedad del bloque actual, si es un gas o un liquidp)
				//Block belowBlock1 = world.getBlockState(new BlockPos(pos.getX(), pos.getY() - 1 * FiniteFluidLogic.GeneralPurposeLogic.getFluidGravity(), pos.getZ())).getBlock();

				//sI EL BLOQUE DE ABAJO NO ES AIRE, Y SI NO TIENE EN SU PROXIMIDAD AL AIRE
				//OSEA, UNA PLANCHA DE BLOQUES DEBAJO
				if (belowBlock != Blocks.AIR & !FiniteFluidLogic.GeneralPurposeLogic.hasAdjacentTarjetBlocksHorizontal(world, pos, Blocks.AIR))
				{
					//VOLVEMOS A OBTENER LA POSICION DEL BLOQUE ABAJO (DEPENDIENDO LA GRAVEDAD DEL BLOQUE ACTUAL)

					//PRIMERO:
					//1.- Si los calculos actuales son mayores al 65% de calculos permitidos
					//2.- Y si el volumen del bloque actual es menor al Quartil 1
					//3.1.- Y si el bloque de abajo NO es un fluido finito realista
					//3.2.- O si el volumen del bloque de abajo es menor al nivel maximo.
					if ((float)FiniteFluidLogic.GeneralPurposeLogic.getCalc() > (float)FiniteFluidLogic.GeneralPurposeLogic.getMaxCalc() * 0.65F && getVolume(world, pos, world.getBlockState(pos)) < Q1_HIGH & (!FiniteFluidLogic.GeneralPurposeLogic.isFiniteFluid(world, posBelow) || getVolume(world, posBelow, world.getBlockState(posBelow)) < MAXIMUM_LEVEL))
					{
						//NOS ESPERAMOS AL PROXIMO TICK
						//world.scheduleUpdate(pos, RealisticFiniteFluidFunctions.returnCorrectBlock(world, pos), this.tickRate(world));
						world.scheduleUpdate(pos, ((Block)(Object)this), this.tickRate(world));
						//world.scheduleUpdate(pos, this, this.tickRate(world));
						return;
					}

					//SI LOS CALCULOS ACTUALES SON MAYORES AL 50% DE CALCULOS PERMITIDPS
					if ((float)FiniteFluidLogic.GeneralPurposeLogic.getCalc() > (float)FiniteFluidLogic.GeneralPurposeLogic.getMaxCalc() * 0.5F)
					{
						//BUSCAMOS AL JUGADOR MAS PROXIMO
						EntityPlayer nearestPlayer = world.getClosestPlayer((double)pos.getX(), (double)pos.getY(), (double)pos.getZ(), FiniteFluidLogic.GeneralPurposeLogic.getPlayerDistanceToCalc(), true);

						//SI EL JUGADOR MAS PROXIMO NO EXISTE
						if (nearestPlayer == null)
						{
							//NOS ESPERAMOS AL PROXIMO TICK
							//world.scheduleUpdate(pos, RealisticFiniteFluidFunctions.returnCorrectBlock(world, pos), this.tickRate(world));
							world.scheduleUpdate(pos, ((Block)(Object)this), this.tickRate(world));
							//world.scheduleUpdate(pos, this, this.tickRate(world));
							return;
						}
					}
				}

				//SI TODO LO ANTERIOR NO SE CUMPLIO --> 
				//AÑADIMOS UN CALCULO A LA LSITA DE CALCULOS ACTUALES
				FiniteFluidLogic.GeneralPurposeLogic.addCalc();

				//SI EL BLOQUE DE ABAJO (DEPENDIENDO GRAVEDAD) ES UN BLOQUE DEBUG
				if (belowBlock == References.DEBUG_BLOCK) //Blocks.DIAMOND_BLOCK
				{
					//LE PONEMOS AL BLOQUE ACTUAL EL VOLUMEN MAXIMO SI ES QUE NO YA TIENE EL VOLUMEN MAXIMO
					IBlockState currentState = world.getBlockState(pos);
					IBlockState newState = setVolume(world, pos, currentState, MAXIMUM_LEVEL); //currentState.withProperty(BlockFiniteFluid.LEVEL, 15);
					if (getVolume(world, pos, currentState) < MAXIMUM_LEVEL) { //15
						setBlockState(world, pos, pos, newState);
						//world.setBlockState(pos, newState, 3);
					}

					//if (world.getBlockState(pos.up()) == Blocks.AIR) {
						//CREAMOS UN BLOQUE NUEVO DEL MISMO FLUIDO ARRIBA CON EL MAXIMO DE FLUIDO
						setBlockState(world, pos, pos.up(), newState);
						//world.setBlockState(pos.up(), newState, 3);

						//EJECUTAMOS LA LOGICA DE MOVIMIENTO DEL BLOQUE DE ARRIBA
						FiniteFluidsLogic.tryLiquidMove(world, pos.up());
					//}
					//world.scheduleUpdate(pos, RealisticFiniteFluidFunctions.returnCorrectBlock(world, pos), this.tickRate(world));
						world.scheduleUpdate(pos, ((Block)(Object)this), this.tickRate(world));
						//world.scheduleUpdate(pos, this, this.tickRate(world));
				}

				//SI NO EXISTEN BLOQUES DE FLUIDO FINITO ALEDAÑOS
				else if (!FluidWorldInteraction.interactWithNeighborLiquid(world, pos))
				{
					//CALCULAMOS EL NIVEL DEL BLOQUE ACTUAL
					////int newLevel = BlockFiniteFluid.getVolume(world, pos, world.getBlockState(pos));

					//Y CALCULAMOS SI NOS PODEMOS MOVER
					if (FiniteFluidsLogic.tryLiquidMove(world, pos))
					{
						//System.out.println("VERGA FLOWING");
						//SI NOS MOVIMOS, PROGRAMAMOS UN TICK
						//world.scheduleUpdate(pos, RealisticFiniteFluidFunctions.returnCorrectBlock(world, pos), this.tickRate(world));
						world.scheduleUpdate(pos, ((Block)(Object)this), this.tickRate(world));
						//world.scheduleUpdate(pos, this, this.tickRate(world));
						//////System.out.println("FLOWING PERMANECE FLOWING: "+pos+" CON LEVEL "+newLevel);
					}

					//SI NO NOS PODEMOS MOVER --> NOS CONVERTIMOS STILL
					else
					{
						//System.out.println("VERGA STILL");
						//GUARDAMOS EL TIPO DE FLUIDO ACTUAL
						//FiniteFluidLogic.GeneralPurposeLogic.setCurrentFluidIndex(((Block)(Object)this));
						//System.out.println("MICHIRRINES");
						int currentFluidIndex = FiniteFluidLogic.GeneralPurposeLogic.getFluidIndex(RealisticFiniteFluidFunctions.getBlock(world, pos, state));
						//System.out.println("RealisticFiniteFluidFunctions.getBlock(world, pos, state): "+RealisticFiniteFluidFunctions.getBlock(world, pos, state));
						//System.out.println("currentFluidIndex: "+currentFluidIndex);

						//////System.out.println("FLOWING VA A STILL: "+pos+" CON LEVEL "+newLevel);
						//CALCULAMOS UN BLOCKSTATE DEL FLUIDO ACTUAL STILL
						Block stillBlock = FiniteFluidLogic.liquids.get(currentFluidIndex).stillBlock;
						//IBlockState newState = stillBlock.getDefaultState().withProperty(BlockFiniteFluid.LEVEL, newLevel);
						//world.setBlockState(pos, newState, 3);

						//Y COLOCAMOS ESTE BLOQUE STILL EN NUESTRO LUGAR CON NUESTRO VOLUMEN ACTUAL
						setBlockState(world, pos, pos, setVolume(null, null, stillBlock.getDefaultState(), newLevel));

						//CALCULAMOS LA POSICION DEL BLOQUE DE ABAJO POR GRAVEDAD
						////BlockPos belowBlock = new BlockPos(pos.getX(), pos.getY() - 1 * FiniteFluidLogic.GeneralPurposeLogic.getFluidGravity(), pos.getZ());

						//SI EL BLOQUE ACTUAL TIENE EL MISMO MATERIAL QUE EL BLOQUE DE ABAJO
						if (world.getBlockState(pos).getMaterial() == world.getBlockState(posBelow).getMaterial()) {

							//TRAS CIERTA PROBABILIDAD, HACEMOS UNA BUSQUEDA DE BORDES OCEANICOS
							if (rand.nextInt(60) == 0) {
								FiniteFluidLogic.OceanFluidsLogic.borderOceanCheck(world, posBelow, true);
							}

							//SI NO TIENE EL MISMO MATERIAL
						} else if (world.getBlockState(pos).getMaterial() != world.getBlockState(posBelow).getMaterial()) {
							//HACEMOS LA BUSQUEDA DE BORDES DE LIQUIDO OCEANICO TODAS LAS VECES
							FiniteFluidLogic.OceanFluidsLogic.borderOceanCheck(world, posBelow, true);                		
						}
					}
				}
			}
			//System.out.println("FLOWING");






		}
	}


	
	@Unique
	@Override
	public void onEntityCollision(World worldIn, BlockPos pos, IBlockState state, Entity entityIn) {
		RealisticFiniteFluidFunctions.onEntityCollision(worldIn, pos, state, entityIn);
	}

	
	
	
	
	// =========================
	// BlockLiquid Overrides
	// =========================
	@Unique
	@Override
    public Vec3d modifyAcceleration(World worldIn, BlockPos pos, Entity entityIn, Vec3d motion) {
        return motion.add(this.calculateFlowVector(worldIn, pos, true));
    }
	
















	// =========================
	// ForgeFluids Functions
	// =========================
	@Unique
	@Override
	public Fluid getFluid() {
		return material == Material.WATER ? FluidRegistry.WATER : FluidRegistry.LAVA;
		//return this.fluid;
        //return super.getFluid();
	}

	/** Convierte mB -> niveles conceptuales (0..16). 0 significa "no alcanza a colocar nada". */
	@Unique
	private static int mbToConceptual(int mb) {
		if (mb < MINIMUM_CONCEPTUAL_LEVEL) return MINIMUM_LEVEL;
		int c = (int) Math.floor(mb * MAXIMUM_CONCEPTUAL_LEVEL / 1000.0);
		return Math.max(MINIMUM_LEVEL, Math.min(MAXIMUM_CONCEPTUAL_LEVEL, c));
	}

	/** Convierte niveles conceptuales (0..16) -> mB aproximados representados por esos niveles */
	@Unique
	private static int conceptualToMB(int conceptual) {
		if (conceptual < MINIMUM_CONCEPTUAL_LEVEL) return MINIMUM_LEVEL;
		return (int) Math.floor(conceptual * 1000.0 / MAXIMUM_CONCEPTUAL_LEVEL);
	}

	/**
	 * place:
	 * - Devuelve la cantidad de mB *usados* del FluidStack para conseguir el nivel colocado.
	 * - Si doPlace==false se simula (no modifica mundo).
	 * - Si ya hay un BlockFiniteFluid del mismo tipo en pos, sumamos niveles hasta 16.
	 */
	@Unique
	@Override
	public int place(World world, BlockPos pos, FluidStack fluidStack, boolean doPlace) {
		if (fluidStack == null || !fluidStack.getFluid().equals(getFluid())) return MINIMUM_LEVEL;

		int amount = fluidStack.amount;
		int addConceptual = mbToConceptual(amount); // 0..16

		if (addConceptual < MINIMUM_CONCEPTUAL_LEVEL) return MINIMUM_LEVEL; // no alcanza a crear/elevar nivel

		//IBlockState currentState = world.getBlockState(pos);
		int currentConceptual = getConceptualVolume(world, pos, null); // 0..16
		// Si hay otro tipo de bloque fluyente distinto, preferimos colocarlo solo si está vacío/puede reemplazar: dejamos esa decisión al llamador.
		// Sumamos niveles (cap 16)
		int newTotalConceptual = Math.min(MAXIMUM_CONCEPTUAL_LEVEL, currentConceptual + addConceptual);
		int usedConceptual = newTotalConceptual - currentConceptual;
		if (usedConceptual < MINIMUM_CONCEPTUAL_LEVEL) return MINIMUM_LEVEL;

		int usedMB = conceptualToMB(usedConceptual);

		if (doPlace) {
			if (newTotalConceptual < MINIMUM_CONCEPTUAL_LEVEL) {
				RealisticFiniteFluidFunctions.setBlockToAir(world, pos);
				//world.setBlockToAir(pos);
			} else {
				// Guardamos LEVEL como propiedad 0..15 (conceptual-1)
				int levelProp = newTotalConceptual - 1;
				RealisticFiniteFluidFunctions.setBlockState(world, pos, pos, ((Block)(Object)this).getDefaultState().withProperty(LEVEL, levelProp));
			}
			// Notificar vecinos si lo consideras necesario:
			world.neighborChanged(pos, ((Block)(Object)this), pos);
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
	@Unique
	@Override
	public FluidStack drain(World world, BlockPos pos, boolean doDrain) {
		IBlockState state = world.getBlockState(pos);
		if (!(RealisticFiniteFluidFunctions.getBlock(world, pos, state) instanceof IRealisticFiniteFluid)) return null;

		IRealisticFiniteFluid bf = (IRealisticFiniteFluid) RealisticFiniteFluidFunctions.getBlock(world, pos, state);
		Fluid blockFluid = bf.getFluid();
		if (blockFluid == null) return null;

		int centerConcept = getConceptualVolume(world, pos, null); // 0..16
		if (centerConcept < MINIMUM_CONCEPTUAL_LEVEL) return null;

		// 1) Si el bloque central ya está full (16) -> bucket completo
		if (centerConcept >= MAXIMUM_CONCEPTUAL_LEVEL) {
			if (doDrain) {
				RealisticFiniteFluidFunctions.setBlockToAir(world, pos);
				//world.setBlockToAir(pos);
				FiniteFluidLogic.FluidWorldInteraction.activateOcean(world, pos);
				world.neighborChanged(pos, RealisticFiniteFluidFunctions.getBlock(world, pos, state), pos);
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
			if (RealisticFiniteFluidFunctions.getBlock(world, p, s) instanceof IRealisticFiniteFluid) {
				IRealisticFiniteFluid nb = (IRealisticFiniteFluid) RealisticFiniteFluidFunctions.getBlock(world, p, s);
				if (nb.getFluid() == blockFluid) total += getConceptualVolume(world, p, null);
			}
		}
		for (BlockPos p : diagonals) {
			IBlockState s = world.getBlockState(p);
			if (RealisticFiniteFluidFunctions.getBlock(world, p, s) instanceof IRealisticFiniteFluid) {
				IRealisticFiniteFluid nb = (IRealisticFiniteFluid) RealisticFiniteFluidFunctions.getBlock(world, p, s);
				if (nb.getFluid() == blockFluid) total += getConceptualVolume(world, p, null);
			}
		}
		{
			IBlockState s = world.getBlockState(below);
			if (RealisticFiniteFluidFunctions.getBlock(world, below, s) instanceof IRealisticFiniteFluid) {
				IRealisticFiniteFluid nb = (IRealisticFiniteFluid) RealisticFiniteFluidFunctions.getBlock(world, below, s);
				if (nb.getFluid() == blockFluid) total += getConceptualVolume(world, below, null);
			}
		}

		// 3) Si hay al menos 16 niveles conceptuales disponibles -> podemos devolver 1000mB
		if (total >= MAXIMUM_CONCEPTUAL_LEVEL) {
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
			int collectedLevels = FiniteFluidLogic.FluidWorldInteraction.bucketRemoveFluidEvenLowCollect(world, pos, centerConcept, MAXIMUM_CONCEPTUAL_LEVEL, blockFluid);
			if (collectedLevels < MINIMUM_CONCEPTUAL_LEVEL) return null;
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
	@Unique
	@Override
	public boolean canDrain(World world, BlockPos pos) {
		IBlockState s = world.getBlockState(pos);
		if (!(RealisticFiniteFluidFunctions.getBlock(world, pos, s) instanceof IRealisticFiniteFluid)) return false;

		IRealisticFiniteFluid bf = (IRealisticFiniteFluid) RealisticFiniteFluidFunctions.getBlock(world, pos, s);
		Fluid f = bf.getFluid();
		if (f == null) return false;

		int center = getConceptualVolume(world, pos, null);
		if (center >= MINIMUM_CONCEPTUAL_LEVEL) return true; // hay fluidos en el propio bloque

		// si no, chequeamos laterales/diagonales/abajo para ver si hay algo que pueda agregarse
		BlockPos[] checks = {
				pos.north(), pos.south(), pos.east(), pos.west(),
				pos.north().east(), pos.north().west(), pos.south().east(), pos.south().west(),
				pos.down()
		};
		for (BlockPos p : checks) {
			IBlockState n = world.getBlockState(p);
			if (RealisticFiniteFluidFunctions.getBlock(world, pos, n) instanceof IRealisticFiniteFluid) {
				IRealisticFiniteFluid nb = (IRealisticFiniteFluid) RealisticFiniteFluidFunctions.getBlock(world, pos, n);
				if (nb.getFluid() == f && getConceptualVolume(world, p, null) > MINIMUM_LEVEL) return true;
			}
		}
		return false;
	}

	/**
	 * getFilledPercentage:
	 * - devuelve entre 0.0 (vacío / no es BlockFiniteFluid) y 1.0 (bloque conceptualmente lleno, 16/16).
	 * - usamos getLevelForBlock (conceptual 1..16) y lo normalizamos a 16.
	 */
	@Unique
	@Override
	public float getFilledPercentage(World world, BlockPos pos) {
		IBlockState state = world.getBlockState(pos);
		if (!(RealisticFiniteFluidFunctions.getBlock(world, pos, state) instanceof IRealisticFiniteFluid)) return (float)MINIMUM_LEVEL;
		int conceptual = getConceptualVolume(world, pos, null); // 0..16
		if (conceptual < MINIMUM_CONCEPTUAL_LEVEL) return (float)MINIMUM_LEVEL;
		return conceptual / (float)MAXIMUM_CONCEPTUAL_LEVEL; // 1/16 == 0.0625 ... 16/16 == 1.0
	} 
















}
