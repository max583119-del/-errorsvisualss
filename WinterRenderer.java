package com.example.client.render;

import com.example.client.config.ErrorsConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;

import java.util.Random;

public class WinterRenderer {

    private static final Random RNG = new Random();

    private static float windPhase = 0f;
    private static long lastTick = System.nanoTime();

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!ErrorsConfig.winterEnabled) return;
            if (client.player == null || client.world == null) return;

            long now = System.nanoTime();
            float dt = (now - lastTick) / 1_000_000_000f;
            lastTick = now;
            if (dt > 0.1f) dt = 0.1f;
            windPhase += dt * 0.3f;

            double windX = Math.sin(windPhase) * 0.25;
            double windZ = Math.cos(windPhase * 0.7f) * 0.20;

            int perTick = Math.max(1, ErrorsConfig.winterDensity / 15);

            for (int i = 0; i < perTick; i++) {
                double px = client.player.getX() + (RNG.nextDouble() - 0.5) * 40;

                // КЛЮЧЕВОЕ: спавним частицы ПО ВСЕЙ ВЫСОТЕ вокруг игрока
                // От -3 (под ногами) до +25 (над головой) — распределены равномерно
                // Так частицы есть и у земли, и в воздухе, независимо от времени жизни
                double py = client.player.getY() - 3 + RNG.nextDouble() * 28;

                double pz = client.player.getZ() + (RNG.nextDouble() - 0.5) * 40;

                float spd = ErrorsConfig.winterSpeed;
                double vx = windX * (0.5 + RNG.nextDouble() * 0.5) * spd;
                double vy = -(0.10 + RNG.nextDouble() * 0.12) * spd;
                double vz = windZ * (0.5 + RNG.nextDouble() * 0.5) * spd;

                ParticleEffect effect;
                int roll = RNG.nextInt(10);
                if (roll < 5) {
                    effect = ParticleTypes.END_ROD;
                } else if (roll < 9) {
                    effect = ParticleTypes.WHITE_ASH;
                } else {
                    effect = ParticleTypes.SNOWFLAKE;
                }

                try {
                    client.particleManager.addParticle(effect, px, py, pz, vx, vy, vz);
                } catch (Throwable ignored) {}
            }
        });
    }
}