package win.sonarus.helper.mixin;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.input.InputWithModifiers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import win.sonarus.helper.features.dialog.PasswordLoginFeature;

@Mixin(Button.class)
public abstract class LoginButtonMixin {
    @Inject(method = "onPress", at = @At("HEAD"))
    private void sonarusHelper$savePassword(InputWithModifiers input, CallbackInfo ci) {
        PasswordLoginFeature.loginButtonPressed((Button) (Object) this);
    }
}
