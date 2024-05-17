package coffee.client.feature.module.impl.render;

import coffee.client.feature.config.BooleanSetting;
import coffee.client.feature.config.DoubleSetting;
import coffee.client.feature.module.Module;
import coffee.client.feature.module.ModuleType;
import coffee.client.helper.event.impl.HeldItemRenderEvent;
import me.x150.jmessenger.MessageSubscription;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.RotationAxis;

public class ViewModel extends Module {
    public ViewModel() {
        super("ViewModel", "view model", ModuleType.RENDER);
    }

    public final DoubleSetting scale = this.config.create(new DoubleSetting.Builder(1).name("Scale").min(0.1).max(1.5).get());
    public final DoubleSetting positionMainX = this.config.create(new DoubleSetting.Builder(0).name("Position main X").min(-3).max(3).get());
    public final DoubleSetting positionMainY = this.config.create(new DoubleSetting.Builder(0).name("Position main Y").min(-3).max(3).get());
    public final DoubleSetting positionMainZ = this.config.create(new DoubleSetting.Builder(0).name("Position main Z").min(-3).max(3).get());
    public final DoubleSetting rotationMainX = this.config.create(new DoubleSetting.Builder(0).name("Rotation main X").min(-180).max(180).get());
    public final DoubleSetting rotationMainY = this.config.create(new DoubleSetting.Builder(0).name("Rotation main Y").min(-180).max(180).get());
    public final DoubleSetting rotationMainZ = this.config.create(new DoubleSetting.Builder(0).name("Rotation main Z").min(-180).max(180).get());
    public final BooleanSetting animateMainX = this.config.create(new BooleanSetting.Builder(false).name("Animate main X").get());
    public final BooleanSetting animateMainY = this.config.create(new BooleanSetting.Builder(false).name("Animate main Y").get());
    public final BooleanSetting animateMainZ = this.config.create(new BooleanSetting.Builder(false).name("Animate main Z").get());
    public final DoubleSetting speedAnimateMain = this.config.create(new DoubleSetting.Builder(1).name("Speed animate main").min(1).max(5).get());
    public final DoubleSetting rotationOffX = this.config.create(new DoubleSetting.Builder(0).name("Rotation off X").min(-180).max(180).get());
    public final DoubleSetting rotationOffY = this.config.create(new DoubleSetting.Builder(0).name("Rotation off Y").min(-180).max(180).get());
    public final DoubleSetting rotationOffZ = this.config.create(new DoubleSetting.Builder(0).name("Rotation main Z").min(-180).max(180).get());
    public final BooleanSetting animateOffX = this.config.create(new BooleanSetting.Builder(false).name("Animate off X").get());
    public final BooleanSetting animateOffY = this.config.create(new BooleanSetting.Builder(false).name("Animate off Y").get());
    public final BooleanSetting animateOffZ = this.config.create(new BooleanSetting.Builder(false).name("Animate off Z").get());
    public final DoubleSetting speedAnimateOff = this.config.create(new DoubleSetting.Builder(1).name("Speed animate off").min(1).max(5).get());
    public final DoubleSetting eatX = this.config.create(new DoubleSetting.Builder(1).name("Eat X").min(-1).max(2).get());
    public final DoubleSetting eatY = this.config.create(new DoubleSetting.Builder(1).name("Eat Y").min(-1).max(2).get());

    private double changeRotate(double value, double speed) {
        return value - speed <= 180 && value - speed > -180 ? value - speed : 180;
    }

    @MessageSubscription
    private void onHeldItemRender(HeldItemRenderEvent event) {
        if (event.getHand() == Hand.MAIN_HAND) {
            if (animateMainX.getValue())
                rotationMainX.setValue(changeRotate(rotationMainX.getValue(), speedAnimateMain.getValue()));
            if (animateMainY.getValue())
                rotationMainY.setValue(changeRotate(rotationMainY.getValue(), speedAnimateMain.getValue()));
            if (animateMainZ.getValue())
                rotationMainZ.setValue(changeRotate(rotationMainZ.getValue(), speedAnimateMain.getValue()));
            event.getStack().translate(positionMainX.getValue(), positionMainY.getValue(), positionMainZ.getValue());
            event.getStack().scale((float) Math.floor(scale.getValue()), (float) Math.floor(scale.getValue()), (float) Math.floor(scale.getValue()));
            event.getStack().multiply(RotationAxis.POSITIVE_X.rotationDegrees((float) Math.floor(rotationMainX.getValue())));
            event.getStack().multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float) Math.floor(rotationMainY.getValue())));
            event.getStack().multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float) Math.floor(rotationMainZ.getValue())));
        } else {
            if (animateOffX.getValue())
                rotationOffX.setValue(changeRotate(rotationOffX.getValue(), speedAnimateOff.getValue()));
            if (animateOffY.getValue())
                rotationOffY.setValue(changeRotate(rotationOffY.getValue(), speedAnimateOff.getValue()));
            if (animateOffZ.getValue())
                rotationOffZ.setValue(changeRotate(rotationOffZ.getValue(), speedAnimateOff.getValue()));
            event.getStack().translate(-positionMainX.getValue(), positionMainY.getValue(), positionMainZ.getValue());
            event.getStack().scale((float) Math.floor(scale.getValue()), (float) Math.floor(scale.getValue()), (float) Math.floor(scale.getValue()));
            event.getStack().multiply(RotationAxis.POSITIVE_X.rotationDegrees((float) Math.floor(rotationOffX.getValue())));
            event.getStack().multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float) Math.floor(rotationOffY.getValue())));
            event.getStack().multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float) Math.floor(rotationOffZ.getValue())));
        }
    }

    @Override
    public void tick() {

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
    public void onWorldRender(MatrixStack matrices) {

    }

    @Override
    public void onHudRender() {

    }
}