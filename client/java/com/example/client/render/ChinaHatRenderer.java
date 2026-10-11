package com.example.client.render;

import com.example.client.config.ErrorsConfig;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.Vec3d;

public class ChinaHatRenderer {

    public static void register() {
        HudRenderCallback.EVENT.register((ctx, tickCounter) -> {
            if (!ErrorsConfig.chinaHat) return;
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null || client.world == null) return;
            float tickDelta = CameraHelper.getTickDelta(client);
            drawHat(client, ctx, tickDelta);
        });
    }

    private static void drawHat(MinecraftClient client, DrawContext ctx, float tickDelta) {
        int screenW = client.getWindow().getScaledWidth();
        int screenH = client.getWindow().getScaledHeight();
        double fov = client.options.getFov().getValue();
        double scale = screenH / (2.0 * Math.tan(Math.toRadians(fov / 2.0)));

        Vec3d cam = CameraHelper.getCameraPos(client);
        Vec3d lerped = client.player.getLerpedPos(tickDelta);

        double tx = lerped.x;
        double ty = lerped.y + client.player.getHeight() + 0.05;
        double tz = lerped.z;

        double rx = tx - cam.x;
        double ry = ty - cam.y;
        double rz = tz - cam.z;

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

        int baseR = (int) (scale / zFinal * 0.85 * ErrorsConfig.chinaHatSize);
        if (baseR < 6) return;

        int alpha = (int) (255 * Math.max(0.1f, Math.min(1f, ErrorsConfig.chinaHatAlpha)));
        int color = colorOf(ErrorsConfig.chinaHatColor);
        int fill = (alpha << 24) | color;
        int edge = (Math.min(255, alpha + 100) << 24) | color;

        int ryBase = (int) (baseR * 0.45);
        int capOffset = Math.max(3, baseR / 12);
        int ryTop = (int) (ryBase * 0.95);
        int topR = (int) (baseR * 0.98);

        fillEllipse(ctx, cx, cy, baseR, ryBase, fill);
        fillEllipse(ctx, cx, cy - capOffset, topR, ryTop, fill);

        for (int i = 0; i < baseR * 2; i++) {
            double a = i * Math.PI / baseR;
            int x = cx + (int) (Math.cos(a) * baseR);
            int yBot = cy + (int) (Math.sin(a) * ryBase);
            int yTop = cy - capOffset + (int) (Math.sin(a) * ryTop);
            ctx.fill(x, Math.min(yBot, yTop), x + 2, Math.max(yBot, yTop) + 1, fill);
        }

        int domeR = Math.max(4, baseR / 4);
        int domeH = Math.max(3, baseR / 8);
        for (int i = 0; i <= domeH; i++) {
            int r = domeR - (domeR * i / domeH);
            int rr = (int) (r * 0.5);
            fillEllipse(ctx, cx, cy - capOffset - i, r, rr, fill);
        }

        outlineEllipse(ctx, cx, cy - capOffset, topR, ryTop, edge, 2);
    }

    private static void fillEllipse(DrawContext ctx, int cx, int cy, int rx, int ry, int color) {
        if (rx <= 0 || ry <= 0) return;
        for (int dy = -ry; dy <= ry; dy++) {
            int dx = (int) (rx * Math.sqrt(Math.max(0, 1.0 - (double) (dy * dy) / (ry * ry))));
            ctx.fill(cx - dx, cy + dy, cx + dx, cy + dy + 1, color);
        }
    }

    private static void outlineEllipse(DrawContext ctx, int cx, int cy, int rx, int ry, int color, int t) {
        int steps = 64;
        for (int i = 0; i < steps; i++) {
            double a1 = i * 2 * Math.PI / steps;
            double a2 = (i + 1) * 2 * Math.PI / steps;
            int x1 = cx + (int) (Math.cos(a1) * rx);
            int y1 = cy + (int) (Math.sin(a1) * ry);
            int x2 = cx + (int) (Math.cos(a2) * rx);
            int y2 = cy + (int) (Math.sin(a2) * ry);
            fillThick(ctx, x1, y1, x2, y2, color, t);
        }
    }

    private static void fillThick(DrawContext ctx, int x1, int y1, int x2, int y2, int color, int t) {
        int dx = Math.abs(x2 - x1), dy = Math.abs(y2 - y1);
        int steps = Math.max(dx, dy);
        int h = Math.max(1, t / 2);
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
            case "cyan" -> 0x00AAFF;
            case "white" -> 0xFFFFFF;
            case "blue" -> 0x0066FF;
            default -> 0x0099FF;
        };
    }
}