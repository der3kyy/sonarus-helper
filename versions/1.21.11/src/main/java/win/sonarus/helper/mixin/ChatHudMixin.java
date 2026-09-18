package win.sonarus.helper.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import win.sonarus.helper.core.SonarusServer;
import win.sonarus.helper.notifications.ChatNotificationDetector;

@Mixin(ChatHud.class)
public abstract class ChatHudMixin {
    @Inject(
            method = "addMessage(Lnet/minecraft/text/Text;)V",
            at = @At("HEAD")
    )
    private void sonarusHelper$onSimpleMessage(Text message, CallbackInfo ci) {
        detect(message);
    }

    @Inject(
            method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;Lnet/minecraft/client/gui/hud/MessageIndicator;)V",
            at = @At("HEAD")
    )
    private void sonarusHelper$onMessage(
            Text message,
            MessageSignatureData signature,
            MessageIndicator indicator,
            CallbackInfo ci
    ) {
        detect(message);
    }

    private static void detect(Text message) {
        if (!SonarusServer.isConnected()) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        ChatNotificationDetector.onMessage(
                message.getString(),
                client.getSession().getUsername()
        );
    }
}
