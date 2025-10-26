package com.gatoborrachon.realisticfinitefluids.mixin.earlymixins;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.gatoborrachon.realisticfinitefluids.init.ModBlocks;

import net.minecraft.block.BlockIce;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(BlockIce.class)
public abstract class MixinBlockIce {
	
    // Inyectar al inicio de harvestBlock (func_180657_a) y cancelar para reemplazar
    @Inject(method = "func_180657_a", at = @At("HEAD"), cancellable = true, remap = true)
    private void onHarvestBlock(World worldIn, EntityPlayer player, BlockPos pos, IBlockState state,
                                @Nullable TileEntity te, ItemStack stack, CallbackInfo ci) {
        if (!worldIn.isRemote) {
            worldIn.setBlockState(pos, ModBlocks.FINITE_WATER_FLOWING.getDefaultState(), 3);
            ci.cancel();  // Cancela la ejecución del método original para evitar comportamiento por defecto
        }
    }

    // Inyectar al inicio de turnIntoWater (func_185679_b) y reemplazar el agua aún que no retorna boolean
    @Inject(method = "func_185679_b", at = @At("HEAD"), cancellable = true, remap = true)
    private void onTurnIntoWater(World world, BlockPos pos, CallbackInfo ci) {
        if (!world.isRemote) {
            world.setBlockState(pos, ModBlocks.FINITE_WATER_FLOWING.getDefaultState(), 3);
            ci.cancel();  // Cancela la ejecución original para evitar poner el agua normal
        }
    }
    
    /*@Inject(method = "func_180657_a", at = @At( //MCP --> func_180657_a - harvestBlock

            value = "INVOKE",
            target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;)Z"
    ), cancellable = true,  remap = true)
    private void replaceFlowingWater(World worldIn, EntityPlayer player, BlockPos pos, IBlockState state,
                                      @Nullable TileEntity te, ItemStack stack, CallbackInfo ci) {
        IBlockState myWater = ModBlocks.FINITE_WATER_FLOWING.getDefaultState();
        worldIn.setBlockState(pos, myWater, 3);
        ci.cancel();
    }


    @Redirect(method = "func_185679_b", at = @At( //func_185679_b --> turnIntoWater
            value = "INVOKE",
            target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;)Z"
    ), remap = true)
    private boolean replaceStillWater(World world, BlockPos pos, IBlockState state) {
        return world.setBlockState(pos, ModBlocks.FINITE_WATER_FLOWING.getDefaultState(), 3);
    }*/
}
