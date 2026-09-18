package win.sonarus.helper.gui;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import win.sonarus.helper.config.SonarusHelperConfig;
import win.sonarus.helper.notifications.WindowsNotifier;
import win.sonarus.helper.update.UpdateManager;

public final class SonarusSettingsScreen extends Screen {
    private final Screen parent;
    private Page page = Page.NOTIFICATIONS;
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

        addLabel(left, 18, 310, "Sonarus Helper 1.1");

        this.addRenderableWidget(Button.builder(
                Component.literal(page == Page.NOTIFICATIONS ? "§aУведомления" : "Уведомления"),
                button -> {
                    page = Page.NOTIFICATIONS;
                    this.rebuildWidgets();
                }
        ).bounds(left, 48, 150, 20).build());

        this.addRenderableWidget(Button.builder(
                Component.literal(page == Page.UPDATES ? "§aОбновления" : "Обновления"),
                button -> {
                    page = Page.UPDATES;
                    this.rebuildWidgets();
                }
        ).bounds(right, 48, 150, 20).build());

        if (page == Page.NOTIFICATIONS) {
            buildNotifications(left, right);
        } else {
            buildUpdates(left, right);
        }

        this.addRenderableWidget(Button.builder(
                Component.literal("Готово"),
                button -> onClose()
        ).bounds(center - 75, this.height - 32, 150, 20).build());

        updateRevision = UpdateManager.revision();
    }

    private void buildNotifications(int left, int right) {
        SonarusHelperConfig config = SonarusHelperConfig.get();

        addToggle(left, 82, 310, "Уведомления Windows", config.notificationsEnabled, value -> {
            config.notificationsEnabled = value;
            SonarusHelperConfig.save();
        });

        addToggle(left, 108, 310, "Только когда Minecraft не в фокусе", config.notificationsOnlyWhenUnfocused, value -> {
            config.notificationsOnlyWhenUnfocused = value;
            SonarusHelperConfig.save();
        });

        addToggle(left, 140, 150, "Упоминания ника", config.notifyMentions, value -> {
            config.notifyMentions = value;
            SonarusHelperConfig.save();
        });

        addToggle(right, 140, 150, "Обновления Helper", config.notifyUpdates, value -> {
            config.notifyUpdates = value;
            SonarusHelperConfig.save();
        });

        this.addRenderableWidget(Button.builder(
                Component.literal("Тестовое уведомление"),
                button -> WindowsNotifier.showTest()
        ).bounds(left, 172, 150, 20).build());

        addLabel(
                right,
                172,
                150,
                WindowsNotifier.isSupported()
                        ? "Windows: доступно"
                        : "Windows: недоступно"
        );

        addLabel(left, 204, 310, "Открыть настройки в игре: H");
    }

    private void buildUpdates(int left, int right) {
        SonarusHelperConfig config = SonarusHelperConfig.get();

        addToggle(left, 82, 310, "Проверять обновления", config.checkUpdates, value -> {
            config.checkUpdates = value;
            SonarusHelperConfig.save();
        });

        addToggle(left, 108, 310, "Автоматически скачивать обновления", config.autoDownloadUpdates, value -> {
            config.autoDownloadUpdates = value;
            SonarusHelperConfig.save();
        });

        this.addRenderableWidget(Button.builder(
                Component.literal("Проверить сейчас"),
                button -> {
                    UpdateManager.checkAsync(true);
                    this.rebuildWidgets();
                }
        ).bounds(left, 140, 150, 20).build());

        if (UpdateManager.updateAvailable()) {
            this.addRenderableWidget(Button.builder(
                    Component.literal("Скачать обновление"),
                    button -> {
                        UpdateManager.downloadAndScheduleAsync();
                        this.rebuildWidgets();
                    }
            ).bounds(right, 140, 150, 20).build());
        } else {
            addLabel(right, 140, 150, "Sonarus Helper 1.1");
        }

        addLabel(left, 172, 310, UpdateManager.statusText());
        addLabel(left, 204, 310, "Установка выполняется после выхода из Minecraft");
    }

    @Override
    public void tick() {
        super.tick();

        long revision = UpdateManager.revision();
        if (page == Page.UPDATES && revision != updateRevision) {
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
                button -> {
                }
        ).bounds(x, y, width, 20).build();
        label.active = false;
        this.addRenderableWidget(label);
    }

    private enum Page {
        NOTIFICATIONS,
        UPDATES
    }

    @FunctionalInterface
    private interface ToggleSetter {
        void set(boolean value);
    }
}
