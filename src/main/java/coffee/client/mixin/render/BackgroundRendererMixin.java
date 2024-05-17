package coffee.client.mixin.render;

import coffee.client.feature.module.ModuleRegistry;
import coffee.client.feature.module.impl.render.Themes;
import coffee.client.feature.module.impl.render.WorldTweaks;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;

@Mixin(BackgroundRenderer.class)
public class BackgroundRendererMixin {
    @Inject(method = "applyFog", at = @At("TAIL"))
    private static void onApplyFog(Camera camera, BackgroundRenderer.FogType fogType, float viewDistance, boolean thickFog, float tickDelta, CallbackInfo info) {
        Color color = Themes.getCurrentTheme().getSecondary().darker().darker().darker().darker().darker().darker().darker().darker().darker().darker().darker().darker();
        WorldTweaks wt = ModuleRegistry.getByClass(WorldTweaks.class);
        if(wt != null && wt.isEnabled() && wt.fogModify.getValue()) {
            RenderSystem.setShaderFogStart(Math.round(wt.fogStart.getValue()));
            RenderSystem.setShaderFogEnd(Math.round(wt.fogEnd.getValue()));
            RenderSystem.setShaderFogColor(color.getRed(), color.getGreen(), color.getBlue());
        }
    }
}
