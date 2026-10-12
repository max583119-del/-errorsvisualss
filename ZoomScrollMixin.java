package com.example.client.mixin;

import com.example.client.config.ErrorsConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class ZoomScrollMixin {

    @Inject(method = "onMouseScroll", at = @At("HEAD"), cancellable = true)
    private void errorsVisuals$zoomScroll(long window, double hAmount, double vAmount, CallbackInfo ci) {
        if (!ErrorsConfig.zoomEnabled) return;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        // Только когда зажат C
        boolean zoomKey = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_C) == GLFW.GLFW_PRESS;
        if (!zoomKey) return;

        // vAmount > 0 = колесо вверх = приблизить
        // vAmount < 0 = колесо вниз = отдалить
        if (vAmount > 0) {
            ErrorsConfig.zoomLevel = Math.min(20.0f, ErrorsConfig.zoomLevel + 0.5f);
        } else if (vAmount < 0) {
            ErrorsConfig.zoomLevel = Math.max(1.5f, ErrorsConfig.zoomLevel - 0.5f);
        } else {
            return;
        }
        ErrorsConfig.save();
        ci.cancel();
    }
}