package com.example.ias.account;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class AccountStorage {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("ias-accounts.json");
    private static final List<Account> ACCOUNTS = new ArrayList<>();

    public static List<Account> getAccounts() { return ACCOUNTS; }

    public static void load() {
        ACCOUNTS.clear();
        if (!Files.exists(PATH)) return;
        try {
            String json = Files.readString(PATH);
            Account[] arr = GSON.fromJson(json, Account[].class);
            if (arr != null) {
                for (Account a : arr) ACCOUNTS.add(a);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static void save() {
        try {
            Files.createDirectories(PATH.getParent());
            Files.writeString(PATH, GSON.toJson(ACCOUNTS));
        } catch (Exception e) { e.printStackTrace(); }
    }
}