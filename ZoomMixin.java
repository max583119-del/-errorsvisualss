package com.example.client.mixin;

import com.example.client.zoom.ZoomManager;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public class ZoomMixin {

    @Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
    private void errorsVisuals$zoom(Camera camera, float tickDelta, boolean changingFov,
                                     CallbackInfoReturnable<Float> cir) {
        // Вызывается каждый кадр — сглаживание плавное
        float mult = ZoomManager.getMultiplier();
        if (mult > 1.001f) {
            cir.setReturnValue(cir.getReturnValue() / mult);
        }
    }
}