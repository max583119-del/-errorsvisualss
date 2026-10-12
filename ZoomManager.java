package com.example.client.zoom;

import com.example.client.config.ErrorsConfig;

public class ZoomManager {
    public static boolean isZooming = false;
    private static float currentMultiplier = 1.0f;
    private static long lastFrameTime = System.nanoTime();

    // Вызывается каждый кадр из ZoomMixin — сглаживание не зависит от FPS
    public static float getMultiplier() {
        long now = System.nanoTime();
        float dt = (now - lastFrameTime) / 1_000_000_000.0f;
        lastFrameTime = now;
        if (dt > 0.1f) dt = 0.1f;
        if (dt < 0.0f) dt = 0.0f;

        float target;
        if (!ErrorsConfig.zoomEnabled) {
            target = 1.0f;
        } else if (isZooming) {
            target = ErrorsConfig.zoomLevel;
        } else {
            target = 1.0f;
        }

        // Экспоненциальное сглаживание — плавно на любом FPS
        float rate = 7.0f;
        float factor = 1.0f - (float) Math.exp(-rate * dt);
        currentMultiplier += (target - currentMultiplier) * factor;

        if (Math.abs(currentMultiplier - target) < 0.001f) {
            currentMultiplier = target;
        }

        return currentMultiplier;
    }
}