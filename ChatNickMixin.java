package com.example.client.mixin;

import com.example.client.config.ErrorsConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ChatHud.class)
public class ChatNickMixin {

    @ModifyVariable(
            method = "addMessage(Lnet/minecraft/text/Text;)V",
            at = @At("HEAD"),
            argsOnly = true
    )
    private Text errorsVisuals$replaceNick(Text message) {
        if (ErrorsConfig.customNickname.isEmpty()) return message;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.getSession() == null) return message;

        String realName = client.getSession().getUsername();
        String raw = message.getString();

        String oldTag = "<" + realName + ">";
        if (raw.contains(oldTag)) {
            String newRaw = raw.replace(oldTag, "<" + ErrorsConfig.customNickname + ">");
            return Text.literal(newRaw);
        }
        return message;
    }
}