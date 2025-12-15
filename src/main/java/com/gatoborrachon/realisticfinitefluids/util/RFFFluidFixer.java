package com.gatoborrachon.realisticfinitefluids.util;

import net.minecraft.block.Block;
import net.minecraft.nbt.*;
import net.minecraft.util.datafix.IFixableData;
import net.minecraft.world.chunk.NibbleArray;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.BlockFluidClassic;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.gatoborrachon.realisticfinitefluids.References;
import com.gatoborrachon.realisticfinitefluids.logic.FiniteFluidLogic;

public class RFFFluidFixer implements IFixableData {

	File worldDir = null;
	
    // --- BLOQUES A REEMPLAZAR ---
    private static final Map<String,String> RFF_TO_VANILLA = new HashMap<>();

    static {
    	restartRFFToVanillaRemap();
    }
    
    public static void restartRFFToVanillaRemap() {
    	RFF_TO_VANILLA.put("realisticfinitefluids:finite_water_still",                 "minecraft:water");
    	RFF_TO_VANILLA.put("realisticfinitefluids:finite_water_flowing",       "minecraft:flowing_water");
    	RFF_TO_VANILLA.put("realisticfinitefluids:finite_lava_still",                   "minecraft:lava");
    	RFF_TO_VANILLA.put("realisticfinitefluids:finite_lava_flowing",         "minecraft:flowing_lava");
    }
	
	
    // --- VERSION DEL FIXER ---
    // Incrementa este número cada que hagas un cambio
    @Override
    public int getFixVersion() {
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
        if (DimensionManager.getCurrentSaveRootDirectory() != this.worldDir) checkForFiniteFluidBlocksIDs();
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

            int x = i & 0xF;
            int y = (i >> 8) & 0xF;
            int z = (i >> 4) & 0xF;


            
            // ================================
            // 1. REEMPLAZAR BLOQUES RFF A VANILLA
            // ================================
        	String blockIdStr = String.valueOf(blockId);
        	if (RFF_TO_VANILLA.containsKey(blockIdStr)) {
                Block newBlock = Block.getBlockFromName(RFF_TO_VANILLA.get(blockIdStr));
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
                    data.set(x, y, z, References.MAXIMUM_CONCEPTUAL_LEVEL);

                    continue;
                }
        	}
        
            
            // ================================
            // 2. REMAPEAR LEVELS 7-0 a 0-7
            // ================================
            if (block instanceof BlockFluidClassic) {
                int oldLevel = data.get(x, y, z);
                int newLevel = remapLevel(oldLevel);
                data.set(x, y, z, newLevel);
            }
        }

        section.setByteArray("Blocks", blocks);
        section.setByteArray("Data", data.getData());
    }
    
    private void checkForFiniteFluidBlocksIDs() {
    	restartRFFToVanillaRemap();
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
        Map<String, String> FiniteFluidIDsToVanilla = new HashMap<>();
        
        
        if (idsBase instanceof NBTTagList) {
            NBTTagList idsList = (NBTTagList) idsBase;

            for (int i = 0; i < idsList.tagCount(); i++) {
                NBTTagCompound entry = idsList.getCompoundTagAt(i);

                if (entry.hasKey("K") && entry.hasKey("V")) {
                    String name = entry.getString("K");
                    int id = entry.getInteger("V");

                    //System.out.println("[RFF] Bloque: " + name + " => ID: " + id);
                    
                    // Aquí puedes llenar tu map RFF_TO_VANILLA por ID
                    if (RFF_TO_VANILLA.containsKey(name)) {
                        int blockId = id;
                        String newBlockName = RFF_TO_VANILLA.get(name);
                        FiniteFluidIDsToVanilla.put(String.valueOf(blockId), newBlockName);
                        if (FiniteFluidLogic.debug) System.out.println("[RFF] Remapeando ID antiguo: " + blockId + " -> " + newBlockName);
                    }
                }
            }
        }

        // Sobrescribimos OCEAN_TO_STILL para que ahora use IDs
        RFF_TO_VANILLA.clear();
        RFF_TO_VANILLA.putAll(FiniteFluidIDsToVanilla);
        if (FiniteFluidLogic.debug)
        	for (String key : RFF_TO_VANILLA.keySet()) {
        		System.out.println("[RFF] KEY FINAL "+key);
        		System.out.println("[RFF] VALUE FINAL "+RFF_TO_VANILLA.get(key));
        	}
        //System.out.println("VERGA: "+RFF_TO_VANILLA);
    }


    // ================================
    // FUNCION DE REMAPEADO
    // ================================
    private int remapLevel(int old) {
        if (old < References.MINIMUM_LEVEL) old = References.MINIMUM_LEVEL;
        if (old > References.MAXIMUM_LEVEL) old = References.MAXIMUM_LEVEL;

        return References.MAXIMUM_LEVEL-old;
    }
}
