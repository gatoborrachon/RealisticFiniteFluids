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
import com.gatoborrachon.realisticfinitefluids.blocks.BlockNewInfiniteSource;
import com.gatoborrachon.realisticfinitefluids.blocks.BlockNewWater_Flow;
import com.gatoborrachon.realisticfinitefluids.init.ModBlocks;
import com.gatoborrachon.realisticfinitefluids.init.ModConfig;

import git.jbredwards.fluidlogged_api.api.util.FluidState;
import git.jbredwards.fluidlogged_api.api.util.FluidloggedUtils;
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
                ModBlocks.INFINITE_WATER_SOURCE, 1, true));
        
        //System.out.println("WATER INDEX: "+(liquids.size()-1));
        fluidIndexMap.put("water", (liquids.size() - 1));
        blockToFluidIndex.put(ModBlocks.FINITE_WATER_FLOWING, liquids.size() - 1);
        blockToFluidIndex.put(ModBlocks.FINITE_WATER_STILL, liquids.size() - 1);
        blockToFluidIndex.put(ModBlocks.INFINITE_WATER_SOURCE, liquids.size() - 1);
        //System.out.println("REVISA ESTA MMDA añadir: "+FiniteFluidLogic.GeneralPurposeLogic.getFluidIndex(ModBlocks.FINITE_WATER_FLOWING));        
        
        
        liquids.add(new NewFluidType("lava",
                ModBlocks.FINITE_LAVA_FLOWING,
                ModBlocks.FINITE_LAVA_STILL,
                ModBlocks.INFINITE_LAVA_SOURCE, 1, true));
        
        //System.out.println("LAVA INDEX: "+(liquids.size()-1));
        fluidIndexMap.put("lava", (liquids.size() - 1));
        blockToFluidIndex.put(ModBlocks.FINITE_LAVA_FLOWING, (liquids.size() - 1));
        blockToFluidIndex.put(ModBlocks.FINITE_LAVA_STILL, (liquids.size() - 1));
        blockToFluidIndex.put(ModBlocks.INFINITE_LAVA_SOURCE, (liquids.size() - 1));
        
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
    	
    	//static final Set<BlockPos> runningOceanSearches = Collections.newSetFromMap(new WeakHashMap<>());
    	
        public static boolean tryOceanMove(World world, BlockPos pos) {
            if (world.isRemote) return false;

            Random rand = new Random();
            int dx = rand.nextBoolean() ? 1 : -1;
            int dz = rand.nextBoolean() ? 1 : -1;
            boolean flip = rand.nextBoolean();

            BlockPos below = pos.down(FiniteFluidLogic.GeneralPurposeLogic.getFluidGravity());

            if (oMove(world, pos, below, true)) return true;

            if (flip) {
                if (oMove(world, pos, pos.add(-dx, -FiniteFluidLogic.GeneralPurposeLogic.getFluidGravity(), 0), true)) return true;
                if (oMove(world, pos, pos.add(dx, -FiniteFluidLogic.GeneralPurposeLogic.getFluidGravity(), 0), true)) return true;
                if (oMove(world, pos, pos.add(0, -FiniteFluidLogic.GeneralPurposeLogic.getFluidGravity(), -dz), true)) return true;
                if (oMove(world, pos, pos.add(0, -FiniteFluidLogic.GeneralPurposeLogic.getFluidGravity(), dz), true)) return true;
                if (oMove(world, pos, pos.add(-dx, 0, 0), true)) return true;
                if (oMove(world, pos, pos.add(dx, 0, 0), true)) return true;
                if (oMove(world, pos, pos.add(0, 0, -dz), true)) return true;
                if (oMove(world, pos, pos.add(0, 0, dz), true)) return true;
            } else {
                if (oMove(world, pos, pos.add(0, -FiniteFluidLogic.GeneralPurposeLogic.getFluidGravity(), -dz), true)) return true;
                if (oMove(world, pos, pos.add(0, -FiniteFluidLogic.GeneralPurposeLogic.getFluidGravity(), dz), true)) return true;
                if (oMove(world, pos, pos.add(-dx, -FiniteFluidLogic.GeneralPurposeLogic.getFluidGravity(), 0), true)) return true;
                if (oMove(world, pos, pos.add(dx, -FiniteFluidLogic.GeneralPurposeLogic.getFluidGravity(), 0), true)) return true;
                if (oMove(world, pos, pos.add(0, 0, -dz), true)) return true;
                if (oMove(world, pos, pos.add(0, 0, dz), true)) return true;
                if (oMove(world, pos, pos.add(-dx, 0, 0), true)) return true;
                if (oMove(world, pos, pos.add(dx, 0, 0), true)) return true;
            }

            return false;
        }
        
        public static boolean oMove(World world, BlockPos from, BlockPos to, boolean doMove) {
            if (world.isRemote) return false;

            IBlockState sourceState = world.getBlockState(from);
            IBlockState destState = world.getBlockState(to);

            Block sourceBlock = sourceState.getBlock();
            Block destBlock = destState.getBlock();

            FiniteFluidLogic.GeneralPurposeLogic.setCurrentFluidIndex(sourceBlock, world, from);
            NewFluidType fluid = (NewFluidType) liquids.get(onFiniteFluidIndex);


            // Si el destino no es agua y cierta probabilidad +
            if (!FiniteFluidLogic.GeneralPurposeLogic.isRealisticFluid(destBlock) && !(new Random().nextInt(10) == 0)) {
                //se puede reemplazar -->
            	if (FiniteFluidLogic.GeneralPurposeLogic.canMoveInto(world, to, from, 15, fluid)) {
            		//Remplaza por agua still
                    if (doMove) {
                    	FiniteFluidLogic.GeneralPurposeLogic.setCurrentFluidIndex(sourceBlock, world, from);
                        //world.setBlockState(to, fluid.stillBlock.getDefaultState().withProperty(BlockFiniteFluid.LEVEL, 4), 3);
                    	BlockFiniteFluid.setBlockState(world, to, BlockFiniteFluid.setVolume(fluid.stillBlock.getDefaultState(), 4));
                        world.scheduleUpdate(from, fluid.oceanBlock, waterTick + 1);
                        return true;
                    } else {
                        return false;
                    }
                }
            }

            // Si es agua pero no oceanica, abortar (sin este codigo, el agua perfora el mundo hacia abajo)
            if (!FiniteFluidLogic.GeneralPurposeLogic.isRealisticFluidNoOcean(destBlock)) return false;

            // Si el destino es agua oceanica (sin este codigo, el agua oceanica solo coloca 1 agua normal y muere)
            if (doMove) {
            	FiniteFluidLogic.GeneralPurposeLogic.setCurrentFluidIndex(sourceBlock, world, from);
                world.setBlockState(to, fluid.oceanBlock.getDefaultState());
                world.scheduleUpdate(from, fluid.oceanBlock, waterTick + 1);
            }
             
            return true;
        }
        
        
        public static void borderOceanCheck(World world, BlockPos pos, int ignored) {
            smallOceanSearch = true;
            borderOceanCheck(world, pos);
        }
        
        public static void borderOceanCheck(World world, BlockPos pos) {
        	//--NO SIRVIO--Evitar calculos a bloques que sean agua???
        	/*if (!FiniteFluidLogic.GeneralPurposeLogic.isWater(world.getBlockState(pos).getBlock())) {
        		System.out.println("BLOQUE 2AGUA: "+ world.getBlockState(pos));
        		return;
        	}*/
        	//System.out.println(pos);
        		
            if (world.isRemote) return;
            
            if (doPressure) {
                FiniteFluidLogic.GeneralPurposeLogic.addToPressure(world, pos, true); // 1 se interpreta como `true` para reverse
            }

            int detectedType = -1;

            // Revisamos este bloque y sus adyacentes por agua
            for (BlockPos offset : new BlockPos[] {
                    pos, pos.west(), pos.east(),
                    pos.down(), pos.up(),
                    pos.north(), pos.south()
            }) {
                int type = FiniteFluidLogic.GeneralPurposeLogic.getFluidIndex(world.getBlockState(offset).getBlock(), world, offset);
                if (detectedType == -1 && type != -1) {
                    detectedType = type;
                }
            }

            if (detectedType < 0) {
                onFiniteFluidIndex = 0;
            } else {
                onFiniteFluidIndex = detectedType;
                int savedType = onFiniteFluidIndex < 0 ? 0 : onFiniteFluidIndex;

                for (int i = 0; i < liquids.size(); i++) {
                	NewFluidType type = (NewFluidType) liquids.get(i);
                    if (type != null && type.oceanBlock != null) {
                        onFiniteFluidIndex = i;

                        boolean valid = true;

                        if (!FiniteFluidLogic.GeneralPurposeLogic.borderBlock(world, pos, type.oceanBlock)) {
                            if (world.getBlockState(pos).getBlock() == type.oceanBlock) {
                                //world.setBlockState(pos, type.flowingBlock.getDefaultState()
                                //        .withProperty(BlockFiniteFluid.LEVEL, 15), 3);
                            	BlockFiniteFluid.setBlockState(world, pos, BlockFiniteFluid.setVolume(type.flowingBlock.getDefaultState(), 15));

                            }

                            onFiniteFluidIndex = savedType;
                            valid = false;
                        }

                        if (valid && onFiniteFluidIndex > -1) {
                            borderOceanCheck2(world, pos.east());
                            borderOceanCheck2(world, pos.west());
                            borderOceanCheck2(world, pos.south());
                            borderOceanCheck2(world, pos.north());
                            borderOceanCheck2(world, pos.up());
                            borderOceanCheck2(world, pos.down());
                        }
                    }
                }

                wakeOcean(world, pos);
                onFiniteFluidIndex = savedType < 0 ? 0 : savedType;
            }
        }
        
        public static boolean hasNearbyOceanWater(World world, BlockPos pos) {
            NewFluidType fluid = (NewFluidType) liquids.get(onFiniteFluidIndex);
            for (BlockPos offset : new BlockPos[] {
                pos.north(), pos.south(), pos.east(), pos.west(), pos.up()/*, pos.down()*/
            }) {
                if (world.getBlockState(offset).getBlock() == fluid.oceanBlock) {
                    return true;
                }
            }
            return false;
        }
        
        
        //Version actaulizada a BlockPos
        public static void borderOceanCheck2(World world, BlockPos pos) {
            if (world.isRemote || onFiniteFluidIndex < 0) return;

            if (doPressure) {
                FiniteFluidLogic.GeneralPurposeLogic.addToPressure(world, pos, true);
            }
            
            

            Block target = world.getBlockState(pos).getBlock();
            NewFluidType fluid = (NewFluidType) liquids.get(onFiniteFluidIndex);
            
            if (target == fluid.oceanBlock) {
            	//if (runningOceanSearches.contains(pos)) return; // ya lo están procesando
            	//runningOceanSearches.add(pos);
            	
                List<BlockPos> result = oceanSearch(world, pos, onFiniteFluidIndex);
                if (result == null || result.isEmpty()) {
                    world.scheduleBlockUpdate(pos, target, waterTick + 1, 0);
                } else {
                    oceanToStill(world, result);
                }
                //runningOceanSearches.remove(pos); // <- Aquí va, SIEMPRE
            }
        }
        
        
        /**
         * A function to "wake" adyacent ocean blocks. Doesn't calculate whether they should become stll-->flowing blocks.
         * @param world
         * @param pos
         */
        public static void wakeOcean(World world, BlockPos pos) {
            int backupType = onFiniteFluidIndex;

            Block block = world.getBlockState(pos).getBlock();
            if (block == null || blockToFluidIndex.get(block) == null) return;
            int fluidIndex = blockToFluidIndex.get(block);

            if (fluidIndex >= 0 && fluidIndex < liquids.size()) {
                NewFluidType type = liquids.get(fluidIndex);
                if (type != null && type.oceanBlock != null) {
                    onFiniteFluidIndex = fluidIndex;

                    // Recorrer todos los vecinos usando EnumFacing
                    for (EnumFacing dir : EnumFacing.values()) {
                        BlockPos neighbor = pos.offset(dir);
                        Block neighborBlock = world.getBlockState(neighbor).getBlock();

                        if (neighborBlock == type.oceanBlock) {
                            world.scheduleBlockUpdate(neighbor, neighborBlock, block.tickRate(world) + 1, 0);
                        }
                    }
                }
            }

            onFiniteFluidIndex = Math.max(0, backupType);
        }
        
        /*public static void wakeOcean(World world, BlockPos pos) {
            int backupType = onFiniteFluidIndex;
            
            //for (EnumFacing dir : EnumFacing.VALUES) {
            //    wakeOcean2(world, pos.offset(dir));
            //}

            for (int i = 0; i < liquids.size(); i++) {
                NewFluidType type = (NewFluidType) liquids.get(i);
                if (type != null && type.oceanBlock != null) {
                	
                    onFiniteFluidIndex = i;
                    wakeOcean2(world, pos.east());
                    wakeOcean2(world, pos.west());
                    wakeOcean2(world, pos.south());
                    wakeOcean2(world, pos.north());
                    wakeOcean2(world, pos.up());
                    wakeOcean2(world, pos.down());
                   //BASURA if (world.getBlockState(pos) == type.flowingBlock)
                   //	world.setBlockToAir(pos);
                }
            }

            onFiniteFluidIndex = Math.max(0, backupType);
        }
        
        
        public static void wakeOcean2(World world, BlockPos pos) {
            Block ocean = ((NewFluidType) liquids.get(onFiniteFluidIndex)).oceanBlock;
            if (world.getBlockState(pos).getBlock() == ocean) {
            	//System.out.println("TIPO DE AWA: "+ocean);
                world.scheduleBlockUpdate(pos, ocean, waterTick + 1, 0);
            }
        }*/
        
        
        //Version actualizada a BlockPos
        public static List<BlockPos> oceanSearch(World world, BlockPos start, int fluidIndex) {
            Set<BlockPos> visited = new HashSet<>();
            Queue<BlockPos> queue = new LinkedList<>();
            
           	////System.out.println("[DEBUG] Ocean search desde: " + start);
        	
        	//if (visited.size() % 100 == 0) {
        	//}
        	


            int limit = smallOceanSearch ? lakeLimit / 10 + 1 : lakeLimit;

            Block target = ((NewFluidType) liquids.get(fluidIndex)).oceanBlock;

            visited.add(start);
            queue.add(start);

            while (!queue.isEmpty() && visited.size() < limit) {            	
                BlockPos current = queue.poll();
                //System.out.println(current);
                ////System.out.println(world.getBlockState(current));
        	    ////System.out.println("[DEBUG] Visitados: " + visited.size());
        	    
            	if (visited.size() > limit) {
            	    ////System.out.println("[PROTECCION] OceanSearch cancelado por tamaño excesivo");
            	    searchStop = true;
            	    return null;
            	}

                
                for (EnumFacing dir : EnumFacing.values()) {
                    BlockPos neighbor = current.offset(dir);

                    if (!visited.contains(neighbor) &&
                        world.getBlockState(neighbor).getBlock() == target) {

                        visited.add(neighbor);
                        queue.add(neighbor);
                    }
                }
            }

            return new ArrayList<>(visited);
        }
        
        
        //Version actualizada a BlockPos
        public static void oceanToStill(World world, List<BlockPos> nodes) {
            for (int i = 0; i < nodes.size(); i++) {
                BlockPos pos = nodes.get(i);
                if (pos == null) continue;

                //  Si hay aire cerca, no lo conviertas aún
                for (EnumFacing face : EnumFacing.values()) {
                    if (world.isAirBlock(pos.offset(face))) {
                        // Delay la conversión
                        world.scheduleBlockUpdate(pos, world.getBlockState(pos).getBlock(), 10, 0);
                        continue;
                    }
                }

                Block block = (i < 15)
                    ? ((NewFluidType) liquids.get(onFiniteFluidIndex)).flowingBlock
                    : ((NewFluidType) liquids.get(onFiniteFluidIndex)).stillBlock;

                int level = 15;
                BlockPos above = pos.up(FiniteFluidLogic.GeneralPurposeLogic.getFluidGravity());

                if (!world.isAirBlock(above)) {
                    level = 15;
                }

                //world.setBlockState(pos, block.getDefaultState().withProperty(BlockFiniteFluid.LEVEL, level), 3);
            	BlockFiniteFluid.setBlockState(world, pos, BlockFiniteFluid.setVolume(block.getDefaultState(), level));
            }
        }

    	
        
        //NO IDEA WHAT IS THIS USED FOR
    	public static void oceanBlock(World world, BlockPos pos) {
    	    Block oceanBlock = ((NewFluidType)liquids.get(onFiniteFluidIndex)).oceanBlock;

    	    if (GeneralPurposeLogic.borderBlock(world, pos, oceanBlock)) {
    	        world.setBlockState(pos, Blocks.ICE.getDefaultState());
    	    }
    	}
    	
    	public static void oceanBlock(byte[] data, int index) {
    	    oceanBlock(data, index, 3);
    	}
        
    	public static void oceanBlock(byte[] data, int index, int depth) {
    	    byte oceanId = (byte) Block.getIdFromBlock(((NewFluidType)liquids.get(onFiniteFluidIndex)).oceanBlock);
    	    byte iceId = (byte) Block.getIdFromBlock(Blocks.ICE);

    	    if ((data[index + 1] == oceanId || (depth != 3 && data[index + 1] == 0))) {
    	        data[index] = iceId;
    	    }

    	    if (index > 0 && (data[index - 1] == oceanId || (depth != 3 && data[index - 1] == 0))) {
    	        data[index] = iceId;
    	        if (depth > 0) oceanBlock(data, index - 1, depth - 1);
    	    }

    	    if (index + 128 < data.length && (data[index + 128] == oceanId || (depth != 3 && data[index + 128] == 0))) {
    	        data[index] = iceId;
    	        if (depth > 0) oceanBlock(data, index + 128, depth - 1);
    	    }

    	    if (index + 2048 < data.length && (data[index + 2048] == oceanId || (depth != 3 && data[index + 2048] == 0))) {
    	        data[index] = iceId;
    	        if (depth > 0) oceanBlock(data, index + 2048, depth - 1);
    	    }

    	    if (index >= 128 && (data[index - 128] == oceanId || (depth != 3 && data[index - 128] == 0))) {
    	        data[index] = iceId;
    	        if (depth > 0) oceanBlock(data, index - 128, depth - 1);
    	    }

    	    if (index >= 2048 && (data[index - 2048] == oceanId || (depth != 3 && data[index - 2048] == 0))) {
    	        data[index] = iceId;
    	        if (depth > 0) oceanBlock(data, index - 2048, depth - 1);
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
	                    //IBlockState stateDown = world.getBlockState(new BlockPos(x, y, z));
	                    IBlockState stateDown = world.getBlockState(new BlockPos(x, y-1, z));

	                    // Evitar cultivos de WEATH TODO implementar cualquier cultivos y mas cosas
	                    NewFluidType fluidType = ((NewFluidType) liquids.get(0));
                        IBlockState water = BlockFiniteFluid.setVolume(fluidType.flowingBlock.getDefaultState(), BlockFiniteFluid.MINIMUM_LEVEL);
                        //fluidType.flowingBlock.getDefaultState().withProperty(BlockFiniteFluid.LEVEL, 0);
	                	if (!(state.getBlock() instanceof BlockCrops) && !(state.getBlock() instanceof BlockFlower) && stateDown.getBlock() != fluidType.oceanBlock  && biome.canRain()) {
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
	        Chunk chunk = world.getChunkFromBlockCoords(new BlockPos(x, 0, z));
	        int y = world.getHeight();  // 256 normalmente

	        x &= 15;
	        z &= 15;

	        for (; y > 0; --y) {
	            IBlockState state = chunk.getBlockState(x, y, z);
	            if (state.getBlock() == Blocks.ICE || getFluidIndex(state.getBlock(), world, new BlockPos(x,y,z)) != -1) {
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
    	
    	//Amt --> Amount
    	//countAdjacentBlocks Cuenta bloques especificos alrededor (6 caras)
    	public static int borderIDAmt(World world, BlockPos pos, Block block)
    	{
    	    int count = 0;

    	    if (world.getBlockState(pos.east()).getBlock() == block) count++;
    	    if (world.getBlockState(pos.west()).getBlock() == block) count++;
    	    if (world.getBlockState(pos.up()).getBlock() == block) count++;
    	    if (world.getBlockState(pos.down()).getBlock() == block) count++;
    	    if (world.getBlockState(pos.north()).getBlock() == block) count++;
    	    if (world.getBlockState(pos.south()).getBlock() == block) count++;

    	    return count;
    	}
    	
    	
    	public static boolean borderBlock(World world, BlockPos pos, Block target)
    	{
    	    return (
    	        world.getBlockState(pos.east()).getBlock() == target ||
    	        world.getBlockState(pos.west()).getBlock() == target ||
    	        world.getBlockState(pos.up()).getBlock() == target ||
    	        world.getBlockState(pos.down()).getBlock() == target ||
    	        world.getBlockState(pos.north()).getBlock() == target ||
    	        world.getBlockState(pos.south()).getBlock() == target
    	    );
    	}
    	
    	
    	/**
    	 * Hay al menos un bloque adyacente (lateral) con cierto bloque que le metemos como 3rd variable
    	 */
    	public static boolean hasSideBorder(World world, BlockPos pos, Block block)
    	{
    	    return world.getBlockState(pos.east()).getBlock() == block ||
    	           world.getBlockState(pos.west()).getBlock() == block ||
    	           world.getBlockState(pos.north()).getBlock() == block ||
    	           world.getBlockState(pos.south()).getBlock() == block;
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


    	
    	public static void setCurrentFluidIndex(Block block, IBlockAccess access, BlockPos pos) {
            onFiniteFluidIndex = getFluidIndex(block, access, pos);
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
        public static int getFluidIndex(Block block, @Nullable IBlockAccess access, @Nullable BlockPos pos) {
        	if (block instanceof BlockFiniteFluid) {
            return blockToFluidIndex.getOrDefault(block, -1);
        } else if (access != null || pos != null) {
        	FluidState fluidState = FluidloggedUtils.getFluidState(access, pos);
        	IBlockState state = fluidState.getState();
        	/*System.out.println("Bloque Original:"+block);
        	System.out.println("Bloque Obtenido:"+state.getBlock());
        	if (access.getBlockState(pos).getBlock() instanceof IFluidBlock) {
            	System.out.println("FluidBlock?:"+fluidState.getFluidBlock());        		
        	}
        	System.out.println(" ");*/
            return blockToFluidIndex.getOrDefault(state.getBlock(), -1);
        	} else {
        		return -1;
        	}
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

        public static int getFluidLevelRender(IBlockAccess world, BlockPos pos, int fluidIndex) {
            IBlockState state = world.getBlockState(pos);
        	if (state.getBlock() instanceof BlockFiniteFluid) {
            if (fluidIndex > -1) {
                int level = BlockFiniteFluid.getVolume(world, pos) *2; //state) * 2;

                if (level > 14) level = 14;

                Block block = state.getBlock();
                if (block == liquids.get(fluidIndex).oceanBlock) {
                    return level;
                }
            }

            //IBlockState state = world.getBlockState(pos);
            return BlockFiniteFluid.getVolume(state);
        	}
			return 15;
        }
        
        public static float getHeight(IBlockAccess access, BlockPos pos, int dx, int dz) {
            int fluidIndex = getFluidIndex(access.getBlockState(pos).getBlock(), access, pos);
            if (fluidIndex == -1) return 0f;
            float total = getFluidLevelRender(access, pos, fluidIndex) + 1.0f;
            int samples = 1;

            BlockPos above = pos.up(getFluidGravity(fluidIndex));
            if (isAnyRealisticFluid(access.getBlockState(above).getBlock(), fluidIndex, access, above)) return 1.0f;
            if (access.getBlockState(pos).getBlock() == liquids.get(fluidIndex).oceanBlock) return 1.0f;

            boolean hasNeighbors = !(access.isAirBlock(pos.east()) &&
            		access.isAirBlock(pos.west()) &&
            		access.isAirBlock(pos.north()) &&
            		access.isAirBlock(pos.south()));

            if (!hasNeighbors) return total / 16.0f;

            BlockPos p1 = pos.add(dx, 0, 0);
            BlockPos p2 = pos.add(0, 0, dz);
            BlockPos p3 = pos.add(dx, 0, dz);

            // === Primer vecino (p1) ===
            if (access.getBlockState(p1).getBlock() == liquids.get(fluidIndex).oceanBlock) return 1.0f;
            if (isAnyRealisticFluid(access, p1, fluidIndex)) {
                if (isLDWater(access, p3, fluidIndex)) return 0.0f;
                total += getFluidLevelRender(access, p1, fluidIndex) + 1.0f;
                samples++;
            }
            if (isFullWaterRender(access, p1.up(getFluidGravity(fluidIndex)), fluidIndex)) return 1.0f;
            if (isLDWater(access, p1, fluidIndex)) return 0.0f;

            // === Segundo vecino (p3) ===
            if (access.getBlockState(p3).getBlock() == liquids.get(fluidIndex).oceanBlock) return 1.0f;
            if (isAnyRealisticFluid(access, p3, fluidIndex)) {
                total += getFluidLevelRender(access, p3, fluidIndex) + 1.0f;
                samples++;
            }
            if (isFullWaterRender(access, p3.up(getFluidGravity(fluidIndex)), fluidIndex)) return 1.0f;

            // === Tercer vecino (p2) ===
            if (access.getBlockState(p2).getBlock() == liquids.get(fluidIndex).oceanBlock) return 1.0f;
            if (isAnyRealisticFluid(access, p2, fluidIndex)) {
                if (isLDWater(access, p3, fluidIndex)) return 0.0f;
                total += getFluidLevelRender(access, p2, fluidIndex) + 1.0f;
                samples++;
            }
            if (isFullWaterRender(access, p2.up(getFluidGravity(fluidIndex)), fluidIndex)) return 1.0f;
            if (isLDWater(access, p2, fluidIndex)) return 0.0f;

            return total / 16.0f / samples;
        }
        
        
        public static void flipLiquids(World world, BlockPos pos) {
            flipLiquids(world, pos, pos.down());
        }
        
        public static void flipLiquids(World world, BlockPos pos1, BlockPos pos2) {
            IBlockState state1 = world.getBlockState(pos1);
            IBlockState state2 = world.getBlockState(pos2);
            Block block1 = state1.getBlock();
            Block block2 = state2.getBlock();

            int type1 = FiniteFluidLogic.GeneralPurposeLogic.getFluidIndex(block1, world, pos1);
            int type2 = FiniteFluidLogic.GeneralPurposeLogic.getFluidIndex(block2, world, pos2);

            if (type1 != -1 && type2 != -1) {
                Block newBlock1 = ((NewFluidType)liquids.get(type2)).flowingBlock;
                Block newBlock2 = ((NewFluidType)liquids.get(type1)).flowingBlock; 
                //int meta1 = block1.getMetaFromState(state1);
                //int meta2 = block2.getMetaFromState(state2);
                int meta1 = BlockFiniteFluid.getVolume(world, pos1); //state1);
                int meta2 = BlockFiniteFluid.getVolume(world, pos2); //state2);

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
            return block == type.flowingBlock || block == type.stillBlock || block == type.oceanBlock;
        }

        public static boolean isAWater(World world, BlockPos pos) {
            Block block = world.getBlockState(pos).getBlock();
            NewFluidType type = (NewFluidType) liquids.get(onFiniteFluidIndex);
            return block == type.oceanBlock || isRealisticFluid(block);
        }


        public static boolean isRealisticFluidNoOcean(Block block) {
            NewFluidType type = (NewFluidType) liquids.get(onFiniteFluidIndex);
            return block != type.oceanBlock && isRealisticFluid(block);
        }


        public static boolean isAnyRealisticFluid(IBlockAccess world, BlockPos pos, int indexToCompare)
        {
            return isAnyRealisticFluid(world.getBlockState(pos).getBlock(), indexToCompare, world, pos);
        }
        
        /**
         * Entonces la funcion isAnyRealisticFluid en realidad tiene los siguientes argumentos:
El primero es un bloque cualquiera, el segundo es un index de la lista de Liquids, 

y la funcion getWaterType (dentro dde isAnyWater) corroborra primero que:
 el bloque cualquiera este dentro de la lista de Liquids sea el bloque que sea (still, flow, ocean) regresandote su index en la lista Liquids, 
 y la funcion isAnyWater se encarga de comparar ambas Index para ver si son el mismo fluido?
 
 
         * @param compare1 Bloque a obtener index de lista Liquids
         * @param compare2 Index de lista Liquids a comparar con la del bloque
         * @return
         */

        public static boolean isAnyRealisticFluid(Block compare1, int indexToCompare, IBlockAccess access, BlockPos pos)
        {
            return getFluidIndex(compare1, access, pos) == indexToCompare;
        }
        
        public static boolean isOtherRealisticFluid(Block compare1, int indexToCompare, IBlockAccess access, BlockPos pos)
        {
        	int indexToCompare1 = getFluidIndex(compare1, access, pos);
            return indexToCompare1 != -1 && indexToCompare1 != indexToCompare;
        }

        
        public static boolean isFullWater(IBlockAccess world, BlockPos pos) {
            Block block = world.getBlockState(pos).getBlock();
            return isFullWater(block);
        }

        
        public static boolean isFullWaterRender(IBlockAccess world, BlockPos pos, int fluidIndex) {
            Block block = world.getBlockState(pos).getBlock();
            return isFullWaterRender(block, fluidIndex);
        }

        public static boolean isFlowingRealisticFluid(Block block)
        {
            return block == ((NewFluidType)liquids.get(onFiniteFluidIndex)).flowingBlock;
        }

        public static boolean isFullWater(Block block)
        {
            return block == ((NewFluidType)liquids.get(onFiniteFluidIndex)).flowingBlock ? true : block == ((NewFluidType)liquids.get(onFiniteFluidIndex)).oceanBlock;
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
        }
        

        
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
                        int level = BlockFiniteFluid.getVolume(world, checkPos);//state);
                        if (level > 0 || block == fluid.oceanBlock) {
                            found = true;
                        	//System.out.println("VERGA TRYTOSAVE_1");

                            if (block != fluid.oceanBlock) {
                                //world.setBlockState(checkPos, fluid.flowingBlock.getDefaultState().withProperty(BlockFiniteFluid.LEVEL, level - 1), 3);
                            	BlockFiniteFluid.setBlockState(world, checkPos, BlockFiniteFluid.setConceptualVolume(fluid.flowingBlock.getDefaultState(), level));
                            }
                            //world.setBlockState(pos, fluid.flowingBlock.getDefaultState().withProperty(BlockFiniteFluid.LEVEL, 0), 3);
                        	BlockFiniteFluid.setBlockState(world, pos, BlockFiniteFluid.setVolume(fluid.flowingBlock.getDefaultState(), BlockFiniteFluid.MINIMUM_LEVEL));
                            break;
                        }
                    }
                } else { // Lados con offset vertical = gravity (abajo o arriba segÃºn gravedad)
                	/*EnumFacing[] order = new EnumFacing[] {
                		    EnumFacing.WEST,
                		    EnumFacing.EAST,
                		    EnumFacing.NORTH,
                		    EnumFacing.SOUTH
                		};

                		for (EnumFacing dir : order) {
                		    checkPos = pos.offset(dir).add(0, yOffset, 0);
                		    IBlockState state = world.getBlockState(checkPos);
                		    Block block = state.getBlock();
                		    if (fluid.isFluid(block)) {
                		        int level = state.getValue(BlockFiniteFluid.LEVEL);
                		        if (level > 0 || block == fluid.oceanBlock) {
                		            found = true;
                		            if (block != fluid.oceanBlock) {
                		                world.setBlockState(checkPos, fluid.flowingBlock.getDefaultState().withProperty(BlockFiniteFluid.LEVEL, level - 1), 3);
                		            }
                		            world.setBlockState(pos, fluid.flowingBlock.getDefaultState().withProperty(BlockFiniteFluid.LEVEL, 0), 3);
                		            break;
                		        }
                		    }
                		}*/
                	for (EnumFacing dir : EnumFacing.HORIZONTALS) {
                        checkPos = pos.offset(dir).add(0, yOffset, 0);
                        IBlockState state = world.getBlockState(checkPos);
                        Block block = state.getBlock();
                        if (fluid.isFluid(block)) {
                            int level = BlockFiniteFluid.getVolume(world, checkPos); //state);
                            if (level > 0 || block == fluid.oceanBlock) {
                                found = true;
                            	//System.out.println("VERGA TRYTOSAVE_2");
                                if (block != fluid.oceanBlock) {
                                    //world.setBlockState(checkPos, fluid.flowingBlock.getDefaultState().withProperty(BlockFiniteFluid.LEVEL, level - 1), 3);
                                	BlockFiniteFluid.setBlockState(world, checkPos, BlockFiniteFluid.setConceptualVolume(fluid.flowingBlock.getDefaultState(), level));
                                }
                                //world.setBlockState(pos, fluid.flowingBlock.getDefaultState().withProperty(BlockFiniteFluid.LEVEL, 0), 3);
                            	BlockFiniteFluid.setBlockState(world, pos, BlockFiniteFluid.setVolume(fluid.flowingBlock.getDefaultState(), BlockFiniteFluid.MINIMUM_LEVEL));
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
                int level = BlockFiniteFluid.getVolume(world, foundPos); ////state);

                // Coloca el nuevo bloque con el mismo nivel que el original
                //world.setBlockState(pos, fluid.flowingBlock.getDefaultState().withProperty(BlockFiniteFluid.LEVEL, level), 3);
            	BlockFiniteFluid.setBlockState(world, pos, BlockFiniteFluid.setVolume(fluid.flowingBlock.getDefaultState(), level));

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
            int totalLevel = BlockFiniteFluid.getConceptualVolume(world, center); //world.getBlockState(center).getValue(BlockFiniteFluid.LEVEL) + 1;
            int count = 1;

            boolean hasWest = false;
            boolean hasEast = false;
            boolean hasNorth = false;
            boolean hasSouth = false;

            // Cardinales
            if (!center.west().equals(exclude) && isRealisticFluid(world, center.west())) {
                totalLevel += BlockFiniteFluid.getConceptualVolume(world, center.west()); //world.getBlockState(center.west()).getValue(BlockFiniteFluid.LEVEL) + 1;
                count++;
                hasWest = true;
            }

            if (!center.east().equals(exclude) && isRealisticFluid(world, center.east())) {
                totalLevel += BlockFiniteFluid.getConceptualVolume(world, center.east()); //world.getBlockState(center.east()).getValue(BlockFiniteFluid.LEVEL) + 1;
                count++;
                hasEast = true;
            }

            if (!center.north().equals(exclude) && isRealisticFluid(world, center.north())) {
                totalLevel += BlockFiniteFluid.getConceptualVolume(world, center.north()); //world.getBlockState(center.north()).getValue(BlockFiniteFluid.LEVEL) + 1;
                count++;
                hasNorth = true;
            }

            if (!center.south().equals(exclude) && isRealisticFluid(world, center.south())) {
                totalLevel += BlockFiniteFluid.getConceptualVolume(world, center.south()); //world.getBlockState(center.south()).getValue(BlockFiniteFluid.LEVEL) + 1;
                count++;
                hasSouth = true;
            }

            // Diagonales (solo si al menos uno de los lados existe)
            if ((hasEast || hasSouth)) {
                BlockPos diag = center.east().south();
                if (!diag.equals(exclude) && isRealisticFluid(world, diag)) {
                    totalLevel += BlockFiniteFluid.getConceptualVolume(world, diag); //world.getBlockState(diag).getValue(BlockFiniteFluid.LEVEL) + 1;
                    count++;
                }
            }

            if ((hasWest || hasNorth)) {
                BlockPos diag = center.west().north();
                if (!diag.equals(exclude) && isRealisticFluid(world, diag)) {
                    totalLevel += BlockFiniteFluid.getConceptualVolume(world, diag); //world.getBlockState(diag).getValue(BlockFiniteFluid.LEVEL) + 1;
                    count++;
                }
            }

            if ((hasWest || hasSouth)) {
                BlockPos diag = center.west().south();
                if (!diag.equals(exclude) && isRealisticFluid(world, diag)) {
                    totalLevel += BlockFiniteFluid.getConceptualVolume(world, diag); //world.getBlockState(diag).getValue(BlockFiniteFluid.LEVEL) + 1;
                    count++;
                }
            }

            if ((hasEast || hasNorth)) {
                BlockPos diag = center.east().north();
                if (!diag.equals(exclude) && isRealisticFluid(world, diag)) {
                    totalLevel += BlockFiniteFluid.getConceptualVolume(world, diag); //world.getBlockState(diag).getValue(BlockFiniteFluid.LEVEL) + 1;
                    count++;
                }
            }

            return (float) totalLevel / count;
        }
        
        public static boolean liquidMove(World world, BlockPos from, BlockPos to, boolean doMove) {
            return FiniteFluidLogic.GeneralPurposeLogic.lMove(world, from, to, doMove, 0);
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
        	if (isOtherRealisticFluid(block, getFluidIndex(world.getBlockState(fromPos).getBlock(), world, fromPos), world, fromPos)) {
        		return false;
        	}
        	
        	//checks para bloques vanilla que no deberian ser rotos, por ser bloques incompletos
            if (block instanceof BlockPistonBase || block instanceof BlockPistonExtension || block instanceof BlockFarmland || block instanceof BlockGrassPath || block instanceof BlockIce) {
                return false;
            }
        	
            // Si es reemplazable (fuego, flores, nieve, etc.)
            if (block.isReplaceable(world, toPos)) {
                world.destroyBlock(toPos, true);
            	//System.out.println("REPLACE"+block);

                return true;
            }

            // Si el nivel de agua es mayor a 7 y el bloque no es completo (ej: flores, placas, etc.) ELIMINADO EN FAVOR DE FLUIDLOGGED API
            /*if (currentLevel > 7 && !state.isFullBlock() && !state.getBlock().hasTileEntity()) {
                world.destroyBlock(toPos, true);
                return true;
            }*/
            

            return false;
        }
        
        public static boolean lMove(World world, BlockPos from, BlockPos to, boolean doMove, int recursionDepth) { 
            if (world.isRemote) return false;
            
            IBlockState fromState = world.getBlockState(from);
            Block fromBlock = fromState.getBlock();
            //FluidLogged API Compat
            if (!(fromBlock instanceof BlockFiniteFluid)) {
            	fromState = FluidloggedUtils.getFluidState(world, from).getState();
            	fromBlock = FluidloggedUtils.getFluidState(world, from).getBlock();
            }
            //////
            if (!(fromBlock instanceof BlockFiniteFluid)) return false; //CHECAR QUE ESTO NO ROMPA EL FUNCIONAMIENTO DEL AGUA
            int sourceLevel = BlockFiniteFluid.getVolume(fromState);

            IBlockState toState = world.getBlockState(to);
            Block toBlock = toState.getBlock();
            //FluidLogged API Compat
            if (!(toBlock instanceof BlockFiniteFluid)) {
            	toState = FluidloggedUtils.getFluidState(world, to).getState();
            	toBlock = FluidloggedUtils.getFluidState(world, to).getBlock();
            }
            //////
            
            //if (!(toBlock instanceof BlockFiniteFluid)) return false;
            int destLevel = toBlock instanceof BlockFiniteFluid ? BlockFiniteFluid.getVolume(toState) : -1;

            setCurrentFluidIndex(fromBlock, world, from);
            NewFluidType fluid = (NewFluidType) liquids.get(onFiniteFluidIndex);

            boolean shouldSearchOutward = false;
            if (fluid.flowingBlock instanceof BlockFiniteFluid) {
                shouldSearchOutward = ((BlockFiniteFluid) fluid.flowingBlock).shouldSearchOutward();
            }


            //Calculos horizontales
            if (shouldSearchOutward && recursionDepth < 32) {
            	/*
            	// Orden fijo: West (-X), East (+X), North (-Z), South (+Z)
            	// Esto asegura que lo "distal" (ej. West si proximal es +X) se procesa primero
            	EnumFacing[] order = new EnumFacing[] {
            	    EnumFacing.WEST,
            	    EnumFacing.EAST,
            	    EnumFacing.NORTH,
            	    EnumFacing.SOUTH
            	};

            	for (EnumFacing dir : order) {
            	    BlockPos neighbor = to.offset(dir);
            	    if (!neighbor.equals(from) && isRealisticFluid(world, neighbor)) {
            	        int neighborLevel = world.getBlockState(neighbor).getValue(BlockFiniteFluid.LEVEL);
            	        if (destLevel > neighborLevel) {
            	            return lMove(world, from, neighbor, doMove, recursionDepth + 1);
            	        }
            	    }
            	}*/
                for (EnumFacing dir : EnumFacing.HORIZONTALS) {
                    BlockPos neighbor = to.offset(dir);
                    if (!neighbor.equals(from) && isRealisticFluid(world, neighbor)) {
                        int neighborLevel = BlockFiniteFluid.getVolume(world.getBlockState(neighbor));
                        if (destLevel > neighborLevel) {
                            return lMove(world, from, neighbor, doMove, recursionDepth + 1);
                        }
                    }
                }

            } else if (!isRealisticFluid(toBlock)) {

                destLevel = -1;
            }
            
            //Reducir deuda de calculos
            int dy = from.getY() - to.getY(); //Es la diferencia entre la altura de neustro bloque contra el que estamos comparando
            //Idealmente tenddria que salir un resultado positivo
            if (doMove && dy == 0 && Math.abs(destLevel - sourceLevel) < 3 && getCalc() > maxCalc * 0.6f) {
                --calcAmt;
                return true;
            }

            
            //Calculos verticales 
            if (dy == getFluidGravity()) {
                if (FiniteFluidLogic.GeneralPurposeLogic.canMoveInto(world, to, from, sourceLevel, fluid)) {
                    if (doMove) {
                    	//System.out.println("CanMoveInto");
                        //world.setBlockState(to, fluid.flowingBlock.getDefaultState().withProperty(BlockFiniteFluid.LEVEL, sourceLevel), 3);
                        BlockFiniteFluid.setBlockState(world, to, BlockFiniteFluid.setVolume(fluid.flowingBlock.getDefaultState(), sourceLevel));
                    	FiniteFluidLogic.GeneralPurposeLogic.tryGrab(world, from, to, 0, fluid);
                        //world.setBlockState(from, Blocks.AIR.getDefaultState());
                    }
                    return true;
                } 
                
                //Si el destino tiene agua y tiene espacio para niveles de agua
                //Ecualizacion Vertical
                if (isRealisticFluid(toBlock) && destLevel < 15) {
                	//System.out.println("destLevel menor a 15");
                    if (doMove) {
                    	//ECUALIZACION
                    		int realSource = sourceLevel + 1;
                        	int realDest   = destLevel + 1;
                        	int transfer = Math.min(15 - destLevel, realSource);

                        	realSource -= transfer;
                        	realDest   += transfer;

                        	sourceLevel = realSource - 1;
                        	destLevel   = realDest   - 1;

                        if (sourceLevel > -1) //SI COMPARAS CONTRA 0, LOS BLOQUES CON VALOR 0 SE VAN AL CARAJO, DEBE SER CONTRA -1
                            //world.setBlockState(from, fluid.flowingBlock.getDefaultState().withProperty(BlockFiniteFluid.LEVEL, sourceLevel), 3);
                        	BlockFiniteFluid.setBlockState(world, from, BlockFiniteFluid.setVolume(fluid.flowingBlock.getDefaultState(), sourceLevel));
                        else
                            FiniteFluidLogic.GeneralPurposeLogic.tryGrab(world, from, to, 0, fluid);

                        //world.setBlockState(to, fluid.flowingBlock.getDefaultState().withProperty(BlockFiniteFluid.LEVEL, destLevel), 3);
                        BlockFiniteFluid.setBlockState(world, to, BlockFiniteFluid.setVolume(fluid.flowingBlock.getDefaultState(), destLevel));

                        
                        if (doPressure && destLevel == 15)
                        	FiniteFluidLogic.GeneralPurposeLogic.addToPressure(world, to, false); // 0 = false
                    }
                    return true;
                }
                return false;
            }
        	
            //Ecualizacion horizontal
            if (isRealisticFluid(toBlock)) {
                // Ecualizacion normal si son del mismo material
                if (calcAvg(world, from, to) && destLevel < BlockFiniteFluid.MAXIMUM_LEVEL && sourceLevel > BlockFiniteFluid.MINIMUM_LEVEL) {
                    if (doMove) {
                        int total = sourceLevel + destLevel + 2; //Convertido a LEVELs conceptuales
                        sourceLevel = total / 2;
                        destLevel = total - sourceLevel - 1;
                        --sourceLevel;
                        
                        if (sourceLevel > -1)
                        	//world.setBlockState(from, fluid.flowingBlock.getDefaultState().withProperty(BlockFiniteFluid.LEVEL, sourceLevel), 3);
                        	BlockFiniteFluid.setBlockState(world, from, BlockFiniteFluid.setVolume(fluid.flowingBlock.getDefaultState(), sourceLevel));
                         else 
                        	FiniteFluidLogic.GeneralPurposeLogic.tryGrab(world, from, to, 0, fluid);


                        //world.setBlockState(to, fluid.flowingBlock.getDefaultState().withProperty(BlockFiniteFluid.LEVEL, destLevel), 3);
                        BlockFiniteFluid.setBlockState(world, to, BlockFiniteFluid.setVolume(fluid.flowingBlock.getDefaultState(), destLevel));

                        if (doPressure && destLevel == BlockFiniteFluid.MAXIMUM_LEVEL)
                            addToPressure(world, to, false); // 0 = false
                    }
                    return true;
                }
                
                
                
                //Division de nuestra agua en otros bloques, horizontalmente hablando
            } else if (FiniteFluidLogic.GeneralPurposeLogic.canMoveInto(world, to, from, sourceLevel, fluid) && sourceLevel > BlockFiniteFluid.MINIMUM_LEVEL) {
                if (doMove) {
                	//FiniteFluidLogic.calcAmt = 0;
                    --sourceLevel;
                    //world.setBlockState(from, fluid.flowingBlock.getDefaultState().withProperty(BlockFiniteFluid.LEVEL, sourceLevel), 3);
                    //world.setBlockState(to, fluid.flowingBlock.getDefaultState().withProperty(BlockFiniteFluid.LEVEL, 0), 3);
                    BlockFiniteFluid.setBlockState(world, from, BlockFiniteFluid.setVolume(fluid.flowingBlock.getDefaultState(), sourceLevel));
                    BlockFiniteFluid.setBlockState(world, to, BlockFiniteFluid.setVolume(fluid.flowingBlock.getDefaultState(), BlockFiniteFluid.MINIMUM_LEVEL));
                }
                return true;
            }
            return false;
        }

        

        
        public static boolean lMoveROTA(World world, BlockPos from, BlockPos to, boolean doMove, int recursionDepth) { 
            if (world.isRemote) return false;
            
            IBlockState fromState = world.getBlockState(from);
            Block fromBlock = fromState.getBlock();
            int sourceLevel = fromState.getValue(BlockFiniteFluid.LEVEL);

            IBlockState toState = world.getBlockState(to);
            Block toBlock = toState.getBlock();
            int destLevel = toBlock instanceof BlockFiniteFluid ? toState.getValue(BlockFiniteFluid.LEVEL) : -1;

            setCurrentFluidIndex(fromBlock, world, from);
            NewFluidType fluid = (NewFluidType) liquids.get(onFiniteFluidIndex);

            boolean shouldSearchOutward = false;
            if (fluid.flowingBlock instanceof BlockFiniteFluid) {
                shouldSearchOutward = ((BlockFiniteFluid) fluid.flowingBlock).shouldSearchOutward();
            }


            //Calculos horizontales
            if (shouldSearchOutward && recursionDepth < 32) {
                for (EnumFacing dir : EnumFacing.HORIZONTALS) {
                    BlockPos neighbor = to.offset(dir);
                    if (!neighbor.equals(from) && isRealisticFluid(world, neighbor)) {
                        int neighborLevel = world.getBlockState(neighbor).getValue(BlockFiniteFluid.LEVEL);
                        if (destLevel > neighborLevel) {
                            return lMove(world, from, neighbor, doMove, recursionDepth + 1);
                        }
                    }
                }

            } else if (!isRealisticFluid(toBlock)) {

                destLevel = -1;
            }
            
            //Reducir deuda de calculos
            int dy = from.getY() - to.getY(); //Es la diferencia entre la altura de neustro bloque contra el que estamos comparando
            //Idealmente tenddria que salir un resultado positivo
            if (doMove && dy == 0 && Math.abs(destLevel - sourceLevel) < 3 && getCalc() > maxCalc * 0.6f) {
                --calcAmt;
                return true;
            }
            //System.out.println("VERGa " + (dy == grav()));

            
            //Calculos verticales 
            if (dy == getFluidGravity()) {
                // Moving into tile or flowing block
                if (!world.isAirBlock(to) && !isRealisticFluid(toBlock) && !from.equals(to)) {
                	//System.out.println("NelCarnal");

                    return false;
                }
                

                if (FiniteFluidLogic.GeneralPurposeLogic.canMoveInto(world, to, from, sourceLevel, fluid)) {
                    if (doMove) {
                    	//System.out.println("CanMoveInto");
                        world.setBlockState(to, fluid.flowingBlock.getDefaultState().withProperty(BlockFiniteFluid.LEVEL, sourceLevel), 3);
                        FiniteFluidLogic.GeneralPurposeLogic.tryGrab(world, from, to, 0, fluid);
                    }
                    return true;
                } 
                
                //Si el destino tiene agua y tiene espacio para niveles de agua
                //Ecualizacion Vertical
                if (isRealisticFluid(toBlock) && destLevel < 15) {
                	//System.out.println("destLevel menor a 15");
                    if (doMove) {
                        
                    	{//Tiene sobreproduccion de agua, codigo "original"
                    		/*int transfer = Math.min(15 - destLevel, sourceLevel + 1);
                        	sourceLevel -= transfer -1;
                        	destLevel += transfer ; //-1 arreglaria el overflow YO NEl, ya vi que pierdo agua*/
                    	}
                        
                    	{ //Tiene perdida de agua --MAS OPTIMO
                    		//ARREGLE LA PERDIDA DE AGUA, TAN SIMPLE COMO NO METER BLOQUES DE AGUA CON LEVEL 0 DENTRO DE TRYGRAB
                    		/**
                    		 * Ok, toca explicacion.
                    		 * Los LEVELs (con los que mis bloques almacenan la cantidad de liquido que tienen actualmente) trabajan 
                    		 * en un sistema literal de valor 0-15, pero en esta escala, tenemos 16 posibles valores (yo les llame 
                    		 * "LEVEls conceptuales", que va del 1 al 16), los sistemas de ecaulizacion consideran actualmente la perdida
                    		 * de LEVELs al hacer calculos con este sistema cuando manejas el LEVEl concetual 1, el LEVEL literal 0.
                    		 * 
                    		 * Los calculos estan perfectos, pero cuando intento interactuar con este sistema (usando cubetas o bombas
                    		 * de agua de otros mods) pues ellos consideran que el LEVEL 0 equivale a LEVEL conceotual 0 cuando no es asi.
                    		 * 
                    		 * Si una cubeta espera encotnrar LEVEL 15, pero lee que 1 bloque de LEVEL 15 (16) se ddividido en 2 de 7 (8)
                    		 * pues va a ver que 7+7=14, no es 15, vale madre. Pero en realidadd es 8+8 conceptaules, dando 16, el valor
                    		 * maximo de LEVEL conceptual, siendo un bloque completo
                    		 * 
                    		 * LA SOLICION A ESTAS INTERACCIONES --> Usar LEVEL conceptual al hacer los calculos, (cuando obtengas la
                    		 * propiedadd LEVEL de mis bloques [RFFBlock.LEVEL], debes sumar 1 a tu int obtenido, para manejar LEVELs
                    		 * conceptuales, PERO CUANDO ACABES DED MANEJAR LEVELS CONCEPTUALES, DEBES RESTAR 1 A LA HORA DE HACER
                    		 * world.setBlockState(pos, fluid.withProperty(RRFBlock.LEVEL), level**-1**), asi traduces bien entre
                    		 * LEVELs literales y LEVELs conceptauels, haces bien tus cauclos sin hacer desmadre.)
                    		 * 
                    		 * Un ejemplo mas --> Un bloque de 16 que se divide en 3 de 4-4-5 (LEVEL literal suma 13, aparente perdida de 2 LEVELs, 
                    		 * "1 por cada bloque ecualizado", pero en realidad es 5-5-6 (LEVEL conceptual de 16, el maximo, bloque completo))
                    		 * 
                    		 * 
                    		 * 
                    		 * 
                    		 * 
                    		 * CONCLUSION --> La logica interna esta bien (almenos la ecualizacion latereal, porque creo que aun habrian perdias en
                    		 * ecualizacion vertical, o alguna combinacion de estas 2 xd), pero al momento de interactuar con LEVELs (al momento de
                    		 * leerlos), debes sumar 1 para usar LEVELs conceptuales, y restar 1 al momento de querer actualizar un BlockState
                    		 */
                    		int realSource = sourceLevel + 1;
                        	int realDest   = destLevel + 1;
                        	int transfer = Math.min(15 - destLevel, realSource);

                        	realSource -= transfer;
                        	realDest   += transfer;

                        	sourceLevel = realSource - 1;
                        	destLevel   = realDest   - 1;
                    	}
                    	
                    	{ //Rompe el pedo xd
                    		/*int total = sourceLevel + destLevel + 2;
                    		int newSource = total / 2;
                    		int newDest   = total - newSource;

                    		sourceLevel = newSource - 1;
                    		destLevel   = newDest - 1;*/
                    	}
                    	
                    	{ //Implementacion ALFA comprendiendo que 0 no se debe usar literal en las maths, sino como un 1 "conceptual"
                    	  // en mi sistema de LEVEL -- CREO QUE IGUAL SIRVE PARA NADA Y TIENE PERDIDA DE AGUA :'v
                    		/*int realSource = sourceLevel + 1;
                    		int realDest = destLevel + 1;
                    		int total = realSource + realDest;

                    		// Repartimos total entre ambos
                    		int newSource = total / 2;
                    		int newDest = total - newSource;

                    		// Convertimos de vuelta al rango 0–15
                    		sourceLevel = newSource - 1;
                    		destLevel = newDest - 1;
                    		*/
                    	}
                    	
                    	
                    	{//IMPLEMENTACION ALFA-3 (usando Floats) VALE MADRE
                    		/*int realSource = sourceLevel + 1;
                    		int realDest = destLevel + 1;
                    		int total = realSource + realDest;

                    		float half = total / 2.0f;
                    		int newSource = (int)Math.floor(half);
                    		int newDest = total - newSource; // Garantiza que sumen igual al total

                    		// Ajuste para evitar sesgo
                    		if (total % 2 != 0) {
                    		    if (destLevel < sourceLevel) {
                    		        newDest++;
                    		        newSource--;
                    		    }
                    		}

                    		sourceLevel = newSource - 1;
                    		destLevel = newDest - 1;*/
                    	}
                    	

                    	{ //CODIGO ORIGINAL
                    		if (sourceLevel > 0) {
                            	world.setBlockState(from, fluid.flowingBlock.getDefaultState().withProperty(BlockFiniteFluid.LEVEL, sourceLevel), 3);
                        	} else {
                            	FiniteFluidLogic.GeneralPurposeLogic.tryGrab(world, from, to, 0, fluid);
                        		world.setBlockState(to, fluid.flowingBlock.getDefaultState().withProperty(BlockFiniteFluid.LEVEL, destLevel), 3);
                        	}
                    	}
                    	
                    	{ //IMPLEMENTACION ALFA // VALE BURGER
                    		/*world.setBlockState(to, fluid.flowingBlock.getDefaultState().withProperty(BlockFiniteFluid.LEVEL, destLevel), 3);

                    		if (sourceLevel > 0) {
                    		    world.setBlockState(from, fluid.flowingBlock.getDefaultState().withProperty(BlockFiniteFluid.LEVEL, sourceLevel), 3);
                    		} else {
                    		    // Solo eliminas si no queda nada
                    		    world.setBlockToAir(from);
                    		}*/
                    	}
                    	
                    	/*{ //IMPLEMENTACION ALFA-2 igual vale verga
                            int realSource = sourceLevel + 1;
                            int realDest = destLevel + 1;
                            int total = realSource + realDest;

                            int newSource = total / 2;
                            int newDest = total - newSource;

                            sourceLevel = newSource - 1;
                            destLevel = newDest - 1;

                            world.setBlockState(to, fluid.flowingBlock.getDefaultState().withProperty(BlockFiniteFluid.LEVEL, destLevel), 3);

                            if (sourceLevel > 0) {
                                world.setBlockState(from, fluid.flowingBlock.getDefaultState().withProperty(BlockFiniteFluid.LEVEL, sourceLevel), 3);
                            } else {
                                world.setBlockToAir(from);
                            }
                    	}*/
                        
                        if (doPressure && destLevel == 15)
                        	FiniteFluidLogic.GeneralPurposeLogic.addToPressure(world, to, false); // 0 = false
                    }
                    return true;
                }
                return false;
            }
            
        	//System.out.println("FROM: "+fromBlock);
        	//System.out.println("TO: "+toBlock);
        	//System.out.println("       ");
        	
            //Ecualizacion horizontal
            if (/*isAnyRealisticFluid(toBlock, FiniteFluidLogic.GeneralPurposeLogic.getWaterType(fromBlock))) {*/ isRealisticFluid(toBlock)) {

            	
            	//if (FiniteFluidLogic.GeneralPurposeLogic.isAnyRealisticFluid(toBlock, FiniteFluidLogic.GeneralPurposeLogic.getWaterType(fromBlock))) {

            	//}
            	
                // Ecualización normal si son del mismo material
                if (calcAvg(world, from, to) && destLevel < 15 && sourceLevel > 0) {
                    if (doMove) {
                        int total = sourceLevel + destLevel + 2;
                        sourceLevel = total / 2;
                        destLevel = total - sourceLevel - 1;
                        --sourceLevel;

                        if (sourceLevel >= 0) //> -1, en teoria son equivalentes
                            world.setBlockState(from, fluid.flowingBlock.getDefaultState().withProperty(BlockFiniteFluid.LEVEL, sourceLevel), 3);
                        else
                        	FiniteFluidLogic.GeneralPurposeLogic.tryGrab(world, from, to, 0, fluid);
                        	world.setBlockState(to, fluid.flowingBlock.getDefaultState().withProperty(BlockFiniteFluid.LEVEL, destLevel), 3);

                        if (doPressure && destLevel == 15)
                            addToPressure(world, to, false); // 0 = false
                    }
                    return true;
                }
                
                
                
                //Division de nuestra agua en otros bloques, horizontalmente hablando
            } else if (FiniteFluidLogic.GeneralPurposeLogic.canMoveInto(world, to, from, sourceLevel, fluid) && sourceLevel > 0) {
            	//System.out.println("FROM: "+fromBlock);
            	//System.out.println("TO: "+toBlock);
            	////System.out.println("       ");
            	
            	//Interaccion entre distintos liquidos            	
                /*boolean bothFiniteFluids = fromBlock instanceof BlockFiniteFluid && toBlock instanceof BlockFiniteFluid;
                Material fromMat = fromState.getMaterial();
                Material toMat = toState.getMaterial();

                if (bothFiniteFluids && fromMat != toMat) {
                    if (doMove) {
                        FiniteFluidLogic.GeneralPurposeLogic.interactWithLiquidHorizontally(world, from, to, sourceLevel, destLevel);
                    }
                    return false;
                }*/
            	//System.out.println("TO2: "+toBlock);
            	//System.out.println("       ");

            	
                if (doMove) {

                	
                	
                	//FiniteFluidLogic.calcAmt = 0;
                	
                    --sourceLevel;
                    world.setBlockState(from, fluid.flowingBlock.getDefaultState().withProperty(BlockFiniteFluid.LEVEL, sourceLevel), 3);
                    world.setBlockState(to, fluid.flowingBlock.getDefaultState().withProperty(BlockFiniteFluid.LEVEL, 0), 3);
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
                if (lMove(world, pos, target, false, 0)) return true;
                
                //No recuerdo porque añadi esto :'v
                if (level > 1) {
                	lMove(world, pos, target, true, 0);
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
        
        
        public static int solidSides(World world, BlockPos pos) {
            int count = 0;

            for (EnumFacing face : EnumFacing.values()) {
                BlockPos neighbor = pos.offset(face);
                IBlockState state = world.getBlockState(neighbor);
                Block block = state.getBlock();

                if (!isAWater(world, neighbor) && block != Blocks.AIR) {
                    count++;
                }
            }

            return count;
        }
        
        public static int waterSides(World world, BlockPos pos) {
            int count = 0;

            for (EnumFacing face : EnumFacing.HORIZONTALS) {
                BlockPos neighbor = pos.offset(face);
                IBlockState state = world.getBlockState(neighbor);
                Block block = state.getBlock();

                if (isAWater(world, neighbor) && block != Blocks.AIR) {
                    count++;
                }
            }

            return count;
        }
        
        public static int getFluidLevel(IBlockAccess world, BlockPos pos) { 
        	return BlockFiniteFluid.getVolume(world, pos); //world.getBlockState(pos)); 
        }
        
        
     // shouldPressure: (meta >= 13 y hay agua en pos.down(grav()))
        public static boolean shouldPressure(World world, BlockPos pos) {
            if (!isAWater(world, pos)) {
                //System.out.println("[DEBUG] shouldPressure FALSE (no es agua) pos=" + pos);
                return false;
            }
            IBlockState s = world.getBlockState(pos);
            int level = BlockFiniteFluid.getVolume(world, pos); //s);
            boolean result = level >= 13 && isAWater(world, pos.down(getFluidGravity()));
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
                metaAtOneBelow = BlockFiniteFluid.getVolume(world, oneBelow); //st);
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

            int metaCurrent = 0;
            IBlockState curState = world.getBlockState(current);
            if (curState.getBlock() instanceof BlockFiniteFluid) {
                metaCurrent = BlockFiniteFluid.getVolume(world, current); //curState);
            }

            if (metaCurrent < 9) {
                byte var18 = 8;
                boolean isAir = world.isAirBlock(current);
                if (isAir) var18 = 7;

                int newMetaAtCurrent = metaCurrent + 7;

                // setCurrentWater(block) -> usamos el bloque en source
                setCurrentFluidIndex(blockAtSource, world, source);

                // ((liquids.get(onFiniteFluidIndex)).flow) con meta newMetaAtCurrent
                NewFluidType fluid = (NewFluidType) liquids.get(onFiniteFluidIndex);
                IBlockState oldCur = world.getBlockState(current);
                //world.setBlockState(current, fluid.flowingBlock.getDefaultState().withProperty(BlockFiniteFluid.LEVEL, newMetaAtCurrent), 3);
                BlockFiniteFluid.setBlockState(world, current, BlockFiniteFluid.setVolume(fluid.flowingBlock.getDefaultState(), newMetaAtCurrent));
                world.notifyBlockUpdate(current, oldCur, world.getBlockState(current), 3);

                IBlockState oldSrc = world.getBlockState(source);
                //world.setBlockState(source, fluid.flowingBlock.getDefaultState().withProperty(BlockFiniteFluid.LEVEL, (int)var18), 3);
                BlockFiniteFluid.setBlockState(world, source, BlockFiniteFluid.setVolume(fluid.flowingBlock.getDefaultState(), (int)var18));
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
                              ? BlockFiniteFluid.getVolume(world, current) : -1;//curState) : -1;

            // if (var12 > 7 & var5 >= var2 & var7 > 0)
            if (metaCurrent > 7 & current.getY() >= source.getY() & depth > 0 && (blockAtSource instanceof BlockFiniteFluid)) {
                int newMetaAtCurrent = metaCurrent - 8;

                // setCurrentWater(block) usando el bloque del source
                ////System.out.println("LMOVE Position: "+ source);
                ////System.out.println("LMOVE Block: "+ blockAtSource);
                setCurrentFluidIndex(blockAtSource, world, source);

                NewFluidType fluid = (NewFluidType) liquids.get(onFiniteFluidIndex);

                IBlockState oldCur = world.getBlockState(current);
                //world.setBlockState(current, fluid.flowingBlock.getDefaultState().withProperty(BlockFiniteFluid.LEVEL, newMetaAtCurrent), 3);
                BlockFiniteFluid.setBlockState(world, current, BlockFiniteFluid.setVolume(fluid.flowingBlock.getDefaultState(), newMetaAtCurrent));
                world.notifyBlockUpdate(current, oldCur, world.getBlockState(current), 3);

                IBlockState oldSrc = world.getBlockState(source);
                //world.setBlockState(source, fluid.flowingBlock.getDefaultState().withProperty(BlockFiniteFluid.LEVEL, 7), 3);
                BlockFiniteFluid.setBlockState(world, source, BlockFiniteFluid.setVolume(fluid.flowingBlock.getDefaultState(), 7));
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
        
        
        public static boolean tryMove(World world, BlockPos pos) {
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
        
        //NUNCA USADO
        public static Vec3d tryMoveVec(World world, BlockPos pos) {
            if (world.isRemote) return Vec3d.ZERO;

            Random rand = new Random();
            int dx = rand.nextBoolean() ? 1 : -1;
            int dz = rand.nextBoolean() ? 1 : -1;
            boolean flip = rand.nextBoolean();

            BlockPos below = pos.down(getFluidGravity());

            if (liquidMove(world, pos, below, true)) return new Vec3d(0, -1, 0);

            if (flip) {
                if (liquidMove(world, pos, pos.add(-dx, -getFluidGravity(), 0), true)) return new Vec3d(-dx, -getFluidGravity(), 0);
                if (liquidMove(world, pos, pos.add(dx, -getFluidGravity(), 0), true)) return new Vec3d(dx, -getFluidGravity(), 0);
                if (liquidMove(world, pos, pos.add(0, -getFluidGravity(), -dz), true)) return new Vec3d(0, -getFluidGravity(), -dz);
                if (liquidMove(world, pos, pos.add(0, -getFluidGravity(), dz), true)) return new Vec3d(0, -getFluidGravity(), dz);
                if (liquidMove(world, pos, pos.add(-dx, 0, 0), true)) return new Vec3d(-dx, 0, 0);
                if (liquidMove(world, pos, pos.add(dx, 0, 0), true)) return new Vec3d(dx, 0, 0);
                if (liquidMove(world, pos, pos.add(0, 0, -dz), true)) return new Vec3d(0, 0, -dz);
                if (liquidMove(world, pos, pos.add(0, 0, dz), true)) return new Vec3d(0, 0, dz);
            } else {
                if (liquidMove(world, pos, pos.add(0, -getFluidGravity(), -dz), true)) return new Vec3d(0, -getFluidGravity(), -dz);
                if (liquidMove(world, pos, pos.add(0, -getFluidGravity(), dz), true)) return new Vec3d(0, -getFluidGravity(), dz);
                if (liquidMove(world, pos, pos.add(-dx, -getFluidGravity(), 0), true)) return new Vec3d(-dx, -getFluidGravity(), 0);
                if (liquidMove(world, pos, pos.add(dx, -getFluidGravity(), 0), true)) return new Vec3d(dx, -getFluidGravity(), 0);
                if (liquidMove(world, pos, pos.add(0, 0, -dz), true)) return new Vec3d(0, 0, -dz);
                if (liquidMove(world, pos, pos.add(0, 0, dz), true)) return new Vec3d(0, 0, dz);
                if (liquidMove(world, pos, pos.add(-dx, 0, 0), true)) return new Vec3d(-dx, 0, 0);
                if (liquidMove(world, pos, pos.add(dx, 0, 0), true)) return new Vec3d(dx, 0, 0);
            }

            return Vec3d.ZERO;
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
        
        
        public static boolean canPlaceEntityOnSide(World world, BlockPos pos, EnumFacing side, Entity entity, ItemStack stack) {
            int fluidIndex = getFluidIndex(world.getBlockState(pos).getBlock(), world, pos);
            return fluidIndex != -1 || world.mayPlace(world.getBlockState(pos).getBlock(), pos, false, side, entity);
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
            int type = getFluidIndex(block, world, pos);
            
            
            BlockPos above = pos.up();
            Block blockAbove = world.getBlockState(above).getBlock();

            BlockPos below = pos.down();
            Block blockBelow = world.getBlockState(below).getBlock();
            int typeBelow = getFluidIndex(blockBelow, world, below);

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

                    if (block == lavaType.oceanBlock) { // o el tipo que uses

                        world.setBlockState(pos, Blocks.OBSIDIAN.getDefaultState());
                        world.playSound(null, pos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.5F, 2.6F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.8F);
                        return true;
                    } else if (block == lavaType.flowingBlock || block == lavaType.stillBlock) {
                    	int lavaLevel = BlockFiniteFluid.getVolume(blockState);
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
                    int typeNeighbor = getFluidIndex(neighborBlock, world, neighborPos);

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
                    BlockPos neighborPos = pos.offset(EnumFacing.getHorizontal(i));
                    Block neighborBlock = world.getBlockState(neighborPos).getBlock();

                    int typeNeighbor = getFluidIndex(neighborBlock, world, neighborPos);
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
		if (level <= 14) {

            BlockPos[] laterals = { pos.north(), pos.south(), pos.east(), pos.west() };
            BlockPos[] diagonals = {
                pos.north().east(), pos.north().west(),
                pos.south().east(), pos.south().west()
            };

            // 1. Laterales
            for (BlockPos p : laterals) {
                if (world.getBlockState(p).getBlock() instanceof BlockFiniteFluid) {
                    int neighborLevel = world.getBlockState(p).getValue(BlockFiniteFluid.LEVEL);
                    int toTake = 15 - level;
                    int newNeighborLevel = neighborLevel - toTake;
                    if (newNeighborLevel < 0) world.setBlockToAir(p);
                    else world.setBlockState(p, world.getBlockState(p).getBlock().getDefaultState().withProperty(BlockFiniteFluid.LEVEL, newNeighborLevel));

                    world.setBlockToAir(pos);
                    totalLevel += neighborLevel;
                    if (totalLevel >= 14) { return true; }
                }
            }


            // 2. Diagonales
            if (totalLevel < 15) {
                for (BlockPos p : diagonals) {
                    if (world.getBlockState(p).getBlock() instanceof BlockFiniteFluid) {
                        int neighborLevel = world.getBlockState(p).getValue(BlockFiniteFluid.LEVEL);
                        int toTake = 15 - level;
                        int newNeighborLevel = neighborLevel - toTake;
                        if (newNeighborLevel < 0) world.setBlockToAir(p);
                        else world.setBlockState(p, world.getBlockState(p).getBlock().getDefaultState().withProperty(BlockFiniteFluid.LEVEL, newNeighborLevel));

                        world.setBlockToAir(pos);
                        totalLevel += neighborLevel;
                        //>= 14 en vez ded >= 15 para evitar perder un liquiddo solo por un Level
                    	////System.out.println("LEVEL" + (totalLevel));
                        if (totalLevel >= 13) { return true; }
                    }
                }
            }

            // 3. Abajo
            if (totalLevel < 15) {
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
	                totalLevel += BlockFiniteFluid.getVolume(world.getBlockState(p)); //world.getBlockState(p).getValue(BlockFiniteFluid.LEVEL);
	            }
	        }

	        // 2. Calcular niveles en diagonales
	        BlockPos[] diagonals = {
	            pos.north().east(), pos.north().west(),
	            pos.south().east(), pos.south().west()
	        };
	        for (BlockPos p : diagonals) {
	            if (world.getBlockState(p).getBlock() instanceof BlockFiniteFluid) {
	                totalLevel += BlockFiniteFluid.getVolume(world.getBlockState(p)); //world.getBlockState(p).getValue(BlockFiniteFluid.LEVEL);
	            }
	        }

	        // 3. Calcular nivel abajo
	        BlockPos below = pos.down();
	        IBlockState belowState = world.getBlockState(below);
	        if (belowState.getBlock() instanceof BlockFiniteFluid) {
	            totalLevel += BlockFiniteFluid.getVolume(belowState); //belowState.getValue(BlockFiniteFluid.LEVEL);
	        }

	        // Si no alcanza 14, no hacemos nada
	        if (totalLevel < 14) {
	            return false; // Nada pasa
	        }

	        // Ahora sí, remover líquido para simular la recogida
	        int needed = 15;
	        //shouldContinue = true;
	        int remaining = needed - level;
	        world.setBlockToAir(pos);

	        // Consumir laterales
	        for (BlockPos p : laterals) {
	            if (remaining <= 0) break;
	            IBlockState s = world.getBlockState(p);
	            if (s.getBlock() instanceof BlockFiniteFluid) {
	                int neighborLevel = BlockFiniteFluid.getVolume(s); //s.getValue(BlockFiniteFluid.LEVEL);
	                int take = Math.min(neighborLevel, remaining);
	                int newLevel = neighborLevel - take;
	                if (newLevel <= 0) world.setBlockToAir(p);
	                else world.setBlockState(p, s.getBlock().getDefaultState().withProperty(BlockFiniteFluid.LEVEL, newLevel));
	                remaining -= take;
	            }
	        }

	        // Consumir diagonales
	        if (remaining > 0) {
	            for (BlockPos p : diagonals) {
	                if (remaining <= 0) break;
	                IBlockState s = world.getBlockState(p);
	                if (s.getBlock() instanceof BlockFiniteFluid) {
	                    int neighborLevel = BlockFiniteFluid.getVolume(s); //s.getValue(BlockFiniteFluid.LEVEL);
	                    int take = Math.min(neighborLevel, remaining);
	                    int newLevel = neighborLevel - take;
	                    if (newLevel <= 0) world.setBlockToAir(p);
	                    else world.setBlockState(p, s.getBlock().getDefaultState().withProperty(BlockFiniteFluid.LEVEL, newLevel));
	                    remaining -= take;
	                }
	            }
	        }

	        // Consumir abajo (si queda algo)
	        if (remaining > 0 && belowState.getBlock() instanceof BlockFiniteFluid) {
	            int neighborLevel = BlockFiniteFluid.getVolume(belowState); //belowState.getValue(BlockFiniteFluid.LEVEL);
	            int take = Math.min(neighborLevel, remaining);
	            int newLevel = neighborLevel - take;
	            if (newLevel <= 0) world.setBlockToAir(below);
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

		    if (collected >= 16) return true; // Ya lleno, terminamos

		    // Posiciones laterales y diagonales
		    BlockPos[] laterals = { pos.north(), pos.south(), pos.east(), pos.west() };
		    BlockPos[] diagonals = {
		        pos.north().east(), pos.north().west(),
		        pos.south().east(), pos.south().west()
		    };

		    // Recolectar suavemente
		    collected = collectEqually(world, laterals, collected, 16, targetFluid);
		    if (collected < 16) collected = collectEqually(world, diagonals, collected, 16, targetFluid);

		    // Última oportunidad: abajo
		    if (collected < 16) {
		        BlockPos below = pos.down();
		        IBlockState belowState = world.getBlockState(below);
		        if (belowState.getBlock() instanceof IFluidBlock) {
		            IFluidBlock fluidBlock = (IFluidBlock) belowState.getBlock();
		            if (fluidBlock.getFluid() == targetFluid) {
		                int neighborLevel = getConceptualLevelForBlock(belowState);
		                int take = Math.min(neighborLevel, 16 - collected);
		                int newLevel = neighborLevel - take;

		                if (newLevel <= 0) {
		                    if (newLevel == 0) {
		                        //world.setBlockState(below, belowState.withProperty(BlockFiniteFluid.LEVEL, 0)); // nivel mínimo
		                    	BlockFiniteFluid.setBlockState(world, below, BlockFiniteFluid.setVolume(belowState, BlockFiniteFluid.MINIMUM_LEVEL));
		                    } else {
		                        world.setBlockToAir(below); // newLevel < 0
		                    }
		                } else {
		                    world.setBlockState(below, setConceptualLevelForBlock(belowState, newLevel));
		                }
		                collected += take;
		            }
		        }
		    }

		    return collected >= 16; // True si alcanzó 16 niveles conceptuales, false si no
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
		                totalAvailable += getConceptualLevelForBlock(state);
		            }
		        }
		    }

		    for (BlockPos p : diagonals) {
		        IBlockState state = world.getBlockState(p);
		        if (state.getBlock() instanceof IFluidBlock) {
		            IFluidBlock fluidBlock = (IFluidBlock) state.getBlock();
		            if (fluidBlock.getFluid() == targetFluid) {
		                totalAvailable += getConceptualLevelForBlock(state);
		            }
		        }
		    }

		    if (belowState.getBlock() instanceof IFluidBlock) {
		        IFluidBlock fluidBlock = (IFluidBlock) belowState.getBlock();
		        if (fluidBlock.getFluid() == targetFluid) {
		            totalAvailable += getConceptualLevelForBlock(belowState);
		        }
		    }

		    if (totalAvailable < 16) return false;

		    // --- Recolectar ---
		    world.setBlockToAir(pos);

		    collected = collectEqually(world, laterals, collected, 16, targetFluid);
		    if (collected < 16) collected = collectEqually(world, diagonals, collected, 16, targetFluid);
		    if (collected < 16 && belowState.getBlock() instanceof IFluidBlock) {
		        IFluidBlock fluidBlock = (IFluidBlock) belowState.getBlock();
		        if (fluidBlock.getFluid() == targetFluid) {
		            int neighborLevel = getConceptualLevelForBlock(belowState);
		            int take = Math.min(neighborLevel, 16 - collected);
		            int newLevel = neighborLevel - take;
		            if (newLevel <= 0) world.setBlockToAir(below);
		            else world.setBlockState(below, setConceptualLevelForBlock(belowState, newLevel));
		            collected += take;
		        }
		    }

		    return true;
		}

		// Helper para extraer el nivel conceptual de un bloque
		private static int getConceptualLevelForBlock(IBlockState state) {
		    if (state.getBlock() instanceof BlockFiniteFluid) {
		        return BlockFiniteFluid.getConceptualVolume(state); //state.getValue(BlockFiniteFluid.LEVEL) + 1;
		    }
		    return 0;
		}

		// Helper para setear el nivel conceptual de un bloque
		private static IBlockState setConceptualLevelForBlock(IBlockState state, int level) {
		    if (state.getBlock() instanceof BlockFiniteFluid) {
		        return BlockFiniteFluid.setConceptualVolume(state, level); //state.withProperty(BlockFiniteFluid.LEVEL, level - 1);
		    }
		    return state;
		}
		
		
		
		public static int collectEqually(World world, BlockPos[] positions, int collected, int spaceLeft, Fluid targetFluid) {
		    for (BlockPos pos : positions) {
		        IBlockState state = world.getBlockState(pos);
		        Block block = state.getBlock();
		        if (block instanceof BlockFiniteFluid) {
		            Fluid fluid = ((BlockFiniteFluid) block).getFluid();
		            if (fluid != targetFluid) continue; // skip distinto tipo

		            int neighborLevel = BlockFiniteFluid.getConceptualVolume(state); //state.getValue(BlockFiniteFluid.LEVEL) + 1;
		            int take = Math.min(neighborLevel, spaceLeft - collected);
		            int newLevel = neighborLevel - take;
		            if (newLevel <= 0) world.setBlockToAir(pos);
		            else BlockFiniteFluid.setBlockState(world, pos, BlockFiniteFluid.setConceptualVolume(state, newLevel)); //world.setBlockState(pos, state.withProperty(BlockFiniteFluid.LEVEL, newLevel - 1));
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

                if (neighborState.getBlock() instanceof BlockNewInfiniteSource) {
                    exposedToOceanWater = true;
                    
                    if (exposedToOceanWater
                        && !(world.getBlockState(pos.down()).getBlock() instanceof BlockFiniteFluid)
                        && !(world.getBlockState(pos).getBlock() instanceof BlockFiniteFluid)) {
                        FiniteFluidLogic.InfiniteWaterSource.borderOceanCheck(world, pos);
                    }
                    break;
                }
            }
	    }
		
		
		
		public static int distributeEqually(World world, List<BlockPos> targets, int remaining, int fluidType) {
            if (targets.isEmpty() || remaining <= 0) return remaining; //original: <=0, ahora es <0, para aceptar al 0 entre los valores

            boolean didSomething = true;

            // Repite mientras queden niveles que distribuir
            while (remaining > 0 && didSomething) {
                didSomething = false;
                for (BlockPos p : targets) {
                    if (remaining <= 0) break; //original: <=0, ahora es <0, para aceptar al 0 entre los valores

                    IBlockState s = world.getBlockState(p);
                    if (!(s.getBlock() instanceof BlockFiniteFluid) || FiniteFluidLogic.GeneralPurposeLogic.getFluidIndex(s.getBlock(), world, p) != fluidType) continue; //Para que no crashee la IC2 FluidCell en el CASO 1) xd
                    int level = BlockFiniteFluid.getVolume(s);
                    if (level < 15) {
                    	int temporalFluidType = FiniteFluidLogic.GeneralPurposeLogic.getFluidIndex(s.getBlock(), world, p); 
                    	Block newBlock1 = ((NewFluidType)liquids.get(temporalFluidType)).flowingBlock;
                    	
                        //world.setBlockState(p, newBlock1.getDefaultState().withProperty(BlockFiniteFluid.LEVEL, level + 1)); //s.withProperty(RFFBlock.LEVEL, level + 1));
                        BlockFiniteFluid.setBlockState(world, p, BlockFiniteFluid.setVolume(newBlock1.getDefaultState(), level+1));	
                        remaining--;
                        
                        didSomething = true;
                    }
                }
            }

            return remaining;
        }
		
		public static int distributeEquallyNoAdyFluid(World world, List<BlockPos> targets, int remaining, Block finiteFluidBlock) {
            if (targets.isEmpty() || remaining <= 0) return remaining; //original: <=0, ahora es <0, para aceptar al 0 entre los valores

            boolean didSomething = true;

            // Repite mientras queden niveles que distribuir
            while (remaining > 0 && didSomething) {
                didSomething = false;
                for (BlockPos p : targets) {
                    if (remaining <= 0) break; //original: <=0, ahora es <0, para aceptar al 0 entre los valores

                    IBlockState s = world.getBlockState(p);
                    if (world.isAirBlock(p) && !(s.getBlock() instanceof BlockFiniteFluid)) { //Para que no crashee la IC2 FluidCell en el CASO 1) xd
                        //if (!(remaining > 16)) { //16 porque estamos en LEVELs conceptuales
                        	//world.setBlockState(p, finiteFluidBlock.getDefaultState().withProperty(BlockFiniteFluid.LEVEL, remaining-1)); //ModBlocks.FINITE_WATER_FLOWING.getDefaultState().withProperty(RFFBlock.LEVEL, remaining-1));
                        BlockFiniteFluid.setBlockState(world, p, BlockFiniteFluid.setConceptualVolume(finiteFluidBlock.getDefaultState(), remaining));	
                    	return 0;	
                        /*} else {
                        	world.setBlockState(p, finiteFluidBlock.getDefaultState().withProperty(RFFBlock.LEVEL, 15)); //15 porque esta en LEVELs directos
                        	return remaining -= 16; //-16 porque esta en LEVELs conceptuales
                        }*/
                    } 
                    
                    int level = BlockFiniteFluid.getVolume(s);
                    if (level < 15) {
                        //world.setBlockState(p, s.withProperty(BlockFiniteFluid.LEVEL, level + 1));
                        BlockFiniteFluid.setBlockState(world, p, BlockFiniteFluid.setVolume(s, level + 1));	
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
		    if (takeFromCenter > 0) {
		        collected += takeFromCenter;
		        int newLevelCenter = original - takeFromCenter;
		        if (newLevelCenter <= 0) {
		            world.setBlockToAir(pos);
		            activateOcean(world, pos);
		        } else {
		            //world.setBlockState(pos, state.withProperty(BlockFiniteFluid.LEVEL, newLevelCenter - 1));
                	BlockFiniteFluid.setBlockState(world, pos, BlockFiniteFluid.setConceptualVolume(state, newLevelCenter));
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
		                int neighborLevel = BlockFiniteFluid.getConceptualVolume(belowState); //belowState.getValue(BlockFiniteFluid.LEVEL) + 1; // conceptual
		                int take = Math.min(neighborLevel, spaceLeft - collected);
		                int newLevel = neighborLevel - take;
		                if (newLevel <= 0) {world.setBlockToAir(below);  activateOcean(world, below);}
		                else BlockFiniteFluid.setBlockState(world, below, BlockFiniteFluid.setConceptualVolume(belowState, newLevel)); //world.setBlockState(below, belowState.withProperty(BlockFiniteFluid.LEVEL, newLevel - 1));
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
		        if (s.getBlock() instanceof BlockFiniteFluid) total += BlockFiniteFluid.getConceptualVolume(s); //s.getValue(BlockFiniteFluid.LEVEL) + 1;
		    }
		    for (BlockPos p : diagonals) {
		        IBlockState s = world.getBlockState(p);
		        if (s.getBlock() instanceof BlockFiniteFluid) total += BlockFiniteFluid.getConceptualVolume(s); //s.getValue(BlockFiniteFluid.LEVEL) + 1;
		    }
		    BlockPos below = pos.down();
		    IBlockState belowState = world.getBlockState(below);
		    if (belowState.getBlock() instanceof BlockFiniteFluid) total += BlockFiniteFluid.getConceptualVolume(belowState); //belowState.getValue(BlockFiniteFluid.LEVEL) + 1;

		    // si no hay suficientes niveles (conceptuales) abortamos
		    if (total < 16) return 0;

		    // Si hay suficientes, borramos/extraemos suavemente igual que en EvenLow
		    int original = levelConceptual;
		    int collected = original;
		    world.setBlockToAir(pos);

		    collected = collectEqually(world, laterals, collected, 16, targetFluid);
		    if (collected < 16) collected = collectEqually(world, diagonals, collected, 16, targetFluid);
		    if (collected < 16 && belowState.getBlock() instanceof BlockFiniteFluid) {
		        int neighborLevel = BlockFiniteFluid.getConceptualVolume(belowState); //belowState.getValue(BlockFiniteFluid.LEVEL) + 1;
		        int take = Math.min(neighborLevel, 16 - collected);
		        int newLevel = neighborLevel - take;
		        if (newLevel <= 0) world.setBlockToAir(below);
		        else BlockFiniteFluid.setBlockState(world, below, BlockFiniteFluid.setConceptualVolume(belowState, newLevel)); //world.setBlockState(below, belowState.withProperty(BlockFiniteFluid.LEVEL, newLevel - 1));
		        collected += take;
		    }

		    return Math.max(0, collected - original);
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
