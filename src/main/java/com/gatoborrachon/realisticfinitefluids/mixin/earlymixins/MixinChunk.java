package com.gatoborrachon.realisticfinitefluids.mixin.earlymixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.gatoborrachon.realisticfinitefluids.events.FluidEventHandler;
import com.gatoborrachon.realisticfinitefluids.init.ModBlocks;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;

@Mixin(Chunk.class)
public abstract class MixinChunk {

	//NOTA PARA EL FUTURO --> 
	//NUNCA USAR WORLD.SETBLOCKSTATE
	//SIEMPRE USAR CHUNK.SETBLOCKSTATE
	
	/**
	 * ESTA FUNCION ES UN PARCHEADOR DE MUNDOS PERO ES BASTANTE INEFICIENTE Y GENERA PAREDES DE PIEDRA EN LOS BORDES DE LOS CHUNKS,
	 * EVITAR USAR A MENOS QUE QUIERAS PARCHES LO MAS PRECISOS POSIBLES
	 */
    @Inject(method = "func_186030_a", at = @At("TAIL"), remap = true) // func_186030_a --> populate
    private void sealOceanLeaks(IChunkProvider provider, IChunkGenerator generator, CallbackInfo ci) {

    	
        Chunk chunk = (Chunk)(Object)this;
        World world = chunk.getWorld();

        
        if (world.isRemote) return;
        if (chunk.isTerrainPopulated()) return; 
        //System.out.println("Is Chunk Sealed: "+FluidEventHandler.isSealed(chunk));

        if (FluidEventHandler.isSealed(chunk)) return; // Ya está sellado, no hacemos nada
        
        int seaLevel = world.getSeaLevel();
        //int seaLevel = 63; //justo arriba de donde aparecen bloques de agua
        //System.out.println("SEA LEVEL: "+seaLevel);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        int chunkX = chunk.x;
        int chunkZ = chunk.z;

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = 1; y < seaLevel; y++) { // y < seaLevel evita modificar justo la superficie
                    pos.setPos((chunkX << 4) + x, y, (chunkZ << 4) + z);
                    IBlockState state = chunk.getBlockState(pos);

                    //if (x == 0 || x == 15 || z == 0 || z == 15) continue;

                    
                    //YA CASI SIRVE
                    if (state.getMaterial() != Material.WATER) {
                        boolean exposedToWater = false;
                        boolean surroundedByGravel = true;
                        boolean exposedToAir = false;
                        boolean topBlockHasNoAirOnSide = true;

                        for (EnumFacing dir : EnumFacing.VALUES) {
                            BlockPos neighbor = pos.offset(dir);
                            IBlockState neighborState = chunk.getBlockState(neighbor);

                            //== AIR  hace que todos los bloques de aire del interior de las cuevas se conviertan en oro, vale verga
                            //WATER_SOURCE hace que solo los bloques que tengan agua vecina, logramos arreglar los huecos que tienen graba, pero
                            if (neighborState.getBlock() == ModBlocks.INFINITE_WATER_SOURCE) {
                                exposedToWater = true;
                                break;
                            }
                        }
                        


                        //
                        if (exposedToWater) { ///&& (chunk.getBlockState(pos.down()).getMaterial() != Material.ROCK) || chunk.getBlockState(pos.down()).getMaterial() != Material.SAND // || chunk.getBlockState(pos.up()).getMaterial() == Material.WATER
                        	if (chunk.getBlockState(pos).getBlock() == Blocks.GRAVEL) {
                                for (EnumFacing dir : EnumFacing.HORIZONTALS) {
                                    BlockPos neighborGravel = pos.offset(dir);
                                    IBlockState neighborGravelState = chunk.getBlockState(neighborGravel);

                                    if (neighborGravelState.getBlock() == ModBlocks.INFINITE_WATER_SOURCE || neighborGravelState.getBlock() == Blocks.AIR) {
                                    	surroundedByGravel = false;
                                        break;
                                    }
                                }
                        	}
                        	
                            for (EnumFacing dir : EnumFacing.VALUES) {
                                BlockPos neighborAir = pos.offset(dir);
                                IBlockState neighborAirState = chunk.getBlockState(neighborAir);
                                if (neighborAirState.getBlock() == Blocks.AIR) {
                                	exposedToAir = true;
                                    break;
                                }
                            }
                        	/*if (!surroundedByGravel || exposedToAir) {
                        		chunk.setBlockState(pos.toImmutable(), Blocks.STONE.getDefaultState());
                            	System.out.println("[SealOceanLeaks] Oro colocado en " + pos);
                        	}*/
                            
                        	
                            for (EnumFacing dir : EnumFacing.HORIZONTALS) {
                            BlockPos neighborOtherBlocks = pos.offset(dir);
                            IBlockState neighborOtherBlocksState = chunk.getBlockState(neighborOtherBlocks);
                            	if (pos.getY() == seaLevel-1 && neighborOtherBlocksState.getBlock() == Blocks.AIR) {
                            		topBlockHasNoAirOnSide = false;
                                	break;
                            	}  
                            }
                            
                            //System.out.println("topBlockHasNoAirOnSide: " +topBlockHasNoAirOnSide);
                            //System.out.println(pos);
                            if (pos.getY() == seaLevel-1 && topBlockHasNoAirOnSide) {
                            	//chunk.setBlockState(pos.toImmutable(), Blocks.GOLD_BLOCK.getDefaultState());
                            	//System.out.println("[SealOceanLeaks] Oro colocado en " + pos);                            	
                            } else 
                        	if (!surroundedByGravel || exposedToAir) {
                        		//if (topBlockHasNoAirOnSide) continue;
                        		chunk.setBlockState(pos.toImmutable(), Blocks.STONE.getDefaultState());
                            	//System.out.println("[SealOceanLeaks] Oro colocado en " + pos);

                        	}
                        	

                        
                        //System.out.println("MIXINVERGON");

                        	
                        }
                    }
                    
                }
            }
        }
        FluidEventHandler.markSealed(chunk); // Marcamos que este chunk ya fue sellado

    }
}


