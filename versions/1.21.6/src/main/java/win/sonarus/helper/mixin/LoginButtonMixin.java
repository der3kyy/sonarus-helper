package win.sonarus.helper.mixin;

import net.minecraft.client.gui.widget.ButtonWidget;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import win.sonarus.helper.features.dialog.PasswordLoginFeature;

@Mixin(ButtonWidget.class)
public abstract class LoginButtonMixin {
    @Inject(method = "onPress", at = @At("HEAD"))
    private void sonarusHelper$savePassword(CallbackInfo ci) {
        PasswordLoginFeature.loginButtonPressed((ButtonWidget) (Object) this);
    }
}
