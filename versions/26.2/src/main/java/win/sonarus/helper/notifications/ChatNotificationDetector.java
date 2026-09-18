package win.sonarus.helper.notifications;

import java.util.Locale;

public final class ChatNotificationDetector {
    private ChatNotificationDetector() {
    }

    public static void onMessage(String message, String username) {
        if (message == null || username == null || username.isBlank()) {
            return;
        }

        String lowerMessage = message.toLowerCase(Locale.ROOT);
        String lowerUsername = username.toLowerCase(Locale.ROOT);

        int index = lowerMessage.indexOf(lowerUsername);
        while (index >= 0) {
            int before = index - 1;
            int after = index + lowerUsername.length();

            boolean leftBoundary = before < 0 || !isNameChar(lowerMessage.charAt(before));
            boolean rightBoundary = after >= lowerMessage.length() || !isNameChar(lowerMessage.charAt(after));

            if (leftBoundary && rightBoundary) {
                WindowsNotifier.show(
                        NotificationType.MENTION,
                        "Вас упомянули на Sonarus",
                        message
                );
                return;
            }

            index = lowerMessage.indexOf(lowerUsername, index + 1);
        }
    }

    private static boolean isNameChar(char value) {
        return Character.isLetterOrDigit(value) || value == '_';
    }
}
