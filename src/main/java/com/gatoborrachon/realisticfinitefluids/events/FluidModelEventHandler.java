package com.gatoborrachon.realisticfinitefluids.events;

import com.gatoborrachon.realisticfinitefluids.compat.FluidCompat;
import com.gatoborrachon.realisticfinitefluids.interfaces.IRealisticFiniteFluid;
import com.gatoborrachon.realisticfinitefluids.logic.FiniteFluidLogic;
import com.gatoborrachon.realisticfinitefluids.logic.RealisticFiniteFluidFunctions;

//import git.jbredwards.fluidlogged_api.api.util.FluidState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.GlStateManager;

@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(modid = "realisticfinitefluids", value = Side.CLIENT)
public class FluidModelEventHandler {

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onModelRegistry(ModelRegistryEvent event) {
		FluidCompat.registerMissingFluidBlockForFluids();
	}
	
    //DEBUG
    @SubscribeEvent
    public static void onRenderWorldLast(RenderWorldLastEvent event) {
        //if (ModConfig.visualDebug) {
    	//if (true) return;
    	if (!FiniteFluidLogic.visualDebug) return;
    	
    	
    	////eSystem.out.println("fluids be infinite "+ModConfig.shouldFluidsBeInfinite);
    	
            Minecraft mc = Minecraft.getMinecraft();
            EntityPlayer player = mc.player;
            World world = mc.world;
            //double partialTicks = event.getPartialTicks();

            GlStateManager.pushMatrix();
            GlStateManager.disableLighting();
            GlStateManager.translate(-mc.getRenderManager().viewerPosX,
                                     -mc.getRenderManager().viewerPosY,
                                     -mc.getRenderManager().viewerPosZ);

            for (BlockPos pos : BlockPos.getAllInBox(player.getPosition().add(-8, -4, -8),
                                                     player.getPosition().add(8, 4, 8))) {
                IBlockState state = world.getBlockState(pos);
                if (RealisticFiniteFluidFunctions.getBlock(world, pos, state) instanceof IRealisticFiniteFluid) {
                    int level = ((IRealisticFiniteFluid)RealisticFiniteFluidFunctions.getBlock(world, pos, state)).getVolume(world, pos, state); //state.getValue(BlockFiniteFluid.LEVEL);
                    //String typeOfBlock = FluidState.of(state).toString().split("mine")[0];
                    		//state.getBlock().getLocalizedName().split(" ")[0];
                    
                    double x = pos.getX() + 0.5;
                    double y = pos.getY() + 1.2;
                    double z = pos.getZ() + 0.5;

                    GlStateManager.pushMatrix();
                    GlStateManager.translate(x, y-0.5, z);
                    GlStateManager.rotate(-mc.getRenderManager().playerViewY, 0, 1, 0);
                    GlStateManager.rotate(mc.getRenderManager().playerViewX, 1, 0, 0);
                    GlStateManager.scale(-0.025F, -0.025F, 0.025F);

                    mc.fontRenderer.drawString(String.valueOf(level), -mc.fontRenderer.getStringWidth(String.valueOf(level)) / 2, 0, 0xFFFFFF);
                    //mc.fontRenderer.drawString(String.valueOf(typeOfBlock), -mc.fontRenderer.getStringWidth(String.valueOf(typeOfBlock)) / 2, 8, 0xFFFFFF);
                    GlStateManager.popMatrix();
                }
            }

            GlStateManager.enableLighting();
            GlStateManager.popMatrix();
        }

    //}

}
