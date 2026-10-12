package com.example.client.mixin;

import com.example.client.config.ErrorsConfig;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.fog.FogRenderer;
import net.minecraft.client.world.ClientWorld;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FogRenderer.class)
public class WinterFogMixin {

    @Inject(method = "getFogColor", at = @At("RETURN"), cancellable = true, require = 0)
    private static void errorsVisuals$fogColor(Camera camera, float tickProgress,
                                               ClientWorld world, int viewDistance,
                                               float skyDarkness,
                                               CallbackInfoReturnable<Vector4f> cir) {
        if (!ErrorsConfig.winterEnabled) return;

        // Цвет зависит от ползунка "Туман":
        // 0.0 — обычный (не трогаем)
        // 1.0 — очень светлый белый туман
        float t = ErrorsConfig.winterFog;
        if (t <= 0.01f) return;

        float r = 0.60f + 0.35f * t;
        float g = 0.70f + 0.28f * t;
        float b = 0.85f + 0.15f * t;

        cir.setReturnValue(new Vector4f(r, g, b, 1.0f));
    }
}