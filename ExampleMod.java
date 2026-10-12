package com.example.client;

import com.example.client.gui.ErrorsVisualsScreen;
import com.example.client.particle.ParticleSpawner;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExampleModClient implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("errorsVisuals");
    public static KeyBinding openMenuKey;

    @Override
    public void onInitializeClient() {
        LOGGER.info("errorsVisuals: загружен!");

        // Регистрируем партиклы при ударе
        ParticleSpawner.register();

        openMenuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.errorsvisuals.menu",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_RIGHT_SHIFT,
            KeyBinding.Category.MISC
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openMenuKey.wasPressed()) {
                if (client.currentScreen == null) {
                    client.setScreen(new ErrorsVisualsScreen());
                }
            }
        });
    }
}