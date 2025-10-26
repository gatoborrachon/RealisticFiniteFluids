package com.gatoborrachon.realisticfinitefluids.mixin.latemixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;


@Mixin(Item.class)
public abstract class MixinItem {

    @Shadow
    protected abstract RayTraceResult rayTrace(World world, EntityPlayer player, boolean useLiquids);

	public abstract ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn);

}
