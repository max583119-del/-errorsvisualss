package com.example.client.mixin;

import com.example.client.config.ErrorsConfig;
import net.minecraft.client.gui.hud.InGameOverlayRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameOverlayRenderer.class)
public class ExampleClientMixin {

    @Inject(method = "renderFireOverlay", at = @At("HEAD"), cancellable = true)
    private static void errorsVisuals$noFire(MatrixStack matrices, VertexConsumerProvider vc, Sprite sprite, CallbackInfo ci) {
        if (ErrorsConfig.noFire) ci.cancel();
    }
}