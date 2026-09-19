package win.sonarus.helper.gui;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import win.sonarus.helper.SonarusHelperClient;
import win.sonarus.helper.config.SonarusHelperConfig;
import win.sonarus.helper.security.PasswordVault;
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

        addLabel(left, 12, 310, "Sonarus Helper 1.1");
        addToggle(left, 36, 310, "Проверять обновления", config.checkUpdates, value -> {
            config.checkUpdates = value;
            SonarusHelperConfig.save();
        });
        addToggle(left, 60, 310, "Автоматически скачивать обновления", config.autoDownloadUpdates, value -> {
            config.autoDownloadUpdates = value;
            SonarusHelperConfig.save();
        });

        this.addRenderableWidget(Button.builder(
                Component.literal("Проверить сейчас"),
                button -> {
                    UpdateManager.checkAsync(true);
                    this.rebuildWidgets();
                }
        ).bounds(left, 84, 150, 20).build());

        if (UpdateManager.updateAvailable()) {
            this.addRenderableWidget(Button.builder(
                    Component.literal("Скачать обновление"),
                    button -> {
                        UpdateManager.downloadAndScheduleAsync();
                        this.rebuildWidgets();
                    }
            ).bounds(right, 84, 150, 20).build());
        } else {
            addLabel(right, 84, 150, "Sonarus Helper 1.1");
        }

        addLabel(left, 108, 310, UpdateManager.statusText());
        addToggle(left, 132, 310, "Запоминать пароль при входе", config.rememberPassword, value -> {
            config.rememberPassword = value;
            if (!value) {
                config.autoFillPassword = false;
            }
            SonarusHelperConfig.save();
        });
        addToggle(left, 156, 310, "Подставлять сохранённый пароль", config.autoFillPassword, value -> {
            config.autoFillPassword = value;
            if (value) {
                config.rememberPassword = true;
            }
            SonarusHelperConfig.save();
        });

        this.addRenderableWidget(Button.builder(
                Component.literal("Удалить сохранённый пароль"),
                button -> {
                    if (this.minecraft != null) { PasswordVault.deleteAsync(this.minecraft.getUser().getName()); }
                }
        ).bounds(left, 180, 310, 20).build());

        // Gradient screen and source remain compiled, but are hidden until explicitly enabled.
        if (SonarusHelperClient.isGradientEnabled()) {
            this.addRenderableWidget(Button.builder(
                    Component.literal("Подобрать градиент"),
                    button -> this.minecraft.gui.setScreen(new SonarusGradientScreen(this))
            ).bounds(left, 204, 310, 20).build());
        }

        this.addRenderableWidget(Button.builder(
                Component.literal("Готово"),
                button -> onClose()
        ).bounds(center - 75, this.height - 28, 150, 20).build());

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

            this.minecraft.gui.setScreen(parent);

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
                button -> {}
        ).bounds(x, y, width, 20).build();
        label.active = false;
        this.addRenderableWidget(label);
    }

    @FunctionalInterface
    private interface ToggleSetter {
        void set(boolean value);
    }
}
