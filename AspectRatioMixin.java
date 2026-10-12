package com.example.client.mixin;

import com.example.client.config.ErrorsConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public class AspectRatioMixin {

    @Inject(method = "getBasicProjectionMatrix", at = @At("HEAD"), cancellable = true)
    private void errorsVisuals$aspect(float fov, CallbackInfoReturnable<Matrix4f> cir) {
        if (!ErrorsConfig.aspectRatioEnabled) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.getWindow() == null) return;

        int fw = client.getWindow().getFramebufferWidth();
        int fh = client.getWindow().getFramebufferHeight();
        if (fh == 0) return;
        float actualAspect = (float) fw / fh;

        float wantedAspect;
        switch (ErrorsConfig.aspectRatioPreset) {
            case "4:3" -> wantedAspect = 4.0f / 3.0f;
            case "21:9" -> wantedAspect = 21.0f / 9.0f;
            case "1:1" -> wantedAspect = 1.0f;
            case "custom" -> wantedAspect = ErrorsConfig.aspectRatioCustom;
            default -> wantedAspect = actualAspect;
        }

        if (Math.abs(wantedAspect - actualAspect) < 0.001f) return;

        Matrix4f m = new Matrix4f();
        m.perspective((float) Math.toRadians(fov), wantedAspect, 0.05f, 1000.0f);
        cir.setReturnValue(m);
    }
}