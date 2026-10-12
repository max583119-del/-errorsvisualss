package com.example.client.render;

import com.example.client.config.ErrorsConfig;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.Vec3d;

public class JumpCircleRenderer {

    public static void register() {
        HudRenderCallback.EVENT.register((ctx, tickCounter) -> {
            if (!ErrorsConfig.jumpCircle) return;
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null || client.world == null) return;
            if (client.player.isOnGround()) return;

            int screenW = client.getWindow().getScaledWidth();
            int screenH = client.getWindow().getScaledHeight();
            double fov = client.options.getFov().getValue();
            double scale = screenH / (2.0 * Math.tan(Math.toRadians(fov / 2.0)));

            Vec3d cam = CameraHelper.getCameraPos(client);

            // Точка под игроком, на 1 блок ниже
            double wx = client.player.getX();
            double wy = client.player.getY() - 1.0;
            double wz = client.player.getZ();

            double rx = wx - cam.x;
            double ry = wy - cam.y;
            double rz = wz - cam.z;

            float yaw = client.gameRenderer.getCamera().getYaw();
            float pitch = client.gameRenderer.getCamera().getPitch();
            double yawRad = Math.toRadians(yaw);
            double pitchRad = Math.toRadians(pitch);
            double cosYaw = Math.cos(yawRad), sinYaw = Math.sin(yawRad);
            double cosPitch = Math.cos(pitchRad), sinPitch = Math.sin(pitchRad);

            double xRot = -rx * cosYaw - rz * sinYaw;
            double zRot = -rx * sinYaw + rz * cosYaw;
            double yRot = ry * cosPitch + zRot * sinPitch;
            double zFinal = -ry * sinPitch + zRot * cosPitch;
            if (zFinal < 0.5) return;

            int cx = (int) (xRot * scale / zFinal + screenW / 2.0);
            int cy = (int) (-yRot * scale / zFinal + screenH / 2.0);

            int radius = (int) (scale / zFinal * 0.8);
            if (radius < 8 || radius > 500) return;

            int color = colorOf(ErrorsConfig.jumpCircleColor) | (0xFF << 24);
            int thick = Math.max(3, radius / 25);

            int steps = 72;
            for (int i = 0; i < steps; i++) {
                double a1 = i * 2 * Math.PI / steps;
                double a2 = (i + 1) * 2 * Math.PI / steps;
                int x1 = cx + (int) (Math.cos(a1) * radius);
                int y1 = cy + (int) (Math.sin(a1) * radius * 0.4);
                int x2 = cx + (int) (Math.cos(a2) * radius);
                int y2 = cy + (int) (Math.sin(a2) * radius * 0.4);
                fillThick(ctx, x1, y1, x2, y2, color, thick);
            }
        });
    }

    private static void fillThick(DrawContext ctx, int x1, int y1, int x2, int y2, int color, int t) {
        int dx = Math.abs(x2 - x1), dy = Math.abs(y2 - y1);
        int steps = Math.max(dx, dy);
        int h = t / 2;
        if (steps == 0) { ctx.fill(x1 - h, y1 - h, x1 + h, y1 + h, color); return; }
        for (int i = 0; i <= steps; i++) {
            float k = (float) i / steps;
            int x = (int) (x1 + (x2 - x1) * k);
            int y = (int) (y1 + (y2 - y1) * k);
            ctx.fill(x - h, y - h, x + h, y + h, color);
        }
    }

    private static int colorOf(String name) {
        return switch (name) {
            case "red" -> 0xFF3355;
            case "green" -> 0x55FF55;
            case "gold" -> 0xFFD700;
            case "cyan" -> 0x55FFFF;
            default -> 0xFFFFFF;
        };
    }
}