package com.gatoborrachon.realisticfinitefluids.logic;

import java.util.Random;

import javax.annotation.Nullable;

import com.gatoborrachon.realisticfinitefluids.References;
import com.gatoborrachon.realisticfinitefluids.interfaces.IRealisticFiniteFluid;

import git.jbredwards.fluidlogged_api.api.util.FluidState;
import git.jbredwards.fluidlogged_api.api.util.FluidloggedUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockSnow;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidRegistry;

public class RealisticFiniteFluidFunctions {
	
	
	@Deprecated
	/**
	 * Absorbs all the fluid contained on the current position and eliminates the block  IN A SCALE 1-16
	 * @param world the current World
	 * @param posthe Position of the block
	 * @return all the LEVELs (IN A SCALE 1-16) contained on this block
	 */
	public int conceptualDrain(World world, BlockPos pos)
	{
		world.setBlockToAir(pos);
		return getConceptualVolume(world, pos, null); //world.getBlockState(pos).getValue(LEVEL)+1;
	}
	
	@Deprecated
	/**
	 * Absorbs all the fluid contained on the current position and eliminates the block
	 * @param world the current World
	 * @param posthe Position of the block
	 * @return all the LEVELs contained on this block
	 */
	public int drain(World world, BlockPos pos)
	{
		world.setBlockToAir(pos);
		return getVolume(world, pos, null); //world.getBlockState(pos).getValue(LEVEL);
	}
	
	
	public static int getVolume(IBlockAccess world, BlockPos pos, IBlockState state) {
		if (state != null) {
			if (state.getBlock() instanceof IRealisticFiniteFluid) return state.getValue(References.LEVEL);
		} else if (world != null && pos != null) {
			state = world.getBlockState(pos);
	    	//FluidLogged API Compat
			FluidState fluidState = FluidloggedUtils.getFluidState(world, pos, state);
			if (state.getBlock() instanceof IRealisticFiniteFluid) return world.getBlockState(pos).getValue(References.LEVEL);
			else if (fluidState.getBlock() instanceof IRealisticFiniteFluid) return fluidState.getValue().getValue(References.LEVEL);		}
		return 0;
	}
	
	public static int getConceptualVolume(IBlockAccess world, BlockPos pos, IBlockState state) {
		if (state != null) {
			if (state.getBlock() instanceof IRealisticFiniteFluid) return state.getValue(References.LEVEL)+1;
		} else if (world != null && pos != null) {
			state = world.getBlockState(pos);
	    	//FluidLogged API Compat
			FluidState fluidState = FluidloggedUtils.getFluidState(world, pos, state);
			if (state.getBlock() instanceof IRealisticFiniteFluid) return world.getBlockState(pos).getValue(References.LEVEL)+1;
			else if (fluidState.getBlock() instanceof IRealisticFiniteFluid) return fluidState.getValue().getValue(References.LEVEL)+1;
		}
	
    	
		return 1;  
	}
	
	
	
	public static IBlockState setVolume(World world, BlockPos pos, IBlockState state, int level) {
		//System.out.println("NEWVOLUME: "+level);
		if (world != null && pos != null) {
			state = world.getBlockState(pos);
			if (RealisticFiniteFluidFunctions.getBlock(world, pos, state) instanceof IRealisticFiniteFluid) {
				return state.withProperty(References.LEVEL, level);
			}
		} else if (state != null) {
			if (RealisticFiniteFluidFunctions.getBlock(null, null, state) instanceof IRealisticFiniteFluid) {
				return state.withProperty(References.LEVEL, level);
			}
		}
		return state;
	}
	
	public static IBlockState setConceptualVolume(World world, BlockPos pos, IBlockState state, int level) {
		if (world != null && pos != null) {
			state = world.getBlockState(pos);
			if (RealisticFiniteFluidFunctions.getBlock(world, pos, state) instanceof IRealisticFiniteFluid) {
				return state.withProperty(References.LEVEL, level-1);
			}
		} else if (state != null) {
			if (RealisticFiniteFluidFunctions.getBlock(null, null, state) instanceof IRealisticFiniteFluid) {
				return state.withProperty(References.LEVEL, level-1);
			}
		}
		return state;
	}
	
	public static void setBlockState(World world, @Nullable BlockPos sourcePos, BlockPos destPos, IBlockState state) {
		//world.setBlockState(destPos, state, 3);
		
    	//FluidLogged API Compat
		
		
		/*
		//SEXTA ITERACION
	    System.out.println("[RFF]");
	    //System.out.println("[RFF] ofState: "+FluidState.of(state));
	    //System.out.println("[RFF] getState: "+FluidState.get(world, destPos));
	    
		if (sourcePos != null && FluidloggedUtils.canFluidFlow(world, destPos, state, FiniteFluidLogic.GeneralPurposeLogic.getFacingBetween(sourcePos, destPos))
				|| FluidState.get(world, destPos).getBlock() instanceof IRealisticFiniteFluid) {
			
    	    FluidState newFluidState = FluidState.of(state);
    	    System.out.println("[RFF] ofState: "+FluidState.of(state));
    	    System.out.println("[RFF] getState: "+FluidState.get(world, destPos));
	        FluidloggedUtils.setFluidState(world, destPos, state, newFluidState, false);
		} else {
			world.setBlockState(destPos, state);
		}*/
		
		
		//QUINTA ITERACION 
		//CASI FUNCIONA
		IBlockState posToPlaceBlock = world.getBlockState(destPos);
		if (posToPlaceBlock.getBlock() instanceof BlockAir || posToPlaceBlock.getBlock() instanceof IRealisticFiniteFluid) {
			world.setBlockState(destPos, state);
			return;
		}
		if (sourcePos != null && FluidloggedUtils.canFluidFlow(world, destPos, state, FiniteFluidLogic.GeneralPurposeLogic.getFacingBetween(sourcePos, destPos))) {
    	    FluidState newFluidState = FluidState.of(state);
    	    //System.out.println("[RFF] ofState: "+newFluidState);
    	    //FluidloggedUtils.setFluidState_Internal(world, world.getChunk(destPos), state, destPos, newFluidState, 3);
	        //FluidloggedUtils.setFluidState(world, destPos, state, newFluidState, false);
	        //USAR posToPlaceBlock EN VEZ DE state
    	    FluidloggedUtils.setFluidState(world, destPos, posToPlaceBlock, newFluidState, false);
	        return;
		}
		
		
		
		
		
		
		//CUARTA ITERACION
		/*IBlockState posToPlaceBlock = world.getBlockState(destPos);
        if (sourcePos != null && FluidloggedUtils.canFluidFlow(world, destPos, state, FiniteFluidLogic.GeneralPurposeLogic.getFacingBetween(sourcePos, destPos))){
    	    FluidState newFluidState = FluidState.of(state);
	        FluidloggedUtils.setFluidState(world, destPos, posToPlaceBlock, newFluidState, false);
	        return;
		} else {
			world.setBlockState(destPos, state);
			//return;
		}*/
		
		
		//TERCER ITERACION VALE VERGA
	    /*if (world.getBlockState(destPos).getBlock() instanceof IRealisticFiniteFluid) {
	    	world.setBlockState(destPos, state);
	    } else {
    		FluidState fluidState = FluidloggedUtils.getFluidState(world, destPos);
    	    if (fluidState.getBlock() instanceof IRealisticFiniteFluid) {
    		    FluidState newFluidState = FluidState.of(state);
    	        FluidloggedUtils.setFluidState(world, destPos, state, newFluidState, false);
    	    }
	    }*/
	    
		
		
		
		
		
		//SEGUNDA ITERACION MEDIO FUNCIONA
		/*IBlockState posToPlaceBlock = world.getBlockState(destPos);
        System.out.println(" ");
        System.out.println("[RFF] Pos: "+destPos);
        System.out.println("[RFF] replacleable: "+(posToPlaceBlock.getMaterial().isReplaceable()));
        
		if (posToPlaceBlock.getBlock() instanceof BlockAir || posToPlaceBlock.getBlock() instanceof IRealisticFiniteFluid) {
			world.setBlockState(destPos, state);
			return;
		}
		if (posToPlaceBlock.getMaterial().isReplaceable()) {
    	    FluidState newFluidState = FluidState.of(state);
	        FluidloggedUtils.setFluidState(world, destPos, posToPlaceBlock, newFluidState, false);
	        return;
		}*/
		
		
		
		
		
		
		/* PRIMER ITERACION VALIO VERGA
    	Block newBlockToCheck = FluidState.of(state).getBlock(); //FluidloggedUtils.getFluidState(world, destPos).getBlock();
    	if (state.getBlock() instanceof IRealisticFiniteFluid) {
        	world.setBlockState(destPos, state, 3);
        	return;
    	} else if (newBlockToCheck instanceof IRealisticFiniteFluid) {
    	    //FluidState newFluidState = new FluidState(((BlockFiniteFluid)newBlockToCheck).getFluid(), newState);
    	    //FluidState newFluidState = FluidloggedUtils.
    	    FluidState newFluidState = FluidState.of(state);

	        FluidloggedUtils.setFluidState(world, destPos, state, newFluidState, false);
	        return;
    	}*/
	}
	
	public static Boolean isEntityInsideMaterialForOverlay(IBlockAccess world, BlockPos pos, IBlockState state, Entity entity, double eyeY, Material material) {
		if (!(getBlock(world, pos, state) instanceof IRealisticFiniteFluid)) return null; // null = no se mete en overlay
		if (material != Material.WATER) return null; // solo aplica para agua finita

		// Altura del agua según LEVEL (0-15)
		float level = getConceptualVolume(null, null, state);
		float fluidHeight = level / ((float)References.MAXIMUM_CONCEPTUAL_LEVEL);
		boolean inside = (pos.getY() + fluidHeight) > (eyeY);

		return inside;
	}
	
	public static boolean tryFreezeWater(World world, BlockPos pos, IBlockState state, Random rand) {
		if (!FiniteFluidLogic.waterCanFreeze) 
			return false;
		
		Material blockUpMaterial = world.getBlockState(pos.up()).getMaterial();
		Block block = getBlock(world, pos, world.getBlockState(pos));
		// congela solo si es agua
		if ((block instanceof IRealisticFiniteFluid) && ((IRealisticFiniteFluid)block).getFluid() != FluidRegistry.WATER) {
			return false;
		}


		// Solo aplica si puede ver el cielo
		/*if (!world.canBlockSeeSky(pos)) {
    	        return false;
    	    }*/

		if (blockUpMaterial == Material.SNOW) {
			//BlockFiniteFluid.setBlockState(world, pos.up(), Blocks.AIR.getDefaultState(), 2);
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
		if (RealisticFiniteFluidFunctions.getBlock(world, pos.up(), world.getBlockState(pos.up())) instanceof IRealisticFiniteFluid) {
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

		if (getBlock(world, pos, state) instanceof IRealisticFiniteFluid) {
			int level = getVolume(null, null, state); //state.getValue(BlockFiniteFluid.LEVEL);

			if (level == References.MAXIMUM_LEVEL) {
				// Nivel máximo, congelar en hielo
				world.setBlockState(pos, Blocks.ICE.getDefaultState(), 2);
				return true;
			} /*else if (level == 0) {
				// Nivel 0, desaparece o capa mínima
				world.setBlockState(pos, Blocks.SNOW_LAYER.getDefaultState()
						.withProperty(BlockSnow.LAYERS, 1), 2);
				return true;
			}*/ else {
				// Mapeo LEVEL 0-6 a capas 1-7
				int snowLayers = level+1; //(level + 1) / 2; // Nivel par e impar van al mismo layer
				world.setBlockState(pos, Blocks.SNOW_LAYER.getDefaultState()
						.withProperty(BlockSnow.LAYERS, snowLayers), 2);
				return true;
			}
		}

		return false;
	}
	
	public static boolean isNearHotBlock(World world, BlockPos pos) {
		for (EnumFacing facing : EnumFacing.values()) {
			BlockPos checkPos = pos.offset(facing);
			Material mat = world.getBlockState(checkPos).getMaterial();

			if (mat == Material.FIRE || mat == Material.LAVA) {
				return true;
			}

			//TODO --> Hacerlo compatible con mas bloques, usar algun mapa de bloques calientes dado por: TAN, PrimalCore, Heat and Climate, etc.
			Block block = getBlock(world, checkPos, world.getBlockState(checkPos));
			if (block == Blocks.LIT_PUMPKIN || block == Blocks.TORCH) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean shouldEvap(World world, BlockPos pos, Random rand) {
		Block block = getBlock(world, pos, world.getBlockState(pos));
		if (!FiniteFluidLogic.enableEvaporation) 
			return false;
		
		if (rand.nextInt(FiniteFluidLogic.evaporationChance) != 0)
			return false;

		if (block instanceof IRealisticFiniteFluid && ((IRealisticFiniteFluid)block).getFluid() != FluidRegistry.WATER)
			return false;
		
		if (getVolume(world, pos, null) > References.MINIMUM_LEVEL) 
			return false;
		
		if (world.getBiome(pos).getTemperature(pos) < 0.15F) 
			return false; 

		if (world.isRaining())
			return false;

		//if (!world.canSeeSky(pos))
		//	return false;
		
		//System.out.println("[RFF] Verga: "+pos);
		

		/*else {
            Material belowMaterial = world.getBlockState(pos.down()).getMaterial();
            //return evaporableBlock == Blocks.GRASS ? true : (evaporableBlock == Blocks.DIRT ? true : (evaporableBlock == Blocks.SAND ? true : (evaporableBlock == Blocks.GRAVEL ? true : world.getTopSolidOrLiquidBlock(pos) == pos)));
            return belowMaterial == Material.GROUND || belowMaterial == Material.GRASS || belowMaterial == Material.SAND;
        	}*/
		
		if (block instanceof IRealisticFiniteFluid) {
			world.setBlockToAir(pos);
			return true;
		}
		return false;
	}
	
	public static boolean interactWithLiquid(World world, BlockPos currentPos, BlockPos targetPos) {
		Material currentMaterial = world.getBlockState(currentPos).getMaterial();

		if (currentMaterial == Material.WATER) {
			IBlockState waterState = world.getBlockState(currentPos);
			IBlockState targetState = world.getBlockState(targetPos);

			if (targetState.getMaterial() != Material.LAVA) return false;

			Block waterBlock = getBlock(world, currentPos, waterState);
			Block targetBlock = getBlock(world, targetPos, targetState);

			int waterMeta = waterBlock.getMetaFromState(waterState);
			int targetMeta = targetBlock.getMetaFromState(targetState);

			int waterType = FiniteFluidLogic.GeneralPurposeLogic.getFluidIndex(waterBlock);     
			int targetType = FiniteFluidLogic.GeneralPurposeLogic.getFluidIndex(targetBlock);

			if (waterType < References.MINIMUM_LEVEL || targetType < References.MINIMUM_LEVEL) return false;

			if (targetBlock instanceof IRealisticFiniteFluid) {
				if (waterMeta >= References.Q3_LOW) {

					if (targetMeta < References.Q1_LOW) {
						//world.setBlockToAir(waterPos);
						world.setBlockState(targetPos, Blocks.COBBLESTONE.getDefaultState());
						FiniteFluidLogic.lavaFunctions.triggerLavaMixEffects(world, targetPos);
						return true;
					}

					if (targetMeta < References.Q3_LOW) {
						world.setBlockState(targetPos, Blocks.STONE.getDefaultState());
						//world.setBlockToAir(waterPos);
						FiniteFluidLogic.lavaFunctions.triggerLavaMixEffects(world, targetPos);
						return true;
					}

					if (targetMeta >= References.Q3_LOW) {
						world.setBlockToAir(currentPos);
						world.setBlockState(targetPos, Blocks.OBSIDIAN.getDefaultState());
						FiniteFluidLogic.lavaFunctions.triggerLavaMixEffects(world, targetPos);
						return true;
					}

				} else if (waterMeta > References.Q1_HIGH) {
					if (targetMeta <= References.Q1_LOW) {
						world.setBlockState(targetPos, Blocks.STONE.getDefaultState());
						//world.setBlockToAir(waterPos);
						FiniteFluidLogic.lavaFunctions.triggerLavaMixEffects(world, targetPos);
						return true;
					}

					if (targetMeta > References.Q1_LOW) {
						//world.setBlockToAir(waterPos);
						world.setBlockState(targetPos, Blocks.COBBLESTONE.getDefaultState());
						FiniteFluidLogic.lavaFunctions.triggerLavaMixEffects(world, targetPos);
						return true;
					}
					if (targetMeta >= References.Q3_LOW) {
						world.setBlockToAir(currentPos);
						FiniteFluidLogic.lavaFunctions.triggerLavaMixEffects(world, targetPos);
						return true;
					}

				} else {
					world.setBlockToAir(currentPos);
					FiniteFluidLogic.lavaFunctions.triggerLavaMixEffects(world, targetPos);
					return true;
				}


			} else {
				//vanilla water?
			}
			return false;








		} else if (currentMaterial == Material.LAVA) {
			Block lavaBlock = getBlock(world, currentPos, world.getBlockState(currentPos));
			Block targetBlock = getBlock(world, targetPos, world.getBlockState(targetPos));

			int lavaMeta = lavaBlock.getMetaFromState(world.getBlockState(currentPos));
			int targetMeta = targetBlock.getMetaFromState(world.getBlockState(targetPos));

			int lavaType = FiniteFluidLogic.GeneralPurposeLogic.getFluidIndex(lavaBlock);     
			int targetType = FiniteFluidLogic.GeneralPurposeLogic.getFluidIndex(targetBlock); 

			//a ver si evita un crash aal poner un bloque encima de lava
			if (lavaType < References.MINIMUM_LEVEL || targetType < References.MINIMUM_LEVEL) return false;

			if (isOceanBlock(world, targetPos, null, targetType)) { //targetBlock == FiniteFluidLogic.liquids.get(targetType).oceanBlock) {
				targetMeta = References.MAXIMUM_LEVEL;
			}

			if ("water".equals(FiniteFluidLogic.liquids.get(targetType).name)) {
				// Comparación de altura y nivel para decidir cuál reemplazar
				if (currentPos.getY() <= targetPos.getY() && lavaMeta < targetMeta) {
					targetPos = currentPos;
					//System.out.println("INVERSION ALV");

				}

				if (lavaMeta >= References.Q3_LOW) { //9
					//System.out.println("      ");
					//System.out.println(world.getBlockState(targetPos).getBlock());
					//System.out.println("LAVA META +9:"+lavaMeta);
					//System.out.println("WATER META :"+targetMeta);

					// Condiciones para decidir qué bloque poner
					if (targetMeta <= References.Q1_LOW) { //5
						world.setBlockToAir(targetPos);
						FiniteFluidLogic.lavaFunctions.triggerLavaMixEffects(world, targetPos);
						return true;
					}

					if (targetMeta < References.Q3_LOW) { //10
						world.setBlockState(targetPos, Blocks.COBBLESTONE.getDefaultState());
						FiniteFluidLogic.lavaFunctions.triggerLavaMixEffects(world, targetPos);
						return true;
					}

					if (targetMeta >= References.Q3_LOW) { //9
						world.setBlockState(currentPos, Blocks.OBSIDIAN.getDefaultState());
						FiniteFluidLogic.lavaFunctions.triggerLavaMixEffects(world, targetPos);
						return true;
					}


				} else if (lavaMeta > References.Q1_LOW) { //5
					//System.out.println("      ");
					//System.out.println(world.getBlockState(targetPos).getBlock());
					//System.out.println("LAVA META +5:"+lavaMeta);
					//System.out.println("WATER META :"+targetMeta);

					if (targetMeta < References.Q1_LOW) { //10
						world.setBlockToAir(targetPos);
						FiniteFluidLogic.lavaFunctions.triggerLavaMixEffects(world, targetPos);
						return true;
					}

					if (targetMeta < References.Q3_LOW) { //9
						world.setBlockState(targetPos, Blocks.COBBLESTONE.getDefaultState());
						FiniteFluidLogic.lavaFunctions.triggerLavaMixEffects(world, targetPos);
						return true;
					}

					if (targetMeta >= References.Q3_LOW) {
						world.setBlockState(targetPos, Blocks.STONE.getDefaultState());
						FiniteFluidLogic.lavaFunctions.triggerLavaMixEffects(world, targetPos);
						return true;
					}


				} else {
					//System.out.println("      ");
					//System.out.println(world.getBlockState(targetPos).getBlock());
					//System.out.println("LAVA META -5:"+lavaMeta);
					//System.out.println("WATER META :"+targetMeta);

					// Condiciones para decidir qué bloque poner
					if (targetMeta < References.Q1_LOW) { //5
						world.setBlockToAir(targetPos);
						FiniteFluidLogic.lavaFunctions.triggerLavaMixEffects(world, targetPos);
						return true;
					}

					if (targetMeta < References.Q3_LOW) { //10
						world.setBlockState(targetPos, Blocks.COBBLESTONE.getDefaultState());
						//world.setBlockToAir(lavaPos);
						FiniteFluidLogic.lavaFunctions.triggerLavaMixEffects(world, targetPos);
						return true;
					}

					if (targetMeta >= References.Q3_LOW) { //9
						world.setBlockToAir(currentPos);
						world.setBlockState(targetPos, Blocks.STONE.getDefaultState());
						FiniteFluidLogic.lavaFunctions.triggerLavaMixEffects(world, targetPos);
						return true;
					}
				}
			}

			return false;
		}
		return false;
	}
	
	public static boolean shouldSearchOutward(Material material) {
		return material == Material.LAVA ? false : true;
	}
	
	/**
	 * Calcula si un fluido (liquido) deberia moverse hacia un vecino siguiendo lo siguiente:
	 * <p>
	 * 1.1.- Primero obtiene el LEVEL de cada posicion <br>
	 * 1.2.- Si el LEVEL de origen (menos 1) es mayor al LEVEL de destino <br>
	 * 1.3.- RESULTADO --> SI FLUIMOS <br>
	 * Ejemplo: LEVEL de 5 deberia moverse a un fluido con LEVEL 3 (5-1 > 3), pero un LEVEL de 4 no (4-1 !> 3)
	 * <p>
	 * ELSE <p>
	 * 2.- Sacamos un LEVEL efectivo de cada posicion mediante la funcion calculateNeighborWaterLevel(),
	 * la cual nos regresa un promedio de LEVELs de los 8 bloques adyacentes tanto al Source como al Dest Block, (sin contar a ellos mismos en la suma). <br>
	 * (Esto basicamente sirve para comparar el total de LEVELs adyancentes a cada fluido, como un parametro de comparacion del LEVEL de bloques inmediatamente adyacentes) <br>
	 * <p>
	 * 2.1.- PRIMER COMPARACION --> Si el EffectiveSourceLevel +1 es distinto al SourceLevel Original 
	 * (Esto significa, si de forma global, hay una cantidad de liquido distinta en promedio en la periferia de este SourceBlock, SI DEBERIAMOS FLUIR) <br>
	 * 
	 * 2.2.- SEGUNDA COMPARACION --> Si el EffectiveDestLevel + 1 es distinto al DestLevel original AND Si el EffectiveSourceLevel menos 0.8 sigue siendo mayor al EffectiveDestLevel 
	 * (Esto significa, si en la periferia inmediata del DestBlock hay una cantidad distinta de volumen en comparacion al DestLevel, Y SI APARTE hay mas liquido adyacente al SourceBlock que en el DestBlock --> NOS MOVEMOS)
	 * 
	 */
	public static boolean shouldFlowToNeighbor(IBlockAccess world, BlockPos sourcePos, BlockPos destPos) {
		int sourceLevel = getConceptualVolume(world, sourcePos, null); //world.getBlockState(posFrom).getValue(LEVEL);
		int destLevel = getConceptualVolume(world, destPos, null); //world.getBlockState(posTo).getValue(LEVEL);

		if (sourceLevel - 1 > destLevel)
		{
			return true;
		}
		else
		{
			float effectiveSourceLevel = FiniteFluidLogic.GeneralPurposeLogic.calculateNeighborWaterLevel(world, sourcePos, destPos);            
			float effectiveDestLevel = FiniteFluidLogic.GeneralPurposeLogic.calculateNeighborWaterLevel(world, destPos, sourcePos);
			return (effectiveSourceLevel + 1.0F != (float)sourceLevel ||
					effectiveDestLevel + 1.0F != (float)destLevel) && effectiveSourceLevel - 0.8F > effectiveDestLevel;
		}
	}
	
	/**
	 * Returns the Flow Vector calculated for the block at the current pos.
	 * @param world
	 * @param pos
	 * @param fluidRequest If this flow vector was requested by a fluid or by another things (ej. a WaterWheel)
	 * @return
	 */
	public static Vec3d calculateFlowVector(IBlockAccess world, BlockPos pos, boolean fluidRequest) {
		Vec3d flow = new Vec3d(0,0,0);
		for(EnumFacing dir : EnumFacing.Plane.HORIZONTAL) {
			BlockPos neighborPos = pos.offset(dir);
			//if (!(world.getBlockState(neighbor).getBlock() instanceof IRealisticFiniteFluid)) return flow;
		    if ( !(getBlock(world, neighborPos, world.getBlockState(neighborPos)) instanceof IRealisticFiniteFluid) ) continue; // NO return
		    
			int levelNeighbor = getVolume(world, neighborPos, null);
			int levelCurrent = getVolume(world, pos, null);

			//int diff = fluidRequest ? levelCurrent - levelNeighbor : (levelCurrent - levelNeighbor)*2;
			//int diff = levelNeighbor - levelCurrent;
			int diff = levelCurrent - levelNeighbor;
			//System.out.println("diff: "+diff);
			
			if (!fluidRequest) diff = diff*3; //TODO Make this configurable, its the strengt at where some waterwheels will produce power

			flow = flow.add(/*.addVector(*/
					dir.getXOffset()/*.getFrontOffsetX()*/ * diff, 
					0, 
					dir.getZOffset()/*getFrontOffsetZ()*/ * diff
					);

			if (flow.length()/*.lengthVector()*/ > 0) 
				flow = flow.normalize();
			//System.out.println("flow: "+flow);

		}
		return flow;
	}
	
	public static BlockPos getPositionOnGravityDirection(BlockPos originalPos) {
		return new BlockPos(originalPos.getX(), originalPos.getY() + FiniteFluidLogic.GeneralPurposeLogic.getFluidGravity(), originalPos.getZ());
	}
	
	public static boolean isOceanBlock(@Nullable IBlockAccess world, @Nullable BlockPos pos, @Nullable IBlockState state, int fluidType) {
		//TODO add an exception when we try to use a metadata value above 15 (in that case, we should not use this system)
		//if MAXIMUM_LEVEL == 15 --> ABORT CALCULATIONS (or let the game crash)
		if (fluidType <= -1) return false;
		if (world != null && pos != null) {
			state = world.getBlockState(pos);
			if (getBlock(world, pos, state) instanceof IRealisticFiniteFluid  && FiniteFluidLogic.GeneralPurposeLogic.getFluidIndex(getBlock(world, pos, state)) == fluidType) return 
					//state.getBlock() == FiniteFluidLogic.liquids.get(fluidType).stillBlock && 
					getVolume(world, pos, state) > References.MAXIMUM_LEVEL;
		} else if (state != null) {
			if (getBlock(null, null, state) instanceof IRealisticFiniteFluid && FiniteFluidLogic.GeneralPurposeLogic.getFluidIndex(getBlock(null, null, state)) == fluidType) return 
					//state.getBlock() == FiniteFluidLogic.liquids.get(fluidType).stillBlock &&
					getVolume(world, pos, state) > References.MAXIMUM_LEVEL;
		}
		return false;
	}
	
	
	
	
	
	
	
	
	public static void onEntityCollision(World worldIn, BlockPos pos, IBlockState state, Entity entityIn) {
		if (FiniteFluidLogic.GeneralPurposeLogic.canMove(worldIn, pos, getVolume(worldIn, pos, state) )) {
			if (entityIn instanceof EntityPlayer && ((EntityPlayer)entityIn).isCreative() && !FiniteFluidLogic.flowingWaterShouldMoveCreativePlayer) return;

			Vec3d flow = calculateFlowVector(worldIn, pos, true);
             
            	double strength = 0.014D;
            	entityIn.motionX += -flow.x * strength;
            	entityIn.motionY += -flow.y * strength;
            	entityIn.motionZ += -flow.z * strength;
		}
	}
	
	
	public static void onBucketItemUse(EntityPlayer player, World worldIn, BlockPos pos, 
			EnumFacing facing, Block containedBlock, int localFluidIndex) {
		//ItemStack stack = player.getHeldItem(hand);
		//System.out.println("VERGA2");

		// Asegúrate de que estés usando tu cubeta con agua/lava finita
		//if (stack.getItem() == ModItems.FINITE_WATER_BUCKET) {
			BlockPos targetPos = pos;//.offset(facing);

			IBlockState targetBlockState = worldIn.getBlockState(targetPos);
			Block targetBlock = getBlock(worldIn, targetPos, targetBlockState);
            //int fluisdIndex = this.localFluidIndex; //FiniteFluidLogic.GeneralPurposeLogic.getFluidIndex(FiniteFluidLogic.liquids.get(this.localFluidIndex).flowingBlock);
            IRealisticFiniteFluid realisticFluid = ((IRealisticFiniteFluid)FiniteFluidLogic.liquids.get(FiniteFluidLogic.onFiniteFluidIndex).flowingBlock);

			// Permitir colocar solo si es aire o es tu propio bloque finito
			if (worldIn.isAirBlock(targetPos) || targetBlock instanceof IRealisticFiniteFluid) {
				if (!worldIn.isRemote) {
					//System.out.println(worldIn.getBlockState(targetPos).getMaterial() == Material.WATER);

					if (targetBlock instanceof IRealisticFiniteFluid) {
						realisticFluid = (IRealisticFiniteFluid)targetBlock;
						//System.out.println("[RFF] getFluidRegistry(targetBlock): "+getFluidRegistry(targetBlock));
						
						//System.out.println("[RFF] this.containedBlock: "+containedBlock);
						//System.out.println("[RFF] getFluidRegistry(this.containedBlock): "+getFluidRegistry(containedBlock));
						if (getFluidRegistry(targetBlock) == getFluidRegistry(containedBlock)) {
							if (realisticFluid.getConceptualVolume(worldIn, targetPos, targetBlockState) == References.MAXIMUM_CONCEPTUAL_LEVEL) {
								//if ((worldIn.getBlockState(targetPos.up()).getBlock().hasTileEntity()) && targetBlock.isReplaceable(worldIn, targetPos)) {
								if (!(getBlock(worldIn, targetPos.up(), worldIn.getBlockState(targetPos.up())).hasTileEntity()) && targetBlock.isReplaceable(worldIn, targetPos.up())) {
									worldIn.setBlockState(targetPos.up(), FiniteFluidLogic.liquids.get(localFluidIndex).flowingBlock.getDefaultState()); 
									return;
								} else 
									return; //Because, if the current block is full and you can't place above it, then you should not place the fluid no matter what
							}
							else { // Lógica de distribución equitativa
								FiniteFluidLogic.FluidWorldInteraction.distributeFluidEquallyForBuckets(worldIn, targetPos, References.MAXIMUM_CONCEPTUAL_LEVEL, localFluidIndex);
								return;
							}
						} else if (targetBlockState.getMaterial() != containedBlock.getDefaultState().getMaterial()) { //have not same fluidRegistry --> 
							realisticFluid = (IRealisticFiniteFluid)targetBlock;
							worldIn.playSound(player, pos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.5F, 2.6F + (worldIn.rand.nextFloat() - worldIn.rand.nextFloat()) * 0.8F);
							realisticFluid.setBlockState(worldIn, player.getPosition(), targetPos, FiniteFluidLogic.liquids.get(localFluidIndex).flowingBlock.getDefaultState());
							return;
						}
					} else {
						realisticFluid.setBlockState(worldIn, player.getPosition(), targetPos, realisticFluid.setVolume(null, null, FiniteFluidLogic.liquids.get(localFluidIndex).flowingBlock.getDefaultState(), References.MAXIMUM_LEVEL));
						return;
					}
					//worldIn.playSound(player, pos, SoundEvents.ITEM_BUCKET_EMPTY, SoundCategory.BLOCKS, 1.0F, 1.0F);
				}
				//worldIn.playSound(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
				//		SoundEvents.ITEM_BUCKET_EMPTY, SoundCategory.BLOCKS, 1.0F, 1.0F, false);

				/*if (!player.capabilities.isCreativeMode) {
					// Vaciar la cubeta
					player.setHeldItem(hand, new ItemStack(Items.BUCKET));
				}*/

				//return EnumActionResult.SUCCESS;
			} /*else {
				return EnumActionResult.FAIL;
			}*/
		//}

		//return super.onItemUse(player, worldIn, pos, hand, facing, hitX, hitY, hitZ);
	}
	
	public static String getFluidRegistry(Block block) {
		return FiniteFluidLogic.liquids.get(FiniteFluidLogic.GeneralPurposeLogic.getFluidIndex(block)).name;
	}
	
	
	
	
	
	
	
	
	
	
	
	public static IBlockState getStateAtViewpoint(IBlockState state, IBlockAccess world, BlockPos pos, Vec3d viewpoint) {
		// Si la cámara está por debajo de la “superficie” (LEVEL/16), seguimos “dentro” del fluido.
		double level = getConceptualVolume(world, pos, state); //state.getValue(LEVEL) + 1;     // 1..16
		double surfaceY = pos.getY() + (level / (double)References.MAXIMUM_CONCEPTUAL_LEVEL);

		//System.out.println("viewpoint.y: "+viewpoint.y); //
		//System.out.println("Current level: "+level); //
		//System.out.println("Block Pos Y: "+pos.getY()); //
		//System.out.println("LEVEL/MaxLevel: "+(level / (double)References.MAXIMUM_CONCEPTUAL_LEVEL)); //
		//System.out.println("surfaceY: "+surfaceY); //
		//System.out.println(""); //
		if (viewpoint.y < surfaceY - References.EPS) {
			return state; // seguimos “dentro” del fluido
		}
		//boolean cato = (viewpoint.y < surfaceY - References.EPS);
		//System.out.println("viewpoint.y < surfaceY - References.EPS"+cato); //
		//return Blocks.WATER.getDefaultState();
		return Blocks.AIR.getDefaultState(); // por encima: aire (vanilla hará el FOV normal)
	}
	
	
	
	public static Vec3d getFogColor(World world, BlockPos pos, IBlockState state, Entity entity, Vec3d originalColor,
			float partialTicks) {
		
			double level = getConceptualVolume(world, pos, state); //state.getValue(LEVEL)+1;
			Vec3d cam = ActiveRenderInfo.projectViewFromEntity(entity, partialTicks);
			double surfaceY = ((double)pos.getY()) + (level / (double)References.MAXIMUM_CONCEPTUAL_LEVEL);
			
			// Solo pintamos niebla si la cámara está DENTRO del fluido
			//return getFogColorBlock(world, pos, state, entity, originalColor, partialTicks);
			return (cam.y < surfaceY - References.EPS) ? getFogColorBlock(world, pos, state, entity, originalColor, partialTicks) : originalColor;
	}
	
	
	/**
	 * {@link Block#getFogColor(World, BlockPos, IBlockState, Entity, Vec3d, float)}
	 */
    public static Vec3d getFogColorBlock(World world, BlockPos pos, IBlockState state, Entity entity, Vec3d originalColor, float partialTicks)
    {
        if (state.getMaterial() == Material.WATER)
        {
            float f12 = 0.0F;

            if (entity instanceof net.minecraft.entity.EntityLivingBase)
            {
                net.minecraft.entity.EntityLivingBase ent = (net.minecraft.entity.EntityLivingBase)entity;
                f12 = (float)net.minecraft.enchantment.EnchantmentHelper.getRespirationModifier(ent) * 0.2F;

                if (ent.isPotionActive(net.minecraft.init.MobEffects.WATER_BREATHING))
                {
                    f12 = f12 * 0.3F + 0.6F;
                }
            }
            return new Vec3d(0.02F + f12, 0.02F + f12, 0.2F + f12);
        }
        else if (state.getMaterial() == Material.LAVA)
        {
            return new Vec3d(0.6F, 0.1F, 0.0F);
        }
        return originalColor;
    }
    
    public static Block getBlock(@Nullable IBlockAccess world, @Nullable BlockPos pos, IBlockState state) {
    	//if (state.getBlock() instanceof IRealisticFiniteFluid) return state.getBlock();

    	if (world != null && pos != null) {
    		FluidState fluidState = FluidState.get(world, pos); //FluidloggedUtils.getFluidState( state);
    	/*if (state.getBlock() instanceof BlockFence) {
			System.out.println("[RFF]");
			System.out.println("[RFF] state: "+state.toString());
			System.out.println("[RFF] fluidState: "+fluidState.toString());
			System.out.println("[RFF] isFluidloggable: "+fluidState.isFluidloggable());
			System.out.println("[RFF] fluidState.getBlock(): "+fluidState.getBlock());
		}*/
    		if (//fluidState.isFluidloggable() ||
    				fluidState.getBlock() instanceof IRealisticFiniteFluid) return fluidState.getBlock();
    	}
		return state.getBlock();
    	
    }
	
	
	
}
