package com.example.client.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3d;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class CameraHelper {
    private static Method getPosMethod = null;
    private static Field posField = null;
    private static boolean tried = false;

    public static Vec3d getCameraPos(MinecraftClient client) {
        if (client.player == null) return Vec3d.ZERO;

        Object camera = client.gameRenderer.getCamera();

        if (!tried) {
            tried = true;
            for (String name : new String[]{"getPos", "getPosition", "getCameraPos"}) {
                try {
                    getPosMethod = camera.getClass().getMethod(name);
                    break;
                } catch (Exception ignored) {}
            }
            if (getPosMethod == null) {
                for (String name : new String[]{"pos", "position"}) {
                    try {
                        posField = camera.getClass().getDeclaredField(name);
                        posField.setAccessible(true);
                        break;
                    } catch (Exception ignored) {}
                }
            }
        }

        try {
            if (getPosMethod != null) {
                Object result = getPosMethod.invoke(camera);
                if (result instanceof Vec3d v) return v;
            }
            if (posField != null) {
                Object result = posField.get(camera);
                if (result instanceof Vec3d v) return v;
            }
        } catch (Exception ignored) {}

        // Фоллбэк — глаза игрока
        return new Vec3d(
                client.player.getX(),
                client.player.getY() + client.player.getEyeHeight(client.player.getPose()),
                client.player.getZ()
        );
    }

    public static float getTickDelta(MinecraftClient client) {
        try {
            Method m = client.getClass().getMethod("getRenderTickCounter");
            Object counter = m.invoke(client);
            Method getDelta = counter.getClass().getMethod("getTickDelta", boolean.class);
            return (float) getDelta.invoke(counter, false);
        } catch (Exception e) {
            return 1.0f;
        }
    }
}