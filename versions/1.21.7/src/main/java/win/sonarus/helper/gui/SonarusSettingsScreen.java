package win.sonarus.helper.gui;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import win.sonarus.helper.config.SonarusHelperConfig;
import win.sonarus.helper.update.UpdateManager;

public final class SonarusSettingsScreen extends Screen {
    private final Screen parent;
    private long updateRevision = -1L;

    public SonarusSettingsScreen(Screen parent) {
        super(Text.literal("Sonarus Helper"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int center = this.width / 2;
        int left = center - 155;
        int right = center + 5;
        SonarusHelperConfig config = SonarusHelperConfig.get();

        addLabel(left, 18, 310, "Sonarus Helper 1.1");

        addToggle(left, 58, 310, "Проверять обновления", config.checkUpdates, value -> {
            config.checkUpdates = value;
            SonarusHelperConfig.save();
        });

        addToggle(left, 84, 310, "Автоматически скачивать обновления", config.autoDownloadUpdates, value -> {
            config.autoDownloadUpdates = value;
            SonarusHelperConfig.save();
        });

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Проверить сейчас"),
                button -> {
                    UpdateManager.checkAsync(true);
                    this.clearAndInit();
                }
        ).dimensions(left, 116, 150, 20).build());

        if (UpdateManager.updateAvailable()) {
            this.addDrawableChild(ButtonWidget.builder(
                    Text.literal("Скачать обновление"),
                    button -> {
                        UpdateManager.downloadAndScheduleAsync();
                        this.clearAndInit();
                    }
            ).dimensions(right, 116, 150, 20).build());
        } else {
            addLabel(right, 116, 150, "Sonarus Helper 1.1");
        }

        addLabel(left, 148, 310, UpdateManager.statusText());
        addLabel(left, 180, 310, "Настройки открываются командой /shelp");
        addLabel(left, 206, 310, "Установка обновления — после выхода из Minecraft");

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Готово"),
                button -> close()
        ).dimensions(center - 75, this.height - 32, 150, 20).build());

        updateRevision = UpdateManager.revision();
    }

    @Override
    public void tick() {
        super.tick();

        long revision = UpdateManager.revision();
        if (revision != updateRevision) {
            updateRevision = revision;
            this.clearAndInit();
        }
    }

    @Override
    public void close() {
        SonarusHelperConfig.save();
        if (this.client != null) {
            this.client.setScreen(parent);
        }
    }

    private void addToggle(int x, int y, int width, String name, boolean value, ToggleSetter setter) {
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal(name + ": " + (value ? "§aВКЛ" : "§cВЫКЛ")),
                button -> {
                    setter.set(!value);
                    this.clearAndInit();
                }
        ).dimensions(x, y, width, 20).build());
    }

    private void addLabel(int x, int y, int width, String text) {
        ButtonWidget label = ButtonWidget.builder(
                Text.literal(text),
                button -> {
                }
        ).dimensions(x, y, width, 20).build();
        label.active = false;
        this.addDrawableChild(label);
    }

    @FunctionalInterface
    private interface ToggleSetter {
        void set(boolean value);
    }
}
