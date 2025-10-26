package com.gatoborrachon.realisticfinitefluids.init;

import net.minecraftforge.common.config.Configuration;
import java.io.File;

public class ModConfig {
    public static int waterTickRate;
    public static int lavaTickRate;
    public static int lakelimit;
    public static int maxCalc;
    public static int playerMaxDistanceToCalc;
    public static boolean enableRain;
    public static int rainAmount;
    public static int rainArea;
    public static boolean scalableRainMethod;
    public static float rainNewMethodAmount;
    public static boolean enableEvaporation;
    public static int evaporationChance;
    public static String oceanBarrierMode;
    public static boolean bucketRemoveLowFluid;
    public static boolean debug;
    public static boolean debugUniversalCompatLog;
    public static boolean waterCanFreeze;
    public static int waterLightOpacity;
    public static boolean replaceVanillaFluids;
    public static boolean doPressure;
    public static boolean universalCompat;
    public static String[] exclusionsPrefixes;
    public static boolean flowingWaterShouldMoveCreativePlayer;
    public static boolean shouldTickRandomly;


    private static final String CATEGORY_GENERAL = "general";
    static Configuration config;
    
    
    public static void loadConfig(File configFile) {
        config = new Configuration(configFile);
        config.load();

        //Tickrates
        waterTickRate = config.getInt("waterTickRate", CATEGORY_GENERAL, 8, 0, Integer.MAX_VALUE,
                "Water tick rate. 4 = Original, 8 = Default.");

        lavaTickRate = config.getInt("lavaTickRate", CATEGORY_GENERAL, 30, 1, Integer.MAX_VALUE,
                "Lava tick rate. 24 = Original, 30 = Default.");
        
        
        //Fluid Logic
        lakelimit = config.getInt("lakeLimit", CATEGORY_GENERAL, 512, 1, Integer.MAX_VALUE, 
        		"Minimum quantity of fluid blocks to consider a fluid mass as an ocean. 2048 = Original. 512 = Default.");
        
        maxCalc = config.getInt("maxCalc", CATEGORY_GENERAL, 1024, 1, Integer.MAX_VALUE, 
        		"Max number of finite fluid calculations every tick. A cap to (try) avoid holding the server.");
        
        playerMaxDistanceToCalc = config.getInt("playerMaxDistanceToCalc", CATEGORY_GENERAL, 1024, 1, Integer.MAX_VALUE, 
        		"Maximum distance between a flowing liquid and a player to decide whether postpone or update now the block at the current tick when currentCalc is starting to get closer to maxCalc.");
        
        
        //Rain Logic
        enableRain = config.getBoolean("enableRain", CATEGORY_GENERAL, true, 
        		"Enable finite water rain.");
        
        rainAmount = config.getInt("rainAmount", CATEGORY_GENERAL, 24, 1, Integer.MAX_VALUE, 
        		"if scalableRainMethod is FALSE: The probability of a water drop to fall per tick when raining. 1 = 100%, 2 = 50%, 100 = 1%. 24 = Default.");
        
        rainArea = config.getInt("rainArea", CATEGORY_GENERAL, 32, 1, Integer.MAX_VALUE, 
        		"if scalableRainMethod is FALSE: The diameter in blocks of the area where water drops should fall. Original = 16. Default = 32.");
        
        scalableRainMethod = config.getBoolean("scalableRainMethod", CATEGORY_GENERAL, true, 
        		"Whether to use the 'render distance-oriented method' or the 'player-oriented method' to place rain water.");
        
        rainNewMethodAmount = config.getFloat("rainNewMethodAmount", CATEGORY_GENERAL, 1f, 0.1f, 24f, 
        		"if scalableRainMethod is TRUE: a proportional modifier to the probability of a water drop to fall per tick when raining. values bigger than 24 won't increase the rain amount. It can't be zero, but 0.1 is valid.");
        
        //Pressure Logic
        doPressure = config.getBoolean("doPressure", CATEGORY_GENERAL, true, 
        		"Whether to activate the pressure system.");
        
        //Evaporation Logic
        enableEvaporation = config.getBoolean("enableEvaporation", CATEGORY_GENERAL, true,
                "If water should evaporate. Only applies to water with the minimum level of fluid.");
        
        evaporationChance = config.getInt("evaporationChance", CATEGORY_GENERAL, 100, 1, Integer.MAX_VALUE,
                "Probability of water evaporation per tick. 1 = 100%, 2 = 50%, 100 = 1%. 100 = Default.");
        
        
        //Misc Logic
        waterCanFreeze = config.getBoolean("waterCanFreeze", CATEGORY_GENERAL, true, 
        		"whether water can become ice.");
        
        oceanBarrierMode = config.getString("oceanBarrierMode", CATEGORY_GENERAL, "optimized",
                "Ocean barrier method (to avoid leaving openings between underwater mines and the ocean): none, optimized, unoptimized.");

        bucketRemoveLowFluid = config.getBoolean("bucketRemoveLowFluid", CATEGORY_GENERAL, false, 
        		"Whether buckets should remove finite fluids on the world when there's not enough fluid to make a full bucket.");
        
        waterLightOpacity = config.getInt("waterLightOpacity", CATEGORY_GENERAL, 1, 0, Integer.MAX_VALUE, 
        		"The amount of light the water will remove when it passes through it. 3 = Vanilla, 1 = Default.");
        
        replaceVanillaFluids = config.getBoolean("replaceVanillaFluids", CATEGORY_GENERAL, false, 
        		"Whether to replace vanilla fluids from already existing worlds or not. This option auto-sets to false when manually set to true (affects game 1 time before resetting itself to false).");
        
        universalCompat = config.getBoolean("universalCompat", CATEGORY_GENERAL, false, 
        		"Enables the Universal Compat Coremod");
        
        exclusionsPrefixes = config.getStringList("exclusionsPrefixes", CATEGORY_GENERAL, new String[] {
        		//NO REMOVER
                "ic2.core.IC2",
                "ic2.core.block.BlockIC2Fluid",
                "ic2.core.init.BlocksItems",
        		"appeng.core.api.definitions.ApiItems",
        		"appeng.items.tools.powered.ToolEntropyManipulator",
        		"cassiokf.industrialrenewal.blocks.BlockGutter",
                //"net.msrandom.witchery.init.items.WitcheryGeneralItems",
                //"buildcraft.lib.fluid.BCFluidBlock",
                "buildcraft.api.enums.EnumSpring",
                //"com.endertech.minecraft.mods.adpother.sources.LavaMixingWater",
                //"mods.railcraft.client.render.models.resource.FluidModelRenderer",
                //"jaredbgreat.climaticbiome.biomes.basic.ActiveVolcano",
                
                
                // TODO --> Eliminar el bloque oceanico, y de ahi descomentar estas 3 lineas de codigo que siguen, y comentar la de net.minecraft
                
                //PARA EVITAR CRASHES ESPERABLES
                //"net.minecraft.block.blockLiquid", //No creo que pase nada malo pero igual no es que me importe modificar esta clase
                //"net.minecraft.client.renderer", //Creo que hay varias cosillas que no quisiera tocar aca jeje
                //"net.minecraft.init.Blocks", //Si excluyo esto, se ha de morir todo xd
                
                //"net.minecraft.stats", //No se si sea util/bueno no excluir esto
                ////"net.minecraftforge.event.ForgeEventFactory", //Igual y estos 2 nunca fueron evitados, asi que los quito de la lista de esclusiones jeje
                ////"net.minecraftforge.fluids.FluidRegistry",

                "net.minecraft",
                "com.gatoborrachon",
                
                
                //PARA NO HACER LOG DE MAS
                "com.llamalad7",
                "it.unimi",
                "crafttweaker",
                "gnu.trove",
                "io.netty",
                "zone.rong",
                "com.google",
                "com.ibm",
                "com.sun",
                "joptsimple",
                "com.mojang",
                "stanhebben",
                "kotlin",
                "com.therandomlabs",
                "fionathemortal.betterbiomeblend",
                "net.optifine",
                "net.malisis",
                "meldexun",
                "io.github.lxgaming.sledgehammer",
                "customskinloader",
                "org.joml.Vector3d",
                "com.cout970.modelloader",
                "paulscode",
                "oshi",
                "com.jcraft"
            	}, "Path prefixes to exclud from the coremod. To exclude a class, add the full path to that class. To exclude an entire package, just add the initial path.");
        		
        flowingWaterShouldMoveCreativePlayer = config.getBoolean("flowingWaterShouldMoveCreativePlayer", CATEGORY_GENERAL, true, 
        		"Whether flowing liquids should move players on creative mode.");
        
        shouldTickRandomly = config.getBoolean("shouldTickRandomly", CATEGORY_GENERAL, true, 
        		"Whether fluids should have random ticks.");
        
        //Debug
        debug = config.getBoolean("debug", CATEGORY_GENERAL, false, 
        		"Allow debug content. Only for testing purposes.");

        debugUniversalCompatLog = config.getBoolean("debugUniversalCompatLog", CATEGORY_GENERAL, false, 
        		"Enables the needed debug logs to find guilt patches made by universalCompat config option.");
        
        
        if (config.hasChanged()) {
            config.save();
        }
    }
    
    public static void saveConfig() {
        config.getCategory("general").get("replaceVanillaFluids").set(replaceVanillaFluids);
        config.save();
    }
}
