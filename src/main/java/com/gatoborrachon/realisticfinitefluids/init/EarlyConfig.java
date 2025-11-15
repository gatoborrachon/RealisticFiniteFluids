package com.gatoborrachon.realisticfinitefluids.init;

import java.io.BufferedReader;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.nio.charset.StandardCharsets;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EarlyConfig {

    private static final Logger LOGGER = LogManager.getLogger("RealisticFiniteFluids");
    public static final String CONFIG_PATH = "config/realisticfinitefluids.cfg";

	
	public static String readBarrierMode() {
        try {
            File file = new File(CONFIG_PATH);
            //LOGGER.info("¿EL ARCHIVO EXISTE? {}", file.exists());
            if (!file.exists()) return "optimized";
            for (String line : Files.readAllLines(file.toPath())) {
                //LOGGER.info("LINES DEL ARCHIVO:" +line);
                if (line.startsWith("    S:oceanBarrierMode")) {
                    //LOGGER.info("ENCONTRADO oceanBarrierMode en el archivo");
                    return line.split("=")[1].trim();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "optimized";
    }
    
	public static String readReplacerMode() {
        try {
            File file = new File(CONFIG_PATH);
            //LOGGER.info("¿EL ARCHIVO EXISTE? {}", file.exists());
            if (!file.exists()) return "false";
            for (String line : Files.readAllLines(file.toPath())) {
                //LOGGER.info("LINES DEL ARCHIVO:" +line);
                if (line.startsWith("    B:replaceVanillaFluids")) {
                    //LOGGER.info("ENCONTRADO replaceVanillaFluids en el archivo");
                    return line.split("=")[1].trim();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "false";
    }
    
	public static String readUniversalCompat() {
        try {
            File file = new File(CONFIG_PATH);
            //LOGGER.info("¿EL ARCHIVO EXISTE? {}", file.exists());
            if (!file.exists()) return "false";
            for (String line : Files.readAllLines(file.toPath())) {
                //LOGGER.info("LINES DEL ARCHIVO:" +line);
                if (line.startsWith("    B:universalCompat")) {
                    //LOGGER.info("ENCONTRADO universalCompat en el archivo");
                    return line.split("=")[1].trim();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "false";
    }
    
    public static boolean readUniversalCompatDebug() {
        try {
            File file = new File(CONFIG_PATH);
            //LOGGER.info("¿EL ARCHIVO EXISTE? {}", file.exists());
            if (!file.exists()) return false;

            for (String line : Files.readAllLines(file.toPath())) {
                //LOGGER.info("LINES DEL ARCHIVO:" +line);
                if (line.trim().startsWith("B:debugUniversalCompatLog")) {
                    //LOGGER.info("ENCONTRADO debugUniversalCompatLog en el archivo");
                    String value = line.split("=")[1].trim();
                    return Boolean.parseBoolean(value);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    
    
    public static List<String> readExclusions() {
        List<String> defaults = Arrays.asList(
        		//NO REMOVER
                "ic2.core.IC2",
                "ic2.core.block.BlockIC2Fluid",
                "ic2.core.init.BlocksItems",
        		"appeng.core.api.definitions.ApiItems",
        		"appeng.items.tools.powered.ToolEntropyManipulator",
        		"cassiokf.industrialrenewal.blocks.BlockGutter",
        		"cassiokf.industrialrenewal.blocks.BlockSteamBoiler",
                "cassiokf.industrialrenewal.blocks.BlockMining",
                //"net.msrandom.witchery.init.items.WitcheryGeneralItems",
                //"buildcraft.lib.fluid.BCFluidBlock",
                "buildcraft.api.enums.EnumSpring",
                //"com.endertech.minecraft.mods.adpother.sources.LavaMixingWater",
                //"mods.railcraft.client.render.models.resource.FluidModelRenderer",
                //"jaredbgreat.climaticbiome.biomes.basic.ActiveVolcano",
                
                
                
                
                // TODO --> Eliminar el bloque oceanico, y de ahi descomentar estas 3 lineas de codigo que siguen, y comentar la de net.minecraft
                
                //PARA EVITAR CRASHES ESPERABLES
                //"net.minecraft.block", //.blockLiquid", //No creo que pase nada malo pero igual no es que me importe modificar esta clase
                //"net.minecraft.client.renderer", //Creo que hay varias cosillas que no quisiera tocar aca jeje
                //"net.minecraft.init", //.Blocks", //Si excluyo esto, se ha de morir todo xd
                //"net.minecraft.world.gen.ChunkGeneratorFlat", //TODO --> Mis mixins ya modifican gran parte de este cuete, falta verificar isOceanBlock de MapGenRavines y MapGenCaves, no tengo ni idea de que hagan esas cosas jaja
                //"net.minecraft.world.gen.ChunkGeneratorOverworld", 
                //"net.minecraft.world.gen.MapGenCaves", 
                //"net.minecraft.world.gen.MapGenRavine", 

                //"net.minecraft.stats", //No se si sea util/bueno no excluir esto
                //"net.minecraftforge",

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
        );

        //File file = new File(CONFIG_PATH);
        Path path = Paths.get(CONFIG_PATH);
        //LOGGER.info("¿EL ARCHIVO EXISTE? {}", !Files.exists(path));
        if (!Files.exists(path)) {
            return defaults;
        }

        try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line;
            while ((line = br.readLine()) != null) {
                String s = line.trim();
                //LOGGER.info("LINES DEL ARCHIVO:" +line);
                if (s.isEmpty() || s.startsWith("#")) continue;

                // Soportar la clave correcta y variantes antiguas por compat
                if (s.startsWith("S:exclusionsPrefixes")) {
                    //LOGGER.info("ENCONTRADO exclusionsPrefixes en el archivo");

                    // Caso A: multilinea
                    if (s.contains("<")) {
                        List<String> collected = new ArrayList<>();
                        // Si hay algo después del '<' en la misma línea, ignoramos (no es común)
                        // Leemos siguientes líneas hasta encontrar '>' de cierre
                        while ((line = br.readLine()) != null) {
                            String item = line.trim();
                            if (item.equals(">")) break;
                            if (item.isEmpty() || item.startsWith("#")) continue;
                            collected.add(item);
                            LOGGER.info("Exception added: "+item);
                        }
                        if (!collected.isEmpty()) return collected;
                        // si estaba vacía, seguir buscando (fallback)
                    } else {
                        // Caso B: inline: S:exclusionsPrefixes=[a, b, c] o S:exclusionsPrefixes = [a,b]
                        int eq = s.indexOf('=');
                        if (eq >= 0) {
                            String raw = s.substring(eq + 1).trim();
                            // quitar corchetes/chevrons si están
                            raw = raw.replaceAll("[\\[\\]<>]", "");
                            String[] parts = raw.split(",");
                            List<String> out = Arrays.stream(parts)
                                                     .map(String::trim)
                                                     .filter(x -> !x.isEmpty())
                                                     .collect(Collectors.toList());
                            if (!out.isEmpty()) return out;
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            // en caso de error, devolvemos defaults para no romper el coremod
        }

        return defaults;
    }
	
}
