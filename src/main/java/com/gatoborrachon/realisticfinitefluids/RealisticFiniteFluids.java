package com.gatoborrachon.realisticfinitefluids;

import com.gatoborrachon.realisticfinitefluids.events.FluidEventHandler;
import com.gatoborrachon.realisticfinitefluids.events.FluidModelEventHandler;
import com.gatoborrachon.realisticfinitefluids.init.ModConfig;
import com.gatoborrachon.realisticfinitefluids.logic.FiniteFluidLogic;
import com.gatoborrachon.realisticfinitefluids.proxy.CommonProxy;
import com.gatoborrachon.realisticfinitefluids.util.RFFFluidFixer;

import net.minecraft.util.datafix.FixTypes;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ModFixs;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

/**
 * TODO
 * ARREGLADO? +++URGENTE --> Ver como evitar que ocurra el cuelgue letal del server thread
 * *Creo que todo es debido a revisar bloques que tienen abajo agua oceanica
 * 
 * +Debo ver como poder colocar la lilypad encima de mi agua directamente arriba, y no apoyada de otro bloque, 
 * -Debo ver porque la lluvia sigue apareciendo encima de bloques de mar
 * 
 * +Debo meteler coremods a las funciones que manejan liquidos en forge (y cambiarlas por mi agua)
 * 
 * 
 * TODO EN CHATGPT
 * COMPLETADO DE FORMA PARCIAL, REQUIERE CAMBIOS EN CADA MOD--1.- Modificar el FluidRegistry.WATER.getBlock() para que devuelva mi bloque Flowing
 * COMPLETADO 2.- Modificar la funcion Drain dentro de BlockFiniteFluid (ya esta en ChatGPT)
 * POSTERGADDO--2.1 Modificar el FluidCell de IC2 para que maneje los mb de agua finita a mi antojo
 * COMPLETADO LA PUTA MADRE 3.- Reemplazar el agua oceanica con bloques de piedra o hielo compacto cuando tiene aire alrededor y esta debjao del nivel del agua
 *  (para evitar inundad cuevas desde los rios y el oceano, esto para evitar cagarnos el rendimiento)
 * COMPLETADO A WEBO --> NO USAR UN MIXIN SOBRE LA GENREACION DEL MUNDO POSTERIOR A ELLA, SINO DDURANTE LA GENRAICON DE CUEVAS Y RAVINES 3.1.- Ver como remover las paredes que ahora se forman, cortando minas a la mitad, asociadas a los bordes de los chunks, va a estar cabron
 * 	si solo puedo trabajar con chunk.getBlock y no world.getBlock porque world provoca stackoverflow
 * 4.- Ver como reemplazar el agua de varias estructuras, ahorita mismo estoy teniendo problemas con el monumento oceanico
 * 
 * 5.- Ver como ahcer que puedas obtener agua con cualquier item del tipo cubeta con tan solo mirar la superficie del bloque de agua, 
 * o poder poern una lilipad encima sin necesidad de apoyar tu campo de vision en un bloque, 
 * 	(algo esta pasando que el agua como que no tiene algun tipo de bounding box, que si tiene el agua vanilla, y yo supongo cualquier ClassicFluid)
 * 
 * COMPLETADO 6.- LAST --> Añadir lava
 * COMPETADO LA PUTA MADRE 6.1.- Arreglar bugs de interaccion entre agua-lava (la lava sobre agua --> obsidiana, agua sobre lava --> Nada (bug visual))
 * COMPLETADO 6.2.- Ver porque, al colocar un cubetazo de lava por ejemplo, en un agua de nivel 0-1, esta se recarga de agua (algun if lo ha de resolver)
 * 
 */

//Quiero acabar los /*puntos 6*/, tal vez el 5, y del 3.1 solo cambiar los mares de lava
//Fuera de eso, libero el mod.

/**
 * MIXINS QUE SE ROMPEN POR LA DISONANCIA ENTRE DEV Y FUERA DEL DEV:
 * MixinField1
 * MixinField2
 * MixinWell
 * 
 * MixinVolcanoDecorator
 * 
 *
 */


/**
 * TODO LIST ACTUAL --> 
 * COMPLETADO 1.- Termianr de remover estrucruras que generen agua vanilla (WorldGenLiquids, //BiomeSwamp, etc)
 * COMPLETADO 2.- A WEBO Checar imcompatibilidades (actualmente seria con la IC2 FluidCell, los tanques de BuildCraft, y la generacion realista de ClimaticBiomes)
 * COMPLETADO 3.- Ver como hacer que en los biomas donde (con agua vanilla) aparece hielo en la superficie, pues esto mismo funcione con mis bloques, debo buscar como lo hace vanilla normalmente
 * COMPLETADO 4.- Evitar que la evaporacion de agua sea activa mientras llueve
 * 5.- Ver que onda con las cubetas de PrimalCore y el Brew Kettle de Growthcraft (porque no funciona a pesar del universal compat coremod)
 * COMPLETADO 6.- Arreglar texturas del flowing water
 * 
 */




//TODO --> 
// COMPLETADO 1) Volver a hacer el DataFixer ()
// COMPLETADO 2) Hacer unaa lista de todos los Fluids (que aparezca en config, para una referencia estatica de todos los Fluids)
// COMPLETADO 3) Arreglar los remaps de los Mixins

// COMPLETADO 4) Arreglar el bug visual del agua (hasta el final, me vale verga)
// 5) Arreglar la textura de los items de cada fluid

// 6) Arreglar compat con FluidLogged API (mixin aa su clase que hace referencia a LEVELs y CORNER_LEVELS paraa usar Reference.LEVEL)
// 7) Arreglar un bug visual con los bloques de fluidos cuando tienen a otro bloque de fluido al lado
// 8) IMPLEMENTAR net.minecraftforge.event.ForgeEventFactory.fireFluidPlaceBlockEvent(worldIn, blockpos, pos, Blocks.FIRE.getDefaultState()));
//worldIn.setBlockState(pos.down(), net.minecraftforge.event.ForgeEventFactory.fireFluidPlaceBlockEvent(worldIn, pos.down(), pos, Blocks.STONE.getDefaultState()));
//JAVA SEARCH --> net.minecraftforge.event.ForgeEventFactory.firePlayerLoadingEvent(net.minecraft.entity.player.EntityPlayer, net.minecraft.world.storage.IPlayerFileData, java.lang.String)


@Mod(modid = References.MODID, name = References.NAME, version = References.VERSION)
public class RealisticFiniteFluids
{
	
	@Instance
	public static RealisticFiniteFluids instance;
	
	@SidedProxy(clientSide = References.CLIENT_PROXY_CLASS, serverSide = References.COMMON_PROXY_CLASS)
	public static CommonProxy proxy;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        //System.out.println("[RFF] CHECK PreInit");
        ModFixs modFixs = FMLCommonHandler.instance().getDataFixer().init(References.MODID, References.FIXER_VERSION);
        modFixs.registerFix(FixTypes.CHUNK, new RFFFluidFixer());
        
    	MinecraftForge.EVENT_BUS.register(FluidEventHandler.class);
    	MinecraftForge.EVENT_BUS.register(FluidModelEventHandler.class);
        ModConfig.loadConfig(event.getSuggestedConfigurationFile());
        FiniteFluidLogic.initFiniteFluidVariables();
        
        
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        //System.out.println("[RFF] CHECK Init");
    	
    }
    
    @EventHandler
    public void postInit(FMLInitializationEvent event) {
        //System.out.println("[RFF] CHECK PostInit");
    }
    
}
