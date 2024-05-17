package coffee.client.feature.module.impl.render;

import coffee.client.feature.config.BooleanSetting;
import coffee.client.feature.config.DoubleSetting;
import coffee.client.feature.config.EnumSetting;
import coffee.client.feature.module.Module;
import coffee.client.feature.module.ModuleRegistry;
import coffee.client.feature.module.ModuleType;
import coffee.client.helper.event.EventSystem;
import coffee.client.helper.event.impl.HeldItemRenderEvent;
import coffee.client.helper.event.impl.PacketEvent;
import coffee.client.mixin.render.IHeldItemRenderer;
import me.x150.jmessenger.MessageSubscription;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.jetbrains.annotations.NotNull;

public class Animations extends Module {
    public Animations() {
        super("Animations", "Animations on items", ModuleType.RENDER);
    }
    
    public final BooleanSetting oldAnimationsM = this.config.create(new BooleanSetting.Builder(true).name("Disable swap main").get());
    public final BooleanSetting oldAnimationsOff = this.config.create(new BooleanSetting.Builder(true).name("Disable swap off").get());
    public final EnumSetting<Mode> mode = this.config.create(new EnumSetting.Builder<>(Mode.Default).name("Mode").get());
    public final BooleanSetting slowAnimation = this.config.create(new BooleanSetting.Builder(true).name("Slow animation").get());
    public final DoubleSetting slowAnimationVal = this.config.create(new DoubleSetting.Builder(12).name("Slow value").min(1).max(50).get());
    

    public boolean flip;
    private ViewModel vm;
    
    public enum Mode {
        Default, One, Two, Three, Four, Five, Six, Seven, Eight, Nine, Ten, Eleven, Twelve, Thirteen, Fourteen
    }


    @Override
    public void tick() {
        if (oldAnimationsM.getValue() && ((IHeldItemRenderer) client.getEntityRenderDispatcher().getHeldItemRenderer()).getEquippedProgressMainHand() <= 1f) {
            ((IHeldItemRenderer) client.getEntityRenderDispatcher().getHeldItemRenderer()).setEquippedProgressMainHand(1f);
            ((IHeldItemRenderer) client.getEntityRenderDispatcher().getHeldItemRenderer()).setItemStackMainHand(client.player.getMainHandStack());
        }

        if (oldAnimationsOff.getValue() && ((IHeldItemRenderer) client.getEntityRenderDispatcher().getHeldItemRenderer()).getEquippedProgressOffHand() <= 1f) {
            ((IHeldItemRenderer) client.getEntityRenderDispatcher().getHeldItemRenderer()).setEquippedProgressOffHand(1f);
            ((IHeldItemRenderer) client.getEntityRenderDispatcher().getHeldItemRenderer()).setItemStackOffHand(client.player.getOffHandStack());
        }
    }

    @Override
    public void enable() {
        vm = ModuleRegistry.getByClass(ViewModel.class);
    }

    @Override
    public void disable() {

    }

    @Override
    public String getContext() {
        return "";
    }

    @Override
    public void onWorldRender(MatrixStack matrices) {

    }

    @Override
    public void onHudRender() {

    }

    @MessageSubscription
    public void onPacketSend(PacketEvent.Received e) {
        if (e.getPacket() instanceof HandSwingC2SPacket)
            flip = !flip;
    }

    private void renderSwordAnimation(MatrixStack matrices, float f, float swingProgress, float equipProgress, Arm arm) {
        if (arm == Arm.LEFT && (mode.getValue() == Mode.Eleven || mode.getValue() == Mode.Ten || mode.getValue() == Mode.Nine || mode.getValue() == Mode.Three || mode.getValue() == Mode.Thirteen)) {
            applyEquipOffset(matrices, arm, equipProgress);
            matrices.translate(-vm.positionMainX.getValue(), vm.positionMainY.getValue(), vm.positionMainZ.getValue());
            applySwingOffset(matrices, arm, swingProgress);
            matrices.translate(vm.positionMainX.getValue(), -vm.positionMainY.getValue(), -vm.positionMainZ.getValue());
            return;
        }

        switch (mode.getValue()) {
            case Default -> {
                applyEquipOffset(matrices, arm, equipProgress);
                translateToViewModelOff(matrices);
                applySwingOffset(matrices, arm, swingProgress);
                translateBacklOff(matrices);
            }
            case One -> {
                float n = -0.4F * MathHelper.sin(MathHelper.sqrt(swingProgress) * 3.1415927F);
                applyEquipOffset(matrices, arm, n);
                int i = arm == Arm.RIGHT ? 1 : -1;
                translateToViewModel(matrices);
                float f1 = MathHelper.sin(swingProgress * swingProgress * 3.1415927F);
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float) i * (45.0F + f1 * -20.0F)));
                float g = MathHelper.sin(MathHelper.sqrt(swingProgress) * 3.1415927F);
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float) i * g * -20.0F));
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(g * 0.0F));
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float) i * -45.0F));
                translateBack(matrices);
            }
            case Two -> {
                applyEquipOffset(matrices, arm, 0.2F * MathHelper.sin(MathHelper.sqrt(swingProgress) * 6.2831855F));
            }
            case Three -> {
                float n = -0.4F * MathHelper.sin(MathHelper.sqrt(swingProgress) * 3.1415927F);
                float g = MathHelper.sin(MathHelper.sqrt(swingProgress) * 3.1415927F);
                applyEquipOffset(matrices, arm, n);
                int i = arm == Arm.RIGHT ? 1 : -1;
                translateToViewModel(matrices);
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float) i * (45.0F + f * -20.0F)));
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float) i * g * -70.0F));
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-70f));
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float) i * -45.0F));
                translateBack(matrices);
            }
            case Four -> {
                applyEquipOffset(matrices, arm, 0);
                translateToViewModel(matrices);
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(swingProgress > 0 ? -MathHelper.sin(swingProgress * 13f) * 37f : 0));
                translateBack(matrices);
            }
            case Five -> {
                applyEquipOffset(matrices, arm, 0);
                int i = arm == Arm.RIGHT ? 1 : -1;
                float g = MathHelper.sin(MathHelper.sqrt(swingProgress) * 3.1415927F);
                translateToViewModel(matrices);
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float) i * g * -20.0F));
                translateBack(matrices);
            }
            case Six -> {
                applyEquipOffset(matrices, arm, equipProgress);
                translateToViewModel(matrices);
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(swingProgress * (flip ? 360.0F : -360)));
                translateBack(matrices);
            }
            case Eight -> {
                applyEquipOffset(matrices, arm, equipProgress);
                translateToViewModel(matrices);
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(swingProgress * -360));
                translateBack(matrices);
            }
            case Seven -> {
                applyEquipOffset(matrices, arm, equipProgress);
                float a = -MathHelper.sin(swingProgress * 3f) / 2f + 1f;
                matrices.scale(a, a, a);
            }
            case Nine -> {
                float g = MathHelper.sin(MathHelper.sqrt(swingProgress) * 3.1415927F);
                applyEquipOffset(matrices, arm, 0);
                translateToViewModel(matrices);
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(50f));
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-30f * (1f - g) - 30f));
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(110f));
                translateBack(matrices);
            }
            case Ten -> {
                float g = MathHelper.sin(MathHelper.sqrt(swingProgress) * 3.1415927F);
                matrices.translate(0, 0, 0);
                applyEquipOffset(matrices, arm, 0);
                translateToViewModel(matrices);
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(50f));
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-60f * g - 50));
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(110f));
                translateBack(matrices);
            }
            case Eleven -> {
                float g = MathHelper.sin(MathHelper.sqrt(swingProgress) * 3.1415927F);
                applyEquipOffset(matrices, arm, 0);
                translateToViewModel(matrices);
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(50f));
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-60f));
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(110f + 20f * g));
                translateBack(matrices);
            }
            case Twelve -> {
                float g = MathHelper.sin(MathHelper.sqrt(swingProgress) * 3.1415927F);
                applyEquipOffset(matrices, arm, 0);
                matrices.translate(0, 0, -g / 4f);
                translateToViewModel(matrices);
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-120f));
                translateBack(matrices);
            }
            case Thirteen -> {
                float g = MathHelper.sin(MathHelper.sqrt(swingProgress) * 3.1415927F);
                applyEquipOffset(matrices, arm, 0);
                translateToViewModel(matrices);
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-MathHelper.sin(swingProgress * 3f) * 60f));
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-60f * g));
                translateBack(matrices);
            }
            case Fourteen ->{
                if(swingProgress > 0) {
                    float g = MathHelper.sin(MathHelper.sqrt(swingProgress) *  (float) Math.PI);
                    matrices.translate(0.56F, equipProgress * -0.2f - 0.5F, -0.7F);
                    translateToViewModel(matrices);
                    matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(45));
                    matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(g * -85.0F));
                    matrices.translate(-0.1F, 0.28F, 0.2F);
                    matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-85.0F));
                    translateBack(matrices);
                } else {
                    float n = -0.4f * MathHelper.sin(MathHelper.sqrt(swingProgress) * (float) Math.PI);
                    float m = 0.2f * MathHelper.sin(MathHelper.sqrt(swingProgress) * ((float) Math.PI * 2));
                    float f1 = -0.2f * MathHelper.sin(swingProgress * (float) Math.PI);
                    matrices.translate(n, m, f1);
                    applyEquipOffset(matrices, arm, equipProgress);
                    applySwingOffset(matrices, arm, swingProgress);
                }
            }
        }
    }


    public void renderFirstPersonIteclientustom(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        if (!player.isUsingSpyglass()) {
            boolean bl = hand == Hand.MAIN_HAND;
            Arm arm = bl ? player.getMainArm() : player.getMainArm().getOpposite();
            matrices.push();

            boolean bl2;
            float f = 0;
            float g;
            float h;
            float j;
            if (item.isOf(Items.CROSSBOW)) {
                bl2 = CrossbowItem.isCharged(item);
                boolean bl3 = arm == Arm.RIGHT;
                int i = bl3 ? 1 : -1;
                if (player.isUsingItem() && player.getItemUseTimeLeft() > 0 && player.getActiveHand() == hand) {
                    applyEquipOffset(matrices, arm, equipProgress);
                    matrices.translate((float) i * -0.4785682F, -0.094387F, 0.05731531F);
                    matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-11.935F));
                    matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float) i * 65.3F));
                    matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float) i * -9.785F));
                    f = (float) item.getMaxUseTime() - ((float) client.player.getItemUseTimeLeft() - tickDelta + 1.0F);
                    g = f / (float) CrossbowItem.getPullTime(item);
                    if (g > 1.0F) {
                        g = 1.0F;
                    }

                    if (g > 0.1F) {
                        h = MathHelper.sin((f - 0.1F) * 1.3F);
                        j = g - 0.1F;
                        float k = h * j;
                        matrices.translate(k * 0.0F, k * 0.004F, k * 0.0F);
                    }

                    matrices.translate(g * 0.0F, g * 0.0F, g * 0.04F);
                    matrices.scale(1.0F, 1.0F, 1.0F + g * 0.2F);
                    matrices.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees((float) i * 45.0F));
                } else {
                    f = -0.4F * MathHelper.sin(MathHelper.sqrt(swingProgress) * 3.1415927F);
                    g = 0.2F * MathHelper.sin(MathHelper.sqrt(swingProgress) * 6.2831855F);
                    h = -0.2F * MathHelper.sin(swingProgress * 3.1415927F);
                    matrices.translate((float) i * f, g, h);
                    applyEquipOffset(matrices, arm, equipProgress);
                    applySwingOffset(matrices, arm, swingProgress);
                    if (bl2 && swingProgress < 0.001F && bl) {
                        matrices.translate((float) i * -0.641864F, 0.0F, 0.0F);
                        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float) i * 10.0F));
                    }
                }

                HeldItemRenderEvent event = new HeldItemRenderEvent(hand, item, equipProgress, matrices);
                EventSystem.manager.send(event);
                renderItem(player, item, bl3 ? ModelTransformationMode.FIRST_PERSON_RIGHT_HAND : ModelTransformationMode.FIRST_PERSON_LEFT_HAND, !bl3, matrices, vertexConsumers, light);
            } else {
                bl2 = arm == Arm.RIGHT;
                int l;
                float m = 0;
                if (player.isUsingItem() && player.getItemUseTimeLeft() > 0 && player.getActiveHand() == hand) {
                    l = bl2 ? 1 : -1;
                    switch (item.getUseAction()) {
                        case NONE, BLOCK -> applyEquipOffset(matrices, arm, equipProgress);
                        case EAT, DRINK -> {
                            applyEatOrDrinkTransformationCustom(matrices, tickDelta, arm, item);
                            applyEquipOffset(matrices, arm, equipProgress);
                        }
                        case BOW -> {
                            applyEquipOffset(matrices, arm, equipProgress);
                            matrices.translate((float) l * -0.2785682F, 0.18344387F, 0.15731531F);
                            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-13.935F));
                            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float) l * 35.3F));
                            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float) l * -9.785F));
                            m = (float) item.getMaxUseTime() - ((float) client.player.getItemUseTimeLeft() - tickDelta + 1.0F);
                            f = m / 20.0F;
                            f = (f * f + f * 2.0F) / 3.0F;
                            if (f > 1.0F) {
                                f = 1.0F;
                            }
                            if (f > 0.1F) {
                                g = MathHelper.sin((m - 0.1F) * 1.3F);
                                h = f - 0.1F;
                                j = g * h;
                                matrices.translate(j * 0.0F, j * 0.004F, j * 0.0F);
                            }
                            matrices.translate(f * 0.0F, f * 0.0F, f * 0.04F);
                            matrices.scale(1.0F, 1.0F, 1.0F + f * 0.2F);
                            matrices.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees((float) l * 45.0F));
                        }
                        case SPEAR -> {
                            applyEquipOffset(matrices, arm, equipProgress);
                            matrices.translate((float) l * -0.5F, 0.7F, 0.1F);
                            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-55.0F));
                            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float) l * 35.3F));
                            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float) l * -9.785F));
                            m = (float) item.getMaxUseTime() - ((float) client.player.getItemUseTimeLeft() - tickDelta + 1.0F);
                            f = m / 10.0F;
                            if (f > 1.0F) {
                                f = 1.0F;
                            }
                            if (f > 0.1F) {
                                g = MathHelper.sin((m - 0.1F) * 1.3F);
                                h = f - 0.1F;
                                j = g * h;
                                matrices.translate(j * 0.0F, j * 0.004F, j * 0.0F);
                            }
                            matrices.translate(0.0F, 0.0F, f * 0.2F);
                            matrices.scale(1.0F, 1.0F, 1.0F + f * 0.2F);
                            matrices.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees((float) l * 45.0F));
                        }
                        case BRUSH -> applyBrushTransformation(matrices, tickDelta, arm, item, equipProgress);
                    }
                } else if (player.isUsingRiptide()) {
                    applyEquipOffset(matrices, arm, equipProgress);
                    l = bl2 ? 1 : -1;
                    matrices.translate((float) l * -0.4F, 0.8F, 0.3F);
                    matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float) l * 65.0F));
                    matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float) l * -85.0F));
                } else {
                    renderSwordAnimation(matrices, f, swingProgress, equipProgress, arm);
                }
                HeldItemRenderEvent event = new HeldItemRenderEvent(hand, item, equipProgress, matrices);
                EventSystem.manager.send(event);
                renderItem(player, item, bl2 ? ModelTransformationMode.FIRST_PERSON_RIGHT_HAND : ModelTransformationMode.FIRST_PERSON_LEFT_HAND, !bl2, matrices, vertexConsumers, light);
            }
            matrices.pop();
        }
    }

    private void applyBrushTransformation(MatrixStack matrices, float tickDelta, Arm arm, @NotNull ItemStack stack, float equipProgress) {
        applyEquipOffset(matrices, arm, equipProgress);
        float f = (float) client.player.getItemUseTimeLeft() - tickDelta + 1.0F;
        float g = 1.0F - f / (float) stack.getMaxUseTime();
        float m = -15.0F + 75.0F * MathHelper.cos(g * 45.0F * 3.1415927F);

        if (arm != Arm.RIGHT) {
            matrices.translate(0.1, 0.83, 0.35);
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-80.0F));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-90.0F));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(m));
            matrices.translate(-0.3, 0.22, 0.35);
        } else {
            matrices.translate(-0.25, 0.22, 0.35);
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-80.0F));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90.0F));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(0.0F));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(m));
        }
    }

    private void applyEquipOffset(@NotNull MatrixStack matrices, Arm arm, float equipProgress) {
        int i = arm == Arm.RIGHT ? 1 : -1;
        matrices.translate((float) i * 0.56F, -0.52F + equipProgress * -0.6F, -0.72F);
    }

    private void applySwingOffset(@NotNull MatrixStack matrices, Arm arm, float swingProgress) {
        int i = arm == Arm.RIGHT ? 1 : -1;
        float f = MathHelper.sin(swingProgress * swingProgress * 3.1415927F);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float) i * (45.0F + f * -20.0F)));
        float g = MathHelper.sin(MathHelper.sqrt(swingProgress) * 3.1415927F);
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float) i * g * -20.0F));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(g * -80.0F));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float) i * -45.0F));
    }

    public void renderItem(LivingEntity entity, ItemStack stack, ModelTransformationMode renderMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        if (stack.isEmpty()) {
            return;
        }
        client.getItemRenderer().renderItem(entity, stack, renderMode, leftHanded, matrices, vertexConsumers, entity.getWorld(), light, OverlayTexture.DEFAULT_UV, entity.getId() + renderMode.ordinal());
    }

    private void applyEatOrDrinkTransformationCustom(MatrixStack matrices, float tickDelta, Arm arm, @NotNull ItemStack stack) {
        float f = (float) client.player.getItemUseTimeLeft() - tickDelta + 1.0F;
        float g = f / (float) stack.getMaxUseTime();
        float h;
        if (g < 0.8F) {
            h = MathHelper.abs(MathHelper.cos(f / 4.0F * 3.1415927F) * 0.005F);
            matrices.translate(0.0F, h, 0.0F);
        }
        h = 1.0F - (float) Math.pow(g, 27.0);
        int i = arm == Arm.RIGHT ? 1 : -1;

        matrices.translate(h * 0.6F * (float) i * vm.eatX.getValue(), h * -0.5F * vm.eatY.getValue(), h * 0.0F);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float) i * h * 90.0F));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(h * 10.0F));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float) i * h * 30.0F));
    }

    private void translateToViewModel(MatrixStack matrices) {
        if (vm.isEnabled())
            matrices.translate(vm.positionMainX.getValue(), vm.positionMainY.getValue(), vm.positionMainZ.getValue());
    }

    private void translateToViewModelOff(MatrixStack matrices) {
        if (vm.isEnabled())
            matrices.translate(-vm.positionMainX.getValue(), vm.positionMainY.getValue(), vm.positionMainZ.getValue());
    }

    private void translateBack(MatrixStack matrices) {
        if (vm.isEnabled())
            matrices.translate(-vm.positionMainX.getValue(), -vm.positionMainY.getValue(), -vm.positionMainZ.getValue());
    }

    private void translateBacklOff(MatrixStack matrices) {
        if (vm.isEnabled())
            matrices.translate(vm.positionMainX.getValue(), -vm.positionMainY.getValue(), -vm.positionMainZ.getValue());
    }
}