package com.example.client.gui;

import com.example.client.config.ErrorsConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ErrorsVisualsScreen extends Screen {

    private int currentTab = 0;

    private final List<ButtonWidget> hoverList = new ArrayList<>();
    private final Map<ButtonWidget, String> tooltips = new HashMap<>();

    public ErrorsVisualsScreen() {
        super(Text.literal("errorsVisuals"));
    }

    private ButtonWidget addBtn(ButtonWidget btn, String tooltip) {
        hoverList.add(btn);
        tooltips.put(btn, tooltip);
        return this.addDrawableChild(btn);
    }

    @Override
    protected void init() {
        hoverList.clear();
        tooltips.clear();

        int cx = (width - 520) / 2;
        int cy = (height - 320) / 2;

        // ============ ВКЛАДКИ ============
        addBtn(ButtonWidget.builder(
                Text.literal(currentTab == 0 ? "§e§l⚔ PvP" : "§7⚔ PvP"),
                b -> { currentTab = 0; this.clearAndInit(); }
        ).dimensions(cx + 15, cy + 50, 130, 26).build(),
                "Визуалы для боя: убирают помехи на экране.");

        addBtn(ButtonWidget.builder(
                Text.literal(currentTab == 1 ? "§e§l◎ Target ESP" : "§7◎ Target ESP"),
                b -> { currentTab = 1; this.clearAndInit(); }
        ).dimensions(cx + 15, cy + 82, 130, 26).build(),
                "Подсветка ближайшего игрока или моба рядом с тобой.");

        addBtn(ButtonWidget.builder(
                Text.literal(currentTab == 2 ? "§e§l✦ Партиклы" : "§7✦ Партиклы"),
                b -> { currentTab = 2; this.clearAndInit(); }
        ).dimensions(cx + 15, cy + 114, 130, 26).build(),
                "Частицы при ударе по мобу или игроку.");

        addBtn(ButtonWidget.builder(
                Text.literal(currentTab == 3 ? "§e§l✦ Визуалы" : "§7✦ Визуалы"),
                b -> { currentTab = 3; this.clearAndInit(); }
        ).dimensions(cx + 15, cy + 146, 130, 26).build(),
                "FullBright, шлейф, шляпа, круг при прыжке.");

        addBtn(ButtonWidget.builder(
                Text.literal(currentTab == 4 ? "§e§l⚙ Прочее" : "§7⚙ Прочее"),
                b -> { currentTab = 4; this.clearAndInit(); }
        ).dimensions(cx + 15, cy + 178, 130, 26).build(),
                "Информация о моде.");

        switch (currentTab) {
            case 0 -> initPvp(cx, cy);
            case 1 -> initEsp(cx, cy);
            case 2 -> initParticles(cx, cy);
            case 3 -> initVisuals(cx, cy);
            case 4 -> initOther(cx, cy);
        }

        addBtn(ButtonWidget.builder(
                Text.literal("§cЗакрыть"),
                b -> this.close()
        ).dimensions(cx + 160, cy + 275, 340, 26).build(),
                "Закрыть меню (или нажми ESC).");
    }

    private void initPvp(int cx, int cy) {
        int bx = cx + 160, by = cy + 55;

        addBtn(ButtonWidget.builder(
                Text.literal("§fУбирать огонь: " + onOff(ErrorsConfig.noFire)),
                b -> {
                    ErrorsConfig.noFire = !ErrorsConfig.noFire;
                    ErrorsConfig.save();
                    this.clearAndInit();
                }
        ).dimensions(bx, by, 340, 26).build(),
                "Когда ты горишь, огонь не закрывает обзор.");

        addBtn(ButtonWidget.builder(
                Text.literal("§fУбирать тыкву: " + onOff(ErrorsConfig.noPumpkin)),
                b -> {
                    ErrorsConfig.noPumpkin = !ErrorsConfig.noPumpkin;
                    ErrorsConfig.save();
                    this.clearAndInit();
                }
        ).dimensions(bx, by + 34, 340, 26).build(),
                "Если носишь тыкву на голове — она не будет закрывать экран.");

        addBtn(ButtonWidget.builder(
                Text.literal("§fItem Scroller: " + onOff(ErrorsConfig.itemScroller)),
                b -> {
                    ErrorsConfig.itemScroller = !ErrorsConfig.itemScroller;
                    ErrorsConfig.save();
                    this.clearAndInit();
                }
        ).dimensions(bx, by + 68, 340, 26).build(),
                "Наведи мышь на слот и крути колёсико — предмет переместится.");

        addBtn(ButtonWidget.builder(
                Text.literal("§fAspect Ratio: " + onOff(ErrorsConfig.aspectRatioEnabled)),
                b -> {
                    ErrorsConfig.aspectRatioEnabled = !ErrorsConfig.aspectRatioEnabled;
                    ErrorsConfig.save();
                    this.clearAndInit();
                }
        ).dimensions(bx, by + 102, 340, 26).build(),
                "Изменяет соотношение сторон экрана (растяг).");

        addBtn(ButtonWidget.builder(
                Text.literal("§fПресет: §b" + ErrorsConfig.aspectRatioPreset),
                b -> {
                    String[] arr = {"16:9", "4:3", "21:9", "1:1"};
                    for (int i = 0; i < arr.length; i++) {
                        if (arr[i].equals(ErrorsConfig.aspectRatioPreset)) {
                            ErrorsConfig.aspectRatioPreset = arr[(i + 1) % arr.length];
                            break;
                        }
                    }
                    ErrorsConfig.save();
                    this.clearAndInit();
                }
        ).dimensions(bx, by + 136, 340, 26).build(),
                "Соотношение сторон: 16:9, 4:3, 21:9, 1:1.");
    }

    private void initEsp(int cx, int cy) {
        int bx = cx + 160, by = cy + 55;

        addBtn(ButtonWidget.builder(
                Text.literal("§fTarget ESP: " + onOff(ErrorsConfig.targetEsp)),
                b -> {
                    ErrorsConfig.targetEsp = !ErrorsConfig.targetEsp;
                    ErrorsConfig.save();
                    this.clearAndInit();
                }
        ).dimensions(bx, by, 340, 26).build(),
                "Показывает индикатор вокруг БЛИЖАЙШЕЙ цели (игрок или моб).");

        addBtn(ButtonWidget.builder(
                Text.literal("§fФорма: §b" + (ErrorsConfig.targetEspShape.equals("circle") ? "Круг" : "Квадрат")),
                b -> {
                    ErrorsConfig.targetEspShape = ErrorsConfig.targetEspShape.equals("circle") ? "square" : "circle";
                    ErrorsConfig.save();
                    this.clearAndInit();
                }
        ).dimensions(bx, by + 34, 340, 26).build(),
                "Форма индикатора: вращающийся круг или квадрат.");

        addBtn(ButtonWidget.builder(
                Text.literal("§fЦвет: §b" + colorLabel(ErrorsConfig.targetEspColor)),
                b -> {
                    String[] arr = {"white", "red", "green", "cyan", "gold"};
                    for (int i = 0; i < arr.length; i++) {
                        if (arr[i].equals(ErrorsConfig.targetEspColor)) {
                            ErrorsConfig.targetEspColor = arr[(i + 1) % arr.length];
                            break;
                        }
                    }
                    ErrorsConfig.save();
                    this.clearAndInit();
                }
        ).dimensions(bx, by + 68, 340, 26).build(),
                "Цвет контура.");

        ButtonWidget info = ButtonWidget.builder(
                Text.literal("§8Видна только ближайшая цель (5 блоков, не через стены)."),
                b -> {}
        ).dimensions(bx, by + 110, 340, 22).build();
        info.active = false;
        this.addDrawableChild(info);
    }

    private void initParticles(int cx, int cy) {
        int bx = cx + 160, by = cy + 55;

        addBtn(ButtonWidget.builder(
                Text.literal("§fПартиклы при ударе: " + onOff(ErrorsConfig.particlesEnabled)),
                b -> {
                    ErrorsConfig.particlesEnabled = !ErrorsConfig.particlesEnabled;
                    ErrorsConfig.save();
                    this.clearAndInit();
                }
        ).dimensions(bx, by, 340, 26).build(),
                "Частицы разлетаются от моба/игрока при ударе.");

        addBtn(ButtonWidget.builder(
                Text.literal("§fТип: §b" + ErrorsConfig.particleType),
                b -> {
                    String[] arr = {"flame", "heart", "note", "soul", "end_rod", "crit"};
                    for (int i = 0; i < arr.length; i++) {
                        if (arr[i].equals(ErrorsConfig.particleType)) {
                            ErrorsConfig.particleType = arr[(i + 1) % arr.length];
                            break;
                        }
                    }
                    ErrorsConfig.save();
                    this.clearAndInit();
                }
        ).dimensions(bx, by + 34, 340, 26).build(),
                "Внешний вид частиц.");

        addBtn(ButtonWidget.builder(
                Text.literal("§c−"),
                b -> {
                    if (ErrorsConfig.particleCount > 1) ErrorsConfig.particleCount--;
                    ErrorsConfig.save();
                    this.clearAndInit();
                }
        ).dimensions(bx, by + 68, 40, 26).build(),
                "Уменьшить количество.");

        ButtonWidget countLabel = ButtonWidget.builder(
                Text.literal("§7Количество: §f" + ErrorsConfig.particleCount),
                b -> {}
        ).dimensions(bx + 46, by + 68, 248, 26).build();
        countLabel.active = false;
        this.addDrawableChild(countLabel);

        addBtn(ButtonWidget.builder(
                Text.literal("§a+"),
                b -> {
                    if (ErrorsConfig.particleCount < 100) ErrorsConfig.particleCount++;
                    ErrorsConfig.save();
                    this.clearAndInit();
                }
        ).dimensions(bx + 300, by + 68, 40, 26).build(),
                "Увеличить количество (макс. 100).");
    }

    private void initVisuals(int cx, int cy) {
        int bx = cx + 160, by = cy + 55;

        addBtn(ButtonWidget.builder(
                Text.literal("§fFullBright: " + onOff(ErrorsConfig.fullBright)),
                b -> {
                    ErrorsConfig.fullBright = !ErrorsConfig.fullBright;
                    ErrorsConfig.save();
                    this.clearAndInit();
                }
        ).dimensions(bx, by, 340, 26).build(),
                "Максимальная яркость. Разрешено на HolyWorld.");

        addBtn(ButtonWidget.builder(
                Text.literal("§fШлейф за игроком: " + onOff(ErrorsConfig.trailEnabled)),
                b -> {
                    ErrorsConfig.trailEnabled = !ErrorsConfig.trailEnabled;
                    ErrorsConfig.save();
                    this.clearAndInit();
                }
        ).dimensions(bx, by + 34, 340, 26).build(),
                "Линия из точек тянется за тобой.");

        addBtn(ButtonWidget.builder(
                Text.literal("§fКруг при прыжке: " + onOff(ErrorsConfig.jumpCircle)),
                b -> {
                    ErrorsConfig.jumpCircle = !ErrorsConfig.jumpCircle;
                    ErrorsConfig.save();
                    this.clearAndInit();
                }
        ).dimensions(bx, by + 68, 340, 26).build(),
                "Круг появляется под тобой в прыжке.");

        addBtn(ButtonWidget.builder(
                Text.literal("§fChina Hat: " + onOff(ErrorsConfig.chinaHat)),
                b -> {
                    ErrorsConfig.chinaHat = !ErrorsConfig.chinaHat;
                    ErrorsConfig.save();
                    this.clearAndInit();
                }
        ).dimensions(bx, by + 102, 340, 26).build(),
                "Прозрачная шляпа над головами игроков.");

        addBtn(ButtonWidget.builder(
                Text.literal("§fЦвет шляпы: §b" + ErrorsConfig.chinaHatColor),
                b -> {
                    String[] arr = {"gold", "red", "green", "cyan", "white"};
                    for (int i = 0; i < arr.length; i++) {
                        if (arr[i].equals(ErrorsConfig.chinaHatColor)) {
                            ErrorsConfig.chinaHatColor = arr[(i + 1) % arr.length];
                            break;
                        }
                    }
                    ErrorsConfig.save();
                    this.clearAndInit();
                }
        ).dimensions(bx, by + 136, 340, 26).build(),
                "Цвет шляпы.");
    }

    private void initOther(int cx, int cy) {
        int bx = cx + 160, by = cy + 55;

        ButtonWidget l1 = ButtonWidget.builder(
                Text.literal("§7Мод: §berrorsVisuals §7v1.4"), b -> {}
        ).dimensions(bx, by, 340, 22).build();
        l1.active = false;
        this.addDrawableChild(l1);

        ButtonWidget l2 = ButtonWidget.builder(
                Text.literal("§7Minecraft: §b1.21.11"), b -> {}
        ).dimensions(bx, by + 26, 340, 22).build();
        l2.active = false;
        this.addDrawableChild(l2);

        ButtonWidget l3 = ButtonWidget.builder(
                Text.literal("§7Открытие меню: §bRShift"), b -> {}
        ).dimensions(bx, by + 52, 340, 22).build();
        l3.active = false;
        this.addDrawableChild(l3);

        ButtonWidget l4 = ButtonWidget.builder(
                Text.literal("§8Настройки сохраняются автоматически"),
                b -> {}
        ).dimensions(bx, by + 90, 340, 22).build();
        l4.active = false;
        this.addDrawableChild(l4);
    }

    private String onOff(boolean b) {
        return b ? "§aВКЛ" : "§cВЫКЛ";
    }

    private String colorLabel(String c) {
        return switch (c) {
            case "red" -> "§cКрасный";
            case "green" -> "§aЗелёный";
            case "cyan" -> "§bГолубой";
            case "gold" -> "§6Золотой";
            default -> "§fБелый";
        };
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, width, height, 0xAA000000);

        int w = 520;
        int h = 320;
        int x = (width - w) / 2;
        int y = (height - h) / 2;

        ctx.fill(x - 4, y - 4, x + w + 4, y + h + 4, 0x4400AAFF);
        ctx.fill(x - 2, y - 2, x + w + 2, y + h + 2, 0x88000000);
        ctx.fill(x, y, x + w, y + h, 0xF00D0D14);

        ctx.fill(x, y, x + w, y + 4, 0xFF00AAFF);
        ctx.fill(x, y + h - 2, x + w, y + h, 0x6600AAFF);
        ctx.fill(x + 155, y + 40, x + 156, y + h - 40, 0x33FFFFFF);

        ctx.drawCenteredTextWithShadow(textRenderer, Text.literal("§b§lerrorsVisuals §7v1.4"),
                x + w / 2, y + 15, 0xFFFFFFFF);

        super.render(ctx, mouseX, mouseY, delta);

        for (ButtonWidget btn : hoverList) {
            if (btn.isHovered() && tooltips.containsKey(btn)) {
                drawTooltip(ctx, tooltips.get(btn), mouseX, mouseY);
                break;
            }
        }
    }

    private void drawTooltip(DrawContext ctx, String text, int mouseX, int mouseY) {
        int pad = 6;
        int textW = textRenderer.getWidth(text);
        int boxW = textW + pad * 2;
        int boxH = 8 + pad * 2;

        int tx = mouseX + 14;
        int ty = mouseY + 14;

        if (tx + boxW > width - 5) tx = width - boxW - 5;
        if (ty + boxH > height - 5) ty = height - boxH - 5;

        ctx.fill(tx - 2, ty - 2, tx + boxW + 2, ty + boxH + 2, 0x6600AAFF);
        ctx.fill(tx, ty, tx + boxW, ty + boxH, 0xF00A0A14);
        ctx.fill(tx, ty, tx + boxW, ty + 2, 0xFF00AAFF);

        ctx.drawText(textRenderer, Text.literal("§f" + text),
                tx + pad, ty + pad + 1, 0xFFFFFFFF, true);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}