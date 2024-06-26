/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.mixin.entity;

import coffee.client.CoffeeMain;
import coffee.client.feature.module.ModuleRegistry;
import coffee.client.feature.module.impl.movement.IgnoreWorldBorder;
import coffee.client.feature.module.impl.render.ESP;
import coffee.client.feature.module.impl.render.FreeLook;
import coffee.client.feature.module.impl.render.Shaders;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.world.border.WorldBorder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {

    @Redirect(
        method = "adjustMovementForCollisions(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Box;Lnet/minecraft/world/World;Ljava/util/List;)Lnet/minecraft/util/math/Vec3d;",
        at = @At(value = "INVOKE", target = "net/minecraft/world/border/WorldBorder.canCollide(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/Box;)Z"))
    private static boolean coffee_preventCollision(WorldBorder instance, Entity entity, Box box) {
        return ModuleRegistry.getByClass(IgnoreWorldBorder.class) != null && !ModuleRegistry.getByClass(IgnoreWorldBorder.class).isEnabled() && instance.canCollide(entity, box);
    }

    @Redirect(method = "updateVelocity", at = @At(value = "INVOKE", target = "net/minecraft/entity/Entity.getYaw()F"))
    float coffee_updateFreelook(Entity instance) {
        FreeLook fl = FreeLook.instance();
        if (fl != null && instance.equals(CoffeeMain.client.player) && fl.isEnabled() && !((boolean) fl.getEnableAA().getValue())) {
            return fl.newyaw;
        }
        return instance.getYaw();
    }

    @Inject(method = "isGlowing", at = @At("HEAD"), cancellable = true)
    void coffee_overwriteGlowing(CallbackInfoReturnable<Boolean> cir) {
        ESP byClass = ModuleRegistry.getByClass(ESP.class);
        Shaders shad = ModuleRegistry.getByClass(Shaders.class);
        if (shad != null && shad.isEnabled()) cir.setReturnValue(shad.shouldRender((Entity) (Object) this));
        if (byClass != null && byClass.isEnabled() && byClass.outlineMode == ESP.Mode.Shader) {
            cir.setReturnValue(byClass.shouldRenderEntity((Entity) (Object) this));
        }
    }
}
