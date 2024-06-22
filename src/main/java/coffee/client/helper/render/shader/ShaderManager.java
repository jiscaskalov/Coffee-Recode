package coffee.client.helper.render.shader;

import coffee.client.CoffeeMain;
import coffee.client.feature.module.ModuleRegistry;
import coffee.client.feature.module.impl.render.Shaders;
import coffee.client.mixinUtil.ShaderEffectDuck;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import ladysnake.satin.api.managed.ManagedShaderEffect;
import ladysnake.satin.api.managed.ShaderEffectManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL30C;

import java.util.ArrayList;
import java.util.List;

import static coffee.client.CoffeeMain.client;

public class ShaderManager {
    private final static List<RenderTask> tasks = new ArrayList<>();
    private CoffeeFramebuffer shaderBuffer;

    public float time = 0;

    public static ManagedShaderEffect DEFAULT_OUTLINE;
    public static ManagedShaderEffect SMOKE_OUTLINE;
    public static ManagedShaderEffect GRADIENT_OUTLINE;
    public static ManagedShaderEffect SNOW_OUTLINE;

    public static ManagedShaderEffect DEFAULT;
    public static ManagedShaderEffect SMOKE;
    public static ManagedShaderEffect GRADIENT;
    public static ManagedShaderEffect SNOW;

    public void renderShader(Runnable runnable, Shader mode) {
        tasks.add(new RenderTask(runnable, mode));
    }

    public void renderShaders() {
        if (DEFAULT == null) {
            shaderBuffer = new CoffeeFramebuffer(client.getFramebuffer().textureWidth, client.getFramebuffer().textureHeight);
            reloadShaders();
        }

        if(shaderBuffer == null)
            return;

        tasks.forEach(t -> applyShader(t.task(), t.shader()));
        tasks.clear();
    }

    public void applyShader(Runnable runnable, Shader mode) {
        Framebuffer clientBuffer = MinecraftClient.getInstance().getFramebuffer();
        RenderSystem.assertOnRenderThreadOrInit();
        if (shaderBuffer.textureWidth != clientBuffer.textureWidth || shaderBuffer.textureHeight != clientBuffer.textureHeight)
            shaderBuffer.resize(clientBuffer.textureWidth, clientBuffer.textureHeight, false);
        GlStateManager._glBindFramebuffer(GL30C.GL_DRAW_FRAMEBUFFER, shaderBuffer.fbo);
        shaderBuffer.beginWrite(true);
        runnable.run();
        shaderBuffer.endWrite();
        GlStateManager._glBindFramebuffer(GL30C.GL_DRAW_FRAMEBUFFER, clientBuffer.fbo);
        clientBuffer.beginWrite(false);
        ManagedShaderEffect shader = getShader(mode);
        Framebuffer mainBuffer = MinecraftClient.getInstance().getFramebuffer();
        PostEffectProcessor effect = shader.getShaderEffect();

        if (effect != null)
            ((ShaderEffectDuck) effect).coffee_addFakeTarget("bufIn", shaderBuffer);

        Framebuffer outBuffer = shader.getShaderEffect().getSecondaryTarget("bufOut");
        setupShader(mode, shader);
        shaderBuffer.clear(false);
        mainBuffer.beginWrite(false);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SrcFactor.ZERO, GlStateManager.DstFactor.ONE);
        RenderSystem.backupProjectionMatrix();
        outBuffer.draw(outBuffer.textureWidth, outBuffer.textureHeight, false);
        RenderSystem.restoreProjectionMatrix();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    public ManagedShaderEffect getShader(@NotNull Shader mode) {
        return switch (mode) {
            case Gradient -> GRADIENT;
            case Smoke -> SMOKE;
            case Snow -> SNOW;
            default -> DEFAULT;
        };
    }

    public ManagedShaderEffect getShaderOutline(@NotNull Shader mode) {
        return switch (mode) {
            case Gradient -> GRADIENT_OUTLINE;
            case Smoke -> SMOKE_OUTLINE;
            case Snow -> SNOW_OUTLINE;
            default -> DEFAULT_OUTLINE;
        };
    }

    public void setupShader(Shader shader, ManagedShaderEffect effect) {
        try {
            Shaders shaders = ModuleRegistry.getByClass(Shaders.class);
            if (shader == Shader.Gradient) {
                effect.setUniformValue("alpha0", shaders.glow.getValue() ? -1.0f : shaders.outlineColor.getValue().getAlpha() / 255.0f);
                effect.setUniformValue("alpha1", (int) Math.floor(shaders.fillAlpha.getValue() / 255f));
                effect.setUniformValue("alpha2", (int) Math.floor(shaders.alpha2.getValue()) / 255f);
                effect.setUniformValue("lineWidth", (int) Math.floor(shaders.lineWidth.getValue()));
                effect.setUniformValue("oct", (int) Math.floor(shaders.octaves.getValue()));
                effect.setUniformValue("quality", (int) Math.floor(shaders.quality.getValue()));
                effect.setUniformValue("factor", (float) Math.floor(shaders.factor.getValue()));
                effect.setUniformValue("moreGradient", (float) Math.floor(shaders.gradient.getValue()));
                effect.setUniformValue("resolution", (float) client.getWindow().getScaledWidth(), (float) client.getWindow().getScaledHeight());
                effect.setUniformValue("time", time);
                effect.render(client.getTickDelta());
                time += 0.008f;
            } else if (shader == Shader.Smoke) {
                effect.setUniformValue("alpha0", shaders.glow.getValue() ? -1.0f : shaders.outlineColor.getValue().getAlpha() / 255.0f);
                effect.setUniformValue("alpha1", (int) Math.floor(shaders.fillAlpha.getValue() / 255f));
                effect.setUniformValue("lineWidth", (int) Math.floor(shaders.lineWidth.getValue()));
                effect.setUniformValue("quality", (int) Math.floor(shaders.quality.getValue()));
                effect.setUniformValue("first", shaders.outlineColor.getValue().getRed(), shaders.outlineColor.getValue().getGreen(), shaders.outlineColor.getValue().getBlue(), shaders.outlineColor.getValue().getAlpha());
                effect.setUniformValue("second", shaders.outlineColor1.getValue().getRed(), shaders.outlineColor1.getValue().getGreen(), shaders.outlineColor1.getValue().getBlue());
                effect.setUniformValue("third", shaders.outlineColor2.getValue().getRed(), shaders.outlineColor2.getValue().getGreen(), shaders.outlineColor2.getValue().getBlue());
                effect.setUniformValue("ffirst", shaders.fillColor1.getValue().getRed(), shaders.fillColor1.getValue().getGreen(), shaders.fillColor1.getValue().getBlue(), shaders.fillColor1.getValue().getAlpha());
                effect.setUniformValue("fsecond", shaders.fillColor2.getValue().getRed(), shaders.fillColor2.getValue().getGreen(), shaders.fillColor2.getValue().getBlue());
                effect.setUniformValue("fthird", shaders.fillColor3.getValue().getRed(), shaders.fillColor3.getValue().getGreen(), shaders.fillColor3.getValue().getBlue());
                effect.setUniformValue("oct", (int) Math.floor(shaders.octaves.getValue()));
                effect.setUniformValue("resolution", (float) client.getWindow().getScaledWidth(), (float) client.getWindow().getScaledHeight());
                effect.setUniformValue("time", time);
                effect.render(client.getTickDelta());
                time += 0.008f;
            } else if (shader == Shader.Default) {
                effect.setUniformValue("alpha0", shaders.glow.getValue() ? -1.0f : shaders.outlineColor.getValue().getAlpha() / 255.0f);
                effect.setUniformValue("lineWidth", (int) Math.floor(shaders.lineWidth.getValue()));
                effect.setUniformValue("quality", (int) Math.floor(shaders.quality.getValue()));
                effect.setUniformValue("color", shaders.fillColor1.getValue().getRed(), shaders.fillColor1.getValue().getGreen(), shaders.fillColor1.getValue().getBlue(), shaders.fillColor1.getValue().getAlpha());
                effect.setUniformValue("outlinecolor", shaders.outlineColor.getValue().getRed(), shaders.outlineColor.getValue().getGreen(), shaders.outlineColor.getValue().getBlue(), shaders.outlineColor.getValue().getAlpha());
                effect.render(client.getTickDelta());
            } else if (shader == Shader.Snow) {
                effect.setUniformValue("color", shaders.fillColor1.getValue().getRed(), shaders.fillColor1.getValue().getGreen(), shaders.fillColor1.getValue().getBlue(), shaders.fillColor1.getValue().getAlpha());
                effect.setUniformValue("quality", (int) Math.floor(shaders.quality.getValue()));
                effect.setUniformValue("resolution", (float) client.getWindow().getScaledWidth(), (float) client.getWindow().getScaledHeight());
                effect.setUniformValue("time", time);
                effect.render(client.getTickDelta());
                time += 0.008f;
            }
        } catch (Exception e) {
            CoffeeMain.log(Level.ERROR, "Failed to setup shader %s, %s".formatted(effect.getShaderEffect().getName(), e.getStackTrace()));
        }
    }

    public void reloadShaders() {
        DEFAULT = ShaderEffectManager.getInstance().manage(new Identifier("coffee", "shaders/post/outline.json"));
        SMOKE = ShaderEffectManager.getInstance().manage(new Identifier("coffee", "shaders/post/smoke.json"));
        GRADIENT = ShaderEffectManager.getInstance().manage(new Identifier("coffee", "shaders/post/gradient.json"));
        SNOW = ShaderEffectManager.getInstance().manage(new Identifier("coffee", "shaders/post/snow.json"));

        DEFAULT_OUTLINE = ShaderEffectManager.getInstance().manage(new Identifier("coffee", "shaders/post/outline.json"), managedShaderEffect -> {
            PostEffectProcessor effect = managedShaderEffect.getShaderEffect();
            if (effect == null) return;

            ((ShaderEffectDuck) effect).coffee_addFakeTarget("bufIn", client.worldRenderer.getEntityOutlinesFramebuffer());
            ((ShaderEffectDuck) effect).coffee_addFakeTarget("bufOut", client.worldRenderer.getEntityOutlinesFramebuffer());
        });

        SMOKE_OUTLINE = ShaderEffectManager.getInstance().manage(new Identifier("coffee", "shaders/post/smoke.json"), managedShaderEffect -> {
            PostEffectProcessor effect = managedShaderEffect.getShaderEffect();
            if (effect == null) return;

            ((ShaderEffectDuck) effect).coffee_addFakeTarget("bufIn", client.worldRenderer.getEntityOutlinesFramebuffer());
            ((ShaderEffectDuck) effect).coffee_addFakeTarget("bufOut", client.worldRenderer.getEntityOutlinesFramebuffer());
        });

        GRADIENT_OUTLINE = ShaderEffectManager.getInstance().manage(new Identifier("coffee", "shaders/post/gradient.json"), managedShaderEffect -> {
            PostEffectProcessor effect = managedShaderEffect.getShaderEffect();
            if (effect == null) return;

            ((ShaderEffectDuck) effect).coffee_addFakeTarget("bufIn", client.worldRenderer.getEntityOutlinesFramebuffer());
            ((ShaderEffectDuck) effect).coffee_addFakeTarget("bufOut", client.worldRenderer.getEntityOutlinesFramebuffer());
        });

        SNOW_OUTLINE = ShaderEffectManager.getInstance().manage(new Identifier("coffee", "shaders/post/snow.json"), managedShaderEffect -> {
            PostEffectProcessor effect = managedShaderEffect.getShaderEffect();
            if (effect == null) return;

            ((ShaderEffectDuck) effect).coffee_addFakeTarget("bufIn", client.worldRenderer.getEntityOutlinesFramebuffer());
            ((ShaderEffectDuck) effect).coffee_addFakeTarget("bufOut", client.worldRenderer.getEntityOutlinesFramebuffer());
        });
    }

    public static class CoffeeFramebuffer extends Framebuffer {
        public CoffeeFramebuffer(int width, int height) {
            super(false);
            RenderSystem.assertOnRenderThreadOrInit();
            resize(width, height, true);
            setClearColor(0f, 0f, 0f, 0f);
        }
    }

    public boolean fullNullCheck() {
        if (GRADIENT == null || SMOKE == null || DEFAULT == null) {
            shaderBuffer = new CoffeeFramebuffer(client.getFramebuffer().textureWidth, client.getFramebuffer().textureHeight);
            reloadShaders();
            return true;
        }

        return false;
    }

    public record RenderTask(Runnable task, Shader shader) {
    }

    public enum Shader {
        Default,
        Smoke,
        Gradient,
        Snow
    }
}