package win.sonarus.helper.mixin;

import net.minecraft.client.gui.widget.LayoutWidget;
import net.minecraft.client.gui.widget.ScrollableLayoutWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ScrollableLayoutWidget.class)
public interface ScrollableLayoutWidgetAccessor {
    @Accessor("layout")
    LayoutWidget sonarusHelper$getInnerLayout();
}
