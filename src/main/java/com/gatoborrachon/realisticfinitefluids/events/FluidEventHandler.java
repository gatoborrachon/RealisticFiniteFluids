package com.gatoborrachon.realisticfinitefluids.events;

import java.util.HashSet;
import java.util.Set;

import com.gatoborrachon.realisticfinitefluids.blocks.BlockFiniteFluid;
import com.gatoborrachon.realisticfinitefluids.blocks.BlockNewInfiniteSource;
import com.gatoborrachon.realisticfinitefluids.init.ModConfig;
import com.gatoborrachon.realisticfinitefluids.init.ModItems;
import com.gatoborrachon.realisticfinitefluids.logic.FiniteFluidLogic;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.entity.player.FillBucketEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.event.world.ExplosionEvent;

import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

//@Mod.EventBusSubscriber
public class FluidEventHandler {
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            FiniteFluidLogic.GeneralPurposeLogic.onTick();
        }
    }
    
	
	
    /*
     *ASI ESTA EL PEDO 
     * 
     * Si usas borderOceanCheck(world, pos) sin verificar que debes ignorar agua debajo de tu bloque, te cuelgas el servidor
     * 
     * Si usar borderOceanCheck(world, pos, int check), reduces muchisimo la carga de trabajo debido a que usas un limite muy pequeño (creo que 11),
     * pero tambien eres propenso a crear agua infinita con el metodo del cual proviene el evento (poner bloques, rompoer bloques, o explosiones)
     * 
     * 
     * 
     * Actualmente, el evento de explosiones maneja otra logica para reducir muchisimo la carga  de trabajo (wakeOcean)
     * 
     * Podria usar borderOceanCheck de 3 argumentos? tal vez, igual tengo que evitar usar bloques que tienen agua oceanica abajo, 
     * Usar borderOceanCheck(3) en vez de wakeOcean podria evitar hacer agua oceanica a base de explociones, pero consumiria mas recursos
     * (usar borderOceanCheck(2) evitando usar bloques que tienen agua oceanica abajo evito que el server muriera, pero todo se laggeo cabron)
     * 
     */

    
    @SubscribeEvent
    public static void onBlockPlaced(BlockEvent.PlaceEvent event) {
        World world = event.getWorld();
        BlockPos pos = event.getPos();
        boolean exposedToOceanWater = false;

        /*
         * Check para ver si es que no estamos en el cliente (debemos ejecutarlo en el servidor)
         * Y
         * Si el bloque de abajo NO es un bloque de agua finita (crashea todo el pedo por algun motivo)
         */
        for (EnumFacing dir : EnumFacing.VALUES) {
            BlockPos neighbor = pos.offset(dir);
            IBlockState neighborState = world.getBlockState(neighbor);

            if (neighborState.getBlock() instanceof BlockNewInfiniteSource) {
            	exposedToOceanWater = true;
                break;
            }
        }
        
        if (!world.isRemote && exposedToOceanWater && !(world.getBlockState(pos.down()).getBlock() instanceof BlockFiniteFluid) && !(world.getBlockState(pos).getBlock() instanceof BlockFiniteFluid)) {
            FiniteFluidLogic.InfiniteWaterSource.borderOceanCheck(world, pos);
        }
    }
   
    @SubscribeEvent
    public static void onBlockBroken(BlockEvent.BreakEvent event) {
        World world = event.getWorld();
        BlockPos pos = event.getPos();
        if (world.isRemote) return;
        
        boolean exposedToOceanWater = false;
        //boolean waterOnTopOrLaterals = false;

        for (EnumFacing dir : EnumFacing.VALUES) {
            BlockPos neighbor = pos.offset(dir);
            IBlockState neighborState = world.getBlockState(neighbor);

            if (neighborState.getBlock() instanceof BlockNewInfiniteSource && dir != EnumFacing.DOWN) {
            	exposedToOceanWater = true;
                break;
            }
        }
        
        //Esta alterantiva a onBlockPlaced evita que el agua se quede estatica si rompes un techo que tiene agua arriba y abajo, sin colgar el server por 
        //llamadas innecesarias (en este caso, si es necesaria la llamada porque los bloques de arriba o los laterales si pueden muverse aca)
        //dudo que sea necesario implementar en onBlockPlaced porque sea como sea, no generas nuevo espacio para mover el agua
        /*if (exposedToOceanWater 
        || (!(world.getBlockState(pos.down()).getBlock() instanceof BlockFiniteFluid)  
        && !(world.getBlockState(pos).getBlock() instanceof BlockFiniteFluid))
        ) {
            FiniteFluidLogic.InfiniteWaterSource.borderOceanCheck(world, pos);
        }*/
        
        //xd, no necesito checar si el bloque actual o el de abajo es agua, al final, cualquer rotura (donde el bloque roto tiene siquiera agua a un lado)
        //pues siempre se generara un espacio por donde moverse el agua
        if (exposedToOceanWater 
        /*&& !(world.getBlockState(pos.down()).getBlock() instanceof BlockFiniteFluid) 
        && !(world.getBlockState(pos).getBlock() instanceof BlockFiniteFluid)*/
        ) {
            FiniteFluidLogic.InfiniteWaterSource.borderOceanCheck(world, pos);
        }
    }
    
    @SubscribeEvent
    public static void onExplosion(ExplosionEvent.Detonate event) {
        World world = event.getWorld();

        /*
         * Este codigo aguanto bien una explosion, pero no aguanto bien 3
         */
        /*if (!world.isRemote) {
            for (BlockPos pos : event.getAffectedBlocks()) {
                if (FiniteFluidLogic.InfiniteWaterSource.hasNearbyOceanWater(world, pos)) {
                    FiniteFluidLogic.InfiniteWaterSource.borderOceanCheck(world, pos);
                }
            }
        }*/
        
        /*
         * Ya antendi porque esta mamada no sirve,
         * Si obligas a borderOceanCheck a checar puros bloques que SOLO SON AGUA, pues vale madre, porque la funcion se ecnarga de despertar
         * tanto a este bloque como a bloques proximos, y si yo añado un check para mandar alv bloques que no son agua, pues bueno ya no se jaja
         * creo que con wakeOcean seria suficienete
         */
        
        if (!world.isRemote) {
            for (BlockPos pos : event.getAffectedBlocks()) {
            	//System.out.println("BLOQUE VERGA: "+pos);
            	//Block thisBlock = world.getBlockState(pos).getBlock();
            	if (world.getBlockState(pos).getBlock() instanceof BlockNewInfiniteSource) {
                	//System.out.println("BLOQUE AGUA: "+pos);
                	//world.scheduleBlockUpdate(pos, waterTick + 1, 0);
                	//world.scheduleUpdate(pos, thisBlock, thisBlock.tickRate(world));
            		//FiniteFluidLogic.InfiniteWaterSource.borderOceanCheck(world, pos);
                	
                	/*
                	 * Tal ves esta funcion hace exactamente lo mismo que el codigo de hasta arriba, pero a ver que onda
                	 * NOTA --> Ya vi que ahce lo mismo que el schedule update xd, a ver si lo hace mejor que yo
                	 * Funciona, pero ahora provoca que puedas hacer "agua infinita" a partir del oceano a punta de explosiones, aunque dudo much
                	 * que alguien pueda aprovechar este "bug" a punta de explosiones
                	 */
            		FiniteFluidLogic.InfiniteWaterSource.wakeOcean(world, pos);
            	}
            }
        }
    }
    
    /*@SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onFillBucket(FillBucketEvent event) {
        RayTraceResult target = event.getTarget();
        if (target == null || target.typeOfHit != RayTraceResult.Type.BLOCK) return;

        World world = event.getWorld();
        BlockPos pos = target.getBlockPos(); 
        
        IBlockState state = world.getBlockState(pos);
        if ((state.getBlock() instanceof BlockFiniteFluid)) {
        	
        boolean exposedToOceanWater = false;

        for (EnumFacing dir : EnumFacing.VALUES) {
            BlockPos neighbor = pos.offset(dir);
            IBlockState neighborState = world.getBlockState(neighbor);

            if (neighborState.getBlock() instanceof BlockNewInfiniteSource) {
                exposedToOceanWater = true;
                if (!world.isRemote
                        && exposedToOceanWater
                        && !(world.getBlockState(pos.down()).getBlock() instanceof BlockFiniteFluid)
                        && !(world.getBlockState(pos).getBlock() instanceof BlockFiniteFluid)) {
                        
                        FiniteFluidLogic.InfiniteWaterSource.borderOceanCheck(world, neighbor);
                    }
                break;
            }
        }
        
        }


    }*/
    
    
    @SubscribeEvent(priority = EventPriority.HIGHEST) // para ejecutar después de otros mods, pero antes del resultado final
    public static void onFillBucket(FillBucketEvent event) {
    	////System.out.println("---------------------------------------------------------------");
    	//System.out.println("[DEBUG] FillBucketEvent detectado: " + event.getEmptyBucket());
    	//System.out.println("[DEBUG] Estado antes de cancelar: " + event.isCanceled());
    	
        RayTraceResult target = event.getTarget();
        if (target == null || target.typeOfHit != RayTraceResult.Type.BLOCK) return;

        World world = event.getWorld();
        EntityPlayer player = event.getEntityPlayer();
        BlockPos pos = target.getBlockPos();
        IBlockState state = world.getBlockState(pos);
        Block block = state.getBlock();

    	////System.out.println("BLOQUE  DETECTADO " + block);

        
        // Si es agua vanilla
        if ((state.getBlock() == Blocks.WATER || state.getBlock() == Blocks.FLOWING_WATER) &&
                state.getValue(BlockLiquid.LEVEL) == 0) {
                event.setFilledBucket(new ItemStack(Items.WATER_BUCKET));
                world.setBlockToAir(pos);
                event.setResult(net.minecraftforge.fml.common.eventhandler.Event.Result.ALLOW);
            } else if ((state.getBlock() == Blocks.LAVA || state.getBlock() == Blocks.FLOWING_LAVA) &&
                       state.getValue(BlockLiquid.LEVEL) == 0) {
                event.setFilledBucket(new ItemStack(Items.LAVA_BUCKET));
                world.setBlockToAir(pos);
                event.setResult(net.minecraftforge.fml.common.eventhandler.Event.Result.ALLOW);
            }
        
        // Caso 1: No es tu bloque --> dejamos que siga vanilla
        if (!(block instanceof BlockFiniteFluid)) return;

        //System.out.println("[DEBUG] Bloque FiniteFluid detectado en " + pos);



        // Ahora ejecutamos tu lógica completa de redistribución
        ItemStack emptyBucket = event.getEmptyBucket();
        if (emptyBucket == null || emptyBucket.getItem() != Items.BUCKET) return;


        
        boolean isLava = state.getMaterial() == Material.LAVA;
        //boolean isWater = state.getMaterial() == Material.WATER;
        int level = BlockFiniteFluid.getConceptualVolume(world, pos, state); //state.getValue(BlockFiniteFluid.LEVEL)+1; // 0-15 //+1 --> 1-16 LEVEL conceptual

        //Codigo extraido de mi mixin para la cubeta vacia, si detecta Material.WATER con LEVEL 0, recoge agua cuando no deberia
        //Tal vez no sea tan necesario el world.setBlockToAir, lo voy a comentar a ver que pasa xd
        //
        //"Evitar obtener agua vanilla de mi agua finita con LEVEL de 0"
        Block blockToCheck = world.getBlockState(pos.down()).getBlock();
        if (level == 0 && !(blockToCheck instanceof BlockFiniteFluid)) {
            //world.setBlockToAir(pos);
            event.setCanceled(true);

            return;
        }

        boolean shouldContinue = false;


        /*if (!(blockToCheck instanceof IFluidBlock)) {
        	event.setCanceled(true); 
        	return;
        }*/
        
        if (ModConfig.bucketRemoveLowFluid) {
        	shouldContinue = FiniteFluidLogic.FluidWorldInteraction.bucketRemoveFluidEvenLowNEW(world, pos, level); //, ((IFluidBlock)blockToCheck).getFluid());
        } else {
        	shouldContinue = FiniteFluidLogic.FluidWorldInteraction.bucketRemoveFluidOnlyFullNEW(world, pos, level);
        }

        if (!shouldContinue) return;

        // Sonido y update
        if (!world.isRemote) {
            player.playSound(SoundEvents.ITEM_BUCKET_FILL, 1.0F, 1.0F);
            world.scheduleUpdate(pos, block, block.tickRate(world));
            world.setBlockToAir(pos);
            
            // Ejecutamos tu código extra de verificación de borde oceánico
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

     // Reemplazo por cubeta personalizada
        ItemStack filled = isLava ? new ItemStack(ModItems.FINITE_LAVA_BUCKET) : new ItemStack(ModItems.FINITE_WATER_BUCKET);

        if (!player.capabilities.isCreativeMode) {
            if (emptyBucket.getCount() == 1) {
                // Caso especial: solo había 1 cubeta, reemplazarla directamente en la mano principal
                player.setHeldItem(EnumHand.MAIN_HAND, filled);
            } else {
                // Si había más de una, reducir el stack y añadir la nueva cubeta al inventario
                emptyBucket.shrink(1);
                if (!player.inventory.addItemStackToInventory(filled)) {
                    player.dropItem(filled, false);
                }
            }
        } else {
            // En creativo, simplemente dar la cubeta custom (sin quitar nada)
            player.setHeldItem(EnumHand.MAIN_HAND, emptyBucket);
        }

        // Cancelamos el evento para evitar que Forge siga con la lógica vanilla
        event.setCanceled(true);
        //System.out.println("[DEBUG] Evento cancelado y cubeta personalizada entregada: " + filled);
        
    }
    
    
    
    
    
    // Guarda los chunks ya sellados en RAM mientras el mundo esté activo
    private static final Set<ChunkPos> sealedChunks = new HashSet<>();

    @SubscribeEvent
    public void onChunkLoad(ChunkDataEvent.Load event) {
        NBTTagCompound data = event.getData();
        if (data.getBoolean("sealed")) {
            sealedChunks.add(event.getChunk().getPos());
        }
    }

    @SubscribeEvent
    public void onChunkSave(ChunkDataEvent.Save event) {
        ChunkPos pos = event.getChunk().getPos();
        if (sealedChunks.contains(pos)) {
            event.getData().setBoolean("sealed", true);
        }
    }

    public static boolean isSealed(Chunk chunk) {
        return sealedChunks.contains(chunk.getPos());
    }

    public static void markSealed(Chunk chunk) {
        sealedChunks.add(chunk.getPos());
    }
    


    
    
    //INTENTO PARA REEMPLAZAR AGUA EN EL MUNDO POR MIS BLOQUES, VER CODIGO DE ARRIBA QUE SI REEMPLAZA BIEN BLOQUES
    private static final Set<ChunkPos> convertedChunks = new HashSet<>();

    @SubscribeEvent
    public void onChunkLoadReplace(ChunkDataEvent.Load event) {
        NBTTagCompound data = event.getData();
        if (data.getBoolean("convertedLiquids")) {
            convertedChunks.add(event.getChunk().getPos());
        }
    }

    @SubscribeEvent
    public void onChunkSaveReplace(ChunkDataEvent.Save event) {
        ChunkPos pos = event.getChunk().getPos();
        if (convertedChunks.contains(pos)) {
            event.getData().setBoolean("convertedLiquids", true);
        }
    }
    
    public static boolean isReplaced(Chunk chunk) {
        return convertedChunks.contains(chunk.getPos());
    }

    public static void markReplaced(Chunk chunk) {
    	convertedChunks.add(chunk.getPos());
    }


}
