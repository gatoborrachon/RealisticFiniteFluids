package com.gatoborrachon.realisticfinitefluids.compat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class FluidIndexConfig {

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    private static final String FILE_NAME = "rff_fluid_indices.json";

	/**
	 * Lista de los fluidos almacenados en la config de cada mundo.
	 */
    public Map<String, Integer> storedFluidIndexes = new HashMap<>();
	/**
	 * Lista de indices libres.
	 */
    public List<Integer> freeIndexes = new ArrayList<>();

    // ------------------------------------------------------------
    // LOAD
    // ------------------------------------------------------------
    public static FluidIndexConfig load(World world) {

        File file = getFile(world);

        if (!file.exists()) {
            return new FluidIndexConfig(); // archivo nuevo
        }

        try (Reader reader = new FileReader(file)) {

            Type type = new TypeToken<FluidIndexConfig>() {}.getType();
            FluidIndexConfig cfg = GSON.fromJson(reader, type);

            if (cfg.storedFluidIndexes == null)
                cfg.storedFluidIndexes = new HashMap<>();

            if (cfg.freeIndexes == null)
                cfg.freeIndexes = new ArrayList<>();

            return cfg;

        } catch (Exception e) {
            e.printStackTrace();
            return new FluidIndexConfig(); // fallback seguro
        }
    }

    // ------------------------------------------------------------
    // SAVE
    // ------------------------------------------------------------
    public void save(World world) {
        normalizeFreeIndexes();
        sortStoredFluidIndexes();

        File file = getFile(world);

        try {
            file.getParentFile().mkdirs();

            try (Writer writer = new FileWriter(file)) {
                GSON.toJson(this, writer);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ------------------------------------------------------------
    // INDEX UTILITIES
    // ------------------------------------------------------------
    public int getNextIndex() {
        return storedFluidIndexes.values()
                .stream()
                .max(Integer::compareTo)
                .orElse(-1) + 1;
    }
    
    public void normalizeFreeIndexes() {
        Set<Integer> unique = new TreeSet<>(freeIndexes);
        freeIndexes.clear();
        freeIndexes.addAll(unique);
    }
    
    public void sortStoredFluidIndexes() {
        this.storedFluidIndexes = this.storedFluidIndexes.entrySet()
            .stream()
            .sorted(Map.Entry.comparingByValue()) // por índice
            .collect(
                LinkedHashMap::new,
                (m, e) -> m.put(e.getKey(), e.getValue()),
                Map::putAll
            );
    }

    // ------------------------------------------------------------
    // FILE LOCATION
    // ------------------------------------------------------------
    private static File getFile(World world) {

        File worldDir;

        if (world != null) {
            worldDir = world.getSaveHandler().getWorldDirectory();
        } else {
            // fallback raro, pero seguro
            worldDir = DimensionManager.getCurrentSaveRootDirectory();
        }

        return new File(worldDir, "data/" + FILE_NAME);
    }
}
