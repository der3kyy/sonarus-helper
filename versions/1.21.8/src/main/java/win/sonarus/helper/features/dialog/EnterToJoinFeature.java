package win.sonarus.helper.features.dialog;

import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.dialog.DialogScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.LayoutWidget;
import net.minecraft.client.gui.widget.ScrollableLayoutWidget;
import net.minecraft.client.gui.widget.Widget;
import org.lwjgl.glfw.GLFW;
import win.sonarus.helper.core.SonarusServer;
import win.sonarus.helper.mixin.DialogScreenAccessor;
import win.sonarus.helper.mixin.ScrollableLayoutWidgetAccessor;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.Set;

public final class EnterToJoinFeature {
    private static final String JOIN_BUTTON_TEXT = "Войти";

    private EnterToJoinFeature() {
    }

    public static boolean handle(Screen screen, int keyCode) {
        if (!(screen instanceof DialogScreen<?> dialogScreen)) {
            return false;
        }

        if (keyCode != GLFW.GLFW_KEY_ENTER && keyCode != GLFW.GLFW_KEY_KP_ENTER) {
            return false;
        }


        if (!SonarusServer.isConnected()) {
            return false;
        }

        Set<ButtonWidget> buttons = new LinkedHashSet<>();

        // Widgets registered directly on Screen.
        for (Element child : screen.children()) {
            if (child instanceof ButtonWidget button) {
                buttons.add(button);
            }
        }

        DialogScreenAccessor accessor = (DialogScreenAccessor) dialogScreen;

        // Header/footer and other widgets in the outer dialog layout.
        collectButtons(accessor.sonarusHelper$getLayout(), buttons);

        // MultiActionDialogScreen places its action grid in the scrollable body.
        ScrollableLayoutWidget contents = accessor.sonarusHelper$getContents();
        if (contents != null) {
            LayoutWidget innerLayout =
                    ((ScrollableLayoutWidgetAccessor) contents).sonarusHelper$getInnerLayout();
            collectButtons(innerLayout, buttons);
        }

        for (ButtonWidget button : buttons) {
            String text = button.getMessage().getString().strip();

            if (!isJoinButton(button, text)) {
                continue;
            }
            button.onPress();
            return true;
        }
        return false;
    }

    private static void collectButtons(LayoutWidget root, Set<ButtonWidget> buttons) {
        if (root == null) {
            return;
        }

        Set<Widget> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        collectButtonsRecursive(root, buttons, visited);
    }

    private static void collectButtonsRecursive(
            LayoutWidget layout,
            Set<ButtonWidget> buttons,
            Set<Widget> visited
    ) {
        if (!visited.add(layout)) {
            return;
        }

        layout.forEachElement(widget -> {
            if (widget instanceof ButtonWidget button) {
                buttons.add(button);
            }

            if (widget instanceof LayoutWidget childLayout && !visited.contains(childLayout)) {
                collectButtonsRecursive(childLayout, buttons, visited);
            }
        });
    }

    private static boolean isJoinButton(ButtonWidget button, String visibleText) {
        return button.active
                && button.visible
                && JOIN_BUTTON_TEXT.equals(visibleText);
    }
}
