package win.sonarus.helper.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import win.sonarus.helper.SonarusHelperClient;
import win.sonarus.helper.core.SonarusServer;
import win.sonarus.helper.gui.SonarusSettingsScreen;
import win.sonarus.helper.notifications.ClientFocusState;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
    private static boolean sonarusHelper$hDown;

    @Inject(method = "tick", at = @At("TAIL"))
    private void sonarusHelper$onTick(CallbackInfo ci) {
        MinecraftClient client = (MinecraftClient) (Object) this;

        if (client.currentScreen == null
                && SonarusServer.isConnected()
                && SonarusHelperClient.consumeSettingsMenuRequest()) {
            client.setScreen(new SonarusSettingsScreen(null));
        }

        boolean hDown = InputUtil.isKeyPressed(
                client.getWindow().getHandle(),
                GLFW.GLFW_KEY_H
        );

        if (hDown
                && !sonarusHelper$hDown
                && client.currentScreen == null
                && SonarusServer.isConnected()) {
            client.setScreen(new SonarusSettingsScreen(null));
        }

        sonarusHelper$hDown = hDown;
    }

    @Inject(method = "onWindowFocusChanged", at = @At("TAIL"))
    private void sonarusHelper$onFocusChanged(boolean focused, CallbackInfo ci) {
        ClientFocusState.setFocused(focused);
    }
}
