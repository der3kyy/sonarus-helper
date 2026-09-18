package win.sonarus.helper;

import net.fabricmc.api.ClientModInitializer;
import win.sonarus.helper.config.SonarusHelperConfig;
import win.sonarus.helper.update.UpdateManager;

public final class SonarusHelperClient implements ClientModInitializer {
    private static volatile boolean settingsMenuRequested;

    public static void requestSettingsMenu() {
        settingsMenuRequested = true;
    }

    public static boolean consumeSettingsMenuRequest() {
        boolean requested = settingsMenuRequested;
        settingsMenuRequested = false;
        return requested;
    }

    @Override
    public void onInitializeClient() {
        SonarusHelperConfig.load();
        UpdateManager.checkOnStartup();
    }
}
