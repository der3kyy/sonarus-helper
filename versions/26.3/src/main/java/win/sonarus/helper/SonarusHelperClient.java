package win.sonarus.helper;

import net.fabricmc.api.ClientModInitializer;
import win.sonarus.helper.config.SonarusHelperConfig;
import win.sonarus.helper.update.UpdateManager;

public final class SonarusHelperClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        SonarusHelperConfig.load();
        UpdateManager.checkOnStartup();
    }
}
