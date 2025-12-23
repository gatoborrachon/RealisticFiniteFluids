package com.gatoborrachon.realisticfinitefluids.mixin.earlymixins;

import java.util.Map;
import java.util.Set;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.ModelLoaderRegistry.LoaderException;

@Mixin(ModelLoaderRegistry.class)
public abstract class MixinModelLoaderRegistry {
	
	@Shadow @Final private static Set<ICustomModelLoader> loaders;
    @Shadow @Final private static Map<ResourceLocation, IModel> cache;

    //@Shadow protected abstract static ResourceLocation getActualLocation(ResourceLocation location);

	
    @Inject(method = "getModel", at = @At("HEAD"), remap = false)
    private static void onGetModel(ResourceLocation location, CallbackInfoReturnable<IModel> cir) throws LoaderException {
        //if (location.getNamespace() == "realisticfinitefluids") {
    	
			System.out.println(" ");
        	System.out.println("[RFF - getModel] Requested ResourceLocation: " + location);
            System.out.println("[RFF - getModel] Namespace: " + location.getNamespace());
            System.out.println("[RFF - getModel] Path: " + location.getPath());
            
            IModel cached = cache.get(location);
            System.out.println("[RFF - getModel] Cache: " + cached);

            

            ResourceLocation actual = ModelLoaderRegistry.getActualLocation(location);
            ICustomModelLoader accepted = null;
            
            for(ICustomModelLoader loader : loaders)
            {
                try
                {
                    if(loader.accepts(actual))
                    {
                        if(accepted != null)
                        {
                            throw new LoaderException(String.format("2 loaders (%s and %s) want to load the same model %s", accepted, loader, location));
                        }
                        System.out.println("[RFF - getModel] loader: " + loader.toString());
                        accepted = loader;
                    }
                }
                catch(Exception e)
                {
                    throw new LoaderException(String.format("Exception checking if model %s can be loaded with loader %s, skipping", location, loader), e);
                }
            }
        	
        //}

        
        /*if (location.getNamespace().equals("forge")
       		&& location.getPath().equals("block/fluid")) {
        	System.out.println("IGNORANDO "+location);
        	cir.cancel();
        	return;
        }*/
        
        
    }
    
	

}
