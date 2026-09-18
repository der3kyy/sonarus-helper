package win.sonarus.helper.mixin;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.KeyInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import win.sonarus.helper.features.dialog.EnterToJoinFeature;

@Mixin(Screen.class)
public abstract class ScreenMixin {
    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void sonarusHelper$handleEnterToJoin(
            KeyInput input,
            CallbackInfoReturnable<Boolean> cir
    ) {
        Screen screen = (Screen) (Object) this;

        if (EnterToJoinFeature.handle(screen, input)) {
            cir.setReturnValue(true);
        }
    }
}
