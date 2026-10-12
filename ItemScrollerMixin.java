package com.example.client.mixin;

import com.example.client.config.ErrorsConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HandledScreen.class)
public abstract class ItemScrollerMixin {

    @Shadow protected Slot focusedSlot;

    // Последний слот, с которым уже сработал перенос
    private static int lastSlotId = -1;

    private static boolean key(long h, int k) {
        return GLFW.glfwGetKey(h, k) == GLFW.GLFW_PRESS;
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void errorsVisuals$drag(CallbackInfo ci) {
        if (!ErrorsConfig.itemScroller) {
            lastSlotId = -1;
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.interactionManager == null) return;

        long h = client.getWindow().getHandle();
        boolean lmb = GLFW.glfwGetMouseButton(h, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        boolean shift = key(h, GLFW.GLFW_KEY_LEFT_SHIFT) || key(h, GLFW.GLFW_KEY_RIGHT_SHIFT);

        // Если комбинация НЕ зажата — сбрасываем
        if (!lmb || !shift) {
            lastSlotId = -1;
            return;
        }

        Slot slot = focusedSlot;
        if (slot == null || slot.id < 0) {
            lastSlotId = -1;
            return;
        }

        // Уже перенесли из этого слота — не повторяем
        if (slot.id == lastSlotId) return;

        if (!slot.hasStack()) {
            lastSlotId = slot.id;
            return;
        }

        // Мгновенный перенос — один пакет на новый слот
        HandledScreen<?> self = (HandledScreen<?>) (Object) this;
        client.interactionManager.clickSlot(
                self.getScreenHandler().syncId, slot.id, 0,
                SlotActionType.QUICK_MOVE, client.player
        );

        lastSlotId = slot.id;
    }

    // === Колёсико — быстрый перенос стака (оставляем) ===
    @Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true)
    private void errorsVisuals$scroll(double mouseX, double mouseY,
                                       double hAmount, double vAmount,
                                       CallbackInfoReturnable<Boolean> cir) {
        if (!ErrorsConfig.itemScroller) return;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.interactionManager == null) return;

        HandledScreen<?> self = (HandledScreen<?>) (Object) this;
        Slot slot = focusedSlot;
        if (slot == null || slot.id < 0 || !slot.hasStack()) return;

        client.interactionManager.clickSlot(
                self.getScreenHandler().syncId, slot.id, 0,
                SlotActionType.QUICK_MOVE, client.player
        );
        cir.setReturnValue(true);
    }
}