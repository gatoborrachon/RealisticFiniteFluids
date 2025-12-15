package com.gatoborrachon.realisticfinitefluids.mixin.earlymixins;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.gatoborrachon.realisticfinitefluids.References;

import net.minecraft.block.properties.PropertyInteger;
import net.minecraftforge.fluids.BlockFluidBase;

@Mixin(BlockFluidBase.class)
public abstract class MixinBlockFluidBase {

    @Shadow @Final public static PropertyInteger LEVEL; // shadow del campo original

    @Inject(method = "<clinit>", at = @At("RETURN"))
    private static void injectSetLevel(CallbackInfo ci) throws Exception {
        // Quitamos el final y reasignamos
        Field levelField = BlockFluidBase.class.getField("LEVEL");
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(levelField, levelField.getModifiers() & ~Modifier.FINAL);

        levelField.set(null, References.LEVEL); // reasignamos a tu LEVEL
    }
}
