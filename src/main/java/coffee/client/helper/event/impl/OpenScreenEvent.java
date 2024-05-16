package coffee.client.helper.event.impl;

import coffee.client.helper.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.client.gui.screen.Screen;

@AllArgsConstructor
@Getter
public class OpenScreenEvent extends Event {
    Screen screen;
}
