package com.example.ias;

import com.example.ias.account.AccountStorage;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IasMod implements ModInitializer {
    public static final String MOD_ID = "ias";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        AccountStorage.load();
        LOGGER.info("[ias] Загружено аккаунтов: " + AccountStorage.getAccounts().size());
    }
}