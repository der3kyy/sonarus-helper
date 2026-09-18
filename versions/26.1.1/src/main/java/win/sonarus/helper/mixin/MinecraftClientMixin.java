package win.sonarus.helper.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import win.sonarus.helper.SonarusHelperClient;
import win.sonarus.helper.core.SonarusServer;
import win.sonarus.helper.gui.SonarusSettingsScreen;
import win.sonarus.helper.notifications.ClientFocusState;

@Mixin(Minecraft.class)
public abstract class MinecraftClientMixin {
    private static boolean sonarusHelper$hDown;

    @Inject(method = "tick", at = @At("TAIL"))
    private void sonarusHelper$onTick(CallbackInfo ci) {
        Minecraft client = (Minecraft) (Object) this;

        ClientFocusState.setFocused(client.isWindowActive());

        if (client.screen == null
                && SonarusServer.isConnected()
                && SonarusHelperClient.consumeSettingsMenuRequest()) {
            client.setScreen(new SonarusSettingsScreen(null));
        }

        boolean hDown = InputConstants.isKeyDown(
                client.getWindow(),
                InputConstants.KEY_H
        );

        if (hDown
                && !sonarusHelper$hDown
                && client.screen == null
                && SonarusServer.isConnected()) {
            client.setScreen(new SonarusSettingsScreen(null));
        }

        sonarusHelper$hDown = hDown;
    }
}
