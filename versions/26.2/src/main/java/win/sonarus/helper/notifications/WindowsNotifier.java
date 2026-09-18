package win.sonarus.helper.notifications;

import win.sonarus.helper.config.SonarusHelperConfig;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class WindowsNotifier {
    private static final long DEDUP_MS = 8_000L;
    private static final Map<String, Long> LAST_SHOWN = new ConcurrentHashMap<>();
    private static final ScheduledExecutorService CLEANUP = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread thread = new Thread(r, "Sonarus Helper Notification Cleanup");
        thread.setDaemon(true);
        return thread;
    });

    private WindowsNotifier() {
    }

    public static boolean isSupported() {
        return System.getProperty("os.name", "").toLowerCase().contains("win")
                && !GraphicsEnvironment.isHeadless()
                && SystemTray.isSupported();
    }

    public static void show(NotificationType type, String title, String message) {
        SonarusHelperConfig config = SonarusHelperConfig.get();

        if (!config.notificationsEnabled || !config.isEnabled(type)) {
            return;
        }

        if (config.notificationsOnlyWhenUnfocused && ClientFocusState.isFocused()) {
            return;
        }

        showInternal(title, message, false);
    }

    public static void showTest() {
        showInternal(
                "Sonarus Helper",
                "Тестовое уведомление работает.",
                true
        );
    }

    private static void showInternal(String title, String message, boolean force) {
        if (!isSupported()) {
            return;
        }

        String cleanTitle = trim(title, 64);
        String cleanMessage = trim(message, 220);
        String key = cleanTitle + "\n" + cleanMessage;
        long now = System.currentTimeMillis();

        if (!force) {
            Long previous = LAST_SHOWN.put(key, now);
            if (previous != null && now - previous < DEDUP_MS) {
                return;
            }
        }

        EventQueue.invokeLater(() -> {
            try {
                SystemTray tray = SystemTray.getSystemTray();
                TrayIcon icon = new TrayIcon(createImage(), "Sonarus Helper");
                icon.setImageAutoSize(true);
                tray.add(icon);
                icon.displayMessage(cleanTitle, cleanMessage, TrayIcon.MessageType.NONE);

                CLEANUP.schedule(
                        () -> EventQueue.invokeLater(() -> tray.remove(icon)),
                        10,
                        TimeUnit.SECONDS
                );
            } catch (Exception ignored) {
            }
        });
    }

    private static Image createImage() {
        BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        try {
            graphics.setColor(new Color(35, 35, 35, 255));
            graphics.fillOval(1, 1, 14, 14);
            graphics.setColor(Color.WHITE);
            graphics.drawString("S", 5, 12);
        } finally {
            graphics.dispose();
        }
        return image;
    }

    private static String trim(String value, int max) {
        if (value == null) {
            return "";
        }

        String normalized = value.replace('\r', ' ').replace('\n', ' ').trim();
        if (normalized.length() <= max) {
            return normalized;
        }

        return normalized.substring(0, Math.max(0, max - 1)) + "…";
    }
}
