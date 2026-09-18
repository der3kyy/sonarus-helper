package win.sonarus.helper.mixin;

import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.components.ScrollableLayout;
import net.minecraft.client.gui.screens.dialog.DialogScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(DialogScreen.class)
public interface DialogScreenAccessor {
    @Accessor("layout")
    HeaderAndFooterLayout sonarusHelper$getLayout();

    @Accessor("bodyScroll")
    ScrollableLayout sonarusHelper$getBodyScroll();
}
