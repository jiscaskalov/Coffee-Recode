package coffee.client.helper.event.impl;

import coffee.client.helper.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

@AllArgsConstructor
@Getter
public class HeldItemRenderEvent extends Event {
    Hand hand;
    ItemStack item;
    float ep;
    MatrixStack stack;
}
