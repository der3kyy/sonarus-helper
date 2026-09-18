package win.sonarus.helper.notifications;

public final class ClientFocusState {
    private static volatile boolean focused = true;

    private ClientFocusState() {
    }

    public static boolean isFocused() {
        return focused;
    }

    public static void setFocused(boolean value) {
        focused = value;
    }
}
