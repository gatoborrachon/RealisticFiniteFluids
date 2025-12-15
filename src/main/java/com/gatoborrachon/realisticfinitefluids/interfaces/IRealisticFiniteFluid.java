package com.gatoborrachon.realisticfinitefluids.interfaces;

import java.util.Map;
import java.util.Random;

import javax.annotation.Nullable;

import com.gatoborrachon.realisticfinitefluids.blocks.properties.UnlistedPropertyBoolean;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.common.property.PropertyFloat;
import net.minecraftforge.fluids.Fluid;

public interface IRealisticFiniteFluid {
    IUnlistedProperty<Vec3d> getFlowDirectionProperty();
	IUnlistedProperty<Map<EnumFacing, IBlockState>> getNeighborStates();
	IUnlistedProperty<Integer> getFluidColor();
	UnlistedPropertyBoolean getIsStill();
	PropertyFloat getCornerLevel(int index);
	
	int getVolume(IBlockAccess world, BlockPos pos, IBlockState state);
	int getConceptualVolume(IBlockAccess world, BlockPos pos, IBlockState state);
	IBlockState setVolume(World world, BlockPos pos, IBlockState state, int level);
	IBlockState setConceptualVolume(World world, BlockPos pos, IBlockState state, int level);
	void setBlockState(World world, BlockPos pos, IBlockState state);
	Boolean isEntityInsideMaterialForOverlay(IBlockAccess world, BlockPos pos, IBlockState state, Entity entity, double eyeY, Material material);
	boolean tryFreezeWater(World world, BlockPos pos, IBlockState state, Random rand);
	boolean isNearHotBlock(World world, BlockPos pos);
	boolean shouldEvap(World world, BlockPos pos, Random rand);
	boolean interactWithLiquid(World world, BlockPos currentPos, BlockPos targetPos);
	boolean shouldSearchOutward(Material material);
	boolean shouldFlowToNeighbor(IBlockAccess world, BlockPos posFrom, BlockPos posTo);
	Vec3d calculateFlowVector(IBlockAccess world, BlockPos pos);	
	BlockPos getPositionOnGravityDirection(BlockPos originalPos);
	boolean isOceanBlock(@Nullable IBlockAccess world, @Nullable BlockPos pos, @Nullable IBlockState state, int fluidType);
	
	Fluid getFluid();

    
}
