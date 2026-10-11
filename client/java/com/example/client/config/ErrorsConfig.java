package com.example.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ErrorsConfig {
    public static boolean noFire = true;
    public static boolean noPumpkin = true;
    public static boolean itemScroller = true;
    public static boolean aspectRatioEnabled = false;
    public static String aspectRatioPreset = "16:9";

    public static boolean particlesEnabled = false;
    public static String particleType = "flame";
    public static int particleCount = 15;

    public static boolean targetEsp = false;
    public static String targetEspShape = "circle";
    public static String targetEspColor = "white";

    public static boolean fullBright = false;
    public static boolean trailEnabled = false;
    public static String trailColor = "cyan";
    public static boolean jumpCircle = false;
    public static String jumpCircleColor = "white";
    public static boolean chinaHat = false;
    public static String chinaHatColor = "gold";
    public static float chinaHatSize = 1.0f;
    public static float chinaHatAlpha = 0.5f;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("errorsvisuals.json");

    private static class Data {
        boolean noFire, noPumpkin, itemScroller;
        boolean aspectRatioEnabled; String aspectRatioPreset;
        boolean particlesEnabled; String particleType; int particleCount;
        boolean targetEsp; String targetEspShape, targetEspColor;
        boolean fullBright, trailEnabled; String trailColor;
        boolean jumpCircle; String jumpCircleColor;
        boolean chinaHat; String chinaHatColor; float chinaHatSize, chinaHatAlpha;
    }

    public static void load() {
        if (!Files.exists(PATH)) { save(); return; }
        try {
            Data d = GSON.fromJson(Files.readString(PATH), Data.class);
            if (d == null) return;
            noFire = d.noFire;
            noPumpkin = d.noPumpkin;
            itemScroller = d.itemScroller;
            aspectRatioEnabled = d.aspectRatioEnabled;
            aspectRatioPreset = d.aspectRatioPreset != null ? d.aspectRatioPreset : "16:9";
            particlesEnabled = d.particlesEnabled;
            particleType = d.particleType != null ? d.particleType : "flame";
            particleCount = d.particleCount > 0 ? d.particleCount : 15;
            targetEsp = d.targetEsp;
            targetEspShape = d.targetEspShape != null ? d.targetEspShape : "circle";
            targetEspColor = d.targetEspColor != null ? d.targetEspColor : "white";
            fullBright = d.fullBright;
            trailEnabled = d.trailEnabled;
            trailColor = d.trailColor != null ? d.trailColor : "cyan";
            jumpCircle = d.jumpCircle;
            jumpCircleColor = d.jumpCircleColor != null ? d.jumpCircleColor : "white";
            chinaHat = d.chinaHat;
            chinaHatColor = d.chinaHatColor != null ? d.chinaHatColor : "gold";
            chinaHatSize = d.chinaHatSize > 0 ? d.chinaHatSize : 1.0f;
            chinaHatAlpha = d.chinaHatAlpha > 0 ? d.chinaHatAlpha : 0.5f;
        } catch (Exception e) {
            System.err.println("[errorsVisuals] Load failed: " + e.getMessage());
        }
    }

    public static void save() {
        try {
            Data d = new Data();
            d.noFire = noFire;
            d.noPumpkin = noPumpkin;
            d.itemScroller = itemScroller;
            d.aspectRatioEnabled = aspectRatioEnabled;
            d.aspectRatioPreset = aspectRatioPreset;
            d.particlesEnabled = particlesEnabled;
            d.particleType = particleType;
            d.particleCount = particleCount;
            d.targetEsp = targetEsp;
            d.targetEspShape = targetEspShape;
            d.targetEspColor = targetEspColor;
            d.fullBright = fullBright;
            d.trailEnabled = trailEnabled;
            d.trailColor = trailColor;
            d.jumpCircle = jumpCircle;
            d.jumpCircleColor = jumpCircleColor;
            d.chinaHat = chinaHat;
            d.chinaHatColor = chinaHatColor;
            d.chinaHatSize = chinaHatSize;
            d.chinaHatAlpha = chinaHatAlpha;
            Files.createDirectories(PATH.getParent());
            Files.writeString(PATH, GSON.toJson(d));
        } catch (IOException e) {
            System.err.println("[errorsVisuals] Save failed: " + e.getMessage());
        }
    }
}