package win.sonarus.helper.gui;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import win.sonarus.helper.config.SonarusHelperConfig;
import win.sonarus.helper.update.UpdateManager;

public final class SonarusSettingsScreen extends Screen {
    private final Screen parent;
    private long updateRevision = -1L;

    public SonarusSettingsScreen(Screen parent) {
        super(Component.literal("Sonarus Helper"));
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

        this.addRenderableWidget(Button.builder(
                Component.literal("Проверить сейчас"),
                button -> {
                    UpdateManager.checkAsync(true);
                    this.rebuildWidgets();
                }
        ).bounds(left, 116, 150, 20).build());

        if (UpdateManager.updateAvailable()) {
            this.addRenderableWidget(Button.builder(
                    Component.literal("Скачать обновление"),
                    button -> {
                        UpdateManager.downloadAndScheduleAsync();
                        this.rebuildWidgets();
                    }
            ).bounds(right, 116, 150, 20).build());
        } else {
            addLabel(right, 116, 150, "Sonarus Helper 1.1");
        }

        addLabel(left, 148, 310, UpdateManager.statusText());
        addLabel(left, 180, 310, "Настройки открываются командой /shelp");
        addLabel(left, 206, 310, "Установка обновления — после выхода из Minecraft");

        this.addRenderableWidget(Button.builder(
                Component.literal("Готово"),
                button -> onClose()
        ).bounds(center - 75, this.height - 32, 150, 20).build());

        updateRevision = UpdateManager.revision();
    }

    @Override
    public void tick() {
        super.tick();

        long revision = UpdateManager.revision();
        if (revision != updateRevision) {
            updateRevision = revision;
            this.rebuildWidgets();
        }
    }

    @Override
    public void onClose() {
        SonarusHelperConfig.save();
        this.minecraft.setScreen(parent);
    }

    private void addToggle(int x, int y, int width, String name, boolean value, ToggleSetter setter) {
        this.addRenderableWidget(Button.builder(
                Component.literal(name + ": " + (value ? "§aВКЛ" : "§cВЫКЛ")),
                button -> {
                    setter.set(!value);
                    this.rebuildWidgets();
                }
        ).bounds(x, y, width, 20).build());
    }

    private void addLabel(int x, int y, int width, String text) {
        Button label = Button.builder(
                Component.literal(text),
                button -> {
                }
        ).bounds(x, y, width, 20).build();
        label.active = false;
        this.addRenderableWidget(label);
    }

    @FunctionalInterface
    private interface ToggleSetter {
        void set(boolean value);
    }
}
