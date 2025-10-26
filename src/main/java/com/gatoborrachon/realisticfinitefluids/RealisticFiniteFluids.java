package com.gatoborrachon.realisticfinitefluids;

import com.gatoborrachon.realisticfinitefluids.events.FluidEventHandler;
import com.gatoborrachon.realisticfinitefluids.events.FluidModelEventHandler;
import com.gatoborrachon.realisticfinitefluids.init.ModConfig;
import com.gatoborrachon.realisticfinitefluids.proxy.CommonProxy;

import net.minecraftforge.common.MinecraftForge;
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
 * 3.- Ver como hacer que en los biomas donde (con agua vanilla) aparece hielo en la superficie, pues esto mismo funcione con mis bloques, debo buscar como lo hace vanilla normalmente
 * COMPLETADO 4.- Evitar que la evaporacion de agua sea activa mientras llueve
 * 5.- Ver que onda con las cubetas de PrimalCore y el Brew Kettle de Growthcraft (porque no funciona a pesar del universal compat coremod)
 * COMPLETADO 6.- Arreglar texturas del flowing water
 * 
 */

@Mod(modid = References.MODID, name = References.NAME, version = References.VERSION)
public class RealisticFiniteFluids
{
	
	
    //private static Logger logger;
    
	@Instance
	public static RealisticFiniteFluids instance;
	
	@SidedProxy(clientSide = References.CLIENT_PROXY_CLASS, serverSide = References.COMMON_PROXY_CLASS)
	public static CommonProxy proxy;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
    	MinecraftForge.EVENT_BUS.register(FluidEventHandler.class);
    	MinecraftForge.EVENT_BUS.register(FluidModelEventHandler.class);
        ModConfig.loadConfig(event.getSuggestedConfigurationFile());
       
        if (ModConfig.replaceVanillaFluids) {
            // Corre tu mixin de reemplazo de agua/lava
            System.out.println("[RFF] Replacing vanilla fluids...");
            
            // Una vez terminado, desactiva la flag en el config
            ModConfig.replaceVanillaFluids = false;
            ModConfig.saveConfig(); // Método que guardaría el config actualizado en disco
            System.out.println("[RFF] Vanilla Fluids Replaced. replaceVanillaFluids is now false.");
        }
               
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
    	
    	

    }
    
    @EventHandler
    public void postInit(FMLInitializationEvent event)
    {
        //VanillaWaterOverride.overrideWaterBlock(ModBlocks.FINITE_WATER_FLOWING);

    }
    
    //public static final ThreadLocal<Boolean> IS_POPULATING = ThreadLocal.withInitial(() -> false);


    


    
}
