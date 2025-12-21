package com.gatoborrachon.realisticfinitefluids.logic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Set;

import javax.annotation.Nullable;

import com.gatoborrachon.realisticfinitefluids.References;
import com.gatoborrachon.realisticfinitefluids.init.ModConfig;
import com.gatoborrachon.realisticfinitefluids.interfaces.IRealisticFiniteFluid;

import git.jbredwards.fluidlogged_api.api.util.FluidState;
import git.jbredwards.fluidlogged_api.api.util.FluidloggedUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.block.BlockFarmland;
import net.minecraft.block.BlockGrassPath;
import net.minecraft.block.BlockIce;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.BlockPistonExtension;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;


public class FiniteFluidLogic {
    //public static int humidity;
    //public static ArrayList<NewFluidType> liquids = new ArrayList<NewFluidType>();
    public static Map<Integer, NewFluidType> liquids = new HashMap<>();
    public static Map<String, Integer> fluidIndexMap = new HashMap<>();
    public static Map<Block, Integer> blockToFluidIndex = new HashMap<>();
    
    public static int onFiniteFluidIndex;
    public static boolean doPressure; //true
    //public static int pressureDelay = 0;
    public static int lakeLimit; //512;
    public static int pressureLimit;
    public static int maxCalc; //1024;
    public static int playerMaxDistanceToCalc;// 1024;
    public static int waterTick;// 4; //20 = tests -- 3 = real
    public static int lavaTick; // 24; //previo: 20 (4*5, ahora es 4*6)
    public static int grabAmt;
    public static boolean shouldTickRandomly;
    public static boolean enableRain;
    //public static int rainAmount = ModConfig.rainAmount; // 24; //Origianl era 12
    //public static int rainArea = ModConfig.rainArea; // 32; //Origianl era 16
    //public static boolean scalableRainMethod = ModConfig.scalableRainMethod;
    public static float rainNewMethodAmount; //ModConfig.rainNewMethodAmount;
    public static int evaporationChance; //ModConfig.evaporationChance; // 100; //100 originalmente, yo creo que 30 ha de ser bueno? no se xd, 100 --> 1%, 1 --> 100%, 2 --> 50%
    public static boolean enableEvaporation;// true;
    
    public static boolean bucketRemoveLowFluid;
    public static boolean debug;
    public static boolean waterCanFreeze;
    public static int waterLightOpacity;
    public static boolean flowingWaterShouldMoveCreativePlayer;
    
    public static boolean shouldFluidsBeInfinite;
    
    public static boolean createBlocksForBlocklessFluids;
    
    
    private static boolean smallOceanSearch;
    private static boolean stopPCheck;
    
    public static BlockPos actualFlowPos;

    
    public static ArrayList<BlockPos> pressure = new ArrayList<BlockPos>();
    public static ArrayList<BlockPos> rpressure = new ArrayList<BlockPos>();
    
    private static int calcAmt;
    
    public FiniteFluidLogic() {  
        onFiniteFluidIndex = 0;
    }
    
    public static void initFiniteFluidVariables() {
        doPressure = ModConfig.doPressure; //true
        lakeLimit = ModConfig.lakelimit; //512;
        pressureLimit = 256;
        maxCalc = ModConfig.maxCalc; //1024;
        playerMaxDistanceToCalc = ModConfig.playerMaxDistanceToCalc;// 1024;
        waterTick = ModConfig.waterTickRate;// 4; //20 = tests -- 3 = real
        lavaTick = ModConfig.lavaTickRate; // 24; //previo: 20 (4*5, ahora es 4*6)
        grabAmt = 7;
        shouldTickRandomly = ModConfig.shouldTickRandomly;
        enableRain = ModConfig.enableRain;// true;
        rainNewMethodAmount = ModConfig.rainAmount; //ModConfig.rainNewMethodAmount;
        evaporationChance = ModConfig.evaporationChance; // 100; //100 originalmente, yo creo que 30 ha de ser bueno? no se xd, 100 --> 1%, 1 --> 100%, 2 --> 50%
        enableEvaporation = ModConfig.enableEvaporation;// true;
        smallOceanSearch = false;
        
        bucketRemoveLowFluid = ModConfig.bucketRemoveLowFluid;
        debug = ModConfig.debug;
        waterCanFreeze = ModConfig.waterCanFreeze;
        waterLightOpacity = ModConfig.waterLightOpacity;
        flowingWaterShouldMoveCreativePlayer = ModConfig.flowingWaterShouldMoveCreativePlayer;
        
        shouldFluidsBeInfinite = ModConfig.shouldFluidsBeInfinite;
        
        createBlocksForBlocklessFluids = ModConfig.createBlocksForBlocklessFluids;
        
		//System.out.println("FiniteFluidLogic.shouldTickRandomly: "+FiniteFluidLogic.shouldTickRandomly);
		//System.out.println("ModConfig.shouldTickRandomly: "+ModConfig.shouldTickRandomly);
        
    }
    
    static {
        pressure = new ArrayList<BlockPos>();
        rpressure = new ArrayList<BlockPos>();
    }
    
    
    public static void clearLiquidLists() {
        liquids.clear();
        fluidIndexMap.clear();
        blockToFluidIndex.clear();
    }
    
    
    
    
    
    
    
    
    public static class FiniteFluidsLogic {

		public static boolean tryLiquidMove(World world, BlockPos pos) {
			////BlockPos newpos = new BlockPos(24, 4, -108);
			////IBlockState newstate = world.getBlockState(newpos);
			//FluidState fluidstate = FluidState.of(newstate);
			////System.out.println("[RFF] block: "+newstate.getBlock());
			////System.out.println("[RFF] fluidLoggedState: "+FluidState.get(world, newpos));
			//System.out.println("[RFF] canFluidOccupy: "+FluidloggedUtils.canFluidOccupy(newstate, world, newpos, fluidstate)); //.getFluidFromState(newstate)); //.isStateFluidloggable(newstate, world, newpos, fluidstate));
			
		    if (world.isRemote) return false;
		
		    Random rand = new Random();
		    int dx = rand.nextBoolean() ? 1 : -1;
		    int dz = rand.nextBoolean() ? 1 : -1;
		    boolean flip = rand.nextBoolean();
		
		    BlockPos below = pos.down(-GeneralPurposeLogic.getFluidGravity());
		
		    if (liquidMove(world, pos, below, true)) return true;
		
		    if (flip) {
		        if (liquidMove(world, pos, pos.add(-dx, GeneralPurposeLogic.getFluidGravity(), 0), true)) return true;
		        if (liquidMove(world, pos, pos.add(dx, GeneralPurposeLogic.getFluidGravity(), 0), true)) return true;
		        if (liquidMove(world, pos, pos.add(0, GeneralPurposeLogic.getFluidGravity(), -dz), true)) return true;
		        if (liquidMove(world, pos, pos.add(0, GeneralPurposeLogic.getFluidGravity(), dz), true)) return true;
		        if (liquidMove(world, pos, pos.add(-dx, 0, 0), true)) return true;
		        if (liquidMove(world, pos, pos.add(dx, 0, 0), true)) return true;
		        if (liquidMove(world, pos, pos.add(0, 0, -dz), true)) return true;
		        if (liquidMove(world, pos, pos.add(0, 0, dz), true)) return true;
		    } else {
		        if (liquidMove(world, pos, pos.add(0, GeneralPurposeLogic.getFluidGravity(), -dz), true)) return true;
		        if (liquidMove(world, pos, pos.add(0, GeneralPurposeLogic.getFluidGravity(), dz), true)) return true;
		        if (liquidMove(world, pos, pos.add(-dx, GeneralPurposeLogic.getFluidGravity(), 0), true)) return true;
		        if (liquidMove(world, pos, pos.add(dx, GeneralPurposeLogic.getFluidGravity(), 0), true)) return true;
		        if (liquidMove(world, pos, pos.add(0, 0, -dz), true)) return true;
		        if (liquidMove(world, pos, pos.add(0, 0, dz), true)) return true;
		        if (liquidMove(world, pos, pos.add(-dx, 0, 0), true)) return true;
		        if (liquidMove(world, pos, pos.add(dx, 0, 0), true)) return true;
		    }

		
		
		    return false;
		}
		
		
		
		public static boolean liquidMove(World world, BlockPos sourcePos, BlockPos destPos, boolean doMove) {
		    return FiniteFluidsLogic.liquidMove(world, sourcePos, destPos, doMove, 0);
		}

		private static boolean liquidMove(World world, BlockPos sourcePos, BlockPos destPos, boolean doMove, int recursionDepth) { 
		    if (world.isRemote) {
		    	//System.out.println("[RFF] Return 1"); 
		    return false;
		    }
		    
		    // 1) OBTENEMOS BLOQUES Y STATES TANTO DEL SOURCE COMO DEL DESTINY
		    IBlockState sourceState = world.getBlockState(sourcePos);
		    Block sourceBlock = RealisticFiniteFluidFunctions.getBlock(world, sourcePos, sourceState);
		    if (!(sourceBlock instanceof IRealisticFiniteFluid)) { 
		    	//System.out.println("[RFF] Return 2 FALSE");  
		    	return false; 
		    	}//CHECAR QUE ESTO NO ROMPA EL FUNCIONAMIENTO DEL AGUA //??
			IRealisticFiniteFluid realisticFluid = ((IRealisticFiniteFluid)sourceBlock);
		    int sourceLevel = realisticFluid.getVolume(world, sourcePos, sourceState);
		
		    IBlockState destState = world.getBlockState(destPos);
		    Block destBlock = RealisticFiniteFluidFunctions.getBlock(world, destPos, destState);
		    int destLevel = destBlock instanceof IRealisticFiniteFluid ? realisticFluid.getVolume(world, destPos, destState) : -1;
		
		    // 2) SETEAMOS EL FLUID INDEX 
		    GeneralPurposeLogic.setCurrentFluidIndex(sourceBlock);
		    NewFluidType fluid = (NewFluidType) liquids.get(onFiniteFluidIndex);
		    //System.out.println("[RFF] destBlock: "+destBlock);
		
		    
		    // 3) MANEJAMOS RECURSION PARA BUSCAR EN LIQUIDOS ADYANCETES, DEPENDE DEL MATERIAL DEL SOURCE BLOCK
		    boolean shouldSearchOutward = false;
		    if (fluid.flowingBlock instanceof IRealisticFiniteFluid) {
		        shouldSearchOutward = ((IRealisticFiniteFluid) fluid.flowingBlock).shouldSearchOutward(fluid.flowingBlock.getDefaultState().getMaterial());
		    }
		
		    // #######################
		    //        RECURSION
		    // #######################
		    //SI PODEMOS HACER BUSQUEDAS ADYACENTES ADICIONALES, Y LA PROFUNDIDAD DE RECURSION ES MENOR A 32 -->
		    if (shouldSearchOutward && recursionDepth < 32) {
		    	//CHECAMOS EN LOS ENUMFACING HORIZONTALES DEL BLOQUE DE DEST BLOCK
		        for (EnumFacing dir : EnumFacing.HORIZONTALS) {
		            BlockPos neighbor = destPos.offset(dir);
		            //CHECAMOS LOS VECINOS SIEMPRE Y CUANDO SEAN FLUIDO FINITO DEL MISMO TIPO PERO NO SEAN EL SOURCE BLOCK
		            if (!neighbor.equals(sourcePos) && GeneralPurposeLogic.isFiniteFluid(world, neighbor)) {
		            	//LE SACAMOS EL VOLUMEN
		                int neighborLevel = realisticFluid.getVolume(world, neighbor, world.getBlockState(neighbor));
		                //Y SI EL BLOQUE DESTINO TIENE MAS LIQUIDO QUE UNO DE ESTOS BLOQUES ADDYACENTES --> 
		                //REINICIAMOS LA FUNCION, TOMANDO COMO DESTINO ESTE BLOQUE VECINO
		                if (destLevel > neighborLevel) {
		                	//System.out.println("[RFF] Return 3 "+recursionDepth);
		                    return liquidMove(world, sourcePos, neighbor, doMove, recursionDepth++);
		                }
		            }
		        }
		
		    // SI NO PODEMOS HACER RECURSION --> CHECAMOS QUE EL BLOQUE DESTINO SEA BLOQUE FINITO
		    } else if (!GeneralPurposeLogic.isFiniteFluid(world, destPos)) {
		        destLevel = -1;
		    }
		    
		    // REDUCIR DEUDA DE CALCULOS
		    //Es la diferencia entre la altura de nuestro bloque contra el que estamos comparando
		    //Idealmente tendria que salir un resultado positivo
		    int dy = sourcePos.getY() - destPos.getY(); 
		    if (doMove && dy == 0 && Math.abs(destLevel - sourceLevel) < 3 && GeneralPurposeLogic.getCalc() > maxCalc * 0.6f) {
		        --calcAmt;
		    	//System.out.println("[RFF] Return 4 TRUE");
		        return true;
		    }
		
		    
		    /**
		     * TODO --> CREO QUE ACA ESTA EL PROBLEMA DE PORQUE LOS BLOQUES DESAPARECEN
		     * NO ESTAN DETECTANDO A SUS BLOQUES HERMANOS,
		     * UN BLOQUE FLOWING QUE CAE DE LADO HACIA ABAJO, SE IMPONE AUNQUE SU HERMANO ESTE EXACTAMENTE DEBAJO
		     */
		    
		    // #######################
		    //   CALCULOS VERTICALES
		    // #######################
		    //MOVIMIENTO NETO HACIA ABAJO
		    if (dy == -GeneralPurposeLogic.getFluidGravity()) {
		    	//SI NOS PODEMOS MOVER --> 
		        if (GeneralPurposeLogic.canMoveInto(world, destPos, sourcePos, sourceLevel, fluid)) {
		        	//EJECUTAMOS EL MOVIMIENTO + tryGrab ????? NO SE QUE HAGA
		            if (doMove) {
		            	realisticFluid.setBlockState(world, sourcePos, destPos, realisticFluid.setVolume(null, null, fluid.flowingBlock.getDefaultState(), sourceLevel));
		            	FiniteFluidsLogic.tryGrab(world, sourcePos, destPos, 0, fluid);
		            }
		        	//System.out.println("[RFF] Return 5 TRUE");
		            return true;
		        }
		        
			    // #########################
			    //   ECUALIZACION VERTICAL
			    // #########################
		        // SI EL DESTINO ES BLOQUE FLUIDO DEL MISMO TIPO
		        // Y TIENE ESPACIO PARA ALMACENAR AGUA
		        if (GeneralPurposeLogic.isFiniteFluid(world, destPos) && destLevel < References.MAXIMUM_LEVEL) {
		            if (doMove) {
		            	//ECUALIZACION VERTICAL -->
		            	//Convertimos los LEVELs literales (0-7) en LEVELs conceptuales (1-8) para calculos precisos evitando
		            	//problemas al usar el 0
		            		int realSource = sourceLevel + 1;
		                	int realDest   = destLevel + 1;
		                	//OBTENEMOS UN VALOR PARA TRANSFERIR AL DEST BLOCK
		                	int transfer = Math.min(References.MAXIMUM_LEVEL - destLevel, realSource);
		
		                	//REALIZAMOS LA TRANSFERENCIA
		                	realSource -= transfer;
		                	realDest   += transfer;
		
		                	//REGRESAMOS AL SISTEMA DE LEVELs LITERALES
		                	sourceLevel = realSource - 1;
		                	destLevel   = realDest   - 1;
		
		                	//SI EL LEVEL RESTANTE ES MAYOR O IGUAL AL MINIMUM_LEVEL
		                	//Esto significa, si el LEVEL del origen todavia da para que exista el bloque, seteamos este LEVEL en el bloque
		                if (sourceLevel >= References.MINIMUM_LEVEL)
		                	realisticFluid.setBlockState(world, null, sourcePos, realisticFluid.setVolume(null, null, fluid.flowingBlock.getDefaultState(), sourceLevel));
		                else //SI NO ALCANZO EL LEVEL DE ORIGEN PARA QUE EXISTA EL SOURCE BLOCK --> tryGrab???
		                    FiniteFluidsLogic.tryGrab(world, sourcePos, destPos, 0, fluid);
		
		                //SETEAMOS UN BLOQUE DE FLUIDO EN EL DEST POSITION
		                realisticFluid.setBlockState(world, sourcePos, destPos, realisticFluid.setVolume(null, null, fluid.flowingBlock.getDefaultState(), destLevel));
		
		                //AÑADIMOS EL BLOQUE AL SISTEMA DE PRESION DIRECTA ASI TIENE EL LEVEL MAXIMO
		                if (doPressure && destLevel == References.MAXIMUM_LEVEL)
		                	PressureSystemLogic.addToPressure(world, destPos, false);
		            }
		        	//System.out.println("[RFF] Return 6 TRUE");
		            return true;
		        }
		    	//System.out.println("[RFF] Return 7 FALSE");
		        //SI NO NOS PODIAMOS MOVER, NI REALIZAMOS ECUALIZACION --> RETURN FALSE;
		        return false;
		    }
			
		    // #########################
		    //   CALCULOS HORIZONTALES
		    // #########################
		    //SI EL BLOQUE DE DESTINO ES UN BLOQUE DE FLUIDO FINITO
		    //if (FluidloggedUtils.canFluidFlow(world, destPos, destState, GeneralPurposeLogic.getFacingBetween(sourcePos, destPos)) && destBlock != Blocks.AIR)
		    if (GeneralPurposeLogic.isFiniteFluid(world, destPos)) {
		        // SI DEBERIAMOS MOVERNOS 
		    	// + EL DEST LEVEL TIENE MENOS FLUIDO DEL MAXIMO PERMITIDO 
		    	// + EL SOURCE LEVEL TIENE MAS FLUIDO DEL MINIMO PERMITIDO
		        if (GeneralPurposeLogic.shouldFlowToNeighbor(world, sourcePos, destPos) && destLevel < References.MAXIMUM_LEVEL && sourceLevel > References.MINIMUM_LEVEL) {
		            if (doMove) {
						// ###########################
						//   ECUALIZACION HORIZONTAL
						// ###########################
		            	int total = sourceLevel + destLevel + 2; //Convertido a LEVELs conceptuales
		                sourceLevel = total / 2;
		                destLevel = total - sourceLevel - 1;
		                --sourceLevel;
		                
			            //SI EL LEVEL RESTANTE ES MAYOR O IGUAL AL MINIMUM_LEVEL
	                	//Esto significa, si el LEVEL del origen todavia da para que exista el bloque, seteamos este LEVEL en el bloque
		                if (sourceLevel >= References.MINIMUM_LEVEL)
		                	realisticFluid.setBlockState(world, null, sourcePos, realisticFluid.setVolume(null, null, fluid.flowingBlock.getDefaultState(), sourceLevel));
		                 else //SI NO ALCANZO EL LEVEL DE ORIGEN PARA QUE EXISTA EL SOURCE BLOCK --> tryGrab???
		                	FiniteFluidsLogic.tryGrab(world, sourcePos, destPos, 0, fluid);
		
		                //SETEAMOS UN BLOQUE DE FLUIDO EN EL DEST POSITION
		                realisticFluid.setBlockState(world, sourcePos, destPos, realisticFluid.setVolume(null, null, fluid.flowingBlock.getDefaultState(), destLevel));
		
		                //AÑADIMOS EL BLOQUE AL SISTEMA DE PRESION DIRECTA ASI TIENE EL LEVEL MAXIMO
		                if (doPressure && destLevel == References.MAXIMUM_LEVEL)
		                    PressureSystemLogic.addToPressure(world, destPos, false); // 0 = false
		            }
		        	//System.out.println("[RFF] Return 8 TRUE");
		            return true;
		        }
		        
		        
		        
		        //SI EL BLOQUE DESTINO NO ES UN FINITE FLUID DEL MISMO TIPO
		        //CHECAMOS SI NOS PODEMOS MOVER + SI EL SOURCE LEVEL ES MAYOR AL MINIMO
		        //(claro que en laa funcion canMoveInto, tambien checamos que el bloque destino
		        //no sea un bloque finito de cualquier tipo)
		    } else if (GeneralPurposeLogic.canMoveInto(world, destPos, sourcePos, sourceLevel, fluid) && sourceLevel > References.MINIMUM_LEVEL) {
		        if (doMove) {
		            --sourceLevel;
		            realisticFluid.setBlockState(world, null, sourcePos, realisticFluid.setVolume(null, null, fluid.flowingBlock.getDefaultState(), sourceLevel));
		            realisticFluid.setBlockState(world, sourcePos, destPos, realisticFluid.setVolume(null, null, fluid.flowingBlock.getDefaultState(), References.MINIMUM_LEVEL));
		        }
		    	//System.out.println("[RFF] Return 9 TRUE");
		        return true;
		    }
		    
			//System.out.println("[RFF] Return 10 FALSE");
		    return false;
		}
		
		
		/**
		 * Funcion para intentar agarrar fluido adyancente (hasta grabAmt bloques de distancia) y evitar dejar un hueco entre los bloques.
		 */
		public static boolean tryGrab(World world, BlockPos sourcePos, BlockPos destPos, int recursionDepth, NewFluidType fluid) {
		    if (world.isRemote) return false;
		
		    //Seteamos que hemos encontrado agua y su posicion en false/null, y eliminamos el SourceBlock.
		    //NOTA --> ENTIENDE, EL SOURCEBLOCK ESTA VACIO, PERO VAMOS A EVITAR QUE QUEDE COMPLETAMENTE HUECO
		    boolean foundWater = false;
		    BlockPos foundPos = null;
		    world.setBlockToAir(sourcePos);
		    world.markBlockRangeForRenderUpdate(sourcePos, sourcePos);
		
		    // Si el bloque "fluyo hacia arriba" y no hay aire en el destino, cancelar
		    // (Supongo que esto es para los fluidos gaseosos, o para alguna situacion en la recursion dentro de esta funcion)
		    if (destPos.getY() > sourcePos.getY() && !world.isAirBlock(destPos)) {
		        return false;
		    }
		    
		    //grabAmt --> Que tan lejos puede jalar fluido para nuestro SourcePos vacio.
		    if (recursionDepth < grabAmt) {
		        // offset 0: misma Y, offset 1: arriba/abajo según gravedad
		        for (int verticalOffset = 0; verticalOffset < 2 && !foundWater; ++verticalOffset) {
		            int yOffset = verticalOffset * -GeneralPurposeLogic.getFluidGravity();
		
		            //CONDICION PARA SETEAR foundWater A TRUE:
		            //1.1.- Que el grado de recursion sea igual a 0 
		            // O 
		            //1.2.- Que el bloque de destino sea distinto a  la posicion que vamos a checar en base a 
		            // offsets verticales + central y horizontales
		            
		            // Y 
		            
		            //2.- Que el bloque a checar sea un Fluido Finito DEL MISMO TIPO (isFiniteFluid())
		            
		            
		            
		            // Centro (misma XZ)
		            BlockPos blockPosToCheck = sourcePos.add(0, yOffset, 0); //.add(0, yOffset, 0);
		            if ((recursionDepth == 0 || !blockPosToCheck.equals(destPos)) && GeneralPurposeLogic.isFiniteFluid(world, blockPosToCheck)) {
		                foundPos = blockPosToCheck;
		                foundWater = true;
		            }
		            
		            // Laterales
		            for (EnumFacing facing : EnumFacing.HORIZONTALS) {
			            blockPosToCheck = sourcePos.add(0, yOffset, 0).offset(facing);
			            if (!foundWater && (recursionDepth == 0 || !blockPosToCheck.equals(destPos)) && GeneralPurposeLogic.isFiniteFluid(world, blockPosToCheck)) {
			                foundPos = blockPosToCheck;
			                foundWater = true;
			            }
			
		            }

		        }
		    }
		
		    //SI ENCONTRAMOS FLUIDO Y SU POSICION ES DISTINTA A NULL
		    if (foundWater && foundPos != null) {
		    	//OBTENEMOS BLOCKSTATE Y VOLUMEN ACTUAL
		        IBlockState state = world.getBlockState(foundPos);
				IRealisticFiniteFluid realisticFluid = ((IRealisticFiniteFluid)fluid.flowingBlock);
		        int level = realisticFluid.getVolume(world, foundPos, state);
		
		        // COLOCAMOS EL LEVEL DEL BLOQUE EN CONTRAMOS EN LA POSICION DEL SOURCEBLOCK
		        realisticFluid.setBlockState(world, null, sourcePos, realisticFluid.setVolume(null, null, fluid.flowingBlock.getDefaultState(), level));
		
		        // REPETIMOS EL MISMO PROCESO PARA EL BLOQUE DEL CUAL EXTRAIMOS AGUA,
		        // ESTO CON EL FIN DE JALAR AGUA A MUCHA DISTANCIA (grabAmt bloques) Y ACERCAR EL AGUA
		        tryGrab(world, foundPos, sourcePos, recursionDepth + 1, fluid);
		        
		    //PERO --> SI TRAS TODOO ESTO, AUN NO LOGRAMOS PONERLE FLUIDO A LA POSICION SOURCE_POS --> 
		    } else if (world.isAirBlock(sourcePos)) {
		        // EJECUTAMOS tryToSave
		        tryToSave(world, sourcePos, fluid);
		    }
		
		    return false;
		}
		
		/**
		 * 
		 */
		public static boolean tryToSave(World world, BlockPos pos, NewFluidType fluid) {
			//Obtenemos la gravedad del fluido actual, y seteamos el valor de "encontrado" a false
		    int gravity = fluid.gravity;
		    boolean found = false;
		
		    //EN UN for DE DIFERENTES ALTURAS DE Y --> 
		    for (int i = 0; i < 2; i++) {
		        int yOffset = (i == 0) ? 1 : -gravity;
		        BlockPos checkPos;
		
		        // Arriba (i == 0)
		        if (i == 0) {
		            checkPos = pos.up();
		            IBlockState state = world.getBlockState(checkPos);
		            Block block = RealisticFiniteFluidFunctions.getBlock(world, checkPos, state);
		            if (fluid.isFluid(block)) {
		        		IRealisticFiniteFluid realisticFluid = ((IRealisticFiniteFluid)block);
		                int level = realisticFluid.getVolume(world, checkPos, state);
		                if (level > References.MINIMUM_LEVEL || realisticFluid.isOceanBlock(world, checkPos, state, GeneralPurposeLogic.getFluidIndex(RealisticFiniteFluidFunctions.getBlock(world, checkPos, state)))) {
		                    found = true;
		                	//System.out.println("VERGA TRYTOSAVE_1");
		
		                    if (!realisticFluid.isOceanBlock(world, checkPos, state, GeneralPurposeLogic.getFluidIndex(RealisticFiniteFluidFunctions.getBlock(world, checkPos, state)))) {
		                    	realisticFluid.setBlockState(world, null, checkPos, realisticFluid.setConceptualVolume(null, null, fluid.flowingBlock.getDefaultState(), level));
		                    }
		                    realisticFluid.setBlockState(world, null, pos, realisticFluid.setVolume(null, null, fluid.flowingBlock.getDefaultState(), References.MINIMUM_LEVEL));
		                    break;
		                }
		            }
		        } else {
		        	for (EnumFacing dir : EnumFacing.HORIZONTALS) {
		                checkPos = pos.offset(dir).add(0, yOffset, 0);
		                IBlockState state = world.getBlockState(checkPos);
		                Block block = RealisticFiniteFluidFunctions.getBlock(world, checkPos, state);
		                if (fluid.isFluid(block)) {
		            		IRealisticFiniteFluid realisticFluid = ((IRealisticFiniteFluid)block);
		                    int level = realisticFluid.getVolume(world, checkPos, state);;
		                    if (level > References.MINIMUM_LEVEL || realisticFluid.isOceanBlock(world, checkPos, state, GeneralPurposeLogic.getFluidIndex(RealisticFiniteFluidFunctions.getBlock(world, checkPos, state)))) {
		                        found = true;
		                    	//System.out.println("VERGA TRYTOSAVE_2");
		                        if (!realisticFluid.isOceanBlock(world, checkPos, state, GeneralPurposeLogic.getFluidIndex(RealisticFiniteFluidFunctions.getBlock(world, checkPos, state)))) {
		                        	realisticFluid.setBlockState(world, null, checkPos, realisticFluid.setConceptualVolume(null, null, fluid.flowingBlock.getDefaultState(), level));
		                        }
		                        realisticFluid.setBlockState(world, null, pos, realisticFluid.setVolume(null, null, fluid.flowingBlock.getDefaultState(), References.MINIMUM_LEVEL));
		                        break;
		                    }
		                }
		            }
		            if (found) break;
		        }
		    }
		    
		    
		    
		    
		    
		    
		
		    if (!found) {
		        if (FiniteFluidLogic.doPressure) {
		            PressureSystemLogic.addToPressure(world, pos, true); //originalmente es true, el port manejaba false por algun motivo
		        }
		        return false;
		    }
		    
		    
		
		    return true;
		}
		
		
		
		
		
		
		
		
    	
    }

    
    
    /**
     *Functions used for ocean blocks
     */
    public static class OceanFluidsLogic {
    	    	
        public static boolean tryOceanMove(World world, BlockPos pos) {
            if (world.isRemote) return false;

            Random rand = new Random();
            int dx = rand.nextBoolean() ? 1 : -1;
            int dz = rand.nextBoolean() ? 1 : -1;
            boolean flip = rand.nextBoolean();

            BlockPos below = pos.down(-FiniteFluidLogic.GeneralPurposeLogic.getFluidGravity());

            if (oceanMove(world, pos, below, true)) return true;

            if (flip) {
                if (oceanMove(world, pos, pos.add(-dx, FiniteFluidLogic.GeneralPurposeLogic.getFluidGravity(), 0), true)) return true;
                if (oceanMove(world, pos, pos.add(dx, FiniteFluidLogic.GeneralPurposeLogic.getFluidGravity(), 0), true)) return true;
                if (oceanMove(world, pos, pos.add(0, FiniteFluidLogic.GeneralPurposeLogic.getFluidGravity(), -dz), true)) return true;
                if (oceanMove(world, pos, pos.add(0, FiniteFluidLogic.GeneralPurposeLogic.getFluidGravity(), dz), true)) return true;
                if (oceanMove(world, pos, pos.add(-dx, 0, 0), true)) return true;
                if (oceanMove(world, pos, pos.add(dx, 0, 0), true)) return true;
                if (oceanMove(world, pos, pos.add(0, 0, -dz), true)) return true;
                if (oceanMove(world, pos, pos.add(0, 0, dz), true)) return true;
            } else {
                if (oceanMove(world, pos, pos.add(0, FiniteFluidLogic.GeneralPurposeLogic.getFluidGravity(), -dz), true)) return true;
                if (oceanMove(world, pos, pos.add(0, FiniteFluidLogic.GeneralPurposeLogic.getFluidGravity(), dz), true)) return true;
                if (oceanMove(world, pos, pos.add(-dx, FiniteFluidLogic.GeneralPurposeLogic.getFluidGravity(), 0), true)) return true;
                if (oceanMove(world, pos, pos.add(dx, FiniteFluidLogic.GeneralPurposeLogic.getFluidGravity(), 0), true)) return true;
                if (oceanMove(world, pos, pos.add(0, 0, -dz), true)) return true;
                if (oceanMove(world, pos, pos.add(0, 0, dz), true)) return true;
                if (oceanMove(world, pos, pos.add(-dx, 0, 0), true)) return true;
                if (oceanMove(world, pos, pos.add(dx, 0, 0), true)) return true;
            }

            return false;
        }
        
        public static boolean oceanMove(World world, BlockPos sourcePos, BlockPos destPos, boolean doMove) {
            if (world.isRemote) return false;

            //DETERMINAMOS TANTO LOS IBLOCKSTATES COMO LOS BLOCKS DE LOS BLOQUES DE ORIGEN Y DE DESTINO
            IBlockState sourceState = world.getBlockState(sourcePos);
            IBlockState destState = world.getBlockState(destPos);

            IRealisticFiniteFluid realisticFluid = ((IRealisticFiniteFluid)RealisticFiniteFluidFunctions.getBlock(world, sourcePos, sourceState));
            Block sourceBlock = RealisticFiniteFluidFunctions.getBlock(world, sourcePos, sourceState);
            Block destBlock = RealisticFiniteFluidFunctions.getBlock(world, destPos, destState);

            //SETEAMOS EL TIPO DE FLUIDO DEL SOURCEBLOCK
            FiniteFluidLogic.GeneralPurposeLogic.setCurrentFluidIndex(sourceBlock);
            NewFluidType fluid = (NewFluidType) liquids.get(onFiniteFluidIndex);


            // SI EL DESTINO NO ES FLUIDO REALISTA Y TRAS CIERTA PROBABILIDAD
            if (!FiniteFluidLogic.GeneralPurposeLogic.isFiniteFluid(world, destPos) && !(new Random().nextInt(10) == 0)) {
                //SI PODEMOS REEMPLAZAR EL BLOQUE DE DESTINO
            	if (FiniteFluidLogic.GeneralPurposeLogic.canMoveInto(world, destPos, sourcePos, References.MAXIMUM_LEVEL, fluid)) {
            		//Y SI SI NOS PODEMOS MOVER REALMENTE
                    if (doMove) {
                    	
                        //SETEAMOS OTRA VEZ EL TIPO DE FLUIDO DEL SOURCEBLOCK
                    	FiniteFluidLogic.GeneralPurposeLogic.setCurrentFluidIndex(sourceBlock);
                    	//PONEMOS UN BLOQUE DE FLUIDO STILL CON UN POCO DE VALOR
                    	realisticFluid.setBlockState(world, sourcePos, destPos, realisticFluid.setVolume(null, null, fluid.stillBlock.getDefaultState(), References.Q1_HIGH));
                        //PROGRAMAMOS UN TICK DEL BLOQUE SOURCE 
                    	world.scheduleUpdate(sourcePos, fluid.stillBlock, waterTick + 1);
                        return true;
                    } else {
                        return false;
                    }
                }
            }

            // SI EN EL BLOQUE DESTINO TENEMOS EL MISMO LIQUIDO Y ES OCEANICO (!not), ABORTAMOS (sin este codigo, el agua perfora el mundo hacia abajo)
            if (!FiniteFluidLogic.GeneralPurposeLogic.isFiniteFluidNoOcean(world, destPos, destState)) return false;
            //if (GeneralPurposeLogic.isRealisticFluid(world, destPos) && !BlockFiniteFluid.isOceanBlock(world, destPos, destState, onFiniteFluidIndex)) return false;
            
            // SI EL DESTINO NO ES UN BLOQUE OCEANICO Y SI ERA FLUIDO FINITO DEL MISMO TIPO (sin este codigo, el agua oceanica solo coloca 1 agua normal y muere)
            if (doMove) {
            	//SETEAMOS OTRA PERRA VEZ EL TIPO DE FLUIDO DEL SOURCE BLOCK
            	FiniteFluidLogic.GeneralPurposeLogic.setCurrentFluidIndex(sourceBlock);
            	//COLOCAMOS UN BLOQUE DE TIPO OCEANICO Y PROGRAMAMOS UN TICK
                //world.setBlockState(destPos, fluid.oceanBlock.getDefaultState());
            	realisticFluid.setBlockState(world, sourcePos, destPos, realisticFluid.setVolume(world, destPos, fluid.stillBlock.getDefaultState(), References.MAXIMUM_CONCEPTUAL_LEVEL));
            	world.scheduleUpdate(sourcePos, fluid.stillBlock, waterTick + 1);
            }
             
            return true;
        }
        
        ///LO DE ARRIBA FUE TODA LA LOGICA DE MOVIMIENTO DEL AGUA, LO QUE SIGUE ES LA LOGICA QUE DETERMINA:
        //1.- CALCULAR LOS BLOQUES OCEANICOS ADYACENTES Y CONVERTIRLOS EN FLUIDO STILL/FLOWING
        //2.- DESPERTERA LOS BLOQUES OCEANICOS DIRECTAMENTE (NO CONVERTIRLOS A AGUA FLOWING)


        /**
         * A function to turn adjacent ocean blocks into flowing/still blocks. For player interaction purposes, and determined by the LakeSize
         * @param pos The position from where to start
         * @param shouldDoSmallOceanSearch Whether to reduce the quantity of fluid blocks to convert into Still/Flowing blocks.
         */
        public static boolean borderOceanCheck(World world, BlockPos pos, boolean shouldDoSmallOceanSearch) {
        	FiniteFluidLogic.smallOceanSearch = shouldDoSmallOceanSearch;
            //if (!world.isRemote) return;
            if (world.isRemote) return false;
            
            //SETEAMOS EL TIPO DE FLUIDO A UN VALOR NO VALIDO (A DETERMINAR POR VALIDO)
            int detectedType = -1;

            // Revisamos este bloque y sus adyacentes por Fluid Finito. Cuando lo encontramos, hacemos break.
            for (BlockPos offset : new BlockPos[] {
                    pos, pos.west(), pos.east(),
                    pos.down(), pos.up(),
                    pos.north(), pos.south()
            }) {
                int type = FiniteFluidLogic.GeneralPurposeLogic.getFluidIndex(RealisticFiniteFluidFunctions.getBlock(world, offset, world.getBlockState(offset)));
                if (detectedType == -1 && type != -1) {
                    detectedType = type;
                    break;
              }
            }
            ////System.out.println("detectedType: '"+detectedType+"' From block: "+world.getBlockState(pos).getBlock().toString());
            
            //SI detectedType SIGUE SIENDO INVALIDO, LO CONVERTIMOS EN EL ID DEL AGUA
            if (detectedType >= 0) {
                //AÑADIMOS BLOQUES A LA LISTA DE PRESION
                if (doPressure) PressureSystemLogic.addToPressure(world, pos, true); //Añadido a la lista Reverse

                onFiniteFluidIndex = detectedType;
                
                //UN INT BACKUP DEL INDEX, SETEADO AL DEL AGUA SI NO ENCONTRAMOS FLUIDO VALIDO
                int savedType = onFiniteFluidIndex < 0 ? 0 : onFiniteFluidIndex;

                //ESTO UTILIZA UN METODO ANTIGUO PARA OBTENER LIQUIDOS VALIDDOS, DEBE CAMBIAR
                //AHORA SOLO CHECA POR EL FLUIDO ACTUAL
                //////for (int i = 0; i < liquids.size(); i++) {
                	int currentIndex = detectedType; //FiniteFluidLogic.GeneralPurposeLogic.getFluidIndex(world.getBlockState(pos).getBlock());
                	//System.out.println("[RFF] Block: "+world.getBlockState(pos).getBlock()+"// at: " +pos);
                	//System.out.println("[RFF] Index: "+currentIndex);
                	//NewFluidType type = (NewFluidType) liquids.get(onFiniteFluidIndex);
                	NewFluidType type = (NewFluidType) liquids.get(currentIndex);
                    if (type != null /*&& type.oceanBlock != null*/) {
                        //////onFiniteFluidIndex = i;
                        boolean valid = true;
                        IRealisticFiniteFluid realisticFluid = ((IRealisticFiniteFluid)type.flowingBlock);

                        //SI EL BLOQUE ACTUAL NO TIENE BLOQUES OCEANICOS ADYACENTES Y EL BLOQUE ACTUAL ES OCEANICO --> SE CONVIERTE EN FLOWING Y SE VUELVE NO VALIDO
                        
                        
                        if (!FiniteFluidLogic.GeneralPurposeLogic.hasAdjacentOceanBlocksAround(world, pos, type.stillBlock)) {
                            if (realisticFluid.isOceanBlock(world, pos, null, onFiniteFluidIndex)) {
                            	realisticFluid.setBlockState(world, null, pos, realisticFluid.setVolume(null, null, type.flowingBlock.getDefaultState(), References.MAXIMUM_LEVEL));
                            }

                            onFiniteFluidIndex = savedType;
                            valid = false;
                        }

                        //SI EL BLOQUE ACTUAL ERA VALIDO Y EL FLUID INDEX ES VALIDO --> HACEMOS BORDEROCEANCHECK2
                        if (valid && onFiniteFluidIndex > -1) {
                    	    for (EnumFacing face : EnumFacing.values()) {
                        	    borderOceanCheck2(world, pos.offset(face), realisticFluid, currentIndex);
                    	    }
                        }
                        return true;
                    }
                //////}

                //DESPERTAMOS BLOQUES OCEANICOS ADYACENTES
                //por lo que veo, en relidad aca casi no ahce absolutamente nada
                //wakeOcean(world, pos);
                //System.out.println("OCEANO DESPERTADO EN: "+pos);
                //System.out.println("BLOQUE DESPERTADO: "+world.getBlockState(pos));
                //System.out.println("");
                onFiniteFluidIndex = savedType < 0 ? 0 : savedType;
            }
			return false;
        }

        
        /**
         * Second BorderOceanCheck, to start turning them into Still/Flowing blocks.
         */
        public static void borderOceanCheck2(World world, BlockPos pos, IRealisticFiniteFluid realisticFluid, int index) {
        	//SI EL MUNDO ES REMOTO O EL INDEX ES INVALUDO --> ABORTAMOS
            if (world.isRemote || onFiniteFluidIndex < 0) return;
            //AÑADIMOS A LISTA DE PRESION
            if (doPressure) PressureSystemLogic.addToPressure(world, pos, true);
            
            //DETERMINAMOS NUESTRO BLOQUE OBJETIVO, EL QUE LE METEMOS, Y SETEAMOS EL TIPO DE FLUIDO
            Block target = RealisticFiniteFluidFunctions.getBlock(world, pos, world.getBlockState(pos));
            //NewFluidType fluid = (NewFluidType) liquids.get(onFiniteFluidIndex);
            
            //SI EL TARGET ES UN BLOQUE DE TIPO OCEANICO
            if (realisticFluid.isOceanBlock(world, pos, null, index)) {            	
            	//HACEMOS UNA LISTA DE BUSQUEDA DE BLOQUES OCEANICOS CON EL MISMO INDEX
                List<BlockPos> result = oceanSearch(world, pos, index);
                //SI LA LISAT ESTA VACIA O NO ES VALIDA --> PROGRAMAMOS TICK DEL TARGET
                if (result == null || result.isEmpty()) {
                    world.scheduleBlockUpdate(pos, target, waterTick + 1, 0);
                //SI LA LISTA ES VALIDA --> OCEAN TO STILL
                } else {
                    oceanToStill(world, result, index);
                }
            }
        }

        
        /**
         * A function to "wake" adyacent ocean blocks. Doesn't calculate whether they should become Ocean--> still/flowing blocks.
         * <p>
         * There's no other way to make OceanBlocks tick.
         */
        public static void wakeOcean(World world, BlockPos pos) {
            int backupType = onFiniteFluidIndex;

            //SETEAMOS LOS BLOQUES, VEMOS QUE NADA SEA INVALIDO, Y OBTENEMOS EL FLUID INDEX
            Block block = RealisticFiniteFluidFunctions.getBlock(world, pos, world.getBlockState(pos));
            //System.out.print("BLOQUE EN WAKE OCEAN: "+blockToFluidIndex.get(block)+"\n");
            //System.out.println("POS: "+pos);
            if (block == null || blockToFluidIndex.get(block) == null) return;
            int fluidIndex = blockToFluidIndex.get(block);
            if (liquids.get(fluidIndex).stillBlock == null) return;
            IRealisticFiniteFluid realisticFluid = ((IRealisticFiniteFluid)block);

            //SI EL FLUID INXES ES VALIDO
            if (fluidIndex >= 0 && fluidIndex < liquids.size()) {
                NewFluidType type = liquids.get(fluidIndex);
                //SI EL TIPO DE FLUIDO ES VALIDO Y SU BLOQUE OCEANICO SI EXISTE
                if (type != null && type.stillBlock != null) {
                    onFiniteFluidIndex = fluidIndex;

                    // CHECAMOS TODOS LOS ENUM FACINGS POR BLOQUES VENICOS
                    for (EnumFacing dir : EnumFacing.values()) {
                        BlockPos neighbor = pos.offset(dir);
                        Block neighborBlock = RealisticFiniteFluidFunctions.getBlock(world, neighbor, world.getBlockState(neighbor));

                        //SI EL BLOQUE VECINO ES UN BLOQUE OCEANICO DEL MISMO TIPO
                        if (realisticFluid.isOceanBlock(world, neighbor, null, fluidIndex)) {
                        	//PROGRAMAMOS TICK --> DESPERTAMOS EL OCEANO ADYACENTE
                            world.scheduleBlockUpdate(neighbor, neighborBlock, block.tickRate(world) + 1, 0);
                            //System.out.println("Posicion"+neighbor);
                        }
                    }
                }
            }

            //SETEAMOS EL FLUID INDEX CACHEDADO, AL ORIGINAL O AL DEL AGUA
            onFiniteFluidIndex = Math.max(0, backupType);
        }

        
        /**
         * Does a check to count how many Ocean Blocks are nearby the current block, dinamically. 
         * <p>
         * When it counts the enough Visited blocks, it returns the list.
         * @param startBlock The initial block.
         * @param fluidIndex The respective Fluid Index for the blocks to search.
         * @return A list of unique visited Ocean blocks.
         */
        public static List<BlockPos> oceanSearch(World world, BlockPos startBlock, int fluidIndex) {
        	//GENERAMOS UNA LISTA DE BLOQUES VISITADOS (lista para evitar volver a checar bloques ya visitados) Y QUEVEADOS (El bloque en el turno actual)
            Set<BlockPos> visited = new HashSet<>();
            Queue<BlockPos> queue = new LinkedList<>();
           	////System.out.println("[DEBUG] Ocean search desde: " + start);
        	//if (visited.size() % 100 == 0) {
        	//}

            //DETERMINAMOS EL LIMITE DE BLOQUES A BUSCAR
            int limit = smallOceanSearch ? lakeLimit / 10 + 1 : lakeLimit;

            //DETERMINAMOS EL TIPO DE TARGET (BLOQUE OCEANICO DEL MISMO TIPO DE FLUIDO)
            //Block target = ((NewFluidType) liquids.get(fluidIndex)).stillBlock;
            ////////int originalIndex = GeneralPurposeLogic.getFluidIndex(world.getBlockState(startBlock).getBlock());
            IRealisticFiniteFluid realisticFluid = ((IRealisticFiniteFluid)RealisticFiniteFluidFunctions.getBlock(world, startBlock, world.getBlockState(startBlock)));


            //AÑADIMOS EL BLOQUE DE INICIO EN LA LISTA DE VISITADOS Y QUEVEADOS
            visited.add(startBlock);
            queue.add(startBlock);

            //SI LA LISTA DE QUEVEADOS NO ESTA VACIA, Y LA DE VISITADOS SIGUE SIENDO MENOR AL TAMAÑO DE OCEANO A BUSCAR
            while (!queue.isEmpty() && visited.size() < limit) {    
            	//DETERMINAMOS LA POSICION ACTUAL EN LA QUEVE
                BlockPos currentPosition = queue.poll();
                //System.out.println(current);
                ////System.out.println(world.getBlockState(current));
        	    ////System.out.println("[DEBUG] Visitados: " + visited.size());
        	    
                //CHECAMOS LOS VECINOS
            	//System.out.println("[RFF] Original Block: "+world.getBlockState(startBlock).getBlock());

                for (EnumFacing dir : EnumFacing.values()) {
                    BlockPos neighbor = currentPosition.offset(dir);

                    //1.- SI NO TENEMOS REGISTRADO A ESTE VECINO EN LA LISTA DE VISITADOS 
                    //2.- Y EL BLOQUE A REVISAR ES DEL TIPO DEL TARGET
                    if (!visited.contains(neighbor) 
                    	&& realisticFluid.isOceanBlock(world, neighbor, null, fluidIndex)
                    	) {
                    	//System.out.println("[RFF] Found Block: "+world.getBlockState(currentPosition.offset(dir)).getBlock());
                    	

                    	//LO AÑADIMOS EN LA LISTA DE VISITADOS Y EN LA QUEVE
                        visited.add(neighbor);
                        queue.add(neighbor);
                        //NOTA --> Los bloques checados en esta parte del codigo, TODOS son metidos a la Queve y en la lista de visitados.
                        //Despues, a cada bloque nuevo en la queve se le hace una busqueda de sus vecinos, los cuales inevitablemente se repetiran
                        //Por ello es que tambien verificamos que no hayan sido visitados previamente, y ya despues solo asi se meten bloques unicos cada vez
                        //Con esto buscamos verificar la mayor cantidad de bloques proximos al bloque de partida, evitar saltarnoslos y que sean puros bloques adyacentes
                        //Aunque si conecta con una masa enorme de agua, tal vez ya no checque los bloques mas proximos, sino los que tenga a la mano
                        //y seguramente llegue al limite rapidamente.
                    }
                }
            }
            //AL FINAL DE LA BUSQUEDA, REGRESAMOS LA LISTA DE BLOQUES VISITADOS (del tamaño valido para ser un oceano)
            return new ArrayList<>(visited);
        }
        
        
        /**
         * Converts all the BlockPos on the input list into Flowing or Still blocks.
         * @param nodes Nodes of BlockPos to convert.
         */
        public static void oceanToStill(World world, List<BlockPos> nodes, int originalIndex) {
        	//POR CADA VALOR EN EL NODO (proveniente de OceanSearch) SE CHECA LO SIGUIENTE --> 
            for (int i = 0; i < nodes.size(); i++) {
                BlockPos pos = nodes.get(i);
                if (pos == null) continue;

                //  SI HAY AIRE CERCA, PROGRAMAMOS EL TICK DE FORMA TARDIA
                for (EnumFacing face : EnumFacing.values()) {
                    if (world.isAirBlock(pos.offset(face))) {
                        // Delay la conversión
                        world.scheduleBlockUpdate(pos, RealisticFiniteFluidFunctions.getBlock(world, pos, world.getBlockState(pos)), 10, 0);
                        continue;
                    }
                }

                //DETERMINAMOS SI USAMOS BLOQUE STILL O FLOWING, NO CREO QUE IMPORTE MUCHO
                Block block = (i < References.MAXIMUM_LEVEL)
                    ? ((NewFluidType) liquids.get(originalIndex)).flowingBlock
                    : ((NewFluidType) liquids.get(originalIndex)).stillBlock;

                IRealisticFiniteFluid realisticFluid = ((IRealisticFiniteFluid)block);
                 //SETEAMOS EL BLOQUE EN CADA POSICION DE LA LISTA DE NODOS A UNO DE FLOWING O STILL
                int level = References.MAXIMUM_LEVEL;
                if (GeneralPurposeLogic.getFluidIndex(RealisticFiniteFluidFunctions.getBlock(world, pos, world.getBlockState(pos))) == originalIndex)
                	realisticFluid.setBlockState(world, null, pos, realisticFluid.setVolume(null, null, block.getDefaultState(), level));
            }
        }

    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
	
    
    public static class GeneralPurposeLogic {

    	/**
    	 * Para manejar Presiones y que la lluvia provoque chargos de agua (Still con el nivel minimo)
    	 */
	    public static void onTick() {
	        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
	        if (server == null) return;

	        World world = server.getWorld(0);  // Mundo principal (overworld)
	        if (world == null) return;

	        calcAmt = 0;
	        
	        /*if (pressure.isEmpty()) {
	            //System.out.println("[DEBUG] La lista de presión está VACÍA.");
	        } else {
	            System.out.println("[DEBUG] Nodos de presión: " + pressure.size());
	        }*/

	        // Presión directa
	        if (!pressure.isEmpty() && pressure.get(0) != null) {
	            BlockPos pos = pressure.remove(0);
	            PressureSystemLogic.removeFromPressure(pos);
	            PressureSystemLogic.checkPressure(world, pos, pos, 0, new ArrayList<>());
	        }

	        // Presión inversa
	        if (!rpressure.isEmpty() && rpressure.get(0) != null) {
	            BlockPos pos = rpressure.remove(0);
	            PressureSystemLogic.removeFromPressure(pos);
	            PressureSystemLogic.checkPressureReverse(world, pos, pos, 0, new ArrayList<>());
	        }
	        
	        

	        // Simulación de lluvia
	        if (world.isRaining() && enableRain && world.playerEntities.size() > 0) {
	            Random rand = new Random();
                int viewDistance = server.getPlayerList().getViewDistance();
	            //if (scalableRainMethod) rainAmount = (int) Math.ceil((48*(1.0/rainNewMethodAmount))/viewDistance); // 48 es mi constante que resuelve 2-->24, 4-->12
	            int rainAmount = (int) Math.ceil((48*(1.0/rainNewMethodAmount))/viewDistance); // 48 es mi constante que resuelve 2-->24, 4-->12

	            //System.out.println(rainAmount);
	            
	            if (rand.nextInt(rainAmount) == 0) {
	                int playerIndex = rand.nextInt(world.playerEntities.size());
	                EntityPlayer player = world.playerEntities.get(playerIndex);
	                int x = 0;
	                int z = 0;
	                
	                //if (scalableRainMethod) {
	                    int rainRadius = viewDistance * 16; // en bloques
		                x = (int) (player.posX + rand.nextInt(rainRadius*2) - rainRadius);
		                z = (int) (player.posZ + rand.nextInt(rainRadius*2) - rainRadius);
	                //} else {
		            //    x = (int) (player.posX + rand.nextInt(rainArea) - rainArea/2);
		            //    z = (int) (player.posZ + rand.nextInt(rainArea) - rainArea/2);
	                //}
	                int y = getTopSolidOrLiquidBlock(world, x, z);

	                if (y != -1) {
	                    Biome biome = world.getBiome(new BlockPos(x, y, z));
	                    BlockPos newPos = new BlockPos(x, y, z);
	                    IBlockState state = world.getBlockState(newPos);
	                    Block block = RealisticFiniteFluidFunctions.getBlock(world, newPos, state);
	                    //IBlockState stateDown = world.getBlockState(new BlockPos(x, y, z));
	                    IBlockState stateDown = world.getBlockState(newPos.down());

	                    // Evitar cultivos de WEATH TODO implementar cualquier cultivos y mas cosas
	                    NewFluidType fluidType = ((NewFluidType) liquids.get(0));
	                    IRealisticFiniteFluid realisticFluid = ((IRealisticFiniteFluid)fluidType.flowingBlock);

                        IBlockState water = realisticFluid.setVolume(null, null, fluidType.flowingBlock.getDefaultState(), References.MINIMUM_LEVEL);
                        //fluidType.flowingBlock.getDefaultState().withProperty(BlockFiniteFluid.LEVEL, 0);
                        BlockPos spawnPos = new BlockPos(x, y, z);
	                	if (!(block instanceof BlockBush) && !(realisticFluid.isOceanBlock(null, null, stateDown, getFluidIndex(RealisticFiniteFluidFunctions.getBlock(world, newPos.down(), stateDown)))) 
	                			&& (biome.canRain() && (world.getBiome(spawnPos).getTemperature(spawnPos) >= 0.15F))) {
	                        realisticFluid.setBlockState(world, spawnPos.up(), spawnPos, water);
	                        //world.setBlockState(spawnPos, water, 3);
	                    }
	                }
	            }
	        }
	    }
	    

	    // Encuentra la parte superior sólida o líquida
	    public static int getTopSolidOrLiquidBlock(World world, int x, int z) {
	        Chunk chunk = world.getChunk(new BlockPos(x, 0, z));
	        int y = world.getHeight();  // 256 normalmente

	        x &= 15;
	        z &= 15;

	        for (; y > 0; --y) {
	            IBlockState state = chunk.getBlockState(x, y, z);
	            World newWorld = chunk.getWorld();
	            BlockPos newPos = new BlockPos(x, y, z);
	            if (RealisticFiniteFluidFunctions.getBlock(newWorld, newPos, state) == Blocks.ICE || getFluidIndex(RealisticFiniteFluidFunctions.getBlock(newWorld, newPos, state)) != -1) {
	                return y + 1;
	            }
	            if (!RealisticFiniteFluidFunctions.getBlock(newWorld, newPos, state).isAir(state, world, new BlockPos(x, y, z)) &&
	                state.getMaterial().isSolid() &&
	                state.getMaterial() != net.minecraft.block.material.Material.LEAVES) {
	                return y + 1;
	            }
	        }

	        return -1;
	    }
    	
    	
    	/**
    	 * Checks if a target block is adjacent to the block in the current world and position given.
    	 * @param pos The position of the block to check if its has adjacent target blocks.
    	 * @param target Target block to compare.
    	 * @return If that block is near the current block.
    	 */
    	public static boolean hasAdjacentTarjetBlocksAround(World world, BlockPos pos, Block target) {
    	    for (EnumFacing face : EnumFacing.values()) {
    	        if (RealisticFiniteFluidFunctions.getBlock(world, pos.offset(face), world.getBlockState(pos.offset(face))) == target) {
    	            return true;
    	        }
    	    }
    	    return false;
    	}
    	
    	public static boolean hasAdjacentOceanBlocksAround(World world, BlockPos pos, Block target) {
    	    for (EnumFacing face : EnumFacing.values()) {
                //IRealisticFiniteFluid realisticFluid = ((IRealisticFiniteFluid)liquids.get(onFiniteFluidIndex).flowingBlock);
    	        if (RealisticFiniteFluidFunctions.getBlock(world, pos.offset(face), world.getBlockState(pos.offset(face))) == target && RealisticFiniteFluidFunctions.getVolume(null, null, world.getBlockState(pos.offset(face))) > References.MAXIMUM_LEVEL) {
    	            return true;
    	        }
    	    }
    	    return false;
    	}
    	
    	
    	/**
    	 * Checks if a target block is adjacent to the block in the current world and position given.
    	 * @param pos The position of the block to check if its has adjacent target blocks.
    	 * @param target Target block to compare.
    	 * @return If that block is near the current block.
    	 */
    	public static boolean hasAdjacentTarjetBlocksHorizontal(World world, BlockPos pos, Block target) {
    	    for (EnumFacing face : EnumFacing.HORIZONTALS) {
    	        if (RealisticFiniteFluidFunctions.getBlock(world, pos.offset(face), world.getBlockState(pos.offset(face))) == target) {
    	            return true;
    	        }
    	    }
    	    return false;
    	}
    	
    	public static boolean hasAdjacentOceanBlocksHorizontal(World world, BlockPos pos, Block target) {
    	    for (EnumFacing face : EnumFacing.HORIZONTALS) {
                IRealisticFiniteFluid realisticFluid = ((IRealisticFiniteFluid)liquids.get(onFiniteFluidIndex).flowingBlock);
    	        if (RealisticFiniteFluidFunctions.getBlock(world, pos.offset(face), world.getBlockState(pos.offset(face))) == target && realisticFluid.getVolume(null, null, world.getBlockState(pos.offset(face))) > References.MAXIMUM_LEVEL) {
    	            return true;
    	        }
    	    }
    	    return false;
    	}
    	
    	
    	public static boolean hasAnyFluidAround(World world, BlockPos pos) {
    	    for (EnumFacing face : EnumFacing.values()) {
    	    	Block block = RealisticFiniteFluidFunctions.getBlock(world, pos.offset(face), world.getBlockState(pos.offset(face)));
    	        if (block instanceof IRealisticFiniteFluid && GeneralPurposeLogic.getFluidIndex(block) == onFiniteFluidIndex) {
    	            return true;
    	        }
    	    }
    	    return false;
    	}
    	
    	
    	/**
    	 * Cuenta cuantos bloques adyacentes son agua valida 
    	 */
    	public static int countAdjacentFluidBlocks(World world, BlockPos pos) {
    	    int count = 0;
    	    for (EnumFacing dir : EnumFacing.values()) {
    	        BlockPos neighbor = pos.offset(dir);
    	        if (isFiniteFluid(world, neighbor)) {
    	            count++;
    	        }
    	    }
    	    return count;
    	}


    	
    	public static void setCurrentFluidIndex(Block block) {
            onFiniteFluidIndex = getFluidIndex(block);
            if (onFiniteFluidIndex < 0) onFiniteFluidIndex = 0;
            return;
    	}

        public static void setCurrentFluidIndex(int fluidIndex) {
            onFiniteFluidIndex = fluidIndex;
            if (onFiniteFluidIndex < 0)
                onFiniteFluidIndex = 0;
        }
        
        /**
         * Gets the finite fluid type of this Block. -1 if the block is not from a registered Finite Fluid
         * @param block the Block to check on the finite fluid list
         * @return the index on the FiniteFluid list of this Block
         */      
        public static int getFluidIndex(Block block) {
            return blockToFluidIndex.getOrDefault(block, -1);
        }
        
        /**
         * Gets the finite fluid type of this FluidRegistry name. -1 if the name is not from a registered Finite Fluid
         * @param fluidRegistryName the name to check on the finite fluid list
         * @return the index on the FiniteFluid list of this FluidRegistryName
         */
        public static int getFluidIndex(String fluidRegistryName) {
            if (!FluidRegistry.isFluidRegistered(fluidRegistryName)) return -1;
            return fluidIndexMap.getOrDefault(fluidRegistryName.toLowerCase(), -1);
        }


        /**
         * Gives the fluid gravity of the fluids OF THE CURRENT onFiniteFluidIndex
         */
        public static int getFluidGravity() {
            return onFiniteFluidIndex < 0 
            		? getFluidGravity(0) 
            		: getFluidGravity(onFiniteFluidIndex);
        }

        /**
         * Gives the fluid gravity of the fluids of the current index
         * @param fluidIndex
         */
        public static int getFluidGravity(int fluidIndex) {
            return fluidIndex < 0 	//undefined index -->
            		? liquids.get(0).gravity 			//Water gravity
            		: liquids.get(fluidIndex).gravity;
        }
        
    	/**
    	 * Gives the current fluid gravity (to calculate if it should go downwards or upwards) depending on its density.
    	 * @param block The block to check its gravity.
    	 * @return Fluid gravity (+1 [downwards] or -1 [upwards]).
    	 */
        public static int getFluidGravity(Block block) {
        	if (block instanceof IRealisticFiniteFluid) {
        		Fluid fluid = ((IRealisticFiniteFluid)block).getFluid();
        		if (fluid.getDensity() >= 0) return 1;
        		else return -1;
        	}
        	return 1;
        }

        public static int getFluidLevelRender(IBlockAccess world, BlockPos pos, int fluidIndex) {
        	IBlockState state = world.getBlockState(pos);
        	if (RealisticFiniteFluidFunctions.getBlock(world, pos, state) instanceof IRealisticFiniteFluid) {
        		IRealisticFiniteFluid realisticFluid = ((IRealisticFiniteFluid)RealisticFiniteFluidFunctions.getBlock(world, pos, state));
        		if (fluidIndex > -1) {
        			int level = realisticFluid.getVolume(world, pos, state) * 2;

        			if (level >= References.MAXIMUM_LEVEL) level = References.MAXIMUM_LEVEL-1;

        			Block block = RealisticFiniteFluidFunctions.getBlock(world, pos, state);
        			if (realisticFluid.isOceanBlock(world, pos, state, getFluidIndex(block))) {
        				return level;
        			}
        		}

        		//IBlockState state = world.getBlockState(pos);
        		return realisticFluid.getVolume(world, pos, state);
        	}
        	return References.MAXIMUM_LEVEL;
        }
        
        /**
         * Gives the current Fluid Height value on the given corner.
         */
        public static float getHeight(IBlockAccess access, BlockPos pos, int dx, int dz) {
            int fluidIndex = getFluidIndex(RealisticFiniteFluidFunctions.getBlock(access, pos, access.getBlockState(pos)));
            if (fluidIndex == -1) return 0f;
            float total = getFluidLevelRender(access, pos, fluidIndex) + 1.0f;
            int samples = 1;
    		IRealisticFiniteFluid realisticFluid = ((IRealisticFiniteFluid)liquids.get(fluidIndex).flowingBlock);

            BlockPos above = pos.up(-getFluidGravity(fluidIndex));
            if (isSameIndexFluid(RealisticFiniteFluidFunctions.getBlock(access, above, access.getBlockState(above)), fluidIndex)) return 1.0f;
            if (realisticFluid.isOceanBlock(access, pos, null, fluidIndex)) return 1.0f;

            
            
            boolean hasNeighbors = !(access.isAirBlock(pos.east()) &&
            		access.isAirBlock(pos.west()) &&
            		access.isAirBlock(pos.north()) &&
            		access.isAirBlock(pos.south()));

            if (!hasNeighbors) return total / (float) References.MAXIMUM_CONCEPTUAL_LEVEL;

            BlockPos p1 = pos.add(dx, 0, 0);
            BlockPos p2 = pos.add(0, 0, dz);
            BlockPos p3 = pos.add(dx, 0, dz);

            // === Primer vecino (p1) ===
            if (realisticFluid.isOceanBlock(access, p1, null, fluidIndex)) return 1.0f;
            if (isSameIndexFluid(access, p1, fluidIndex)) {
                if (isOnlyVerticallyFallingFluid(access, p3, fluidIndex)) return 0.0f;
                total += getFluidLevelRender(access, p1, fluidIndex) + 1.0f;
                samples++;
            }
            if (isFullWaterRender(access, p1.up(-getFluidGravity(fluidIndex)), fluidIndex)) return 1.0f;
            if (isOnlyVerticallyFallingFluid(access, p1, fluidIndex)) return 0.0f;

            // === Segundo vecino (p3) ===
            if (realisticFluid.isOceanBlock(access, p3, null, fluidIndex)) return 1.0f;
            if (isSameIndexFluid(access, p3, fluidIndex)) {
                total += getFluidLevelRender(access, p3, fluidIndex) + 1.0f;
                samples++;
            }
            if (isFullWaterRender(access, p3.up(-getFluidGravity(fluidIndex)), fluidIndex)) return 1.0f;

            // === Tercer vecino (p2) ===
            if (realisticFluid.isOceanBlock(access, p2, null, fluidIndex)) return 1.0f;
            if (isSameIndexFluid(access, p2, fluidIndex)) {
                if (isOnlyVerticallyFallingFluid(access, p3, fluidIndex)) return 0.0f;
                total += getFluidLevelRender(access, p2, fluidIndex) + 1.0f;
                samples++;
            }
            if (isFullWaterRender(access, p2.up(-getFluidGravity(fluidIndex)), fluidIndex)) return 1.0f;
            if (isOnlyVerticallyFallingFluid(access, p2, fluidIndex)) return 0.0f;

            return total / (float)References.MAXIMUM_CONCEPTUAL_LEVEL / samples;
        }

        
        
        /**
         * Flips the Blocks on the given BlockPos
         */
        public static void flipLiquids(World world, BlockPos pos1, BlockPos pos2) {
            IBlockState state1 = world.getBlockState(pos1);
            IBlockState state2 = world.getBlockState(pos2);
            Block block1 = RealisticFiniteFluidFunctions.getBlock(world, pos1, state1);
            Block block2 = RealisticFiniteFluidFunctions.getBlock(world, pos2, state2);

            int type1 = FiniteFluidLogic.GeneralPurposeLogic.getFluidIndex(block1);
            int type2 = FiniteFluidLogic.GeneralPurposeLogic.getFluidIndex(block2);

            if (type1 != -1 && type2 != -1) {
                Block newBlock1 = ((NewFluidType)liquids.get(type2)).flowingBlock;
                Block newBlock2 = ((NewFluidType)liquids.get(type1)).flowingBlock; 
                
        		IRealisticFiniteFluid realisticFluid1 = ((IRealisticFiniteFluid)newBlock1);
        		IRealisticFiniteFluid realisticFluid2 = ((IRealisticFiniteFluid)newBlock2);

                //int meta1 = block1.getMetaFromState(state1);
                //int meta2 = block2.getMetaFromState(state2);
                int meta1 = realisticFluid1.getVolume(world, pos1, state1);
                int meta2 = realisticFluid2.getVolume(world, pos2, state2);

                //world.setBlockState(pos1, newBlock1.getStateFromMeta(meta2), 3);
                realisticFluid1.setBlockState(world, pos2, pos1, realisticFluid1.setVolume(null, null, state1, meta2)); // newBlock1.getStateFromMeta(meta2));
                //world.setBlockState(pos2, newBlock2.getStateFromMeta(meta1), 3);
                realisticFluid2.setBlockState(world, pos1, pos2, realisticFluid2.setVolume(null, null, state2, meta1)); // newBlock2.getStateFromMeta(meta1));
            }
        }
        
        
    	
    	/**
    	 * If the Block at the current Pos is a RealisticFiniteFluid OF THE SAME INDEX OF onFiniteFluidIndex
    	 * @return true if the Block at the current Pos is a Realistic Finite Fluid OF THE SAME INDEX
    	 */
        public static boolean isFiniteFluid(IBlockAccess world, BlockPos pos) {
            //return isFiniteFluid(world.getBlockState(pos));
            NewFluidType type = (NewFluidType) liquids.get(onFiniteFluidIndex);
            Block block = RealisticFiniteFluidFunctions.getBlock(world, pos, world.getBlockState(pos));
            return block == type.flowingBlock || block == type.stillBlock;
        }
        

    	/**
    	 * If the current Block is a RealisticFiniteFluid OF THE SAME INDEX OF onFiniteFluidIndex
    	 * @return true if the current Block is a Realistic Finite Fluid OF THE SAME INDEX
    	 */
        /*public static boolean isFiniteFluid(IBlockState state) {
            return isFiniteFluid(RealisticFiniteFluidFunctions.getBlock(state));
        }*/

    	/**
    	 * If the current Block is a RealisticFiniteFluid OF THE SAME INDEX OF onFiniteFluidIndex
    	 * @return true if the current Block is a Realistic Finite Fluid OF THE SAME INDEX
    	 */
        /*public static boolean isFiniteFluid(Block block) {
            NewFluidType type = (NewFluidType) liquids.get(onFiniteFluidIndex);
            //System.out.println("[RFF]");
            //System.out.println("[RFF] type.name: "+type.name);
            //System.out.println("[RFF] block: "+block.toString());
            return block == type.flowingBlock || block == type.stillBlock;
        }*/

    	/**
    	 * If the current Block is a RealisticFiniteFluid OF THE SAME INDEX OF onFiniteFluidIndex, counting the Ocean Blocks too.
    	 * @return true if the current Block is a Realistic Finite Fluid OF THE SAME INDEX
    	 */
        public static boolean isAnyFiniteFluid(World world, BlockPos pos) {
            Block block = RealisticFiniteFluidFunctions.getBlock(world, pos, world.getBlockState(pos));
            //NewFluidType type = (NewFluidType) liquids.get(onFiniteFluidIndex);
            //THIS SHIT MIGHT CRASH --> 
    		IRealisticFiniteFluid realisticFluid = ((IRealisticFiniteFluid)liquids.get(onFiniteFluidIndex).flowingBlock);

            return realisticFluid.isOceanBlock(world, pos, null, onFiniteFluidIndex) || isFiniteFluid(world, pos);
        }


    	/**
    	 * If the current Block is a RealisticFiniteFluid OF THE SAME INDEX OF onFiniteFluidIndex, EXCEPT Ocean Blocks.
    	 * @return true if the current Block is a Realistic Finite Fluid OF THE SAME INDEX
    	 */
        public static boolean isFiniteFluidNoOcean(@Nullable IBlockAccess world, @Nullable BlockPos pos, IBlockState state) {
            //NewFluidType type = (NewFluidType) liquids.get(onFiniteFluidIndex);
    		IRealisticFiniteFluid realisticFluid = ((IRealisticFiniteFluid)liquids.get(onFiniteFluidIndex).flowingBlock);

            return (!realisticFluid.isOceanBlock(world, pos, null, onFiniteFluidIndex)) 
            		&& isFiniteFluid(world, pos);
        }


        /**
         * Whether the block at the current pos is from the same index to compare.
         * @return True of the block is from the same index
         */
        public static boolean isSameIndexFluid(IBlockAccess world, BlockPos pos, int indexToCompare) {
            return isSameIndexFluid(RealisticFiniteFluidFunctions.getBlock(world, pos, world.getBlockState(pos)), indexToCompare);
        }
        

        /**
         * Whether the block is from the same index to compare.
         * @return True of the block is from the same index
         */
        public static boolean isSameIndexFluid(Block blockToCompare, int indexToCompare) {
            return getFluidIndex(blockToCompare) == indexToCompare;
        }
        
        public static boolean isDifferentIndexFluid(Block blockToCompare, int indexToCompare2)
        {
        	int indexToCompare1 = getFluidIndex(blockToCompare);
            return indexToCompare1 != -1 && indexToCompare1 != indexToCompare2;
        }


        /**
         * Checks if the Block at the current Pos is from the same given fluidIndex AND if it is Flowing or Ocean Block
         */
        public static boolean isFullWaterRender(IBlockAccess world, BlockPos pos, int fluidIndex) {
            Block block = RealisticFiniteFluidFunctions.getBlock(world, pos, world.getBlockState(pos));
    		IRealisticFiniteFluid realisticFluid = ((IRealisticFiniteFluid)liquids.get(onFiniteFluidIndex).flowingBlock);
            return block == ((NewFluidType)liquids.get(fluidIndex)).flowingBlock ? true : realisticFluid.isOceanBlock(world, pos, null, fluidIndex);
        }


        /**
         * Calculates if the fluid should flow into the second BlockPos using an algorithm of Effective Adyacent LEVELs average (promedio del valor LEVELs de los 8 bloques aledaños)
         * Check {@link RealisticFiniteFluidFunctions#shouldFlowToNeighbor(IBlockAccess, BlockPos, BlockPos)} for more info.
         */
        public static boolean shouldFlowToNeighbor(IBlockAccess world, BlockPos center, BlockPos exclude) {
            //Block block = ((NewFluidType)liquids.get(onFiniteFluidIndex)).flowingBlock;
            Block block = liquids.get(getFluidIndex(RealisticFiniteFluidFunctions.getBlock(world, center, world.getBlockState(center))) ).flowingBlock;

            if (block instanceof IRealisticFiniteFluid) {
            	IRealisticFiniteFluid fluid = (IRealisticFiniteFluid) block;
                return fluid.shouldFlowToNeighbor(world, center, exclude);
            } else {
                return false;
            }
        }
        
        /**
         * Calculates the *average* of Fluid LEVELs in the 8 adyacent positions
         */
        public static float calculateNeighborWaterLevel(IBlockAccess world, BlockPos center, BlockPos exclude) { //getAvg
    		IRealisticFiniteFluid realisticFluid = ((IRealisticFiniteFluid)RealisticFiniteFluidFunctions.getBlock(world, center, world.getBlockState(center)));
            int totalLevel = realisticFluid.getConceptualVolume(world, center, world.getBlockState(center)); //world.getBlockState(center).getValue(BlockFiniteFluid.LEVEL) + 1;
            int count = 1;

            boolean hasWest = false;
            boolean hasEast = false;
            boolean hasNorth = false;
            boolean hasSouth = false;

            // Cardinales
            if (!center.west().equals(exclude) && isFiniteFluid(world, center.west())) {
                totalLevel += realisticFluid.getConceptualVolume(world, center.west(), world.getBlockState(center.west())); //world.getBlockState(center.west()).getValue(BlockFiniteFluid.LEVEL) + 1;
                count++;
                hasWest = true;
            }

            if (!center.east().equals(exclude) && isFiniteFluid(world, center.east())) {
                totalLevel += realisticFluid.getConceptualVolume(world, center.east(), world.getBlockState(center.east())); //world.getBlockState(center.east()).getValue(BlockFiniteFluid.LEVEL) + 1;
                count++;
                hasEast = true;
            }

            if (!center.north().equals(exclude) && isFiniteFluid(world, center.north())) {
                totalLevel += realisticFluid.getConceptualVolume(world, center.north(), world.getBlockState(center.north())); //world.getBlockState(center.north()).getValue(BlockFiniteFluid.LEVEL) + 1;
                count++;
                hasNorth = true;
            }

            if (!center.south().equals(exclude) && isFiniteFluid(world, center.south())) {
                totalLevel += realisticFluid.getConceptualVolume(world, center.south(), world.getBlockState(center.south())); //world.getBlockState(center.south()).getValue(BlockFiniteFluid.LEVEL) + 1;
                count++;
                hasSouth = true;
            }

            // Diagonales (solo si al menos uno de los lados existe)
            if ((hasEast || hasSouth)) {
                BlockPos diag = center.east().south();
                if (!diag.equals(exclude) && isFiniteFluid(world, diag)) {
                    totalLevel += realisticFluid.getConceptualVolume(world, diag, world.getBlockState(diag)); //world.getBlockState(diag).getValue(BlockFiniteFluid.LEVEL) + 1;
                    count++;
                }
            }

            if ((hasWest || hasNorth)) {
                BlockPos diag = center.west().north();
                if (!diag.equals(exclude) && isFiniteFluid(world, diag)) {
                    totalLevel += realisticFluid.getConceptualVolume(world, diag, world.getBlockState(diag)); //world.getBlockState(diag).getValue(BlockFiniteFluid.LEVEL) + 1;
                    count++;
                }
            }

            if ((hasWest || hasSouth)) {
                BlockPos diag = center.west().south();
                if (!diag.equals(exclude) && isFiniteFluid(world, diag)) {
                    totalLevel += realisticFluid.getConceptualVolume(world, diag, world.getBlockState(diag)); //world.getBlockState(diag).getValue(BlockFiniteFluid.LEVEL) + 1;
                    count++;
                }
            }

            if ((hasEast || hasNorth)) {
                BlockPos diag = center.east().north();
                if (!diag.equals(exclude) && isFiniteFluid(world, diag)) {
                    totalLevel += realisticFluid.getConceptualVolume(world, diag, world.getBlockState(diag)); //world.getBlockState(diag).getValue(BlockFiniteFluid.LEVEL) + 1;
                    count++;
                }
            }
            //System.out.println("totalLevel: "+totalLevel);
            //System.out.println("count: "+count);
            //System.out.println("");

            return (float) totalLevel / count;
        }
        


       
        /**
         * Checks if the SourcePos Block can move into the DestPos Block
         */
		public static boolean canMoveInto(World world, BlockPos destPos, @Nullable BlockPos sourcePos,  @Nullable int currentLevel, @Nullable NewFluidType fluidType) {
            IBlockState state = world.getBlockState(destPos);
            Block block = RealisticFiniteFluidFunctions.getBlock(world, destPos, state);
            if (world.isRemote) return false;
            //if (!world.isRemote) return false;
            
            //CORRECCION MIA
            //Si es agua:
            if (isFiniteFluid(world, destPos) && state.getMaterial() == Material.WATER && world.getBlockState(sourcePos).getMaterial() == Material.LAVA && currentLevel > 5) {
            	//System.out.println("canMoveInto"+block);
            	return true;
            } 
            
            /*if (isFiniteFluid(world, destPos)) {
            	//System.out.println("canMoveInto"+block);
            	return false;
            }*/
            
            // Si es aire
            if (world.isAirBlock(destPos)) return true;

            // Si no hay tipo de liquido actual (seguro nunca pasa, pero por si acaso)
            if (fluidType == null) return true;
            
            //Check para evitar romper otros fluidos
            if (block instanceof IRealisticFiniteFluid) return false;
            
            // Para fluir en bloques FluidLoggeables
            boolean canEnter = FluidloggedUtils.canFluidFlow(world, destPos, state, getFacingBetween(sourcePos, destPos));
            if (canEnter) return true;
            
            // Si no fluye sobre medios bloques, entonces no podemos movernos
            if (!fluidType.flowsOverHalfBlocks) return false;
            
            //Check para evitar romper otros bloques de fluido realistico
        	if (isDifferentIndexFluid(block, getFluidIndex(RealisticFiniteFluidFunctions.getBlock(world, sourcePos, world.getBlockState(sourcePos))))) {
        		return false;
        	}
        	
        	//checks para bloques vanilla que no deberian ser rotos, por ser Replaceable
            if (block instanceof BlockPistonBase || block instanceof BlockPistonExtension || block instanceof BlockFarmland || block instanceof BlockGrassPath || block instanceof BlockIce) {
                return false;
            }
        	
            // Si es reemplazable (fuego, flores, nieve, etc.)
            //POR ALGUN MOTIVO ESTA MADRE ROMPE DE TO-DO, espero que el !instanceof ayude
            /*
            if (block.isReplaceable(world, destPos) && !(block instanceof IRealisticFiniteFluid)) {
                world.destroyBlock(destPos, true);
            	//System.out.println("REPLACE"+block);

                return true;
            }
            */

            // Si el nivel de agua es mayor a 7 y el bloque no es completo (ej: flores, placas, etc.)
            /*
            if (currentLevel > 7 && !state.isFullBlock() && !RealisticFiniteFluidFunctions.getBlock(state).hasTileEntity() 
            		&& !(RealisticFiniteFluidFunctions.getBlock(state) instanceof IRealisticFiniteFluid)) {
                world.destroyBlock(destPos, true);
                return true;
            }
            */
            

            return false;
        }
        
        public static boolean canMoveIntoForRender(IBlockAccess world, BlockPos toPos, @Nullable BlockPos fromPos,  @Nullable int currentLevel, @Nullable NewFluidType fluidType) {
            IBlockState state = world.getBlockState(toPos);
            Block block = RealisticFiniteFluidFunctions.getBlock(world, toPos, state);

            //CORRECCION MIA
            //Si es agua:
            if (isFiniteFluid(world, toPos) && state.getMaterial() == Material.WATER && world.getBlockState(fromPos).getMaterial() == Material.LAVA && currentLevel > 5) {
            	//System.out.println("canMoveInto"+block);
            	return true;
            } else if (isFiniteFluid(world, toPos)) {
            	//System.out.println("canMoveInto"+block);

            	return false;
            }
            
            // Si es aire
            if (world.isAirBlock(toPos)) return true;

            // Si no hay tipo de liquido actual (seguro nunca pasa, pero por si acaso)
            if (fluidType == null) return true;

            // Si no fluye sobre medios bloques, entonces no podemos movernos
            if (!fluidType.flowsOverHalfBlocks) return false;

            //Check para evitar romper liquidos de otros mods
            if (block instanceof BlockFluidClassic) return false;
            
            //Check para evitar romper otros bloques de fluido realistico
        	if (isDifferentIndexFluid(block, getFluidIndex(RealisticFiniteFluidFunctions.getBlock(world, fromPos, world.getBlockState(fromPos))))) {
        		return false;
        	}
        	
        	//checks para bloques vanilla que no deberian ser rotos, por ser Replaceable
            if (block instanceof BlockPistonBase || block instanceof BlockPistonExtension || block instanceof BlockFarmland || block instanceof BlockGrassPath || block instanceof BlockIce) {
                return false;
            }
        	
            // Si es reemplazable (fuego, flores, nieve, etc.)
            if (block.isReplaceable(world, toPos)) {
            	//System.out.println("REPLACE"+block);

                return true;
            }

            // Si el nivel de agua es mayor a 7 y el bloque no es completo (ej: flores, placas, etc.)
            if (currentLevel > 7 && !state.isFullBlock() && !RealisticFiniteFluidFunctions.getBlock(world, toPos, state).hasTileEntity()) {
                return true;
            }
            

            return false;
        }
        
        
        public static boolean liquidMoveForRender(IBlockAccess world, BlockPos sourcePos, BlockPos destPos, boolean doMove, int recursionDepth) { 
            IBlockState sourceState = world.getBlockState(sourcePos);
            Block sourceBlock = RealisticFiniteFluidFunctions.getBlock(world, sourcePos, sourceState);
            if (!(sourceBlock instanceof IRealisticFiniteFluid)) return false; //CHECAR QUE ESTO NO ROMPA EL FUNCIONAMIENTO DEL AGUA
    		IRealisticFiniteFluid realisticFluid = ((IRealisticFiniteFluid)sourceBlock);
    		int sourceLevel = realisticFluid.getVolume(world, sourcePos, sourceState);

            IBlockState destState = world.getBlockState(destPos);
            Block destBlock = RealisticFiniteFluidFunctions.getBlock(world, destPos, destState);
            int destLevel = destBlock instanceof IRealisticFiniteFluid ? realisticFluid.getVolume(world, destPos, destState) : -1;

            setCurrentFluidIndex(sourceBlock);
            NewFluidType fluid = (NewFluidType) liquids.get(onFiniteFluidIndex);

            boolean shouldSearchOutward = false;
            if (fluid.flowingBlock instanceof IRealisticFiniteFluid) {
                shouldSearchOutward = ((IRealisticFiniteFluid) fluid.flowingBlock).shouldSearchOutward(fluid.flowingBlock.getDefaultState().getMaterial());
            }


            //Calculos horizontales
            if (shouldSearchOutward && recursionDepth < 32) {
                for (EnumFacing dir : EnumFacing.HORIZONTALS) {
                    BlockPos neighbor = destPos.offset(dir);
                    if (!neighbor.equals(sourcePos) && isFiniteFluid(world, neighbor)) {
                        int neighborLevel = realisticFluid.getVolume(world, neighbor, world.getBlockState(neighbor));
                        if (destLevel > neighborLevel) {
                            return liquidMoveForRender(world, sourcePos, neighbor, doMove, recursionDepth + 1);
                        }
                    }
                }

            } else if (!isFiniteFluid(world, destPos)) {
                destLevel = -1;
            }
            
            //Reducir deuda de calculos
            int dy = sourcePos.getY() - destPos.getY(); //Es la diferencia entre la altura de nuestro bloque contra el que estamos comparando
            //Idealmente tenddria que salir un resultado positivo
            if (doMove && dy == 0 && Math.abs(destLevel - sourceLevel) < 3 && getCalc() > maxCalc * 0.6f) {
                //--calcAmt;
                return true;
            }

            
            //Calculos verticales 
            if (dy == -getFluidGravity()) {
                if (FiniteFluidLogic.GeneralPurposeLogic.canMoveIntoForRender(world, destPos, sourcePos, sourceLevel, fluid)) {
                    return true;
                } 
                
                //Si el destino tiene agua y tiene espacio para niveles de agua
                //Ecualizacion Vertical
                if (isFiniteFluid(world, destPos) && destLevel < References.MAXIMUM_LEVEL) {
                    return true;
                }
                return false;
            }
        	
            //Ecualizacion horizontal
            if (isFiniteFluid(world, destPos)) {
                // Ecualizacion normal si son del mismo material
                if (shouldFlowToNeighbor(world, sourcePos, destPos) && destLevel < References.MAXIMUM_LEVEL && sourceLevel > References.MINIMUM_LEVEL) {
                    return true;
                }
                
                
                
                //Division de nuestra agua en otros bloques, horizontalmente hablando
            } else if (FiniteFluidLogic.GeneralPurposeLogic.canMoveIntoForRender(world, destPos, sourcePos, sourceLevel, fluid) && sourceLevel > References.MINIMUM_LEVEL) {
                return true;
            }
            return false;
        }
        
        
        public static boolean canMove(World world, BlockPos pos, int level) {
            if (world.isRemote) return false;
            IBlockState actualState = world.getBlockState(pos);

            BlockPos[] targets = new BlockPos[] {
                pos.down(-getFluidGravity()),
                pos.add(-1, getFluidGravity(), 0),
                pos.add(1, getFluidGravity(), 0),
                pos.add(0, getFluidGravity(), -1),
                pos.add(0, getFluidGravity(), 1),
                pos.add(-1, 0, 0),
                pos.add(1, 0, 0),
                pos.add(0, 0, -1),
                pos.add(0, 0, 1)
            };

            for (BlockPos target : targets) {
                if (FiniteFluidsLogic.liquidMove(world, pos, target, false, 0)) return true;
                
                //No recuerdo porque añadi esto :'v
                /*if (level > BlockFiniteFluid.MINIMUM_CONCEPTUAL_LEVEL) {
                	liquidMove(world, pos, target, true, 0);
                }*/
                
                //Para que la lava pueda meterse sobre el agua, aunque dudo que sea necesario si el agua interactua con la lava y 
                //se evapora
                if (actualState.getMaterial() == Material.LAVA && ( world.getBlockState(target).getMaterial() == Material.WATER)) {
                	return true;
                }
            }
            
            BlockPos up = pos.up();
            IBlockState aboveState = world.getBlockState(up);
            Material materialAbove = aboveState.getMaterial();

            if (actualState.getMaterial() == Material.LAVA && materialAbove == Material.WATER) {
                return true; // Fuerza la conversión a flowing si hay líquido encima
            }
            

            return false;
        }
        
        public static boolean canMoveForRender(IBlockAccess world, BlockPos pos, int level) {
            IBlockState actualState = world.getBlockState(pos);

            BlockPos[] targets = new BlockPos[] {
                pos.down(-getFluidGravity()),
                pos.add(-1, getFluidGravity(), 0),
                pos.add(1, getFluidGravity(), 0),
                pos.add(0, getFluidGravity(), -1),
                pos.add(0, getFluidGravity(), 1),
                pos.add(-1, 0, 0),
                pos.add(1, 0, 0),
                pos.add(0, 0, -1),
                pos.add(0, 0, 1)
            };

            for (BlockPos target : targets) {
                if (liquidMoveForRender(world, pos, target, false, 0)) return true;
                
                //No recuerdo porque añadi esto :'v
                /*if (level > BlockFiniteFluid.MINIMUM_CONCEPTUAL_LEVEL) {
                	liquidMove(world, pos, target, true, 0);
                }*/
                
                //Para que la lava pueda meterse sobre el agua, aunque dudo que sea necesario si el agua interactua con la lava y 
                //se evapora
                if (actualState.getMaterial() == Material.LAVA && ( world.getBlockState(target).getMaterial() == Material.WATER)) {
                	return true;
                }
            }
            
            BlockPos up = pos.up();
            IBlockState aboveState = world.getBlockState(up);
            Material materialAbove = aboveState.getMaterial();

            if (actualState.getMaterial() == Material.LAVA && materialAbove == Material.WATER) {
                return true; // Fuerza la conversión a flowing si hay líquido encima
            }
            

            return false;
        }
        
        /**
         * If the current pos is AIR and it doesn't have (the same) fluid adyacent or below it
         * @param fluidIndex The index to compare
         */
        //isLDWater --> isLinearDropWater?
        public static boolean isOnlyVerticallyFallingFluid(IBlockAccess world, BlockPos pos, int fluidIndex) {
            BlockPos below = pos.down(-getFluidGravity());
            return world.isAirBlock(pos)
                && isSameIndexFluid(world, below, fluidIndex)
                && !isSameIndexFluid(world, pos.west(), fluidIndex)
                && !isSameIndexFluid(world, pos.east(), fluidIndex)
                && !isSameIndexFluid(world, pos.north(), fluidIndex)
                && !isSameIndexFluid(world, pos.south(), fluidIndex);
        }
        
        public static EnumFacing getFacingBetween(BlockPos source, BlockPos dest) {
            BlockPos delta = dest.subtract(source);
            return EnumFacing.getFacingFromVector(
                delta.getX(),
                delta.getY(),
                delta.getZ()
            );
        }
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        

        //Miscellaneous
		public static void addCalc()
        {
            ++calcAmt;
        }

        public static int getCalc()
        {
            return calcAmt;
        }

        public static int getMaxCalc()
        {
            return maxCalc;
        }

        public static double getPlayerDistanceToCalc()
        {
            return (double)playerMaxDistanceToCalc;
        } 
        
        
   
    	

    }
    
    
    
    
    public static class PressureSystemLogic {

		// shouldPressure: (meta >= 13 y hay agua en pos.down(grav()))
		public static boolean shouldPressure(World world, BlockPos pos) {
		    if (!GeneralPurposeLogic.isAnyFiniteFluid(world, pos)) {
		        //System.out.println("[DEBUG] shouldPressure FALSE (no es agua) pos=" + pos);
		        return false;
		    }
		    IBlockState s = world.getBlockState(pos);
			IRealisticFiniteFluid realisticFluid = ((IRealisticFiniteFluid)RealisticFiniteFluidFunctions.getBlock(world, pos, s));
		    int level = realisticFluid.getVolume(world, pos, s);
		    boolean result = level > References.Q3_HIGH && GeneralPurposeLogic.isAnyFiniteFluid(world, pos.down(-GeneralPurposeLogic.getFluidGravity()));
		    //System.out.println("[DEBUG] shouldPressure pos=" + pos + " level=" + level + " grav=" + grav() + " -> " + result);
		    return result;
		}

		// shouldPressureReverse: (solo si es aire en pos, hay agua en pos.down(grav()) y meta(pos.down()) > 7)
		// OJO: la version antigua usa (y - 1) para el meta, sin multiplicar por grav(). Lo dejamos igual.
		public static boolean shouldPressureReverse(World world, BlockPos pos) {
		    boolean isAir = world.isAirBlock(pos);
		    if (!isAir) {
		        //System.out.println("[DEBUG] shouldPressureReverse FALSE (no es aire) pos=" + pos);
		        return false;
		    }
		    boolean waterUnderByGrav = GeneralPurposeLogic.isAnyFiniteFluid(world, pos.down(-GeneralPurposeLogic.getFluidGravity()));
		    int metaAtOneBelow = -1;
		    BlockPos oneBelow = pos.down(); // EXACTO como el original (y - 1), NO usa grav() aquí
		    IBlockState st = world.getBlockState(oneBelow);
		    if (RealisticFiniteFluidFunctions.getBlock(world, oneBelow, st) instanceof IRealisticFiniteFluid) {
				IRealisticFiniteFluid realisticFluid = ((IRealisticFiniteFluid)RealisticFiniteFluidFunctions.getBlock(world, oneBelow, st));
		        metaAtOneBelow = realisticFluid.getVolume(world, oneBelow, st);
		    }
		    boolean result = waterUnderByGrav & (metaAtOneBelow > 7);
		    //System.out.println("[DEBUG] shouldPressureReverse pos=" + pos + " grav=" + grav()
		            //+ " waterUnderByGrav=" + waterUnderByGrav + " meta(oneBelow)=" + metaAtOneBelow + " -> " + result);
		    return result;
		}

		public static boolean checkPressure(World world, BlockPos source, BlockPos current, int depth, ArrayList<BlockPos> visited) {
		    if (world.isRemote) return false;
		
		    if (depth == 0) {
		        if (!shouldPressure(world, source)) {
		            //System.out.println("[DEBUG] checkPressure ABORT start: shouldPressure=false source=" + source);
		            return false;
		        }
		        stopPCheck = false;
		    }
		
		    if (stopPCheck) {
		        //System.out.println("[DEBUG] checkPressure stopPCheck=true source=" + source + " current=" + current + " depth=" + depth);
		        return false;
		    }
		    if (depth > pressureLimit) {
		        //System.out.println("[DEBUG] checkPressure depth>limit source=" + source + " depth=" + depth);
		        return false;
		    }
		
		    // Misma lógica del original para NO ir en contra de la gravedad:
		    if (GeneralPurposeLogic.getFluidGravity() <= 0) {
		        if (current.getY() > source.getY()) {
		            //System.out.println("[DEBUG] checkPressure bloqueado por gravedad (grav>0 y current.y>source.y) src=" + source + " cur=" + current);
		            return false;
		        }
		    } else {
		        if (current.getY() < source.getY()) {
		            //System.out.println("[DEBUG] checkPressure bloqueado por gravedad (grav<0 y current.y<source.y) src=" + source + " cur=" + current);
		            return false;
		        }
		    }
		
		    //Block blockAtCurrent = world.getBlockState(current).getBlock();
		    Block blockAtSource  = RealisticFiniteFluidFunctions.getBlock(world, source, world.getBlockState(source));
		
		    // if (!isAWater(var9) & var9 != 0) -> si no es fluido y no es aire, aborta
		    if (!GeneralPurposeLogic.isAnyFiniteFluid(world, current) & !world.isAirBlock(current)) {
		        //System.out.println("[DEBUG] checkPressure current no es agua ni aire. current=" + current + " block=" + blockAtCurrent.getLocalizedName());
		        return false;
		    }
		
		    if (nodeContains(visited, current)) {
		        //System.out.println("[DEBUG] checkPressure ya visitado current=" + current + " depth=" + depth);
		        return false;
		    }
		
		    // El original limpia el nodo y sus 6 vecinos de ambas colas
		    removeFromPressure(current);
		    removeFromPressure(current.west());
		    removeFromPressure(current.east());
		    removeFromPressure(current.down());
		    removeFromPressure(current.up());
		    removeFromPressure(current.north());
		    removeFromPressure(current.south());
		
		    int metaCurrent = References.MINIMUM_LEVEL;
		    IBlockState curState = world.getBlockState(current);
		    if (RealisticFiniteFluidFunctions.getBlock(world, current, curState) instanceof IRealisticFiniteFluid) {
				IRealisticFiniteFluid realisticFluid = ((IRealisticFiniteFluid)RealisticFiniteFluidFunctions.getBlock(world, current, curState));
		        metaCurrent = realisticFluid.getVolume(world, current, curState);
		    }
		
		    if (metaCurrent <= References.Q2_HIGH) {
		        byte defaultMeta = References.Q2_HIGH;
		        boolean isAir = world.isAirBlock(current);
		        if (isAir) defaultMeta = References.Q2_LOW;
		
		        int newMetaAtCurrent = metaCurrent + References.Q2_LOW;
		
		        // setCurrentWater(block) -> usamos el bloque en source
		        GeneralPurposeLogic.setCurrentFluidIndex(blockAtSource);
		
		        // ((liquids.get(onFiniteFluidIndex)).flow) con meta newMetaAtCurrent
		        NewFluidType fluid = (NewFluidType) liquids.get(onFiniteFluidIndex);
		        IBlockState oldCur = world.getBlockState(current);
		
				IRealisticFiniteFluid realisticFluid = ((IRealisticFiniteFluid)liquids.get(onFiniteFluidIndex).flowingBlock);//world.setBlockState(current, fluid.flowingBlock.getDefaultState().withProperty(BlockFiniteFluid.LEVEL, newMetaAtCurrent), 3);
				realisticFluid.setBlockState(world, null, current, realisticFluid.setVolume(null, null, fluid.flowingBlock.getDefaultState(), newMetaAtCurrent));
		        world.notifyBlockUpdate(current, oldCur, world.getBlockState(current), 3);
		
		        IBlockState oldSrc = world.getBlockState(source);
		        //world.setBlockState(source, fluid.flowingBlock.getDefaultState().withProperty(BlockFiniteFluid.LEVEL, (int)var18), 3);
		        realisticFluid.setBlockState(world, null, source, realisticFluid.setVolume(null, null, fluid.flowingBlock.getDefaultState(), (int)defaultMeta));
		        world.notifyBlockUpdate(source, oldSrc, world.getBlockState(source), 3);
		
		        stopPCheck = true;
		
		        Random rnd = new Random();
		        boolean flipOrder = false;
		        int xSign = 1;
		        int zSign = 1;
		        if (rnd.nextInt(2) == 0) flipOrder = true;
		        if (rnd.nextInt(2) == 0) xSign = -1;
		        if (rnd.nextInt(2) == 0) zSign = -1;
		
		        // MISMO ORDEN Y MISMOS DESPLAZAMIENTOS QUE EL ORIGINAL
		        if (flipOrder) {
		            addToPressure(world, source.down(-GeneralPurposeLogic.getFluidGravity()), false);
		            addToPressure(world, source.add(-1 * -xSign, 0, 0), false);
		            addToPressure(world, source.add( 1 * -xSign, 0, 0), false);
		            addToPressure(world, source.add(0, 0, 1 * zSign), false);
		            addToPressure(world, source.add(0, 0,  -1 * zSign), false);
		        } else {
		            addToPressure(world, source.add(0, 0, 1 * zSign), false);
		            addToPressure(world, source.add(0, 0,  -1 * zSign), false);
		            addToPressure(world, source.add(-1 * -xSign, 0, 0), false);
		            addToPressure(world, source.add( 1 * -xSign, 0, 0), false);
		            addToPressure(world, source.down(-GeneralPurposeLogic.getFluidGravity()), false);
		        }
		
		        //System.out.println("[DEBUG] checkPressure TRANSFER src=" + source + " cur=" + current
		               //+ " metaCur(old)=" + metaCurrent + " -> set cur=" + newMetaAtCurrent + " src=" + var18);
		        return true;
		    } else {
		        // Recursión con aleatorios exactamente como el original
		        visited.add(current);
		        Random rnd = new Random();
		        int xStep = (rnd.nextInt(2) == 0) ? -1 : 1;
		        int yStep = (rnd.nextInt(2) == 0) ? -1 : 1;
		        int zStep = (rnd.nextInt(2) == 0) ? -1 : 1;
		        boolean order = (rnd.nextInt(2) == 0);
		
		        if (order) {
		            if (checkPressure(world, source, current.add(-xStep, 0, 0), depth + 1, visited)) return true;
		            if (checkPressure(world, source, current.add( xStep, 0, 0), depth + 1, visited)) return true;
		            if (checkPressure(world, source, current.add(0, -yStep * GeneralPurposeLogic.getFluidGravity(), 0), depth + 1, visited)) return true;
		            if (checkPressure(world, source, current.add(0,  yStep * GeneralPurposeLogic.getFluidGravity(), 0), depth + 1, visited)) return true;
		            if (checkPressure(world, source, current.add(0, 0, -zStep), depth + 1, visited)) return true;
		            if (checkPressure(world, source, current.add(0, 0,  zStep), depth + 1, visited)) return true;
		        } else {
		            if (checkPressure(world, source, current.add(0,  yStep * GeneralPurposeLogic.getFluidGravity(), 0), depth + 1, visited)) return true;
		            if (checkPressure(world, source, current.add( xStep, 0, 0), depth + 1, visited)) return true;
		            if (checkPressure(world, source, current.add(0, 0, -zStep), depth + 1, visited)) return true;
		            if (checkPressure(world, source, current.add(0, -yStep * GeneralPurposeLogic.getFluidGravity(), 0), depth + 1, visited)) return true;
		            if (checkPressure(world, source, current.add(-xStep, 0, 0), depth + 1, visited)) return true;
		            if (checkPressure(world, source, current.add(0, 0,  zStep), depth + 1, visited)) return true;
		        }
		
		        //System.out.println("[DEBUG] checkPressure SIN CAMINO src=" + source + " ended at cur=" + current + " depth=" + depth);
		        return false;
		    }
		}

		public static boolean checkPressureReverse(World world, BlockPos source, BlockPos current, int depth, ArrayList<BlockPos> visited) {
		    if (world.isRemote) return false;
		
		    if (depth == 0) {
		        if (!shouldPressureReverse(world, source)) {
		            //System.out.println("[DEBUG] checkPressureReverse ABORT start: shouldPressureReverse=false source=" + source);
		            return false;
		        }
		        stopPCheck = false;
		    }
		
		    if (stopPCheck) {
		        //System.out.println("[DEBUG] checkPressureReverse stopPCheck=true source=" + source + " current=" + current + " depth=" + depth);
		        return false;
		    }
		    if (depth > pressureLimit) {
		        //System.out.println("[DEBUG] checkPressureReverse depth>limit source=" + source + " depth=" + depth);
		        return false;
		    }
		
		    //Block blockAtCurrent = world.getBlockState(current).getBlock();
		    Block blockAtSource = RealisticFiniteFluidFunctions.getBlock(world, source, world.getBlockState(source));
		
		    // if (!isWater(var9) & var7 != 0) -> si current no es agua y depth>0, corta
		    if (!GeneralPurposeLogic.isAnyFiniteFluid(world, current) & depth != 0) {
		        //System.out.println("[DEBUG] checkPressureReverse current no es agua y depth>0. current=" + current);
		        return false;
		    }
		
		    if (nodeContains(visited, current)) {
		        //System.out.println("[DEBUG] checkPressureReverse ya visitado current=" + current + " depth=" + depth);
		        return false;
		    }
		
		    IBlockState curState = world.getBlockState(current);
		    //int metaCurrent = (curState.getBlock() instanceof BlockFiniteFluid) 
		    //		? BlockFiniteFluid.getVolume(world, current, curState) : -1;
		
		    int metaCurrent = -1;
		    if (RealisticFiniteFluidFunctions.getBlock(world, current, curState) instanceof IRealisticFiniteFluid) {
		        IRealisticFiniteFluid realisticFluid = ((IRealisticFiniteFluid)RealisticFiniteFluidFunctions.getBlock(world, current, curState));
		        metaCurrent = realisticFluid.getVolume(world, current, curState);
		    }
		                      
		    // if (var12 > 7 & var5 >= var2 & var7 > 0)
		    if (metaCurrent > References.Q2_LOW & current.getY() >= source.getY() & depth > 0 && (blockAtSource instanceof IRealisticFiniteFluid)) {
		        int newMetaAtCurrent = metaCurrent - References.Q2_HIGH;
		
		        // setCurrentWater(block) usando el bloque del source
		        ////System.out.println("LMOVE Position: "+ source);
		        ////System.out.println("LMOVE Block: "+ blockAtSource);
		        GeneralPurposeLogic.setCurrentFluidIndex(blockAtSource);
		
		        NewFluidType fluid = (NewFluidType) liquids.get(onFiniteFluidIndex);
		        IBlockState oldCur = world.getBlockState(current);
		
		        IRealisticFiniteFluid realisticFluid = ((IRealisticFiniteFluid)liquids.get(onFiniteFluidIndex).flowingBlock);//world.setBlockState(current, fluid.flowingBlock.getDefaultState().withProperty(BlockFiniteFluid.LEVEL, newMetaAtCurrent), 3);
		        //world.setBlockState(current, fluid.flowingBlock.getDefaultState().withProperty(BlockFiniteFluid.LEVEL, newMetaAtCurrent), 3);
		        realisticFluid.setBlockState(world, null, current, realisticFluid.setVolume(null, null, fluid.flowingBlock.getDefaultState(), newMetaAtCurrent));
		        world.notifyBlockUpdate(current, oldCur, world.getBlockState(current), 3);
		
		        IBlockState oldSrc = world.getBlockState(source);
		        //world.setBlockState(source, fluid.flowingBlock.getDefaultState().withProperty(BlockFiniteFluid.LEVEL, 7), 3);
		        realisticFluid.setBlockState(world, null, source, realisticFluid.setVolume(null, null, fluid.flowingBlock.getDefaultState(), 7));
		        world.notifyBlockUpdate(source, oldSrc, world.getBlockState(source), 3);
		
		        stopPCheck = true;
		
		        Random rnd = new Random();
		        boolean flip = false;
		        int xSign = 1;
		        int zSign = 1;
		        if (rnd.nextInt(2) == 0) flip = true;
		        if (rnd.nextInt(2) == 0) xSign = -1;
		        if (rnd.nextInt(2) == 0) zSign = -1;
		
		        // MISMO ORDEN del original, alrededor de "current"
		        if (flip) {
		            addToPressure(world, current.down(-GeneralPurposeLogic.getFluidGravity()), false);
		            addToPressure(world, current.add(-1 * -xSign, 0, 0), false);
		            addToPressure(world, current.add( 1 * -xSign, 0, 0), false);
		            addToPressure(world, current.add(0, 0, 1 * zSign), false);
		            addToPressure(world, current.add(0, 0,  -1 * zSign), false);
		        } else {
		            addToPressure(world, current.add(0, 0, 1 * zSign), false);
		            addToPressure(world, current.add(0, 0,  -1 * zSign), false);
		            addToPressure(world, current.add(-1 * -xSign, 0, 0), false);
		            addToPressure(world, current.add( 1 * -xSign, 0, 0), false);
		            addToPressure(world, current.down(-GeneralPurposeLogic.getFluidGravity()), false);
		        }
		
		        //System.out.println("[DEBUG] checkPressureReverse TRANSFER src=" + source + " cur=" + current
		                //+ " metaCur(old)=" + metaCurrent + " -> set cur=" + newMetaAtCurrent + " src=7");
		        return true;
		    } else {
		        visited.add(current);
		        Random rnd = new Random();
		        int xStep = (rnd.nextInt(2) == 0) ? -1 : 1;
		        int yStep = (rnd.nextInt(2) == 0) ? -1 : 1;
		        int zStep = (rnd.nextInt(2) == 0) ? -1 : 1;
		        boolean order = (rnd.nextInt(2) == 0);
		
		        if (order) {
		            if (checkPressureReverse(world, source, current.add(-xStep, 0, 0), depth + 1, visited)) return true;
		            if (checkPressureReverse(world, source, current.add( xStep, 0, 0), depth + 1, visited)) return true;
		            if (checkPressureReverse(world, source, current.add(0, -yStep * GeneralPurposeLogic.getFluidGravity(), 0), depth + 1, visited)) return true;
		            if (checkPressureReverse(world, source, current.add(0,  yStep * GeneralPurposeLogic.getFluidGravity(), 0), depth + 1, visited)) return true;
		            if (checkPressureReverse(world, source, current.add(0, 0, -zStep), depth + 1, visited)) return true;
		            if (checkPressureReverse(world, source, current.add(0, 0,  zStep), depth + 1, visited)) return true;
		        } else {
		            if (checkPressureReverse(world, source, current.add(0,  yStep * GeneralPurposeLogic.getFluidGravity(), 0), depth + 1, visited)) return true;
		            if (checkPressureReverse(world, source, current.add( xStep, 0, 0), depth + 1, visited)) return true;
		            if (checkPressureReverse(world, source, current.add(0, 0, -zStep), depth + 1, visited)) return true;
		            if (checkPressureReverse(world, source, current.add(0, -yStep * GeneralPurposeLogic.getFluidGravity(), 0), depth + 1, visited)) return true;
		            if (checkPressureReverse(world, source, current.add(-xStep, 0, 0), depth + 1, visited)) return true;
		            if (checkPressureReverse(world, source, current.add(0, 0,  zStep), depth + 1, visited)) return true;
		        }
		
		        //System.out.println("[DEBUG] checkPressureReverse SIN CAMINO src=" + source + " ended at cur=" + current + " depth=" + depth);
		        return false;
		    }
		}

		// Añadir a presión
		public static void addToPressure(World world, BlockPos pos, boolean reverse) {
		    if (reverse) {
		        if (!nodeContains(rpressure, pos)) {
		            rpressure.add(pos);
		            //System.out.println("[DEBUG] addToPressure (reverse): añadido nodo " + pos);
		        } else {
		            //System.out.println("[DEBUG] addToPressure (reverse): ya existía nodo " + pos);
		        }
		    } else {
		        if (!nodeContains(pressure, pos)) {
		            pressure.add(pos);
		            //System.out.println("[DEBUG] addToPressure: añadido nodo " + pos);
		        } else {
		            //System.out.println("[DEBUG] addToPressure: ya existía nodo " + pos);
		        }
		    }
		}

		// Eliminar de presión
		public static void removeFromPressure(BlockPos pos) {
			nodeRemove(pressure, pos);
			nodeRemove(rpressure, pos);
		    //boolean removed = nodeRemove(pressure, pos) | nodeRemove(rpressure, pos);
		    //System.out.println("[DEBUG] removeFromPressure: nodo " + pos + " eliminado=" + removed);
		}

		// Verificar si lista contiene un nodo
		public static boolean nodeContains(ArrayList<BlockPos> list, BlockPos pos) {
		    for (int i = 0; i < list.size(); i++) {
		        BlockPos check = list.get(i);
		        if (check != null &&
		            check.getX() == pos.getX() &&
		            check.getY() == pos.getY() &&
		            check.getZ() == pos.getZ()) {
		            return true;
		        }
		    }
		    return false;
		}

		// Eliminar nodo específico
		public static boolean nodeRemove(ArrayList<BlockPos> list, BlockPos pos) {
		    if (list.isEmpty()) {
		        return false;
		    }
		
		    boolean removed = false;
		    for (int i = 0; i < list.size(); i++) {
		        BlockPos check = list.get(i);
		        if (check != null &&
		            check.getX() == pos.getX() &&
		            check.getY() == pos.getY() &&
		            check.getZ() == pos.getZ()) {
		            list.remove(i);
		            removed = true;
		            i--; // ojo, porque cambiamos el índice al remover
		        }
		    }
		
		    return removed;
		}
    	
    }
    
    
    
    
    
    
	public static class lavaFunctions {
		
		public static void burnArea(World world, BlockPos centerPos) {
		    // Lista de posiciones adyacentes en las 6 direcciones cardinales
		    BlockPos[] adjacentPositions = new BlockPos[] {
		        centerPos.east(),
		        centerPos.west(),
		        centerPos.north(),
		        centerPos.south(),
		        centerPos.up(),
		        centerPos.down()
		    };

		    for (BlockPos pos : adjacentPositions) {
		        IBlockState state = world.getBlockState(pos);
		        if (state.getMaterial().getCanBurn()) {
		            world.setBlockState(pos, Blocks.FIRE.getDefaultState());
		        }
		    }
		}
		
		
	    public static void triggerLavaMixEffects(World world, BlockPos pos) {
	        // Reproduce el sonido "fizz"
	        world.playSound(
	            null, // player: null para que lo escuchen todos cerca
	            pos.getX() + 0.5, 
	            pos.getY() + 0.5, 
	            pos.getZ() + 0.5, 
	            SoundEvents.BLOCK_FIRE_EXTINGUISH,
	            SoundCategory.BLOCKS,
	            0.5F,
	            2.6F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.8F
	        );

	        // Genera partículas de humo
	        for (int i = 0; i < 8; ++i) {
	            double x = pos.getX() + Math.random();
	            double y = pos.getY() + 1.2D;
	            double z = pos.getZ() + Math.random();
	            world.spawnParticle(EnumParticleTypes.SMOKE_LARGE, x, y, z, 0.0D, 0.0D, 0.0D);
	        }
	    }
		
		
	}
	
	
	public static class FluidWorldInteraction{
		/**
		 * Controls interactions between FiniteFluid's Materials and Vanilla Fluids
		 * @param world
		 * @param pos
		 * @return
		 */
		public static boolean interactWithNeighborLiquid(World world, BlockPos pos) {
		    IBlockState blockState = world.getBlockState(pos);
		    Block block = blockState.getBlock();
		    int type = GeneralPurposeLogic.getFluidIndex(block);
		    
		    
		    BlockPos above = pos.up();
		    Block blockAbove = world.getBlockState(above).getBlock();
		
		    BlockPos below = pos.down();
		    Block blockBelow = world.getBlockState(below).getBlock();
		    int typeBelow = GeneralPurposeLogic.getFluidIndex(blockBelow);
		    if (block instanceof IRealisticFiniteFluid && ((IRealisticFiniteFluid)block).getFluid().isGaseous()) return false;
		
			//System.out.println("block"+block+world.getBlockState(pos).getMaterial().toString());
		
			
			//if (blockAbove == Blocks.WATER || blockAbove == Blocks.FLOWING_WATER) {
		    	//System.out.println("blockAbove: "+blockAbove);
		    	//System.out.println("block: "+block);
		    	//System.out.println("block Mat es Lava:"+ (world.getBlockState(pos).getMaterial() == Material.LAVA));
		    	//System.out.println("       ");        		
			//}
			
		    //Interaccion con agua vanilla --> PARA DARLE OBSIDDIANA A LAS RAVINES
		    if ((blockAbove == Blocks.WATER || blockAbove == Blocks.FLOWING_WATER) && blockState.getMaterial() == Material.LAVA) {
		    	//System.out.println("typeBelow > -1"+(typeBelow));        		
		        if (type > -1) {
		            NewFluidType lavaType = liquids.get(type);
		            IRealisticFiniteFluid realisticFluid = ((IRealisticFiniteFluid)lavaType.flowingBlock);
		
		            if (realisticFluid.isOceanBlock(world, pos, blockState, type)) {
		
		                world.setBlockState(pos, Blocks.OBSIDIAN.getDefaultState());
		                world.playSound(null, pos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.5F, 2.6F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.8F);
		                return true;
		            } else if (block == lavaType.flowingBlock || block == lavaType.stillBlock) {
		            	int lavaLevel = realisticFluid.getVolume(world, pos, blockState);
		            	if (lavaLevel > 9) {
		                    world.setBlockState(pos, Blocks.OBSIDIAN.getDefaultState());
		                    world.playSound(null, pos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.5F, 2.6F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.8F);
		            	} else {
		                    world.setBlockState(pos, Blocks.STONE.getDefaultState());
		                    world.playSound(null, pos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.5F, 2.6F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.8F);
		            	}
		            	
		            }
		        }
		    }
		    
		    if (type == -1) return false;
		
		
		
		    if (typeBelow > -1) {
		        if (((NewFluidType) liquids.get(typeBelow)).gravity > ((NewFluidType) liquids.get(type)).gravity) {
		        	GeneralPurposeLogic.flipLiquids(world, pos, below);
		            return true;
		        }
		
		        if (block instanceof IRealisticFiniteFluid && ((IRealisticFiniteFluid) block).interactWithLiquid(world, pos, below)) {
		            return true;
		        }
		
		        if (blockBelow instanceof IRealisticFiniteFluid && ((IRealisticFiniteFluid) blockBelow).interactWithLiquid(world, below, pos)) {
		            return true;
		        }
		        
		
		    }
		
		    if (block == ((NewFluidType) liquids.get(type)).stillBlock) {
		        for (int i = 0; i < 4; ++i) {
		            int dx = pos.getX();
		            int dy = pos.getY();
		            int dz = pos.getZ();
		
		            if (i == 0) dx -= 1;
		            if (i == 1) dx += 1;
		            if (i == 2) dz -= 1;
		            if (i == 3) dz += 1;
		
		            BlockPos neighborPos = new BlockPos(dx, dy, dz);
		            Block neighborBlock = world.getBlockState(neighborPos).getBlock();
		            int typeNeighbor = GeneralPurposeLogic.getFluidIndex(neighborBlock);
		
		            if (typeNeighbor > -1) {
		                if (
		                    (block == ((NewFluidType) liquids.get(type)).stillBlock || ((NewFluidType) liquids.get(type)).gravity <= -1) &
		                    (neighborBlock == ((NewFluidType) liquids.get(typeNeighbor)).stillBlock || ((NewFluidType) liquids.get(typeNeighbor)).gravity <= -1) &&
		                    ((NewFluidType) liquids.get(typeNeighbor)).gravity != ((NewFluidType) liquids.get(type)).gravity
		                ) {
		                	GeneralPurposeLogic.flipLiquids(world, pos, neighborPos);
		                    return true;
		                }
		
		                if (block instanceof IRealisticFiniteFluid && ((IRealisticFiniteFluid) block).interactWithLiquid(world, pos, neighborPos)) {
		                    return true;
		                }
		
		                if (neighborBlock instanceof IRealisticFiniteFluid && ((IRealisticFiniteFluid) neighborBlock).interactWithLiquid(world, neighborPos, pos)) {
		                    return true;
		                }
		            }
		        }
		    } else {
		        // Es un bloque flowing: revisa solo interacciones ligeras
		        for (int i = 0; i < 4; ++i) {
		            BlockPos neighborPos = pos.offset(EnumFacing.byHorizontalIndex(i));
		            Block neighborBlock = world.getBlockState(neighborPos).getBlock();
		
		            int typeNeighbor = GeneralPurposeLogic.getFluidIndex(neighborBlock);
		            if (typeNeighbor > -1) {
		                if (block instanceof IRealisticFiniteFluid && ((IRealisticFiniteFluid) block).interactWithLiquid(world, pos, neighborPos)) {
		                    return true;
		                }
		
		                if (neighborBlock instanceof IRealisticFiniteFluid && ((IRealisticFiniteFluid) neighborBlock).interactWithLiquid(world, neighborPos, pos)) {
		                    return true;
		                }
		            }
		        }
		    }
		
		    return false;
		}
		
		
		
		public static boolean bucketRemoveFluidEvenLowNEW(World world, BlockPos pos, int level) {
		    IBlockState centerState = world.getBlockState(pos);
		    if (!(centerState.getBlock() instanceof IRealisticFiniteFluid)) return false;

		    IRealisticFiniteFluid centerBlock = (IRealisticFiniteFluid) centerState.getBlock();
		    Fluid targetFluid = centerBlock.getFluid(); // fluido del bloque central

		    int collected = level; // Nivel inicial del bloque central
		    world.setBlockToAir(pos); // Removemos el bloque principal

		    if (collected >= References.MAXIMUM_CONCEPTUAL_LEVEL) return true; // Ya lleno, terminamos

		    // Posiciones laterales y diagonales
		    BlockPos[] laterals = { pos.north(), pos.south(), pos.east(), pos.west() };
		    BlockPos[] diagonals = {
		        pos.north().east(), pos.north().west(),
		        pos.south().east(), pos.south().west()
		    };
		    
		    // 9x9 --> GASES
		    if (collected < References.MAXIMUM_CONCEPTUAL_LEVEL && targetFluid.isGaseous()) {
		    	int radius = 2;

		    	List<BlockPos> around = getPositionsInCube(pos, radius);
		    	sortByHorizontalThenDistance(pos, around);

		    	collected = collectEqually(
		    	    world,
		    	    around.toArray(new BlockPos[0]),
		    	    collected,
		    	    References.MAXIMUM_CONCEPTUAL_LEVEL,
		    	    targetFluid
		    	);
		    }

		    // Recolectar suavemente
		    collected = collectEqually(world, laterals, collected, References.MAXIMUM_CONCEPTUAL_LEVEL, targetFluid);
		    if (collected < References.MAXIMUM_CONCEPTUAL_LEVEL) 
		    	collected = collectEqually(world, diagonals, collected, References.MAXIMUM_CONCEPTUAL_LEVEL, targetFluid);

		    // Última oportunidad: abajo
		    if (collected < References.MAXIMUM_CONCEPTUAL_LEVEL) {
		        BlockPos below = pos.down();
		        IBlockState belowState = world.getBlockState(below);
		        if (belowState.getBlock() instanceof IRealisticFiniteFluid) {
		        	IRealisticFiniteFluid fluidBlock = (IRealisticFiniteFluid) belowState.getBlock();
		            if (fluidBlock.getFluid() == targetFluid) {
	                    IRealisticFiniteFluid realisticFluid = ((IRealisticFiniteFluid)belowState.getBlock());
		                int neighborLevel = realisticFluid.getConceptualVolume(world, below, belowState);
		                int take = Math.min(neighborLevel, References.MAXIMUM_CONCEPTUAL_LEVEL - collected);
		                int newLevel = neighborLevel - take;

		                if (newLevel <= References.MINIMUM_LEVEL) {
		                    if (newLevel == References.MINIMUM_LEVEL) {
		                        //world.setBlockState(below, belowState.withProperty(BlockFiniteFluid.LEVEL, 0)); // nivel mínimo
		                    	realisticFluid.setBlockState(world, null, below, realisticFluid.setVolume(world, below, belowState, References.MINIMUM_LEVEL));
		                    } else {
		                        world.setBlockToAir(below); // newLevel < 0
		                    }
		                } else {
		                	realisticFluid.setBlockState(world, null, below, realisticFluid.setConceptualVolume(world, below, belowState, newLevel));
		                }
		                collected += take;
		            }
		        }
		    }

		    return collected >= References.MAXIMUM_CONCEPTUAL_LEVEL; // True si alcanzó 16 niveles conceptuales, false si no
		}

		
		public static boolean bucketRemoveFluidOnlyFullNEW(World world, BlockPos pos, int level) {
		    IBlockState centerState = world.getBlockState(pos);
		    if (!(centerState.getBlock() instanceof IRealisticFiniteFluid)) return false;

		    IRealisticFiniteFluid centerBlock = (IRealisticFiniteFluid) centerState.getBlock();
		    Fluid targetFluid = centerBlock.getFluid(); // fluido del bloque central

		    int collected = level;

		    // Posiciones a revisar
		    BlockPos[] laterals = { pos.north(), pos.south(), pos.east(), pos.west() };
		    BlockPos[] diagonals = {
		        pos.north().east(), pos.north().west(),
		        pos.south().east(), pos.south().west()
		    };
		    BlockPos below = pos.down();
		    IBlockState belowState = world.getBlockState(below);

		    // --- Calcular total disponible ---
		    int totalAvailable = level;

		    for (BlockPos p : laterals) {
		        IBlockState state = world.getBlockState(p);
		        if (state.getBlock() instanceof IRealisticFiniteFluid) {
		        	IRealisticFiniteFluid fluidBlock = (IRealisticFiniteFluid) state.getBlock();
		            if (fluidBlock.getFluid() == targetFluid) {
	                    IRealisticFiniteFluid realisticFluid = ((IRealisticFiniteFluid)state.getBlock());
		                totalAvailable += realisticFluid.getConceptualVolume(world, p, state);
		            }
		        }
		    }

		    for (BlockPos p : diagonals) {
		        IBlockState state = world.getBlockState(p);
		        if (state.getBlock() instanceof IRealisticFiniteFluid) {
		        	IRealisticFiniteFluid fluidBlock = (IRealisticFiniteFluid) state.getBlock();
		            if (fluidBlock.getFluid() == targetFluid) {
	                    IRealisticFiniteFluid realisticFluid = ((IRealisticFiniteFluid)state.getBlock());
		                totalAvailable += realisticFluid.getConceptualVolume(world, p, state);
		            }
		        }
		    }

		    if (belowState.getBlock() instanceof IRealisticFiniteFluid) {
		    	IRealisticFiniteFluid fluidBlock = (IRealisticFiniteFluid) belowState.getBlock();
		        if (fluidBlock.getFluid() == targetFluid) {
                    IRealisticFiniteFluid realisticFluid = ((IRealisticFiniteFluid)belowState.getBlock());
		            totalAvailable += realisticFluid.getConceptualVolume(world, below, belowState);
		        }
		    }

		    if (totalAvailable < References.MAXIMUM_CONCEPTUAL_LEVEL) return false;

		    // --- Recolectar ---
		    world.setBlockToAir(pos);

		    collected = collectEqually(world, laterals, collected, References.MAXIMUM_CONCEPTUAL_LEVEL, targetFluid);
		    if (collected < References.MAXIMUM_CONCEPTUAL_LEVEL) collected = collectEqually(world, diagonals, collected, References.MAXIMUM_CONCEPTUAL_LEVEL, targetFluid);
		    if (collected < References.MAXIMUM_CONCEPTUAL_LEVEL && belowState.getBlock() instanceof IRealisticFiniteFluid) {
		    	IRealisticFiniteFluid fluidBlock = (IRealisticFiniteFluid) belowState.getBlock();
		        if (fluidBlock.getFluid() == targetFluid) {
                    IRealisticFiniteFluid realisticFluid = ((IRealisticFiniteFluid)belowState.getBlock());
		            int neighborLevel = realisticFluid.getConceptualVolume(world, below, belowState);
		            int take = Math.min(neighborLevel, References.MAXIMUM_CONCEPTUAL_LEVEL - collected);
		            int newLevel = neighborLevel - take;
		            if (newLevel <= References.MINIMUM_LEVEL) world.setBlockToAir(below);
		            else world.setBlockState(below, realisticFluid.setConceptualVolume(world, below, belowState, newLevel));
		            collected += take;
		        }
		    }

		    return true;
		}
		
		public static int collectEqually(World world, BlockPos[] positions, int collected, int spaceLeft, Fluid targetFluid) {
		    for (BlockPos pos : positions) {
		        IBlockState state = world.getBlockState(pos);
		        Block block = state.getBlock();
		        if (block instanceof IRealisticFiniteFluid) {
		            Fluid fluid = ((IRealisticFiniteFluid) block).getFluid();
		            if (fluid != targetFluid) continue; // skip distinto tipo
                    IRealisticFiniteFluid realisticFluid = ((IRealisticFiniteFluid)block);

		            int neighborLevel = realisticFluid.getConceptualVolume(world, pos, state); //state.getValue(BlockFiniteFluid.LEVEL) + 1;
		            int take = Math.min(neighborLevel, spaceLeft - collected);
		            int newLevel = neighborLevel - take;
		            if (newLevel <= References.MINIMUM_LEVEL) world.setBlockToAir(pos);
		            else realisticFluid.setBlockState(world, null, pos, realisticFluid.setConceptualVolume(world, pos, state, newLevel)); //world.setBlockState(pos, state.withProperty(BlockFiniteFluid.LEVEL, newLevel - 1));
		            collected += take;
		            activateOcean(world, pos);
		            
		            if (collected >= spaceLeft) break;
		        }
		    }
		    return collected;
		}
		
		public static void activateOcean(World world, BlockPos pos) {
			boolean exposedToOceanWater = false;
			//System.out.println("exposedToOceanWater original " + exposedToOceanWater);

			for (EnumFacing dir : EnumFacing.VALUES) {
				BlockPos neighbor = pos.offset(dir);
				IBlockState neighborState = world.getBlockState(neighbor);
				////System.out.println("Bloque a explorar " + neighborState.getBlock());
				if (!(neighborState.getBlock() instanceof IRealisticFiniteFluid)) return;
				IRealisticFiniteFluid realisticFluid = ((IRealisticFiniteFluid)neighborState.getBlock());

				if (realisticFluid.isOceanBlock(world, neighbor, neighborState, GeneralPurposeLogic.getFluidIndex(neighborState.getBlock()))) {
					exposedToOceanWater = true;

					if (exposedToOceanWater
							&& !(world.getBlockState(pos.down()).getBlock() instanceof IRealisticFiniteFluid)
							&& !(world.getBlockState(pos).getBlock() instanceof IRealisticFiniteFluid)) {
						FiniteFluidLogic.OceanFluidsLogic.borderOceanCheck(world, pos, false);
					}
					break;
				}
				
			}
		}
		
		public static int distributeEqually(World world, BlockPos pos, int remaining, int fluidType) { 
            List<BlockPos> targets = Arrays.asList(
	                pos.north(), pos.south(), pos.east(), pos.west(),
	                pos.north().east(), pos.north().west(),
	                pos.south().east(), pos.south().west()
            		);
	        return distributeEqually(world, targets, remaining, fluidType);
		}

		
		public static int distributeEqually(World world, List<BlockPos> targets, int remaining, int fluidType) {
            if (targets.isEmpty() || remaining <= References.MINIMUM_LEVEL) return remaining; //original: <=0, ahora es <0, para aceptar al 0 entre los valores

            boolean didSomething = true;

            // Repite mientras queden niveles que distribuir
            while (remaining > References.MINIMUM_LEVEL && didSomething) {
                didSomething = false;
                for (BlockPos p : targets) {
                    if (remaining <= References.MINIMUM_LEVEL) break; //original: <=0, ahora es <0, para aceptar al 0 entre los valores

                    IBlockState s = world.getBlockState(p);
                    if (!(s.getBlock() instanceof IRealisticFiniteFluid) || FiniteFluidLogic.GeneralPurposeLogic.getFluidIndex(s.getBlock()) != fluidType) continue; //Para que no crashee la IC2 FluidCell en el CASO 1) xd
                    IRealisticFiniteFluid realisticFluid = ((IRealisticFiniteFluid)liquids.get(onFiniteFluidIndex).flowingBlock);
                    int level = realisticFluid.getVolume(world, p, s);
                    if (level < References.MAXIMUM_LEVEL) {
                    	int temporalFluidType = FiniteFluidLogic.GeneralPurposeLogic.getFluidIndex(s.getBlock()); 
                    	Block newBlock1 = ((NewFluidType)liquids.get(temporalFluidType)).flowingBlock;
                    	
                        //world.setBlockState(p, newBlock1.getDefaultState().withProperty(BlockFiniteFluid.LEVEL, level + 1)); //s.withProperty(RFFBlock.LEVEL, level + 1));
                    	realisticFluid.setBlockState(world, null, p, realisticFluid.setVolume(null, null, newBlock1.getDefaultState(), level+1));	
                        remaining--;
                        
                        didSomething = true;
                    }
                }
            }

            return remaining;
        }
		
		public static int distributeEquallyNoAdyFluid(World world, List<BlockPos> targets, int remaining, Block finiteFluidBlock) {
            if (targets.isEmpty() || remaining <= References.MINIMUM_LEVEL) return remaining; //original: <=0, ahora es <0, para aceptar al 0 entre los valores

            boolean didSomething = true;

            // Repite mientras queden niveles que distribuir
            while (remaining > References.MINIMUM_LEVEL && didSomething) {
                didSomething = false;
                for (BlockPos p : targets) {
                    if (remaining <= References.MINIMUM_LEVEL) break; //original: <=0, ahora es <0, para aceptar al 0 entre los valores

                    IBlockState s = world.getBlockState(p);
                    //THIS SHIT MIGHT CRASH
                    IRealisticFiniteFluid realisticFluid = ((IRealisticFiniteFluid)liquids.get(onFiniteFluidIndex).flowingBlock);
                    if (world.isAirBlock(p) && !(s.getBlock() instanceof IRealisticFiniteFluid)) { //Para que no crashee la IC2 FluidCell en el CASO 1) xd
                        //if (!(remaining > 16)) { //16 porque estamos en LEVELs conceptuales
                        	//world.setBlockState(p, finiteFluidBlock.getDefaultState().withProperty(BlockFiniteFluid.LEVEL, remaining-1)); //ModBlocks.FINITE_WATER_FLOWING.getDefaultState().withProperty(RFFBlock.LEVEL, remaining-1));
                    	realisticFluid.setBlockState(world, null, p, realisticFluid.setConceptualVolume(null, null, finiteFluidBlock.getDefaultState(), remaining));	
                    	return References.MINIMUM_LEVEL;	
                        /*} else {
                        	world.setBlockState(p, finiteFluidBlock.getDefaultState().withProperty(RFFBlock.LEVEL, 15)); //15 porque esta en LEVELs directos
                        	return remaining -= 16; //-16 porque esta en LEVELs conceptuales
                        }*/
                    } 
                    
                    int level = realisticFluid.getVolume(world, p, s);
                    if (level < References.MAXIMUM_LEVEL) {
                        //world.setBlockState(p, s.withProperty(BlockFiniteFluid.LEVEL, level + 1));
                    	realisticFluid.setBlockState(world, null, p, realisticFluid.setVolume(world, p, s, level + 1));	
                        remaining--;
                        
                        didSomething = true;
                    }
                }
            }

            return remaining;
        }
		
		
		
		public static void distributeFluidEquallyForBuckets(World world, BlockPos pos, int incomingLevel, int fluidIndex) {
			int remaining = incomingLevel; // 15 niveles = 1000mb //AHORA SON 16 LEVELS
			IBlockState flowingBlock = liquids.get(fluidIndex).flowingBlock.getDefaultState();

			// Listas de bloques válidos
			List<BlockPos> lateralTargets = new ArrayList<>();
			List<BlockPos> diagonalTargets = new ArrayList<>();

			// 1. Revisa si el bloque actual es válido y no está lleno
			IBlockState centerState = world.getBlockState(pos);
			IRealisticFiniteFluid realisticFluid;

			if (centerState.getBlock() instanceof IRealisticFiniteFluid && FiniteFluidLogic.GeneralPurposeLogic.getFluidIndex(centerState.getBlock()) == fluidIndex) {
                realisticFluid = ((IRealisticFiniteFluid)centerState.getBlock());
                int currentLevel = realisticFluid.getConceptualVolume(world, pos, centerState); //centerState.getValue(BlockFiniteFluid.LEVEL)+1;
				int toAdd = Math.min(References.MAXIMUM_CONCEPTUAL_LEVEL - currentLevel, remaining); //remaining es el maximo, 16 LEVELs conceptuales
				if (toAdd > References.MINIMUM_LEVEL) {
					//world.setBlockState(pos, centerState.withProperty(BlockFiniteFluid.LEVEL, currentLevel + toAdd-1));
					realisticFluid.setBlockState(world, null, pos, realisticFluid.setConceptualVolume(world, pos, centerState, currentLevel + toAdd));
					remaining -= toAdd;
				}

			} else {
                realisticFluid = ((IRealisticFiniteFluid)liquids.get(onFiniteFluidIndex).flowingBlock);
				//world.setBlockState(pos, ModBlocks.FINITE_LAVA_FLOWING.getDefaultState().withProperty(BlockFiniteFluid.LEVEL, remaining-1)); //COMO LA ENTRADA SON 16, PUES REGRESAMOS A LITERALES
				realisticFluid.setBlockState(world, null, pos, realisticFluid.setConceptualVolume(null, null, flowingBlock, remaining));
			}

			// Coordenadas para laterales y diagonales
			BlockPos[] laterals = {
					pos.north(), pos.south(), pos.east(), pos.west()
			};
			BlockPos[] diagonals = {
					pos.north().east(), pos.north().west(),
					pos.south().east(), pos.south().west()
			};

			// 2. Recolecta objetivos laterales válidos
			for (BlockPos p : laterals) {
				IBlockState s = world.getBlockState(p);
				if (s.getBlock() instanceof IRealisticFiniteFluid) {
	                //IRealisticFiniteFluid realisticFluid = ((IRealisticFiniteFluid)s.getBlock());
					if (FiniteFluidLogic.GeneralPurposeLogic.getFluidIndex(s.getBlock()) == fluidIndex 
						&& realisticFluid.getConceptualVolume(world, p, s) < References.MAXIMUM_CONCEPTUAL_LEVEL) {
						lateralTargets.add(p);
					}
				}
			}

			// 3. Recolecta objetivos diagonales válidos
			for (BlockPos p : diagonals) {
				IBlockState s = world.getBlockState(p);
				if (s.getBlock() instanceof IRealisticFiniteFluid) {
	                //IRealisticFiniteFluid realisticFluid = ((IRealisticFiniteFluid)s.getBlock());
					if (FiniteFluidLogic.GeneralPurposeLogic.getFluidIndex(s.getBlock()) == fluidIndex 
						&& realisticFluid.getConceptualVolume(world, p, s) < References.MAXIMUM_CONCEPTUAL_LEVEL) {
						diagonalTargets.add(p);
					}
				}
			}

			// 4. Distribuye equitativamente a laterales
			remaining = FiniteFluidLogic.FluidWorldInteraction.distributeEqually(world, lateralTargets, remaining, fluidIndex); //1 --> FluidType of lava

			// 5. Si sigue sobrando, distribuye a diagonales
			remaining = FiniteFluidLogic.FluidWorldInteraction.distributeEqually(world, diagonalTargets, remaining, fluidIndex); //1 --> FluidType of lava

			//System.out.println(remaining);
			//System.out.println("VERGA-1");
			// 6. Si todavía sobra, intenta poner un nuevo bloque arriba
			if (remaining > References.MINIMUM_LEVEL && remaining <= References.MAXIMUM_CONCEPTUAL_LEVEL) { //ChatGPT dijo que <16 --> <=16
				BlockPos above = pos.up();
				if (world.isAirBlock(above) && !world.getBlockState(above).getBlock().hasTileEntity()) {
					//world.setBlockState(above, ModBlocks.FINITE_LAVA_FLOWING.getDefaultState().withProperty(BlockFiniteFluid.LEVEL, remaining-1)); //COMO LA ENTRADA SON 16, PUES REGRESAMOS A LITERALES
					realisticFluid.setBlockState(world, null, above, realisticFluid.setConceptualVolume(null, null, flowingBlock, remaining));
					remaining = References.MINIMUM_LEVEL;
				}
			}


			// Si aún queda, se pierde (puedes loguearlo si quieres)
		}
		

		public static int bucketRemoveFluidEvenLowCollect(
		        World world, BlockPos pos, int levelConceptual, int spaceLeft, Fluid targetFluid) {

		    int original = levelConceptual;
		    int collected = 0;

		    IBlockState state = world.getBlockState(pos);
		    Block block = state.getBlock();

		    // Aseguramos que el bloque inicial corresponda al fluid target
		    if (!(block instanceof IRealisticFiniteFluid)) return 0;
		    Fluid fluidHere = ((IRealisticFiniteFluid) block).getFluid();
		    if (fluidHere != targetFluid) return 0;
            IRealisticFiniteFluid realisticFluid = ((IRealisticFiniteFluid)block);


		    // cuánto puedo realmente tomar del bloque central
		    int takeFromCenter = Math.min(original, spaceLeft);
		    if (takeFromCenter > References.MINIMUM_LEVEL) {
		        collected += takeFromCenter;
		        int newLevelCenter = original - takeFromCenter;
		        if (newLevelCenter <= References.MINIMUM_LEVEL) {
		            world.setBlockToAir(pos);
		            activateOcean(world, pos);
		        } else {
		            //world.setBlockState(pos, state.withProperty(BlockFiniteFluid.LEVEL, newLevelCenter - 1));
		        	realisticFluid.setBlockState(world, null, pos, realisticFluid.setConceptualVolume(world, pos, state, newLevelCenter));
		        }
		    }

		    // si ya está lleno, salimos
		    if (collected >= spaceLeft) {
		        return collected;
		    }
		    
		    // 9x9 --> GASES
		    if (collected < spaceLeft && targetFluid.isGaseous()) {
		    	int radius = 2;

		    	List<BlockPos> around = getPositionsInCube(pos, radius);
		    	sortByHorizontalThenDistance(pos, around);

		    	collected = collectEqually(
		    	    world,
		    	    around.toArray(new BlockPos[0]),
		    	    collected,
		    	    spaceLeft,
		    	    targetFluid
		    	);
		    }

		    // Laterales
		    BlockPos[] laterals = { pos.north(), pos.south(), pos.east(), pos.west() };
		    collected = collectEqually(world, laterals, collected, spaceLeft, targetFluid);

		    // Diagonales
		    if (collected < spaceLeft) {
		        BlockPos[] diagonals = {
		            pos.north().east(), pos.north().west(),
		            pos.south().east(), pos.south().west()
		        };
		        collected = collectEqually(world, diagonals, collected, spaceLeft, targetFluid);
		    }
		    

		    
		    // Abajo
		    if (collected < spaceLeft) {
		        BlockPos below = pos.down();
		        IBlockState belowState = world.getBlockState(below);
		        Block belowBlock = belowState.getBlock();
		        if (belowBlock instanceof IRealisticFiniteFluid) {
		            Fluid belowFluid = ((IRealisticFiniteFluid) belowBlock).getFluid();
		            if (belowFluid == targetFluid) {
		                int neighborLevel = realisticFluid.getConceptualVolume(world, below, belowState); //belowState.getValue(BlockFiniteFluid.LEVEL) + 1; // conceptual
		                int take = Math.min(neighborLevel, spaceLeft - collected);
		                int newLevel = neighborLevel - take;
		                if (newLevel <= References.MINIMUM_LEVEL) {world.setBlockToAir(below);  activateOcean(world, below);}
		                else realisticFluid.setBlockState(world, null, below, realisticFluid.setConceptualVolume(world, below, belowState, newLevel)); //world.setBlockState(below, belowState.withProperty(BlockFiniteFluid.LEVEL, newLevel - 1));
		                collected += take;
		            }
		        }
		    }

		    return collected;
		}
		
		public static List<BlockPos> getPositionsInCube(BlockPos center, int radius) {
		    List<BlockPos> result = new ArrayList<>();

		    for (int dx = -radius; dx <= radius; dx++) {
		        for (int dy = -radius; dy <= radius; dy++) {
		            for (int dz = -radius; dz <= radius; dz++) {
		                if (dx == 0 && dy == 0 && dz == 0) continue; // no incluir centro
		                result.add(center.add(dx, dy, dz));
		            }
		        }
		    }

		    return result;
		}
		
		public static void sortByHorizontalThenDistance(BlockPos center, List<BlockPos> list) {
		    list.sort((a, b) -> {
		        int dyA = Math.abs(a.getY() - center.getY());
		        int dyB = Math.abs(b.getY() - center.getY());

		        if (dyA != dyB)
		            return Integer.compare(dyA, dyB);

		        return Double.compare(a.distanceSq(center), b.distanceSq(center));
		    });
		}


		
		// Igual pero solo si SÍ había suficiente para una cubeta completa (solo-full).
		// Devuelve los niveles extraidos (>=0). Si no hay suficientes, devuelve 0 y no modifica nada.
		public static int bucketRemoveFluidOnlyFullCollect(World world, BlockPos pos, int levelConceptual, Fluid targetFluid) {
		    int total = levelConceptual;
            IRealisticFiniteFluid realisticFluid;


		    BlockPos[] laterals = { pos.north(), pos.south(), pos.east(), pos.west() };
		    BlockPos[] diagonals = {
		        pos.north().east(), pos.north().west(),
		        pos.south().east(), pos.south().west()
		    };
		    for (BlockPos p : laterals) {
		        IBlockState s = world.getBlockState(p);
		        if (s.getBlock() instanceof IRealisticFiniteFluid) {
		            realisticFluid = ((IRealisticFiniteFluid)s.getBlock());
		        	total += realisticFluid.getConceptualVolume(world, p, s); //s.getValue(BlockFiniteFluid.LEVEL) + 1;
		        }
		    }
		    for (BlockPos p : diagonals) {
		        IBlockState s = world.getBlockState(p);
		        if (s.getBlock() instanceof IRealisticFiniteFluid) {
		            realisticFluid = ((IRealisticFiniteFluid)s.getBlock());
		            total += realisticFluid.getConceptualVolume(world, p, s); //s.getValue(BlockFiniteFluid.LEVEL) + 1;
		        }
		    }
		    BlockPos below = pos.down();
		    IBlockState belowState = world.getBlockState(below);
		    if (belowState.getBlock() instanceof IRealisticFiniteFluid) {
	            realisticFluid = ((IRealisticFiniteFluid)belowState.getBlock());
	            total += realisticFluid.getConceptualVolume(world, below, belowState); //belowState.getValue(BlockFiniteFluid.LEVEL) + 1;
		    }

		    // si no hay suficientes niveles (conceptuales) abortamos
		    if (total < References.MAXIMUM_CONCEPTUAL_LEVEL) return References.MINIMUM_LEVEL;

		    // Si hay suficientes, borramos/extraemos suavemente igual que en EvenLow
		    int original = levelConceptual;
		    int collected = original;
		    world.setBlockToAir(pos);

		    collected = collectEqually(world, laterals, collected, References.MAXIMUM_CONCEPTUAL_LEVEL, targetFluid);
		    if (collected < References.MAXIMUM_CONCEPTUAL_LEVEL) collected = collectEqually(world, diagonals, collected, References.MAXIMUM_CONCEPTUAL_LEVEL, targetFluid);
		    if (collected < References.MAXIMUM_CONCEPTUAL_LEVEL && belowState.getBlock() instanceof IRealisticFiniteFluid) {
		    	realisticFluid = ((IRealisticFiniteFluid)belowState.getBlock());
		        int neighborLevel = realisticFluid.getConceptualVolume(world, below, belowState); //belowState.getValue(BlockFiniteFluid.LEVEL) + 1;
		        int take = Math.min(neighborLevel, References.MAXIMUM_CONCEPTUAL_LEVEL - collected);
		        int newLevel = neighborLevel - take;
		        if (newLevel <= References.MINIMUM_LEVEL) world.setBlockToAir(below);
		        else realisticFluid.setBlockState(world, null, below, realisticFluid.setConceptualVolume(world, below, belowState, newLevel)); //world.setBlockState(below, belowState.withProperty(BlockFiniteFluid.LEVEL, newLevel - 1));
		        collected += take;
		    }

		    return Math.max(References.MINIMUM_LEVEL, collected - original);
		}


	}
	
	
	
	public static class FiniteGassesLogic {

		/** TODO
		 * PRIMER INTENTO --> CODIGO CHATGPT
		 */
		
		/*
		public static boolean tryGasMove(World world, BlockPos pos,Random rand) {	
		    if (world.isRemote)
		        return false;
		    
		    IBlockState state = world.getBlockState(pos);

		    if (!FiniteFluidLogic.GeneralPurposeLogic.isFiniteFluid(state.getBlock()))
		        return false;
		    
		    IRealisticFiniteFluid gasBlock = (IRealisticFiniteFluid)state.getBlock();

		    int level = gasBlock.getVolume(world, pos, state);
		    if (level < References.MINIMUM_LEVEL)
		        return false;

		    NewFluidType gasType = liquids.get(
		    		GeneralPurposeLogic.getFluidIndex( 
		    				state.getBlock() 
		    				)
		    		);

		    // Pequeña aleatoriedad para evitar estados muertos
		    if (rand.nextFloat() > 0.85f)
		        return false;

		    if (gasMove(world, pos, gasType, rand, true)) return true;
		    
		    else return false;
		}
		
		public static boolean gasMove(World world, BlockPos sourcePos, NewFluidType gasType, Random rand, boolean doMove) {
		    return gasMove(world, sourcePos, gasType, rand, doMove, 0);
		}
		
		private static boolean gasMove(World world,BlockPos sourcePos, NewFluidType gasType, Random rand, boolean doMove, int depth) {
		    if (depth > 1)
		        return false;
		    
		    IBlockState sourceState = world.getBlockState(sourcePos);

		    IRealisticFiniteFluid gasBlock = (IRealisticFiniteFluid)sourceState.getBlock();

		    int sourceLevel = gasBlock.getVolume(world, sourcePos, sourceState);
		    if (sourceLevel <= References.MINIMUM_LEVEL)
		        return false;

		    int moves = 0;

		    // Iteramos direcciones (orden no crítico porque shouldGas... decide)
		    boolean hasMoved = false;
		    for (EnumFacing dir : EnumFacing.VALUES) {

		        if (moves >= References.MAX_GAS_MOVES_PER_TICK)
		        	return false;
		            //break;

		        BlockPos destPos = sourcePos.offset(dir);
		        IBlockState destState = world.getBlockState(destPos);

		        if (!shouldGasFlowToNeighbor(
		                world,
		                sourcePos,
		                destPos,
		                dir,
		                sourceState,
		                destState,
		                gasType
		        )) {
		            continue;
		        }

		        int destLevel = GeneralPurposeLogic.isFiniteFluid(destState.getBlock()) ? gasBlock.getVolume(world, destPos, destState) : 0;

		        // Cantidad a mover (difusión suave)
		        int transfer = 1;

		        if (sourceLevel - transfer < 0)
		            continue;
		        
		        //SI SE LOGRO TRANSFERIR FLUIDO, REGRESAMOS TRUE
		        hasMoved = true;
		        
		        // ---------- Aplicar movimiento ----------
		        int newSourceLevel = sourceLevel - transfer;
		        int newDestLevel   = Math.min(destLevel + transfer, References.MAXIMUM_LEVEL);

		        if (doMove) {
		        	// Source
		        	if (newSourceLevel <= 0) {
		        		world.setBlockToAir(sourcePos);
		        	} else { //ACA SE USABA LA FLAG 2
		        		gasBlock.setBlockState(world, sourcePos, gasBlock.setVolume(world, sourcePos, sourceState, newSourceLevel));
		        	}

		        	// Dest
		        	if (destLevel == 0) {
		        		gasBlock.setBlockState(world, destPos, gasType.flowingBlock.getDefaultState());
		        	}

		        	IBlockState updatedDest = world.getBlockState(destPos);
		        	gasBlock.setBlockState(world, destPos, gasBlock.setVolume(world, destPos, updatedDest, newDestLevel));

		        }

		        // ---------- Actualizar estado local ----------
		        sourceLevel = newSourceLevel;
		        sourceState = world.getBlockState(sourcePos);

		        moves++;

		        if (sourceLevel <= References.MINIMUM_LEVEL)
		            break;
		    }
			return hasMoved;
		}

		
		
		public static boolean shouldGasFlowToNeighbor(
		        World world,
		        BlockPos sourcePos,
		        BlockPos destPos,
		        EnumFacing dir,
		        IBlockState sourceState,
		        IBlockState destState,
		        NewFluidType gasType
		) {
		    IRealisticFiniteFluid gasBlock = (IRealisticFiniteFluid)sourceState.getBlock();
			// ---------- Early exit ----------
		    //if (!canGasMoveInto(world, destPos, gasType))
			if (!canGasMoveInto(world, destPos, sourcePos, gasBlock.getVolume(world, sourcePos, sourceState), gasType))
		        return false;

		    int sourceLevel = gasBlock.getVolume(world, sourcePos, sourceState);
		    int destLevel   = GeneralPurposeLogic.isFiniteFluid(destState.getBlock()) ? gasBlock.getVolume(world, destPos, destState) : 0;

		    if (sourceLevel <= 0)
		        return false;

		    // ---------- Gradiente ----------
		    float delta = sourceLevel - destLevel;
		    if (delta < References.GAS_MIN_DIFF)
		        return false;

		    // ---------- Flotabilidad ----------
		    float density = gasType.getFluid().getDensity(); // density < 0 --> gas
		    float buoyancy = MathHelper.clamp(-density / References.DENSITY_NORMALIZER, 0.2f, 2.0f);

		    float directionBias = 0.0f;

		    if (dir == EnumFacing.UP) {
		        directionBias = 1.0f * buoyancy;
		    }
		    else if (dir == EnumFacing.DOWN) {
		        float overpressureBonus = 0.0f;
		        if (sourceLevel >= References.GAS_OVERPRESSURE_LEVEL) {
		            overpressureBonus = (sourceLevel - References.GAS_OVERPRESSURE_LEVEL) * 0.3f;
		        }
		        directionBias = -0.6f + overpressureBonus;
		    }
		    else {
		        directionBias = 0.2f; // laterales
		    }

		    // ---------- Cohesión ----------
		    int Ns = countGasNeighbors(world, sourcePos, gasType);
		    int Nd = countGasNeighbors(world, destPos, gasType);

		    float cohesionPenalty = Math.max(0, Ns - Nd) * References.COHESION_WEIGHT;

		    // ---------- Saturación ----------
		    float saturationPenalty = 0.0f;
		    if (destLevel >= sourceLevel - 1) {
		        saturationPenalty = References.SATURATION_PENALTY;
		    }

		    // ---------- Score final ----------
		    float score =
		            delta
		          + directionBias
		          - cohesionPenalty
		          - saturationPenalty;

		    return score >= References.GAS_MIN_DIFF;
		}
		
		
		
		
		public static float calculateGasNeighborhoodPressure(
		        World world,
		        BlockPos pos,
		        NewFluidType gasType
		) {
		    float pressure = 0.0f;

		    for (EnumFacing face : EnumFacing.VALUES) {
		        BlockPos neighborPos = pos.offset(face);
		        IBlockState neigborState = world.getBlockState(neighborPos);
		        Block neigborBlock = neigborState.getBlock();
		        NewFluidType currentFluidType = liquids.get(GeneralPurposeLogic.getFluidIndex(neigborBlock));

		        if (GeneralPurposeLogic.isFiniteFluid(neigborBlock) && currentFluidType == gasType) {
		        	
		        	if (neigborBlock instanceof IRealisticFiniteFluid) {
		        		IRealisticFiniteFluid gasBlock = (IRealisticFiniteFluid)neigborBlock;

		            int level = gasBlock.getVolume(world, neighborPos, neigborState);

		            // vecinos más densos aportan más presión
		            pressure += (level / 7.0f);
		        	}
		        }
		    }

		    return pressure;
		}
		
		
		
		
        //Checks if the SourcePos Block can move into the DestPos Block
		public static boolean canGasMoveInto(World world, BlockPos destPos, @Nullable BlockPos sourcePos,  @Nullable int currentLevel, @Nullable NewFluidType fluidType) {
            IBlockState state = world.getBlockState(destPos);
            Block block = state.getBlock();
            if (world.isRemote) return false;
            //if (!world.isRemote) return false;
            
            // Si es aire
            if (world.isAirBlock(destPos)) return true;

            // Si no hay tipo de liquido actual (seguro nunca pasa, pero por si acaso)
            if (fluidType == null) return true;

            // Si no fluye sobre medios bloques, entonces no podemos movernos
            if (!fluidType.flowsOverHalfBlocks) return false;

            //Check para evitar romper liquidos de otros mods
            //if (block instanceof IRealisticFiniteFluid) return false;
            
            //Check para evitar romper otros bloques de fluido realistico
        	//if (isDifferentIndexFluid(block, getFluidIndex(world.getBlockState(sourcePos).getBlock()))) 
        	//	return false; 
        	
        	//checks para bloques vanilla que no deberian ser rotos, por ser Replaceable
            if (block instanceof BlockPistonBase || block instanceof BlockPistonExtension || block instanceof BlockFarmland || block instanceof BlockGrassPath || block instanceof BlockIce) {
                return false;
            }
        	
            // Si es reemplazable (fuego, flores, nieve, etc.)
            //POR ALGUN MOTIVO ESTA MADRE ROMPE DE TO-DO, espero que el !instanceof ayude
            if (block.isReplaceable(world, destPos) 
            		&& !(block instanceof IRealisticFiniteFluid)) {
                world.destroyBlock(destPos, true);
            	//System.out.println("REPLACE"+block);

                return true;
            }

            // Si el nivel de agua es mayor a 7 y el bloque no es completo (ej: flores, placas, etc.)
            if (currentLevel > 7 && !state.isFullBlock() && !state.getBlock().hasTileEntity() 
            		&& !(state.getBlock() instanceof IRealisticFiniteFluid)) {
                world.destroyBlock(destPos, true);
                return true;
            }
            

            return false;
        }
		
		
		
		
		
		
		public static int countGasNeighbors(World world, BlockPos pos, NewFluidType gasType) {
		    int count = 0;

		    for (EnumFacing face : EnumFacing.values()) {
		        BlockPos neighborPos = pos.offset(face);
		        IBlockState neighborState = world.getBlockState(neighborPos);
		        Block neighborBlock = neighborState.getBlock();

		        // Debe ser un bloque fluido clásico
		        if (!(neighborBlock instanceof IRealisticFiniteFluid)) {
		            continue;
		        }

		        IRealisticFiniteFluid neighborFluid = (IRealisticFiniteFluid) neighborBlock;

		        // Debe ser gaseoso
		        Fluid neighborGas = neighborFluid.getFluid();
		        if (neighborGas == null || !neighborGas.isGaseous()) {
		            continue;
		        }

		        // Debe ser el mismo tipo de gas
		        if (neighborGas != gasType.getFluid()) {
		            continue;
		        }

		        // Opcional pero recomendable: no contar niveles 0 (gas residual)
		        if (neighborFluid.getVolume(world, neighborPos, neighborState) <= References.MINIMUM_LEVEL) {
		            continue;
		        }

		        count++;
		    }

		    return count;
		}
		
		*/
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		/** TODO
		 * SEGUNDO INTENTO --> PORT DE Glens Gasses
		 */
		/*public static boolean tryGasMove(World world, BlockPos pos) {
		    if (world.isRemote) return false;

		    int moves = 0;

		    // Orden pseudoaleatorio como en líquidos
		    EnumFacing[] horizontals = EnumFacing.HORIZONTALS.clone();
		    Collections.shuffle(Arrays.asList(horizontals), world.rand);

		    // 1.- Movimiento vertical primero (gas ligero sube)
		    int gravity = GeneralPurposeLogic.getFluidGravity();
		    BlockPos vertical = pos.up(gravity);

		    if (gasMove(world, pos, vertical, true)) {
		        if (++moves >= References.MAX_GAS_MOVES_PER_TICK) return true;
		    }

		    // 2.- Movimiento horizontal (dispersion)
		    for (EnumFacing dir : horizontals) {
		        BlockPos side = pos.offset(dir);
		        if (gasMove(world, pos, side, true)) {
		            if (++moves >= References.MAX_GAS_MOVES_PER_TICK) return true;
		        }
		    }

		    // 3.- Disipacion (si no se movio)
		    if (moves == 0 && References.ENABLE_GAS_DISSIPATION) {
		        dissipateGas(world, pos);
		    }

		    return moves > 0;
		}
		
		
		public static boolean gasMove(World world, BlockPos sourcePos, BlockPos destPos, boolean doMove) {
		    if (world.isRemote) return false;

		    IBlockState sourceState = world.getBlockState(sourcePos);
		    Block sourceBlock = sourceState.getBlock();

		    if (!(sourceBlock instanceof IRealisticFiniteFluid)) return false;

		    IRealisticFiniteFluid gas = (IRealisticFiniteFluid) sourceBlock;
		    int sourceLevel = gas.getVolume(world, sourcePos, sourceState);

		    if (sourceLevel <= References.MINIMUM_LEVEL) return false;

		    IBlockState destState = world.getBlockState(destPos);
		    Block destBlock = destState.getBlock();

		    int destLevel = GeneralPurposeLogic.isFiniteFluid(destBlock)
		            ? gas.getVolume(world, destPos, destState)
		            : -1;

		    // Permiso físico
		    if (!GeneralPurposeLogic.canMoveInto(world, destPos, sourcePos, sourceLevel, null))
		        return false;

		    // Diferencia mínima
		    if (destLevel >= 0 && sourceLevel - destLevel < References.GAS_MIN_DIFF)
		        return false;

		    // Saturación local (muchos vecinos = menos flujo)
		    int Ns = countGasNeighbors(world, sourcePos, sourceBlock);
		    int saturationPenalty = Ns * References.SATURATION_PENALTY;

		    if (sourceLevel - saturationPenalty <= References.MINIMUM_LEVEL)
		        return false;

		    // Decisión fina (presión local)
		    if (destLevel >= 0 && !GeneralPurposeLogic.shouldFlowToNeighbor(world, sourcePos, destPos))
		        return false;

		    if (!doMove) return true;

		    // Transferencia
		    int transfer = Math.max(1, (sourceLevel - destLevel) / 2);
		    transfer = Math.min(transfer, sourceLevel);

		    int newSource = sourceLevel - transfer;
		    int newDest = destLevel < 0 ? transfer : destLevel + transfer;

		    if (newSource >= References.MINIMUM_LEVEL) {
		        gas.setBlockState(world, sourcePos,
		                gas.setVolume(null, null, sourceBlock.getDefaultState(), newSource));
		    } else {
		        world.setBlockToAir(sourcePos);
		    }

		    gas.setBlockState(world, destPos,
		            gas.setVolume(null, null, sourceBlock.getDefaultState(), newDest));

		    return true;
		}
		
		
		
		
		
		public static int countGasNeighbors(World world, BlockPos pos, Block gasBlock) {
		    int count = 0;

		    for (EnumFacing dir : EnumFacing.VALUES) {
		        BlockPos p = pos.offset(dir);
		        if (world.getBlockState(p).getBlock() == gasBlock) {
		            count++;
		        }
		    }
		    return count;
		}
		
		
		
		
		
		private static void dissipateGas(World world, BlockPos pos) {
		    IBlockState state = world.getBlockState(pos);
		    Block block = state.getBlock();

		    if (!(block instanceof IRealisticFiniteFluid)) return;

		    IRealisticFiniteFluid gas = (IRealisticFiniteFluid) block;
		    int level = gas.getVolume(world, pos, state);

		    if (level > References.MINIMUM_LEVEL) {
		        gas.setBlockState(world, pos,
		                gas.setVolume(null, null, block.getDefaultState(), level - 1));
		    } else {
		        world.setBlockToAir(pos);
		    }
		}*/
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		/** TODO
		 * Tercer intento --> CHatGPT 2.0
		 */
		
		/* 
		public static boolean tryGasMove(World world, BlockPos pos, Random rand) {
		        if (world.isRemote)
		            return false;

		        IBlockState state = world.getBlockState(pos);
		        Block block = state.getBlock();

		        if (!FiniteFluidLogic.GeneralPurposeLogic.isFiniteFluid(block))
		            return false;

		        IRealisticFiniteFluid gasBlock = (IRealisticFiniteFluid) block;
		        int level = gasBlock.getVolume(world, pos, state);

		        if (level < References.MINIMUM_LEVEL)
		            return false;

		        // Aleatoriedad para evitar estados muertos
		        if (rand.nextFloat() > 0.85f)
		            return false;

		        NewFluidType gasType = liquids.get(GeneralPurposeLogic.getFluidIndex(block));

		        return gasMove(world, pos, gasType, rand);
		    }

		    private static boolean gasMove(World world, BlockPos pos, NewFluidType gasType, Random rand) {
		        IBlockState state = world.getBlockState(pos);
		        IRealisticFiniteFluid gasBlock = (IRealisticFiniteFluid) state.getBlock();
		        int level = gasBlock.getVolume(world, pos, state);

		        if (level <= References.MINIMUM_LEVEL)
		            return false;

		        boolean moved = false;
		        int moves = 0;

		        // Recorrer vecinos
		        for (EnumFacing dir : EnumFacing.VALUES) {
		            if (moves >= References.MAX_GAS_MOVES_PER_TICK)
		                break;

		            BlockPos neighborPos = pos.offset(dir);
		            IBlockState neighborState = world.getBlockState(neighborPos);

		            if (!shouldGasFlowToNeighbor(world, pos, neighborPos, dir, state, neighborState, gasType))
		                continue;

		            int neighborLevel = GeneralPurposeLogic.isFiniteFluid(neighborState.getBlock()) 
		                                ? ((IRealisticFiniteFluid)neighborState.getBlock()).getVolume(world, neighborPos, neighborState)
		                                : 0;

		            int transfer = 1; // cantidad a mover siempre 1
		            if (level - transfer < 0)
		                continue;

		            int newSourceLevel = level - transfer;
		            int newDestLevel = Math.min(neighborLevel + transfer, References.MAXIMUM_LEVEL);

		            // Aplicar cambios
		            if (newSourceLevel <= 0) {
		                world.setBlockToAir(pos);
		            } else {
		                gasBlock.setBlockState(world, pos, gasBlock.setVolume(world, pos, state, newSourceLevel));
		            }

		            if (neighborLevel == 0) {
		                gasBlock.setBlockState(world, neighborPos, gasType.flowingBlock.getDefaultState());
		            }

		            IBlockState updatedNeighbor = world.getBlockState(neighborPos);
		            gasBlock.setBlockState(world, neighborPos, gasBlock.setVolume(world, neighborPos, updatedNeighbor, newDestLevel));

		            level = newSourceLevel;
		            state = world.getBlockState(pos);

		            moved = true;
		            moves++;

		            if (level <= References.MINIMUM_LEVEL)
		                break;
		        }

		        return moved;
		    }

		    // Esta función se mantiene igual que tu helper
		    public static boolean shouldGasFlowToNeighbor(
		            World world,
		            BlockPos sourcePos,
		            BlockPos destPos,
		            EnumFacing dir,
		            IBlockState sourceState,
		            IBlockState destState,
		            NewFluidType gasType
		    ) {
		        IRealisticFiniteFluid gasBlock = (IRealisticFiniteFluid) sourceState.getBlock();

		        if (!canGasMoveInto(world, destPos, sourcePos, gasBlock.getVolume(world, sourcePos, sourceState), gasType))
		            return false;

		        int sourceLevel = gasBlock.getVolume(world, sourcePos, sourceState);
		        int destLevel = GeneralPurposeLogic.isFiniteFluid(destState.getBlock())
		                ? ((IRealisticFiniteFluid) destState.getBlock()).getVolume(world, destPos, destState)
		                : 0;

		        if (sourceLevel <= 0)
		            return false;

		        float delta = sourceLevel - destLevel;
		        if (delta < References.GAS_MIN_DIFF)
		            return false;

		        float density = gasType.getFluid().getDensity();
		        float buoyancy = MathHelper.clamp(-density / References.DENSITY_NORMALIZER, 0.2f, 2.0f);

		        //float directionBias = switch (dir) {
		        //    case UP -> 1.0f * buoyancy;
		        //    case DOWN -> -0.6f + Math.max(0, (sourceLevel - References.GAS_OVERPRESSURE_LEVEL) * 0.3f);
		        //    default -> 0.2f;
		        //};
		        
		        float directionBias;
		        
			    if (dir == EnumFacing.UP) {
			        directionBias = 1.0f * buoyancy;
			    }
			    else if (dir == EnumFacing.DOWN) {
			        float overpressureBonus = 0.0f;
			        if (sourceLevel >= References.GAS_OVERPRESSURE_LEVEL) {
			            overpressureBonus = (sourceLevel - References.GAS_OVERPRESSURE_LEVEL) * 0.3f;
			        }
			        directionBias = -0.6f + overpressureBonus;
			    }
			    else {
			        directionBias = 0.2f; // laterales
			    }
		        
		        

		        int Ns = countGasNeighbors(world, sourcePos, gasType);
		        int Nd = countGasNeighbors(world, destPos, gasType);
		        float cohesionPenalty = Math.max(0, Ns - Nd) * References.COHESION_WEIGHT;

		        float saturationPenalty = destLevel >= sourceLevel - 1 ? References.SATURATION_PENALTY : 0.0f;

		        return delta + directionBias - cohesionPenalty - saturationPenalty >= References.GAS_MIN_DIFF;
		    }
		
		    
	        //Checks if the SourcePos Block can move into the DestPos Block
			public static boolean canGasMoveInto(World world, BlockPos destPos, @Nullable BlockPos sourcePos,  @Nullable int currentLevel, @Nullable NewFluidType fluidType) {
	            IBlockState state = world.getBlockState(destPos);
	            Block block = state.getBlock();
	            //Block sourceBlock = world.getBlockState(sourcePos).getBlock();
	            if (world.isRemote) return false;
	            //if (!world.isRemote) return false;
	            
	            // Si es aire
	            if (world.isAirBlock(destPos)) return true;

	            // Si no hay tipo de liquido actual (seguro nunca pasa, pero por si acaso)
	            //if (fluidType == null) return true;
	            
	            if (block instanceof IRealisticFiniteFluid) {
	            	int sourceVolume = ((IRealisticFiniteFluid)block).getVolume(world, sourcePos, null);
	            	int destVolume = ((IRealisticFiniteFluid)block).getVolume(world, destPos, null);
	            	if (destVolume < References.MAXIMUM_LEVEL && sourceVolume > References.MINIMUM_LEVEL) 
	            		return true;
	            }
	            
	            // Si no fluye sobre medios bloques, entonces no podemos movernos
	            if (!fluidType.flowsOverHalfBlocks) return false;

	            //Check para evitar romper liquidos de otros mods
	            //if (block instanceof IRealisticFiniteFluid) return false;
	            
	            //Check para evitar romper otros bloques de fluido realistico
	        	//if (isDifferentIndexFluid(block, getFluidIndex(world.getBlockState(sourcePos).getBlock()))) 
	        	//	return false; 
	        	
	        	//checks para bloques vanilla que no deberian ser rotos, por ser Replaceable
	            if (block instanceof BlockPistonBase || block instanceof BlockPistonExtension || block instanceof BlockFarmland || block instanceof BlockGrassPath || block instanceof BlockIce) {
	                return false;
	            }
	        	
	            // Si es reemplazable (fuego, flores, nieve, etc.)
	            //POR ALGUN MOTIVO ESTA MADRE ROMPE DE TO-DO, espero que el !instanceof ayude
	            if (block.isReplaceable(world, destPos) 
	            		&& !(block instanceof IRealisticFiniteFluid)) {
	                world.destroyBlock(destPos, true);
	            	//System.out.println("REPLACE"+block);

	                return true;
	            }

	            // Si el nivel de agua es mayor a 7 y el bloque no es completo (ej: flores, placas, etc.)
	            if (currentLevel > 7 && !state.isFullBlock() && !state.getBlock().hasTileEntity() 
	            		&& !(state.getBlock() instanceof IRealisticFiniteFluid)) {
	                world.destroyBlock(destPos, true);
	                return true;
	            }
	            

	            return false;
	        }
			
			
			
			
			
			
			public static int countGasNeighbors(World world, BlockPos pos, NewFluidType gasType) {
			    int count = 0;

			    for (EnumFacing face : EnumFacing.values()) {
			        BlockPos neighborPos = pos.offset(face);
			        IBlockState neighborState = world.getBlockState(neighborPos);
			        Block neighborBlock = neighborState.getBlock();

			        // Debe ser un bloque fluido clásico
			        if (!(neighborBlock instanceof IRealisticFiniteFluid)) {
			            continue;
			        }

			        IRealisticFiniteFluid neighborFluid = (IRealisticFiniteFluid) neighborBlock;

			        // Debe ser gaseoso
			        Fluid neighborGas = neighborFluid.getFluid();
			        if (neighborGas == null || !neighborGas.isGaseous()) {
			            continue;
			        }

			        // Debe ser el mismo tipo de gas
			        if (neighborGas != gasType.getFluid()) {
			            continue;
			        }

			        // Opcional pero recomendable: no contar niveles 0 (gas residual)
			        if (neighborFluid.getVolume(world, neighborPos, neighborState) <= References.MINIMUM_LEVEL) {
			            continue;
			        }

			        count++;
			    }

			    return count;
			}
			
			
			*/
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		/** TODO
		 * CUARTO INTENTO --> INTENTO MIO
		 */
		public static boolean tryGasMove(World world, BlockPos pos) {
		    if (world.isRemote) return false;
		
		    Random rand = new Random();
		    
		    /*
		    int dx = rand.nextBoolean() ? 1 : -1;
		    int dz = rand.nextBoolean() ? 1 : -1;
		    boolean flip = rand.nextBoolean();
		    int shouldMove = rand.nextInt(10);
		
		    //BlockPos below = pos.down(-GeneralPurposeLogic.getFluidGravity());
		
		    //if (liquidMove(world, pos, below, true)) return true;
		
		    //for (EnumFacing facing : EnumFacing.VALUES) {
		    //	return gasMove(world, pos, pos.offset(facing), true);
		    //}
		    	
		    if (flip) {
		    	//LATERALS
		    	//if (shouldMove < 4) {
		    		if (gasMove(world, pos, pos.add(-dx, 0, 0), true)) return true;
		    		if (gasMove(world, pos, pos.add(dx, 0, 0), true)) return true;
		    		if (gasMove(world, pos, pos.add(0, 0, -dz), true)) return true;
		    		if (gasMove(world, pos, pos.add(0, 0, dz), true)) return true;
		    	//}
		        //UP
		    	//if (shouldMove <= 7)
		    		if (gasMove(world, pos, pos.down(-GeneralPurposeLogic.getFluidGravity()), true)) return true;
		    		if (gasMove(world, pos, pos.down(-GeneralPurposeLogic.getFluidGravity()), true)) return true;
		        //DOWN
		    	//if (shouldMove >= 8)
		    		if (gasMove(world, pos, pos.down(GeneralPurposeLogic.getFluidGravity()), true)) return true;
		        
		        //if (gasMove(world, pos, pos.add(-dx, GeneralPurposeLogic.getFluidGravity(), 0), true)) return true;
		        //if (gasMove(world, pos, pos.add(dx, GeneralPurposeLogic.getFluidGravity(), 0), true)) return true;
		        //if (gasMove(world, pos, pos.add(0, GeneralPurposeLogic.getFluidGravity(), -dz), true)) return true;
		        //if (gasMove(world, pos, pos.add(0, GeneralPurposeLogic.getFluidGravity(), dz), true)) return true;
		        
		        //if (gasMove(world, pos, pos.add(-dx, -GeneralPurposeLogic.getFluidGravity(), 0), true)) return true;
		        //if (gasMove(world, pos, pos.add(dx, -GeneralPurposeLogic.getFluidGravity(), 0), true)) return true;
		        //if (gasMove(world, pos, pos.add(0, -GeneralPurposeLogic.getFluidGravity(), -dz), true)) return true;
		        //if (gasMove(world, pos, pos.add(0, -GeneralPurposeLogic.getFluidGravity(), dz), true)) return true;

		    } else {
		    	//LATERALS
		    	//if (shouldMove < 4) {
		    		if (gasMove(world, pos, pos.add(0, 0, -dz), true)) return true;
		    		if (gasMove(world, pos, pos.add(0, 0, dz), true)) return true;
		    		if (gasMove(world, pos, pos.add(-dx, 0, 0), true)) return true;
		    		if (gasMove(world, pos, pos.add(dx, 0, 0), true)) return true;
		    	//}

		    	//DOWN
		    	//if (shouldMove >= 8)
		    		if (gasMove(world, pos, pos.down(GeneralPurposeLogic.getFluidGravity()), true)) return true;
		    	//UP
		    	//if (shouldMove <= 7)
		    		if (gasMove(world, pos, pos.down(-GeneralPurposeLogic.getFluidGravity()), true)) return true;
		    		if (gasMove(world, pos, pos.down(-GeneralPurposeLogic.getFluidGravity()), true)) return true;

		        //if (gasMove(world, pos, pos.add(0, GeneralPurposeLogic.getFluidGravity(), -dz), true)) return true;
		        //if (gasMove(world, pos, pos.add(0, GeneralPurposeLogic.getFluidGravity(), dz), true)) return true;
		        //if (gasMove(world, pos, pos.add(-dx, GeneralPurposeLogic.getFluidGravity(), 0), true)) return true;
		        //if (gasMove(world, pos, pos.add(dx, GeneralPurposeLogic.getFluidGravity(), 0), true)) return true;
		        //if (gasMove(world, pos, pos.add(0, 0, -dz), true)) return true;
		        //if (gasMove(world, pos, pos.add(0, 0, dz), true)) return true;
		        //if (gasMove(world, pos, pos.add(-dx, 0, 0), true)) return true;
		        //if (gasMove(world, pos, pos.add(dx, 0, 0), true)) return true;
		        
		    }
		    */

		
		    //Random rand = world.rand;

		    List<BlockPos> targets = new ArrayList<>();

		    int g = GeneralPurposeLogic.getFluidGravity();

		    // UP (más probable)
		    for (int i = 0; i < 5; i++)
		        targets.add(pos.down(-g));

		    // LATERALS
		    for (int i = 0; i < 4; i++) {
		        targets.add(pos.east());
		        targets.add(pos.west());
		        targets.add(pos.north());
		        targets.add(pos.south());
		    }

		    // DOWN (raro)
		    targets.add(pos.down(g));

		    // Mezclar
		    Collections.shuffle(targets, rand);

		    // Probar en orden
		    for (BlockPos dest : targets) {
		        if (gasMove(world, pos, dest, true)) {
		            return true;
		        }
		    }
		
		    return false;
		}
		
		
		
		
		
		
		
		
		public static boolean gasMove(World world, BlockPos sourcePos, BlockPos destPos, boolean doMove) {
			//world.setBlockState(sourcePos, Blocks.WATER.getDefaultState().withProperty(References.LEVEL, 0));
		    return gasMove(world, sourcePos, destPos, doMove, 0);
		}

		/**
		 * DETERMINACION DE MOVIMIENTO
		 * 1.- Los gases, al ser puestos en un ambiente al aire libre, se expanderan hasta LEVEL 0, 
		 * y formaran una nube por consecuencia.
		 * YA
		 * 
		 * 2.- Los gases, al encontrarse en su estado en equilibrio (totalmente expandidos), se moveran de forma aleatoria,
		 * YA
		 * 
		 * 2.1.- Los gases, al moverse de forma aleatoria, tendran cierta preferencia para ir hacia arriba que hacia abajo,
		 * excepto cuando se estan expandiendo
		 * 
		 * GASES AL EXPANDIRSE --> NO IMPORTA LA DIRECCION
		 * GASES AL MOVERSE ALEATORIAMENTE --> PREFERENCIA ARRIBA>LATERALES>>>ABAJO
		 * 
		 * 
		 * 
		 * 3.- Los gases, al encontrarse rodeados en sus 6 caras por gas del mismo LEVEL, 
		 * absorberan LEVELs adyacentes por el incremento de la presion (similar a tryGrab?)
		 * 
		 * 
		 * 
		 * 
		 */
		private static boolean gasMove(World world, BlockPos sourcePos, BlockPos destPos, boolean doMove, int recursionDepth) { 
		    if (world.isRemote) {
		    	//System.out.println("[RFF] Return 1"); 
		    return false;
		    }
		    int shouldMove = new Random().nextInt(10);
		    
		    // 1) OBTENEMOS BLOQUES Y STATES TANTO DEL SOURCE COMO DEL DESTINY
		    IBlockState sourceState = world.getBlockState(sourcePos);
		    Block sourceBlock = RealisticFiniteFluidFunctions.getBlock(world, sourcePos, sourceState);
		    if (!(sourceBlock instanceof IRealisticFiniteFluid)) { 
		    	//System.out.println("[RFF] Return 2 FALSE");  
		    	return false; 
		    	}//CHECAR QUE ESTO NO ROMPA EL FUNCIONAMIENTO DEL AGUA //??
			IRealisticFiniteFluid realisticFluid = ((IRealisticFiniteFluid)sourceBlock);
		    int sourceLevel = realisticFluid.getVolume(world, sourcePos, sourceState);
		
		    IBlockState destState = world.getBlockState(destPos);
		    Block destBlock = RealisticFiniteFluidFunctions.getBlock(world, destPos, destState);
		    int destLevel = destBlock instanceof IRealisticFiniteFluid ? realisticFluid.getVolume(world, destPos, destState) : -1;
		
		    // 2) SETEAMOS EL FLUID INDEX 
		    GeneralPurposeLogic.setCurrentFluidIndex(sourceBlock);
		    NewFluidType fluid = (NewFluidType) liquids.get(onFiniteFluidIndex);
		
		    
		    
		    
		    // #######################
		    //        RECURSION
		    // #######################
		    // 3) MANEJAMOS RECURSION PARA BUSCAR EN LIQUIDOS ADYANCETES, DEPENDE DEL MATERIAL DEL SOURCE BLOCK
		    boolean shouldSearchOutward = false;
		    if (fluid.flowingBlock instanceof IRealisticFiniteFluid) {
		        shouldSearchOutward = ((IRealisticFiniteFluid) fluid.flowingBlock).shouldSearchOutward(fluid.flowingBlock.getDefaultState().getMaterial());
		    }
		
		    //SI PODEMOS HACER BUSQUEDAS ADYACENTES ADICIONALES, Y LA PROFUNDIDAD DE RECURSION ES MENOR A 32 -->
		    if (shouldSearchOutward && recursionDepth < 32) {
		    	//CHECAMOS EN LOS ENUMFACING HORIZONTALES DEL BLOQUE DE DEST BLOCK
		        for (EnumFacing dir : EnumFacing.HORIZONTALS) {
		            BlockPos neighbor = destPos.offset(dir);
		            //CHECAMOS LOS VECINOS SIEMPRE Y CUANDO SEAN FLUIDO FINITO DEL MISMO TIPO PERO NO SEAN EL SOURCE BLOCK
		            if (!neighbor.equals(sourcePos) && GeneralPurposeLogic.isFiniteFluid(world, neighbor)) {
		            	//LE SACAMOS EL VOLUMEN
		                int neighborLevel = realisticFluid.getVolume(world, neighbor, world.getBlockState(neighbor));
		                //Y SI EL BLOQUE DESTINO TIENE MAS LIQUIDO QUE UNO DE ESTOS BLOQUES ADDYACENTES --> 
		                //REINICIAMOS LA FUNCION, TOMANDO COMO DESTINO ESTE BLOQUE VECINO
		                if (destLevel > neighborLevel) {
		                	//System.out.println("[RFF] Return 3 "+recursionDepth);
		                    return gasMove(world, sourcePos, neighbor, doMove, recursionDepth++);
		                }
		            }
		        }
		
		    // SI NO PODEMOS HACER RECURSION --> CHECAMOS QUE EL BLOQUE DESTINO SEA BLOQUE FINITO
		    } else if (!GeneralPurposeLogic.isFiniteFluid(world, destPos)) {
		        destLevel = -1;
		    }
		    
		   
		    
		    // REDUCIR DEUDA DE CALCULOS
		    //Es la diferencia entre la altura de nuestro bloque contra el que estamos comparando
		    //Idealmente tendria que salir un resultado positivo
		    int dy = sourcePos.getY() - destPos.getY(); 
		    if (doMove && dy == 0 && Math.abs(destLevel - sourceLevel) < 3 && GeneralPurposeLogic.getCalc() > maxCalc * 0.6f) {
		        --calcAmt;
		    	//System.out.println("[RFF] Return 4 TRUE");
		        return true;
		    }
		    
		    
		    
		    // #######################
		    //   DIFUSION HACIA AIRE
		    // #######################
		    if (GeneralPurposeLogic.canMoveInto(world, destPos, sourcePos, sourceLevel, fluid)) {
	    		//TODO --> AÑADIR DETERMINACION DE MOVIMIENTO POR PROBABILIDADES
		    	if (doMove) {
		    		
		    		//0 1 2 /// 3 4 5 6 7 /// 8 9 

		    		//UP
		    		if (dy == -GeneralPurposeLogic.getFluidGravity() && shouldMove > 7) {
		    			return false;
		    		}
		    		//DOWN
		    		else if (dy == GeneralPurposeLogic.getFluidGravity() && shouldMove > 3) {
		    			return false;
		    		}
		    		//LATERALS
		    		//else if (shouldMove >= 8) {
		    			
		    		//}
		    		
		    		
		    		if (sourceLevel > References.MINIMUM_LEVEL) {
		    			int realSource = sourceLevel + 1; //--> CONVERTIMOS LITERAL LEVELS (0-7) A CONCEPTUAL LEVELS (1-8)
		    			int realDest   = 0;
		    			int transfer = (int) Math.floor(realSource/2);

		    			realSource -= transfer;
		    			realDest   += transfer;

		    			sourceLevel = realSource - 1;
		    			destLevel   = realDest   - 1;

		    			realisticFluid.setBlockState(world, null, sourcePos, realisticFluid.setVolume(null, null, fluid.flowingBlock.getDefaultState(), sourceLevel));
		    			realisticFluid.setBlockState(world, sourcePos, destPos, realisticFluid.setVolume(null, null, fluid.flowingBlock.getDefaultState(), destLevel));
		    		} else {
		    			//System.out.println("[RFF] Michirrines");
		    			if (GeneralPurposeLogic.hasAnyFluidAround(world, sourcePos)) { //, fluid)) {
		    				realisticFluid.setBlockState(world, sourcePos, destPos, realisticFluid.setVolume(null, null, fluid.flowingBlock.getDefaultState(), sourceLevel));
		    				FiniteFluidsLogic.tryGrab(world, sourcePos, destPos, recursionDepth, fluid);	
		    			} else {
		    				realisticFluid.setBlockState(world, sourcePos, destPos, realisticFluid.setVolume(null, null, fluid.flowingBlock.getDefaultState(), sourceLevel));
		    				world.setBlockToAir(sourcePos);			    			
		    			}
		    		}
		    	}
		    	
		    	return true;
		    }
		    

		    
			//System.out.println("[RFF] Michirrines");

		    
		    
		    // ########################
		    //   DIFUSION HACIA GASES
		    // ########################
		    else if (GeneralPurposeLogic.isFiniteFluid(world, destPos)) {
		    	/*BlockPos posBelow = sourcePos.down();
		    	IBlockState stateBelow = world.getBlockState(posBelow);
		    	Block blockBelow = stateBelow.getBlock();
		    	
		    	if (blockBelow instanceof IRealisticFiniteFluid 
		    			&& GeneralPurposeLogic.getFluidIndex(blockBelow) == onFiniteFluidIndex) {
		    		
		    		int levelBelow = realisticFluid.getVolume(world, posBelow, stateBelow);
			    	if (sourceLevel > levelBelow) {
						//System.out.println("[RFF] Michirrines:"+sourcePos);
			    		//GeneralPurposeLogic.flipLiquids(world, sourcePos, destPos);
			    		realisticFluid.setBlockState(world, sourcePos, stateBelow);
			    		realisticFluid.setBlockState(world, posBelow, sourceState);
			    		//realisticFluid.setBlockState(world, sourcePos, Blocks.WATER.getDefaultState());
			    		return true;
			    	}
		    	}*/

		    	
		    	  //shouldFlowToNeighbor(world, sourcePos, destPos) 
		    	if (GeneralPurposeLogic.shouldFlowToNeighbor(world, sourcePos, destPos) //sourceLevel > neighborhoodGasPressure(world, sourcePos)+1 
		    			&& destLevel < References.MAXIMUM_LEVEL 
		    			&& sourceLevel > References.MINIMUM_LEVEL) {
		    		
		    		//COPYPASTEADO DE liquidMove()
		            if (doMove) {
						// ################
						//   ECUALIZACION
						// ################
		            	int total = sourceLevel + destLevel + 2; //Convertido a LEVELs conceptuales
		                sourceLevel = total / 2;
		                destLevel = total - sourceLevel - 1;
		                --sourceLevel;
		                
			            //SI EL LEVEL RESTANTE ES MAYOR O IGUAL AL MINIMUM_LEVEL
	                	//Esto significa, si el LEVEL del origen todavia da para que exista el bloque, seteamos este LEVEL en el bloque
		                if (sourceLevel >= References.MINIMUM_LEVEL)
		                	realisticFluid.setBlockState(world, null, sourcePos, realisticFluid.setVolume(null, null, fluid.flowingBlock.getDefaultState(), sourceLevel));
		                 else //SI NO ALCANZO EL LEVEL DE ORIGEN PARA QUE EXISTA EL SOURCE BLOCK --> tryGrab???
		                	FiniteFluidsLogic.tryGrab(world, sourcePos, destPos, 0, fluid);
		
		                //SETEAMOS UN BLOQUE DE FLUIDO EN EL DEST POSITION
		                realisticFluid.setBlockState(world, sourcePos, destPos, realisticFluid.setVolume(null, null, fluid.flowingBlock.getDefaultState(), destLevel));
		
		                //AÑADIMOS EL BLOQUE AL SISTEMA DE PRESION DIRECTA ASI TIENE EL LEVEL MAXIMO
		                //if (doPressure && destLevel == References.MAXIMUM_LEVEL)
		                //    PressureSystemLogic.addToPressure(world, destPos, false); // 0 = false
		            }
		            return true;
		    	}
		    	
		    	
		    	
		    	
		    	
		    	
		    	/*
			    // ---------- Gradiente ----------
			    float delta = sourceLevel - destLevel;
			    if (delta < References.GAS_MIN_DIFF) //Basicame
			        return false;

			    // ---------- Flotabilidad ----------
			    float density = ((sourceLevel)); // density < 0 --> gas
			    float buoyancy = MathHelper.clamp(-density, 0.2f, 2.0f);

			    float directionBias = 0.0f;

			    //UP
			    if (dy == -GeneralPurposeLogic.getFluidGravity()) {
			        directionBias = 1.0f * buoyancy;
			    }
			    //BOTTOM
			    else if (dy == GeneralPurposeLogic.getFluidGravity()) {
			        float overpressureBonus = 0.0f;
			        if (sourceLevel >= References.GAS_OVERPRESSURE_LEVEL) {
			            overpressureBonus = (sourceLevel - References.GAS_OVERPRESSURE_LEVEL) * 0.3f;
			        }
			        directionBias = -0.6f + overpressureBonus;
			    }
			    //LATERALS
			    else {
			        directionBias = 0.2f; // laterales
			    }
			    */
		    	
		    	
		    	
		    }
		    
		    
		
		    
		    
		    
		    
		    
		    
		    
		    
		    
		    
		    
		    
		    
		    
		    // #######################
		    //   CALCULOS VERTICALES
		    // #######################
		    //MOVIMIENTO NETO HACIA ARRIBA
		    /*
		    
		    if (dy == -GeneralPurposeLogic.getFluidGravity()) {
		    	//SI NOS PODEMOS MOVER --> 
		        if (GeneralPurposeLogic.canMoveInto(world, destPos, sourcePos, sourceLevel, fluid)) {
		        	//EJECUTAMOS EL MOVIMIENTO + tryGrab ????? NO SE QUE HAGA
		            if (doMove) {
		            	realisticFluid.setBlockState(world, destPos, realisticFluid.setVolume(null, null, fluid.flowingBlock.getDefaultState(), sourceLevel));
		            	realisticFluid.setBlockState(world, destPos, realisticFluid.setVolume(null, null, fluid.flowingBlock.getDefaultState(), sourceLevel));
		            	//FiniteFluidsLogic.tryGrab(world, sourcePos, destPos, 0, fluid);
		            }
		        	//System.out.println("[RFF] Return 5 TRUE");
		            return true;
		        }
		        
			    // #########################
			    //   ECUALIZACION VERTICAL
			    // #########################
		        // SI EL DESTINO ES BLOQUE FLUIDO DEL MISMO TIPO
		        // Y TIENE ESPACIO PARA ALMACENAR AGUA
		        if (GeneralPurposeLogic.isFiniteFluid(destBlock) && destLevel < References.MAXIMUM_LEVEL) {
		            if (doMove) {
		            	
		            	
		            	///////
		            	//ECUALIZACION VERTICAL -->
		            	//Convertimos los LEVELs literales (0-7) en LEVELs conceptuales (1-8) para calculos precisos evitando
		            	//problemas al usar el 0
		            		int realSource = sourceLevel + 1;
		                	int realDest   = destLevel + 1;
		                	//OBTENEMOS UN VALOR PARA TRANSFERIR AL DEST BLOCK
		                	int transfer = Math.min(References.MAXIMUM_LEVEL - destLevel, realSource);
		
		                	//REALIZAMOS LA TRANSFERENCIA
		                	realSource -= transfer;
		                	realDest   += transfer;
		
		                	//REGRESAMOS AL SISTEMA DE LEVELs LITERALES
		                	sourceLevel = realSource - 1;
		                	destLevel   = realDest   - 1;
		
		                	//SI EL LEVEL RESTANTE ES MAYOR O IGUAL AL MINIMUM_LEVEL
		                	//Esto significa, si el LEVEL del origen todavia da para que exista el bloque, seteamos este LEVEL en el bloque
		                if (sourceLevel >= References.MINIMUM_LEVEL)
		                	realisticFluid.setBlockState(world, sourcePos, realisticFluid.setVolume(null, null, fluid.flowingBlock.getDefaultState(), sourceLevel));
		                else //SI NO ALCANZO EL LEVEL DE ORIGEN PARA QUE EXISTA EL SOURCE BLOCK --> tryGrab???
		                    FiniteFluidsLogic.tryGrab(world, sourcePos, destPos, 0, fluid);
		
		                //SETEAMOS UN BLOQUE DE FLUIDO EN EL DEST POSITION
		                realisticFluid.setBlockState(world, destPos, realisticFluid.setVolume(null, null, fluid.flowingBlock.getDefaultState(), destLevel));
		
		                //AÑADIMOS EL BLOQUE AL SISTEMA DE PRESION DIRECTA ASI TIENE EL LEVEL MAXIMO
		                if (doPressure && destLevel == References.MAXIMUM_LEVEL)
		                	PressureSystemLogic.addToPressure(world, destPos, false);
		            
		            //////
		            }
		        	//System.out.println("[RFF] Return 6 TRUE");
		            return true;
		        }
		    	//System.out.println("[RFF] Return 7 FALSE");
		        //SI NO NOS PODIAMOS MOVER, NI REALIZAMOS ECUALIZACION --> RETURN FALSE;
		        return false;
		        
		    } 
		    
		    
		    //MOVIMIENTO NETO HACIA ABAJO
		    else if (dy == GeneralPurposeLogic.getFluidGravity()) {
		    	
		    }
		    
		    */
		    
		    
		    
			
		    // #########################
		    //   CALCULOS HORIZONTALES
		    // #########################
		    //SI EL BLOQUE DE DESTINO ES UN BLOQUE DE FLUIDO FINITO
		    /*
		    
		    if (GeneralPurposeLogic.isFiniteFluid(destBlock)) {
		        // SI DEBERIAMOS MOVERNOS 
		    	// + EL DEST LEVEL TIENE MENOS FLUIDO DEL MAXIMO PERMITIDO 
		    	// + EL SOURCE LEVEL TIENE MAS FLUIDO DEL MINIMO PERMITIDO
		        if (GeneralPurposeLogic.shouldFlowToNeighbor(world, sourcePos, destPos) && destLevel < References.MAXIMUM_LEVEL && sourceLevel > References.MINIMUM_LEVEL) {
		            if (doMove) {
						// ###########################
						//   ECUALIZACION HORIZONTAL
						// ###########################
		            	int total = sourceLevel + destLevel + 2; //Convertido a LEVELs conceptuales
		                sourceLevel = total / 2;
		                destLevel = total - sourceLevel - 1;
		                --sourceLevel;
		                
			            //SI EL LEVEL RESTANTE ES MAYOR O IGUAL AL MINIMUM_LEVEL
	                	//Esto significa, si el LEVEL del origen todavia da para que exista el bloque, seteamos este LEVEL en el bloque
		                if (sourceLevel >= References.MINIMUM_LEVEL)
		                	realisticFluid.setBlockState(world, sourcePos, realisticFluid.setVolume(null, null, fluid.flowingBlock.getDefaultState(), sourceLevel));
		                 else //SI NO ALCANZO EL LEVEL DE ORIGEN PARA QUE EXISTA EL SOURCE BLOCK --> tryGrab???
		                	FiniteFluidsLogic.tryGrab(world, sourcePos, destPos, 0, fluid);
		
		                //SETEAMOS UN BLOQUE DE FLUIDO EN EL DEST POSITION
		                realisticFluid.setBlockState(world, destPos, realisticFluid.setVolume(null, null, fluid.flowingBlock.getDefaultState(), destLevel));
		
		                //AÑADIMOS EL BLOQUE AL SISTEMA DE PRESION DIRECTA ASI TIENE EL LEVEL MAXIMO
		                if (doPressure && destLevel == References.MAXIMUM_LEVEL)
		                    PressureSystemLogic.addToPressure(world, destPos, false); // 0 = false
		            }
		        	//System.out.println("[RFF] Return 8 TRUE");
		            return true;
		        }
		        
		        
		        
		        //SI EL BLOQUE DESTINO NO ES UN FINITE FLUID DEL MISMO TIPO
		        //CHECAMOS SI NOS PODEMOS MOVER + SI EL SOURCE LEVEL ES MAYOR AL MINIMO
		        //(claro que en laa funcion canMoveInto, tambien checamos que el bloque destino
		        //no sea un bloque finito de cualquier tipo)
		    } else if (GeneralPurposeLogic.canMoveInto(world, destPos, sourcePos, sourceLevel, fluid) && sourceLevel > References.MINIMUM_LEVEL) {
		        if (doMove) {
		            --sourceLevel;
		            realisticFluid.setBlockState(world, sourcePos, realisticFluid.setVolume(null, null, fluid.flowingBlock.getDefaultState(), sourceLevel));
		            realisticFluid.setBlockState(world, destPos, realisticFluid.setVolume(null, null, fluid.flowingBlock.getDefaultState(), References.MINIMUM_LEVEL));

		        }
		    	//System.out.println("[RFF] Return 9 TRUE");
		        return true;
		    }
		    
		    */
		    
			//System.out.println("[RFF] Return 10 FALSE");
		    return false;
		}
		
		
		public static int countGasNeighbors(World world, BlockPos pos, NewFluidType gasType) {
		    int count = 0;

		    for (EnumFacing face : EnumFacing.values()) {
		        BlockPos neighborPos = pos.offset(face);
		        IBlockState neighborState = world.getBlockState(neighborPos);
		        Block neighborBlock = RealisticFiniteFluidFunctions.getBlock(world, neighborPos, neighborState);

		        // Debe ser un bloque fluido clásico
		        if (!(neighborBlock instanceof IRealisticFiniteFluid)) {
		            continue;
		        }

		        IRealisticFiniteFluid neighborFluid = (IRealisticFiniteFluid) neighborBlock;

		        // Debe ser gaseoso
		        Fluid neighborGas = neighborFluid.getFluid();
		        if (neighborGas == null || !neighborGas.isGaseous()) {
		            continue;
		        }

		        // Debe ser el mismo tipo de gas
		        if (neighborGas != gasType.getFluid()) {
		            continue;
		        }

		        // Opcional pero recomendable: no contar niveles 0 (gas residual)
		        /*if (neighborFluid.getVolume(world, neighborPos, neighborState) <= References.MINIMUM_LEVEL) {
		            continue;
		        }*/

		        count++;
		    }

		    return count;
		}
		
		
		
		
		
		
		
		
		
		public static float neighborhoodGasPressure(World world, BlockPos pos) {
		    int pressure = 0;
		    int count = 0;

		    for (EnumFacing face : EnumFacing.VALUES) {
		        BlockPos neighborPos = pos.offset(face);
		        IBlockState neigborState = world.getBlockState(neighborPos);
		        Block neigborBlock = RealisticFiniteFluidFunctions.getBlock(world, neighborPos, neigborState);
		        int currentIndex = GeneralPurposeLogic.getFluidIndex(neigborBlock);

		        if (GeneralPurposeLogic.isFiniteFluid(world, neighborPos) && currentIndex == onFiniteFluidIndex) {
		        	
		        	if (neigborBlock instanceof IRealisticFiniteFluid) {
		        	IRealisticFiniteFluid gasBlock = (IRealisticFiniteFluid)neigborBlock;

		            int level = gasBlock.getVolume(world, neighborPos, neigborState);

		            // vecinos más densos aportan más presión
		            //pressure += (level / 7.0f);
		            pressure += level;
		        	count++;
		        	}
		        }
		    }

		    return (float)pressure/(float)count;
		}
		
		
		
        public static boolean canGasMove(World world, BlockPos pos, int level) {
            if (world.isRemote) return false;
            
        	EnumFacing[] facings = EnumFacing.VALUES;
            BlockPos[] targets = new BlockPos[facings.length];
        	for (int i = 0; i < facings.length; i++) {
        	    EnumFacing face = facings[i];
        		targets[i] = pos.offset(face);
        	}

            for (BlockPos target : targets) {
                if (FiniteGassesLogic.gasMove(world, pos, target, false, 0)) return true;
            }

            return false;
        }
		
		
		
	
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
	}

}
