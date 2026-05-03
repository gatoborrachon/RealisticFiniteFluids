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
    //public static int rainAmount;
    //public static int rainArea;
    //public static boolean scalableRainMethod;
    //public static float rainNewMethodAmount;
    public static float rainAmount;
    public static boolean enableEvaporation;
    public static int evaporationChance;
    public static boolean bucketRemoveLowFluid;
    public static boolean visualDebug;
    public static boolean logDebug;
    public static boolean waterCanFreeze;
    public static int waterLightOpacity;
    public static boolean doPressure;
    public static boolean flowingWaterShouldMoveCreativePlayer;
    public static boolean shouldTickRandomly;
    
    public static boolean shouldFluidsBeInfinite;
    
    public static boolean createBlocksForBlocklessFluids;
    public static int pressureLimit;
    
    public static String[] infiniteModdedFluids;
    
    public static boolean maxOrNormalFluidHeight;
    public static boolean dynamicOrStaticTexture;
    

    private static final String CATEGORY_GENERAL = "general";
    static Configuration config;
    
    
    public static void loadConfig(File configFile) {
        config = new Configuration(configFile);
        config.load();

        //Tickrates
        waterTickRate = config.getInt("waterTickRate", CATEGORY_GENERAL, 8, 0, Integer.MAX_VALUE,
                "Water tick rate. 4 = Original, 8 = Default.");

        lavaTickRate = config.getInt("lavaTickRate", CATEGORY_GENERAL, 30, 0, Integer.MAX_VALUE,
                "Lava tick rate. 24 = Original, 30 = Default.");
        
        
        //Fluid Logic
        lakelimit = config.getInt("lakeLimit", CATEGORY_GENERAL, 512, 1, Integer.MAX_VALUE, 
        		"The amount of Ocean blocks that will turn into Still/Flowing blocks when you interact with them. 2048 = Original. 512 = Default.");
        
        maxCalc = config.getInt("maxCalc", CATEGORY_GENERAL, 1024, 1, Integer.MAX_VALUE, 
        		"Max number of finite fluid calculations every tick. A cap to (try) avoid holding the server.");
        
        playerMaxDistanceToCalc = config.getInt("playerMaxDistanceToCalc", CATEGORY_GENERAL, 1024, 1, Integer.MAX_VALUE, 
        		"Maximum distance between a flowing liquid and a player to decide whether postpone or update now the block at the current tick when currentCalc is starting to get closer to maxCalc.");
        
        
        //Rain Logic
        enableRain = config.getBoolean("enableRain", CATEGORY_GENERAL, true, 
        		"Enable finite water rain.");
        
        /*rainAmount = config.getInt("rainAmount", CATEGORY_GENERAL, 24, 1, Integer.MAX_VALUE, 
        		"if scalableRainMethod is FALSE: The probability of a water drop to fall per tick when raining. 1 = 100%, 2 = 50%, 100 = 1%. 24 = Default.");
        
        rainArea = config.getInt("rainArea", CATEGORY_GENERAL, 32, 1, Integer.MAX_VALUE, 
        		"if scalableRainMethod is FALSE: The diameter in blocks of the area where water drops should fall. Original = 16. Default = 32.");
        
        scalableRainMethod = config.getBoolean("scalableRainMethod", CATEGORY_GENERAL, true, 
        		"Whether to use the 'render distance-oriented method' or the 'player-oriented method' to place rain water.");
        */
        rainAmount = config.getFloat("rainAmount", CATEGORY_GENERAL, 1f, 0.1f, 24f, 
        		"A proportional modifier to the probability of a water drop to fall per tick when raining. values bigger than 24 won't increase the rain amount. It can't be zero, but 0.1 is valid.");
        
        //Pressure Logic
        doPressure = config.getBoolean("doPressure", CATEGORY_GENERAL, true, 
        		"Whether to activate the pressure system.");
        
        pressureLimit = config.getInt("pressureLimit", CATEGORY_GENERAL, 256, 0, Integer.MAX_VALUE, 
        		"The maximum tries we will check for blocks to apply pressure.");
        
        //Evaporation Logic
        enableEvaporation = config.getBoolean("enableEvaporation", CATEGORY_GENERAL, true,
                "If water should evaporate. Only applies to water with the minimum level of fluid.");
        
        evaporationChance = config.getInt("evaporationChance", CATEGORY_GENERAL, 10, 1, Integer.MAX_VALUE,
                "Probability of water evaporation per tick. 1 = 100%, 2 = 50%, 100 = 1%. 100 = Default.");
        
        
        //Misc Logic
        waterCanFreeze = config.getBoolean("waterCanFreeze", CATEGORY_GENERAL, true, 
        		"whether water can become ice.");
        
        bucketRemoveLowFluid = config.getBoolean("bucketRemoveLowFluid", CATEGORY_GENERAL, false, 
        		"Whether buckets should remove finite fluids on the world when there's not enough fluid to make a full bucket.");
        
        waterLightOpacity = config.getInt("waterLightOpacity", CATEGORY_GENERAL, 1, 0, Integer.MAX_VALUE, 
        		"The amount of light the water will remove when it passes through it. 3 = Vanilla, 1 = Default.");

        flowingWaterShouldMoveCreativePlayer = config.getBoolean("flowingWaterShouldMoveCreativePlayer", CATEGORY_GENERAL, true, 
        		"Whether flowing liquids should move players on creative mode.");
        
        shouldTickRandomly = config.getBoolean("shouldTickRandomly", CATEGORY_GENERAL, true, 
        		"Whether fluids should have random ticks.");
        
        shouldFluidsBeInfinite = config.getBoolean("shouldFluidsBeInfinite", CATEGORY_GENERAL, true, 
        		"Whether big fluid masses (lakes, oceans) should act as infinite fluid sources. It also works as a security switch to deactivate Ocean Blocks (set to false just in case you find a finite fluid eating your world [HOPE YOU NEVER HAVE TO DO THIS]).");
        
        createBlocksForBlocklessFluids = config.getBoolean("createBlocksForBlocklessFluids", CATEGORY_GENERAL, true, 
        		"Create finite fluid Blocks for Fluids that have no default Block assigned.");
        
        infiniteModdedFluids = config.getStringList("infiniteModdedFluids", CATEGORY_GENERAL, new String[] {}, 
        		"List of modded fluids' names that should spawn as infinite fluids. You can find the list of fluids inside your world inside: ´/.minecraft/saves/YOUR_WORLD_NAME/data´ ");
        
        maxOrNormalFluidHeight = config.getBoolean("maxOrNormalFluidHeight", CATEGORY_GENERAL, true, 
        		"If fluids should have the full block height or not. Only has aesthetic effect.");
        
        dynamicOrStaticTexture = config.getBoolean("dynamicOrStaticTexture", CATEGORY_GENERAL, true, 
        		"If fluids should use the dynamic fluid texture or not. Only has aesthetic effect.");
        
        
        
        
        //Debug
        visualDebug = config.getBoolean("visualDebug", CATEGORY_GENERAL, false, 
        		"Allow visualDebug content. Only for testing purposes.");

        logDebug = config.getBoolean("logDebug", CATEGORY_GENERAL, false, 
        		"Allow logDebug prints for special info about registerings.");

        
        if (config.hasChanged()) {
            config.save();
        }
    }

}
