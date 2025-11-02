package com.gatoborrachon.realisticfinitefluids.events;

import com.gatoborrachon.realisticfinitefluids.blocks.BlockFiniteFluid;
import com.gatoborrachon.realisticfinitefluids.init.ModBlocks;
import com.gatoborrachon.realisticfinitefluids.init.ModConfig;
import com.gatoborrachon.realisticfinitefluids.logic.FiniteFluidLogic;
import com.gatoborrachon.realisticfinitefluids.render.BakedModelFiniteFluid;
import com.gatoborrachon.realisticfinitefluids.render.RenderNewFluids;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeColorHelper;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.GlStateManager;

@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(modid = "realisticfinitefluids", value = Side.CLIENT)
public class FluidModelEventHandler {
	
	@SubscribeEvent
	public static void onModelRegistry(ModelRegistryEvent event) {
	    ModelLoader.setCustomStateMapper(ModBlocks.FINITE_WATER_FLOWING, new StateMapperBase() {
	        @Override
	        protected ModelResourceLocation getModelResourceLocation(IBlockState state) {
	            return new ModelResourceLocation("realisticfinitefluids:finite_water_flowing", "normal");
	        }
	    });
	    
	    ModelLoader.setCustomStateMapper(ModBlocks.FINITE_WATER_STILL, new StateMapperBase() {
	        @Override
	        protected ModelResourceLocation getModelResourceLocation(IBlockState state) {
	            return new ModelResourceLocation("realisticfinitefluids:finite_water_still", "normal");
	        }
	    });
	    
	    ModelLoader.setCustomStateMapper(ModBlocks.INFINITE_WATER_SOURCE, new StateMapperBase() {
	        @Override
	        protected ModelResourceLocation getModelResourceLocation(IBlockState state) {
	            return new ModelResourceLocation("realisticfinitefluids:infinite_water_source", "normal");
	        }
	    });
	    
	    
	    
	    //LAVA
	    ModelLoader.setCustomStateMapper(ModBlocks.FINITE_LAVA_FLOWING, new StateMapperBase() {
	        @Override
	        protected ModelResourceLocation getModelResourceLocation(IBlockState state) {
	            return new ModelResourceLocation("realisticfinitefluids:finite_lava_flowing", "normal");
	        }
	    });
	    
	    ModelLoader.setCustomStateMapper(ModBlocks.FINITE_LAVA_STILL, new StateMapperBase() {
	        @Override
	        protected ModelResourceLocation getModelResourceLocation(IBlockState state) {
	            return new ModelResourceLocation("realisticfinitefluids:finite_lava_still", "normal");
	        }
	    });
	    
	    ModelLoader.setCustomStateMapper(ModBlocks.INFINITE_LAVA_SOURCE, new StateMapperBase() {
	        @Override
	        protected ModelResourceLocation getModelResourceLocation(IBlockState state) {
	            return new ModelResourceLocation("realisticfinitefluids:infinite_lava_source", "normal");
	        }
	    });

	    //System.out.println("[DEBUG] StateMapper personalizado registrado");
	}
	
	
	


    @SubscribeEvent
    public static void onModelBake(ModelBakeEvent event) {
        RenderNewFluids renderer = new RenderNewFluids(); // Usa tu lista de líquidos aquí
        
        //Esto esta aca para que se enlacen los bloques con cada index tanto despues de que los bloques son correctamente registrados como antes de que se ancla el fluid model baker con cada bloque.
    	FiniteFluidLogic.initLiquids();
        
        
        //WATER
        //Flowing
        ModelResourceLocation modelLocFlowing = new ModelResourceLocation("realisticfinitefluids:finite_water_flowing", "normal");
        TextureAtlasSprite spriteFlowing = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite("minecraft:blocks/water_flow");
        BakedModelFiniteFluid modelFlowing = new BakedModelFiniteFluid(renderer, spriteFlowing, FiniteFluidLogic.GeneralPurposeLogic.getFluidIndex(ModBlocks.FINITE_WATER_FLOWING));
        event.getModelRegistry().putObject(modelLocFlowing, modelFlowing);
      

        //Still
        ModelResourceLocation modelLocStill = new ModelResourceLocation("realisticfinitefluids:finite_water_still", "normal");
        TextureAtlasSprite spriteStill = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite("minecraft:blocks/water_still");
        BakedModelFiniteFluid modelStill = new BakedModelFiniteFluid(renderer, spriteStill, FiniteFluidLogic.GeneralPurposeLogic.getFluidIndex(ModBlocks.FINITE_WATER_STILL));
        event.getModelRegistry().putObject(modelLocStill, modelStill);   
        
        //Ocean
        ModelResourceLocation modelLocOcean = new ModelResourceLocation("realisticfinitefluids:infinite_water_source", "normal");
        BakedModelFiniteFluid modelOcean = new BakedModelFiniteFluid(renderer, spriteStill, FiniteFluidLogic.GeneralPurposeLogic.getFluidIndex(ModBlocks.INFINITE_WATER_SOURCE));
        event.getModelRegistry().putObject(modelLocOcean, modelOcean);   
    
        
        
        
        
        //LAVA
        //Flowing
        ModelResourceLocation modelLocLavaFlowing = new ModelResourceLocation("realisticfinitefluids:finite_lava_flowing", "normal");
        TextureAtlasSprite spriteLavaFlowing = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite("minecraft:blocks/lava_flow");
        BakedModelFiniteFluid modelLavaFlowing = new BakedModelFiniteFluid(renderer, spriteLavaFlowing, FiniteFluidLogic.GeneralPurposeLogic.getFluidIndex(ModBlocks.FINITE_LAVA_FLOWING));
        event.getModelRegistry().putObject(modelLocLavaFlowing, modelLavaFlowing);
        
        //Still
        ModelResourceLocation modelLocLavaStill = new ModelResourceLocation("realisticfinitefluids:finite_lava_still", "normal");
        TextureAtlasSprite spriteLavaStill = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite("minecraft:blocks/lava_still");
        BakedModelFiniteFluid modelLavaStill = new BakedModelFiniteFluid(renderer, spriteLavaStill, FiniteFluidLogic.GeneralPurposeLogic.getFluidIndex(ModBlocks.FINITE_LAVA_FLOWING));
        event.getModelRegistry().putObject(modelLocLavaStill, modelLavaStill);

        //Ocean
        ModelResourceLocation modelLocLavaOcean = new ModelResourceLocation("realisticfinitefluids:infinite_lava_source", "normal");
        BakedModelFiniteFluid modelLavaOcean = new BakedModelFiniteFluid(renderer, spriteLavaStill, FiniteFluidLogic.GeneralPurposeLogic.getFluidIndex(ModBlocks.INFINITE_LAVA_SOURCE));
        event.getModelRegistry().putObject(modelLocLavaOcean, modelLavaOcean);   
        
    }
    


    @SubscribeEvent
    public static void onBlockColors(net.minecraftforge.client.event.ColorHandlerEvent.Block event) {
        BlockColors blockColors = event.getBlockColors();

        blockColors.registerBlockColorHandler(
            (state, world, pos, tintIndex) -> {
                if (world != null && pos != null) {
                    return BiomeColorHelper.getWaterColorAtPos(world, pos); // <- import net.minecraft.world.biome.BiomeColorHelper
                }
                return 0x3F76E4; // fallback azul vanilla
            },
            ModBlocks.INFINITE_WATER_SOURCE,
            ModBlocks.FINITE_WATER_FLOWING,
            ModBlocks.FINITE_WATER_STILL
        );
    }

    @SubscribeEvent
    public static void onItemColors(net.minecraftforge.client.event.ColorHandlerEvent.Item event) {
        // Opcional: si también quieres tintado para el item del bloque (cubos, etc.)
        final BlockColors bc = Minecraft.getMinecraft().getBlockColors();
        event.getItemColors().registerItemColorHandler(
            (stack, tintIndex) -> {
                Block b = Block.getBlockFromItem(stack.getItem());
                return b != null
                    ? bc.colorMultiplier(b.getDefaultState(), null, null, tintIndex)
                    : 0xFFFFFFFF;
            },
            Item.getItemFromBlock(ModBlocks.INFINITE_WATER_SOURCE),
            Item.getItemFromBlock(ModBlocks.FINITE_WATER_FLOWING),
            Item.getItemFromBlock(ModBlocks.FINITE_WATER_STILL)
        );
    }

    
    
    //DEBUG
    @SubscribeEvent
    public static void onRenderWorldLast(RenderWorldLastEvent event) {
        if (ModConfig.debug) {
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
                if (state.getBlock() instanceof BlockFiniteFluid) {
                    int level = BlockFiniteFluid.getVolume(state); //state.getValue(BlockFiniteFluid.LEVEL);

                    double x = pos.getX() + 0.5;
                    double y = pos.getY() + 1.2;
                    double z = pos.getZ() + 0.5;

                    GlStateManager.pushMatrix();
                    GlStateManager.translate(x, y, z);
                    GlStateManager.rotate(-mc.getRenderManager().playerViewY, 0, 1, 0);
                    GlStateManager.rotate(mc.getRenderManager().playerViewX, 1, 0, 0);
                    GlStateManager.scale(-0.025F, -0.025F, 0.025F);

                    mc.fontRenderer.drawString(String.valueOf(level), -mc.fontRenderer.getStringWidth(String.valueOf(level)) / 2, 0, 0xFFFFFF);
                    GlStateManager.popMatrix();
                }
            }

            GlStateManager.enableLighting();
            GlStateManager.popMatrix();
        }

    }

}
