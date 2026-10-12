package com.example.client.gui;

import com.example.client.config.ErrorsConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ErrorsVisualsScreen extends Screen {

    private static final int BG_PANEL      = 0xA02E2E38;
    private static final int BG_CARD       = 0x50383844;
    private static final int BG_CARD_HOVER = 0x70424550;
    private static final int BG_CARD_ON    = 0x60FFAA55;
    private static final int BG_SUB_PANEL  = 0xC032323C;
    private static final int BG_SUB_HOVER  = 0x40FFFFFF;
    private static final int BG_SIDEBAR_A  = 0x40FFAA55;
    private static final int BG_SIDEBAR_H  = 0x18FFFFFF;
    private static final int BG_SEARCH     = 0x40000000;
    private static final int BG_NICK_EDIT  = 0x60FFAA55;

    private static final int ACCENT        = 0xFFFFAA55;
    private static final int ACCENT_SOFT   = 0x60FFAA55;
    private static final int TEXT_MAIN     = 0xFFE8E2D6;
    private static final int TEXT_DIM      = 0xFFA8A29A;
    private static final int TEXT_ACTIVE   = 0xFFFFFFFF;
    private static final int TOGGLE_OFF    = 0xFF3A3A44;
    private static final int TOGGLE_ON     = 0xFFFFAA55;
    private static final int TOGGLE_KNOB   = 0xFFF0F0F0;
    private static final int DIVIDER       = 0x20FFFFFF;
    private static final int BORDER        = 0x25FFFFFF;

    private int panelX, panelY, panelW, panelH;
    private final int sidebarW = 130;
    private final int topbarH = 34;

    private int cardW, cardH = 24, cardGap = 5, cardPad = 10, cols = 3;

    private int currentTab = 0;
    private final List<Card> cards = new ArrayList<>();
    private final List<Card> filtered = new ArrayList<>();
    private Card expanded = null;
    private String expandedName = null;

    private String search = "";
    private boolean searchFocused = false;

    private boolean nicknameEditing = false;
    private String nicknameBuffer = "";

    private static final String[] TAB_NAMES = {"PvP", "Визуалы", "Питомцы", "Эффекты", "Прочее"};
    private static final String[] TAB_ICONS = {"⚔", "◆", "✧", "❄", "⚙"};

    private static class Card {
        int x, y, w, h;
        String name;
        String tag;
        boolean toggle;
        List<Sub> subs = new ArrayList<>();
        Runnable onToggle;
        float hover = 0f, toggleAnim;

        Card(String name, String tag, boolean toggle, Runnable onToggle) {
            this.name = name; this.tag = tag;
            this.toggle = toggle; this.onToggle = onToggle;
            this.toggleAnim = toggle ? 1f : 0f;
        }

        boolean isHovered(double mx, double my) {
            return mx >= x && mx <= x + w && my >= y && my <= y + h;
        }

        boolean isToggle(double mx, double my) {
            int tw = 20;
            int paddingRight = 14;
            int tx = x + w - tw - paddingRight;
            int ty = y + (h - 10) / 2;
            return mx >= tx - 2 && mx <= tx + tw + 2 && my >= ty - 2 && my <= ty + 12;
        }

        boolean matches(String q) {
            if (q.isEmpty()) return true;
            return name.toLowerCase().contains(q.toLowerCase());
        }
    }

    private static class Sub {
        String name;
        String value;
        Runnable onClick;
        boolean isSlider;
        float min, max;
        Consumer<Float> onSlide;
        Supplier<Float> getValue;

        int x, y, w, h = 22;

        Sub(String name, String value, Runnable onClick) {
            this.name = name; this.value = value;
            this.onClick = onClick;
            this.isSlider = false;
        }

        Sub(String name, Runnable onClick) {
            this.name = name;
            this.value = null;
            this.onClick = onClick;
            this.isSlider = false;
        }

        Sub(String name, float min, float max,
            Supplier<Float> getValue, Consumer<Float> onSlide) {
            this.name = name;
            this.min = min; this.max = max;
            this.getValue = getValue; this.onSlide = onSlide;
            this.isSlider = true;
        }

        boolean isHovered(double mx, double my) {
            return mx >= x && mx <= x + w && my >= y && my <= y + h;
        }
    }

    public ErrorsVisualsScreen() {
        super(Text.literal("errorsVisuals"));
    }

    @Override
    protected void init() {
        cards.clear();
        expanded = null;

        panelW = Math.min(500, width - 40);
        panelH = Math.min(340, height - 40);
        panelX = (width - panelW) / 2;
        panelY = (height - panelH) / 2;

        int availW = panelW - sidebarW - cardPad * 2;
        cardW = Math.max(80, (availW - cardGap * (cols - 1)) / cols);

        switch (currentTab) {
            case 0 -> fillPvp();
            case 1 -> fillVisuals();
            case 2 -> fillPets();
            case 3 -> fillEffects();
            case 4 -> fillOther();
        }
        applyFilter();
        layout();

        if (expandedName != null) {
            for (Card c : filtered) {
                if (c.name.equals(expandedName)) {
                    expanded = c;
                    layoutSubs();
                    break;
                }
            }
        }
    }

    private void add(String name, String tag, boolean toggle, Runnable action, Sub... subs) {
        Card c = new Card(name, tag, toggle, action);
        for (Sub s : subs) c.subs.add(s);
        cards.add(c);
    }

    private void fillPvp() {
        add("No Fire", null, ErrorsConfig.noFire,
                () -> { ErrorsConfig.noFire = !ErrorsConfig.noFire; ErrorsConfig.save(); refresh(); });
        add("No Pumpkin", null, ErrorsConfig.noPumpkin,
                () -> { ErrorsConfig.noPumpkin = !ErrorsConfig.noPumpkin; ErrorsConfig.save(); refresh(); });
        add("Item Scroller", "Shift", ErrorsConfig.itemScroller,
                () -> { ErrorsConfig.itemScroller = !ErrorsConfig.itemScroller; ErrorsConfig.save(); refresh(); });

        List<Sub> ar = new ArrayList<>();
        ar.add(new Sub("Пресет: " + ErrorsConfig.aspectRatioPreset, () -> {
            String[] a = {"16:9", "4:3", "21:9", "1:1"};
            int i = -1;
            for (int k = 0; k < a.length; k++) if (a[k].equals(ErrorsConfig.aspectRatioPreset)) { i = k; break; }
            ErrorsConfig.aspectRatioPreset = a[(i + 1) % a.length];
            ErrorsConfig.save(); refreshKeepExpanded();
        }));
        ar.add(new Sub("Значение", 0.5f, 3.0f,
                () -> ErrorsConfig.aspectRatioCustom,
                v -> { ErrorsConfig.aspectRatioCustom = v; ErrorsConfig.save(); }));
        add("Aspect Ratio", null, ErrorsConfig.aspectRatioEnabled,
                () -> { ErrorsConfig.aspectRatioEnabled = !ErrorsConfig.aspectRatioEnabled; ErrorsConfig.save(); refresh(); },
                ar.toArray(new Sub[0]));
    }

    private void fillVisuals() {
        add("Target ESP", null, ErrorsConfig.targetEsp,
                () -> { ErrorsConfig.targetEsp = !ErrorsConfig.targetEsp; ErrorsConfig.save(); refresh(); },
                new Sub("Стиль: " + styleLabel(ErrorsConfig.targetEspStyle), () -> {
                    String[] a = {"square", "ghost", "rhombus", "corner"};
                    ErrorsConfig.targetEspStyle = cycle(a, ErrorsConfig.targetEspStyle);
                    ErrorsConfig.save(); refreshKeepExpanded();
                }),
                new Sub("Цвет: " + ErrorsConfig.targetEspColor, () -> {
                    String[] a = {"white", "red", "green", "cyan", "gold", "blue"};
                    ErrorsConfig.targetEspColor = cycle(a, ErrorsConfig.targetEspColor);
                    ErrorsConfig.save(); refreshKeepExpanded();
                }),
                new Sub("Прозрачность", 0.1f, 1.0f,
                        () -> ErrorsConfig.targetEspOpacity,
                        v -> { ErrorsConfig.targetEspOpacity = v; ErrorsConfig.save(); }),
                new Sub("Кубы: " + ErrorsConfig.targetEspCubeCount, 1f, 12f,
                        () -> (float) ErrorsConfig.targetEspCubeCount,
                        v -> { ErrorsConfig.targetEspCubeCount = Math.round(v); ErrorsConfig.save(); }),
                new Sub("Ромбы: " + ErrorsConfig.targetEspDiamondCount, 1f, 12f,
                        () -> (float) ErrorsConfig.targetEspDiamondCount,
                        v -> { ErrorsConfig.targetEspDiamondCount = Math.round(v); ErrorsConfig.save(); }),
                new Sub("Призраки: " + ErrorsConfig.targetEspGhostCount, 1f, 12f,
                        () -> (float) ErrorsConfig.targetEspGhostCount,
                        v -> { ErrorsConfig.targetEspGhostCount = Math.round(v); ErrorsConfig.save(); }),
                new Sub("Цвет по HP: " + (ErrorsConfig.targetEspHealthColor ? "ВКЛ" : "ВЫКЛ"), () -> {
                    ErrorsConfig.targetEspHealthColor = !ErrorsConfig.targetEspHealthColor;
                    ErrorsConfig.save(); refreshKeepExpanded();
                }));

        List<Sub> par = new ArrayList<>();
        par.add(new Sub("Тип: " + ErrorsConfig.particleType, () -> {
            String[] a = {"flame", "heart", "note", "soul", "end_rod", "crit"};
            ErrorsConfig.particleType = cycle(a, ErrorsConfig.particleType);
            ErrorsConfig.save(); refreshKeepExpanded();
        }));
        par.add(new Sub("Кол-во", 1f, 100f,
                () -> (float) ErrorsConfig.particleCount,
                v -> { ErrorsConfig.particleCount = Math.round(v); ErrorsConfig.save(); }));
        add("Партиклы", null, ErrorsConfig.particlesEnabled,
                () -> { ErrorsConfig.particlesEnabled = !ErrorsConfig.particlesEnabled; ErrorsConfig.save(); refresh(); },
                par.toArray(new Sub[0]));

        List<Sub> hat = new ArrayList<>();
        hat.add(new Sub("Цвет: " + ErrorsConfig.chinaHatColor, () -> {
            String[] a = {"gold", "red", "green", "cyan", "white", "blue"};
            ErrorsConfig.chinaHatColor = cycle(a, ErrorsConfig.chinaHatColor);
            ErrorsConfig.save(); refreshKeepExpanded();
        }));
        hat.add(new Sub("Размер", 0.4f, 3.0f,
                () -> ErrorsConfig.chinaHatSize,
                v -> { ErrorsConfig.chinaHatSize = v; ErrorsConfig.save(); }));
        hat.add(new Sub("Прозрачность", 0.1f, 1.0f,
                () -> ErrorsConfig.chinaHatAlpha,
                v -> { ErrorsConfig.chinaHatAlpha = v; ErrorsConfig.save(); }));
        add("China Hat", null, ErrorsConfig.chinaHat,
                () -> { ErrorsConfig.chinaHat = !ErrorsConfig.chinaHat; ErrorsConfig.save(); refresh(); },
                hat.toArray(new Sub[0]));

        add("FullBright", null, ErrorsConfig.fullBright,
                () -> { ErrorsConfig.fullBright = !ErrorsConfig.fullBright; ErrorsConfig.save(); refresh(); });
        add("Шлейф", null, ErrorsConfig.trailEnabled,
                () -> { ErrorsConfig.trailEnabled = !ErrorsConfig.trailEnabled; ErrorsConfig.save(); refresh(); });
        add("Круг", null, ErrorsConfig.jumpCircle,
                () -> { ErrorsConfig.jumpCircle = !ErrorsConfig.jumpCircle; ErrorsConfig.save(); refresh(); });
        add("Zoom", "C", ErrorsConfig.zoomEnabled,
                () -> { ErrorsConfig.zoomEnabled = !ErrorsConfig.zoomEnabled; ErrorsConfig.save(); refresh(); },
                new Sub("Уровень", 1.5f, 20f,
                        () -> ErrorsConfig.zoomLevel,
                        v -> { ErrorsConfig.zoomLevel = v; ErrorsConfig.save(); }));
    }

    private String styleLabel(String s) {
        return switch (s) {
            case "ghost" -> "Призрак";
            case "rhombus" -> "2D Ромбы";
            case "corner" -> "Скобки";
            default -> "3D Кубы";
        };
    }

    private void fillPets() {
        List<Sub> subs = new ArrayList<>();
        subs.add(new Sub("Тип: " + ErrorsConfig.petsType, () -> {
            String[] a = {"heart", "flame", "note", "soul", "end_rod", "crit"};
            ErrorsConfig.petsType = cycle(a, ErrorsConfig.petsType);
            ErrorsConfig.save(); refreshKeepExpanded();
        }));
        subs.add(new Sub("Количество", 1f, 16f,
                () -> (float) ErrorsConfig.petsCount,
                v -> { ErrorsConfig.petsCount = Math.round(v); ErrorsConfig.save(); }));
        subs.add(new Sub("Радиус", 0.5f, 4.0f,
                () -> ErrorsConfig.petsRadius,
                v -> { ErrorsConfig.petsRadius = v; ErrorsConfig.save(); }));
        subs.add(new Sub("Скорость", 10f, 300f,
                () -> ErrorsConfig.petsSpeed,
                v -> { ErrorsConfig.petsSpeed = v; ErrorsConfig.save(); }));

        add("Питомцы", null, ErrorsConfig.petsEnabled,
                () -> { ErrorsConfig.petsEnabled = !ErrorsConfig.petsEnabled; ErrorsConfig.save(); refresh(); },
                subs.toArray(new Sub[0]));
    }

    private void fillEffects() {
        add("Зимний режим", null, ErrorsConfig.winterEnabled,
                () -> { ErrorsConfig.winterEnabled = !ErrorsConfig.winterEnabled; ErrorsConfig.save(); refresh(); },
                new Sub("Плотность", 20f, 200f,
                        () -> (float) ErrorsConfig.winterDensity,
                        v -> { ErrorsConfig.winterDensity = Math.round(v); ErrorsConfig.save(); }),
                new Sub("Скорость", 0.3f, 3.0f,
                        () -> ErrorsConfig.winterSpeed,
                        v -> { ErrorsConfig.winterSpeed = v; ErrorsConfig.save(); }),
                new Sub("Туман", 0f, 1f,
                        () -> ErrorsConfig.winterFog,
                        v -> { ErrorsConfig.winterFog = v; ErrorsConfig.save(); }));
    }

    private void fillOther() {
        add("errorsVisuals", "v4.1", false, () -> {});
        add("Minecraft", "1.21.11", false, () -> {});
        add("Меню", "RShift", false, () -> {});
    }

    private void applyFilter() {
        filtered.clear();
        for (Card c : cards) if (c.matches(search)) filtered.add(c);
    }

    private void layout() {
        int sx = panelX + sidebarW + cardPad;
        int sy = panelY + topbarH + 28;
        for (int i = 0; i < filtered.size(); i++) {
            Card c = filtered.get(i);
            c.w = cardW; c.h = cardH;
            c.x = sx + (i % cols) * (cardW + cardGap);
            c.y = sy + (i / cols) * (cardH + cardGap);
        }
    }

    private void refreshKeepExpanded() {
        if (expanded != null) expandedName = expanded.name;
        refresh();
    }

    private void refresh() { this.clearAndInit(); }

    private String cycle(String[] a, String cur) {
        for (int i = 0; i < a.length; i++) if (a[i].equals(cur)) return a[(i + 1) % a.length];
        return a[0];
    }

    private String truncate(String text, int maxWidth) {
        if (textRenderer.getWidth(text) <= maxWidth) return text;
        String cut = text;
        while (cut.length() > 1 && textRenderer.getWidth(cut + "…") > maxWidth) {
            cut = cut.substring(0, cut.length() - 1);
        }
        return cut + "…";
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        double mx = click.x(), my = click.y();
        int btn = click.button();

        // ===== Профиль (ник внизу слева) =====
        int profX1 = panelX + 8;
        int profX2 = panelX + sidebarW - 8;
        int profY1 = panelY + panelH - 40;
        int profY2 = panelY + panelH - 4;
        boolean clickedProfile = mx >= profX1 && mx <= profX2 && my >= profY1 && my <= profY2;

        if (clickedProfile) {
            nicknameEditing = true;
            nicknameBuffer = ErrorsConfig.customNickname;
            searchFocused = false;
            return true;
        }

        if (nicknameEditing) {
            ErrorsConfig.customNickname = nicknameBuffer;
            ErrorsConfig.save();
            nicknameEditing = false;
        }

        for (int i = 0; i < TAB_NAMES.length; i++) {
            int sy = panelY + topbarH + 20 + i * 26;
            if (mx >= panelX + 10 && mx <= panelX + sidebarW - 8 && my >= sy && my <= sy + 22) {
                currentTab = i;
                search = ""; searchFocused = false; expandedName = null;
                this.clearAndInit();
                return true;
            }
        }

        int sX = panelX + sidebarW + 80;
        int sY = panelY + 8;
        int sW = Math.max(80, panelW - sidebarW - 100);
        int sH = 18;
        if (mx >= sX && mx <= sX + sW && my >= sY && my <= sY + sH) {
            searchFocused = true;
            return true;
        } else searchFocused = false;

        if (expanded != null) {
            layoutSubs();
            for (Sub s : expanded.subs) {
                if (s.isHovered(mx, my)) {
                    if (s.isSlider) {
                        float t = (float) ((mx - (s.x + 8)) / (s.w - 16));
                        t = Math.max(0f, Math.min(1f, t));
                        float val = s.min + (s.max - s.min) * t;
                        s.onSlide.accept(val);
                    } else {
                        if (s.onClick != null) s.onClick.run();
                    }
                    return true;
                }
            }
        }

        for (Card c : filtered) {
            if (c.isHovered(mx, my)) {
                if (c.isToggle(mx, my)) {
                    if (c.onToggle != null) c.onToggle.run();
                    return true;
                }
                if (btn == 0) {
                    if (!c.subs.isEmpty()) {
                        if (expanded == c) {
                            expanded = null;
                            expandedName = null;
                        } else {
                            expanded = c;
                            expandedName = c.name;
                            layoutSubs();
                        }
                        return true;
                    }
                    if (c.onToggle != null) c.onToggle.run();
                    return true;
                }
            }
        }
        return super.mouseClicked(click, doubled);
    }

    private void layoutSubs() {
        if (expanded == null) return;
        int y = expanded.y + expanded.h + 4;
        for (Sub s : expanded.subs) {
            s.x = expanded.x;
            s.y = y;
            s.w = expanded.w;
            y += s.h + 2;
        }
    }

    @Override
    public boolean charTyped(CharInput input) {
        int cp = input.codepoint();
        char chr = (cp > 0 && cp < 65536) ? (char) cp : 0;

        if (nicknameEditing) {
            if (chr != 0 && (Character.isLetterOrDigit(chr)
                    || chr == '_' || chr == '-' || chr == ' ')
                    && nicknameBuffer.length() < 16) {
                nicknameBuffer += chr;
            }
            return true;
        }

        if (searchFocused && chr != 0
                && (Character.isLetterOrDigit(chr) || chr == ' ' || chr == '_' || chr == '-')) {
            search += chr; applyFilter(); layout();
            return true;
        }
        return super.charTyped(input);
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        int key = input.key();

        if (nicknameEditing) {
            if (key == 259) { // Backspace
                if (!nicknameBuffer.isEmpty()) {
                    nicknameBuffer = nicknameBuffer.substring(0, nicknameBuffer.length() - 1);
                }
                return true;
            }
            if (key == 257 || key == 335) { // Enter
                ErrorsConfig.customNickname = nicknameBuffer;
                ErrorsConfig.save();
                nicknameEditing = false;
                return true;
            }
            if (key == 256) { // Escape — отмена
                nicknameEditing = false;
                return true;
            }
            return true;
        }

        if (searchFocused) {
            if (key == 259) {
                if (!search.isEmpty()) {
                    search = search.substring(0, search.length() - 1);
                    applyFilter(); layout();
                }
                return true;
            }
            if (key == 256) { searchFocused = false; return true; }
        }
        return super.keyPressed(input);
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, width, height, 0x40000000);

        ctx.fill(panelX, panelY, panelX + panelW, panelY + panelH, BG_PANEL);
        ctx.fill(panelX, panelY, panelX + panelW, panelY + 1, BORDER);
        ctx.fill(panelX, panelY + panelH - 1, panelX + panelW, panelY + panelH, BORDER);
        ctx.fill(panelX, panelY, panelX + 1, panelY + panelH, BORDER);
        ctx.fill(panelX + panelW - 1, panelY, panelX + panelW, panelY + panelH, BORDER);

        int divX = panelX + sidebarW;
        ctx.fill(divX, panelY + 8, divX + 1, panelY + panelH - 8, DIVIDER);

        ctx.drawText(textRenderer, Text.literal("§6✦ §r§f§lerrorsVisuals"),
                panelX + 14, panelY + 12, TEXT_ACTIVE, false);

        for (int i = 0; i < TAB_NAMES.length; i++) {
            int sy = panelY + topbarH + 20 + i * 26;
            boolean hover = mouseX >= panelX + 8 && mouseX <= divX - 6 && mouseY >= sy && mouseY <= sy + 22;
            boolean active = i == currentTab;
            if (active) {
                drawRounded(ctx, panelX + 8, sy, sidebarW - 14, 22, 5, BG_SIDEBAR_A);
                drawRounded(ctx, panelX + 8, sy + 5, 2, 12, 1, ACCENT);
            } else if (hover) {
                drawRounded(ctx, panelX + 8, sy, sidebarW - 14, 22, 5, BG_SIDEBAR_H);
            }
            String prefix = active ? "§6" : (hover ? "§f" : "§8");
            ctx.drawText(textRenderer, Text.literal(prefix + TAB_ICONS[i] + "  §r" +
                    (active ? "§f" : "§7") + TAB_NAMES[i]),
                    panelX + 16, sy + 7, TEXT_MAIN, false);
        }

        // ===== ПРОФИЛЬ / НИК =====
        int uY = panelY + panelH - 36;
        ctx.fill(panelX + 10, uY - 1, divX - 8, uY, DIVIDER);

        if (nicknameEditing) {
            drawRounded(ctx, panelX + 10, uY + 2, sidebarW - 22, 30, 4, BG_NICK_EDIT);
        }

        drawCircle(ctx, panelX + 20, uY + 16, 7, ACCENT);

        String displayName;
        if (nicknameEditing) {
            displayName = nicknameBuffer + "§7|";
        } else if (!ErrorsConfig.customNickname.isEmpty()) {
            displayName = ErrorsConfig.customNickname;
        } else {
            try { displayName = MinecraftClient.getInstance().getSession().getUsername(); }
            catch (Exception ignored) { displayName = "Player"; }
        }
        ctx.drawText(textRenderer, Text.literal("§f" + truncate(displayName, sidebarW - 46)),
                panelX + 32, uY + 8, TEXT_MAIN, false);
        ctx.drawText(textRenderer,
                Text.literal(nicknameEditing ? "§7Enter — сохранить" : "§8клик — сменить ник"),
                panelX + 32, uY + 20, TEXT_DIM, false);

        int tX = panelX + sidebarW;
        ctx.drawText(textRenderer, Text.literal("§7◆ §f§l" + TAB_NAMES[currentTab]),
                tX + 12, panelY + 12, TEXT_ACTIVE, false);

        int fps = 0;
        try { fps = MinecraftClient.getInstance().getCurrentFps(); } catch (Exception ignored) {}
        String fpsT = fps + " fps";
        ctx.drawText(textRenderer, Text.literal("§a" + fpsT),
                panelX + panelW - 12 - textRenderer.getWidth(fpsT), panelY + 12, TEXT_ACTIVE, false);

        int sX = tX + 80;
        int sY = panelY + 8;
        int sW = Math.max(80, panelW - sidebarW - 100);
        int sH = 18;

        ctx.fill(sX, sY, sX + sW, sY + sH, BG_SEARCH);
        int borderColor = searchFocused ? ACCENT : BORDER;
        ctx.fill(sX, sY, sX + sW, sY + 1, borderColor);
        ctx.fill(sX, sY + sH - 1, sX + sW, sY + sH, borderColor);
        ctx.fill(sX, sY, sX + 1, sY + sH, borderColor);
        ctx.fill(sX + sW - 1, sY, sX + sW, sY + sH, borderColor);

        ctx.drawText(textRenderer, Text.literal("§8⌕"), sX + 5, sY + 5, TEXT_DIM, false);

        String display = search.isEmpty()
                ? (searchFocused ? "§7|" : "§8поиск...")
                : "§f" + truncate(search, sW - 26) + (searchFocused ? "§7|" : "");
        ctx.drawText(textRenderer, Text.literal(display), sX + 18, sY + 5, TEXT_MAIN, false);

        ctx.fill(tX, panelY + topbarH + 2, panelX + panelW - 8, panelY + topbarH + 3, DIVIDER);

        for (Card c : filtered) drawCard(ctx, c, mouseX, mouseY);

        if (expanded != null && !expanded.subs.isEmpty()) {
            layoutSubs();

            int pX = expanded.x - 4;
            int pY = expanded.y + expanded.h + 1;
            int pW = expanded.w + 8;
            int pH = expanded.subs.size() * 24 + 8;

            ctx.fill(pX, pY, pX + pW, pY + pH, BG_SUB_PANEL);
            ctx.fill(pX, pY, pX + pW, pY + 1, BORDER);
            ctx.fill(pX, pY + pH - 1, pX + pW, pY + pH, BORDER);
            ctx.fill(pX, pY, pX + 1, pY + pH, BORDER);
            ctx.fill(pX + pW - 1, pY, pX + pW, pY + pH, BORDER);

            for (Sub s : expanded.subs) {
                boolean hov = s.isHovered(mouseX, mouseY);
                if (hov && !s.isSlider) {
                    ctx.fill(s.x + 2, s.y + 1, s.x + s.w - 2, s.y + s.h - 1, BG_SUB_HOVER);
                }

                if (s.isSlider) {
                    String nm = truncate(s.name, s.w - 60);
                    ctx.drawText(textRenderer, Text.literal("§7" + nm),
                            s.x + 8, s.y + 2, TEXT_DIM, false);

                    float val = s.getValue.get();
                    String valText = String.format("%.2f", val);
                    int vW = textRenderer.getWidth(valText);
                    ctx.drawText(textRenderer, Text.literal("§e" + valText),
                            s.x + s.w - vW - 8, s.y + 2, TEXT_MAIN, false);

                    int barX = s.x + 8;
                    int barY = s.y + 15;
                    int barW = s.w - 16;
                    int barH = 3;

                    ctx.fill(barX, barY, barX + barW, barY + barH, 0xFF202028);

                    float t = (val - s.min) / (s.max - s.min);
                    t = Math.max(0f, Math.min(1f, t));
                    int fillW = (int) (barW * t);
                    if (fillW > 0) {
                        ctx.fill(barX, barY, barX + fillW, barY + barH, ACCENT);
                    }

                    int knobX = barX + fillW;
                    int knobY = barY + barH / 2;
                    drawCircle(ctx, knobX, knobY, 4, 0xFFFFFFFF);
                    drawCircle(ctx, knobX, knobY, 3, ACCENT);
                } else {
                    String text;
                    int maxTextW = s.w - 16;
                    if (s.value != null) {
                        int valW = textRenderer.getWidth(s.value);
                        maxTextW = s.w - valW - 20;
                    }
                    text = truncate(s.name, maxTextW);

                    ctx.drawText(textRenderer, Text.literal("§7" + text),
                            s.x + 8, s.y + 7, TEXT_DIM, false);

                    if (s.value != null) {
                        int vW = textRenderer.getWidth(s.value);
                        ctx.drawText(textRenderer, Text.literal("§e" + s.value),
                                s.x + s.w - vW - 8, s.y + 7, TEXT_MAIN, false);
                    }
                }
            }
        }
    }

    private void drawCard(DrawContext ctx, Card c, int mx, int my) {
        boolean hover = c.isHovered(mx, my);
        float t = hover ? 1f : 0f;
        c.hover += (t - c.hover) * 0.25f;
        float tt = c.toggle ? 1f : 0f;
        c.toggleAnim += (tt - c.toggleAnim) * 0.25f;

        int bg = lerp(BG_CARD, BG_CARD_HOVER, c.hover);
        if (c.toggle) bg = lerp(bg, BG_CARD_ON, 0.5f);

        drawRounded(ctx, c.x, c.y, c.w, c.h, 6, bg);
        drawRoundedOutline(ctx, c.x, c.y, c.w, c.h, 6,
                expanded == c ? ACCENT_SOFT : (hover ? BORDER : 0x15FFFFFF));

        int tw = 20, th = 10;
        int paddingRight = 14;
        int tx = c.x + c.w - tw - paddingRight;
        int ty = c.y + (c.h - th) / 2;

        int maxNameW = c.w - (paddingRight + tw + 6) - 10;
        String nm = truncate(c.name, maxNameW);
        ctx.drawText(textRenderer, Text.literal("§f" + nm),
                c.x + 10, c.y + (c.h - 8) / 2, TEXT_MAIN, false);

        if (!c.subs.isEmpty()) {
            ctx.drawText(textRenderer, Text.literal(expanded == c ? "§6▾" : "§8▸"),
                    tx - 10, c.y + (c.h - 8) / 2, TEXT_DIM, false);
        }

        drawRounded(ctx, tx, ty, tw, th, th / 2, lerp(TOGGLE_OFF, TOGGLE_ON, c.toggleAnim));
        drawRoundedOutline(ctx, tx, ty, tw, th, th / 2, 0x30000000);

        int kr = 3;
        int kx = (int) (tx + 2 + kr + (tw - 4 - kr * 2) * c.toggleAnim);
        int ky = ty + th / 2;
        drawCircle(ctx, kx, ky, kr, TOGGLE_KNOB);
    }

    private void drawCircle(DrawContext ctx, int cx, int cy, int r, int color) {
        for (int dy = -r; dy <= r; dy++) {
            int dx = (int) Math.sqrt(r * r - dy * dy);
            ctx.fill(cx - dx, cy + dy, cx + dx + 1, cy + dy + 1, color);
        }
    }

    private void drawRounded(DrawContext ctx, int x, int y, int w, int h, int r, int color) {
        if (w <= 0 || h <= 0 || color == 0) return;
        if (r * 2 > h) r = h / 2;
        if (r * 2 > w) r = w / 2;
        if (r <= 0) { ctx.fill(x, y, x + w, y + h, color); return; }
        ctx.fill(x + r, y, x + w - r, y + h, color);
        for (int dy = 0; dy < r; dy++) {
            int dx = (int) Math.round(r - Math.sqrt(r * r - (r - dy) * (r - dy)));
            ctx.fill(x + dx, y + dy, x + w - dx, y + dy + 1, color);
            ctx.fill(x + dx, y + h - 1 - dy, x + w - dx, y + h - dy, color);
        }
    }

    private void drawRoundedOutline(DrawContext ctx, int x, int y, int w, int h, int r, int color) {
        if (w <= 0 || h <= 0 || color == 0) return;
        if (r * 2 > h) r = h / 2;
        if (r * 2 > w) r = w / 2;

        ctx.fill(x + r, y, x + w - r, y + 1, color);
        ctx.fill(x + r, y + h - 1, x + w - r, y + h, color);
        ctx.fill(x, y + r, x + 1, y + h - r, color);
        ctx.fill(x + w - 1, y + r, x + w, y + h - r, color);

        for (int dy = 0; dy < r; dy++) {
            int dx = (int) Math.round(r - Math.sqrt(r * r - (r - dy) * (r - dy)));
            ctx.fill(x + dx, y + dy, x + dx + 1, y + dy + 1, color);
            ctx.fill(x + w - 1 - dx, y + dy, x + w - dx, y + dy + 1, color);
            ctx.fill(x + dx, y + h - 1 - dy, x + dx + 1, y + h - dy, color);
            ctx.fill(x + w - 1 - dx, y + h - 1 - dy, x + w - dx, y + h - dy, color);
        }
    }

    private static int lerp(int a, int b, float t) {
        if (t < 0f) t = 0f;
        if (t > 1f) t = 1f;
        int aa = (a >> 24) & 0xFF, ar = (a >> 16) & 0xFF, ag = (a >> 8) & 0xFF, ab = a & 0xFF;
        int ba = (b >> 24) & 0xFF, br = (b >> 16) & 0xFF, bg = (b >> 8) & 0xFF, bb = b & 0xFF;
        return (((int) (aa + (ba - aa) * t)) << 24) |
                (((int) (ar + (br - ar) * t)) << 16) |
                (((int) (ag + (bg - ag) * t)) << 8) |
                ((int) (ab + (bb - ab) * t));
    }

    @Override
    public boolean shouldPause() { return false; }
}