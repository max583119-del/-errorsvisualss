package com.example.client.mixin;

import com.example.client.config.ErrorsConfig;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(World.class)
public class WinterNightMixin {

    @Inject(method = "getTimeOfDay", at = @At("RETURN"), cancellable = true)
    private void errorsVisuals$night(CallbackInfoReturnable<Long> cir) {
        if (!ErrorsConfig.winterEnabled) return;
        cir.setReturnValue(18000L);
    }
}