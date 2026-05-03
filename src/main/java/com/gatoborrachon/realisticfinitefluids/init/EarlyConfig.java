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

    public static boolean readTickRandomly() {
        try {
            File file = new File(CONFIG_PATH);
            //LOGGER.info("¿EL ARCHIVO EXISTE? {}", file.exists());
            if (!file.exists()) return true;

            for (String line : Files.readAllLines(file.toPath())) {
                //LOGGER.info("LINES DEL ARCHIVO:" +line);
                if (line.trim().startsWith("B:shouldTickRandomly")) {
                    //LOGGER.info("ENCONTRADO shouldTickRandomly en el archivo");
                    String value = line.split("=")[1].trim();
                    return Boolean.parseBoolean(value);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }
    

    static List<String> infiniteModdedFluids = null;
    
    public static List<String> readInfiniteModdedFluids() {
    	if (infiniteModdedFluids != null) return infiniteModdedFluids;
 	   
    	infiniteModdedFluids = Arrays.asList(/*Should be empty*/);
    	
        //File file = new File(CONFIG_PATH);
        Path path = Paths.get(CONFIG_PATH);
        //LOGGER.info("¿EL ARCHIVO EXISTE? {}", !Files.exists(path));
        if (!Files.exists(path)) {
            return infiniteModdedFluids;
        }

        try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line;
            while ((line = br.readLine()) != null) {
                String s = line.trim();
                //LOGGER.info("LINES DEL ARCHIVO:" +line);
                if (s.isEmpty() || s.startsWith("#")) continue;

                // Soportar la clave correcta y variantes antiguas por compat
                if (s.startsWith("S:infiniteModdedFluids")) {
                    //LOGGER.info("ENCONTRADO infiniteModdedFluids en el archivo");

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
                            //LOGGER.info("Infinite modded fluid added: "+item);
                        }
                        if (!collected.isEmpty()) return collected;
                        // si estaba vacía, seguir buscando (fallback)
                    } else {
                        // Caso B: inline: S:infiniteModdedFluids=[a, b, c] o S:infiniteModdedFluids = [a,b]
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
            // en caso de error, devolvemos infiniteModdedFluids (que deberia estar vacio)
        }

        return infiniteModdedFluids;
    }
	
}
