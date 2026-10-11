package com.example.client.render;

import com.example.client.config.ErrorsConfig;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

public class TargetEspRenderer {

    public static void register() {
        HudRenderCallback.EVENT.register((drawContext, tickCounter) -> {
            if (!ErrorsConfig.targetEsp) return;

            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null || client.world == null) return;

            // Ищем ОДНУ ближайшую цель
            LivingEntity nearest = null;
            double nearestDist = Double.MAX_VALUE;

            for (Entity entity : client.world.getEntities()) {
                if (!(entity instanceof LivingEntity living)) continue;
                if (living == client.player) continue;
                if (living.isInvisible()) continue;
                if (living.isInvisibleTo(client.player)) continue;

                double dist = client.player.distanceTo(living);
                if (dist > 5.0) continue;
                if (!canSee(client, living)) continue;

                if (dist < nearestDist) {
                    nearestDist = dist;
                    nearest = living;
                }
            }

            if (nearest == null) return;

            // Координаты камеры
            double camX = client.player.getX();
            double camY = client.player.getY() + client.player.getEyeHeight(client.player.getPose());
            double camZ = client.player.getZ();

            float yaw = client.player.getYaw();
            float pitch = client.player.getPitch();

            double yawRad = Math.toRadians(yaw);
            double pitchRad = Math.toRadians(pitch);
            double cosYaw = Math.cos(yawRad), sinYaw = Math.sin(yawRad);
            double cosPitch = Math.cos(pitchRad), sinPitch = Math.sin(pitchRad);

            int screenW = client.getWindow().getScaledWidth();
            int screenH = client.getWindow().getScaledHeight();

            double fov = client.options.getFov().getValue();
            double scale = screenH / (2.0 * Math.tan(Math.toRadians(fov / 2.0)));

            double tx = nearest.getX();
            double ty = nearest.getY() + nearest.getHeight() / 2.0;
            double tz = nearest.getZ();

            double rx = tx - camX;
            double ry = ty - camY;
            double rz = tz - camZ;

            double xRot = -rx * cosYaw - rz * sinYaw;
            double zRot = -rx * sinYaw + rz * cosYaw;
            double yRot = ry * cosPitch + zRot * sinPitch;
            double zFinal = -ry * sinPitch + zRot * cosPitch;
            if (zFinal < 0.5) return;

            int screenX = (int) (xRot * scale / zFinal + screenW / 2.0);
            int screenY = (int) (-yRot * scale / zFinal + screenH / 2.0);

            // Анимации
            float rotation = (System.currentTimeMillis() % 4000) / 4000f * 360f;
            float pulse = (float) (0.85 + 0.15 * Math.sin(System.currentTimeMillis() / 300.0));
            int colorRGB = getColorRGB();

            int size = (int) Math.max(18, 90.0 / Math.max(1.0, nearestDist));
            size = (int) (size * pulse);

            if (ErrorsConfig.targetEspShape.equals("circle")) {
                drawGlowCircle(drawContext, screenX, screenY, size, rotation, colorRGB);
            } else {
                drawGlowSquare(drawContext, screenX, screenY, size, rotation, colorRGB);
            }
        });
    }

    private static int getColorRGB() {
        return switch (ErrorsConfig.targetEspColor) {
            case "red" -> 0xFF3355;
            case "green" -> 0x55FF55;
            case "cyan" -> 0x55FFFF;
            case "gold" -> 0xFFD700;
            default -> 0xFFFFFF;
        };
    }

    private static boolean canSee(MinecraftClient client, LivingEntity target) {
        if (client.player == null || client.world == null) return false;
        Vec3d eyePos = new Vec3d(
                client.player.getX(),
                client.player.getY() + client.player.getEyeHeight(client.player.getPose()),
                client.player.getZ()
        );
        Vec3d targetPos = new Vec3d(
                target.getX(),
                target.getY() + target.getHeight() / 2.0,
                target.getZ()
        );
        HitResult result = client.world.raycast(new RaycastContext(
                eyePos, targetPos,
                RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.NONE,
                client.player
        ));
        return result.getType() == HitResult.Type.MISS;
    }

    private static void drawGlowCircle(DrawContext ctx, int cx, int cy, int radius, float rot, int colorRGB) {
        for (int i = 4; i >= 1; i--) {
            int r = radius + i * 3;
            int alpha = 20 / i;
            int col = (alpha << 24) | colorRGB;
            drawDashedCircle(ctx, cx, cy, r, rot, col, 6, 40);
        }
        drawDashedCircle(ctx, cx, cy, radius, rot, 0xFF000000 | colorRGB, 8, 22);
        drawSolidCircle(ctx, cx, cy, radius - 3, (0x60 << 24) | colorRGB);
    }

    private static void drawDashedCircle(DrawContext ctx, int cx, int cy, int radius,
                                          float rotDeg, int color, int segments, float arcDeg) {
        for (int s = 0; s < segments; s++) {
            double startAngle = Math.toRadians(rotDeg + s * (360.0 / segments));
            double endAngle = startAngle + Math.toRadians(arcDeg);
            int steps = 10;
            int prevX = 0, prevY = 0;
            for (int i = 0; i <= steps; i++) {
                double a = startAngle + (endAngle - startAngle) * i / steps;
                int x = cx + (int) (Math.cos(a) * radius);
                int y = cy + (int) (Math.sin(a) * radius);
                if (i > 0) drawLine(ctx, prevX, prevY, x, y, color);
                prevX = x;
                prevY = y;
            }
        }
    }

    private static void drawSolidCircle(DrawContext ctx, int cx, int cy, int radius, int color) {
        int steps = 72;
        for (int i = 0; i < steps; i++) {
            double a1 = i * 2 * Math.PI / steps;
            double a2 = (i + 1) * 2 * Math.PI / steps;
            int x1 = cx + (int) (Math.cos(a1) * radius);
            int y1 = cy + (int) (Math.sin(a1) * radius);
            int x2 = cx + (int) (Math.cos(a2) * radius);
            int y2 = cy + (int) (Math.sin(a2) * radius);
            drawLine(ctx, x1, y1, x2, y2, color);
        }
    }

    private static void drawGlowSquare(DrawContext ctx, int cx, int cy, int size, float rot, int colorRGB) {
        for (int i = 4; i >= 1; i--) {
            int half = size / 2 + i * 3;
            int alpha = 20 / i;
            int col = (alpha << 24) | colorRGB;
            drawRotatedSquare(ctx, cx, cy, half, rot, col, 6);
        }
        drawRotatedSquare(ctx, cx, cy, size / 2, rot, 0xFF000000 | colorRGB, 8);
        drawRotatedSquare(ctx, cx, cy, size / 2 - 3, rot, (0x60 << 24) | colorRGB, 4);
    }

    private static void drawRotatedSquare(DrawContext ctx, int cx, int cy, int half,
                                           float rotDeg, int color, int segmentsPerSide) {
        double[][] corners = {
                {-half, -half}, {half, -half}, {half, half}, {-half, half}
        };
        double rad = Math.toRadians(rotDeg);
        double cos = Math.cos(rad), sin = Math.sin(rad);
        for (double[] c : corners) {
            double x = c[0] * cos - c[1] * sin;
            double y = c[0] * sin + c[1] * cos;
            c[0] = x;
            c[1] = y;
        }
        for (int side = 0; side < 4; side++) {
            double[] a = corners[side];
            double[] b = corners[(side + 1) % 4];
            int steps = segmentsPerSide;
            int prevX = cx + (int) a[0], prevY = cy + (int) a[1];
            for (int i = 1; i <= steps; i++) {
                float t = (float) i / steps;
                int x = cx + (int) (a[0] + (b[0] - a[0]) * t);
                int y = cy + (int) (a[1] + (b[1] - a[1]) * t);
                drawLine(ctx, prevX, prevY, x, y, color);
                prevX = x;
                prevY = y;
            }
        }
    }

    private static void drawLine(DrawContext ctx, int x1, int y1, int x2, int y2, int color) {
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int steps = Math.max(dx, dy);
        if (steps == 0) { ctx.fill(x1, y1, x1 + 2, y1 + 2, color); return; }
        for (int i = 0; i <= steps; i++) {
            float t = (float) i / steps;
            int x = (int) (x1 + (x2 - x1) * t);
            int y = (int) (y1 + (y2 - y1) * t);
            ctx.fill(x, y, x + 2, y + 2, color);
        }
    }
}