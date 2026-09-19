package win.sonarus.helper.mixin;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientCommandSource;
import net.minecraft.network.packet.s2c.play.CommandTreeS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import win.sonarus.helper.SonarusHelperClient;
import win.sonarus.helper.core.SonarusServer;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin {
    // Register with Minecraft's local command tree to enable suggestions and valid syntax highlighting.
    @Inject(method = "onCommandTree", at = @At("TAIL"))
    private void sonarusHelper$registerLocalCommand(CommandTreeS2CPacket packet, CallbackInfo ci) {
        if (!SonarusServer.isConnected()) {
            return;
        }

        ((ClientPlayNetworkHandler) (Object) this).getCommandDispatcher().register(
                LiteralArgumentBuilder.<ClientCommandSource>literal("shelp")
                        .executes(context -> {
                            SonarusHelperClient.requestSettingsMenu();
                            return 1;
                        })
        );
    }

    @Inject(method = "sendChatCommand", at = @At("HEAD"), cancellable = true)
    private void sonarusHelper$handleCommand(String command, CallbackInfo ci) {
        if (!SonarusServer.isConnected() || !"shelp".equalsIgnoreCase(command.trim())) {
            return;
        }

        SonarusHelperClient.requestSettingsMenu();
        ci.cancel();
    }
}
