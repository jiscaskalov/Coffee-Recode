package coffee.client.helper.text;

import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * This class does nothing except ensure that {@link ClickEvent}'s containing Meteor Client commands can only be executed if they come from the client.
 */
public class CoffeeClickEvent extends ClickEvent {
    public CoffeeClickEvent(Action action, String value) {
        super(action, value);
    }
}
