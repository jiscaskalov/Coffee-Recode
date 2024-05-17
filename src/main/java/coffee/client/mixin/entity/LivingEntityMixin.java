/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.mixin.entity;

import coffee.client.CoffeeMain;
import coffee.client.feature.module.ModuleRegistry;
import coffee.client.feature.module.impl.movement.Jesus;
import coffee.client.feature.module.impl.movement.NoLevitation;
import coffee.client.feature.module.impl.movement.NoPush;
import coffee.client.feature.module.impl.render.Animations;
import coffee.client.feature.module.impl.render.FreeLook;
import coffee.client.helper.manager.AttackManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.fluid.FluidState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(value = LivingEntity.class, priority = 990)
public abstract class LivingEntityMixin {
    @Shadow protected abstract void jump();
    Animations anim = ModuleRegistry.getByClass(Animations.class);

    @Inject(method = {"getHandSwingDuration"}, at = {@At("HEAD")}, cancellable = true)
    private void getArmSwingAnimationEnd(final CallbackInfoReturnable<Integer> info) {
        if (anim != null && anim.isEnabled() && anim.slowAnimation.getValue())
            info.setReturnValue((int) Math.floor(anim.slowAnimationVal.getValue()));
    }

    @Inject(method = "onAttacking", at = @At("HEAD"))
    public void coffee_setLastAttacked(Entity target, CallbackInfo ci) {
        if (this.equals(CoffeeMain.client.player) && target instanceof LivingEntity entity) {
            AttackManager.registerLastAttacked(entity);
        }
    }

    @Inject(method = "canWalkOnFluid", at = @At("HEAD"), cancellable = true)
    public void coffee_overwriteFluidWalk(FluidState fluidState, CallbackInfoReturnable<Boolean> cir) {
        if (CoffeeMain.client.player == null) {
            return;
        }
        // shut up monkey these are mixins you fucking idiot
        if (this.equals(CoffeeMain.client.player)) {
            Jesus jesus = ModuleRegistry.getByClass(Jesus.class);
            if (jesus != null && jesus.isEnabled() && jesus.mode.getValue() == Jesus.Mode.Solid) {
                cir.setReturnValue(true);
            }
        }
    }

    @Inject(method = "pushAwayFrom", at = @At("HEAD"), cancellable = true, require = 0)
    public void coffee_cancelCollision(Entity entity, CallbackInfo ci) {
        if (this.equals(CoffeeMain.client.player)) {
            NoPush np = ModuleRegistry.getByClass(NoPush.class);
            if (np != null && np.isEnabled()) {
                ci.cancel();
            }
        }
    }

    // INCREDIBLE baritone hack, never fucking do this
    // also fuck you leijurv for doing the same redirect as me
    @ModifyVariable(method = "jump", at = @At(value = "STORE"), ordinal = 0)
    private float coffee_replaceYaw(float f) {
        FreeLook fl = FreeLook.instance();
        if (fl != null && equals(CoffeeMain.client.player) && fl.isEnabled() && !((boolean) fl.getEnableAA().getValue())) {
            return (float) Math.toDegrees(fl.newyaw);
        }
        return f;
    }
}
