package win.sonarus.helper.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.multiplayer.chat.GuiMessageTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import win.sonarus.helper.core.SonarusServer;
import win.sonarus.helper.notifications.ChatNotificationDetector;

@Mixin(ChatComponent.class)
public abstract class ChatComponentMixin {
    @Inject(method = "addServerSystemMessage", at = @At("HEAD"))
    private void sonarusHelper$onServerSystemMessage(Component message, CallbackInfo ci) {
        detect(message);
    }

    @Inject(method = "addPlayerMessage", at = @At("HEAD"))
    private void sonarusHelper$onPlayerMessage(
            Component message,
            MessageSignature signature,
            GuiMessageTag tag,
            CallbackInfo ci
    ) {
        detect(message);
    }

    private static void detect(Component message) {
        if (!SonarusServer.isConnected()) {
            return;
        }

        Minecraft client = Minecraft.getInstance();
        ChatNotificationDetector.onMessage(
                message.getString(),
                client.getUser().getName()
        );
    }
}
