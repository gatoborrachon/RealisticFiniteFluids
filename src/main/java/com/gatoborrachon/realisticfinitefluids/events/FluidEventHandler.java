package com.gatoborrachon.realisticfinitefluids.events;

import com.gatoborrachon.realisticfinitefluids.compat.FluidCompat;
import com.gatoborrachon.realisticfinitefluids.interfaces.IRealisticFiniteFluid;
import com.gatoborrachon.realisticfinitefluids.logic.FiniteFluidLogic;
import com.gatoborrachon.realisticfinitefluids.logic.RealisticFiniteFluidFunctions;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

//@Mod.EventBusSubscriber
public class FluidEventHandler {
	
	
	@SubscribeEvent
	public static void onWorldLoad(WorldEvent.Load event) {
	    if (!event.getWorld().isRemote) {
	    	FiniteFluidLogic.clearLiquidLists();
	        FluidCompat.loadFiniteFluids(event.getWorld());
	    }
	}
	
	
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
    public static void onBlockPlaced(BlockEvent.PlaceEvent event) { //BlockEvent.EntityPlaceEvent <-- Creo que podria usar esto
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

            IRealisticFiniteFluid realisticFluid = ((IRealisticFiniteFluid)FiniteFluidLogic.liquids.get(FiniteFluidLogic.onFiniteFluidIndex).flowingBlock);
            if (realisticFluid.isOceanBlock(world, neighbor, neighborState, FiniteFluidLogic.GeneralPurposeLogic.getFluidIndex(RealisticFiniteFluidFunctions.getBlock(world, neighbor, neighborState)))) {
            	exposedToOceanWater = true;
                break;
            }
        }
        
        if (!world.isRemote && exposedToOceanWater && !(RealisticFiniteFluidFunctions.getBlock(world, pos.down(), world.getBlockState(pos.down())) instanceof IRealisticFiniteFluid) && !(RealisticFiniteFluidFunctions.getBlock(world, pos, world.getBlockState(pos)) instanceof IRealisticFiniteFluid)) {
            FiniteFluidLogic.OceanFluidsLogic.borderOceanCheck(world, pos, false);
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

            IRealisticFiniteFluid realisticFluid = ((IRealisticFiniteFluid)FiniteFluidLogic.liquids.get(FiniteFluidLogic.onFiniteFluidIndex).flowingBlock);
            if (realisticFluid.isOceanBlock(world, neighbor, neighborState, FiniteFluidLogic.GeneralPurposeLogic.getFluidIndex(RealisticFiniteFluidFunctions.getBlock(world, neighbor, neighborState))) && dir != EnumFacing.DOWN) {
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
            FiniteFluidLogic.OceanFluidsLogic.borderOceanCheck(world, pos);
        }*/
        
        //xd, no necesito checar si el bloque actual o el de abajo es agua, al final, cualquer rotura (donde el bloque roto tiene siquiera agua a un lado)
        //pues siempre se generara un espacio por donde moverse el agua
        if (exposedToOceanWater 
        /*&& !(world.getBlockState(pos.down()).getBlock() instanceof BlockFiniteFluid) 
        && !(world.getBlockState(pos).getBlock() instanceof BlockFiniteFluid)*/
        ) {
            FiniteFluidLogic.OceanFluidsLogic.borderOceanCheck(world, pos, false);
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
                if (FiniteFluidLogic.OceanFluidsLogic.hasNearbyOceanWater(world, pos)) {
                    FiniteFluidLogic.OceanFluidsLogic.borderOceanCheck(world, pos);
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
            	//Block thisBlock = world.getBlockState(pos).getBlock();
            	//System.out.println("BLOQUE VERGA: '" +thisBlock+ "' EN POS: '"+pos+"'");
            	//if (RealisticFiniteFluidFunctions.isOceanBlock(world, pos, null, 
            	//		FiniteFluidLogic.GeneralPurposeLogic.getFluidIndex(world.getBlockState(pos).getBlock()))) {
            	
                	//System.out.println("BLOQUE AGUA: "+pos);
                	//world.scheduleBlockUpdate(pos, waterTick + 1, 0);
                	//world.scheduleUpdate(pos, thisBlock, thisBlock.tickRate(world));
            		if (FiniteFluidLogic.OceanFluidsLogic.borderOceanCheck(world, pos, true))  {
                    	//System.out.println("BLOQUE VERGA: '" +thisBlock+ "' EN POS: '"+pos+"'");
            			break;
            		}
                	
                	/*
                	 * Tal ves esta funcion hace exactamente lo mismo que el codigo de hasta arriba, pero a ver que onda
                	 * NOTA --> Ya vi que ahce lo mismo que el schedule update xd, a ver si lo hace mejor que yo
                	 * Funciona, pero ahora provoca que puedas hacer "agua infinita" a partir del oceano a punta de explosiones, aunque dudo much
                	 * que alguien pueda aprovechar este "bug" a punta de explosiones
                	 */
            		//FiniteFluidLogic.OceanFluidsLogic.wakeOcean(world, pos);
            	//}
            }
        }
    }

    
}
