package com.example.client.mixin;

import com.example.client.config.ErrorsConfig;
import net.minecraft.client.render.LightmapTextureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LightmapTextureManager.class)
public class FullBrightMixin {

    @ModifyVariable(method = "update", at = @At("STORE"), ordinal = 0)
    private float errorsVisuals$brightness(float original) {
        if (ErrorsConfig.fullBright) return 15.0f;
        return original;
    }
}
