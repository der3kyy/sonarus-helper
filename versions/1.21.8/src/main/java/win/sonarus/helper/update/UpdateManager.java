package win.sonarus.helper.update;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import win.sonarus.helper.config.SonarusHelperConfig;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public final class UpdateManager {
    private static final String REPOSITORY = "der3kyy/sonarus-helper";
    private static final long AUTO_CHECK_INTERVAL_MS = 6L * 60L * 60L * 1000L;

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r, "Sonarus Helper Updater");
        thread.setDaemon(true);
        return thread;
    });

    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(12))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    private static final AtomicBoolean BUSY = new AtomicBoolean(false);
    private static final AtomicBoolean INSTALL_HOOK_REGISTERED = new AtomicBoolean(false);

    private static volatile State state = State.IDLE;
    private static volatile String statusText = "Обновления ещё не проверялись";
    private static volatile Release availableRelease;
    private static volatile long revision = 0L;

    private UpdateManager() {
    }

    public static void checkOnStartup() {
        SonarusHelperConfig config = SonarusHelperConfig.get();
        if (!config.checkUpdates) {
            return;
        }

        long now = System.currentTimeMillis();
        if (now - config.lastUpdateCheckEpochMs < AUTO_CHECK_INTERVAL_MS) {
            return;
        }

        checkAsync(false);
    }

    public static void checkAsync(boolean manual) {
        if (!BUSY.compareAndSet(false, true)) {
            return;
        }

        setState(State.CHECKING, "Проверка обновлений…");

        EXECUTOR.execute(() -> {
            try {
                Release release = fetchLatestRelease();
                availableRelease = release;

                SonarusHelperConfig config = SonarusHelperConfig.get();
                config.lastUpdateCheckEpochMs = System.currentTimeMillis();
                SonarusHelperConfig.save();

                String currentVersion = currentModVersion();

                if (compareVersions(release.version(), currentVersion) <= 0) {
                    setState(State.UP_TO_DATE, "Установлена последняя версия: " + currentVersion);
                    return;
                }

                setState(State.AVAILABLE, "Доступна версия " + release.version());


                if (config.autoDownloadUpdates) {
                    downloadAndScheduleInternal(release);
                }
            } catch (Exception exception) {
                setState(
                        State.ERROR,
                        manual
                                ? "Ошибка проверки обновлений"
                                : "Не удалось проверить обновления"
                );
            } finally {
                BUSY.set(false);
            }
        });
    }

    public static void downloadAndScheduleAsync() {
        Release release = availableRelease;
        if (release == null || !BUSY.compareAndSet(false, true)) {
            return;
        }

        EXECUTOR.execute(() -> {
            try {
                downloadAndScheduleInternal(release);
            } catch (Exception exception) {
                setState(State.ERROR, "Не удалось скачать обновление");
            } finally {
                BUSY.set(false);
            }
        });
    }

    public static long revision() {
        return revision;
    }

    public static String statusText() {
        return statusText;
    }

    public static boolean updateAvailable() {
        return state == State.AVAILABLE && availableRelease != null;
    }

    public static boolean windowsUpdateSupported() {
        return isWindows();
    }

    private static Release fetchLatestRelease() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.github.com/repos/" + REPOSITORY + "/releases/latest"))
                .timeout(Duration.ofSeconds(20))
                .header("Accept", "application/vnd.github+json")
                .header("User-Agent", "Sonarus-Helper/" + currentModVersion())
                .GET()
                .build();

        HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("GitHub HTTP " + response.statusCode());
        }

        JsonObject root = JsonParser.parseString(response.body()).getAsJsonObject();
        String tag = normalizeVersion(root.get("tag_name").getAsString());
        String minecraft = currentMinecraftVersion();
        String expectedAsset = "sonarus-helper-" + tag + "-mc" + minecraft + ".jar";

        JsonArray assets = root.getAsJsonArray("assets");
        for (JsonElement element : assets) {
            JsonObject asset = element.getAsJsonObject();
            String name = asset.get("name").getAsString();

            if (!expectedAsset.equals(name)) {
                continue;
            }

            return new Release(
                    tag,
                    name,
                    asset.get("browser_download_url").getAsString()
            );
        }

        throw new IOException("No release asset for Minecraft " + minecraft);
    }

    private static void downloadAndScheduleInternal(Release release) throws Exception {
        if (!isWindows()) {
            setState(State.ERROR, "Автоустановка сейчас доступна только в Windows");
            return;
        }

        Path currentJar = currentModJar();
        if (currentJar == null || !Files.isRegularFile(currentJar)) {
            setState(State.ERROR, "Не удалось определить текущий JAR мода");
            return;
        }

        setState(State.DOWNLOADING, "Скачивание версии " + release.version() + "…");

        Path updateDir = FabricLoader.getInstance()
                .getConfigDir()
                .resolve("sonarus-helper-update");
        Files.createDirectories(updateDir);

        Path downloaded = updateDir.resolve(release.assetName() + ".download");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(release.downloadUrl()))
                .timeout(Duration.ofSeconds(60))
                .header("User-Agent", "Sonarus-Helper/" + currentModVersion())
                .GET()
                .build();

        HttpResponse<Path> response = HTTP.send(
                request,
                HttpResponse.BodyHandlers.ofFile(downloaded)
        );

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            Files.deleteIfExists(downloaded);
            throw new IOException("Asset HTTP " + response.statusCode());
        }

        Path destination = currentJar.getParent().resolve(release.assetName());
        writeUpdaterScript(currentJar, downloaded, destination);

        setState(
                State.READY_TO_INSTALL,
                "Версия " + release.version() + " установится после выхода из Minecraft"
        );
    }

    private static void writeUpdaterScript(Path currentJar, Path downloaded, Path destination) throws IOException {
        Path script = FabricLoader.getInstance()
                .getConfigDir()
                .resolve("sonarus-helper-update.ps1");

        long pid = ProcessHandle.current().pid();

        String body = "$ErrorActionPreference = 'SilentlyContinue'\r\n"
                + "$pidToWait = " + pid + "\r\n"
                + "while (Get-Process -Id $pidToWait -ErrorAction SilentlyContinue) { Start-Sleep -Milliseconds 500 }\r\n"
                + "Start-Sleep -Milliseconds 750\r\n"
                + "Remove-Item -LiteralPath '" + ps(currentJar) + "' -Force -ErrorAction SilentlyContinue\r\n"
                + "Move-Item -LiteralPath '" + ps(downloaded) + "' -Destination '" + ps(destination) + "' -Force\r\n"
                + "Remove-Item -LiteralPath $MyInvocation.MyCommand.Path -Force -ErrorAction SilentlyContinue\r\n";

        Files.writeString(script, body, StandardCharsets.UTF_8);

        if (INSTALL_HOOK_REGISTERED.compareAndSet(false, true)) {
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    new ProcessBuilder(
                            "powershell.exe",
                            "-NoProfile",
                            "-ExecutionPolicy",
                            "Bypass",
                            "-WindowStyle",
                            "Hidden",
                            "-File",
                            script.toAbsolutePath().toString()
                    ).start();
                } catch (IOException ignored) {
                }
            }, "Sonarus Helper Update Installer"));
        }
    }

    private static Path currentModJar() {
        try {
            ModContainer container = FabricLoader.getInstance()
                    .getModContainer("sonarus_helper")
                    .orElseThrow();

            List<Path> paths = container.getOrigin().getPaths();
            return paths.stream()
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".jar"))
                    .findFirst()
                    .orElse(null);
        } catch (Exception ignored) {
            return null;
        }
    }

    private static String currentModVersion() {
        return FabricLoader.getInstance()
                .getModContainer("sonarus_helper")
                .map(container -> container.getMetadata().getVersion().getFriendlyString())
                .orElse("0");
    }

    private static String currentMinecraftVersion() {
        return FabricLoader.getInstance()
                .getModContainer("minecraft")
                .map(container -> container.getMetadata().getVersion().getFriendlyString())
                .orElse("unknown");
    }

    private static int compareVersions(String left, String right) {
        int[] a = numericParts(left);
        int[] b = numericParts(right);
        int length = Math.max(a.length, b.length);

        for (int index = 0; index < length; index++) {
            int av = index < a.length ? a[index] : 0;
            int bv = index < b.length ? b[index] : 0;

            if (av != bv) {
                return Integer.compare(av, bv);
            }
        }

        return 0;
    }

    private static int[] numericParts(String value) {
        String normalized = normalizeVersion(value);
        String[] raw = normalized.split("[^0-9]+");
        return java.util.Arrays.stream(raw)
                .filter(part -> !part.isBlank())
                .mapToInt(part -> {
                    try {
                        return Integer.parseInt(part);
                    } catch (NumberFormatException ignored) {
                        return 0;
                    }
                })
                .toArray();
    }

    private static String normalizeVersion(String value) {
        if (value == null) {
            return "0";
        }

        String normalized = value.trim();
        if (normalized.startsWith("v") || normalized.startsWith("V")) {
            normalized = normalized.substring(1);
        }
        return normalized;
    }

    private static boolean isWindows() {
        return System.getProperty("os.name", "")
                .toLowerCase(Locale.ROOT)
                .contains("win");
    }

    private static String ps(Path path) {
        return path.toAbsolutePath().toString().replace("'", "''");
    }

    private static void setState(State newState, String text) {
        state = newState;
        statusText = text;
        revision++;
    }

    private enum State {
        IDLE,
        CHECKING,
        UP_TO_DATE,
        AVAILABLE,
        DOWNLOADING,
        READY_TO_INSTALL,
        ERROR
    }

    private record Release(String version, String assetName, String downloadUrl) {
    }
}
