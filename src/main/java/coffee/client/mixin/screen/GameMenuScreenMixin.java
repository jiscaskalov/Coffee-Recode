/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.mixin.screen;

import coffee.client.feature.command.impl.SelfDestruct;
import coffee.client.feature.gui.screen.*;
import coffee.client.feature.gui.widget.RoundButton;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.realms.gui.screen.RealmsMainScreen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameMenuScreen.class)
public class GameMenuScreenMixin extends Screen {
    protected GameMenuScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "initWidgets", at = @At("RETURN"))
    void coffee_addClientButtons(CallbackInfo ci) {
        if (SelfDestruct.shouldSelfDestruct()) {
            return;
        }
        addDrawableChild(new RoundButton(RoundButton.STANDARD, 5, 5, 60, 20, "Themes", () -> client.setScreen(new ThemeEditScreen())));
        addDrawableChild(new RoundButton(RoundButton.STANDARD, 5, 30, 60, 20, "Edit HUD", () -> client.setScreen(new HudEditorScreen())));
        addDrawableChild(new RoundButton(RoundButton.STANDARD, 5, 55, 60, 20, "Waypoints", () -> client.setScreen(new WaypointEditScreen())));
    }

    @ModifyArg(method = "method_19845", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;setScreen(Lnet/minecraft/client/gui/screen/Screen;)V"), index = 0)
    Screen modifySetScreen(Screen screen) {
        if (screen instanceof TitleScreen) {
            return HomeScreen.instance();
        } else if (screen instanceof RealmsMainScreen) {
            return new RealmsMainScreen(HomeScreen.instance());
        } else if (screen instanceof MultiplayerScreen) {
            return new MultiplayerScreen(HomeScreen.instance());
        }
        return screen;
    }
}
