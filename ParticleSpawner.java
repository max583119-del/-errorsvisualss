package com.example.client.particle;

import com.example.client.config.ErrorsConfig;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.ActionResult;

import java.util.Random;

public class ParticleSpawner {
    private static final Random RANDOM = new Random();

    public static void register() {
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (!world.isClient()) return ActionResult.PASS;
            if (!ErrorsConfig.particlesEnabled) return ActionResult.PASS;
            spawnHitParticles(entity);
            return ActionResult.PASS;
        });
    }

    private static void spawnHitParticles(Entity target) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return;

        ParticleEffect effect = switch (ErrorsConfig.particleType) {
            case "heart" -> ParticleTypes.HEART;
            case "note" -> ParticleTypes.NOTE;
            case "soul" -> ParticleTypes.SOUL;
            case "end_rod" -> ParticleTypes.END_ROD;
            case "crit" -> ParticleTypes.CRIT;
            default -> ParticleTypes.FLAME;
        };

        double x = target.getX();
        double y = target.getY() + target.getHeight() / 2.0;
        double z = target.getZ();

        int count = ErrorsConfig.particleCount;
        for (int i = 0; i < count; i++) {
            double dx = (RANDOM.nextDouble() - 0.5) * 0.8;
            double dy = (RANDOM.nextDouble() - 0.5) * 0.8;
            double dz = (RANDOM.nextDouble() - 0.5) * 0.8;

            client.particleManager.addParticle(
                    effect,
                    x + dx, y + dy, z + dz,
                    dx * 0.4, dy * 0.4 + 0.1, dz * 0.4
            );
        }
    }
}