package win.sonarus.helper.features.dialog;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.Layout;
import net.minecraft.client.gui.components.ScrollableLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.dialog.DialogScreen;
import net.minecraft.client.input.KeyEvent;
import com.mojang.blaze3d.platform.InputConstants;
import win.sonarus.helper.core.SonarusServer;
import win.sonarus.helper.mixin.DialogScreenAccessor;
import win.sonarus.helper.mixin.ScrollableLayoutAccessor;

import java.util.LinkedHashSet;
import java.util.Set;

public final class EnterToJoinFeature {
    private static final String JOIN_BUTTON_TEXT = "Войти";

    private EnterToJoinFeature() {
    }

    public static boolean handle(Screen screen, KeyEvent event) {
        if (!(screen instanceof DialogScreen<?> dialogScreen)) {
            return false;
        }

        int keyCode = event.key();
        if (keyCode != InputConstants.KEY_RETURN && keyCode != InputConstants.KEY_NUMPADENTER) {
            return false;
        }

        if (!SonarusServer.isConnected()) {
            return false;
        }

        Set<Button> buttons = new LinkedHashSet<>();

        for (GuiEventListener child : screen.children()) {
            if (child instanceof Button button) {
                buttons.add(button);
            }
        }

        DialogScreenAccessor accessor = (DialogScreenAccessor) dialogScreen;
        collectButtons(accessor.sonarusHelper$getLayout(), buttons);

        ScrollableLayout bodyScroll = accessor.sonarusHelper$getBodyScroll();
        if (bodyScroll != null) {
            Layout innerLayout = ((ScrollableLayoutAccessor) bodyScroll).sonarusHelper$getContent();
            collectButtons(innerLayout, buttons);
        }

        for (Button button : buttons) {
            String text = button.getMessage().getString().strip();
            if (!isJoinButton(button, text)) {
                continue;
            }

            button.onPress(event);
            return true;
        }

        return false;
    }

    private static void collectButtons(Layout layout, Set<Button> buttons) {
        if (layout == null) {
            return;
        }

        layout.visitWidgets((AbstractWidget widget) -> {
            if (widget instanceof Button button) {
                buttons.add(button);
            }
        });
    }

    private static boolean isJoinButton(Button button, String visibleText) {
        return button.active
                && button.visible
                && JOIN_BUTTON_TEXT.equals(visibleText);
    }
}
