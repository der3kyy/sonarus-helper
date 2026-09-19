package win.sonarus.helper.mixin;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import win.sonarus.helper.SonarusHelperClient;
import win.sonarus.helper.core.SonarusServer;
import win.sonarus.helper.gui.SonarusSettingsScreen;
import win.sonarus.helper.features.dialog.PasswordLoginFeature;
import win.sonarus.helper.features.command.ClientCommandFeature;

@Mixin(Minecraft.class)
public abstract class MinecraftClientMixin {
    @Inject(method = "tick", at = @At("TAIL"))
    private void sonarusHelper$onTick(CallbackInfo ci) {
        Minecraft client = (Minecraft) (Object) this;
        ClientCommandFeature.registerIfNeeded();
        PasswordLoginFeature.tick(client.gui.screen());

        if (client.gui.screen() == null
                && SonarusServer.isConnected()
                && SonarusHelperClient.consumeSettingsMenuRequest()) {
            client.gui.setScreen(new SonarusSettingsScreen(null));
        }

    }
}
