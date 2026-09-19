package win.sonarus.helper.security;

import net.fabricmc.loader.api.FabricLoader;
import win.sonarus.helper.config.SonarusHelperConfig;

import java.io.BufferedReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Windows DPAPI CurrentUser vault. No plaintext password in JSON, JVM command arguments,
 * server packets beyond the normal login action, telemetry or log messages.
 */
public final class PasswordVault {
    private static final Path DIRECTORY = FabricLoader.getInstance().getConfigDir()
            .resolve("sonarus-helper-credentials");
    private static final ExecutorService WORKER = Executors.newSingleThreadExecutor(task -> {
        Thread thread = new Thread(task, "Sonarus Helper local credential vault");
        thread.setDaemon(true);
        return thread;
    });
    private static final String SCRIPT = """
            $ErrorActionPreference = 'Stop'
            Add-Type -AssemblyName System.Security
            $mode = [Console]::ReadLine()
            $nick = [Text.Encoding]::UTF8.GetString([Convert]::FromBase64String([Console]::ReadLine()))
            $inputBytes = [Convert]::FromBase64String([Console]::ReadLine())
            $entropy = [Text.Encoding]::UTF8.GetBytes('SonarusHelper|play.sonarus.win|' + $nick.ToLowerInvariant())
            if ($mode -eq 'protect') {
                $outputBytes = [Security.Cryptography.ProtectedData]::Protect($inputBytes, $entropy, [Security.Cryptography.DataProtectionScope]::CurrentUser)
            } elseif ($mode -eq 'unprotect') {
                $outputBytes = [Security.Cryptography.ProtectedData]::Unprotect($inputBytes, $entropy, [Security.Cryptography.DataProtectionScope]::CurrentUser)
            } else { exit 2 }
            [Console]::Out.WriteLine([Convert]::ToBase64String($outputBytes))
            """;

    private PasswordVault() {
    }

    public static boolean isAvailable() {
        return System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win");
    }

    public static void saveAsync(String nickname, String password) {
        if (!isAvailable() || !validNickname(nickname) || password == null || password.isEmpty()) {
            return;
        }
        WORKER.execute(() -> {
            if (!SonarusHelperConfig.get().rememberPassword) {
                return;
            }
            try {
                String encrypted = dpapi("protect", nickname, password);
                if (encrypted == null || encrypted.isEmpty()) {
                    return;
                }
                Files.createDirectories(DIRECTORY);
                Path file = fileFor(nickname);
                Path temp = Files.createTempFile(DIRECTORY, "credential-", ".tmp");
                try {
                    Files.writeString(temp, "DPAPI1:" + encrypted, StandardCharsets.US_ASCII);
                    Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
                } finally {
                    Files.deleteIfExists(temp);
                }
            } catch (Exception ignored) {
                // Never print credentials, ciphertext or OS account details.
            }
        });
    }

    public static void loadAsync(String nickname, Consumer<String> callback) {
        if (!isAvailable() || !validNickname(nickname)) {
            return;
        }
        WORKER.execute(() -> {
            String password = null;
            try {
                Path file = fileFor(nickname);
                if (Files.isRegularFile(file) && Files.size(file) < 8192) {
                    String data = Files.readString(file, StandardCharsets.US_ASCII);
                    if (data.startsWith("DPAPI1:")) {
                        password = dpapi("unprotect", nickname, data.substring(7));
                    }
                }
            } catch (Exception ignored) {
                // A different Windows account cannot decrypt this credential.
            }
            if (password != null && !password.isEmpty()) {
                callback.accept(password);
            }
        });
    }

    public static void deleteAsync(String nickname) {
        if (!validNickname(nickname)) {
            return;
        }
        WORKER.execute(() -> {
            try {
                Files.deleteIfExists(fileFor(nickname));
            } catch (Exception ignored) {
            }
        });
    }

    private static String dpapi(String mode, String nickname, String data) throws Exception {
        String encodedScript = Base64.getEncoder().encodeToString(SCRIPT.getBytes(StandardCharsets.UTF_16LE));
        Process process = new ProcessBuilder(
                "powershell.exe", "-NoProfile", "-NonInteractive", "-EncodedCommand", encodedScript
        ).redirectError(ProcessBuilder.Redirect.DISCARD).start();
        try {
            try (OutputStreamWriter input = new OutputStreamWriter(process.getOutputStream(), StandardCharsets.US_ASCII)) {
                input.write(mode + "\n");
                input.write(Base64.getEncoder().encodeToString(nickname.getBytes(StandardCharsets.UTF_8)) + "\n");
                String payload = "unprotect".equals(mode)
                        ? data
                        : Base64.getEncoder().encodeToString(data.getBytes(StandardCharsets.UTF_8));
                input.write(payload + "\n");
            }
            if (!process.waitFor(10, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                return null;
            }
            if (process.exitValue() != 0) {
                return null;
            }
            try (BufferedReader output = process.inputReader(StandardCharsets.US_ASCII)) {
                String result = output.readLine();
                if (result == null || result.length() > 8192) {
                    return null;
                }
                byte[] bytes = Base64.getDecoder().decode(result);
                if ("unprotect".equals(mode)) {
                    return new String(bytes, StandardCharsets.UTF_8);
                }
                return result;
            }
        } finally {
            process.destroyForcibly();
        }
    }

    private static Path fileFor(String nickname) throws Exception {
        String canonical = nickname.toLowerCase(Locale.ROOT);
        byte[] hash = MessageDigest.getInstance("SHA-256")
                .digest(("sonarus-helper|play.sonarus.win|" + canonical).getBytes(StandardCharsets.UTF_8));
        return DIRECTORY.resolve(HexFormat.of().formatHex(hash) + ".dpapi");
    }

    private static boolean validNickname(String nickname) {
        return nickname != null && nickname.matches("[A-Za-z0-9_]{3,16}");
    }
}
