package win.sonarus.helper.mixin;

import net.minecraft.client.gui.screen.dialog.DialogScreen;
import net.minecraft.client.gui.widget.ScrollableLayoutWidget;
import net.minecraft.client.gui.widget.ThreePartsLayoutWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(DialogScreen.class)
public interface DialogScreenAccessor {
    @Accessor("layout")
    ThreePartsLayoutWidget sonarusHelper$getLayout();

    @Accessor("contents")
    ScrollableLayoutWidget sonarusHelper$getContents();
}
