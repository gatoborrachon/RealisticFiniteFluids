package com.gatoborrachon.realisticfinitefluids.util;

import net.minecraft.block.Block;
import net.minecraft.nbt.*;
import net.minecraft.util.datafix.IFixableData;
import net.minecraft.world.chunk.NibbleArray;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.Constants;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.gatoborrachon.realisticfinitefluids.References;
import com.gatoborrachon.realisticfinitefluids.blocks.BlockFiniteFluid;

public class RFFFluidFixer implements IFixableData {

	File worldDir = null;
	
    // --- BLOQUES A REEMPLAZAR ---
    private static final Map<String,String> VANILLA_TO_RFF = new HashMap<>();
    private static final Map<String,String> OCEAN_TO_STILL = new HashMap<>();
    

    static {
        // 1. Vanilla --> tus fluidos
        VANILLA_TO_RFF.put("minecraft:water",            "realisticfinitefluids:finite_water_still");
        VANILLA_TO_RFF.put("minecraft:flowing_water",    "realisticfinitefluids:finite_water_flowing");
        VANILLA_TO_RFF.put("minecraft:lava",             "realisticfinitefluids:finite_lava_still");
        VANILLA_TO_RFF.put("minecraft:flowing_lava",     "realisticfinitefluids:finite_lava_flowing");

        // 2. Oceanic (los viejos infinitos)
        restartOceanToStillRemap();
    }
    public static void restartOceanToStillRemap() {
        OCEAN_TO_STILL.put("realisticfinitefluids:infinite_water_source", "realisticfinitefluids:finite_water_still");
        OCEAN_TO_STILL.put("realisticfinitefluids:infinite_lava_source",  "realisticfinitefluids:finite_lava_still");
    }
	
	
    // --- VERSION DEL FIXER ---
    // Incrementa este número cada que hagas un cambio
    @Override
    public int getFixVersion() {
        //OCEAN_TO_STILL.clear();
        //OCEAN_TO_STILL.put("realisticfinitefluids:infinite_water_source", "realisticfinitefluids:finite_water_still");
        //OCEAN_TO_STILL.put("realisticfinitefluids:infinite_lava_source",  "realisticfinitefluids:finite_lava_still");
    	//readedNBT = false;
        return References.FIXER_VERSION;
    }

    @Override
    public NBTTagCompound fixTagCompound(NBTTagCompound compound) {
        //System.out.println("COMPOUND: "+compound);
        if (!compound.hasKey("Level", Constants.NBT.TAG_COMPOUND)) return compound;  
        NBTTagCompound level = compound.getCompoundTag("Level");
        //System.out.println("LEVEL: "+level);

        if (!level.hasKey("Sections", Constants.NBT.TAG_LIST)) return compound;
        NBTTagList sections = level.getTagList("Sections", Constants.NBT.TAG_COMPOUND);
        //System.out.println("SECTION: "+sections);

        //System.out.println("VERGA");
        if (DimensionManager.getCurrentSaveRootDirectory() != this.worldDir) checkForOceanBlocksIDs();
        for (int i = 0; i < sections.tagCount(); i++) {
            fixSection(sections.getCompoundTagAt(i));
        }
        return compound;
    }


    
    private void fixSection(NBTTagCompound section) {
        //System.out.println("SECTION2: "+section);
        if (!section.hasKey("Blocks", Constants.NBT.TAG_BYTE_ARRAY)) return;
        byte[] blocks = section.getByteArray("Blocks");
        NibbleArray add = section.hasKey("Add") ? new NibbleArray(section.getByteArray("Add")) : null;
        NibbleArray data = new NibbleArray(section.getByteArray("Data"));
        

        for (int i = 0; i < blocks.length; i++) {
            //int blockId = blocks[i] & 0xFF;
            int low = blocks[i] & 0xFF;
            int high = add != null ? add.getFromIndex(i) : 0;
            int blockId = (high << 8) | low;
            
            Block block = Block.getBlockById(blockId);
        	//System.out.println("MODIFICANDO BLOCK "+block);
        	//System.out.println("MODIFICANDO DATA "+data.toString());
        	//System.out.println(" ");
        	
            if (block == null) continue;

            String name = block.getRegistryName().toString();

            int x = i & 0xF;
            int y = (i >> 8) & 0xF;
            int z = (i >> 4) & 0xF;


            
            // ================================
            // 1. REEMPLAZAR BLOQUES VANILLA
            // ================================
            if (VANILLA_TO_RFF.containsKey(name)) {
                Block newBlock = Block.getBlockFromName(VANILLA_TO_RFF.get(name));
                if (newBlock != null) {
                    blocks[i] = (byte) Block.getIdFromBlock(newBlock);
                    data.set(x, y, z, BlockFiniteFluid.MAXIMUM_CONCEPTUAL_LEVEL);
                    continue;
                }
            }

            // ================================
            // 2. REEMPLAZAR BLOQUES OCEANICOS
            // ================================
        	String blockIdStr = String.valueOf(blockId);
            /*if (OCEAN_TO_STILL.containsKey(blockIdStr)) {

                Block newBlock = Block.getBlockFromName(OCEAN_TO_STILL.get(blockIdStr));
                if (newBlock != null) {
                    blocks[i] = (byte) Block.getIdFromBlock(newBlock);
                    data.set(x, y, z, BlockFiniteFluid.MAXIMUM_CONCEPTUAL_LEVEL);
                    continue;
                }
            }*/
            
        	if (OCEAN_TO_STILL.containsKey(blockIdStr)) {
                Block newBlock = Block.getBlockFromName(OCEAN_TO_STILL.get(blockIdStr));
                if (newBlock != null) {
                    int newId = Block.getIdFromBlock(newBlock);

                    // -------------------------
                    // 3. Reinsertar newId en Blocks + Add
                    // -------------------------
                    blocks[i] = (byte) (newId & 0xFF);

                    if (add != null) add.set(x, y, z,  (newId >> 8) & 0xF);
                    // -------------------------
                    // 4. Force LEVEL = 8
                    // -------------------------
                    data.set(x, y, z, BlockFiniteFluid.MAXIMUM_CONCEPTUAL_LEVEL);

                    continue;
                }
        	}
        
        
            
            // ================================
            // 3. REMAPEAR LEVELS 0–15 --> 0–7
            // ================================
            if (block instanceof BlockFiniteFluid) {
                int oldLevel = data.get(x, y, z);
                int newLevel = remapLevel(oldLevel);
                data.set(x, y, z, newLevel);
            }
        }

        section.setByteArray("Blocks", blocks);
        section.setByteArray("Data", data.getData());
    }
    
    private void checkForOceanBlocksIDs() {
    	restartOceanToStillRemap();
    	this.worldDir = DimensionManager.getCurrentSaveRootDirectory();
    	File levelDat = new File(worldDir, "level.dat");
    	NBTTagCompound level = null;
    	
		try {
			level = CompressedStreamTools.readCompressed(new FileInputStream(levelDat));
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (level == null) return;
    	NBTTagCompound fml = level.getCompoundTag("FML");
    	NBTTagCompound registries = fml.getCompoundTag("Registries");
    	NBTTagCompound blocks = registries.getCompoundTag("minecraft:blocks");
    	NBTBase idsBase = blocks.getTag("ids");

        //System.out.println("[RFF] KEYSET" +blocks.getKeySet() + " VERGA");
        //System.out.println("[RFF] KEYSET" +ids.getKeySet() + " VERGA");
    	
        // Nuevo mapa ID (String) -> nombre del bloque final
        Map<String, String> oceanIDsToStill = new HashMap<>();
        
        
        if (idsBase instanceof NBTTagList) {
            NBTTagList idsList = (NBTTagList) idsBase;

            for (int i = 0; i < idsList.tagCount(); i++) {
                NBTTagCompound entry = idsList.getCompoundTagAt(i);

                if (entry.hasKey("K") && entry.hasKey("V")) {
                    String name = entry.getString("K");
                    int id = entry.getInteger("V");

                    //System.out.println("[RFF] Bloque: " + name + " => ID: " + id);
                    
                    // Aquí puedes llenar tu map OCEAN_TO_STILL por ID
                    if (OCEAN_TO_STILL.containsKey(name)) {
                        int blockId = id;
                        String newBlockName = OCEAN_TO_STILL.get(name);
                        //OCEAN_TO_STILL_BY_ID.put(blockId, newBlockName);
                        oceanIDsToStill.put(String.valueOf(blockId), newBlockName);
                        System.out.println("[RFF] Remapeando ID antiguo: " + blockId + " -> " + newBlockName);
                    }
                }
            }
        } 

        // Sobrescribimos OCEAN_TO_STILL para que ahora use IDs
        OCEAN_TO_STILL.clear();
        OCEAN_TO_STILL.putAll(oceanIDsToStill);
        for (String key : OCEAN_TO_STILL.keySet()) {
        	System.out.println("KEY FINAL "+key);
        	System.out.println("VALUE FINAAL "+OCEAN_TO_STILL.get(key));
        }
        //System.out.println("VERGA: "+OCEAN_TO_STILL);
    }


    // ================================
    // FUNCION DE REMAPEADO
    // ================================
    private int remapLevel(int old) {
        if (old < 0) old = 0;
        if (old > 15) old = 15;

        return old / 2;
    }
}
