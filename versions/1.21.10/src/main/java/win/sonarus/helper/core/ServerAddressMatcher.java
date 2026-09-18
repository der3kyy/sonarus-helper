package win.sonarus.helper.core;

import java.util.Locale;

public final class ServerAddressMatcher {
    public static final String SONARUS_HOST = "play.sonarus.win";

    private ServerAddressMatcher() {
    }

    public static boolean isSonarus(String address) {
        if (address == null) {
            return false;
        }

        String normalized = address.trim().toLowerCase(Locale.ROOT);

        while (normalized.endsWith(".")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }

        return normalized.equals(SONARUS_HOST)
                || normalized.startsWith(SONARUS_HOST + ":")
                || normalized.equals("10.29.240.51:26565")
                || normalized.equals("178.168.208.14:26565");
    }
}
