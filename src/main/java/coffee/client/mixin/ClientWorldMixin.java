package coffee.client.mixin;

import coffee.client.feature.module.ModuleRegistry;
import coffee.client.feature.module.impl.render.Themes;
import coffee.client.feature.module.impl.render.WorldTweaks;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.awt.*;

@Mixin(ClientWorld.class)
public class ClientWorldMixin {
    @Inject(method = "getSkyColor", at = @At("HEAD"), cancellable = true)
    private void getSkyColorHook(Vec3d cameraPos, float tickDelta, CallbackInfoReturnable<Vec3d> cir) {
        WorldTweaks wt = ModuleRegistry.getByClass(WorldTweaks.class);
        if (wt != null && wt.isEnabled() && wt.fogModify.getValue()) {
            Color c = Themes.getCurrentTheme().getSecondary().darker().darker().darker().darker().darker().darker().darker().darker().darker().darker().darker().darker();
            cir.setReturnValue(new Vec3d(c.getRed(), c.getGreen(), c.getBlue()));
        }
    }
}
