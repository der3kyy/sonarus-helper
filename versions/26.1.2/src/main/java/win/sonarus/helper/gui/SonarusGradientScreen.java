package win.sonarus.helper.gui;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public final class SonarusGradientScreen extends Screen {
    private static final int MIN_COLORS = 2;
    private static final int MAX_COLORS = 5;
    private static final String[] DEFAULT_COLORS = {
            "#00C6FF", "#7A5CFF", "#FF4FD8", "#FFB84D", "#63E6BE"
    };

    private final Screen parent;
    private final String[] colors = DEFAULT_COLORS.clone();
    private final EditBox[] colorFields = new EditBox[MAX_COLORS];

    private String textValue = "Sonarus";
    private int colorCount = 2;
    private EditBox textField;
    private EditBox outputField;
    private Button previewButton;

    public SonarusGradientScreen(Screen parent) {
        super(Component.literal("Градиент текста"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int center = this.width / 2;
        int left = center - 155;

        addLabel(left, 16, 310, "Градиент текста");

        this.textField = new EditBox(this.font, left, 46, 310, 20, Component.literal("Текст"));
        this.textField.setMaxLength(128);
        this.textField.setValue(textValue);
        this.textField.setResponder(value -> {
            textValue = value;
            refreshPreview();
        });
        this.addRenderableWidget(this.textField);

        this.previewButton = Button.builder(
                gradientText(),
                button -> {
                }
        ).bounds(left, 76, 310, 24).build();
        this.previewButton.active = false;
        this.addRenderableWidget(this.previewButton);

        int gap = 6;
        int fieldWidth = (310 - gap * (MAX_COLORS - 1)) / MAX_COLORS;
        for (int i = 0; i < colorCount; i++) {
            int index = i;
            int x = left + i * (fieldWidth + gap);
            addLabel(x, 108, fieldWidth, "Цвет " + (i + 1));

            EditBox field = new EditBox(
                    this.font, x, 130, fieldWidth, 20, Component.literal("Цвет " + (i + 1))
            );
            field.setMaxLength(7);
            field.setValue(colors[i]);
            field.setResponder(value -> {
                colors[index] = value;
                refreshPreview();
            });
            colorFields[i] = field;
            this.addRenderableWidget(field);
        }

        Button remove = Button.builder(
                Component.literal("− цвет"),
                button -> {
                    if (colorCount > MIN_COLORS) {
                        colorCount--;
                        this.rebuildWidgets();
                    }
                }
        ).bounds(left, 158, 150, 20).build();
        remove.active = colorCount > MIN_COLORS;
        this.addRenderableWidget(remove);

        Button add = Button.builder(
                Component.literal("+ цвет"),
                button -> {
                    if (colorCount < MAX_COLORS) {
                        colorCount++;
                        this.rebuildWidgets();
                    }
                }
        ).bounds(left + 160, 158, 150, 20).build();
        add.active = colorCount < MAX_COLORS;
        this.addRenderableWidget(add);

        addLabel(left, 188, 310, "MiniMessage");

        this.outputField = new EditBox(this.font, left, 210, 310, 20, Component.literal("MiniMessage"));
        this.outputField.setMaxLength(1024);
        this.outputField.setValue(miniMessage());
        this.addRenderableWidget(this.outputField);

        int bottomY = this.height - 32;

        this.addRenderableWidget(Button.builder(
                Component.literal("Копировать"),
                button -> {
                    if (this.minecraft != null && allColorsValid()) {
                        this.minecraft.keyboardHandler.setClipboard(this.outputField.getValue());
                    }
                }
        ).bounds(left, bottomY, 120, 20).build());

        this.addRenderableWidget(Button.builder(
                Component.literal("Сбросить"),
                button -> {
                    textValue = "Sonarus";
                    colorCount = 2;
                    System.arraycopy(DEFAULT_COLORS, 0, colors, 0, MAX_COLORS);
                    this.rebuildWidgets();
                }
        ).bounds(left + 125, bottomY, 90, 20).build());

        this.addRenderableWidget(Button.builder(
                Component.literal("Назад"),
                button -> onClose()
        ).bounds(left + 220, bottomY, 90, 20).build());

        refreshPreview();
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
    }

    private void refreshPreview() {
        if (previewButton != null) {
            previewButton.setMessage(gradientText());
        }
        if (outputField != null) {
            outputField.setValue(miniMessage());
        }
    }

    private MutableComponent gradientText() {
        String value = textValue.isEmpty() ? "Предпросмотр" : textValue;
        int[] codePoints = value.codePoints().toArray();
        MutableComponent result = Component.empty();

        for (int i = 0; i < codePoints.length; i++) {
            double position = codePoints.length <= 1 ? 0.0 : (double) i / (codePoints.length - 1);
            int color = colorAt(position);
            String glyph = new String(Character.toChars(codePoints[i]));
            result.append(Component.literal(glyph).withStyle(style -> style.withColor(color)));
        }
        return result;
    }

    private int colorAt(double position) {
        if (!allColorsValid()) {
            return 0xAAAAAA;
        }

        double scaled = position * (colorCount - 1);
        int segment = Math.min((int) Math.floor(scaled), colorCount - 2);
        double local = scaled - segment;

        int from = parseColor(colors[segment]);
        int to = parseColor(colors[segment + 1]);

        int r = lerp((from >> 16) & 0xFF, (to >> 16) & 0xFF, local);
        int g = lerp((from >> 8) & 0xFF, (to >> 8) & 0xFF, local);
        int b = lerp(from & 0xFF, to & 0xFF, local);
        return (r << 16) | (g << 8) | b;
    }

    private static int lerp(int from, int to, double t) {
        return (int) Math.round(from + (to - from) * t);
    }

    private String miniMessage() {
        if (!allColorsValid()) {
            return "Исправьте HEX-цвета (#RRGGBB)";
        }

        StringBuilder result = new StringBuilder("<gradient");
        for (int i = 0; i < colorCount; i++) {
            result.append(':').append(normalizeColor(colors[i]));
        }
        result.append('>').append(textValue).append("</gradient>");
        return result.toString();
    }

    private boolean allColorsValid() {
        for (int i = 0; i < colorCount; i++) {
            if (!isValidColor(colors[i])) {
                return false;
            }
        }
        return true;
    }

    private static boolean isValidColor(String value) {
        if (value == null) {
            return false;
        }
        String hex = value.startsWith("#") ? value.substring(1) : value;
        return hex.matches("[0-9a-fA-F]{6}");
    }

    private static String normalizeColor(String value) {
        String hex = value.startsWith("#") ? value.substring(1) : value;
        return "#" + hex.toUpperCase();
    }

    private static int parseColor(String value) {
        return Integer.parseInt(normalizeColor(value).substring(1), 16);
    }

    private void addLabel(int x, int y, int width, String value) {
        Button label = Button.builder(
                Component.literal(value),
                button -> {
                }
        ).bounds(x, y, width, 20).build();
        label.active = false;
        this.addRenderableWidget(label);
    }
}
