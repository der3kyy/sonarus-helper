package win.sonarus.helper.mixin;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import win.sonarus.helper.SonarusHelperClient;
import win.sonarus.helper.core.SonarusServer;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin {
    @Inject(method = "sendChatCommand", at = @At("HEAD"), cancellable = true)
    private void sonarusHelper$handleCommand(String command, CallbackInfo ci) {
        if (!SonarusServer.isConnected() || !"sonarus".equalsIgnoreCase(command.trim())) {
            return;
        }

        SonarusHelperClient.requestSettingsMenu();
        ci.cancel();
    }
}
