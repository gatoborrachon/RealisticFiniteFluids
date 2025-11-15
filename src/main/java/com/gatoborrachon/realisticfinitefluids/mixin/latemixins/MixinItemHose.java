package com.gatoborrachon.realisticfinitefluids.mixin.latemixins;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fluids.capability.wrappers.BlockLiquidWrapper;
import net.minecraft.block.state.IBlockState;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;

import com.tiviacz.travelersbackpack.gui.inventory.InventoryTravelersBackpack;
import com.tiviacz.travelersbackpack.items.ItemHose;
import com.tiviacz.travelersbackpack.util.Reference;
import com.gatoborrachon.realisticfinitefluids.blocks.BlockFiniteFluid;
import com.gatoborrachon.realisticfinitefluids.blocks.BlockNewInfiniteSource;
import com.gatoborrachon.realisticfinitefluids.logic.FiniteFluidLogic;
import com.gatoborrachon.realisticfinitefluids.logic.NewFluidType;
import com.tiviacz.travelersbackpack.capability.CapabilityUtils;
import com.tiviacz.travelersbackpack.fluids.FluidEffectRegistry;

@Mixin(value = ItemHose.class, remap = false)
public abstract class MixinItemHose extends Item /*extends MixinItem*/ {
	    // ====== NBT keys por tanque ======
	    private static final String NBT_KEY_LEVELS_LEFT  = "FiniteLevelsLeft";   // conceptuales (1..infinito, clamp por capacidad)
	    private static final String NBT_KEY_LEVELS_RIGHT = "FiniteLevelsRight";
	    private static final String NBT_KEY_FLUID_LEFT   = "FiniteFluidLeft";    // registry name (e.g. "water")
	    private static final String NBT_KEY_FLUID_RIGHT  = "FiniteFluidRight";

	    private static final int LEVELS_PER_BUCKET = 16;   // 1000 mb

	    @Shadow
	    public abstract FluidTank getSelectedFluidTank(ItemStack stack, InventoryTravelersBackpack inv);

	    @Shadow
	    public abstract NBTTagCompound getTagCompound(ItemStack stack);

	    @Invoker("getHoseTank")
	    private static int callGetHoseTank(ItemStack stack) {
	        throw new AssertionError();
	    }

	    @Invoker("getHoseMode")
	    private static int callGetHoseMode(ItemStack stack) {
	        throw new AssertionError();
	    }
	    
	    /*@Shadow
	    private static int getHoseTank(ItemStack stack); // 1=Left, 2=Right
	    @Shadow
	    private static int getHoseMode(ItemStack stack); // 1=Suck, 2=Spill, 3=Drink
	    
		/*public static int getHoseMode(ItemStack stack)
		{
			if(stack.getTagCompound() != null)
			{
				return stack.getTagCompound().getInteger("Mode");
				//1 = Suck mode
				//2 = Spill mode
				//3 = Drink mode
			}
			return 0;
		}
		
		public static int getHoseTank(ItemStack stack)
		{
			if(stack.getTagCompound() != null)
			{
				return stack.getTagCompound().getInteger("Tank");
				//1 = Left tank
				//2 = Right tank
			}
			return 0;
		}*/
	    
	    // =============== Helpers NBT por lado ===============
	    private static String nbtKeyLevelsForSide(int side) {
	        return side == 1 ? NBT_KEY_LEVELS_LEFT : NBT_KEY_LEVELS_RIGHT;
	    }
	    private static String nbtKeyFluidForSide(int side) {
	        return side == 1 ? NBT_KEY_FLUID_LEFT : NBT_KEY_FLUID_RIGHT;
	    }
	    private static boolean hasFiniteLevels(ItemStack stack, int side) {
	        if (stack == null) return false;
	        NBTTagCompound tag = stack.getTagCompound();
	        return tag != null && tag.hasKey(nbtKeyLevelsForSide(side));
	    }
	    private static int getFiniteLevels(ItemStack stack, int side) {
	        if (!hasFiniteLevels(stack, side)) return 0;
	        return Math.max(0, stack.getTagCompound().getInteger(nbtKeyLevelsForSide(side)));
	    }
	    private static String getFiniteFluidName(ItemStack stack, int side) {
	        if (stack == null || stack.getTagCompound() == null) return "";
	        NBTTagCompound tag = stack.getTagCompound();
	        return tag.hasKey(nbtKeyFluidForSide(side)) ? tag.getString(nbtKeyFluidForSide(side)) : "";
	    }
	    private static void setFiniteLevelsAndFluid(ItemStack stack, int side, int levels, @Nullable String fluidName, int clampMax) {
	        if (stack == null) return;
	        NBTTagCompound tag = stack.getTagCompound();
	        if (tag == null) tag = new NBTTagCompound();

	        if (levels <= 0) {
	            tag.removeTag(nbtKeyLevelsForSide(side));
	            tag.removeTag(nbtKeyFluidForSide(side));
	        } else {
	            int clamped = clampMax > 0 ? Math.min(levels, clampMax) : levels;
	            tag.setInteger(nbtKeyLevelsForSide(side), clamped);
	            if (fluidName != null && !fluidName.isEmpty()) {
	                tag.setString(nbtKeyFluidForSide(side), fluidName);
	            }
	        }
	        stack.setTagCompound(tag);
	    }

	    // =============== Conversión Levels <-> mB (bucket=1000mB=16 levels) ===============
	    private static int levelsToMB(int levels) {
	        if (levels <= 0) return 0;
	        // floor para no sobrellenar
	        return (int)Math.floor(levels * 1000.0 / LEVELS_PER_BUCKET);
	    }
	    private static int mbToLevels(int mb) {
	        if (mb <= 0) return 0;
	        return (int)Math.floor(mb * (double)LEVELS_PER_BUCKET / 1000.0);
	    }

	    // Máximo conceptual según la capacidad del tanque
	    private static int getMaxLevelsForTank(FluidTank tank) {
	        if (tank == null) return LEVELS_PER_BUCKET; // fallback
	        int buckets = tank.getCapacity() / 1000;
	        return Math.max(LEVELS_PER_BUCKET, buckets * LEVELS_PER_BUCKET);
	    }

	    // Sincroniza: Levels+Fluid (hose NBT) -> Tank (mB)
	    private static void syncTankFromLevels(FluidTank tank, int levelsConceptual, @Nullable String fluidName) {
	        if (tank == null) return;
	        tank.drain(Integer.MAX_VALUE, true); // limpiar
	        if (levelsConceptual <= 0 || fluidName == null || fluidName.isEmpty()) return;
	        if (!FluidRegistry.isFluidRegistered(fluidName)) return;
	        int mb = levelsToMB(levelsConceptual);
	        if (mb <= 0) return;
	        FluidStack fill = new FluidStack(FluidRegistry.getFluid(fluidName), mb);
	        tank.fill(fill, true);
	    }

	    // Sincroniza: Tank (mB) -> Levels (hose NBT)
	    private static int syncLevelsFromTankToNBT(ItemStack stack, int side, FluidTank tank) {
	        if (tank == null) return 0;
	        FluidStack fs = tank.getFluid();
	        int mb = (fs == null ? 0 : fs.amount);
	        int levels = mbToLevels(mb);
	        String name = (fs == null || fs.getFluid() == null) ? "" : fs.getFluid().getName();
	        int maxLevels = getMaxLevelsForTank(tank);
	        setFiniteLevelsAndFluid(stack, side, levels, name, maxLevels);
	        return levels;
	    }

	    private static int currentLevelsFromNBTOrTank(ItemStack stack, int side, FluidTank tank) {
	        int levels = getFiniteLevels(stack, side);
	        if (levels <= 0) {
	            // derive desde tanque y guardar
	            return syncLevelsFromTankToNBT(stack, side, tank);
	        }
	        return levels;
	    }

	    // ======= Utilidades de raytrace heredadas de Item =======
	    protected MixinItemHose() { super(); } // ctor mixin

	    

	    
	    // ===================== OVERWRITE =====================
	    @Overwrite
	    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
	        ItemStack stack = playerIn.getHeldItem(handIn);
	        if (!CapabilityUtils.isWearingBackpack(playerIn) || handIn != EnumHand.MAIN_HAND) {
	            return new ActionResult<>(EnumActionResult.FAIL, stack);
	        }

	        // Asegura tener NBT base
	        if (stack.getTagCompound() == null) {
	            this.getTagCompound(stack);
	            return new ActionResult<>(EnumActionResult.SUCCESS, stack);
	        }

	        final int hoseMode = callGetHoseMode(stack);  // 1=Suck, 2=Spill, 3=Drink
	        final int hoseSide = callGetHoseTank(stack);  // 1=Left, 2=Right (si 0, asumimos Right)
	        final int side = (hoseSide == 1 ? 1 : 2);

	        InventoryTravelersBackpack inv = CapabilityUtils.getBackpackInv(playerIn);
	        FluidTank tank = this.getSelectedFluidTank(stack, inv);
	        final int MAX_LEVELS_TANK = getMaxLevelsForTank(tank);
	        final boolean sneak = playerIn.isSneaking();

	        // Raytrace como el original
	        RayTraceResult fluidRT = this.rayTrace(worldIn, playerIn, true);
	        RayTraceResult solidRT = this.rayTrace(worldIn, playerIn, false);

	        // === Estado actual (preferimos NBT) ===
	        int currentLevels = currentLevelsFromNBTOrTank(stack, side, tank); // conceptuales
	        FluidStack tankFs = (tank == null ? null : tank.getFluid());
	        ////int tankMB = (tankFs == null ? 0 : tankFs.amount);
	        Fluid tankFluid = (tankFs == null ? null : tankFs.getFluid());
	        String storedFluidName = getFiniteFluidName(stack, side);
	        if ((storedFluidName == null || storedFluidName.isEmpty()) && tankFluid != null) {
	            storedFluidName = tankFluid.getName();
	        }

	        // === MODO 1: SUCK (sólo recoger; JAMÁS colocar) ===
	        if (hoseMode == 1) {
	            if (fluidRT != null && fluidRT.typeOfHit == RayTraceResult.Type.BLOCK && playerIn.canPlayerEdit(fluidRT.getBlockPos(), fluidRT.sideHit, stack)) {
	                BlockPos pos = fluidRT.getBlockPos();
	                IBlockState state = worldIn.getBlockState(pos);
	                Block block = state.getBlock();

	                // ---- Caso 1 & 3: apuntando a finito -> recoger suavemente ----
	                if (block instanceof BlockFiniteFluid) {
	                    BlockFiniteFluid finiteBlock = (BlockFiniteFluid) block;
	                    Fluid blockFluid = finiteBlock.getFluid();
	                    int spaceLeft = Math.max(0, MAX_LEVELS_TANK - currentLevels);
	                    if (spaceLeft > 0) {
	                        int blockConcept = BlockFiniteFluid.getConceptualVolume(worldIn, pos, state); //state.getValue(BlockFiniteFluid.LEVEL) + 1;
	                        int delta = FiniteFluidLogic.FluidWorldInteraction.bucketRemoveFluidEvenLowCollect(worldIn, pos, blockConcept, spaceLeft, ((IFluidBlock)state.getBlock()).getFluid());
	                        if (delta > 0) {
	                            currentLevels += delta;
	                            if (currentLevels > MAX_LEVELS_TANK) currentLevels = MAX_LEVELS_TANK;
	                            String useName = (blockFluid != null ? blockFluid.getName() : storedFluidName);
	                            setFiniteLevelsAndFluid(stack, side, currentLevels, useName, MAX_LEVELS_TANK);
	                            syncTankFromLevels(tank, currentLevels, useName);
	                            inv.markTankDirty();
	                            worldIn.playSound(null, pos, blockFluid.getFillSound(), SoundCategory.BLOCKS, 1.0F, 1.0F);
	                            return new ActionResult<>(EnumActionResult.SUCCESS, stack);
	                        }
	                    }
	                }

	                // ---- Compat vanilla/Forge: IFluidBlock / BlockLiquid (recoger bucket) ----
	                // Mantiene la lógica original, pero al llenar, actualizamos NBT (levels + fluid)
	                if (block instanceof IFluidBlock) {
	                    Fluid fluid = ((IFluidBlock) block).getFluid();
	                    FluidStack fs = new FluidStack(fluid, Reference.BUCKET);
	                    if (tank.getFluidAmount() == 0 || tank.getFluid().isFluidEqual(fs)) {
	                        int amount = tank.fill(fs, false);
	                        if (amount > 0 && tank.getFluidAmount() + amount <= tank.getCapacity()) {
	                            worldIn.setBlockToAir(pos);
	                            tank.fill(fs, true);
	                            inv.markTankDirty();
	                            // NBT ++ 1000mb => +16 levels
	                            String name = (fs.getFluid() != null ? fs.getFluid().getName() : storedFluidName);
	                            int addedLevels = mbToLevels(amount);
	                            currentLevels = Math.min(MAX_LEVELS_TANK, currentLevels + addedLevels);
	                            setFiniteLevelsAndFluid(stack, side, currentLevels, name, MAX_LEVELS_TANK);
	                            worldIn.playSound(null, pos, fs.getFluid().getFillSound(), SoundCategory.BLOCKS, 1.0F, 1.0F);
	                            return new ActionResult<>(EnumActionResult.SUCCESS, stack);
	                        }
	                    }
	                } else if (block instanceof BlockLiquid) {
	                    BlockLiquidWrapper wrapper = new BlockLiquidWrapper((BlockLiquid) block, worldIn, pos);
	                    FluidStack fs = new FluidStack(wrapper.getTankProperties()[0].getContents(), Reference.BUCKET);
	                    if (tank.getFluidAmount() == 0 || tank.getFluid().isFluidEqual(fs)) {
	                        int amount = tank.fill(fs, false);
	                        if (amount > 0 && tank.getFluidAmount() + amount <= tank.getCapacity()) {
	                            worldIn.setBlockToAir(pos);
	                            tank.fill(fs, true);
	                            inv.markTankDirty();
	                            String name = (fs.getFluid() != null ? fs.getFluid().getName() : storedFluidName);
	                            int addedLevels = mbToLevels(amount);
	                            currentLevels = Math.min(MAX_LEVELS_TANK, currentLevels + addedLevels);
	                            setFiniteLevelsAndFluid(stack, side, currentLevels, name, MAX_LEVELS_TANK);
	                            worldIn.playSound(null, pos, fs.getFluid().getFillSound(), SoundCategory.BLOCKS, 1.0F, 1.0F);
	                            return new ActionResult<>(EnumActionResult.SUCCESS, stack);
	                        }
	                    }
	                }
	            }

	            // Importante: en modo SUCK nunca colocamos, así que terminamos aquí
	            return new ActionResult<>(EnumActionResult.FAIL, stack);
	        }

	        // === MODO 2: SPILL (sólo colocar; JAMÁS recoger) ===
	        if (hoseMode == 2) {
	            if (solidRT != null && solidRT.typeOfHit == RayTraceResult.Type.BLOCK) {
	                // Calcular pos de colocación (como el original)
	                int x = solidRT.getBlockPos().getX();
	                int y = solidRT.getBlockPos().getY();
	                int z = solidRT.getBlockPos().getZ();
	                if (!worldIn.getBlockState(solidRT.getBlockPos()).getBlock().isReplaceable(worldIn, solidRT.getBlockPos())) {
	                    switch (solidRT.sideHit) {
	                        case WEST:  --x; break;
	                        case EAST:  ++x; break;
	                        case DOWN:  --y; break;
	                        case NORTH: --z; break;
	                        case SOUTH: ++z; break;
	                        case UP:    ++y; break;
	                        default: break;
	                    }
	                }
	                BlockPos placePos = new BlockPos(x, y, z);
	                IBlockState at = worldIn.getBlockState(placePos);
	                Block atBlock = at.getBlock();

	                // Si tenemos levels en NBT, intentamos colocar "finito" primero
	                if (currentLevels > 0) {
	                    int fluidTypeId = FiniteFluidLogic.GeneralPurposeLogic.getFluidIndex(storedFluidName);
	                    if (fluidTypeId != -1) {
	                        Block finiteFlowing = ((NewFluidType) FiniteFluidLogic.liquids.get(fluidTypeId)).flowingBlock;

	                        // Caso 5: sneak + colocar suavemente en red (pos y adyacentes)
	                        if (atBlock instanceof BlockFiniteFluid) {
	                            List<BlockPos> targets = Arrays.asList(placePos, placePos.north(), placePos.south(), placePos.east(), placePos.west());
	                            // Vamos a colocar ya sea 1 cubo (16) o todo si estamos en sneak
	                            int toPlace = sneak ? currentLevels : Math.min(currentLevels, LEVELS_PER_BUCKET);
	                            int remaining = FiniteFluidLogic.FluidWorldInteraction.distributeEqually(worldIn, targets, toPlace, fluidTypeId);
	                            int placed = toPlace - remaining;
	                            if (placed > 0) {
	                                currentLevels -= placed;
	                                setFiniteLevelsAndFluid(stack, side, currentLevels, storedFluidName, MAX_LEVELS_TANK);
	                                syncTankFromLevels(tank, currentLevels, storedFluidName);
	                                worldIn.playSound(null, placePos, FluidRegistry.getFluid(storedFluidName).getEmptySound(), SoundCategory.BLOCKS, 1.0F, 1.0F);
	                                inv.markTankDirty();
	                                return new ActionResult<>(EnumActionResult.SUCCESS, stack);
	                            }
	                        } else {
	                            // Caso 6 (sneak): distribuir incluso si no está lleno (un solo target)
	                            if (sneak && currentLevels <= 16) {
	                                List<BlockPos> targets = Arrays.asList(placePos);
	                                int remaining = FiniteFluidLogic.FluidWorldInteraction.distributeEquallyNoAdyFluid(worldIn, targets, currentLevels, finiteFlowing);
	                                int placed = currentLevels - remaining;
	                                if (placed > 0) {
	                                    currentLevels = remaining;
	                                    setFiniteLevelsAndFluid(stack, side, currentLevels, storedFluidName, MAX_LEVELS_TANK);
	                                    syncTankFromLevels(tank, currentLevels, storedFluidName);
	                                    worldIn.playSound(null, placePos, FluidRegistry.getFluid(storedFluidName).getEmptySound(), SoundCategory.BLOCKS, 1.0F, 1.0F);
	                                    inv.markTankDirty();
	                                    return new ActionResult<>(EnumActionResult.SUCCESS, stack);
	                                }
	                            }
	                            // Caso 2 (normal): sólo colocar si tenemos al menos 1 cubo (16 conceptuales)
	                            else if (currentLevels >= LEVELS_PER_BUCKET) {
	                                List<BlockPos> targets = Arrays.asList(placePos);
	                                int toPlace = currentLevels; // la función maneja remanente
	                                int remaining = FiniteFluidLogic.FluidWorldInteraction.distributeEqually(worldIn, targets, toPlace, fluidTypeId);
	                                if (remaining != currentLevels) {
	                                    currentLevels = remaining;
	                                    setFiniteLevelsAndFluid(stack, side, currentLevels, storedFluidName, MAX_LEVELS_TANK);
	                                    syncTankFromLevels(tank, currentLevels, storedFluidName);
	                                    worldIn.playSound(null, placePos, FluidRegistry.getFluid(storedFluidName).getEmptySound(), SoundCategory.BLOCKS, 1.0F, 1.0F);
	                                    inv.markTankDirty();
	                                    return new ActionResult<>(EnumActionResult.SUCCESS, stack);
	                                }
	                            }
	                        }
	                    }
	                }

	                // ---- Compat vanilla/Forge: colocar como líquido normal del tanque ----
	                if (tank.getFluid() != null && tank.getFluidAmount() > 0 && tank.getFluid().getFluid().canBePlacedInWorld()) {
	                    FluidStack fluidStack = tank.getFluid();
	                    Material material = at.getMaterial();
	                    boolean replaceable = !material.isSolid();

	                    // Dimensiones que vaporizan agua (copiado del original) + sincronía NBT
	                    if (worldIn.provider.doesWaterVaporize() && fluidStack.getFluid() == FluidRegistry.WATER) {
	                        // drena 1000mb y refleja en NBT (-16)
	                        FluidStack drained = tank.drain(Reference.BUCKET, true);
	                        inv.markTankDirty();
	                        if (drained != null && drained.amount > 0) {
	                            int minusLevels = mbToLevels(drained.amount);
	                            currentLevels = Math.max(0, currentLevels - minusLevels);
	                            setFiniteLevelsAndFluid(stack, side, currentLevels, storedFluidName, MAX_LEVELS_TANK);
	                        }

	                        worldIn.playSound(null, x + 0.5D, y + 0.5D, z + 0.5D, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.5F, 2.6F + (worldIn.rand.nextFloat() - worldIn.rand.nextFloat()) * 0.8F);
	                        for (int i = 0; i < 3; ++i) {
	                            double d0 = placePos.getX() + worldIn.rand.nextDouble();
	                            double d1 = placePos.getY() + worldIn.rand.nextDouble() * 0.5D + 0.5D;
	                            double d2 = placePos.getZ() + worldIn.rand.nextDouble();
	                            worldIn.spawnParticle(EnumParticleTypes.SMOKE_LARGE, d0, d1, d2, 0.0D, 0.0D, 0.0D);
	                        }
	                        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
	                    } else {
	                        // Colocación normal (bucket)
	                        FluidStack drainedSim = tank.drain(Reference.BUCKET, false);
	                        if (drainedSim != null && drainedSim.amount >= Reference.BUCKET) {
	                            if (!worldIn.isRemote && replaceable && !material.isLiquid()) {
	                                worldIn.destroyBlock(placePos, true);
	                            }
	                            if (worldIn.setBlockState(placePos, fluidStack.getFluid().getBlock().getDefaultState())) {
	                                tank.drain(Reference.BUCKET, true);
	                                worldIn.getBlockState(placePos).neighborChanged(worldIn, placePos, fluidStack.getFluid().getBlock(), placePos);
	                            }
	                            worldIn.playSound(null, placePos, drainedSim.getFluid().getEmptySound(), SoundCategory.BLOCKS, 1.0F, 1.0F);
	                            inv.markTankDirty();

	                            // NBT -- 1000mb => -16
	                            int minusLevels = mbToLevels(Reference.BUCKET);
	                            currentLevels = Math.max(0, currentLevels - minusLevels);
	                            setFiniteLevelsAndFluid(stack, side, currentLevels, storedFluidName, MAX_LEVELS_TANK);

	                            return new ActionResult<>(EnumActionResult.SUCCESS, stack);
	                        }
	                    }
	                }
	            }

	            // En modo SPILL nunca recogemos, así que si no colocamos, fallamos
	            return new ActionResult<>(EnumActionResult.FAIL, stack);
	        }

	        // === MODO 3 (Drink): intacto (comportamiento original) ===
	        if (hoseMode == 3) {
	            if (tank != null && tank.getFluid() != null) {
	                if (FluidEffectRegistry.hasFluidEffectAndCanExecute(tank.getFluid(), worldIn, playerIn)) {
	                    playerIn.setActiveHand(EnumHand.MAIN_HAND);
	                    return new ActionResult<>(EnumActionResult.SUCCESS, stack);
	                }
	            }
	        }

	        return new ActionResult<>(EnumActionResult.FAIL, stack);
	    }
}