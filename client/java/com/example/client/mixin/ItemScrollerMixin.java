package com.example.client.mixin;

import com.example.client.config.ErrorsConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class ItemScrollerMixin {

    @Inject(method = "onMouseScroll", at = @At("HEAD"), cancellable = true)
    private void errorsVisuals$onScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        if (!ErrorsConfig.itemScroller) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (!(client.currentScreen instanceof HandledScreen<?> screen)) return;
        if (client.player == null || client.interactionManager == null) return;

        double mx = client.mouse.getX() * client.getWindow().getScaledWidth() / (double) client.getWindow().getWidth();
        double my = client.mouse.getY() * client.getWindow().getScaledHeight() / (double) client.getWindow().getHeight();

        Slot slot = findSlot(screen, mx, my);
        if (slot == null || slot.id < 0 || !slot.hasStack()) return;

        client.interactionManager.clickSlot(
                screen.getScreenHandler().syncId,
                slot.id, 0,
                SlotActionType.QUICK_MOVE,
                client.player
        );
        ci.cancel();
    }

    private Slot findSlot(HandledScreen<?> screen, double mx, double my) {
        for (Slot slot : screen.getScreenHandler().slots) {
            if (mx >= slot.x && mx < slot.x + 16 && my >= slot.y && my < slot.y + 16) {
                return slot;
            }
        }
        return null;
    }
}