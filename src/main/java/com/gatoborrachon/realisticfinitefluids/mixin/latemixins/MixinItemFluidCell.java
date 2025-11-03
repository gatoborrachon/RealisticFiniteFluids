package com.gatoborrachon.realisticfinitefluids.mixin.latemixins;

import java.util.Arrays;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.gatoborrachon.realisticfinitefluids.blocks.BlockFiniteFluid;
import com.gatoborrachon.realisticfinitefluids.init.ModBlocks;
import com.gatoborrachon.realisticfinitefluids.logic.FiniteFluidLogic;
import com.gatoborrachon.realisticfinitefluids.logic.NewFluidType;

import ic2.core.item.ItemFluidCell;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;


@Mixin(ItemFluidCell.class)
public abstract class MixinItemFluidCell {

    private static final String NBT_KEY_LEVELS = "FiniteLevels"; // niveles conceptuales 1..16
    //private static final String NBT_KEY_FLUID = "FiniteFluid";   // registry name, e.g. "water"
    private static final int MAX_LEVELS = BlockFiniteFluid.MAXIMUM_LEVEL+1;

    @Inject(method = "onItemUse", at = @At("HEAD"), cancellable = true)
    private void onItemUseCustom(EntityPlayer player, World world, BlockPos pos, EnumHand hand,
                                 EnumFacing side, float xOffset, float yOffset, float zOffset,
                                 CallbackInfoReturnable<EnumActionResult> cir) {

        //ItemStack IC2FluidCell = player.getHeldItem(hand);
        ItemStack originalIC2FluidCell = player.getHeldItem(hand);
        if (originalIC2FluidCell.isEmpty()) return; // deja que el vanilla maneje
        

     // Decide sobre qué stack trabajará el resto de tu código
     ItemStack IC2FluidCell = originalIC2FluidCell;
     boolean isCopy = false;
     if (originalIC2FluidCell.getCount() > 1) {
         IC2FluidCell = originalIC2FluidCell.copy();
         IC2FluidCell.setCount(1);
         isCopy = true;
     }
     
    	////System.out.println("IC2FluidCell size: "+IC2FluidCell.getCount());

        // fluid actual en la celda (mb) si existe
        FluidStack tankFs = getFluid(IC2FluidCell);
        int tankMB = (tankFs == null ? 0 : tankFs.amount);
        Fluid tankFluidType = (tankFs == null ? null : tankFs.getFluid());
    	////System.out.println("tankMB: "+tankMB);
    	//System.out.println("tankFluidType: "+tankFluidType.getName());

        // NBT levels + fluid (preferimos NBT si existe)
        boolean hasNBTLevels = hasFiniteLevels(IC2FluidCell);
    	////System.out.println("hasNBTLevels: "+hasNBTLevels);
        int nbtLevels = getFiniteLevels(IC2FluidCell); // 0..16 (conceptual)
    	////System.out.println("nbtLevels: "+nbtLevels);
        String nbtFluidName = tankFs == null ? "" : tankFs.getFluid().getName(); 
        //getFiniteFluidName(IC2FluidCell); // puede ser null
    	////System.out.println("nbtFluidName: "+nbtFluidName);

        // derive current conceptual levels: prefer NBT, si no hay NBT derivar de mb
        int currentLevels = hasNBTLevels ? nbtLevels : (int)Math.floor(tankMB * (double)MAX_LEVELS / 1000.0);
    	////System.out.println("currentLevels-1: "+currentLevels);
    	////System.out.println("currentLevels-2: "+((int)Math.floor(tankMB * (double)MAX_LEVELS / 1000.0)));
        // derive "fluid type" for these conceptual levels:
        Fluid finiteFluid = null;
        if (hasNBTLevels && nbtFluidName != null && !nbtFluidName.isEmpty()) {
            finiteFluid = FluidRegistry.getFluid(nbtFluidName);
        	////System.out.println("finiteFluid-1: "+finiteFluid);
        } else if (tankFluidType != null) {
            finiteFluid = tankFluidType;
        	////System.out.println("finiteFluid-2: "+finiteFluid);
        }
    	////System.out.println("finiteFluid-3: "+finiteFluid);

    	

        IBlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        
        BlockPos targetPos = pos;
        if (!block.isReplaceable(world, pos)) {
            targetPos = pos.offset(side); // bloque adyacente en la dirección clickeada
            pos = targetPos;
            state = world.getBlockState(targetPos);
            block = state.getBlock();
        }
        
        boolean targetIsFinite = (block instanceof BlockFiniteFluid);
    	////System.out.println("targetIsFinite: "+targetIsFinite);

        // si ni el item ni el target están relacionados con finite fluids -> NO interceptamos
        if (!targetIsFinite && currentLevels == 0 && tankMB == 0) {
            return; // dejamos que el comportamiento original suceda
        }

        /* 
//1) +NORMAL + FluidCell CON liquido + apuntando a bloque de agua finita (obviamente considera si apuntas a lava/agua y tenemos agua/lava en la IC2 FluidCell) --> collectSmoothly solo si la celda no esta llena (menos de 16 niveles conceptuales)
//2) +NORMAL + FluidCell CON liquido + apuntando a cualquier otro bloque--> distributeEqually (colocar) SOLO Si tenemos 16 niveles conceptuales/1000mb

//3) +NORMAL + FluidCell SIN liquido + Apuntando a bloque de agua finita --> collectSmootly 
4) +NORMAL + FluidCell SIN liquido + Apuntando a cualquier bloque --> No ahce nada xd



//5) +SNEAK + FluidCell Con liqudo + Apuntando a bloque de agua finita --> distributeSmoothly (fuerzas colocar agua/lava que tienes si el liquido es el mismo, si no pues reemplazas el liquido del mundo)
//6) +SNEAK + FluidCell Con liqudo + Apuntando a cualquier bloque --> distributeSmootly

7) +SNEAK + FluidCell SIN liqudo + Apuntando a bloque de agua finita --> Nada, sneak es para forzar sacar liquido
8) +SNEAK + FluidCell SIN liqudo + Apuntando a cualquier bloque --> Nada, sneak es para fozar sacar liquido

CASOS QUE REQUIEREN CONFIRMAR EL BLOQUE A COLOCAR --> Caso 2, 6, 1.2, 5
        */
        
        boolean sneak = player.isSneaking();

        // si apuntamos a un BlockFiniteFluid, intenta las interacciones especiales
        if (targetIsFinite) {
            BlockFiniteFluid finiteBlock = (BlockFiniteFluid) block;
            Fluid blockFluid = finiteBlock.getFluid(); // asumes que existe getter returning Fluid
            ////System.out.println("blockFluid: "+blockFluid);
            
            // Si la celda tiene NBT/MB y no sabemos el tipo, asumimos el tipo del tanque si existe
            if (finiteFluid == null && tankFluidType != null) finiteFluid = tankFluidType;

            // Decide si el fluido de la celda es "compatible" con el bloque objetivo
            boolean cellHasFluid = (currentLevels > 0 || tankMB > 0);
            ////boolean sameFluid = (cellHasFluid && finiteFluid != null && finiteFluid == blockFluid);

            // ----- CASOS -----
            // SNEAK + CELDA CON LIQUIDO -> FORZAR COLOCAR (distribute) CASO -- 5)
            if (sneak && cellHasFluid) { 
            	////System.out.println("DEBUG CASO 5");
                // construimos la lista de objetivos (pos central + laterales)
                List<BlockPos> targets = Arrays.asList(pos, pos.north(), pos.south(), pos.east(), pos.west());
                
            	int fluidType = FiniteFluidLogic.GeneralPurposeLogic.getFluidIndex(nbtFluidName);                 
                int remaining = FiniteFluidLogic.FluidWorldInteraction.distributeEqually(world, targets, currentLevels, fluidType);
                //int placed = currentLevels - remaining;
                currentLevels = remaining;
                // guardamos fluid name en NBT si no estaba
                //if (finiteFluid != null) setFiniteFluidName(IC2FluidCell, FluidRegistry.getFluidName(finiteFluid));
                setFiniteLevels(IC2FluidCell, currentLevels);
                syncFluidMBFromLevels(IC2FluidCell, currentLevels, finiteFluid);
                	//manejar copias
                	handleCopy(player, originalIC2FluidCell, IC2FluidCell, isCopy);
                cir.setReturnValue(EnumActionResult.SUCCESS);
                cir.cancel();
                return;
            }

            // NORMAL + CELDA CON LIQUIDO + apuntando a BlockFiniteFluid --CASO 1)
            if (!sneak && cellHasFluid && currentLevels < MAX_LEVELS) {
            	////System.out.println("DEBUG CASO 1.1");

                // RECoger suavemente
                int blockLevelConceptual = BlockFiniteFluid.getConceptualVolume(state); //state.getValue(BlockFiniteFluid.LEVEL) + 1;
                // usa la nueva función que devuelve cuantos niveles conceptuales EXTRA se obtuvieron
                int delta = FiniteFluidLogic.FluidWorldInteraction.bucketRemoveFluidEvenLowCollect(world, pos, blockLevelConceptual, MAX_LEVELS - currentLevels, ((IFluidBlock)state.getBlock()).getFluid());
            	////System.out.println("DEBUG CASO 1.1:delta "+delta);
            	////System.out.println("DEBUG CASO 1.1:currentLevels "+currentLevels);
                currentLevels += delta;
            	////System.out.println("DEBUG CASO 1.1:currentLevels + delta "+currentLevels);

                if (currentLevels > MAX_LEVELS) currentLevels = MAX_LEVELS;
                // if we don't have recorded finiteFluid type but tank could tell us, assign it
                if (finiteFluid == null && tankFluidType != null) finiteFluid = tankFluidType;
                //if (finiteFluid != null) setFiniteFluidName(IC2FluidCell, FluidRegistry.getFluidName(finiteFluid));
                setFiniteLevels(IC2FluidCell, currentLevels);
                syncFluidMBFromLevels(IC2FluidCell, currentLevels, finiteFluid);
                	//manejar copias
                	handleCopy(player, originalIC2FluidCell, IC2FluidCell, isCopy);
                cir.setReturnValue(EnumActionResult.SUCCESS);
                cir.cancel();
                return;
            }

            // NORMAL y CELDA FULL -> intentar colocar (comportamiento normal de colocación) -- CASO 1) tambien
            if (!sneak && currentLevels >= MAX_LEVELS) {
            	////System.out.println("DEBUG CASO 1.2");
                List<BlockPos> targets = Arrays.asList(pos, pos.north(), pos.south(), pos.east(), pos.west());
                
            	int fluidType = FiniteFluidLogic.GeneralPurposeLogic.getFluidIndex(nbtFluidName); 
                int remaining = FiniteFluidLogic.FluidWorldInteraction.distributeEqually(world, targets, currentLevels, fluidType);
                currentLevels = remaining;
                setFiniteLevels(IC2FluidCell, currentLevels);
                syncFluidMBFromLevels(IC2FluidCell, currentLevels, finiteFluid);
                	//manejar copias
                	handleCopy(player, originalIC2FluidCell, IC2FluidCell, isCopy);
                cir.setReturnValue(EnumActionResult.SUCCESS);
                cir.cancel();
                return;
            }

            // si no hemos hecho nada y celda vacía -> intentar recoger (si la celda está VACIA) -- CASO 3)
            if (!sneak && !cellHasFluid) {
            	/*if (IC2FluidCell.getCount() > 1) {
            		IC2FluidCell.shrink(1);
            	    ItemStack empty = IC2Items.getItem("fluid_cell");
            	    ItemHandlerHelper.giveItemToPlayer(player, empty);
            	}*/ 
            		
            	////System.out.println("DEBUG CASO 3");
                // intentar recoger desde el bloque al que apuntamos (equivalente a llenar una celda vacía)
                int blockLevelConceptual = BlockFiniteFluid.getConceptualVolume(state); //state.getValue(BlockFiniteFluid.LEVEL) + 1;
            	////System.out.println("DEBUG CASO 3:blockLevelConceptual"+blockLevelConceptual);
                int delta = FiniteFluidLogic.FluidWorldInteraction.bucketRemoveFluidEvenLowCollect(world, pos, blockLevelConceptual, MAX_LEVELS - currentLevels, ((IFluidBlock)state.getBlock()).getFluid());
            	////System.out.println("DEBUG CASO 3:delta"+delta);
                if (delta > 0) {
                    currentLevels += delta;
                    //setFiniteFluidName(IC2FluidCell, FluidRegistry.getFluidName(blockFluid));
                    setFiniteLevels(IC2FluidCell, currentLevels);
                    syncFluidMBFromLevels(IC2FluidCell, currentLevels, blockFluid);
                    world.neighborChanged(pos, block, pos);
                    	//manejar copias
                    	handleCopy(player, originalIC2FluidCell, IC2FluidCell, isCopy);
                    cir.setReturnValue(EnumActionResult.SUCCESS);
                    cir.cancel();
                    return;
                }
            }
        }
        
        

        // ----- Si apuntamos a otro bloque (no finito) -----
        // SNEAK + CELDA CON LIQUIDO -> colocar suavemente en el lugar (forzar colocar)
        if (sneak && currentLevels > 0) {
        	////System.out.println("DEBUG CASO 6");
            // intentamos colocar en pos (o pos.offset(side) si no se puede)
            BlockPos placePos = pos;
            /*if (!world.mayPlace(ModBlocks.FINITE_WATER_FLOWING, placePos, false, side, null)) {
                placePos = pos.offset(side);
            }*/
            if (world.isAirBlock(placePos) || world.mayPlace(ModBlocks.FINITE_WATER_FLOWING, placePos, false, side, null)) {
                // colocamos usando distributeEqually solo con un objetivo: la posición
                List<BlockPos> targets = Arrays.asList(placePos);
                
            	int fluidType = FiniteFluidLogic.GeneralPurposeLogic.getFluidIndex(nbtFluidName); 
            	Block blockFluidType = ((NewFluidType)FiniteFluidLogic.liquids.get(fluidType)).flowingBlock;
            	//IBlockState blockstateToPlace = blockFluidType.getDefaultState().withProperty(RFFBlock.LEVEL, MAX_LEVELS - 1);
            	
                int remaining = FiniteFluidLogic.FluidWorldInteraction.distributeEquallyNoAdyFluid(world, targets, currentLevels, blockFluidType);
                currentLevels = remaining;
            	////System.out.println("DEBUG CASO 6:currentLevels "+currentLevels);
                // assign fluid type if needed
                if (finiteFluid == null && tankFluidType != null) finiteFluid = tankFluidType;
                //if (finiteFluid != null) setFiniteFluidName(IC2FluidCell, FluidRegistry.getFluidName(finiteFluid));
                setFiniteLevels(IC2FluidCell, currentLevels);
                syncFluidMBFromLevels(IC2FluidCell, currentLevels, finiteFluid);
                	//manejar copias
                	handleCopy(player, originalIC2FluidCell, IC2FluidCell, isCopy);
                cir.setReturnValue(EnumActionResult.SUCCESS);
                cir.cancel();
                return;
            }
        }

        // NORMAL + CELDA CON LIQUIDO + APUNTANDO A BLOQUE NO-FINITO:
        // sólo colocar *si* tiene 16 niveles (completo), como pediste
    	int fluidType = FiniteFluidLogic.GeneralPurposeLogic.getFluidIndex(nbtFluidName); 
    	////System.out.println("DEBUG CASO 2:fluidType: "+fluidType);
        if (!sneak && currentLevels >= MAX_LEVELS && fluidType != -1) {
        	////System.out.println("DEBUG CASO 2");
        	////System.out.println("DEBUG CASO 2:currentLevels: "+currentLevels);
            BlockPos placePos = pos;
            if (!world.mayPlace(ModBlocks.FINITE_WATER_FLOWING, placePos, false, side, null)) {
                placePos = pos.offset(side);
            }
            if (world.isAirBlock(placePos) || world.mayPlace(ModBlocks.FINITE_WATER_FLOWING, placePos, false, side, null)) {
            	Block blockFluidType = ((NewFluidType)FiniteFluidLogic.liquids.get(fluidType)).flowingBlock;
            	////System.out.println("DEBUG CASO 2:blockFluidType: "+blockFluidType);
            	IBlockState blockstateToPlace = BlockFiniteFluid.setVolume(blockFluidType.getDefaultState(), BlockFiniteFluid.MAXIMUM_LEVEL); //blockFluidType.getDefaultState().withProperty(BlockFiniteFluid.LEVEL, MAX_LEVELS - 1);
            	
            	////System.out.println("DEBUG CASO 2:blockstateToPlace: "+blockstateToPlace);
                world.setBlockState(placePos, blockstateToPlace); //ModBlocks.FINITE_WATER_FLOWING.getDefaultState().withProperty(RFFBlock.LEVEL, MAX_LEVELS - 1));
                currentLevels -= MAX_LEVELS;
                setFiniteLevels(IC2FluidCell, currentLevels);
                syncFluidMBFromLevels(IC2FluidCell, currentLevels, finiteFluid);
                	//manejar copias
                	handleCopy(player, originalIC2FluidCell, IC2FluidCell, isCopy);
                cir.setReturnValue(EnumActionResult.SUCCESS);
                cir.cancel();
                return;
            }
        }

        // si no hicimos nada, dejamos que la lógica original ocurra (no cancelar)
        return;
    }

    // ---------- helpers NBT ----------
    private boolean hasFiniteLevels(ItemStack stack) {
        return stack.hasTagCompound() && stack.getTagCompound().hasKey(NBT_KEY_LEVELS);
    }

    private int getFiniteLevels(ItemStack stack) {
        if (!hasFiniteLevels(stack)) return 0;
        return stack.getTagCompound().getInteger(NBT_KEY_LEVELS);
    }

    /*private String getFiniteFluidName(ItemStack stack) {
        if (!stack.hasTagCompound()) return null;
        NBTTagCompound tag = stack.getTagCompound();
        return tag.hasKey(NBT_KEY_FLUID) ? tag.getString(NBT_KEY_FLUID) : null;
    }*/

    /*private void setFiniteFluidName(ItemStack stack, String name) {
        if (name == null) return;
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) tag = new NBTTagCompound();
        tag.setString(NBT_KEY_FLUID, name);
        stack.setTagCompound(tag);
    }*/

    private void setFiniteLevels(ItemStack stack, int levels) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) tag = new NBTTagCompound();
        if (levels <= 0) {
            tag.removeTag(NBT_KEY_LEVELS);
            //tag.removeTag(NBT_KEY_FLUID);
        } else {
            tag.setInteger(NBT_KEY_LEVELS, Math.min(levels, MAX_LEVELS));
        }
        stack.setTagCompound(tag);
    }

    // ---------- Sincronizar FluidHandler con los niveles ----------
    private void syncFluidMBFromLevels(ItemStack stack, int levelsConceptual, Fluid fluid) {
        // convierte niveles conceptuales -> mb (aprox. 1000/16)
        int mb = (int) Math.floor(levelsConceptual * 1000.0 / MAX_LEVELS);
        // mueve contenido del IFluidHandlerItem del stack
        IFluidHandlerItem handler = FluidUtil.getFluidHandler(stack);
        if (handler == null) return;
        // limpiar
        handler.drain(Integer.MAX_VALUE, true);
        if (mb <= 0 || fluid == null) return;
        FluidStack fill = new FluidStack(fluid, mb);
        handler.fill(fill, true);
    }

    private FluidStack getFluid(ItemStack stack) {
        IFluidHandlerItem handler = FluidUtil.getFluidHandler(stack);
        if (handler == null) return null;
        return handler.drain(Integer.MAX_VALUE, false);
    }
    
    // ---------- Manejar si usamos una copia ddel stack original ----------
    private void handleCopy(EntityPlayer player, ItemStack originalIC2FluidCell, ItemStack IC2FluidCell, boolean isCopy) {
    	if (isCopy) {
    	    originalIC2FluidCell.setCount(originalIC2FluidCell.getCount()-1);//.shrink(1);
    	    if (!player.inventory.addItemStackToInventory(IC2FluidCell)) {
    	        player.dropItem(IC2FluidCell, false);
    	    }
    	}
    }
    
    
}


