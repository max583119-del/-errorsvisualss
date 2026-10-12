package com.example.client.render;

import com.example.client.config.ErrorsConfig;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

public class TargetEspRenderer {

    private static double cosYaw, sinYaw, cosPitch, sinPitch, scale;
    private static int screenW, screenH;
    private static Vec3d camPos;

    private static long lastFrameNs = System.nanoTime();
    private static float time = 0f;

    private static final double[][] CUBE_V = {
        {-1,-1,-1}, { 1,-1,-1}, { 1,-1, 1}, {-1,-1, 1},
        {-1, 1,-1}, { 1, 1,-1}, { 1, 1, 1}, {-1, 1, 1}
    };
    private static final int[][] CUBE_E = {
        {0,1},{1,2},{2,3},{3,0},
        {4,5},{5,6},{6,7},{7,4},
        {0,4},{1,5},{2,6},{3,7}
    };
    private static final double[][] DIAMOND_V = {
        {-1, 0, 0}, { 0,-1, 0}, { 1, 0, 0}, { 0, 1, 0}
    };
    private static final int[][] DIAMOND_E = {
        {0,1},{1,2},{2,3},{3,0}
    };

    private static final int[] pX = new int[8];
    private static final int[] pY = new int[8];
    private static final boolean[] pOk = new boolean[8];

    private static final double TO_RAD = Math.PI / 180.0;
    private static final double MAX_DIST_SQ = 25.0;

    public static void register() {
        HudRenderCallback.EVENT.register((ctx, tickCounter) -> {
            try { render(ctx); } catch (Throwable ignored) {}
        });
    }

    private static void render(DrawContext ctx) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;
        if (!ErrorsConfig.targetEsp) return;

        long now = System.nanoTime();
        float dTime = (now - lastFrameNs) / 1_000_000_000f;
        lastFrameNs = now;
        if (dTime > 0.1f) dTime = 0.1f;
        time += dTime;

        LivingEntity target = findTarget(client);
        if (target == null) return;

        camPos = CameraHelper.getCameraPos(client);
        float yaw = client.gameRenderer.getCamera().getYaw();
        float pitch = client.gameRenderer.getCamera().getPitch();
        screenW = client.getWindow().getScaledWidth();
        screenH = client.getWindow().getScaledHeight();
        double fov = client.options.getFov().getValue();

        double yawRad = yaw * TO_RAD;
        double pitchRad = pitch * TO_RAD;
        cosYaw = Math.cos(yawRad); sinYaw = Math.sin(yawRad);
        cosPitch = Math.cos(pitchRad); sinPitch = Math.sin(pitchRad);
        scale = screenH / (2.0 * Math.tan((fov * TO_RAD) / 2.0));

        float tickDelta = CameraHelper.getTickDelta(client);
        Vec3d pos = target.getLerpedPos(tickDelta);

        Vec3d center = pos.add(0, target.getHeight() * 0.5, 0);
        if (!hasLineOfSight(client, camPos, center)) return;

        int rgb = getColorRGB(target);
        int alpha = (int) (ErrorsConfig.targetEspOpacity * 255);
        if (alpha < 8) return;

        switch (ErrorsConfig.targetEspStyle) {
            case "ghost"   -> drawGhosts(ctx, target, pos, rgb, alpha);
            case "rhombus" -> drawDiamonds(ctx, target, pos, rgb, alpha);
            case "corner"  -> drawCornerBox(ctx, target, pos, rgb, alpha);
            default        -> drawCubes(ctx, target, pos, rgb, alpha);
        }
    }

    // ==================== 3D КУБЫ ====================
    private static void drawCubes(DrawContext ctx, LivingEntity target, Vec3d pos,
                                   int rgb, int alpha) {
        int count = ErrorsConfig.targetEspCubeCount;
        if (count <= 0) return;

        float height = target.getHeight();
        float baseRadius = Math.max(0.7f, target.getWidth() * 1.1f);

        for (int i = 0; i < count; i++) {
            float phase = i * (6.283185f / count);
            float orbitSpeed = 1.3f + (i % 3) * 0.3f;
            float angle = time * orbitSpeed + phase;

            float yBase = height * (0.20f + (i % 3) * 0.28f);
            float yWave = (float) Math.sin(time * 1.8f + i * 1.5f) * height * 0.12f;

            double ox = Math.cos(angle) * baseRadius;
            double oz = Math.sin(angle) * baseRadius * 0.9;
            double oy = yBase + yWave;

            double cubeX = pos.x + ox;
            double cubeY = pos.y + oy;
            double cubeZ = pos.z + oz;

            float size = 0.14f;

            float sx = time * 2.5f + i * 0.7f;
            float sy = time * 1.8f + i * 1.3f;
            float sz = time * 3.0f + i * 0.4f;
            double cx = Math.cos(sx), sX = Math.sin(sx);
            double cy = Math.cos(sy), sY = Math.sin(sy);
            double cz = Math.cos(sz), sZ = Math.sin(sz);

            for (int k = 0; k < 8; k++) {
                double vx = CUBE_V[k][0] * size;
                double vy = CUBE_V[k][1] * size;
                double vz = CUBE_V[k][2] * size;

                double ny = vy * cx - vz * sX;
                double nz = vy * sX + vz * cx;
                vy = ny; vz = nz;

                double nx2 = vx * cy + vz * sY;
                nz = -vx * sY + vz * cy;
                vx = nx2; vz = nz;

                nx2 = vx * cz - vy * sZ;
                ny = vx * sZ + vy * cz;
                vx = nx2; vy = ny;

                project(cubeX + vx, cubeY + vy, cubeZ + vz, k);
            }

            // Пульсация яркости куба
            float pulse = 0.85f + 0.15f * (float) Math.sin(time * 2f + i * 1.3f);

            for (int[] e : CUBE_E) {
                if (pOk[e[0]] && pOk[e[1]]) {
                    drawGlowLine(ctx, pX[e[0]], pY[e[0]], pX[e[1]], pY[e[1]], rgb, alpha, pulse);
                }
            }
        }
    }

    // ==================== ПРИЗРАКИ (орбы со шлейфом) ====================
    private static void drawGhosts(DrawContext ctx, LivingEntity target, Vec3d pos,
                                    int rgb, int alpha) {
        int count = ErrorsConfig.targetEspGhostCount;
        if (count <= 0) return;

        float height = target.getHeight();
        float baseRadius = Math.max(0.7f, target.getWidth() * 1.05f);
        int coreRgb = blendToWhite(rgb, 0.80f);
        int TRAIL_POINTS = 6;
        float TRAIL_STEP = 0.10f;

        for (int i = 0; i < count; i++) {
            float phase = i * (6.283185f / count);
            float orbitSpeed = 1.6f + (i & 1) * 0.4f;
            float baseAngle = time * orbitSpeed + phase;
            float yOffset = height * (0.30f + (i % 3) * 0.18f);

            for (int t = TRAIL_POINTS; t >= 1; t--) {
                float trailAngle = baseAngle - t * TRAIL_STEP;
                drawGhostTrailPoint(ctx, pos, trailAngle, baseRadius, yOffset, height,
                                    coreRgb, alpha, t, TRAIL_POINTS);
            }
            drawGhostOrb(ctx, pos, baseAngle, baseRadius, yOffset, height, rgb, coreRgb, alpha);
        }
    }

    private static void drawGhostTrailPoint(DrawContext ctx, Vec3d pos, float angle,
                                             float baseRadius, float yOffset, float height,
                                             int coreRgb, int alpha, int trailIndex, int maxTrail) {
        double lx = Math.cos(angle) * baseRadius;
        double lz = Math.sin(angle) * baseRadius * 0.85;
        double ly = yOffset + Math.sin(angle) * height * 0.15;

        double wx = pos.x + lx, wy = pos.y + ly, wz = pos.z + lz;
        if (!project(wx, wy, wz, 0)) return;
        int px = pX[0], py = pY[0];

        float fade = 1.0f - (float) trailIndex / (maxTrail + 1.0f);
        fade = fade * fade;
        int a = (int) (alpha * fade * 0.55f);
        if (a < 5) return;

        double dist = sqrtDist(wx, wy, wz);
        int size = (int) (60.0 / Math.max(1.0, dist));
        if (size < 3) size = 3;
        else if (size > 8) size = 8;
        int s = Math.max(2, (int) (size * fade));
        ctx.fill(px - s/2, py - s/2, px + s/2, py + s/2, (a << 24) | coreRgb);
    }

    private static void drawGhostOrb(DrawContext ctx, Vec3d pos, float angle,
                                      float baseRadius, float yOffset, float height,
                                      int rgb, int coreRgb, int alpha) {
        double lx = Math.cos(angle) * baseRadius;
        double lz = Math.sin(angle) * baseRadius * 0.85;
        double ly = yOffset + Math.sin(angle) * height * 0.15;

        double wx = pos.x + lx, wy = pos.y + ly, wz = pos.z + lz;
        if (!project(wx, wy, wz, 0)) return;
        int px = pX[0], py = pY[0];

        double dist = sqrtDist(wx, wy, wz);
        int size = (int) (60.0 / Math.max(1.0, dist));
        if (size < 3) size = 3;
        else if (size > 8) size = 8;

        float pulse = 0.88f + 0.12f * (float) Math.sin(time * 4f + angle * 2);
        int a = (int) (alpha * pulse);
        if (a < 8) return;

        int a1 = (int) (a * 0.18f);
        if (a1 > 4) drawCircle(ctx, px, py, size + 5, (a1 << 24) | (rgb & 0xFFFFFF));
        int a2 = (int) (a * 0.40f);
        if (a2 > 4) drawCircle(ctx, px, py, size + 3, (a2 << 24) | (rgb & 0xFFFFFF));
        int a3 = (int) (a * 0.75f);
        if (a3 > 4) drawCircle(ctx, px, py, size + 1, (a3 << 24) | coreRgb);
        int a4 = a;
        if (a4 > 4) drawCircle(ctx, px, py, size - 1, (a4 << 24) | 0xFFFFFF);
    }

    // ==================== 2D РОМБЫ ====================
    private static void drawDiamonds(DrawContext ctx, LivingEntity target, Vec3d pos,
                                      int rgb, int alpha) {
        int count = ErrorsConfig.targetEspDiamondCount;
        if (count <= 0) return;

        float height = target.getHeight();
        float baseRadius = Math.max(0.7f, target.getWidth() * 1.2f);

        for (int i = 0; i < count; i++) {
            float phase = i * (6.283185f / count);
            float orbitSpeed = 1.3f + (i % 3) * 0.3f;
            float angle = time * orbitSpeed + phase;

            float yBase = height * (0.20f + (i % 4) * 0.22f);
            float yWave = (float) Math.sin(time * 1.8f + i * 1.5f) * height * 0.10f;

            double ox = Math.cos(angle) * baseRadius;
            double oz = Math.sin(angle) * baseRadius * 0.85;
            double oy = yBase + yWave;

            double dX = pos.x + ox, dY = pos.y + oy, dZ = pos.z + oz;

            float size = 0.14f;
            float spinZ = time * 2.0f + i * 0.9f;
            double cz = Math.cos(spinZ), sZ = Math.sin(spinZ);

            for (int k = 0; k < 4; k++) {
                double vx = DIAMOND_V[k][0] * size;
                double vy = DIAMOND_V[k][1] * size;
                double nx = vx * cz - vy * sZ;
                double ny = vx * sZ + vy * cz;
                project(dX + nx, dY + ny, dZ, k);
            }

            float pulse = 0.85f + 0.15f * (float) Math.sin(time * 2f + i * 1.1f);

            for (int[] e : DIAMOND_E) {
                if (pOk[e[0]] && pOk[e[1]]) {
                    drawGlowLine(ctx, pX[e[0]], pY[e[0]], pX[e[1]], pY[e[1]], rgb, alpha, pulse);
                }
            }
        }
    }

    // ==================== СКОБКИ ====================
    private static void drawCornerBox(DrawContext ctx, LivingEntity target, Vec3d pos,
                                       int rgb, int alpha) {
        float halfW = target.getWidth() / 2f + 0.03f;
        float height = target.getHeight();

        project(pos.x - halfW, pos.y,          pos.z - halfW, 0);
        project(pos.x + halfW, pos.y,          pos.z - halfW, 1);
        project(pos.x + halfW, pos.y,          pos.z + halfW, 2);
        project(pos.x - halfW, pos.y,          pos.z + halfW, 3);
        project(pos.x - halfW, pos.y + height, pos.z - halfW, 4);
        project(pos.x + halfW, pos.y + height, pos.z - halfW, 5);
        project(pos.x + halfW, pos.y + height, pos.z + halfW, 6);
        project(pos.x - halfW, pos.y + height, pos.z + halfW, 7);

        int[][] edges = {
            {0,1},{1,2},{2,3},{3,0},
            {4,5},{5,6},{6,7},{7,4},
            {0,4},{1,5},{2,6},{3,7}
        };
        float pulse = 0.85f + 0.15f * (float) Math.sin(time * 2f);
        for (int[] e : edges) {
            if (pOk[e[0]] && pOk[e[1]]) {
                drawGlowLine(ctx, pX[e[0]], pY[e[0]], pX[e[1]], pY[e[1]], rgb, alpha, pulse);
            }
        }
    }

    // ==================== ПРОЕКЦИЯ ====================
    private static boolean project(double wx, double wy, double wz, int index) {
        double rx = wx - camPos.x;
        double ry = wy - camPos.y;
        double rz = wz - camPos.z;

        double xRot = -rx * cosYaw - rz * sinYaw;
        double zRot = -rx * sinYaw + rz * cosYaw;
        double yRot = ry * cosPitch + zRot * sinPitch;
        double zFinal = -ry * sinPitch + zRot * cosPitch;

        if (zFinal < 0.5) { pOk[index] = false; return false; }

        double sx = xRot * scale / zFinal + screenW * 0.5;
        double sy = -yRot * scale / zFinal + screenH * 0.5;

        if (Double.isNaN(sx) || Double.isNaN(sy)
                || Double.isInfinite(sx) || Double.isInfinite(sy)
                || Math.abs(sx) > 20000 || Math.abs(sy) > 20000) {
            pOk[index] = false;
            return false;
        }

        pX[index] = (int) sx;
        pY[index] = (int) sy;
        pOk[index] = true;
        return true;
    }

    private static double sqrtDist(double wx, double wy, double wz) {
        double dx = wx - camPos.x;
        double dy = wy - camPos.y;
        double dz = wz - camPos.z;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    // ==================== ГЛАВНОЕ: КРАСИВАЯ ЛИНИЯ СО СВЕЧЕНИЕМ ====================
    // 3 слоя: внешний ореол 5px (низкая альфа), средний 3px (средняя), ядро 1px (яркое)
    private static void drawGlowLine(DrawContext ctx, int x1, int y1, int x2, int y2,
                                      int rgb, int alpha, float pulse) {
        // Внешний ореол — очень мягкий, расширяет линию
        int glowA = (int) (alpha * 0.10f * pulse);
        if (glowA > 3) {
            drawLine(ctx, x1, y1, x2, y2, (glowA << 24) | (rgb & 0xFFFFFF), 5);
        }
        // Средний слой — заметное свечение
        int midA = (int) (alpha * 0.30f * pulse);
        if (midA > 5) {
            drawLine(ctx, x1, y1, x2, y2, (midA << 24) | (rgb & 0xFFFFFF), 3);
        }
        // Ядро — тонкое, яркое, чуть ближе к белому
        int coreA = (int) (alpha * 0.95f);
        if (coreA > 5) {
            int coreRgb = blendToWhite(rgb, 0.55f);
            drawLine(ctx, x1, y1, x2, y2, (coreA << 24) | coreRgb, 1);
        }
    }

    // Плавная линия — шаг 1 пиксель (вместо 2) для мягкости
    private static void drawLine(DrawContext ctx, int x1, int y1, int x2, int y2,
                                  int color, int thickness) {
        int dx = x2 - x1, dy = y2 - y1;
        int adx = dx < 0 ? -dx : dx;
        int ady = dy < 0 ? -dy : dy;
        int steps = adx > ady ? adx : ady;

        if (steps == 0) {
            ctx.fill(x1, y1, x1 + thickness, y1 + thickness, color);
            return;
        }
        if (steps > 3000) return;

        // Шаг = 1 для тонких линий (плавно), 2 для толстых (быстро)
        int step = (thickness >= 3) ? 2 : 1;
        for (int i = 0; i <= steps; i += step) {
            float t = (float) i / steps;
            int x = x1 + (int) (dx * t);
            int y = y1 + (int) (dy * t);
            ctx.fill(x, y, x + thickness, y + thickness, color);
        }
    }

    private static void drawCircle(DrawContext ctx, int cx, int cy, int r, int color) {
        if (r <= 0 || r > 40) return;
        int r2 = r * r;
        for (int dy = -r; dy <= r; dy++) {
            int dx = (int) Math.sqrt(r2 - dy * dy);
            ctx.fill(cx - dx, cy + dy, cx + dx + 1, cy + dy + 1, color);
        }
    }

    private static int blendToWhite(int rgb, float t) {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        r = (int) (r + (255 - r) * t);
        g = (int) (g + (255 - g) * t);
        b = (int) (b + (255 - b) * t);
        return (r << 16) | (g << 8) | b;
    }

    // ==================== ХЕЛПЕРЫ ====================
    private static boolean hasLineOfSight(MinecraftClient client, Vec3d from, Vec3d to) {
        try {
            RaycastContext rc = new RaycastContext(
                    from, to,
                    RaycastContext.ShapeType.COLLIDER,
                    RaycastContext.FluidHandling.NONE,
                    client.player);
            var hit = client.world.raycast(rc);
            if (hit == null) return true;
            double hitDist = hit.getPos().distanceTo(from);
            double targetDist = to.distanceTo(from);
            return hitDist >= targetDist * 0.85;
        } catch (Throwable t) {
            return true;
        }
    }

    private static LivingEntity findTarget(MinecraftClient client) {
        LivingEntity nearest = null;
        double nearestSq = MAX_DIST_SQ;
        for (Entity e : client.world.getEntities()) {
            if (!(e instanceof LivingEntity living)) continue;
            if (living == client.player) continue;
            if (living.isInvisible()) continue;
            if (!living.isAlive()) continue;
            double sq = client.player.squaredDistanceTo(living);
            if (sq >= nearestSq) continue;
            nearestSq = sq;
            nearest = living;
        }
        return nearest;
    }

    private static int getColorRGB(LivingEntity target) {
        if (ErrorsConfig.targetEspHealthColor) {
            float hp = target.getHealth() / Math.max(1.0f, target.getMaxHealth());
            if (hp > 0.66f) return 0x55FF55;
            if (hp > 0.33f) return 0xFFD700;
            return 0xFF3355;
        }
        return switch (ErrorsConfig.targetEspColor) {
            case "red" -> 0xFF3355;
            case "green" -> 0x55FF55;
            case "cyan" -> 0x55FFFF;
            case "gold" -> 0xFFD700;
            case "blue" -> 0x0099FF;
            default -> 0xFFFFFF;
        };
    }
}