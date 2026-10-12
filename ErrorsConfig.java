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
    public static float aspectRatioCustom = 1.777f;

    public static boolean particlesEnabled = false;
    public static String particleType = "flame";
    public static int particleCount = 15;

    public static boolean targetEsp = false;
    public static String targetEspStyle = "square";
    public static String targetEspColor = "white";
    public static float targetEspOpacity = 1.0f;
    public static boolean targetEspHealthColor = false;
    public static int targetEspCubeCount = 4;
    public static int targetEspDiamondCount = 4;
    public static int targetEspGhostCount = 4;

    public static boolean fullBright = false;
    public static boolean trailEnabled = false;
    public static String trailColor = "cyan";
    public static boolean jumpCircle = false;
    public static String jumpCircleColor = "white";
    public static boolean chinaHat = false;
    public static String chinaHatColor = "gold";
    public static float chinaHatSize = 1.0f;
    public static float chinaHatAlpha = 0.5f;

    public static boolean zoomEnabled = true;
    public static float zoomLevel = 4.0f;

    public static boolean petsEnabled = false;
    public static String petsType = "heart";
    public static int petsCount = 4;
    public static float petsRadius = 1.2f;
    public static float petsSpeed = 60f;

    public static boolean winterEnabled = false;
    public static int winterDensity = 80;
    public static boolean winterTint = true;
    public static boolean winterStorm = true;
    public static float winterSpeed = 1.0f;
    public static float winterFog = 0.5f;

    // ===== Ник в главном меню =====
    public static String customNickname = "";

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("errorsvisuals.json");

    private static class Data {
        boolean noFire, noPumpkin, itemScroller, aspectRatioEnabled;
        String aspectRatioPreset; float aspectRatioCustom;
        boolean particlesEnabled; String particleType; int particleCount;
        boolean targetEsp; String targetEspStyle, targetEspColor;
        float targetEspOpacity; boolean targetEspHealthColor;
        int targetEspCubeCount, targetEspDiamondCount, targetEspGhostCount;
        boolean fullBright, trailEnabled; String trailColor;
        boolean jumpCircle; String jumpCircleColor;
        boolean chinaHat; String chinaHatColor; float chinaHatSize, chinaHatAlpha;
        boolean zoomEnabled; float zoomLevel;
        boolean petsEnabled; String petsType; int petsCount; float petsRadius, petsSpeed;
        boolean winterEnabled; int winterDensity; boolean winterTint; boolean winterStorm;
        float winterSpeed; float winterFog;
        String customNickname;
    }

    public static void load() {
        if (!Files.exists(PATH)) { save(); return; }
        try {
            Data d = GSON.fromJson(Files.readString(PATH), Data.class);
            if (d == null) return;
            noFire = d.noFire; noPumpkin = d.noPumpkin; itemScroller = d.itemScroller;
            aspectRatioEnabled = d.aspectRatioEnabled;
            aspectRatioPreset = d.aspectRatioPreset != null ? d.aspectRatioPreset : "16:9";
            aspectRatioCustom = d.aspectRatioCustom > 0 ? d.aspectRatioCustom : 1.777f;
            particlesEnabled = d.particlesEnabled;
            particleType = d.particleType != null ? d.particleType : "flame";
            particleCount = d.particleCount > 0 ? d.particleCount : 15;
            targetEsp = d.targetEsp;
            targetEspStyle = d.targetEspStyle != null ? d.targetEspStyle : "square";
            targetEspColor = d.targetEspColor != null ? d.targetEspColor : "white";
            targetEspOpacity = d.targetEspOpacity > 0 ? d.targetEspOpacity : 1.0f;
            targetEspHealthColor = d.targetEspHealthColor;
            targetEspCubeCount = d.targetEspCubeCount > 0 ? d.targetEspCubeCount : 4;
            targetEspDiamondCount = d.targetEspDiamondCount > 0 ? d.targetEspDiamondCount : 4;
            targetEspGhostCount = d.targetEspGhostCount > 0 ? d.targetEspGhostCount : 4;
            fullBright = d.fullBright; trailEnabled = d.trailEnabled;
            trailColor = d.trailColor != null ? d.trailColor : "cyan";
            jumpCircle = d.jumpCircle;
            jumpCircleColor = d.jumpCircleColor != null ? d.jumpCircleColor : "white";
            chinaHat = d.chinaHat;
            chinaHatColor = d.chinaHatColor != null ? d.chinaHatColor : "gold";
            chinaHatSize = d.chinaHatSize > 0 ? d.chinaHatSize : 1.0f;
            chinaHatAlpha = d.chinaHatAlpha > 0 ? d.chinaHatAlpha : 0.5f;
            zoomEnabled = d.zoomEnabled;
            zoomLevel = d.zoomLevel > 1.0f ? d.zoomLevel : 4.0f;
            petsEnabled = d.petsEnabled;
            petsType = d.petsType != null ? d.petsType : "heart";
            petsCount = d.petsCount > 0 ? d.petsCount : 4;
            petsRadius = d.petsRadius > 0 ? d.petsRadius : 1.2f;
            petsSpeed = d.petsSpeed > 0 ? d.petsSpeed : 60f;
            winterEnabled = d.winterEnabled;
            winterDensity = d.winterDensity > 0 ? d.winterDensity : 80;
            winterTint = d.winterTint;
            winterStorm = d.winterStorm;
            winterSpeed = d.winterSpeed > 0 ? d.winterSpeed : 1.0f;
            winterFog = d.winterFog > 0 ? d.winterFog : 0.5f;
            customNickname = d.customNickname != null ? d.customNickname : "";
        } catch (Exception e) {
            System.err.println("[errorsVisuals] Load failed: " + e.getMessage());
        }
    }

    public static void save() {
        try {
            Data d = new Data();
            d.noFire = noFire; d.noPumpkin = noPumpkin; d.itemScroller = itemScroller;
            d.aspectRatioEnabled = aspectRatioEnabled; d.aspectRatioPreset = aspectRatioPreset;
            d.aspectRatioCustom = aspectRatioCustom;
            d.particlesEnabled = particlesEnabled; d.particleType = particleType; d.particleCount = particleCount;
            d.targetEsp = targetEsp; d.targetEspStyle = targetEspStyle; d.targetEspColor = targetEspColor;
            d.targetEspOpacity = targetEspOpacity; d.targetEspHealthColor = targetEspHealthColor;
            d.targetEspCubeCount = targetEspCubeCount;
            d.targetEspDiamondCount = targetEspDiamondCount;
            d.targetEspGhostCount = targetEspGhostCount;
            d.fullBright = fullBright; d.trailEnabled = trailEnabled; d.trailColor = trailColor;
            d.jumpCircle = jumpCircle; d.jumpCircleColor = jumpCircleColor;
            d.chinaHat = chinaHat; d.chinaHatColor = chinaHatColor;
            d.chinaHatSize = chinaHatSize; d.chinaHatAlpha = chinaHatAlpha;
            d.zoomEnabled = zoomEnabled; d.zoomLevel = zoomLevel;
            d.petsEnabled = petsEnabled; d.petsType = petsType; d.petsCount = petsCount;
            d.petsRadius = petsRadius; d.petsSpeed = petsSpeed;
            d.winterEnabled = winterEnabled; d.winterDensity = winterDensity;
            d.winterTint = winterTint; d.winterStorm = winterStorm;
            d.winterSpeed = winterSpeed; d.winterFog = winterFog;
            d.customNickname = customNickname;
            Files.createDirectories(PATH.getParent());
            Files.writeString(PATH, GSON.toJson(d));
        } catch (IOException e) {
            System.err.println("[errorsVisuals] Save failed: " + e.getMessage());
        }
    }
}