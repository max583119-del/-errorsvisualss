package com.example.ias.gui;

import com.example.ias.account.Account;
import com.example.ias.account.AccountStorage;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;

public class AccountSwitcherScreen extends Screen {

    private static final int BG = 0xC0202028;
    private static final int TEXT = 0xFFFFFFFF;
    private static final int TEXT_DIM = 0xFFA8A29A;

    public AccountSwitcherScreen() {
        super(Text.literal("Аккаунты"));
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, width, height, 0x80000000);
        ctx.fill(width / 2 - 150, height / 2 - 120, width / 2 + 150, height / 2 + 120, BG);
        ctx.drawText(textRenderer, Text.literal("§6✦ Аккаунты"),
                width / 2 - 140, height / 2 - 110, TEXT, false);

        var accounts = AccountStorage.getAccounts();
        if (accounts.isEmpty()) {
            ctx.drawText(textRenderer, Text.literal("§7Нет аккаунтов."),
                    width / 2 - 140, height / 2 - 80, TEXT_DIM, false);
            ctx.drawText(textRenderer, Text.literal("§7Добавь вручную в config/ias-accounts.json"),
                    width / 2 - 140, height / 2 - 65, TEXT_DIM, false);
        }

        int y = height / 2 - 80;
        for (Account acc : accounts) {
            boolean hover = mouseX >= width / 2 - 140 && mouseX <= width / 2 + 140
                    && mouseY >= y - 2 && mouseY <= y + 14;
            int bg = hover ? 0x40FFAA55 : 0x20000000;
            ctx.fill(width / 2 - 140, y - 2, width / 2 + 140, y + 14, bg);
            ctx.drawText(textRenderer, Text.literal("§f" + acc.name),
                    width / 2 - 136, y, TEXT, false);
            y += 20;
        }

        ctx.drawText(textRenderer, Text.literal("§8Esc — закрыть"),
                width / 2 - 140, height / 2 + 100, TEXT_DIM, false);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        double mx = click.x(), my = click.y();
        var accounts = AccountStorage.getAccounts();
        int y = height / 2 - 80;
        for (Account acc : accounts) {
            if (mx >= width / 2 - 140 && mx <= width / 2 + 140 && my >= y - 2 && my <= y + 14) {
                MinecraftClient client = MinecraftClient.getInstance();
                client.getSession();
                // TODO: switch session
                client.setScreen(null);
                return true;
            }
            y += 20;
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if (input.key() == 256) {
            MinecraftClient.getInstance().setScreen(null);
            return true;
        }
        return super.keyPressed(input);
    }

    @Override
    public boolean shouldPause() { return false; }
}