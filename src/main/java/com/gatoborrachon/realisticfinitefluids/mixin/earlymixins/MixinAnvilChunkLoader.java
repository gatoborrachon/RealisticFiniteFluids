package com.gatoborrachon.realisticfinitefluids.mixin.earlymixins;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.gatoborrachon.realisticfinitefluids.blocks.BlockFiniteFluid;
import com.gatoborrachon.realisticfinitefluids.init.ModConfig;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.NibbleArray;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;


@Mixin(AnvilChunkLoader.class)
public abstract class MixinAnvilChunkLoader {

    private static final Set<ChunkPos> processedChunks = new HashSet<>();

    // -- BLOQUES A REEMPLAZAR -- 
    private static final Map<String,String> VANILLA_TO_RFF = new HashMap<>();
    private static final Map<String,String> OCEAN_TO_STILL = new HashMap<>();

    static {
    	//1.- Bloques vanilla --> Finitos
        VANILLA_TO_RFF.put("minecraft:water",            "realisticfinitefluids:finite_water_still");
        VANILLA_TO_RFF.put("minecraft:flowing_water",    "realisticfinitefluids:finite_water_flowing");
        VANILLA_TO_RFF.put("minecraft:lava",             "realisticfinitefluids:finite_lava_still");
        VANILLA_TO_RFF.put("minecraft:flowing_lava",     "realisticfinitefluids:finite_lava_flowing");

        //2.- Bloques oceanicos --> Still
        OCEAN_TO_STILL.put("realisticfinitefluids:infinite_water_source", "realisticfinitefluids:finite_water_still");
        OCEAN_TO_STILL.put("realisticfinitefluids:infinite_lava_source",  "realisticfinitefluids:finite_lava_still");
    }

    //Funcion para remapear LEVELs viejos (de 0-15) a nuevos (de 0-7)
    private int remapLevel(int old) {
        if (old < 0) old = 0;
        if (old > 15) old = 15;
        return old / 2;
    }
    
    @Inject(
            method = "func_75823_a", // readChunkFromNBT
            at = @At("RETURN"),
            cancellable = true,
            remap = true
        )
    private void onReadChunkFromNBT(net.minecraft.world.World worldIn, NBTTagCompound nbt, CallbackInfoReturnable<Chunk> cir) {
        Chunk chunk = cir.getReturnValue();
        if (chunk == null) return;

        ChunkPos pos = chunk.getPos();
        if (processedChunks.contains(pos)) return; // ya reemplazado

        // recorrer todas las secciones del chunk
        ExtendedBlockStorage[] storageArray = chunk.getBlockStorageArray();
        for (int i = 0; i < storageArray.length; i++) {
            if (storageArray[i] != null) {
                for (int x = 0; x < 16; x++) {
                    for (int y = 0; y < 16; y++) {
                        for (int z = 0; z < 16; z++) {
                            IBlockState state = storageArray[i].get(x, y, z);
                            if (state != null) {
                                String blockName = state.getBlock().getRegistryName().toString();

                                
                                
                                if (blockName.equals("minecraft:water") || blockName.equals("minecraft:flowing_water")) {
                                    Block newBlock = Block.getBlockFromName("realisticfinitefluids:finite_water_still").getDefaultState().withProperty(BlockFiniteFluid.LEVEL, BlockFiniteFluid.MAXIMUM_CONCEPTUAL_LEVEL).getBlock();
                                    if (newBlock != null) storageArray[i].set(x, y, z, newBlock.getDefaultState());
                                } else if (blockName.equals("minecraft:lava") || blockName.equals("minecraft:flowing_lava")) {
                                    Block newBlock = Block.getBlockFromName("realisticfinitefluids:finite_lava_still").getDefaultState().withProperty(BlockFiniteFluid.LEVEL, BlockFiniteFluid.MAXIMUM_CONCEPTUAL_LEVEL).getBlock();
                                    if (newBlock != null) storageArray[i].set(x, y, z, newBlock.getDefaultState());
                                }
                                
                                
                                
                            }
                        }
                    }
                }
            }
        }

        // marcar chunk como procesado
        processedChunks.add(pos);
    }
}


/*


    @Inject(
        method = "func_75823_a", // readChunkFromNBT
        at = @At("HEAD"),
        cancellable = true,
        remap = true
    )
    private void onReadChunkFromNBT(net.minecraft.world.World worldIn, NBTTagCompound nbt, CallbackInfoReturnable<Chunk> cir) {
        Chunk chunk = cir.getReturnValue();
        if (chunk == null) return;

        ChunkPos pos = chunk.getPos();
        if (processedChunks.contains(pos)) return;
        
        
        
        NBTTagList sectionsNBT = nbt.getTagList("Sections", 10);
        for (int i = 0; i < sectionsNBT.tagCount(); i++) {
            NBTTagCompound sectionNBT = sectionsNBT.getCompoundTagAt(i);
            if (!sectionNBT.hasKey("Palette", 9)) continue; // TAG_LIST

            NBTTagList palette = sectionNBT.getTagList("Palette", 10);
            for (int j = 0; j < palette.tagCount(); j++) {
                NBTTagCompound entry = palette.getCompoundTagAt(j);
                String name = entry.getString("Name");
                if (OCEAN_TO_STILL.containsKey(name)) {
                    // reemplaza el bloque en el NBT
                    entry.setString("Name", OCEAN_TO_STILL.get(name));

                    // Si hay Properties (como LEVEL), ponle LEVEL = 8
                    if (!entry.hasKey("Properties", 10)) {
                        entry.setTag("Properties", new NBTTagCompound());
                    }
                    entry.getCompoundTag("Properties").setInteger("LEVEL", BlockFiniteFluid.MAXIMUM_CONCEPTUAL_LEVEL);
                }
            }
        }
        
        
        
        

        ExtendedBlockStorage[] storageArray = chunk.getBlockStorageArray();

        for (ExtendedBlockStorage section : storageArray) {
            if (section == null) continue;
            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < 16; y++) {
                    for (int z = 0; z < 16; z++) {
                        IBlockState state = section.get(x, y, z);
                        if (state == null) continue;

                        Block block = state.getBlock();
                        String name = block.getRegistryName().toString();

                        //Remapear bloques vanilla --> Finitos
                        //if (ModConfig.replaceVanillaFluids) {
                            if (VANILLA_TO_RFF.containsKey(name)) {
                                Block newBlock = Block.getBlockFromName(VANILLA_TO_RFF.get(name));
                                if (newBlock != null) {
                                    IBlockState newState = newBlock.getDefaultState()
                                            .withProperty(BlockFiniteFluid.LEVEL, BlockFiniteFluid.MAXIMUM_CONCEPTUAL_LEVEL);
                                    section.set(x, y, z, newState);
                                }
                                continue;
                            }
                        //}

                        //if (ModConfig.replaceOldFiniteFluids) {
                            //Remapear bloques oceanicos --> Still
                            /*if (OCEAN_TO_STILL.containsKey(name)) {
                                //System.out.println("BLOQUE: "+name);
                                Block newBlock = Block.getBlockFromName(OCEAN_TO_STILL.get(name));
                                if (newBlock != null) {
                                    IBlockState newState = newBlock.getDefaultState()
                                            .withProperty(BlockFiniteFluid.LEVEL, BlockFiniteFluid.MAXIMUM_CONCEPTUAL_LEVEL);
                                    section.set(x, y, z, newState);
                                }
                                continue;
                            }*/ /*

                            //Remapear LEVELs de los demas bloques finitos
                            if (block instanceof BlockFiniteFluid) {
                                int oldLevel = state.getValue(BlockFiniteFluid.LEVEL);
                                int newLevel = remapLevel(oldLevel);
                                IBlockState newState = state.withProperty(BlockFiniteFluid.LEVEL, newLevel);
                                section.set(x, y, z, newState);
                            }
                        //}
                        
                        
                    }
                }
            }
        }

        processedChunks.add(pos);
    }

    


*/

