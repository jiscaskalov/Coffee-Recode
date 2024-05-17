/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.mixin.entity;

import coffee.client.CoffeeMain;
import coffee.client.feature.module.Module;
import coffee.client.feature.module.ModuleRegistry;
import coffee.client.feature.module.impl.misc.PortalGUI;
import coffee.client.feature.module.impl.movement.NoPush;
import coffee.client.feature.module.impl.movement.NoSlow;
import coffee.client.feature.module.impl.movement.Phase;
import coffee.client.feature.module.impl.render.Freecam;
import coffee.client.helper.manager.ConfigManager;
import coffee.client.helper.util.Rotations;
import coffee.client.helper.util.Utils;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(value = ClientPlayerEntity.class, priority = 900)
public class ClientPlayerEntityMixin {
    @Inject(method = "tick", at = @At("HEAD"))
    public void coffee_preTick(CallbackInfo ci) {
        Utils.TickManager.tick();
        if (!ConfigManager.enabled) {
            ConfigManager.enableModules();
        }
        for (Module module : ModuleRegistry.getModules()) {
            if (module.isEnabled()) {
                module.tick();
            }
        }
    }

    @Redirect(method = "updateNausea", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;shouldPause()Z"))
    public boolean coffee_overwritePauseScreen(Screen screen) {
        PortalGUI pg = ModuleRegistry.getByClass(PortalGUI.class);
        return pg != null && pg.isEnabled() || screen.shouldPause();
    }

    @Inject(method = "pushOutOfBlocks", at = @At("HEAD"), cancellable = true)
    public void coffee_preventPush(double x, double z, CallbackInfo ci) {
        Freecam freecam = ModuleRegistry.getByClass(Freecam.class);
        NoPush noPush = ModuleRegistry.getByClass(NoPush.class);
        Phase phase = ModuleRegistry.getByClass(Phase.class);
        if (freecam == null || noPush == null || phase == null) return;
        if (freecam.isEnabled() || noPush.isEnabled() || phase.isEnabled()) {
            ci.cancel();
        }
    }

    @Redirect(method = "sendMovementPackets", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;getYaw()F"))
    float coffee_replaceMovementPacketYaw(ClientPlayerEntity instance) {
        if (Rotations.isEnabled()) {
            return Rotations.getClientYaw();
        } else {
            return instance.getYaw();
        }
    }

    @Redirect(method = "sendMovementPackets", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;getPitch()F"))
    float coffee_replaceMovementPacketPitch(ClientPlayerEntity instance) {
        if (Rotations.isEnabled()) {
            return Rotations.getClientPitch();
        } else {
            return instance.getPitch();
        }
    }

    @Redirect(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isUsingItem()Z"), require = 0)
    boolean coffee_fakeIsUsingItem(ClientPlayerEntity instance) {
        NoSlow noSlow = ModuleRegistry.getByClass(NoSlow.class);
        if (this.equals(CoffeeMain.client.player) && noSlow != null && noSlow.isEnabled() && noSlow.isEating()) {
            return false;
        }
        return instance.isUsingItem();
    }

}
