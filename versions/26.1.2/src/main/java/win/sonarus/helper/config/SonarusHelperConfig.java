package win.sonarus.helper.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class SonarusHelperConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance()
            .getConfigDir()
            .resolve("sonarus-helper.json");

    private static SonarusHelperConfig instance = defaults();


    public boolean checkUpdates = true;
    public boolean autoDownloadUpdates = false;
    public boolean rememberPassword = false;
    public boolean autoFillPassword = false;
    public long lastUpdateCheckEpochMs = 0L;

    private static SonarusHelperConfig defaults() {
        return new SonarusHelperConfig();
    }

    public static synchronized SonarusHelperConfig get() {
        return instance;
    }

    public static synchronized void load() {
        if (!Files.isRegularFile(PATH)) {
            save();
            return;
        }

        try {
            String json = Files.readString(PATH, StandardCharsets.UTF_8);
            SonarusHelperConfig loaded = GSON.fromJson(json, SonarusHelperConfig.class);
            if (loaded != null) {
                instance = loaded;
            }
        } catch (Exception ignored) {
            instance = defaults();
        }

        save();
    }

    public static synchronized void save() {
        try {
            Files.createDirectories(PATH.getParent());
            Files.writeString(PATH, GSON.toJson(instance), StandardCharsets.UTF_8);
        } catch (IOException ignored) {
        }
    }

}
