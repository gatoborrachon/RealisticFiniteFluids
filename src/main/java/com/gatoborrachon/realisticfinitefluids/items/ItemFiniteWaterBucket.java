package com.gatoborrachon.realisticfinitefluids.items;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.gatoborrachon.realisticfinitefluids.blocks.BlockFiniteFluid;
import com.gatoborrachon.realisticfinitefluids.blocks.BlockNewWater_Flow;
import com.gatoborrachon.realisticfinitefluids.init.ModBlocks;
import com.gatoborrachon.realisticfinitefluids.init.ModItems;
import com.gatoborrachon.realisticfinitefluids.logic.FiniteFluidLogic;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBucket;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.wrappers.FluidBucketWrapper;

public class ItemFiniteWaterBucket extends ItemBucket  {

    private final Block containedBlock;

      public ItemFiniteWaterBucket(String name, Block containedBlock) {
            super(containedBlock);
            this.containedBlock = containedBlock;
            this.setContainerItem(Items.BUCKET); // Te regresa cubeta vacía
            this.setMaxStackSize(1);
            this.setCreativeTab(CreativeTabs.MISC); // Cambia si tienes tu propia tab
            this.setUnlocalizedName(name);
            this.setRegistryName(name);
    		ModItems.ITEMS.add(this);

        }

        @Override
        public ItemStack onItemUseFinish(ItemStack stack, World worldIn, EntityLivingBase entityLiving) {
            return new ItemStack(Items.BUCKET);
        }

        /**
         * Called when the equipped item is right clicked.
         */
        @Override
        public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
            ItemStack stack = playerIn.getHeldItem(handIn);

            // Interactuar con IFluidHandler
            RayTraceResult rayTraceResult = this.rayTrace(worldIn, playerIn, false);
            if (rayTraceResult == null || rayTraceResult.typeOfHit != RayTraceResult.Type.BLOCK)
                return new ActionResult<>(EnumActionResult.PASS, stack);

            BlockPos pos = rayTraceResult.getBlockPos();
            EnumFacing face = rayTraceResult.sideHit;
            pos = pos.offset(face);

            if (!playerIn.canPlayerEdit(pos, face, stack)) {
                return new ActionResult<>(EnumActionResult.FAIL, stack);
            }

            // ¿Hay un IFluidHandler?
            IFluidHandler handler = FluidUtil.getFluidHandler(worldIn, pos, face);
            if (handler != null) {
                FluidStack fluid = new FluidStack(FluidRegistry.WATER, Fluid.BUCKET_VOLUME);
                int filled = handler.fill(fluid, true);
                if (filled > 0 && !playerIn.capabilities.isCreativeMode) {
                    return new ActionResult<>(EnumActionResult.SUCCESS, new ItemStack(Items.BUCKET));
                }
            } 
            else
            {
                boolean flag1 = worldIn.getBlockState(pos).getBlock().isReplaceable(worldIn, pos);
                BlockPos blockpos1 = flag1 && rayTraceResult.sideHit == EnumFacing.UP ? pos : pos.offset(rayTraceResult.sideHit);

                if (!playerIn.canPlayerEdit(blockpos1, rayTraceResult.sideHit, stack))
                {
                    return new ActionResult<ItemStack>(EnumActionResult.FAIL, stack);
                }
                else if (this.tryPlaceContainedLiquid(playerIn, worldIn, pos))
                {
                    if (playerIn instanceof EntityPlayerMP)
                    {
                        CriteriaTriggers.PLACED_BLOCK.trigger((EntityPlayerMP)playerIn, blockpos1, stack);
                    }

                    playerIn.addStat(StatList.getObjectUseStats(this));
                    return !playerIn.capabilities.isCreativeMode ? new ActionResult(EnumActionResult.SUCCESS, new ItemStack(Items.BUCKET)) : new ActionResult(EnumActionResult.SUCCESS, stack);
                }
                else
                {
                    return new ActionResult<ItemStack>(EnumActionResult.FAIL, stack);
                }
            }
			return null;

            // Si no, coloca el líquido como Vanilla
            //return super.onItemRightClick(worldIn, playerIn, handIn);
        }
        
        
        public boolean tryPlaceContainedLiquid(@Nullable EntityPlayer player, World worldIn, BlockPos posIn)
        {
            if (this.containedBlock == Blocks.AIR)
            {
                return false;
            } else if (worldIn.getBlockState(posIn).getBlock() instanceof BlockLiquid) {
            	return false;
            }
            else
            {
                IBlockState iblockstate = worldIn.getBlockState(posIn);
                Material material = iblockstate.getMaterial();
                boolean flag = !material.isSolid();
                boolean flag1 = iblockstate.getBlock().isReplaceable(worldIn, posIn);

                if (!worldIn.isAirBlock(posIn) && !flag && !flag1)
                {
                    return false;
                }
                else
                {
                    if (worldIn.provider.doesWaterVaporize() && this.containedBlock instanceof BlockNewWater_Flow)
                    {
                        int l = posIn.getX();
                        int i = posIn.getY();
                        int j = posIn.getZ();
                        worldIn.playSound(player, posIn, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.5F, 2.6F + (worldIn.rand.nextFloat() - worldIn.rand.nextFloat()) * 0.8F);

                        for (int k = 0; k < 8; ++k)
                        {
                            worldIn.spawnParticle(EnumParticleTypes.SMOKE_LARGE, (double)l + Math.random(), (double)i + Math.random(), (double)j + Math.random(), 0.0D, 0.0D, 0.0D);
                        }
                    }
                    else
                    {
                        if (!worldIn.isRemote && (flag || flag1) && !material.isLiquid())
                        {
                            worldIn.destroyBlock(posIn, true);
                        }

                        SoundEvent soundevent = /*this.containedBlock == Blocks.FLOWING_LAVA ? SoundEvents.ITEM_BUCKET_EMPTY_LAVA :*/ SoundEvents.ITEM_BUCKET_EMPTY;
                        worldIn.playSound(player, posIn, soundevent, SoundCategory.BLOCKS, 1.0F, 1.0F);
                        //System.out.println("VERGA-2");
                        //worldIn.setBlockState(posIn, ModBlocks.FINITE_WATER_FLOWING.getDefaultState().withProperty(BlockFiniteFluid.LEVEL, 15));                        	
                    	BlockFiniteFluid.setBlockState(worldIn, posIn, BlockFiniteFluid.setVolume(ModBlocks.FINITE_WATER_FLOWING.getDefaultState(), BlockFiniteFluid.MAXIMUM_LEVEL));
                        //distributeFluidEqually(worldIn, posIn, 15);
                        /*IBlockState blockWhereToPutWater = worldIn.getBlockState(posIn);
                        if (blockWhereToPutWater.getBlock() instanceof BlockFiniteFluid) {
                        	int actualLevel = blockWhereToPutWater.getValue(RFFBlock.LEVEL);
                        	
                            BlockPos[] laterals = {
                                    posIn.north(), posIn.south(), posIn.east(), posIn.west()
                                };
                            for (BlockPos p : laterals) {
                                if (worldIn.getBlockState(p).getBlock() instanceof BlockFiniteFluid) {
                                    int neighborLevel = worldIn.getBlockState(p).getValue(RFFBlock.LEVEL);
                                    int toPut = 15-level;

                                        int newNeighborLevel = neighborLevel - toTake;
                                        if (newNeighborLevel < 0) worldIn.setBlockToAir(p);
                                        else worldIn.setBlockState(p, (worldIn.getBlockState(p)).getBlock().getDefaultState().withProperty(RFFBlock.LEVEL, newNeighborLevel));
                                        worldIn.setBlockToAir(pos);


                                    totalLevel += neighborLevel;

                                    if (totalLevel >= 15) { 
                                    	shouldContinue = true;
                                    	break;
                                    	} 
                                }
                            }
                        	
                            worldIn.setBlockState(posIn, ModBlocks.FINITE_WATER_FLOWING.getDefaultState().withProperty(RFFBlock.LEVEL, value));                        	
                        }*/
                        //worldIn.setBlockState(posIn, this.containedBlock.getDefaultState(), 11);
                    }

                    return true;
                }
            }
        }
        
        @Override
		public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand,
				EnumFacing facing, float hitX, float hitY, float hitZ) {
            ItemStack stack = player.getHeldItem(hand);
            //System.out.println("VERGA2");

            // Asegúrate de que estés usando tu cubeta con agua/lava finita
            if (stack.getItem() == ModItems.FINITE_WATER_BUCKET) {
                BlockPos targetPos = pos.offset(facing);

                if (!worldIn.isBlockLoaded(targetPos)) {
                    return EnumActionResult.FAIL;
                }

                // Este log te confirmará que sí entra aquí
                //System.out.println("Intentando colocar agua finita en " + targetPos);

                if (!player.canPlayerEdit(targetPos, facing, stack)) {
                    return EnumActionResult.FAIL;
                }

                IBlockState targetBlockState = worldIn.getBlockState(targetPos);
                Block targetBlock = targetBlockState.getBlock();

                // Permitir colocar solo si es aire o es tu propio bloque finito
                if (worldIn.isAirBlock(targetPos) || targetBlock instanceof BlockFiniteFluid) {
                    if (!worldIn.isRemote) {
                		//System.out.println(worldIn.getBlockState(targetPos).getMaterial() == Material.WATER);

                    	if (targetBlock instanceof BlockFiniteFluid && worldIn.getBlockState(targetPos).getMaterial() == Material.WATER) {
                    		//System.out.println("VERGA WATER BUCKET");
                    		if (BlockFiniteFluid.getConceptualVolume(targetBlockState) == 16) worldIn.setBlockState(targetPos.up(), ModBlocks.FINITE_WATER_FLOWING.getDefaultState());
                    		else
                            // Lógica de distribución equitativa
                            distributeFluidEqually(worldIn, targetPos, 16); // 15 como nivel completo                    		
                    	} else if (targetBlock instanceof BlockFiniteFluid && worldIn.getBlockState(targetPos).getMaterial() == Material.LAVA) {
                            worldIn.playSound(player, pos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.5F, 2.6F + (worldIn.rand.nextFloat() - worldIn.rand.nextFloat()) * 0.8F);
                            worldIn.setBlockState(targetPos, ModBlocks.FINITE_WATER_FLOWING.getDefaultState());
                    	} else {
                            //distributeFluidEqually(worldIn, targetPos, 15); // 15 como nivel completo                    		
                            //worldIn.setBlockState(targetPos, ModBlocks.FINITE_WATER_FLOWING.getDefaultState().withProperty(BlockFiniteFluid.LEVEL, 15));                        	
                        	BlockFiniteFluid.setBlockState(worldIn, targetPos, BlockFiniteFluid.setVolume(ModBlocks.FINITE_WATER_FLOWING.getDefaultState(), BlockFiniteFluid.MAXIMUM_LEVEL));

                    	}
                        worldIn.playSound(player, pos, SoundEvents.ITEM_BUCKET_EMPTY, SoundCategory.BLOCKS, 1.0F, 1.0F);
                    }
                    worldIn.playSound(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                    		SoundEvents.ITEM_BUCKET_EMPTY, SoundCategory.BLOCKS, 1.0F, 1.0F, false);

                    if (!player.capabilities.isCreativeMode) {
                        // Vaciar la cubeta
                        player.setHeldItem(hand, new ItemStack(Items.BUCKET));
                    }

                    return EnumActionResult.SUCCESS;
                } else {
                    return EnumActionResult.FAIL;
                }
            }

            return super.onItemUse(player, worldIn, pos, hand, facing, hitX, hitY, hitZ);
		}
        
        public static void distributeFluidEqually(World world, BlockPos pos, int incomingLevel) {
            int remaining = incomingLevel; // 15 niveles = 1000mb

            // Listas de bloques válidos
            List<BlockPos> lateralTargets = new ArrayList<>();
            List<BlockPos> diagonalTargets = new ArrayList<>();

            // 1. Revisa si el bloque actual es válido y no está lleno
            IBlockState centerState = world.getBlockState(pos);

            if (centerState.getBlock() instanceof BlockFiniteFluid && centerState.getMaterial() == Material.WATER) {
                int currentLevel = BlockFiniteFluid.getConceptualVolume(centerState); //centerState.getValue(BlockFiniteFluid.LEVEL)+1;
                int toAdd = Math.min(16 - currentLevel, remaining);
                if (toAdd > 0) {
                    //world.setBlockState(pos, centerState.withProperty(BlockFiniteFluid.LEVEL, currentLevel + toAdd-1));
                	BlockFiniteFluid.setBlockState(world, pos, BlockFiniteFluid.setVolume(centerState, currentLevel + toAdd-1));
                	remaining -= toAdd;
                }

            } else {
            	//world.setBlockState(pos, ModBlocks.FINITE_WATER_FLOWING.getDefaultState().withProperty(BlockFiniteFluid.LEVEL, remaining-1));
            	BlockFiniteFluid.setBlockState(world, pos, BlockFiniteFluid.setConceptualVolume(ModBlocks.FINITE_WATER_FLOWING.getDefaultState(), remaining));
            }

            // Coordenadas para laterales y diagonales
            BlockPos[] laterals = {
                pos.north(), pos.south(), pos.east(), pos.west()
            };
            BlockPos[] diagonals = {
                pos.north().east(), pos.north().west(),
                pos.south().east(), pos.south().west()
            };

            // 2. Recolecta objetivos laterales válidos
            for (BlockPos p : laterals) {
                IBlockState s = world.getBlockState(p);
                if (s.getBlock() instanceof BlockFiniteFluid && s.getMaterial() == Material.WATER && BlockFiniteFluid.getConceptualVolume(s) < 16) {
                    lateralTargets.add(p);
                }
            }

            // 3. Recolecta objetivos diagonales válidos
            for (BlockPos p : diagonals) {
                IBlockState s = world.getBlockState(p);
                if (s.getBlock() instanceof BlockFiniteFluid && s.getMaterial() == Material.WATER && BlockFiniteFluid.getConceptualVolume(s) < 16) {
                    diagonalTargets.add(p);
                }
            }

            // 4. Distribuye equitativamente a laterales
            remaining = FiniteFluidLogic.FluidWorldInteraction.distributeEqually(world, lateralTargets, remaining, 0); //0 --> FluidType of water

            // 5. Si sigue sobrando, distribuye a diagonales
            remaining = FiniteFluidLogic.FluidWorldInteraction.distributeEqually(world, diagonalTargets, remaining, 0); //0 --> FluidType of water

            //System.out.println(remaining);
            //System.out.println("VERGA-1");
            // 6. Si todavía sobra, intenta poner un nuevo bloque arriba
            if (remaining > 0 && remaining <=16) { //ChatGPT dijo que <16 --> <=16
                BlockPos above = pos.up();
                if (world.isAirBlock(above)) {
                    //world.setBlockState(above, ModBlocks.FINITE_WATER_FLOWING.getDefaultState().withProperty(BlockFiniteFluid.LEVEL, remaining -1));
                	BlockFiniteFluid.setBlockState(world, above, BlockFiniteFluid.setConceptualVolume(ModBlocks.FINITE_WATER_FLOWING.getDefaultState(), remaining));
                	remaining = 0;
                }
            }
            

            // Si aún queda, se pierde (puedes loguearlo si quieres)
        }
        
       


		
		
		
		@Override
		public Item getContainerItem() {
		    return Items.BUCKET; // Devuelve cubeta vacía
		}
		
		@Override
		public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable NBTTagCompound nbt) {
		    return new FluidBucketWrapper(stack) {
		        @Override
		        public FluidStack getFluid() {
		            return new FluidStack(FluidRegistry.WATER, Fluid.BUCKET_VOLUME);
		        }

		        @Override
		        public ItemStack getContainer() {
		            return new ItemStack(Items.BUCKET);
		        }

		        @Override
		        public FluidStack drain(FluidStack resource, boolean doDrain) {
		            if (resource != null && resource.getFluid() == FluidRegistry.WATER) {
		                if (doDrain) {
		                    // Sustituir el contenido por cubeta vacía
		                    this.container.shrink(1);
		                }
		                return new FluidStack(FluidRegistry.WATER, Fluid.BUCKET_VOLUME);
		            }
		            return null;
		        }

		        @Override
		        public FluidStack drain(int maxDrain, boolean doDrain) {
		            if (maxDrain >= Fluid.BUCKET_VOLUME) {
		                if (doDrain) {
		                    this.container.shrink(1);
		                }
		                return new FluidStack(FluidRegistry.WATER, Fluid.BUCKET_VOLUME);
		            }
		            return null;
		        }
		    };
		}

        /*// Esto es para que tu cubeta tenga soporte de fluidos
        @Override
        public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable NBTTagCompound nbt) {
            return new FluidBucketWrapper(stack) {
                @Override
                public FluidStack getFluid() {
                    return new FluidStack(FluidRegistry.WATER, Fluid.BUCKET_VOLUME);
                }
                
                @Override
                public FluidStack drain(FluidStack resource, boolean doDrain) {
                    if (resource != null && resource.getFluid() == FluidRegistry.WATER) {
                        if (doDrain) return new FluidStack(FluidRegistry.WATER, Fluid.BUCKET_VOLUME);
                        return resource;
                    }
                    return null;
                }
            };
        }*/
        
        
    }
