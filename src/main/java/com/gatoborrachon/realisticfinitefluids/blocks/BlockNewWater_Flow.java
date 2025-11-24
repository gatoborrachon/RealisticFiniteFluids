package com.gatoborrachon.realisticfinitefluids.blocks;

import java.util.Random;

import com.gatoborrachon.realisticfinitefluids.init.ModBlocks;
import com.gatoborrachon.realisticfinitefluids.init.ModConfig;
import com.gatoborrachon.realisticfinitefluids.init.ModItems;
import com.gatoborrachon.realisticfinitefluids.logic.FiniteFluidLogic;
import com.gatoborrachon.realisticfinitefluids.logic.NewFluidType;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;

public class BlockNewWater_Flow extends BlockFiniteFluid 
{
	public BlockNewWater_Flow(String name, Fluid fluid, Material material)
    {
        super(name, fluid, material);
		setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
		ModBlocks.BLOCKS.add(this);
		ModItems.ITEMS.add(new ItemBlock(this).setRegistryName(this.getRegistryName()));
        this.setDefaultState(this.blockState.getBaseState().withProperty(LEVEL, Integer.valueOf(MAXIMUM_LEVEL)));

        this.setTickRandomly(FiniteFluidLogic.shouldTickRandomly);
    }


    /**
     * Ticks the block if it's been scheduled
     */
    @Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
        if (!world.isRemote)
        {
        	
        	if (pos == new BlockPos(251, 62, 79)) {
        		System.out.println("level "+getVolume(world, pos, state));
        		System.out.println("math amount "+FiniteFluidLogic.GeneralPurposeLogic.getCalc());
        	}
        	//SETEAR EL TIPO DE FLUIDO ACTUAL EN LOS CALCULOS DE LA LOGICA
        	FiniteFluidLogic.GeneralPurposeLogic.setCurrentFluidIndex(this);

        	//DETERMIANR TANTO EL BLOQUE ACTUAL COMO EL BLOQUE DE ABAJO
            IBlockState currentaState = world.getBlockState(pos);
            
            //Aca yo controlo lo de interaccion de flowing con ocean xd
            //Avoid too much block updates over oceanic liquid
            if (isOceanBlock(world, pos.down(), null, FiniteFluidLogic.onFiniteFluidIndex) //downBlock == FiniteFluidLogic.liquids.get(FiniteFluidLogic.GeneralPurposeLogic.getFluidIndex(currentBlock)).oceanBlock 
            	&& BlockFiniteFluid.getVolume(world, pos, currentaState) < Q1_HIGH) { //8
            	int newValue = BlockFiniteFluid.getVolume(world, pos, world.getBlockState(pos))/2; //3
            	//world.setBlockState(pos, currentaState.withProperty(BlockFiniteFluid.LEVEL, newValue));
            	BlockFiniteFluid.setBlockState(world, pos, BlockFiniteFluid.setVolume(world, pos, currentaState, newValue));
            }
            
            //Despertar bloques oceanicos (para evitar dejarlos sin actualizar, y que se vean raros)
            FiniteFluidLogic.InfiniteWaterSource.wakeOcean(world, pos);

            //REVISAR SI ACTUALMENTE PODEMOS EJECUTAR CALCULOS Y NO SOBRECARGAR EL CPU
            if (FiniteFluidLogic.GeneralPurposeLogic.getCalc() > FiniteFluidLogic.GeneralPurposeLogic.getMaxCalc())
            {
            	world.scheduleUpdate(pos, this, this.tickRate(world));
            }
            
            //SI SI PODEMOS HACER CALCULOS -->
            else
            {
            	//Obtenemos el bloque inmediatamente abajo (dependiendo la gravedad del bloque actual, si es un gas o un liquidp)
                Block belowBlock1 = world.getBlockState(new BlockPos(pos.getX(), pos.getY() - 1 * FiniteFluidLogic.GeneralPurposeLogic.getFluidGravity(), pos.getZ())).getBlock();

                //sI EL BLOQUE DE ABAJO NO ES AIRE, Y SI NO TIENE EN SU PROXIMIDAD AL AIRE
                //OSEA, UNA PLANCHA DE BLOQUES DEBAJO
                if (belowBlock1 != Blocks.AIR & !FiniteFluidLogic.GeneralPurposeLogic.hasAdjacentTarjetBlocksHorizontal(world, pos, Blocks.AIR))
                {
                	//VOLVEMOS A OBTENER LA POSICION DEL BLOQUE ABAJO (DEPENDIENDO LA GRAVEDAD DEL BLOQUE ACTUAL)
                	BlockPos posBelow = new BlockPos(pos.getX(), pos.getY() - 1 * FiniteFluidLogic.GeneralPurposeLogic.getFluidGravity(), pos.getZ());
                    
                	//PRIMERO:
                	//1.- Si los calculos actuales son mayores al 65% de calculos permitidos
                	//2.- Y si el volumen del bloque actual es menor al Quartil 1
                	//3.1.- Y si el bloque de abajo NO es un fluido finito realista
                	//3.2.- O si el volumen del bloque de abajo es menor al nivel maximo.
                	if ((float)FiniteFluidLogic.GeneralPurposeLogic.getCalc() > (float)FiniteFluidLogic.GeneralPurposeLogic.getMaxCalc() * 0.65F && BlockFiniteFluid.getVolume(world, pos, world.getBlockState(pos)) < Q1_HIGH & (!FiniteFluidLogic.GeneralPurposeLogic.isRealisticFluid(belowBlock1) || BlockFiniteFluid.getVolume(world, posBelow, world.getBlockState(posBelow)) < MAXIMUM_LEVEL))
                    {
                		//NOS ESPERAMOS AL PROXIMO TICK
                    	world.scheduleUpdate(pos, this, this.tickRate(world));
                        return;
                    }

                	//SI LOS CALCULOS ACTUALES SON MAYORES AL 50% DE CALCULOS PERMITIDPS
                    if ((float)FiniteFluidLogic.GeneralPurposeLogic.getCalc() > (float)FiniteFluidLogic.GeneralPurposeLogic.getMaxCalc() * 0.5F)
                    {
                    	//BUSCAMOS AL JUGADOR MAS PROXIMO
                        EntityPlayer nearestPlayer = world.getClosestPlayer((double)pos.getX(), (double)pos.getY(), (double)pos.getZ(), FiniteFluidLogic.GeneralPurposeLogic.getPlayerDistanceToCalc(), true);

                        //SI EL JUGADOR MAS PROXIMO NO EXISTE
                        if (nearestPlayer == null)
                        {
                        	//NOS ESPERAMOS AL PROXIMO TICK
                        	world.scheduleUpdate(pos, this, this.tickRate(world));
                            return;
                        }
                    }
                }

                //SI TODO LO ANTERIOR NO SE CUMPLIO --> 
                //AÑADIMOS UN CALCULO A LA LSITA DE CALCULOS ACTUALES
                FiniteFluidLogic.GeneralPurposeLogic.addCalc();

                //SI EL BLOQUE DE ABAJO (DPENDIENDO GRAVEDAD) ES UN BLOQUE DEBUG
                if (world.getBlockState(new BlockPos(pos.getX(), pos.getY() - 1 * FiniteFluidLogic.GeneralPurposeLogic.getFluidGravity(), pos.getZ())).getBlock() == Blocks.DIAMOND_BLOCK)
                {
                	//LE PONEMOS AL BLOQUE ACTUALEL VOLUMEN MAXIMO SI ES QUE NO YA TIENE EL VOLUMEN MAXIMO
                    IBlockState currentState = world.getBlockState(pos);
                    IBlockState newState = BlockFiniteFluid.setVolume(world, pos, currentState, MAXIMUM_LEVEL); //currentState.withProperty(BlockFiniteFluid.LEVEL, 15);
                    if (BlockFiniteFluid.getVolume(world, pos, currentState) < MAXIMUM_LEVEL) { //15
                    	BlockFiniteFluid.setBlockState(world, pos, newState);
                    //world.setBlockState(pos, newState, 3);
                    }
                    
                    //CREAMOS UN BLOQUE NUEVO DEL MISMO FLUIDO ARRIBA CON EL MAXIMO DE FLUIDO
                	BlockFiniteFluid.setBlockState(world, pos.up(), newState);
                    //world.setBlockState(pos.up(), newState, 3);
                    
                	//EJECUTAMOS LA LOGICA DE MOVIMIENTO DEL BLOQUE DE ARRIBA
                	FiniteFluidLogic.GeneralPurposeLogic.tryLiquidMove(world, pos.up());
                    world.scheduleUpdate(pos, this, this.tickRate(world));
                }
                
                //SI NO EXISTEN BLOQUES DE FLUIDO FINITO ALEDAÑOS
                else if (!FiniteFluidLogic.GeneralPurposeLogic.checkForNeighborLiquid(world, pos))
                {
                	//CALCULAMOS EL NIVEL DEL BLOQUE ACTUAL
                    int newLevel = BlockFiniteFluid.getVolume(world, pos, world.getBlockState(pos));
                    
                    //Y CALCULAMOS SI NOS PODEMOS MOVER
                    if (FiniteFluidLogic.GeneralPurposeLogic.tryLiquidMove(world, pos))
                    {
                    	//SI NOS VOMIMOS, PROGRAMAMOS UN TICK
                    	world.scheduleUpdate(pos, this, this.tickRate(world));
                    }
                    
                    //SI NO NOS PODEMOS MOVER --> NOS CONVERTIMOS STILL
                    else
                    {
                    	//GUARDAMOS EL TIPO DE FLUIDO ACTUAL
                    	FiniteFluidLogic.GeneralPurposeLogic.setCurrentFluidIndex(this);
                    	//int currentFluidIndex = FiniteFluidLogic.GeneralPurposeLogic.getFluidIndex(this);

                    	//CALCULAMOS UN BLOCKSTATE DEL FLUDO ACTUAL STILL
                    	Block stillBlock = ((NewFluidType) FiniteFluidLogic.liquids.get(FiniteFluidLogic.onFiniteFluidIndex)).stillBlock;
                    	//IBlockState newState = stillBlock.getDefaultState().withProperty(BlockFiniteFluid.LEVEL, newLevel);
                    	//world.setBlockState(pos, newState, 3);
                    	
                    	//Y COLOCAMOS ESTE BLOQUE STILL EN NUESTRO LUGAR CON NUESTRO VOLUMEN ACTUAL
                    	BlockFiniteFluid.setBlockState(world, pos, BlockFiniteFluid.setVolume(null, null, stillBlock.getDefaultState(), newLevel));
                    	
                    	//CALCULAMOS LA POSICION DEL BLOQUE DE ABAJO POR GRAVEDAD
                    	BlockPos belowBlock = new BlockPos(pos.getX(), pos.getY() - 1 * FiniteFluidLogic.GeneralPurposeLogic.getFluidGravity(), pos.getZ());
                    	
                    	//SI EL BLOQUE ACTUAL TIENE EL MISMO MATERIAL QUE EL BLOQUE DE ABAJO
                    	if (world.getBlockState(pos).getMaterial() == world.getBlockState(belowBlock).getMaterial()) {
                            
                            //TRAS CIERTA PROBABILIDAD, HACEMOS UNA BUSQUEDA DE BORDES OCEANICOS
                            if (rand.nextInt(60) == 0) {
                            	FiniteFluidLogic.InfiniteWaterSource.borderOceanCheck(world, belowBlock, true);
                            }
                            
                            //SI NO TIENE EL MISMO MATERAIAL
                    	} else if (world.getBlockState(pos).getMaterial() != world.getBlockState(belowBlock).getMaterial()) {
                    		//HACEMOS LA BUSQUEDA DE BORDES DE LIQUIDO OCEANICO TODAS LAS VECES
                        	FiniteFluidLogic.InfiniteWaterSource.borderOceanCheck(world, belowBlock, true);                		
                    	}
                    }
                }
            }
        }
		super.updateTick(world, pos, state, rand);
    }
    
    
	@Override
    public void onEntityCollision(World worldIn, BlockPos pos, IBlockState state, Entity entityIn) {
		if (entityIn instanceof EntityPlayer && ((EntityPlayer)entityIn).isCreative() && !ModConfig.flowingWaterShouldMoveCreativePlayer /*&& state.getMaterial() == Material.WATER*/) return;
		
		Vec3d flow = calculateFlowVector(worldIn, pos);
        
        double strength = 0.014D;
        entityIn.motionX += -flow.x * strength;
        entityIn.motionY += -flow.y * strength;
        entityIn.motionZ += -flow.z * strength;
    }
	

	@Override
    public boolean interactWithLiquid(World world, BlockPos waterPos, BlockPos targetPos)
    {
        IBlockState waterState = world.getBlockState(waterPos);
        IBlockState targetState = world.getBlockState(targetPos);
        
        if (targetState.getMaterial() != Material.LAVA) return false;
        
        Block waterBlock = waterState.getBlock();
        Block targetBlock = targetState.getBlock();

        int waterMeta = waterBlock.getMetaFromState(waterState);
        int targetMeta = targetBlock.getMetaFromState(targetState);

        int waterType = FiniteFluidLogic.GeneralPurposeLogic.getFluidIndex(waterBlock);     
        int targetType = FiniteFluidLogic.GeneralPurposeLogic.getFluidIndex(targetBlock);
        
        if (waterType < MINIMUM_LEVEL || targetType < MINIMUM_LEVEL) return false;
        
        if (targetBlock instanceof BlockFiniteFluid) {
            if (waterMeta >= Q3_LOW) {
            	
                if (targetMeta < Q1_LOW) {
                    //world.setBlockToAir(waterPos);
                    world.setBlockState(targetPos, Blocks.COBBLESTONE.getDefaultState());
                    BlockNewLava_Flow.triggerLavaMixEffects(world, targetPos);
                    return true;
                }

                if (targetMeta < Q3_LOW) {
                    world.setBlockState(targetPos, Blocks.STONE.getDefaultState());
                    //world.setBlockToAir(waterPos);
                    BlockNewLava_Flow.triggerLavaMixEffects(world, targetPos);
                    return true;
                }

                if (targetMeta >= Q3_LOW) {
                    world.setBlockToAir(waterPos);
                    world.setBlockState(targetPos, Blocks.OBSIDIAN.getDefaultState());
                    BlockNewLava_Flow.triggerLavaMixEffects(world, targetPos);
                    return true;
                }

            } else if (waterMeta > Q1_HIGH) {
                if (targetMeta <= Q1_LOW) {
                    world.setBlockState(targetPos, Blocks.STONE.getDefaultState());
                    //world.setBlockToAir(waterPos);
                    BlockNewLava_Flow.triggerLavaMixEffects(world, targetPos);
                    return true;
                }

                if (targetMeta > Q1_LOW) {
                    //world.setBlockToAir(waterPos);
                    world.setBlockState(targetPos, Blocks.COBBLESTONE.getDefaultState());
                    BlockNewLava_Flow.triggerLavaMixEffects(world, targetPos);
                    return true;
                }
                if (targetMeta >= Q3_LOW) {
                    world.setBlockToAir(waterPos);
                    BlockNewLava_Flow.triggerLavaMixEffects(world, targetPos);
                    return true;
                }

            } else {
                world.setBlockToAir(waterPos);
                BlockNewLava_Flow.triggerLavaMixEffects(world, targetPos);
                return true;
            }

            
        } else {
        	//vanilla water?
        }
        

    	
        return false;    
        }


}
