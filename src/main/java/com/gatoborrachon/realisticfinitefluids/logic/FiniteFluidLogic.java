package com.gatoborrachon.realisticfinitefluids.logic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Set;

import javax.annotation.Nullable;

import com.gatoborrachon.realisticfinitefluids.blocks.BlockFiniteFluid;
import com.gatoborrachon.realisticfinitefluids.blocks.BlockNewWater_Flow;
import com.gatoborrachon.realisticfinitefluids.init.ModBlocks;
import com.gatoborrachon.realisticfinitefluids.init.ModConfig;

import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockFarmland;
import net.minecraft.block.BlockFlower;
import net.minecraft.block.BlockGrassPath;
import net.minecraft.block.BlockIce;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.BlockPistonExtension;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;


public class FiniteFluidLogic {
    //public static int humidity;
    public static ArrayList<NewFluidType> liquids = new ArrayList<NewFluidType>();
    private static Map<String, Integer> fluidIndexMap = new HashMap<>();
    private static Map<Block, Integer> blockToFluidIndex = new HashMap<>();
    
    public static int onFiniteFluidIndex;
    public static boolean doPressure = ModConfig.doPressure; //true
    //public static int pressureDelay = 0;
    public static int lakeLimit = ModConfig.lakelimit; //512;
    public static int pressureLimit = 256;
    public static int maxCalc = ModConfig.maxCalc; //1024;
    public static int playerMaxDistanceToCalc = ModConfig.playerMaxDistanceToCalc;// 1024;
    public static int waterTick = ModConfig.waterTickRate;// 4; //20 = tests -- 3 = real
    public static int lavaTick = ModConfig.lavaTickRate; // 24; //previo: 20 (4*5, ahora es 4*6)
    public static int grabAmt = 7;
    public static boolean shouldTickRandomly = ModConfig.shouldTickRandomly;
    public static boolean enableRain = ModConfig.enableRain;// true;
    public static int rainAmount = ModConfig.rainAmount; // 24; //Origianl era 12
    public static int rainArea = ModConfig.rainArea; // 32; //Origianl era 16
    public static boolean scalableRainMethod = ModConfig.scalableRainMethod;
    public static float rainNewMethodAmount = ModConfig.rainNewMethodAmount;
    public static int evaporationChance = ModConfig.evaporationChance; // 100; //100 originalmente, yo creo que 30 ha de ser bueno? no se xd, 100 --> 1%, 1 --> 100%, 2 --> 50%
    public static boolean enableEvaporation = ModConfig.enableEvaporation;// true;
    private static boolean stopPCheck;
    private static boolean smallOceanSearch = false;
    
    public static ArrayList<BlockPos> pressure = new ArrayList<BlockPos>();
    public static ArrayList<BlockPos> rpressure = new ArrayList<BlockPos>();
    
    private static int calcAmt;
    private static boolean searchStop;


    
    public FiniteFluidLogic() {  
        onFiniteFluidIndex = 0;
    }
    
    public static void initLiquids() {
        liquids.add(new NewFluidType("water",
                ModBlocks.FINITE_WATER_FLOWING,
                ModBlocks.FINITE_WATER_STILL,
                /*ModBlocks.INFINITE_WATER_SOURCE,*/ 1, true));
        
        //System.out.println("WATER INDEX: "+(liquids.size()-1));
        fluidIndexMap.put("water", (liquids.size() - 1));
        blockToFluidIndex.put(ModBlocks.FINITE_WATER_FLOWING, liquids.size() - 1);
        blockToFluidIndex.put(ModBlocks.FINITE_WATER_STILL, liquids.size() - 1);
        //blockToFluidIndex.put(ModBlocks.INFINITE_WATER_SOURCE, liquids.size() - 1);
        //System.out.println("REVISA ESTA MMDA añadir: "+FiniteFluidLogic.GeneralPurposeLogic.getFluidIndex(ModBlocks.FINITE_WATER_FLOWING));        
        
        
        liquids.add(new NewFluidType("lava",
                ModBlocks.FINITE_LAVA_FLOWING,
                ModBlocks.FINITE_LAVA_STILL,
                /*ModBlocks.INFINITE_LAVA_SOURCE,*/ 1, true));
        
        //System.out.println("LAVA INDEX: "+(liquids.size()-1));
        fluidIndexMap.put("lava", (liquids.size() - 1));
        blockToFluidIndex.put(ModBlocks.FINITE_LAVA_FLOWING, (liquids.size() - 1));
        blockToFluidIndex.put(ModBlocks.FINITE_LAVA_STILL, (liquids.size() - 1));
        //blockToFluidIndex.put(ModBlocks.INFINITE_LAVA_SOURCE, (liquids.size() - 1));
        
        //System.out.println("REVISA ESTA MMDA añadir: "+FiniteFluidLogic.GeneralPurposeLogic.getFluidIndex(ModBlocks.FINITE_LAVA_FLOWING));   

        
    }
    
    static {
        pressure = new ArrayList<BlockPos>();
        rpressure = new ArrayList<BlockPos>();
    }
    
    public static class FiniteFlowingWaterLogic {

    	
    }
    
    /**
     *Functions used for ocean blocks
     */
    public static class InfiniteWaterSource {
    	    	
        public static boolean tryOceanMove(World world, BlockPos pos) {
            if (world.isRemote) return false;

            Random rand = new Random();
            int dx = rand.nextBoolean() ? 1 : -1;
            int dz = rand.nextBoolean() ? 1 : -1;
            boolean flip = rand.nextBoolean();

            BlockPos below = pos.down(FiniteFluidLogic.GeneralPurposeLogic.getFluidGravity());

            if (oceanMove(world, pos, below, true)) return true;

            if (flip) {
                if (oceanMove(world, pos, pos.add(-dx, -FiniteFluidLogic.GeneralPurposeLogic.getFluidGravity(), 0), true)) return true;
                if (oceanMove(world, pos, pos.add(dx, -FiniteFluidLogic.GeneralPurposeLogic.getFluidGravity(), 0), true)) return true;
                if (oceanMove(world, pos, pos.add(0, -FiniteFluidLogic.GeneralPurposeLogic.getFluidGravity(), -dz), true)) return true;
                if (oceanMove(world, pos, pos.add(0, -FiniteFluidLogic.GeneralPurposeLogic.getFluidGravity(), dz), true)) return true;
                if (oceanMove(world, pos, pos.add(-dx, 0, 0), true)) return true;
                if (oceanMove(world, pos, pos.add(dx, 0, 0), true)) return true;
                if (oceanMove(world, pos, pos.add(0, 0, -dz), true)) return true;
                if (oceanMove(world, pos, pos.add(0, 0, dz), true)) return true;
            } else {
                if (oceanMove(world, pos, pos.add(0, -FiniteFluidLogic.GeneralPurposeLogic.getFluidGravity(), -dz), true)) return true;
                if (oceanMove(world, pos, pos.add(0, -FiniteFluidLogic.GeneralPurposeLogic.getFluidGravity(), dz), true)) return true;
                if (oceanMove(world, pos, pos.add(-dx, -FiniteFluidLogic.GeneralPurposeLogic.getFluidGravity(), 0), true)) return true;
                if (oceanMove(world, pos, pos.add(dx, -FiniteFluidLogic.GeneralPurposeLogic.getFluidGravity(), 0), true)) return true;
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

            Block sourceBlock = sourceState.getBlock();
            Block destBlock = destState.getBlock();

            //SETEAMOS EL TIPO DE FLUIDO DEL SOURCEBLOCK
            FiniteFluidLogic.GeneralPurposeLogic.setCurrentFluidIndex(sourceBlock);
            NewFluidType fluid = (NewFluidType) liquids.get(onFiniteFluidIndex);


            // SI EL DESTINO NO ES FLUIDO REALISTA Y TRAS CIERTA PROBABILIDAD
            if (!FiniteFluidLogic.GeneralPurposeLogic.isRealisticFluid(destBlock) && !(new Random().nextInt(10) == 0)) {
                //SI PODEMOS REEMPLAZAR EL BLOQUE DE DESTINO
            	if (FiniteFluidLogic.GeneralPurposeLogic.canMoveInto(world, destPos, sourcePos, BlockFiniteFluid.MAXIMUM_LEVEL, fluid)) {
            		//Y SI SI NOS PODEMOS MOVER REALMENTE
                    if (doMove) {
                    	
                        //SETEAMOS OTRA VEZ EL TIPO DE FLUIDO DEL SOURCEBLOCK
                    	FiniteFluidLogic.GeneralPurposeLogic.setCurrentFluidIndex(sourceBlock);
                    	//PONEMOS UN BLOQUE DE FLUIDO STILL CON UN POCO DE VALOR
                    	BlockFiniteFluid.setBlockState(world, destPos, BlockFiniteFluid.setVolume(null, null, fluid.stillBlock.getDefaultState(), BlockFiniteFluid.Q1_HIGH));
                        //PROGRAMAMOS UN TICK DEL BLOQUE SOURCE 
                    	world.scheduleUpdate(sourcePos, fluid.stillBlock, waterTick + 1);
                        return true;
                    } else {
                        return false;
                    }
                }
            }

            // SI EN EL BLOQUE DESTINO TENEMOS EL MISMO LIQUIDO Y ES OCEANICO (!not), ABORTAMOS (sin este codigo, el agua perfora el mundo hacia abajo)
            if (!FiniteFluidLogic.GeneralPurposeLogic.isRealisticFluidNoOcean(world, destPos, destState)) return false;
            //if (GeneralPurposeLogic.isRealisticFluid(world, destPos) && !BlockFiniteFluid.isOceanBlock(world, destPos, destState, onFiniteFluidIndex)) return false;
            
            // SI EL DESTINO NO ES UN BLOQUE OCEANICO Y SI ERA FLUIDO FINITO DEL MISMO TIPO (sin este codigo, el agua oceanica solo coloca 1 agua normal y muere)
            if (doMove) {
            	//SETEAMOS OTRA PERRA VEZ EL TIPO DE FLUIDO DEL SOURCE BLOCK
            	FiniteFluidLogic.GeneralPurposeLogic.setCurrentFluidIndex(sourceBlock);
            	//COLOCAMOS UN BLOQUE DE TIPO OCEANICO Y PROGRAMAMOS UN TICK
                //world.setBlockState(destPos, fluid.oceanBlock.getDefaultState());
                BlockFiniteFluid.setBlockState(world, destPos, BlockFiniteFluid.setVolume(world, destPos, fluid.stillBlock.getDefaultState(), BlockFiniteFluid.MAXIMUM_CONCEPTUAL_LEVEL));
            	world.scheduleUpdate(sourcePos, fluid.stillBlock, waterTick + 1);
            }
             
            return true;
        }
        
        ///LO DE ARRIBA FUE TODA LA LOGICA DE MOVIMIENTO DEL AGUA, LO QUE SIGUE ES LA LOGICA QUE DETERMINA:
        //1.- CALCULAR LOS BLOQUES OCEANICOS ADYACENTES Y CONVERTIRLOS EN FLUIDO STILL/FLOWING
        //2.- DESPERTERA LOS BLOQUES OCEANICOS DIRECTAMENTE (NO CONVERTIRLOS A AGUA FLOWING)


        /**
         * A function to turn adjacent ocean blocks into flowing/still blocks. For player interaction purposes, and determined by the LakeSize
         * @param world The world.
         * @param pos The position from where to start
         * @param shouldDoSmallOceanSearch Whether to reduce the quantity of fluid blocks to convert into Still/Flowing blocks.
         */
        public static void borderOceanCheck(World world, BlockPos pos, boolean shouldDoSmallOceanSearch) {
        	FiniteFluidLogic.smallOceanSearch = shouldDoSmallOceanSearch;
            if (world.isRemote) return;
            
            //AÑADIMOS BLOQUES A LA LISTA DE PRESION
            if (doPressure) FiniteFluidLogic.GeneralPurposeLogic.addToPressure(world, pos, true); //Añadido a la lista Reverse

            //SETEAMOS EL TIPO DE FLUIDO A UN VALOR NO VALIDO (A DETERMINAR POR VALIDO)
            int detectedType = -1;

            // Revisamos este bloque y sus adyacentes por Fluid Finito. Cuando lo encontramos, hacemos break.
            for (BlockPos offset : new BlockPos[] {
                    pos, pos.west(), pos.east(),
                    pos.down(), pos.up(),
                    pos.north(), pos.south()
            }) {
                int type = FiniteFluidLogic.GeneralPurposeLogic.getFluidIndex(world.getBlockState(offset).getBlock());
                if (detectedType == -1 && type != -1) {
                    detectedType = type;
                    break;
              }
            }
            
            //SI detectedType SIGUE SIENDO INVALIDO, LO CONVERTIMOS EN EL ID DEL AGUA
            if (detectedType < 0) {
                onFiniteFluidIndex = 0;
            } else {
                onFiniteFluidIndex = detectedType;
                
                //UN INT BACKUP DEL INDEX, SETEADO AL DEL AGUA SI NO ENCONTRAMOS FLUIDO VALIDO
                int savedType = onFiniteFluidIndex < 0 ? 0 : onFiniteFluidIndex;

                //ESTO UTILIZA UN METODO ANTIGUO PARA OBTENER LIQUIDOS VALIDDOS, DEBE CAMBIAR
                //AHORA SOLO CHECA POR EL FLUIDO ACTUAL
                //////for (int i = 0; i < liquids.size(); i++) {
                	NewFluidType type = (NewFluidType) liquids.get(onFiniteFluidIndex);
                    if (type != null /*&& type.oceanBlock != null*/) {
                        //////onFiniteFluidIndex = i;
                        boolean valid = true;

                        //SI EL BLOQUE ACTUAL NO TIENE BLOQUES OCEANICOS ADYACENTES Y EL BLOQUE ACTUAL ES OCEANICO --> SE CONVIERTE EN FLOWING Y SE VUELVE NO VALIDO
                        
                        
                        if (!FiniteFluidLogic.GeneralPurposeLogic.hasAdjacentOceanBlocksAround(world, pos, type.stillBlock)) {
                            if (BlockFiniteFluid.isOceanBlock(world, pos, null, onFiniteFluidIndex)) {
                            	BlockFiniteFluid.setBlockState(world, pos, BlockFiniteFluid.setVolume(null, null, type.flowingBlock.getDefaultState(), BlockFiniteFluid.MAXIMUM_LEVEL));
                            }

                            onFiniteFluidIndex = savedType;
                            valid = false;
                        }

                        //SI EL BLOQUE ACTUAL ERA VALIDO Y EL FLUID INDEX ES VALIDO --> HACEMOS BORDEROCEANCHECK2
                        if (valid && onFiniteFluidIndex > -1) {
                    	    for (EnumFacing face : EnumFacing.values()) {
                        	    borderOceanCheck2(world, pos.offset(face));
                    	    }
                        }
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
        }
        
        /**
         * Second BorderOceanCheck, to start turning them into Still/Flowing blocks.
         * @param world
         * @param pos
         */
        public static void borderOceanCheck2(World world, BlockPos pos) {
        	//SI EL MUNDO ES REMOTO O EL INDEX ES INVALUDO --> ABORTAMOS
            if (world.isRemote || onFiniteFluidIndex < 0) return;
            //AÑADIMOS A LISTA DE PRESION
            if (doPressure) FiniteFluidLogic.GeneralPurposeLogic.addToPressure(world, pos, true);
            
            //DETERMINAMOS NUESTRO BLOQUE OBJETIVO, EL QUE LE METEMOS, Y SETEAMOS EL TIPO DE FLUIDO
            Block target = world.getBlockState(pos).getBlock();
            //NewFluidType fluid = (NewFluidType) liquids.get(onFiniteFluidIndex);
            
            //SI EL TARGET ES UN BLOQUE DE TIPO OCEANICO
            if (BlockFiniteFluid.isOceanBlock(world, pos, null, onFiniteFluidIndex)) {            	
            	//HACEMOS UNA LISTA DE BUSQUEDA DE BLOQUES OCEANICOS CON EL MISMO INDEX
                List<BlockPos> result = oceanSearch(world, pos, onFiniteFluidIndex);
                //SI LA LISAT ESTA VACIA O NO ES VALIDA --> PROGRAMAMOS TICK DEL TARGET
                if (result == null || result.isEmpty()) {
                    world.scheduleBlockUpdate(pos, target, waterTick + 1, 0);
                //SI LA LISTA ES VALIDA --> OCEAN TO STILL
                } else {
                    oceanToStill(world, result);
                }
            }
        }

        
        /**
         * A function to "wake" adyacent ocean blocks. Doesn't calculate whether they should become Ocean--> still/flowing blocks.
         * <p>
         * There's no other way to make OceanBlocks tick.
         * @param world
         * @param pos
         */
        public static void wakeOcean(World world, BlockPos pos) {
            int backupType = onFiniteFluidIndex;

            //SETEAMOS LOS BLOQUES, VEMOS QUE NADA SEA INVALIDO, Y OBTENEMOS EL FLUID INDEX
            Block block = world.getBlockState(pos).getBlock();
            //System.out.print("BLOQUE EN WAKE OCEAN: "+blockToFluidIndex.get(block)+"\n");
            //System.out.println("POS: "+pos);
            if (block == null || blockToFluidIndex.get(block) == null) return;
            int fluidIndex = blockToFluidIndex.get(block);
            if (liquids.get(fluidIndex).stillBlock == null) return;

            //SI EL FLUID INXES ES VALIDO
            if (fluidIndex >= 0 && fluidIndex < liquids.size()) {
                NewFluidType type = liquids.get(fluidIndex);
                //SI EL TIPO DE FLUIDO ES VALIDO Y SU BLOQUE OCEANICO SI EXISTE
                if (type != null && type.stillBlock != null) {
                    onFiniteFluidIndex = fluidIndex;

                    // CHECAMOS TODOS LOS ENUM FACINGS POR BLOQUES VENICOS
                    for (EnumFacing dir : EnumFacing.values()) {
                        BlockPos neighbor = pos.offset(dir);
                        Block neighborBlock = world.getBlockState(neighbor).getBlock();

                        //SI EL BLOQUE VECINO ES UN BLOQUE OCEANICO DEL MISMO TIPO
                        if (BlockFiniteFluid.isOceanBlock(world, neighbor, null, fluidIndex)) {
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
         * @param world
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
            int originalIndex = GeneralPurposeLogic.getFluidIndex(world.getBlockState(startBlock).getBlock());

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
                for (EnumFacing dir : EnumFacing.values()) {
                    BlockPos neighbor = currentPosition.offset(dir);

                    //1.- SI NO TENEMOS REGISTRADO A ESTE VECINO EN LA LISTA DE VISITADOS 
                    //2.- Y EL BLOQUE A REVISAR ES DEL TIPO DEL TARGET
                    if (!visited.contains(neighbor) 
                    	&& BlockFiniteFluid.isOceanBlock(world, neighbor, null, originalIndex)
                    	) {

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
         * Converts all the BlockPos on this list into Flowing or Still blocks.
         * @param world
         * @param nodes Nodes of BlockPos to convert.
         */
        public static void oceanToStill(World world, List<BlockPos> nodes) {
        	//POR CADA VALOR EN EL NODO (proveniente de OceanSearch) SE CHECA LO SIGUIENTE --> 
            for (int i = 0; i < nodes.size(); i++) {
                BlockPos pos = nodes.get(i);
                if (pos == null) continue;

                //  SI HAY AIRE CERCA, PROGRAMAMOS EL TICK DE FORMA TARDIA
                for (EnumFacing face : EnumFacing.values()) {
                    if (world.isAirBlock(pos.offset(face))) {
                        // Delay la conversión
                        world.scheduleBlockUpdate(pos, world.getBlockState(pos).getBlock(), 10, 0);
                        continue;
                    }
                }

                //DETERMINAMOS SI USAMOS BLOQUE STILL O FLOWING, NO CREO QUE IMPORTE MUCHO
                Block block = (i < BlockFiniteFluid.MAXIMUM_LEVEL)
                    ? ((NewFluidType) liquids.get(onFiniteFluidIndex)).flowingBlock
                    : ((NewFluidType) liquids.get(onFiniteFluidIndex)).stillBlock;

                 //SETEAMOS EL BLOQUE EN CADA POSICION DE LA LISTA DE NODOS A UNO DE FLOWING O STILL
                int level = BlockFiniteFluid.MAXIMUM_LEVEL;
            	BlockFiniteFluid.setBlockState(world, pos, BlockFiniteFluid.setVolume(null, null, block.getDefaultState(), level));
            }
        }

    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
	
    
    public static class GeneralPurposeLogic {

    	/**
    	 * Para manejar Presiones y que la lluvia provoque chargos de agua (BlockNewWater_Still con el nivel minimo)
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
	            removeFromPressure(pos);
	            checkPressure(world, pos, pos, 0, new ArrayList<>());
	        }

	        // Presión inversa
	        if (!rpressure.isEmpty() && rpressure.get(0) != null) {
	            BlockPos pos = rpressure.remove(0);
	            removeFromPressure(pos);
	            checkPressureReverse(world, pos, pos, 0, new ArrayList<>());
	        }
	        
	        

	        // Simulación de lluvia
	        if (world.isRaining() && enableRain && world.playerEntities.size() > 0) {
	            Random rand = new Random();
                int viewDistance = server.getPlayerList().getViewDistance();
	            if (scalableRainMethod) rainAmount = (int) Math.ceil((48*(1.0/rainNewMethodAmount))/viewDistance); // 48 es mi constante que resuelve 2-->24, 4-->12

	            //System.out.println(rainAmount);
	            
	            if (rand.nextInt(rainAmount) == 0) {
	                int playerIndex = rand.nextInt(world.playerEntities.size());
	                EntityPlayer player = world.playerEntities.get(playerIndex);
	                int x = 0;
	                int z = 0;
	                
	                if (scalableRainMethod) {
	                    int rainRadius = viewDistance * 16; // en bloques
		                x = (int) (player.posX + rand.nextInt(rainRadius*2) - rainRadius);
		                z = (int) (player.posZ + rand.nextInt(rainRadius*2) - rainRadius);
	                } else {
		                x = (int) (player.posX + rand.nextInt(rainArea) - rainArea/2);
		                z = (int) (player.posZ + rand.nextInt(rainArea) - rainArea/2);
	                }
	                int y = getTopSolidOrLiquidBlock(world, x, z);

	                if (y != -1) {
	                    Biome biome = world.getBiome(new BlockPos(x, y, z));
	                    IBlockState state = world.getBlockState(new BlockPos(x, y, z));
	                    Block block = state.getBlock();
	                    //IBlockState stateDown = world.getBlockState(new BlockPos(x, y, z));
	                    IBlockState stateDown = world.getBlockState(new BlockPos(x, y-1, z));

	                    // Evitar cultivos de WEATH TODO implementar cualquier cultivos y mas cosas
	                    NewFluidType fluidType = ((NewFluidType) liquids.get(0));
                        IBlockState water = BlockFiniteFluid.setVolume(null, null, fluidType.flowingBlock.getDefaultState(), BlockFiniteFluid.MINIMUM_LEVEL);
                        //fluidType.flowingBlock.getDefaultState().withProperty(BlockFiniteFluid.LEVEL, 0);
	                	if (!(block instanceof BlockCrops) && !(block instanceof BlockFlower) && !(BlockFiniteFluid.isOceanBlock(null, null, stateDown, getFluidIndex(stateDown.getBlock()))) && biome.canRain()) {
	                        BlockPos spawnPos = new BlockPos(x, y, z);
	                        BlockFiniteFluid.setBlockState(world, spawnPos, water);
	                        //world.setBlockState(spawnPos, water, 3);
	                    }
	                }
	            }
	        }
	    }
	    

	    // Encuentra la parte superior sólida o líquida (como en dJoslin)
	    public static int getTopSolidOrLiquidBlock(World world, int x, int z) {
	        Chunk chunk = world.getChunk(new BlockPos(x, 0, z));
	        int y = world.getHeight();  // 256 normalmente

	        x &= 15;
	        z &= 15;

	        for (; y > 0; --y) {
	            IBlockState state = chunk.getBlockState(x, y, z);
	            if (state.getBlock() == Blocks.ICE || getFluidIndex(state.getBlock()) != -1) {
	                return y + 1;
	            }
	            if (!state.getBlock().isAir(state, world, new BlockPos(x, y, z)) &&
	                state.getMaterial().isSolid() &&
	                state.getMaterial() != net.minecraft.block.material.Material.LEAVES) {
	                return y + 1;
	            }
	        }

	        return -1;
	    }
    	
    	
    	/**
    	 * Checks if a target block is adjacent to the block in the current world and position given.
    	 * @param world
    	 * @param pos The position of the block to check if its has adjacent target blocks.
    	 * @param target Target block to compare.
    	 * @return If that block is near the current block.
    	 */
    	public static boolean hasAdjacentTarjetBlocksAround(World world, BlockPos pos, Block target) {
    	    for (EnumFacing face : EnumFacing.values()) {
    	        if (world.getBlockState(pos.offset(face)).getBlock() == target) {
    	            return true;
    	        }
    	    }
    	    return false;
    	}
    	
    	public static boolean hasAdjacentOceanBlocksAround(World world, BlockPos pos, Block target) {
    	    for (EnumFacing face : EnumFacing.values()) {
    	        if (world.getBlockState(pos.offset(face)).getBlock() == target && BlockFiniteFluid.getVolume(null, null, world.getBlockState(pos.offset(face))) > BlockFiniteFluid.MAXIMUM_LEVEL) {
    	            return true;
    	        }
    	    }
    	    return false;
    	}
    	
    	
    	/**
    	 * Checks if a target block is adjacent to the block in the current world and position given.
    	 * @param world
    	 * @param pos The position of the block to check if its has adjacent target blocks.
    	 * @param target Target block to compare.
    	 * @return If that block is near the current block.
    	 */
    	public static boolean hasAdjacentTarjetBlocksHorizontal(World world, BlockPos pos, Block target) {
    	    for (EnumFacing face : EnumFacing.HORIZONTALS) {
    	        if (world.getBlockState(pos.offset(face)).getBlock() == target) {
    	            return true;
    	        }
    	    }
    	    return false;
    	}
    	
    	public static boolean hasAdjacentOceanBlocksHorizontal(World world, BlockPos pos, Block target) {
    	    for (EnumFacing face : EnumFacing.HORIZONTALS) {
    	        if (world.getBlockState(pos.offset(face)).getBlock() == target && BlockFiniteFluid.getVolume(null, null, world.getBlockState(pos.offset(face))) > BlockFiniteFluid.MAXIMUM_LEVEL) {
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
    	        if (isRealisticFluid(world, neighbor)) {
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


        public static int getFluidGravity()
        {
            return onFiniteFluidIndex < 0 ? getFluidGravity(0) : getFluidGravity(onFiniteFluidIndex);
        }

        public static int getFluidGravity(int fluidIndex)
        {
            return fluidIndex < 0 /*undefined index*/ ? ((NewFluidType)liquids.get(0)).gravity /*Water gravity*/ : ((NewFluidType)liquids.get(fluidIndex)).gravity;
        }
        
    	/**
    	 * Gives the current fluid gravity (to calculate if it should go downwards or upwards) depending on its density.
    	 * @param block The block to check its gravity.
    	 * @return Fluid gravity (+1 [downwards] or -1 [upwards]).
    	 */
        public static int getFluidGravity(Block block) {
        	if (block instanceof IFluidBlock) {
        		Fluid fluid = ((IFluidBlock)block).getFluid();
        		if (fluid.getDensity() >= 0) return 1;
        		else return -1;
        	}
        	return 1;
        }

        public static int getFluidLevelRender(IBlockAccess world, BlockPos pos, int fluidIndex) {
            IBlockState state = world.getBlockState(pos);
        	if (state.getBlock() instanceof BlockFiniteFluid) {
            if (fluidIndex > -1) {
                int level = BlockFiniteFluid.getVolume(world, pos, state) * 2;

                if (level >= BlockFiniteFluid.MAXIMUM_LEVEL) level = BlockFiniteFluid.MAXIMUM_LEVEL-1;

                Block block = state.getBlock();
                if (BlockFiniteFluid.isOceanBlock(world, pos, state, getFluidIndex(block))) {
                    return level;
                }
            }

            //IBlockState state = world.getBlockState(pos);
            return BlockFiniteFluid.getVolume(world, pos, state);
        	}
			return BlockFiniteFluid.MAXIMUM_LEVEL;
        }
        
        public static float getHeight(IBlockAccess access, BlockPos pos, int dx, int dz) {
            int fluidIndex = getFluidIndex(access.getBlockState(pos).getBlock());
            if (fluidIndex == -1) return 0f;
            float total = getFluidLevelRender(access, pos, fluidIndex) + 1.0f;
            int samples = 1;

            BlockPos above = pos.up(getFluidGravity(fluidIndex));
            if (isAnyRealisticFluid(access.getBlockState(above).getBlock(), fluidIndex)) return 1.0f;
            if (BlockFiniteFluid.isOceanBlock(access, pos, null, fluidIndex)) return 1.0f;

            
            
            boolean hasNeighbors = !(access.isAirBlock(pos.east()) &&
            		access.isAirBlock(pos.west()) &&
            		access.isAirBlock(pos.north()) &&
            		access.isAirBlock(pos.south()));

            if (!hasNeighbors) return total / (float) BlockFiniteFluid.MAXIMUM_CONCEPTUAL_LEVEL;

            BlockPos p1 = pos.add(dx, 0, 0);
            BlockPos p2 = pos.add(0, 0, dz);
            BlockPos p3 = pos.add(dx, 0, dz);

            // === Primer vecino (p1) ===
            if (BlockFiniteFluid.isOceanBlock(access, p1, null, fluidIndex)) return 1.0f;
            if (isAnyRealisticFluid(access, p1, fluidIndex)) {
                if (isLDWater(access, p3, fluidIndex)) return 0.0f;
                total += getFluidLevelRender(access, p1, fluidIndex) + 1.0f;
                samples++;
            }
            if (isFullWaterRender(access, p1.up(getFluidGravity(fluidIndex)), fluidIndex)) return 1.0f;
            if (isLDWater(access, p1, fluidIndex)) return 0.0f;

            // === Segundo vecino (p3) ===
            if (BlockFiniteFluid.isOceanBlock(access, p3, null, fluidIndex)) return 1.0f;
            if (isAnyRealisticFluid(access, p3, fluidIndex)) {
                total += getFluidLevelRender(access, p3, fluidIndex) + 1.0f;
                samples++;
            }
            if (isFullWaterRender(access, p3.up(getFluidGravity(fluidIndex)), fluidIndex)) return 1.0f;

            // === Tercer vecino (p2) ===
            if (BlockFiniteFluid.isOceanBlock(access, p2, null, fluidIndex)) return 1.0f;
            if (isAnyRealisticFluid(access, p2, fluidIndex)) {
                if (isLDWater(access, p3, fluidIndex)) return 0.0f;
                total += getFluidLevelRender(access, p2, fluidIndex) + 1.0f;
                samples++;
            }
            if (isFullWaterRender(access, p2.up(getFluidGravity(fluidIndex)), fluidIndex)) return 1.0f;
            if (isLDWater(access, p2, fluidIndex)) return 0.0f;

            return total / (float)BlockFiniteFluid.MAXIMUM_CONCEPTUAL_LEVEL / samples;
        }
        
        
        public static void flipLiquids(World world, BlockPos pos) {
            flipLiquids(world, pos, pos.down());
        }
        
        public static void flipLiquids(World world, BlockPos pos1, BlockPos pos2) {
            IBlockState state1 = world.getBlockState(pos1);
            IBlockState state2 = world.getBlockState(pos2);
            Block block1 = state1.getBlock();
            Block block2 = state2.getBlock();

            int type1 = FiniteFluidLogic.GeneralPurposeLogic.getFluidIndex(block1);
            int type2 = FiniteFluidLogic.GeneralPurposeLogic.getFluidIndex(block2);

            if (type1 != -1 && type2 != -1) {
                Block newBlock1 = ((NewFluidType)liquids.get(type2)).flowingBlock;
                Block newBlock2 = ((NewFluidType)liquids.get(type1)).flowingBlock; 
                //int meta1 = block1.getMetaFromState(state1);
                //int meta2 = block2.getMetaFromState(state2);
                int meta1 = BlockFiniteFluid.getVolume(world, pos1, state1);
                int meta2 = BlockFiniteFluid.getVolume(world, pos2, state2);

                //world.setBlockState(pos1, newBlock1.getStateFromMeta(meta2), 3);
                BlockFiniteFluid.setBlockState(world, pos1, newBlock1.getStateFromMeta(meta2));
                //world.setBlockState(pos2, newBlock2.getStateFromMeta(meta1), 3);
                BlockFiniteFluid.setBlockState(world, pos2, newBlock2.getStateFromMeta(meta1));
            }
        }
        
        
    	
    	
        public static boolean isRealisticFluid(World world, BlockPos pos) {
            return isRealisticFluid(world.getBlockState(pos).getBlock());
        }


        public static boolean isRealisticFluid(Block block) {
            NewFluidType type = (NewFluidType) liquids.get(onFiniteFluidIndex);
            return block == type.flowingBlock || block == type.stillBlock;
        }

        public static boolean isAWater(World world, BlockPos pos) {
            Block block = world.getBlockState(pos).getBlock();
            //NewFluidType type = (NewFluidType) liquids.get(onFiniteFluidIndex);
            return BlockFiniteFluid.isOceanBlock(world, pos, null, onFiniteFluidIndex) || isRealisticFluid(block);
        }


        /**
         * If this block is NOT ocean block BUT IS REALISTIC FLUID
         * @param block
         * @return
         */
        public static boolean isRealisticFluidNoOcean(@Nullable IBlockAccess world, @Nullable BlockPos pos, IBlockState state) {
            //NewFluidType type = (NewFluidType) liquids.get(onFiniteFluidIndex);
            return (!BlockFiniteFluid.isOceanBlock(world, pos, null, onFiniteFluidIndex)) 
            		&& isRealisticFluid(
            				world
            				.getBlockState(pos)
            				.getBlock());
        }


        public static boolean isAnyRealisticFluid(IBlockAccess world, BlockPos pos, int indexToCompare)
        {
            return isAnyRealisticFluid(world.getBlockState(pos).getBlock(), indexToCompare);
        }
        

        /**
         * Whether the block is from the same index to compare.
         * @param blockToCompare
         * @param indexToCompare
         * @return True of the block is from the same index
         */
        public static boolean isAnyRealisticFluid(Block blockToCompare, int indexToCompare) {
            return getFluidIndex(blockToCompare) == indexToCompare;
        }
        
        public static boolean isDifferentRealisticFluid(Block blockToCompare, int indexToCompare2)
        {
        	int indexToCompare1 = getFluidIndex(blockToCompare);
            return indexToCompare1 != -1 && indexToCompare1 != indexToCompare2;
        }

        
        public static boolean isFullWater(IBlockAccess world, BlockPos pos) {
            Block block = world.getBlockState(pos).getBlock();
            return block == ((NewFluidType)liquids.get(onFiniteFluidIndex)).flowingBlock ? true : BlockFiniteFluid.isOceanBlock(world, pos, null, onFiniteFluidIndex);
        }

        
        public static boolean isFullWaterRender(IBlockAccess world, BlockPos pos, int fluidIndex) {
            Block block = world.getBlockState(pos).getBlock();
            return block == ((NewFluidType)liquids.get(fluidIndex)).flowingBlock ? true : BlockFiniteFluid.isOceanBlock(world, pos, null, fluidIndex);
        }

        /*public static boolean isFlowingRealisticFluid(Block block)
        {
            return block == ((NewFluidType)liquids.get(onFiniteFluidIndex)).flowingBlock;
        }

        public static boolean isFullWater(Block block)
        {
            return block == ((NewFluidType)liquids.get(onFiniteFluidIndex)).flowingBlock ? true : BlockFiniteFluid.isOceanBlock(world, pos, null, onFiniteFluidIndex);
        }

        public static boolean isFullWaterRender(Block block, int fluidIndex) //var1 deberia ser 0
        {
            return block == ((NewFluidType)liquids.get(fluidIndex)).flowingBlock ? true : block == ((NewFluidType)liquids.get(fluidIndex)).oceanBlock;
        }

        public static boolean isStillRealisticFluid(Block block)
        {
            return block == ((NewFluidType)liquids.get(onFiniteFluidIndex)).stillBlock;
        }
        
        public static boolean isOceanRealisticFluid(Block block)
        {
            return block == ((NewFluidType)liquids.get(onFiniteFluidIndex)).oceanBlock;
        }*/
        

        
        public static boolean tryToSave(World world, BlockPos pos, NewFluidType fluid) {
            int gravity = fluid.gravity;
            boolean found = false;

            for (int i = 0; i < 2; i++) {
                int yOffset = (i == 0) ? 1 : gravity;
                BlockPos checkPos;

                // Arriba (i == 0)
                if (i == 0) {
                    checkPos = pos.up();
                    IBlockState state = world.getBlockState(checkPos);
                    Block block = state.getBlock();
                    if (fluid.isFluid(block)) {
                        int level = BlockFiniteFluid.getVolume(world, checkPos, state);
                        if (level > BlockFiniteFluid.MINIMUM_LEVEL || BlockFiniteFluid.isOceanBlock(world, checkPos, state, getFluidIndex(state.getBlock()))) {
                            found = true;
                        	//System.out.println("VERGA TRYTOSAVE_1");

                            if (!BlockFiniteFluid.isOceanBlock(world, checkPos, state, getFluidIndex(state.getBlock()))) {
                                //world.setBlockState(checkPos, fluid.flowingBlock.getDefaultState().withProperty(BlockFiniteFluid.LEVEL, level - 1), 3);
                            	BlockFiniteFluid.setBlockState(world, checkPos, BlockFiniteFluid.setConceptualVolume(null, null, fluid.flowingBlock.getDefaultState(), level));
                            }
                            //world.setBlockState(pos, fluid.flowingBlock.getDefaultState().withProperty(BlockFiniteFluid.LEVEL, 0), 3);
                        	BlockFiniteFluid.setBlockState(world, pos, BlockFiniteFluid.setVolume(null, null, fluid.flowingBlock.getDefaultState(), BlockFiniteFluid.MINIMUM_LEVEL));
                            break;
                        }
                    }
                } else {
                	for (EnumFacing dir : EnumFacing.HORIZONTALS) {
                        checkPos = pos.offset(dir).add(0, yOffset, 0);
                        IBlockState state = world.getBlockState(checkPos);
                        Block block = state.getBlock();
                        if (fluid.isFluid(block)) {
                            int level = BlockFiniteFluid.getVolume(world, checkPos, state);;
                            if (level > BlockFiniteFluid.MINIMUM_LEVEL || BlockFiniteFluid.isOceanBlock(world, checkPos, state, getFluidIndex(state.getBlock()))) {
                                found = true;
                            	//System.out.println("VERGA TRYTOSAVE_2");
                                if (!BlockFiniteFluid.isOceanBlock(world, checkPos, state, getFluidIndex(state.getBlock()))) {
                                    //world.setBlockState(checkPos, fluid.flowingBlock.getDefaultState().withProperty(BlockFiniteFluid.LEVEL, level - 1), 3);
                                	BlockFiniteFluid.setBlockState(world, checkPos, BlockFiniteFluid.setConceptualVolume(null, null, fluid.flowingBlock.getDefaultState(), level));
                                }
                                //world.setBlockState(pos, fluid.flowingBlock.getDefaultState().withProperty(BlockFiniteFluid.LEVEL, 0), 3);
                            	BlockFiniteFluid.setBlockState(world, pos, BlockFiniteFluid.setVolume(null, null, fluid.flowingBlock.getDefaultState(), BlockFiniteFluid.MINIMUM_LEVEL));
                                break;
                            }
                        }
                    }
                    if (found) break;
                }
            }

            if (!found) {
                if (FiniteFluidLogic.doPressure) {
                    FiniteFluidLogic.GeneralPurposeLogic.addToPressure(world, pos, true); //originalmente es true, el port manejaba false por algun motivo
                }
                return false;
            }

            return true;
        }
        
        

        

        
        

        
        //SEGUNDA ITERACION
        public static boolean tryGrab(World world, BlockPos pos, BlockPos fromPos, int depth, NewFluidType fluid) {
            if (world.isRemote) return false;

            boolean foundWater = false;
            BlockPos foundPos = null;
            world.setBlockToAir(pos);
            world.markBlockRangeForRenderUpdate(pos, pos);

            // Si el bloque desde el que cayó está arriba y no hay aire arriba, cancelar
            if (fromPos.getY() > pos.getY() && !world.isAirBlock(fromPos)) {
                return false;
            }

            if (depth < grabAmt) {
                // offset 0: misma Y, offset 1: arriba/abajo según gravedad
                for (int verticalOffset = 0; verticalOffset < 2 && !foundWater; ++verticalOffset) {
                    int yOffset = verticalOffset * getFluidGravity();

                    // Centro (misma XZ)
                    BlockPos check = pos.add(0, yOffset, 0);
                    if ((depth == 0 || !check.equals(fromPos)) && isRealisticFluid(world.getBlockState(check).getBlock())) {
                        foundPos = check;
                        foundWater = true;
                    }

                    // Oeste
                    check = pos.add(-1, yOffset, 0);
                    if (!foundWater && (depth == 0 || !check.equals(fromPos)) && isRealisticFluid(world.getBlockState(check).getBlock())) {
                        foundPos = check;
                        foundWater = true;
                    }

                    // Este
                    check = pos.add(1, yOffset, 0);
                    if (!foundWater && (depth == 0 || !check.equals(fromPos)) && isRealisticFluid(world.getBlockState(check).getBlock())) {
                        foundPos = check;
                        foundWater = true;
                    }

                    // Norte
                    check = pos.add(0, yOffset, -1);
                    if (!foundWater && (depth == 0 || !check.equals(fromPos)) && isRealisticFluid(world.getBlockState(check).getBlock())) {
                        foundPos = check;
                        foundWater = true;
                    }

                    // Sur
                    check = pos.add(0, yOffset, 1);
                    if (!foundWater && (depth == 0 || !check.equals(fromPos)) && isRealisticFluid(world.getBlockState(check).getBlock())) {
                        foundPos = check;
                        foundWater = true;
                    }
                }
            }

            if (foundWater && foundPos != null) {
                IBlockState state = world.getBlockState(foundPos);
                int level = BlockFiniteFluid.getVolume(world, foundPos, state);

                // Coloca el nuevo bloque con el mismo nivel que el original
                //world.setBlockState(pos, fluid.flowingBlock.getDefaultState().withProperty(BlockFiniteFluid.LEVEL, level), 3);
            	BlockFiniteFluid.setBlockState(world, pos, BlockFiniteFluid.setVolume(null, null, fluid.flowingBlock.getDefaultState(), level));

                // Repetir para el bloque del cual se extrajo el agua
                tryGrab(world, foundPos, pos, depth + 1, fluid);
            } else if (world.isAirBlock(pos)) {
                // Si quedó aire, intenta salvar el bloque
                tryToSave(world, pos, fluid);
            }

            return false;
        }

        
        public static boolean calcAvg(World world, BlockPos center, BlockPos exclude) {
            Block block = ((NewFluidType)liquids.get(onFiniteFluidIndex)).flowingBlock;

            if (block instanceof BlockFiniteFluid) {
                BlockFiniteFluid fluid = (BlockFiniteFluid) block;
                return fluid.calcAvg(world, center, exclude);
            } else {
                return false;
            }
        }
        
        public static float calculateNeighborWaterLevel(World world, BlockPos center, BlockPos exclude) { //getAvg
            int totalLevel = BlockFiniteFluid.getConceptualVolume(world, center, world.getBlockState(center)); //world.getBlockState(center).getValue(BlockFiniteFluid.LEVEL) + 1;
            int count = 1;

            boolean hasWest = false;
            boolean hasEast = false;
            boolean hasNorth = false;
            boolean hasSouth = false;

            // Cardinales
            if (!center.west().equals(exclude) && isRealisticFluid(world, center.west())) {
                totalLevel += BlockFiniteFluid.getConceptualVolume(world, center.west(), world.getBlockState(center.west())); //world.getBlockState(center.west()).getValue(BlockFiniteFluid.LEVEL) + 1;
                count++;
                hasWest = true;
            }

            if (!center.east().equals(exclude) && isRealisticFluid(world, center.east())) {
                totalLevel += BlockFiniteFluid.getConceptualVolume(world, center.east(), world.getBlockState(center.east())); //world.getBlockState(center.east()).getValue(BlockFiniteFluid.LEVEL) + 1;
                count++;
                hasEast = true;
            }

            if (!center.north().equals(exclude) && isRealisticFluid(world, center.north())) {
                totalLevel += BlockFiniteFluid.getConceptualVolume(world, center.north(), world.getBlockState(center.north())); //world.getBlockState(center.north()).getValue(BlockFiniteFluid.LEVEL) + 1;
                count++;
                hasNorth = true;
            }

            if (!center.south().equals(exclude) && isRealisticFluid(world, center.south())) {
                totalLevel += BlockFiniteFluid.getConceptualVolume(world, center.south(), world.getBlockState(center.south())); //world.getBlockState(center.south()).getValue(BlockFiniteFluid.LEVEL) + 1;
                count++;
                hasSouth = true;
            }

            // Diagonales (solo si al menos uno de los lados existe)
            if ((hasEast || hasSouth)) {
                BlockPos diag = center.east().south();
                if (!diag.equals(exclude) && isRealisticFluid(world, diag)) {
                    totalLevel += BlockFiniteFluid.getConceptualVolume(world, diag, world.getBlockState(diag)); //world.getBlockState(diag).getValue(BlockFiniteFluid.LEVEL) + 1;
                    count++;
                }
            }

            if ((hasWest || hasNorth)) {
                BlockPos diag = center.west().north();
                if (!diag.equals(exclude) && isRealisticFluid(world, diag)) {
                    totalLevel += BlockFiniteFluid.getConceptualVolume(world, diag, world.getBlockState(diag)); //world.getBlockState(diag).getValue(BlockFiniteFluid.LEVEL) + 1;
                    count++;
                }
            }

            if ((hasWest || hasSouth)) {
                BlockPos diag = center.west().south();
                if (!diag.equals(exclude) && isRealisticFluid(world, diag)) {
                    totalLevel += BlockFiniteFluid.getConceptualVolume(world, diag, world.getBlockState(diag)); //world.getBlockState(diag).getValue(BlockFiniteFluid.LEVEL) + 1;
                    count++;
                }
            }

            if ((hasEast || hasNorth)) {
                BlockPos diag = center.east().north();
                if (!diag.equals(exclude) && isRealisticFluid(world, diag)) {
                    totalLevel += BlockFiniteFluid.getConceptualVolume(world, diag, world.getBlockState(diag)); //world.getBlockState(diag).getValue(BlockFiniteFluid.LEVEL) + 1;
                    count++;
                }
            }

            return (float) totalLevel / count;
        }
        


       
        
        public static boolean canMoveInto(World world, BlockPos toPos, @Nullable BlockPos fromPos,  @Nullable int currentLevel, @Nullable NewFluidType fluidType) {
            IBlockState state = world.getBlockState(toPos);
            Block block = state.getBlock();

            //CORRECCION MIA
            //Si es agua:
            if (isRealisticFluid(world, toPos) && state.getMaterial() == Material.WATER && world.getBlockState(fromPos).getMaterial() == Material.LAVA && currentLevel > 5) {
            	//System.out.println("canMoveInto"+block);
            	return true;
            } else if (isRealisticFluid(world, toPos)) {
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
        	if (isDifferentRealisticFluid(block, getFluidIndex(world.getBlockState(fromPos).getBlock()))) {
        		return false;
        	}
        	
        	//checks para bloques vanilla que no deberian ser rotos, por ser Replaceable
            if (block instanceof BlockPistonBase || block instanceof BlockPistonExtension || block instanceof BlockFarmland || block instanceof BlockGrassPath || block instanceof BlockIce) {
                return false;
            }
        	
            // Si es reemplazable (fuego, flores, nieve, etc.)
            if (block.isReplaceable(world, toPos)) {
                world.destroyBlock(toPos, true);
            	//System.out.println("REPLACE"+block);

                return true;
            }

            // Si el nivel de agua es mayor a 7 y el bloque no es completo (ej: flores, placas, etc.)
            if (currentLevel > 7 && !state.isFullBlock() && !state.getBlock().hasTileEntity()) {
                world.destroyBlock(toPos, true);
                return true;
            }
            

            return false;
        }
        
        public static boolean liquidMove(World world, BlockPos sourcePos, BlockPos destPos, boolean doMove) {
            return FiniteFluidLogic.GeneralPurposeLogic.liquidMove(world, sourcePos, destPos, doMove, 0);
        }
        
        public static boolean liquidMove(World world, BlockPos sourcePos, BlockPos destPos, boolean doMove, int recursionDepth) { 
            if (world.isRemote) return false;
            
            IBlockState sourceState = world.getBlockState(sourcePos);
            Block sourceBlock = sourceState.getBlock();
            if (!(sourceBlock instanceof BlockFiniteFluid)) return false; //CHECAR QUE ESTO NO ROMPA EL FUNCIONAMIENTO DEL AGUA
            int sourceLevel = BlockFiniteFluid.getVolume(world, sourcePos, sourceState);

            IBlockState destState = world.getBlockState(destPos);
            Block destBlock = destState.getBlock();
            int destLevel = destBlock instanceof BlockFiniteFluid ? BlockFiniteFluid.getVolume(world, destPos, destState) : -1;

            setCurrentFluidIndex(sourceBlock);
            NewFluidType fluid = (NewFluidType) liquids.get(onFiniteFluidIndex);

            boolean shouldSearchOutward = false;
            if (fluid.flowingBlock instanceof BlockFiniteFluid) {
                shouldSearchOutward = ((BlockFiniteFluid) fluid.flowingBlock).shouldSearchOutward();
            }


            //Calculos horizontales
            if (shouldSearchOutward && recursionDepth < 32) {
                for (EnumFacing dir : EnumFacing.HORIZONTALS) {
                    BlockPos neighbor = destPos.offset(dir);
                    if (!neighbor.equals(sourcePos) && isRealisticFluid(world, neighbor)) {
                        int neighborLevel = BlockFiniteFluid.getVolume(world, neighbor, world.getBlockState(neighbor));
                        if (destLevel > neighborLevel) {
                            return liquidMove(world, sourcePos, neighbor, doMove, recursionDepth + 1);
                        }
                    }
                }

            } else if (!isRealisticFluid(destBlock)) {
                destLevel = -1;
            }
            
            //Reducir deuda de calculos
            int dy = sourcePos.getY() - destPos.getY(); //Es la diferencia entre la altura de nuestro bloque contra el que estamos comparando
            //Idealmente tenddria que salir un resultado positivo
            if (doMove && dy == 0 && Math.abs(destLevel - sourceLevel) < 3 && getCalc() > maxCalc * 0.6f) {
                --calcAmt;
                return true;
            }

            
            //Calculos verticales 
            if (dy == getFluidGravity()) {
                if (FiniteFluidLogic.GeneralPurposeLogic.canMoveInto(world, destPos, sourcePos, sourceLevel, fluid)) {
                    if (doMove) {
                        BlockFiniteFluid.setBlockState(world, destPos, BlockFiniteFluid.setVolume(null, null, fluid.flowingBlock.getDefaultState(), sourceLevel));
                    	FiniteFluidLogic.GeneralPurposeLogic.tryGrab(world, sourcePos, destPos, 0, fluid);
                    }
                    return true;
                } 
                
                //Si el destino tiene agua y tiene espacio para niveles de agua
                //Ecualizacion Vertical
                if (isRealisticFluid(destBlock) && destLevel < BlockFiniteFluid.MAXIMUM_LEVEL) {
                    if (doMove) {
                    	//ECUALIZACION
                    		int realSource = sourceLevel + 1;
                        	int realDest   = destLevel + 1;
                        	int transfer = Math.min(BlockFiniteFluid.MAXIMUM_LEVEL - destLevel, realSource);

                        	realSource -= transfer;
                        	realDest   += transfer;

                        	sourceLevel = realSource - 1;
                        	destLevel   = realDest   - 1;

                        if (sourceLevel >= BlockFiniteFluid.MINIMUM_LEVEL) //SI COMPARAS CONTRA 0, LOS BLOQUES CON VALOR 0 SE VAN AL CARAJO, DEBE SER CONTRA -1
                        	BlockFiniteFluid.setBlockState(world, sourcePos, BlockFiniteFluid.setVolume(null, null, fluid.flowingBlock.getDefaultState(), sourceLevel));
                        else
                            FiniteFluidLogic.GeneralPurposeLogic.tryGrab(world, sourcePos, destPos, 0, fluid);

                        BlockFiniteFluid.setBlockState(world, destPos, BlockFiniteFluid.setVolume(null, null, fluid.flowingBlock.getDefaultState(), destLevel));

                        
                        if (doPressure && destLevel == BlockFiniteFluid.MAXIMUM_LEVEL)
                        	FiniteFluidLogic.GeneralPurposeLogic.addToPressure(world, destPos, false); // 0 = false
                    }
                    return true;
                }
                return false;
            }
        	
            //Ecualizacion horizontal
            if (isRealisticFluid(destBlock)) {
                // Ecualizacion normal si son del mismo material
                if (calcAvg(world, sourcePos, destPos) && destLevel < BlockFiniteFluid.MAXIMUM_LEVEL && sourceLevel > BlockFiniteFluid.MINIMUM_LEVEL) {
                    if (doMove) {
                        int total = sourceLevel + destLevel + 2; //Convertido a LEVELs conceptuales
                        sourceLevel = total / 2;
                        destLevel = total - sourceLevel - 1;
                        --sourceLevel;
                        
                        if (sourceLevel >= BlockFiniteFluid.MINIMUM_LEVEL)
                        	BlockFiniteFluid.setBlockState(world, sourcePos, BlockFiniteFluid.setVolume(null, null, fluid.flowingBlock.getDefaultState(), sourceLevel));
                         else 
                        	FiniteFluidLogic.GeneralPurposeLogic.tryGrab(world, sourcePos, destPos, 0, fluid);


                        BlockFiniteFluid.setBlockState(world, destPos, BlockFiniteFluid.setVolume(null, null, fluid.flowingBlock.getDefaultState(), destLevel));

                        if (doPressure && destLevel == BlockFiniteFluid.MAXIMUM_LEVEL)
                            addToPressure(world, destPos, false); // 0 = false
                    }
                    return true;
                }
                
                
                
                //Division de nuestra agua en otros bloques, horizontalmente hablando
            } else if (FiniteFluidLogic.GeneralPurposeLogic.canMoveInto(world, destPos, sourcePos, sourceLevel, fluid) && sourceLevel > BlockFiniteFluid.MINIMUM_LEVEL) {
                if (doMove) {
                    --sourceLevel;
                    BlockFiniteFluid.setBlockState(world, sourcePos, BlockFiniteFluid.setVolume(null, null, fluid.flowingBlock.getDefaultState(), sourceLevel));
                    BlockFiniteFluid.setBlockState(world, destPos, BlockFiniteFluid.setVolume(null, null, fluid.flowingBlock.getDefaultState(), BlockFiniteFluid.MINIMUM_LEVEL));
                }
                return true;
            }
            return false;
        }
        
        
        public static boolean canMove(World world, BlockPos pos, int level) {
            if (world.isRemote) return false;
            IBlockState actualState = world.getBlockState(pos);

            BlockPos[] targets = new BlockPos[] {
                pos.down(getFluidGravity()),
                pos.add(-1, -1 * getFluidGravity(), 0),
                pos.add(1, -1 * getFluidGravity(), 0),
                pos.add(0, -1 * getFluidGravity(), -1),
                pos.add(0, -1 * getFluidGravity(), 1),
                pos.add(-1, 0, 0),
                pos.add(1, 0, 0),
                pos.add(0, 0, -1),
                pos.add(0, 0, 1)
            };

            for (BlockPos target : targets) {
                if (liquidMove(world, pos, target, false, 0)) return true;
                
                //No recuerdo porque añadi esto :'v
                if (level > BlockFiniteFluid.MINIMUM_CONCEPTUAL_LEVEL) {
                	liquidMove(world, pos, target, true, 0);
                }
                
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
        
        
        
     // shouldPressure: (meta >= 13 y hay agua en pos.down(grav()))
        public static boolean shouldPressure(World world, BlockPos pos) {
            if (!isAWater(world, pos)) {
                //System.out.println("[DEBUG] shouldPressure FALSE (no es agua) pos=" + pos);
                return false;
            }
            IBlockState s = world.getBlockState(pos);
            int level = BlockFiniteFluid.getVolume(world, pos, s);
            boolean result = level > BlockFiniteFluid.Q3_HIGH && isAWater(world, pos.down(getFluidGravity()));
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
            boolean waterUnderByGrav = isAWater(world, pos.down(getFluidGravity()));
            int metaAtOneBelow = -1;
            BlockPos oneBelow = pos.down(); // EXACTO como el original (y - 1), NO usa grav() aquí
            IBlockState st = world.getBlockState(oneBelow);
            if (st.getBlock() instanceof BlockFiniteFluid) {
                metaAtOneBelow = BlockFiniteFluid.getVolume(world, oneBelow, st);
            }
            boolean result = waterUnderByGrav & (metaAtOneBelow > 7);
            //System.out.println("[DEBUG] shouldPressureReverse pos=" + pos + " grav=" + grav()
                    //+ " waterUnderByGrav=" + waterUnderByGrav + " meta(oneBelow)=" + metaAtOneBelow + " -> " + result);
            return result;
        }

        // ===== CHECK PRESSURE (DIRECTA) =====

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
            if (getFluidGravity() > 0) {
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
            Block blockAtSource  = world.getBlockState(source).getBlock();

            // if (!isAWater(var9) & var9 != 0) -> si no es fluido y no es aire, aborta
            if (!isAWater(world, current) & !world.isAirBlock(current)) {
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

            int metaCurrent = BlockFiniteFluid.MINIMUM_LEVEL;
            IBlockState curState = world.getBlockState(current);
            if (curState.getBlock() instanceof BlockFiniteFluid) {
                metaCurrent = BlockFiniteFluid.getVolume(world, current, curState);
            }

            if (metaCurrent <= BlockFiniteFluid.Q2_HIGH) {
                byte defaultMeta = BlockFiniteFluid.Q2_HIGH;
                boolean isAir = world.isAirBlock(current);
                if (isAir) defaultMeta = BlockFiniteFluid.Q2_LOW;

                int newMetaAtCurrent = metaCurrent + BlockFiniteFluid.Q2_LOW;

                // setCurrentWater(block) -> usamos el bloque en source
                setCurrentFluidIndex(blockAtSource);

                // ((liquids.get(onFiniteFluidIndex)).flow) con meta newMetaAtCurrent
                NewFluidType fluid = (NewFluidType) liquids.get(onFiniteFluidIndex);
                IBlockState oldCur = world.getBlockState(current);
                //world.setBlockState(current, fluid.flowingBlock.getDefaultState().withProperty(BlockFiniteFluid.LEVEL, newMetaAtCurrent), 3);
                BlockFiniteFluid.setBlockState(world, current, BlockFiniteFluid.setVolume(null, null, fluid.flowingBlock.getDefaultState(), newMetaAtCurrent));
                world.notifyBlockUpdate(current, oldCur, world.getBlockState(current), 3);

                IBlockState oldSrc = world.getBlockState(source);
                //world.setBlockState(source, fluid.flowingBlock.getDefaultState().withProperty(BlockFiniteFluid.LEVEL, (int)var18), 3);
                BlockFiniteFluid.setBlockState(world, source, BlockFiniteFluid.setVolume(null, null, fluid.flowingBlock.getDefaultState(), (int)defaultMeta));
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
                    addToPressure(world, source.down(getFluidGravity()), false);
                    addToPressure(world, source.add(-1 * xSign, 0, 0), false);
                    addToPressure(world, source.add( 1 * xSign, 0, 0), false);
                    addToPressure(world, source.add(0, 0, -1 * zSign), false);
                    addToPressure(world, source.add(0, 0,  1 * zSign), false);
                } else {
                    addToPressure(world, source.add(0, 0, -1 * zSign), false);
                    addToPressure(world, source.add(0, 0,  1 * zSign), false);
                    addToPressure(world, source.add(-1 * xSign, 0, 0), false);
                    addToPressure(world, source.add( 1 * xSign, 0, 0), false);
                    addToPressure(world, source.down(getFluidGravity()), false);
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
                    if (checkPressure(world, source, current.add(0, -yStep * getFluidGravity(), 0), depth + 1, visited)) return true;
                    if (checkPressure(world, source, current.add(0,  yStep * getFluidGravity(), 0), depth + 1, visited)) return true;
                    if (checkPressure(world, source, current.add(0, 0, -zStep), depth + 1, visited)) return true;
                    if (checkPressure(world, source, current.add(0, 0,  zStep), depth + 1, visited)) return true;
                } else {
                    if (checkPressure(world, source, current.add(0,  yStep * getFluidGravity(), 0), depth + 1, visited)) return true;
                    if (checkPressure(world, source, current.add( xStep, 0, 0), depth + 1, visited)) return true;
                    if (checkPressure(world, source, current.add(0, 0, -zStep), depth + 1, visited)) return true;
                    if (checkPressure(world, source, current.add(0, -yStep * getFluidGravity(), 0), depth + 1, visited)) return true;
                    if (checkPressure(world, source, current.add(-xStep, 0, 0), depth + 1, visited)) return true;
                    if (checkPressure(world, source, current.add(0, 0,  zStep), depth + 1, visited)) return true;
                }

                //System.out.println("[DEBUG] checkPressure SIN CAMINO src=" + source + " ended at cur=" + current + " depth=" + depth);
                return false;
            }
        }

        // ===== CHECK PRESSURE (REVERSA) =====

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
            Block blockAtSource  = world.getBlockState(source).getBlock();

            // if (!isWater(var9) & var7 != 0) -> si current no es agua y depth>0, corta
            if (!isAWater(world, current) & depth != 0) {
                //System.out.println("[DEBUG] checkPressureReverse current no es agua y depth>0. current=" + current);
                return false;
            }

            if (nodeContains(visited, current)) {
                //System.out.println("[DEBUG] checkPressureReverse ya visitado current=" + current + " depth=" + depth);
                return false;
            }

            IBlockState curState = world.getBlockState(current);
            int metaCurrent = (curState.getBlock() instanceof BlockFiniteFluid)
                              ? BlockFiniteFluid.getVolume(world, current, curState) : -1;

            // if (var12 > 7 & var5 >= var2 & var7 > 0)
            if (metaCurrent > BlockFiniteFluid.Q2_LOW & current.getY() >= source.getY() & depth > 0 && (blockAtSource instanceof BlockFiniteFluid)) {
                int newMetaAtCurrent = metaCurrent - BlockFiniteFluid.Q2_HIGH;

                // setCurrentWater(block) usando el bloque del source
                ////System.out.println("LMOVE Position: "+ source);
                ////System.out.println("LMOVE Block: "+ blockAtSource);
                setCurrentFluidIndex(blockAtSource);

                NewFluidType fluid = (NewFluidType) liquids.get(onFiniteFluidIndex);

                IBlockState oldCur = world.getBlockState(current);
                //world.setBlockState(current, fluid.flowingBlock.getDefaultState().withProperty(BlockFiniteFluid.LEVEL, newMetaAtCurrent), 3);
                BlockFiniteFluid.setBlockState(world, current, BlockFiniteFluid.setVolume(null, null, fluid.flowingBlock.getDefaultState(), newMetaAtCurrent));
                world.notifyBlockUpdate(current, oldCur, world.getBlockState(current), 3);

                IBlockState oldSrc = world.getBlockState(source);
                //world.setBlockState(source, fluid.flowingBlock.getDefaultState().withProperty(BlockFiniteFluid.LEVEL, 7), 3);
                BlockFiniteFluid.setBlockState(world, source, BlockFiniteFluid.setVolume(null, null, fluid.flowingBlock.getDefaultState(), 7));
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
                    addToPressure(world, current.down(getFluidGravity()), false);
                    addToPressure(world, current.add(-1 * xSign, 0, 0), false);
                    addToPressure(world, current.add( 1 * xSign, 0, 0), false);
                    addToPressure(world, current.add(0, 0, -1 * zSign), false);
                    addToPressure(world, current.add(0, 0,  1 * zSign), false);
                } else {
                    addToPressure(world, current.add(0, 0, -1 * zSign), false);
                    addToPressure(world, current.add(0, 0,  1 * zSign), false);
                    addToPressure(world, current.add(-1 * xSign, 0, 0), false);
                    addToPressure(world, current.add( 1 * xSign, 0, 0), false);
                    addToPressure(world, current.down(getFluidGravity()), false);
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
                    if (checkPressureReverse(world, source, current.add(0, -yStep * getFluidGravity(), 0), depth + 1, visited)) return true;
                    if (checkPressureReverse(world, source, current.add(0,  yStep * getFluidGravity(), 0), depth + 1, visited)) return true;
                    if (checkPressureReverse(world, source, current.add(0, 0, -zStep), depth + 1, visited)) return true;
                    if (checkPressureReverse(world, source, current.add(0, 0,  zStep), depth + 1, visited)) return true;
                } else {
                    if (checkPressureReverse(world, source, current.add(0,  yStep * getFluidGravity(), 0), depth + 1, visited)) return true;
                    if (checkPressureReverse(world, source, current.add( xStep, 0, 0), depth + 1, visited)) return true;
                    if (checkPressureReverse(world, source, current.add(0, 0, -zStep), depth + 1, visited)) return true;
                    if (checkPressureReverse(world, source, current.add(0, -yStep * getFluidGravity(), 0), depth + 1, visited)) return true;
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
            boolean removed = nodeRemove(pressure, pos) | nodeRemove(rpressure, pos);
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
        
        
        public static boolean tryLiquidMove(World world, BlockPos pos) {
            if (world.isRemote) return false;

            Random rand = new Random();
            int dx = rand.nextBoolean() ? 1 : -1;
            int dz = rand.nextBoolean() ? 1 : -1;
            boolean flip = rand.nextBoolean();

            BlockPos below = pos.down(getFluidGravity());

            if (liquidMove(world, pos, below, true)) return true;

            if (flip) {
                if (liquidMove(world, pos, pos.add(-dx, -getFluidGravity(), 0), true)) return true;
                if (liquidMove(world, pos, pos.add(dx, -getFluidGravity(), 0), true)) return true;
                if (liquidMove(world, pos, pos.add(0, -getFluidGravity(), -dz), true)) return true;
                if (liquidMove(world, pos, pos.add(0, -getFluidGravity(), dz), true)) return true;
                if (liquidMove(world, pos, pos.add(-dx, 0, 0), true)) return true;
                if (liquidMove(world, pos, pos.add(dx, 0, 0), true)) return true;
                if (liquidMove(world, pos, pos.add(0, 0, -dz), true)) return true;
                if (liquidMove(world, pos, pos.add(0, 0, dz), true)) return true;
            } else {
                if (liquidMove(world, pos, pos.add(0, -getFluidGravity(), -dz), true)) return true;
                if (liquidMove(world, pos, pos.add(0, -getFluidGravity(), dz), true)) return true;
                if (liquidMove(world, pos, pos.add(-dx, -getFluidGravity(), 0), true)) return true;
                if (liquidMove(world, pos, pos.add(dx, -getFluidGravity(), 0), true)) return true;
                if (liquidMove(world, pos, pos.add(0, 0, -dz), true)) return true;
                if (liquidMove(world, pos, pos.add(0, 0, dz), true)) return true;
                if (liquidMove(world, pos, pos.add(-dx, 0, 0), true)) return true;
                if (liquidMove(world, pos, pos.add(dx, 0, 0), true)) return true;
            }

            return false;
        }

        
        public static boolean isLDWater(IBlockAccess access, BlockPos pos, int fluidIndex) {
            BlockPos below = pos.down(getFluidGravity());
            return access.isAirBlock(pos)
                && isAnyRealisticFluid(access, below, fluidIndex)
                && !isAnyRealisticFluid(access, pos.west(), fluidIndex)
                && !isAnyRealisticFluid(access, pos.east(), fluidIndex)
                && !isAnyRealisticFluid(access, pos.north(), fluidIndex)
                && !isAnyRealisticFluid(access, pos.south(), fluidIndex);
        }
        

        /**
         * Controls interactions between FiniteFluid's Materials and Vanilla Fluids
         * @param world
         * @param pos
         * @return
         */
        public static boolean checkForNeighborLiquid(World world, BlockPos pos) {
            IBlockState blockState = world.getBlockState(pos);
            Block block = blockState.getBlock();
            int type = getFluidIndex(block);
            
            
            BlockPos above = pos.up();
            Block blockAbove = world.getBlockState(above).getBlock();

            BlockPos below = pos.down();
            Block blockBelow = world.getBlockState(below).getBlock();
            int typeBelow = getFluidIndex(blockBelow);

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

                    if (BlockFiniteFluid.isOceanBlock(world, pos, blockState, type)) {

                        world.setBlockState(pos, Blocks.OBSIDIAN.getDefaultState());
                        world.playSound(null, pos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.5F, 2.6F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.8F);
                        return true;
                    } else if (block == lavaType.flowingBlock || block == lavaType.stillBlock) {
                    	int lavaLevel = BlockFiniteFluid.getVolume(world, pos, blockState);
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
            	


                if (((NewFluidType) liquids.get(typeBelow)).gravity < ((NewFluidType) liquids.get(type)).gravity) {
                	FiniteFluidLogic.GeneralPurposeLogic.flipLiquids(world, pos, below);
                    return true;
                }

                if (block instanceof BlockNewWater_Flow && ((BlockNewWater_Flow) block).interactWithLiquid(world, pos, below)) {
                    return true;
                }

                if (blockBelow instanceof BlockNewWater_Flow && ((BlockNewWater_Flow) blockBelow).interactWithLiquid(world, below, pos)) {
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
                    int typeNeighbor = getFluidIndex(neighborBlock);

                    if (typeNeighbor > -1) {
                        if (
                            (block == ((NewFluidType) liquids.get(type)).stillBlock || ((NewFluidType) liquids.get(type)).gravity > -1) &
                            (neighborBlock == ((NewFluidType) liquids.get(typeNeighbor)).stillBlock || ((NewFluidType) liquids.get(typeNeighbor)).gravity > -1) &&
                            ((NewFluidType) liquids.get(typeNeighbor)).gravity != ((NewFluidType) liquids.get(type)).gravity
                        ) {
                        	flipLiquids(world, pos, neighborPos);
                            return true;
                        }

                        if (block instanceof BlockNewWater_Flow && ((BlockNewWater_Flow) block).interactWithLiquid(world, pos, neighborPos)) {
                            return true;
                        }

                        if (neighborBlock instanceof BlockNewWater_Flow && ((BlockNewWater_Flow) neighborBlock).interactWithLiquid(world, neighborPos, pos)) {
                            return true;
                        }
                    }
                }
            } else {
                // Es un bloque flowing: revisa solo interacciones ligeras
                for (int i = 0; i < 4; ++i) {
                    BlockPos neighborPos = pos.offset(EnumFacing.byHorizontalIndex(i));
                    Block neighborBlock = world.getBlockState(neighborPos).getBlock();

                    int typeNeighbor = getFluidIndex(neighborBlock);
                    if (typeNeighbor > -1) {
                        if (block instanceof BlockNewWater_Flow && ((BlockNewWater_Flow) block).interactWithLiquid(world, pos, neighborPos)) {
                            return true;
                        }

                        if (neighborBlock instanceof BlockNewWater_Flow && ((BlockNewWater_Flow) neighborBlock).interactWithLiquid(world, neighborPos, pos)) {
                            return true;
                        }
                    }
                }
            }

            return false;
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
		
		
	}
	
	
	public static class FluidWorldInteraction{
		@Deprecated
		public static boolean bucketRemoveFluidEvenLowOLD(World world, BlockPos pos, int level) {
	        int totalLevel = level;
		if (level < BlockFiniteFluid.MAXIMUM_LEVEL) {

            BlockPos[] laterals = { pos.north(), pos.south(), pos.east(), pos.west() };
            BlockPos[] diagonals = {
                pos.north().east(), pos.north().west(),
                pos.south().east(), pos.south().west()
            };

            // 1. Laterales
            for (BlockPos p : laterals) {
                if (world.getBlockState(p).getBlock() instanceof BlockFiniteFluid) {
                    int neighborLevel = world.getBlockState(p).getValue(BlockFiniteFluid.LEVEL);
                    int toTake = BlockFiniteFluid.MAXIMUM_LEVEL - level;
                    int newNeighborLevel = neighborLevel - toTake;
                    if (newNeighborLevel < BlockFiniteFluid.MINIMUM_LEVEL) world.setBlockToAir(p);
                    else world.setBlockState(p, world.getBlockState(p).getBlock().getDefaultState().withProperty(BlockFiniteFluid.LEVEL, newNeighborLevel));

                    world.setBlockToAir(pos);
                    totalLevel += neighborLevel;
                    if (totalLevel >= BlockFiniteFluid.MAXIMUM_LEVEL-1) { return true; }
                }
            }


            // 2. Diagonales
            if (totalLevel < BlockFiniteFluid.MAXIMUM_LEVEL) {
                for (BlockPos p : diagonals) {
                    if (world.getBlockState(p).getBlock() instanceof BlockFiniteFluid) {
                        int neighborLevel = world.getBlockState(p).getValue(BlockFiniteFluid.LEVEL);
                        int toTake = BlockFiniteFluid.MAXIMUM_LEVEL - level;
                        int newNeighborLevel = neighborLevel - toTake;
                        if (newNeighborLevel < BlockFiniteFluid.MINIMUM_LEVEL) world.setBlockToAir(p);
                        else world.setBlockState(p, world.getBlockState(p).getBlock().getDefaultState().withProperty(BlockFiniteFluid.LEVEL, newNeighborLevel));

                        world.setBlockToAir(pos);
                        totalLevel += neighborLevel;
                        //>= 14 en vez ded >= 15 para evitar perder un liquiddo solo por un Level
                    	////System.out.println("LEVEL" + (totalLevel));
                        if (totalLevel >= BlockFiniteFluid.MAXIMUM_LEVEL-2) { return true; }
                    }
                }
            }

            // 3. Abajo
            if (totalLevel < BlockFiniteFluid.MAXIMUM_LEVEL) {
                BlockPos below = pos.down();
                IBlockState belowState = world.getBlockState(below);
                if (belowState.getBlock() instanceof BlockFiniteFluid) {
                    world.setBlockState(below, belowState.getBlock().getDefaultState().withProperty(BlockFiniteFluid.LEVEL, level));
                    world.setBlockToAir(pos);
                    return true;
                }
            }
        } else if (level > 14) {
            return true;
        }
		return false;
	}
		
		@Deprecated
		public static boolean bucketRemoveFluidOnlyFullOLD(World world, BlockPos pos, int level) {
	           int totalLevel = level;
	           
	           //int totalLevel = level;

	        // 1. Calcular niveles en laterales
	        BlockPos[] laterals = { pos.north(), pos.south(), pos.east(), pos.west() };
	        for (BlockPos p : laterals) {
	            if (world.getBlockState(p).getBlock() instanceof BlockFiniteFluid) {
	                totalLevel += BlockFiniteFluid.getVolume(world, p, world.getBlockState(p)); //world.getBlockState(p).getValue(BlockFiniteFluid.LEVEL);
	            }
	        }

	        // 2. Calcular niveles en diagonales
	        BlockPos[] diagonals = {
	            pos.north().east(), pos.north().west(),
	            pos.south().east(), pos.south().west()
	        };
	        for (BlockPos p : diagonals) {
	            if (world.getBlockState(p).getBlock() instanceof BlockFiniteFluid) {
	                totalLevel += BlockFiniteFluid.getVolume(world, p, world.getBlockState(p)); //world.getBlockState(p).getValue(BlockFiniteFluid.LEVEL);
	            }
	        }

	        // 3. Calcular nivel abajo
	        BlockPos below = pos.down();
	        IBlockState belowState = world.getBlockState(below);
	        if (belowState.getBlock() instanceof BlockFiniteFluid) {
	            totalLevel += BlockFiniteFluid.getVolume(world, below, belowState); //belowState.getValue(BlockFiniteFluid.LEVEL);
	        }

	        // Si no alcanza 14, no hacemos nada
	        if (totalLevel < 14) {
	            return false; // Nada pasa
	        }

	        // Ahora sí, remover líquido para simular la recogida
	        int needed = BlockFiniteFluid.MAXIMUM_LEVEL;
	        //shouldContinue = true;
	        int remaining = needed - level;
	        world.setBlockToAir(pos);

	        // Consumir laterales
	        for (BlockPos p : laterals) {
	            if (remaining <= BlockFiniteFluid.MINIMUM_LEVEL) break;
	            IBlockState s = world.getBlockState(p);
	            if (s.getBlock() instanceof BlockFiniteFluid) {
	                int neighborLevel = BlockFiniteFluid.getVolume(world, p, s); //s.getValue(BlockFiniteFluid.LEVEL);
	                int take = Math.min(neighborLevel, remaining);
	                int newLevel = neighborLevel - take;
	                if (newLevel <= BlockFiniteFluid.MINIMUM_LEVEL) world.setBlockToAir(p);
	                else world.setBlockState(p, s.getBlock().getDefaultState().withProperty(BlockFiniteFluid.LEVEL, newLevel));
	                remaining -= take;
	            }
	        }

	        // Consumir diagonales
	        if (remaining > BlockFiniteFluid.MINIMUM_LEVEL) {
	            for (BlockPos p : diagonals) {
	                if (remaining <= BlockFiniteFluid.MINIMUM_LEVEL) break;
	                IBlockState s = world.getBlockState(p);
	                if (s.getBlock() instanceof BlockFiniteFluid) {
	                    int neighborLevel = BlockFiniteFluid.getVolume(world, p, s); //s.getValue(BlockFiniteFluid.LEVEL);
	                    int take = Math.min(neighborLevel, remaining);
	                    int newLevel = neighborLevel - take;
	                    if (newLevel <= BlockFiniteFluid.MINIMUM_LEVEL) world.setBlockToAir(p);
	                    else world.setBlockState(p, s.getBlock().getDefaultState().withProperty(BlockFiniteFluid.LEVEL, newLevel));
	                    remaining -= take;
	                }
	            }
	        }

	        // Consumir abajo (si queda algo)
	        if (remaining > BlockFiniteFluid.MINIMUM_LEVEL && belowState.getBlock() instanceof BlockFiniteFluid) {
	            int neighborLevel = BlockFiniteFluid.getVolume(world, below, belowState); //belowState.getValue(BlockFiniteFluid.LEVEL);
	            int take = Math.min(neighborLevel, remaining);
	            int newLevel = neighborLevel - take;
	            if (newLevel <= BlockFiniteFluid.MINIMUM_LEVEL) world.setBlockToAir(below);
	            else world.setBlockState(below, belowState.getBlock().getDefaultState().withProperty(BlockFiniteFluid.LEVEL, newLevel));
	        }
	        return true;
		}
		
		
		
		
		///////////////////////////////////////////////////////
		
		
		
		public static boolean bucketRemoveFluidEvenLowNEW(World world, BlockPos pos, int level) {
		    IBlockState centerState = world.getBlockState(pos);
		    if (!(centerState.getBlock() instanceof IFluidBlock)) return false;

		    IFluidBlock centerBlock = (IFluidBlock) centerState.getBlock();
		    Fluid targetFluid = centerBlock.getFluid(); // fluido del bloque central

		    int collected = level; // Nivel inicial del bloque central
		    world.setBlockToAir(pos); // Removemos el bloque principal

		    if (collected >= BlockFiniteFluid.MAXIMUM_CONCEPTUAL_LEVEL) return true; // Ya lleno, terminamos

		    // Posiciones laterales y diagonales
		    BlockPos[] laterals = { pos.north(), pos.south(), pos.east(), pos.west() };
		    BlockPos[] diagonals = {
		        pos.north().east(), pos.north().west(),
		        pos.south().east(), pos.south().west()
		    };

		    // Recolectar suavemente
		    collected = collectEqually(world, laterals, collected, BlockFiniteFluid.MAXIMUM_CONCEPTUAL_LEVEL, targetFluid);
		    if (collected < BlockFiniteFluid.MAXIMUM_CONCEPTUAL_LEVEL) collected = collectEqually(world, diagonals, collected, BlockFiniteFluid.MAXIMUM_CONCEPTUAL_LEVEL, targetFluid);

		    // Última oportunidad: abajo
		    if (collected < BlockFiniteFluid.MAXIMUM_CONCEPTUAL_LEVEL) {
		        BlockPos below = pos.down();
		        IBlockState belowState = world.getBlockState(below);
		        if (belowState.getBlock() instanceof IFluidBlock) {
		            IFluidBlock fluidBlock = (IFluidBlock) belowState.getBlock();
		            if (fluidBlock.getFluid() == targetFluid) {
		                int neighborLevel = BlockFiniteFluid.getConceptualVolume(world, below, belowState);
		                int take = Math.min(neighborLevel, BlockFiniteFluid.MAXIMUM_CONCEPTUAL_LEVEL - collected);
		                int newLevel = neighborLevel - take;

		                if (newLevel <= BlockFiniteFluid.MINIMUM_LEVEL) {
		                    if (newLevel == BlockFiniteFluid.MINIMUM_LEVEL) {
		                        //world.setBlockState(below, belowState.withProperty(BlockFiniteFluid.LEVEL, 0)); // nivel mínimo
		                    	BlockFiniteFluid.setBlockState(world, below, BlockFiniteFluid.setVolume(world, below, belowState, BlockFiniteFluid.MINIMUM_LEVEL));
		                    } else {
		                        world.setBlockToAir(below); // newLevel < 0
		                    }
		                } else {
		                    world.setBlockState(below, BlockFiniteFluid.setConceptualVolume(world, below, belowState, newLevel));
		                }
		                collected += take;
		            }
		        }
		    }

		    return collected >= BlockFiniteFluid.MAXIMUM_CONCEPTUAL_LEVEL; // True si alcanzó 16 niveles conceptuales, false si no
		}

		
		public static boolean bucketRemoveFluidOnlyFullNEW(World world, BlockPos pos, int level) {
		    IBlockState centerState = world.getBlockState(pos);
		    if (!(centerState.getBlock() instanceof IFluidBlock)) return false;

		    IFluidBlock centerBlock = (IFluidBlock) centerState.getBlock();
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
		        if (state.getBlock() instanceof IFluidBlock) {
		            IFluidBlock fluidBlock = (IFluidBlock) state.getBlock();
		            if (fluidBlock.getFluid() == targetFluid) {
		                totalAvailable += BlockFiniteFluid.getConceptualVolume(world, p, state);
		            }
		        }
		    }

		    for (BlockPos p : diagonals) {
		        IBlockState state = world.getBlockState(p);
		        if (state.getBlock() instanceof IFluidBlock) {
		            IFluidBlock fluidBlock = (IFluidBlock) state.getBlock();
		            if (fluidBlock.getFluid() == targetFluid) {
		                totalAvailable += BlockFiniteFluid.getConceptualVolume(world, p, state);
		            }
		        }
		    }

		    if (belowState.getBlock() instanceof IFluidBlock) {
		        IFluidBlock fluidBlock = (IFluidBlock) belowState.getBlock();
		        if (fluidBlock.getFluid() == targetFluid) {
		            totalAvailable += BlockFiniteFluid.getConceptualVolume(world, below, belowState);
		        }
		    }

		    if (totalAvailable < BlockFiniteFluid.MAXIMUM_CONCEPTUAL_LEVEL) return false;

		    // --- Recolectar ---
		    world.setBlockToAir(pos);

		    collected = collectEqually(world, laterals, collected, BlockFiniteFluid.MAXIMUM_CONCEPTUAL_LEVEL, targetFluid);
		    if (collected < BlockFiniteFluid.MAXIMUM_CONCEPTUAL_LEVEL) collected = collectEqually(world, diagonals, collected, BlockFiniteFluid.MAXIMUM_CONCEPTUAL_LEVEL, targetFluid);
		    if (collected < BlockFiniteFluid.MAXIMUM_CONCEPTUAL_LEVEL && belowState.getBlock() instanceof IFluidBlock) {
		        IFluidBlock fluidBlock = (IFluidBlock) belowState.getBlock();
		        if (fluidBlock.getFluid() == targetFluid) {
		            int neighborLevel = BlockFiniteFluid.getConceptualVolume(world, below, belowState);
		            int take = Math.min(neighborLevel, BlockFiniteFluid.MAXIMUM_CONCEPTUAL_LEVEL - collected);
		            int newLevel = neighborLevel - take;
		            if (newLevel <= BlockFiniteFluid.MINIMUM_LEVEL) world.setBlockToAir(below);
		            else world.setBlockState(below, BlockFiniteFluid.setConceptualVolume(world, below, belowState, newLevel));
		            collected += take;
		        }
		    }

		    return true;
		}
		
		public static int collectEqually(World world, BlockPos[] positions, int collected, int spaceLeft, Fluid targetFluid) {
		    for (BlockPos pos : positions) {
		        IBlockState state = world.getBlockState(pos);
		        Block block = state.getBlock();
		        if (block instanceof BlockFiniteFluid) {
		            Fluid fluid = ((BlockFiniteFluid) block).getFluid();
		            if (fluid != targetFluid) continue; // skip distinto tipo

		            int neighborLevel = BlockFiniteFluid.getConceptualVolume(world, pos, state); //state.getValue(BlockFiniteFluid.LEVEL) + 1;
		            int take = Math.min(neighborLevel, spaceLeft - collected);
		            int newLevel = neighborLevel - take;
		            if (newLevel <= BlockFiniteFluid.MINIMUM_LEVEL) world.setBlockToAir(pos);
		            else BlockFiniteFluid.setBlockState(world, pos, BlockFiniteFluid.setConceptualVolume(world, pos, state, newLevel)); //world.setBlockState(pos, state.withProperty(BlockFiniteFluid.LEVEL, newLevel - 1));
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

                if (BlockFiniteFluid.isOceanBlock(world, neighbor, neighborState, GeneralPurposeLogic.getFluidIndex(neighborState.getBlock()))) {
                    exposedToOceanWater = true;
                    
                    if (exposedToOceanWater
                        && !(world.getBlockState(pos.down()).getBlock() instanceof BlockFiniteFluid)
                        && !(world.getBlockState(pos).getBlock() instanceof BlockFiniteFluid)) {
                        FiniteFluidLogic.InfiniteWaterSource.borderOceanCheck(world, pos, false);
                    }
                    break;
                }
            }
	    }
		
		
		
		public static int distributeEqually(World world, List<BlockPos> targets, int remaining, int fluidType) {
            if (targets.isEmpty() || remaining <= BlockFiniteFluid.MINIMUM_LEVEL) return remaining; //original: <=0, ahora es <0, para aceptar al 0 entre los valores

            boolean didSomething = true;

            // Repite mientras queden niveles que distribuir
            while (remaining > BlockFiniteFluid.MINIMUM_LEVEL && didSomething) {
                didSomething = false;
                for (BlockPos p : targets) {
                    if (remaining <= BlockFiniteFluid.MINIMUM_LEVEL) break; //original: <=0, ahora es <0, para aceptar al 0 entre los valores

                    IBlockState s = world.getBlockState(p);
                    if (!(s.getBlock() instanceof BlockFiniteFluid) || FiniteFluidLogic.GeneralPurposeLogic.getFluidIndex(s.getBlock()) != fluidType) continue; //Para que no crashee la IC2 FluidCell en el CASO 1) xd
                    int level = BlockFiniteFluid.getVolume(world, p, s);
                    if (level < BlockFiniteFluid.MAXIMUM_LEVEL) {
                    	int temporalFluidType = FiniteFluidLogic.GeneralPurposeLogic.getFluidIndex(s.getBlock()); 
                    	Block newBlock1 = ((NewFluidType)liquids.get(temporalFluidType)).flowingBlock;
                    	
                        //world.setBlockState(p, newBlock1.getDefaultState().withProperty(BlockFiniteFluid.LEVEL, level + 1)); //s.withProperty(RFFBlock.LEVEL, level + 1));
                        BlockFiniteFluid.setBlockState(world, p, BlockFiniteFluid.setVolume(null, null, newBlock1.getDefaultState(), level+1));	
                        remaining--;
                        
                        didSomething = true;
                    }
                }
            }

            return remaining;
        }
		
		public static int distributeEquallyNoAdyFluid(World world, List<BlockPos> targets, int remaining, Block finiteFluidBlock) {
            if (targets.isEmpty() || remaining <= BlockFiniteFluid.MINIMUM_LEVEL) return remaining; //original: <=0, ahora es <0, para aceptar al 0 entre los valores

            boolean didSomething = true;

            // Repite mientras queden niveles que distribuir
            while (remaining > BlockFiniteFluid.MINIMUM_LEVEL && didSomething) {
                didSomething = false;
                for (BlockPos p : targets) {
                    if (remaining <= BlockFiniteFluid.MINIMUM_LEVEL) break; //original: <=0, ahora es <0, para aceptar al 0 entre los valores

                    IBlockState s = world.getBlockState(p);
                    if (world.isAirBlock(p) && !(s.getBlock() instanceof BlockFiniteFluid)) { //Para que no crashee la IC2 FluidCell en el CASO 1) xd
                        //if (!(remaining > 16)) { //16 porque estamos en LEVELs conceptuales
                        	//world.setBlockState(p, finiteFluidBlock.getDefaultState().withProperty(BlockFiniteFluid.LEVEL, remaining-1)); //ModBlocks.FINITE_WATER_FLOWING.getDefaultState().withProperty(RFFBlock.LEVEL, remaining-1));
                        BlockFiniteFluid.setBlockState(world, p, BlockFiniteFluid.setConceptualVolume(null, null, finiteFluidBlock.getDefaultState(), remaining));	
                    	return BlockFiniteFluid.MINIMUM_LEVEL;	
                        /*} else {
                        	world.setBlockState(p, finiteFluidBlock.getDefaultState().withProperty(RFFBlock.LEVEL, 15)); //15 porque esta en LEVELs directos
                        	return remaining -= 16; //-16 porque esta en LEVELs conceptuales
                        }*/
                    } 
                    
                    int level = BlockFiniteFluid.getVolume(world, p, s);
                    if (level < BlockFiniteFluid.MAXIMUM_LEVEL) {
                        //world.setBlockState(p, s.withProperty(BlockFiniteFluid.LEVEL, level + 1));
                        BlockFiniteFluid.setBlockState(world, p, BlockFiniteFluid.setVolume(world, p, s, level + 1));	
                        remaining--;
                        
                        didSomething = true;
                    }
                }
            }

            return remaining;
        }
		
		
		
		
		
		
		
		


		public static int bucketRemoveFluidEvenLowCollect(
		        World world, BlockPos pos, int levelConceptual, int spaceLeft, Fluid targetFluid) {

		    int original = levelConceptual;
		    int collected = 0;

		    IBlockState state = world.getBlockState(pos);
		    Block block = state.getBlock();

		    // Aseguramos que el bloque inicial corresponda al fluid target
		    if (!(block instanceof BlockFiniteFluid)) return 0;
		    Fluid fluidHere = ((BlockFiniteFluid) block).getFluid();
		    if (fluidHere != targetFluid) return 0;

		    // cuánto puedo realmente tomar del bloque central
		    int takeFromCenter = Math.min(original, spaceLeft);
		    if (takeFromCenter > BlockFiniteFluid.MINIMUM_LEVEL) {
		        collected += takeFromCenter;
		        int newLevelCenter = original - takeFromCenter;
		        if (newLevelCenter <= BlockFiniteFluid.MINIMUM_LEVEL) {
		            world.setBlockToAir(pos);
		            activateOcean(world, pos);
		        } else {
		            //world.setBlockState(pos, state.withProperty(BlockFiniteFluid.LEVEL, newLevelCenter - 1));
                	BlockFiniteFluid.setBlockState(world, pos, BlockFiniteFluid.setConceptualVolume(world, pos, state, newLevelCenter));
		        }
		    }

		    // si ya está lleno, salimos
		    if (collected >= spaceLeft) {
		        return collected;
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
		        if (belowBlock instanceof BlockFiniteFluid) {
		            Fluid belowFluid = ((BlockFiniteFluid) belowBlock).getFluid();
		            if (belowFluid == targetFluid) {
		                int neighborLevel = BlockFiniteFluid.getConceptualVolume(world, below, belowState); //belowState.getValue(BlockFiniteFluid.LEVEL) + 1; // conceptual
		                int take = Math.min(neighborLevel, spaceLeft - collected);
		                int newLevel = neighborLevel - take;
		                if (newLevel <= BlockFiniteFluid.MINIMUM_LEVEL) {world.setBlockToAir(below);  activateOcean(world, below);}
		                else BlockFiniteFluid.setBlockState(world, below, BlockFiniteFluid.setConceptualVolume(world, below, belowState, newLevel)); //world.setBlockState(below, belowState.withProperty(BlockFiniteFluid.LEVEL, newLevel - 1));
		                collected += take;
		            }
		        }
		    }

		    return collected;
		}


		
		// Igual pero solo si SÍ había suficiente para una cubeta completa (solo-full).
		// Devuelve los niveles extraidos (>=0). Si no hay suficientes, devuelve 0 y no modifica nada.
		public static int bucketRemoveFluidOnlyFullCollect(World world, BlockPos pos, int levelConceptual, Fluid targetFluid) {
		    int total = levelConceptual;

		    BlockPos[] laterals = { pos.north(), pos.south(), pos.east(), pos.west() };
		    BlockPos[] diagonals = {
		        pos.north().east(), pos.north().west(),
		        pos.south().east(), pos.south().west()
		    };
		    for (BlockPos p : laterals) {
		        IBlockState s = world.getBlockState(p);
		        if (s.getBlock() instanceof BlockFiniteFluid) total += BlockFiniteFluid.getConceptualVolume(world, p, s); //s.getValue(BlockFiniteFluid.LEVEL) + 1;
		    }
		    for (BlockPos p : diagonals) {
		        IBlockState s = world.getBlockState(p);
		        if (s.getBlock() instanceof BlockFiniteFluid) total += BlockFiniteFluid.getConceptualVolume(world, p, s); //s.getValue(BlockFiniteFluid.LEVEL) + 1;
		    }
		    BlockPos below = pos.down();
		    IBlockState belowState = world.getBlockState(below);
		    if (belowState.getBlock() instanceof BlockFiniteFluid) total += BlockFiniteFluid.getConceptualVolume(world, below, belowState); //belowState.getValue(BlockFiniteFluid.LEVEL) + 1;

		    // si no hay suficientes niveles (conceptuales) abortamos
		    if (total < BlockFiniteFluid.MAXIMUM_CONCEPTUAL_LEVEL) return BlockFiniteFluid.MINIMUM_LEVEL;

		    // Si hay suficientes, borramos/extraemos suavemente igual que en EvenLow
		    int original = levelConceptual;
		    int collected = original;
		    world.setBlockToAir(pos);

		    collected = collectEqually(world, laterals, collected, BlockFiniteFluid.MAXIMUM_CONCEPTUAL_LEVEL, targetFluid);
		    if (collected < BlockFiniteFluid.MAXIMUM_CONCEPTUAL_LEVEL) collected = collectEqually(world, diagonals, collected, BlockFiniteFluid.MAXIMUM_CONCEPTUAL_LEVEL, targetFluid);
		    if (collected < BlockFiniteFluid.MAXIMUM_CONCEPTUAL_LEVEL && belowState.getBlock() instanceof BlockFiniteFluid) {
		        int neighborLevel = BlockFiniteFluid.getConceptualVolume(world, below, belowState); //belowState.getValue(BlockFiniteFluid.LEVEL) + 1;
		        int take = Math.min(neighborLevel, BlockFiniteFluid.MAXIMUM_CONCEPTUAL_LEVEL - collected);
		        int newLevel = neighborLevel - take;
		        if (newLevel <= BlockFiniteFluid.MINIMUM_LEVEL) world.setBlockToAir(below);
		        else BlockFiniteFluid.setBlockState(world, below, BlockFiniteFluid.setConceptualVolume(world, below, belowState, newLevel)); //world.setBlockState(below, belowState.withProperty(BlockFiniteFluid.LEVEL, newLevel - 1));
		        collected += take;
		    }

		    return Math.max(BlockFiniteFluid.MINIMUM_LEVEL, collected - original);
		}
		


		
		
		
		
		
		
		
	}
    
	public static class math {
		//getEuclidianDistance
	    public double getDist(double posX1, double posY1, double posZ1, double posX2, double posY2, double posZ2)
	    {
	        double xDiferencial = posX1 - posX2;
	        double yDiferencial = posY1 - posY2;
	        double zDiferencial = posZ1 - posZ2;
	        return xDiferencial * xDiferencial + yDiferencial * yDiferencial + zDiferencial * zDiferencial;
	    }
	    
	    
	}
	
	

}
