package com.example.client.mixin;

import com.example.client.config.ErrorsConfig;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(GameRenderer.class)
public class AspectRatioMixin {

    @ModifyVariable(method = "getBasicProjectionMatrix", at = @At("HEAD"), argsOnly = true)
    private float errorsVisuals$fov(float fov) {
        if (!ErrorsConfig.aspectRatioEnabled) return fov;

        return switch (ErrorsConfig.aspectRatioPreset) {
            case "4:3" -> fov * 0.75f;
            case "21:9" -> fov * 1.25f;
            case "1:1" -> fov * 0.6f;
            default -> fov;
        };
    }
}