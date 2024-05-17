package coffee.client.feature.module.impl.render;

import coffee.client.CoffeeMain;
import coffee.client.feature.config.BooleanSetting;
import coffee.client.feature.config.ColorSetting;
import coffee.client.feature.config.DoubleSetting;
import coffee.client.feature.config.EnumSetting;
import coffee.client.feature.module.Module;
import coffee.client.feature.module.ModuleType;
import coffee.client.helper.render.shader.ShaderManager;
import coffee.client.mixin.render.IGameRendererMixin;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;

import java.awt.*;

public class Shaders extends Module {
    public Shaders() {
        super("Shaders", "Cool shaders", ModuleType.RENDER);
    }

    //Thanks to @0x3C50 for Shader rendering example

    public final BooleanSetting hands = this.config.create(new BooleanSetting.Builder(true).name("Hands").get());
    public final BooleanSetting players = this.config.create(new BooleanSetting.Builder(true).name("Players").get());
    public final BooleanSetting self = this.config.create(new BooleanSetting.Builder(true).name("Self").get());
    public final BooleanSetting crystals = this.config.create(new BooleanSetting.Builder(true).name("Crystals").get());
    public final BooleanSetting creatures = this.config.create(new BooleanSetting.Builder(false).name("Creatures").get());
    public final BooleanSetting monsters = this.config.create(new BooleanSetting.Builder(false).name("Monsters").get());
    public final BooleanSetting ambients = this.config.create(new BooleanSetting.Builder(false).name("Ambients").get());
    public final BooleanSetting others = this.config.create(new BooleanSetting.Builder(false).name("Others").get());

    public final EnumSetting<ShaderManager.Shader> mode = this.config.create(new EnumSetting.Builder<>(ShaderManager.Shader.Default).name("Mode").get());
    public final EnumSetting<ShaderManager.Shader> handsMode = this.config.create(new EnumSetting.Builder<>(ShaderManager.Shader.Default).name("Hands Mode").get());

    public final DoubleSetting maxRange = this.config.create(new DoubleSetting.Builder(64).name("Max range").min(16).max(256).get());
    public final DoubleSetting factor = this.config.create(new DoubleSetting.Builder(2).name("Gradient Factor").min(0).max(20).get());
    public final DoubleSetting gradient = this.config.create(new DoubleSetting.Builder(2).name("Gradient").min(0).max(20).get());
    public final DoubleSetting alpha2 = this.config.create(new DoubleSetting.Builder(170).name("Gradient alpha").min(0).max(255).get());
    public final DoubleSetting lineWidth = this.config.create(new DoubleSetting.Builder(2).name("Line width").min(0).max(20).get());
    public final DoubleSetting quality = this.config.create(new DoubleSetting.Builder(3).name("Quality").min(0).max(20).get());
    public final DoubleSetting octaves = this.config.create(new DoubleSetting.Builder(10).name("Smoke octaves").min(5).max(30).get());
    public final DoubleSetting fillAlpha = this.config.create(new DoubleSetting.Builder(170).name("Fill alpha").min(0).max(255).get());
    public final BooleanSetting glow = this.config.create(new BooleanSetting.Builder(true).name("Smoke glow").get());

    public final ColorSetting outlineColor = this.config.create(new ColorSetting.Builder(new Color(0x8800FF00)).name("Outline").get());
    public final ColorSetting outlineColor1 = this.config.create(new ColorSetting.Builder(new Color(0x8800FF00)).name("Smoke outline").get());
    public final ColorSetting outlineColor2 = this.config.create(new ColorSetting.Builder(new Color(0x8800FF00)).name("Smoke outline 2").get());
    public final ColorSetting fillColor1 = this.config.create(new ColorSetting.Builder(new Color(0x8800FF00)).name("Fill").get());
    public final ColorSetting fillColor2 = this.config.create(new ColorSetting.Builder(new Color(0x8800FF00)).name("Smoke fill").get());
    public final ColorSetting fillColor3 = this.config.create(new ColorSetting.Builder(new Color(0x8800FF00)).name("Smoke fill 2").get());

    public boolean shouldRender(Entity entity) {
        if (entity == null)
            return false;

        if (client.player == null)
            return false;

        if (client.player.squaredDistanceTo(entity.getPos()) > (maxRange.getValue() * maxRange.getValue()))
            return false;

        if (entity instanceof PlayerEntity) {
            if (entity == client.player && !self.getValue())
                return false;
            return players.getValue();
        }

        if (entity instanceof EndCrystalEntity)
            return crystals.getValue();

        return switch (entity.getType().getSpawnGroup()) {
            case CREATURE, WATER_CREATURE -> creatures.getValue();
            case MONSTER -> monsters.getValue();
            case AMBIENT, WATER_AMBIENT -> ambients.getValue();
            default -> others.getValue();
        };
    }

    public static boolean rendering = false;

    @Override
    public void onWorldRender(MatrixStack matrices) {
        if (hands.getValue())
            CoffeeMain.SHADER_MANAGER.renderShader(() -> ((IGameRendererMixin) client.gameRenderer).irenderHand(client.gameRenderer.getCamera(), client.getTickDelta(), matrices.peek().getPositionMatrix()), handsMode.getValue());
    }

    @Override
    public void tick() {

    }

    @Override
    public void enable() {

    }

    @Override
    public void disable() {
        CoffeeMain.SHADER_MANAGER.reloadShaders();
    }

    @Override
    public String getContext() {
        return "";
    }

    @Override
    public void onHudRender() {

    }
}
