package com.example.client.render;

import com.example.client.config.ErrorsConfig;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayDeque;
import java.util.Deque;

public class TrailRenderer {
    private static final Deque<Vec3d> points = new ArrayDeque<>();
    private static final int MAX = 50;
    private static Vec3d last = null;

    public static void register() {
        HudRenderCallback.EVENT.register((ctx, tickCounter) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null || client.world == null) return;

            if (!ErrorsConfig.trailEnabled) { points.clear(); last = null; return; }

            Vec3d pos = new Vec3d(client.player.getX(), client.player.getY() + 0.2, client.player.getZ());
            if (last == null || pos.distanceTo(last) > 0.25) {
                points.addLast(pos);
                while (points.size() > MAX) points.removeFirst();
                last = pos;
            }
            if (points.size() < 2) return;

            int screenW = client.getWindow().getScaledWidth();
            int screenH = client.getWindow().getScaledHeight();
            double fov = client.options.getFov().getValue();
            double scale = screenH / (2.0 * Math.tan(Math.toRadians(fov / 2.0)));

            Vec3d cam = CameraHelper.getCameraPos(client);

            float yaw = client.gameRenderer.getCamera().getYaw();
            float pitch = client.gameRenderer.getCamera().getPitch();
            double yawRad = Math.toRadians(yaw);
            double pitchRad = Math.toRadians(pitch);
            double cosYaw = Math.cos(yawRad), sinYaw = Math.sin(yawRad);
            double cosPitch = Math.cos(pitchRad), sinPitch = Math.sin(pitchRad);

            int rgb = colorOf(ErrorsConfig.trailColor);

            int prevX = -1, prevY = -1;
            int i = 0, total = points.size();
            for (Vec3d p : points) {
                double rx = p.x - cam.x;
                double ry = p.y - cam.y;
                double rz = p.z - cam.z;

                double xRot = -rx * cosYaw - rz * sinYaw;
                double zRot = -rx * sinYaw + rz * cosYaw;
                double yRot = ry * cosPitch + zRot * sinPitch;
                double zFinal = -ry * sinPitch + zRot * cosPitch;
                i++;
                if (zFinal < 0.5) { prevX = -1; continue; }

                int sx = (int) (xRot * scale / zFinal + screenW / 2.0);
                int sy = (int) (-yRot * scale / zFinal + screenH / 2.0);

                int alpha = (int) (255.0 * i / total);
                int color = (alpha << 24) | rgb;
                int thick = Math.max(3, (int) (10.0 / Math.max(1.0, zFinal)));

                if (prevX != -1) {
                    drawThick(ctx, prevX, prevY, sx, sy, color, thick);
                }
                prevX = sx; prevY = sy;
            }
        });
    }

    private static void drawThick(DrawContext ctx, int x1, int y1, int x2, int y2, int color, int t) {
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int steps = Math.max(dx, dy);
        int half = t / 2;
        if (steps == 0) { ctx.fill(x1 - half, y1 - half, x1 + half, y1 + half, color); return; }
        for (int i = 0; i <= steps; i++) {
            float k = (float) i / steps;
            int x = (int) (x1 + (x2 - x1) * k);
            int y = (int) (y1 + (y2 - y1) * k);
            ctx.fill(x - half, y - half, x + half, y + half, color);
        }
    }

    private static int colorOf(String name) {
        return switch (name) {
            case "red" -> 0xFF3355;
            case "green" -> 0x55FF55;
            case "gold" -> 0xFFD700;
            case "white" -> 0xFFFFFF;
            default -> 0x55FFFF;
        };
    }
}