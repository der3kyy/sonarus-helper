package win.sonarus.helper.features.dialog;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ScrollableLayout;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.Layout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.dialog.DialogScreen;
import win.sonarus.helper.config.SonarusHelperConfig;
import win.sonarus.helper.core.SonarusServer;
import win.sonarus.helper.mixin.DialogScreenAccessor;
import win.sonarus.helper.mixin.ScrollableLayoutAccessor;
import win.sonarus.helper.security.PasswordVault;

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

        EditBox field = findPasswordField(screen);
        if (field == null || !field.getValue().isEmpty()) {
            return;
        }
        Minecraft client = Minecraft.getInstance();
        String nickname = client.getUser().getName();
        PasswordVault.loadAsync(nickname, password -> client.execute(() -> {
            if (client.gui.screen() == screen
                    && isLoginDialog(screen)
                    && nickname.equalsIgnoreCase(client.getUser().getName())
                    && SonarusHelperConfig.get().rememberPassword
                    && SonarusHelperConfig.get().autoFillPassword
                    && field.getValue().isEmpty()) {
                field.setValue(password);
            }
        }));
    }

    public static void loginButtonPressed(Button button) {
        if (!"Войти".equals(button.getMessage().getString().strip())
                || !SonarusHelperConfig.get().rememberPassword
                || !PasswordVault.isAvailable()) {
            return;
        }
        Minecraft client = Minecraft.getInstance();
        Screen screen = client.gui.screen();
        if (!isLoginDialog(screen)) {
            return;
        }
        EditBox field = findPasswordField(screen);
        if (field != null && !field.getValue().isEmpty()) {
            PasswordVault.saveAsync(client.getUser().getName(), field.getValue());
        }
    }

    private static boolean isLoginDialog(Screen screen) {
        if (!(screen instanceof DialogScreen<?> dialog)
                || !"Авторизация".equals(screen.getTitle().getString().strip())
                || !isOfficialAddress()) {
            return false;
        }
        Set<Button> buttons = new LinkedHashSet<>();
        for (GuiEventListener element : screen.children()) {
            if (element instanceof Button button) {
                buttons.add(button);
            }
        }
        DialogScreenAccessor accessor = (DialogScreenAccessor) dialog;
        collect(accessor.sonarusHelper$getLayout(), buttons, null);
        ScrollableLayout body = accessor.sonarusHelper$getBodyScroll();
        if (body != null) {
            collect(((ScrollableLayoutAccessor) body).sonarusHelper$getContent(), buttons, null);
        }
        return buttons.stream().anyMatch(button -> "Войти".equals(button.getMessage().getString().strip()));
    }

    private static EditBox findPasswordField(Screen screen) {
        Set<EditBox> fields = new LinkedHashSet<>();
        for (GuiEventListener element : screen.children()) {
            if (element instanceof EditBox field) {
                fields.add(field);
            }
        }
        DialogScreenAccessor accessor = (DialogScreenAccessor) screen;
        collect(accessor.sonarusHelper$getLayout(), null, fields);
        ScrollableLayout body = accessor.sonarusHelper$getBodyScroll();
        if (body != null) {
            collect(((ScrollableLayoutAccessor) body).sonarusHelper$getContent(), null, fields);
        }
        return fields.size() == 1 ? fields.iterator().next() : null;
    }

    private static void collect(Layout layout, Set<Button> buttons, Set<EditBox> fields) {
        if (layout == null) {
            return;
        }
        layout.visitWidgets((AbstractWidget widget) -> {
            if (buttons != null && widget instanceof Button button) {
                buttons.add(button);
            }
            if (fields != null && widget instanceof EditBox field) {
                fields.add(field);
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
