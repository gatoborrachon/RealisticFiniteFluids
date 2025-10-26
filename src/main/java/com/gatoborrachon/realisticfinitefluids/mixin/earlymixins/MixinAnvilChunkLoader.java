package com.gatoborrachon.realisticfinitefluids.mixin.earlymixins;

import java.util.HashSet;
import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;


@Mixin(AnvilChunkLoader.class)
public abstract class MixinAnvilChunkLoader {

    /**
     * Inyectamos justo después de que el chunk fue leído desde NBT,
     * antes de que se regrese el objeto "Chunk" al servidor.
     */
    // Set estático para recordar qué chunks ya fueron procesados
    private static final Set<ChunkPos> processedChunks = new HashSet<>();

    @Inject(
        method = "func_75823_a", // readChunkFromNBT
        at = @At("RETURN"),
        cancellable = true, remap = true
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
                                    Block newBlock = Block.getBlockFromName("realisticfinitefluids:infinite_water_source");
                                    if (newBlock != null) storageArray[i].set(x, y, z, newBlock.getDefaultState());
                                } else if (blockName.equals("minecraft:lava") || blockName.equals("minecraft:flowing_lava")) {
                                    Block newBlock = Block.getBlockFromName("realisticfinitefluids:infinite_lava_source");
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

	
    /*@Inject(
        method = "func_75823_a", // func_75823_a --> readChunkFromNBT
        at = @At("RETURN"),
        cancellable = true, remap = true
    )
    private void onReadChunkFromNBT(net.minecraft.world.World worldIn, NBTTagCompound nbt, CallbackInfoReturnable<Chunk> cir) {
        Chunk chunk = cir.getReturnValue();

        if (chunk == null) return;

        // recorrer todas las secciones del chunk
        chunk.getBlockStorageArray(); // devuelve ExtendedBlockStorage[]
        for (int i = 0; i < chunk.getBlockStorageArray().length; i++) {
            if (chunk.getBlockStorageArray()[i] != null) {
                for (int x = 0; x < 16; x++) {
                    for (int y = 0; y < 16; y++) {
                        for (int z = 0; z < 16; z++) {
                            // bloque actual
                            net.minecraft.block.state.IBlockState state = chunk.getBlockStorageArray()[i].get(x, y, z);

                            if (state != null) {
                                String blockName = state.getBlock().getRegistryName().toString();

                                if (blockName.equals("minecraft:water") || blockName.equals("minecraft:flowing_water")) {
                                    net.minecraft.block.Block newBlock = net.minecraft.block.Block.getBlockFromName("realisticfinitefluids:infinite_water_source");
                                    if (newBlock != null) {
                                        chunk.getBlockStorageArray()[i].set(x, y, z, newBlock.getDefaultState());
                                    }
                                } else if (blockName.equals("minecraft:lava") || blockName.equals("minecraft:flowing_lava")) {
                                    net.minecraft.block.Block newBlock = net.minecraft.block.Block.getBlockFromName("realisticfinitefluids:infinite_lava_source");
                                    if (newBlock != null) {
                                        chunk.getBlockStorageArray()[i].set(x, y, z, newBlock.getDefaultState());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }*/
}


/*@Mixin(ChunkProviderServer.class)
public abstract class MixinChunkProviderServer {

    /*@Inject(method = "func_186025_d", at = @At("RETURN"), remap = true) //func_186025_d --> provideChunk
    private void replaceVanillaLiquids(int chunkX, int chunkZ, CallbackInfoReturnable<Chunk> cir) {
        Chunk chunk = cir.getReturnValue();
        WorldServer world = (WorldServer) chunk.getWorld();

        if (world.isRemote) return;
        if (FluidEventHandler.isReplaced(chunk)) return; // ya reemplazado
        FluidEventHandler.markReplaced(chunk); // marcar antes para evitar recursion

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int replacedCount = 0;

        // Limitar Y para no tocar todo el mundo, por ejemplo hasta nivel de mar + 10
        int minY = 1;
        int maxY = world.getSeaLevel() + 100;

        // Limpiar ticks pendientes solo para este chunk y solo para agua/lava
        List<NextTickListEntry> pendingTicks = world.getPendingBlockUpdates(chunk, true);
        if (pendingTicks != null) {
            Iterator<NextTickListEntry> it = pendingTicks.iterator();
            while (it.hasNext()) {
                NextTickListEntry entry = it.next();
                Block b = entry.getBlock();
                if (b == Blocks.WATER || b == Blocks.FLOWING_WATER || b == Blocks.LAVA || b == Blocks.FLOWING_LAVA) {
                    it.remove();
                } else {
                    world.scheduleUpdate(entry.position, b, 0); // re-agendar otros ticks
                }
            }
        }

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = minY; y <= maxY; y++) {
                    pos.setPos((chunkX << 4) + x, y, (chunkZ << 4) + z);
                    IBlockState state = chunk.getBlockState(pos);
                    Block block = state.getBlock();

                    if (block == Blocks.WATER || block == Blocks.FLOWING_WATER) {
                        chunk.setBlockState(pos.toImmutable(),
                                ModBlocks.INFINITE_WATER_SOURCE.getDefaultState()
                                        .withProperty(BlockFiniteFluid.LEVEL, 15));
                        replacedCount++;
                    } else if (block == Blocks.LAVA || block == Blocks.FLOWING_LAVA) {
                        chunk.setBlockState(pos.toImmutable(),
                                ModBlocks.INFINITE_LAVA_SOURCE.getDefaultState()
                                        .withProperty(BlockFiniteFluid.LEVEL, 15));
                        replacedCount++;
                    }
                }
            }
        }

        System.out.println("[MixinFluidReplace] Reemplazados " + replacedCount + " bloques en chunk: " + chunkX + ", " + chunkZ);
    }*/




    /*@Inject(method = "func_186025_d", at = @At("RETURN"), remap = true) //func_186025_d --> provideChunk
    private void replaceVanillaLiquids(int chunkX, int chunkZ, CallbackInfoReturnable<Chunk> cir) {
        Chunk chunk = cir.getReturnValue();
        World world = chunk.getWorld();

        if (world.isRemote) return;
        if (FluidEventHandler.isReplaced(chunk)) return; // ya reemplazado

        // --- LIMPIEZA DE TICKS PENDIENTES ---
        if (world instanceof WorldServer) {
            WorldServer ws = (WorldServer) world;
            List<NextTickListEntry> pendingTicks = ws.getPendingBlockUpdates(chunk, true); // remove=true
            int removed = 0;
            if (pendingTicks != null) {
                for (NextTickListEntry tick : pendingTicks) {
                    Block b = tick.getBlock();
                    if (b == Blocks.WATER || b == Blocks.FLOWING_WATER
                     || b == Blocks.LAVA  || b == Blocks.FLOWING_LAVA) {
                        removed++;
                    }
                }
            }
            System.out.println("[MixinFluidReplace] Eliminados " + removed + " ticks pendientes de fluidos en chunk " + chunk.getPos());
        }
        // --- FIN LIMPIEZA DE TICKS ---

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int replacedCount = 0;

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = 1; y < world.getHeight(); y++) {
                    pos.setPos((chunkX << 4) + x, y, (chunkZ << 4) + z);
                    IBlockState state = chunk.getBlockState(pos);

                    if (state.getBlock() == Blocks.WATER || state.getBlock() == Blocks.FLOWING_WATER) {
                        chunk.setBlockState(pos.toImmutable(),
                                ModBlocks.INFINITE_WATER_SOURCE.getDefaultState()
                                        .withProperty(BlockFiniteFluid.LEVEL, 15));
                        replacedCount++;
                    } else if (state.getBlock() == Blocks.LAVA || state.getBlock() == Blocks.FLOWING_LAVA) {
                        chunk.setBlockState(pos.toImmutable(),
                                ModBlocks.INFINITE_LAVA_SOURCE.getDefaultState()
                                        .withProperty(BlockFiniteFluid.LEVEL, 15));
                        replacedCount++;
                    }
                }
            }
        }

        FluidEventHandler.markReplaced(chunk);
        System.out.println("[MixinFluidReplace] Reemplazados " + replacedCount + " bloques en chunk: " + chunkX + ", " + chunkZ);
    }
    
    
}*/





