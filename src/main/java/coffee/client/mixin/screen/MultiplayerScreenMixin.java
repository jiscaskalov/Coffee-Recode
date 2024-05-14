/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.mixin.screen;

import coffee.client.CoffeeMain;
import coffee.client.feature.command.impl.SelfDestruct;
import coffee.client.feature.gui.element.impl.TextElement;
import coffee.client.feature.gui.screen.ProxyManagerScreen;
import coffee.client.feature.gui.widget.RoundButton;
import coffee.client.helper.font.FontRenderers;
import coffee.client.helper.font.renderer.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;

@Mixin(MultiplayerScreen.class)
public class MultiplayerScreenMixin extends Screen {
    @Shadow
    @Final
    private Screen parent;

    public MultiplayerScreenMixin() {
        super(Text.of(""));
    }

    @Inject(method = "init", at = @At("RETURN"))
    void coffee_postInit(CallbackInfo ci) {
        if (SelfDestruct.shouldSelfDestruct()) {
            return;
        }
        double sourceY = 32 / 2d - 20 / 2d;
        RoundButton proxies = new RoundButton(RoundButton.STANDARD, 5, sourceY, 60, 20, "Proxies", () -> CoffeeMain.client.setScreen(new ProxyManagerScreen(this)));
        addDrawableChild(proxies);
    }
}
