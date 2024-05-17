package coffee.client.feature.module.impl.render;

import coffee.client.feature.config.BooleanSetting;
import coffee.client.feature.config.DoubleSetting;
import coffee.client.feature.config.EnumSetting;
import coffee.client.feature.module.Module;
import coffee.client.feature.module.ModuleType;
import coffee.client.helper.font.FontRenderers;
import coffee.client.helper.render.AnimationUtils;
import coffee.client.helper.render.Renderer;
import coffee.client.helper.util.Utils;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RotationAxis;

import java.awt.*;
import java.util.HashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static coffee.client.helper.render.Renderer.R2D.*;

public class HitParticles extends Module {
    final EnumSetting<Mode> mode = this.config.create(new EnumSetting.Builder<Mode>(Mode.Stars).name("Mode").description("Shape mode").get());
    final EnumSetting<Physics> physics = this.config.create(new EnumSetting.Builder<Physics>(Physics.Fall).name("Physics").description("How the shapes move").get());
    final BooleanSetting selfp = this.config.create(new BooleanSetting.Builder(false).name("Self").get());
    final BooleanSetting entities = this.config.create(new BooleanSetting.Builder(false).name("Entities").description("Whether or not to display particles when hitting an entity.").get());
    final DoubleSetting amount = this.config.create(new DoubleSetting.Builder(2).name("Amount").precision(1).min(1).max(10).get());
    final DoubleSetting lifeTime = this.config.create(new DoubleSetting.Builder(2).name("Life time").precision(1).min(1).max(10).get());
    final DoubleSetting speed = this.config.create(new DoubleSetting.Builder(2).name("Speed").precision(1).min(1).max(20).get());
    final DoubleSetting starsScale = this.config.create(new DoubleSetting.Builder(3).name("Stars Scale").precision(1).min(1).max(10).get());
    final EnumSetting<ColorMode> colorMode = this.config.create(new EnumSetting.Builder<>(ColorMode.Single).name("ColorMode").get());

    public HitParticles() {
        super("HitParticles", "Particles on hit", ModuleType.RENDER);
    }

    private final HashMap<Integer, Float> healthMap = new HashMap<>();
    private final CopyOnWriteArrayList<Particle> particles = new CopyOnWriteArrayList<>();
    private final Color colorH = new Color(3142544);
    private final Color colorD = new Color(15811379);

    @Override
    public void tick() {
        particles.removeIf(Particle::update);

        if (mode.getValue() == Mode.Text) {
            for (Entity entity : client.world.getEntities()) {
                if (entity == null || client.player.squaredDistanceTo(entity) > 256f || !entity.isAlive() || !(entity instanceof LivingEntity lent))
                    continue;

                Color c = switch (colorMode.getValue()) {
                    case Theme -> Themes.getCurrentTheme().getSecondary();
                    case LightRainbow -> Renderer.R2D.rainbow(18, (int) Utils.random2(1, 228), .6f, 1, 1);
                    case Rainbow -> Renderer.R2D.rainbow(18, (int) Utils.random2(1, 228), 1f, 1, 1);
                    case Fade -> Renderer.R2D.fade(18, (int) Utils.random2(1, 228), new Color(-6974059), 1);
                    case Single -> new Color(0x8800FF00);
                };
                float health = lent.getHealth() + lent.getAbsorptionAmount();
                float lastHealth = healthMap.getOrDefault(entity.getId(), health);
                healthMap.put(entity.getId(), health);
                if (lastHealth == health)
                    continue;

                particles.add(new Particle((float) lent.getX(), Utils.random2((float) (lent.getY() + lent.getHeight()), (float) lent.getY()), (float) lent.getZ(), c,
                        Utils.random2(0, 180), Utils.random2(10f, 60f), health - lastHealth));
            }
            return;
        }

        for (PlayerEntity player : client.world.getPlayers()) {
            if (!selfp.getValue() && player == client.player) continue;
            if (player.hurtTime > 0) {
                Color c = switch (colorMode.getValue()) {
                    case Theme -> Themes.getCurrentTheme().getSecondary();
                    case LightRainbow -> Renderer.R2D.rainbow(18, (int) Utils.random2(1, 228), .6f, 1, 1);
                    case Rainbow -> Renderer.R2D.rainbow(18, (int) Utils.random2(1, 228), 1f, 1, 1);
                    case Fade -> Renderer.R2D.fade(18, (int) Utils.random2(1, 228), new Color(-6974059), 1);
                    case Single -> new Color(0x8800FF00);
                };
                for (int i = 0; i < amount.getValue(); i++) {
                    particles.add(new Particle((float) player.getX(), Utils.random2((float) (player.getY() + player.getHeight()), (float) player.getY()), (float) player.getZ(), c, Utils.random2(0, 180), Utils.random2(10f, 60f), 0));
                }
            }
        }
    }

    @Override
    public void enable() {

    }

    @Override
    public void disable() {

    }

    @Override
    public String getContext() {
        return "";
    }

    @Override
    public void onWorldRender(MatrixStack stack) {
        RenderSystem.disableDepthTest();
        if (client.player != null && client.world != null) {
            for (Particle particle : particles) {
                particle.render(stack);
            }
        }
        RenderSystem.enableDepthTest();
    }

    @Override
    public void onHudRender() {

    }

    public class Particle {
        float x;
        float y;
        float z;

        float px;
        float py;
        float pz;

        float motionX;
        float motionY;
        float motionZ;

        float rotationAngle;
        float rotationSpeed;
        float health;

        long time;
        Color color;

        public Particle(float x, float y, float z, Color color, float rotationAngle, float rotationSpeed, float health) {
            this.x = x;
            this.y = y;
            this.z = z;
            px = x;
            py = y;
            pz = z;
            motionX = Utils.random2(-Float.parseFloat(String.valueOf(speed.getValue())) / 50f, Float.parseFloat(String.valueOf(speed.getValue())) / 50f);
            motionY = Utils.random2(-Float.parseFloat(String.valueOf(speed.getValue())) / 50f, Float.parseFloat(String.valueOf(speed.getValue())) / 50f);
            motionZ = Utils.random2(-Float.parseFloat(String.valueOf(speed.getValue())) / 50f, Float.parseFloat(String.valueOf(speed.getValue())) / 50f);
            time = System.currentTimeMillis();
            this.color = color;
            this.rotationAngle = rotationAngle;
            this.rotationSpeed = rotationSpeed;
            this.health = health;
        }

        public long getTime() {
            return time;
        }

        public boolean update() {
            double sp = Math.sqrt(motionX * motionX + motionZ * motionZ);
            px = x;
            py = y;
            pz = z;

            x += motionX;
            y += motionY;
            z += motionZ;

            if (posBlock(x, y - starsScale.getValue() / 10f, z)) {
                motionY = -motionY / 1.1f;
                motionX = motionX / 1.1f;
                motionZ = motionZ / 1.1f;
            } else {
                if (posBlock(x - sp, y, z - sp)
                        || posBlock(x + sp, y, z + sp)
                        || posBlock(x + sp, y, z - sp)
                        || posBlock(x - sp, y, z + sp)
                        || posBlock(x + sp, y, z)
                        || posBlock(x - sp, y, z)
                        || posBlock(x, y, z + sp)
                        || posBlock(x, y, z - sp)
                ) {
                    motionX = -motionX;
                    motionZ = -motionZ;
                }
            }

            if (physics.getValue() == Physics.Fall)
                motionY -= 0.035f;

            motionX /= 1.005f;
            motionZ /= 1.005f;
            motionY /= 1.005f;

            return System.currentTimeMillis() - getTime() > lifeTime.getValue() * 1000;
        }

        public void render(MatrixStack matrixStack) {
            float size = Float.parseFloat(String.valueOf(starsScale.getValue()));
            float scale = mode.getValue() == Mode.Text ? 0.025f * size : 0.07f;

            final double posX = Renderer.R2D.interpolate(px, x, client.getTickDelta()) - client.getEntityRenderDispatcher().camera.getPos().getX();
            final double posY = Renderer.R2D.interpolate(py, y, client.getTickDelta()) + 0.1 - client.getEntityRenderDispatcher().camera.getPos().getY();
            final double posZ = Renderer.R2D.interpolate(pz, z, client.getTickDelta()) - client.getEntityRenderDispatcher().camera.getPos().getZ();

            matrixStack.push();
            matrixStack.translate(posX, posY, posZ);

            matrixStack.scale(scale, scale, scale);

            matrixStack.translate(size / 2, size / 2, size / 2);
            matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-client.gameRenderer.getCamera().getYaw()));
            matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(client.gameRenderer.getCamera().getPitch()));

            if (mode.getValue() == Mode.Text)
                matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180));
            else
                matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(rotationAngle += (float) (AnimationUtils.deltaTime() * rotationSpeed)));

            matrixStack.translate(-size / 2, -size / 2, -size / 2);

            switch (mode.getValue()) {
                case Orbiz -> {
                    drawOrbiz(matrixStack, 0.0f, 0.3, color);
                    drawOrbiz(matrixStack, -0.1f, 0.5, color);
                    drawOrbiz(matrixStack, -0.2f, 0.7, color);
                }
                case Stars -> drawStar(matrixStack, color, size);
                case Hearts -> drawHeart(matrixStack, color, size);
                case Bloom -> drawBloom(matrixStack, color, size);
                case Text ->
                        FontRenderers.getRenderer().drawCenteredString(matrixStack, Utils.round(health) + " ", 0, 0, (health > 0 ? colorH : colorD).getRGB());
            }

            matrixStack.scale(0.8f, 0.8f, 0.8f);
            matrixStack.pop();
        }

        private boolean posBlock(double x, double y, double z) {
            Block b = client.world.getBlockState(BlockPos.ofFloored(x, y, z)).getBlock();
            return (!(b instanceof AirBlock) && b != Blocks.WATER && b != Blocks.LAVA);
        }
    }

    public enum Physics {
        Fall, Fly
    }

    public enum Mode {
        Orbiz, Stars, Hearts, Bloom, Text
    }

    public enum ColorMode {
        Theme,
        LightRainbow,
        Rainbow,
        Fade,
        Single
    }
}
