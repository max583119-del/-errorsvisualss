package com.example.client.render;

import com.example.client.config.ErrorsConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;

public class PetRenderer {

    private static float angle = 0f;

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!ErrorsConfig.petsEnabled) return;
            if (client.player == null || client.world == null) return;

            // Угол вращения
            angle += ErrorsConfig.petsSpeed / 20f;
            if (angle >= 360f) angle -= 360f;

            ParticleEffect effect = switch (ErrorsConfig.petsType) {
                case "flame" -> ParticleTypes.FLAME;
                case "note" -> ParticleTypes.NOTE;
                case "soul" -> ParticleTypes.SOUL;
                case "end_rod" -> ParticleTypes.END_ROD;
                case "crit" -> ParticleTypes.CRIT;
                default -> ParticleTypes.HEART;
            };

            double px = client.player.getX();
            double py = client.player.getY() + 1.2;
            double pz = client.player.getZ();
            float radius = ErrorsConfig.petsRadius;
            int count = ErrorsConfig.petsCount;

            for (int i = 0; i < count; i++) {
                double a = Math.toRadians(angle + (360.0 / count) * i);
                double x = px + Math.cos(a) * radius;
                double z = pz + Math.sin(a) * radius;
                client.particleManager.addParticle(effect, x, py, z, 0, 0, 0);
            }
        });
    }
}