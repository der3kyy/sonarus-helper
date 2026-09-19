package win.sonarus.helper.gui;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public final class SonarusGradientScreen extends Screen {
    private static final int MIN_COLORS = 2;
    private static final int MAX_COLORS = 5;
    private static final String[] DEFAULT_COLORS = {
            "#00C6FF", "#7A5CFF", "#FF4FD8", "#FFB84D", "#63E6BE"
    };

    private final Screen parent;
    private final String[] colors = DEFAULT_COLORS.clone();
    private final TextFieldWidget[] colorFields = new TextFieldWidget[MAX_COLORS];

    private String textValue = "Sonarus";
    private int colorCount = 2;
    private TextFieldWidget textField;
    private TextFieldWidget outputField;
    private ButtonWidget previewButton;

    public SonarusGradientScreen(Screen parent) {
        super(Text.literal("Градиент текста"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int center = this.width / 2;
        int left = center - 155;

        addLabel(left, 16, 310, "Градиент текста");

        this.textField = new TextFieldWidget(this.textRenderer, left, 46, 310, 20, Text.literal("Текст"));
        this.textField.setMaxLength(128);
        this.textField.setText(textValue);
        this.textField.setChangedListener(value -> {
            textValue = value;
            refreshPreview();
        });
        this.addDrawableChild(this.textField);

        this.previewButton = ButtonWidget.builder(
                gradientText(),
                button -> {
                }
        ).dimensions(left, 76, 310, 24).build();
        this.previewButton.active = false;
        this.addDrawableChild(this.previewButton);

        int gap = 6;
        int fieldWidth = (310 - gap * (MAX_COLORS - 1)) / MAX_COLORS;
        for (int i = 0; i < colorCount; i++) {
            int index = i;
            int x = left + i * (fieldWidth + gap);
            addLabel(x, 108, fieldWidth, "Цвет " + (i + 1));

            TextFieldWidget field = new TextFieldWidget(
                    this.textRenderer, x, 130, fieldWidth, 20, Text.literal("Цвет " + (i + 1))
            );
            field.setMaxLength(7);
            field.setText(colors[i]);
            field.setChangedListener(value -> {
                colors[index] = value;
                refreshPreview();
            });
            colorFields[i] = field;
            this.addDrawableChild(field);
        }

        ButtonWidget remove = ButtonWidget.builder(
                Text.literal("− цвет"),
                button -> {
                    if (colorCount > MIN_COLORS) {
                        colorCount--;
                        this.clearAndInit();
                    }
                }
        ).dimensions(left, 158, 150, 20).build();
        remove.active = colorCount > MIN_COLORS;
        this.addDrawableChild(remove);

        ButtonWidget add = ButtonWidget.builder(
                Text.literal("+ цвет"),
                button -> {
                    if (colorCount < MAX_COLORS) {
                        colorCount++;
                        this.clearAndInit();
                    }
                }
        ).dimensions(left + 160, 158, 150, 20).build();
        add.active = colorCount < MAX_COLORS;
        this.addDrawableChild(add);

        addLabel(left, 188, 310, "MiniMessage");

        this.outputField = new TextFieldWidget(this.textRenderer, left, 210, 310, 20, Text.literal("MiniMessage"));
        this.outputField.setMaxLength(1024);
        this.outputField.setText(miniMessage());
        this.addDrawableChild(this.outputField);

        int bottomY = this.height - 32;

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Копировать"),
                button -> {
                    if (this.client != null && allColorsValid()) {
                        this.client.keyboard.setClipboard(this.outputField.getText());
                    }
                }
        ).dimensions(left, bottomY, 120, 20).build());

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Сбросить"),
                button -> {
                    textValue = "Sonarus";
                    colorCount = 2;
                    System.arraycopy(DEFAULT_COLORS, 0, colors, 0, MAX_COLORS);
                    this.clearAndInit();
                }
        ).dimensions(left + 125, bottomY, 90, 20).build());

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Назад"),
                button -> close()
        ).dimensions(left + 220, bottomY, 90, 20).build());

        refreshPreview();
    }

    @Override
    public void close() {
        if (this.client != null) {
            this.client.setScreen(parent);
        }
    }

    private void refreshPreview() {
        if (previewButton != null) {
            previewButton.setMessage(gradientText());
        }
        if (outputField != null) {
            outputField.setText(miniMessage());
        }
    }

    private MutableText gradientText() {
        String value = textValue.isEmpty() ? "Предпросмотр" : textValue;
        int[] codePoints = value.codePoints().toArray();
        MutableText result = Text.empty();

        for (int i = 0; i < codePoints.length; i++) {
            double position = codePoints.length <= 1 ? 0.0 : (double) i / (codePoints.length - 1);
            int color = colorAt(position);
            String glyph = new String(Character.toChars(codePoints[i]));
            result.append(Text.literal(glyph).styled(style -> style.withColor(color)));
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
        ButtonWidget label = ButtonWidget.builder(
                Text.literal(value),
                button -> {
                }
        ).dimensions(x, y, width, 20).build();
        label.active = false;
        this.addDrawableChild(label);
    }
}
