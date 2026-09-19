package win.sonarus.helper.features.dialog;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.dialog.DialogScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.LayoutWidget;
import net.minecraft.client.gui.widget.ScrollableLayoutWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.Widget;
import win.sonarus.helper.config.SonarusHelperConfig;
import win.sonarus.helper.core.SonarusServer;
import win.sonarus.helper.mixin.DialogScreenAccessor;
import win.sonarus.helper.mixin.ScrollableLayoutWidgetAccessor;
import win.sonarus.helper.security.PasswordVault;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.Set;

public final class PasswordLoginFeature {
    private static Screen lastScreen;

    private PasswordLoginFeature() {
    }

    public static void tick(Screen screen) {
        if (!isLoginDialog(screen)) {
            lastScreen = null;
            return;
        }
        if (lastScreen == screen) {
            return;
        }
        lastScreen = screen;
        if (!SonarusHelperConfig.get().rememberPassword
                || !SonarusHelperConfig.get().autoFillPassword
                || !PasswordVault.isAvailable()) {
            return;
        }

        TextFieldWidget field = findPasswordField(screen);
        if (field == null || !field.getText().isEmpty()) {
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        String nickname = client.getSession().getUsername();
        PasswordVault.loadAsync(nickname, password -> client.execute(() -> {
            if (client.currentScreen == screen
                    && isLoginDialog(screen)
                    && nickname.equalsIgnoreCase(client.getSession().getUsername())
                    && SonarusHelperConfig.get().rememberPassword
                    && SonarusHelperConfig.get().autoFillPassword
                    && field.getText().isEmpty()) {
                field.setText(password);
            }
        }));
    }

    public static void loginButtonPressed(ButtonWidget button) {
        if (!"Войти".equals(button.getMessage().getString().strip())
                || !SonarusHelperConfig.get().rememberPassword
                || !PasswordVault.isAvailable()) {
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        Screen screen = client.currentScreen;
        if (!isLoginDialog(screen)) {
            return;
        }
        TextFieldWidget field = findPasswordField(screen);
        if (field != null && !field.getText().isEmpty()) {
            PasswordVault.saveAsync(client.getSession().getUsername(), field.getText());
        }
    }

    private static boolean isLoginDialog(Screen screen) {
        if (!(screen instanceof DialogScreen<?> dialog)
                || !"Авторизация".equals(screen.getTitle().getString().strip())
                || !isOfficialAddress()) {
            return false;
        }
        Set<ButtonWidget> buttons = new LinkedHashSet<>();
        for (Element element : screen.children()) {
            if (element instanceof ButtonWidget button) {
                buttons.add(button);
            }
        }
        DialogScreenAccessor accessor = (DialogScreenAccessor) dialog;
        collect(accessor.sonarusHelper$getLayout(), buttons, null);
        ScrollableLayoutWidget contents = accessor.sonarusHelper$getContents();
        if (contents != null) {
            collect(((ScrollableLayoutWidgetAccessor) contents).sonarusHelper$getInnerLayout(), buttons, null);
        }
        return buttons.stream().anyMatch(button -> "Войти".equals(button.getMessage().getString().strip()));
    }

    private static TextFieldWidget findPasswordField(Screen screen) {
        Set<TextFieldWidget> fields = new LinkedHashSet<>();
        for (Element element : screen.children()) {
            if (element instanceof TextFieldWidget field) {
                fields.add(field);
            }
        }
        DialogScreenAccessor accessor = (DialogScreenAccessor) screen;
        collect(accessor.sonarusHelper$getLayout(), null, fields);
        ScrollableLayoutWidget contents = accessor.sonarusHelper$getContents();
        if (contents != null) {
            collect(((ScrollableLayoutWidgetAccessor) contents).sonarusHelper$getInnerLayout(), null, fields);
        }
        return fields.size() == 1 ? fields.iterator().next() : null;
    }

    private static void collect(LayoutWidget root, Set<ButtonWidget> buttons, Set<TextFieldWidget> fields) {
        if (root == null) {
            return;
        }
        Set<Widget> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        collectRecursive(root, buttons, fields, visited);
    }

    private static void collectRecursive(LayoutWidget root, Set<ButtonWidget> buttons,
                                         Set<TextFieldWidget> fields, Set<Widget> visited) {
        if (!visited.add(root)) {
            return;
        }
        root.forEachElement(widget -> {
            if (buttons != null && widget instanceof ButtonWidget button) {
                buttons.add(button);
            }
            if (fields != null && widget instanceof TextFieldWidget field) {
                fields.add(field);
            }
            if (widget instanceof LayoutWidget child && !visited.contains(child)) {
                collectRecursive(child, buttons, fields, visited);
            }
        });
    }

    private static boolean isOfficialAddress() {
        if (!SonarusServer.isConnected()) {
            return false;
        }
        String address = SonarusServer.currentAddress();
        return address != null && (address.equalsIgnoreCase("play.sonarus.win")
                || address.equalsIgnoreCase("play.sonarus.win:26565")
                || address.equalsIgnoreCase("play.sonarus.win:25565"));
    }
}
