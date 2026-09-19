package win.sonarus.helper.mixin;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.network.protocol.game.ClientboundCommandsPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import win.sonarus.helper.SonarusHelperClient;
import win.sonarus.helper.core.SonarusServer;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {
    // Register with Minecraft's local command tree to enable suggestions and valid syntax highlighting.
    @Inject(method = "handleCommands", at = @At("TAIL"))
    private void sonarusHelper$registerLocalCommand(ClientboundCommandsPacket packet, CallbackInfo ci) {
        if (!SonarusServer.isConnected()) {
            return;
        }

        ((ClientPacketListener) (Object) this).getCommands().register(
                LiteralArgumentBuilder.<ClientSuggestionProvider>literal("shelp")
                        .executes(context -> {
                            SonarusHelperClient.requestSettingsMenu();
                            return 1;
                        })
        );
    }

    @Inject(method = "sendCommand", at = @At("HEAD"), cancellable = true)
    private void sonarusHelper$handleCommand(String command, CallbackInfo ci) {
        if (!SonarusServer.isConnected() || !"shelp".equalsIgnoreCase(command.trim())) {
            return;
        }

        SonarusHelperClient.requestSettingsMenu();
        ci.cancel();
    }
}
