package com.example.client.mixin;

import com.example.client.config.ErrorsConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {

    @Inject(method = "renderOverlay", at = @At("HEAD"), cancellable = true)
    private void errorsVisuals$noPumpkin(DrawContext context, Identifier texture, float opacity, CallbackInfo ci) {
        if (texture == null || texture.getPath() == null) return;
        if (!texture.getPath().equals("textures/misc/pumpkinblur.png")) return;
        if (ErrorsConfig.noPumpkin) {
            ci.cancel();
        }
    }
}