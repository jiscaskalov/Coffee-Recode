package coffee.client.feature.module.impl.render;

import coffee.client.feature.config.BooleanSetting;
import coffee.client.feature.config.DoubleSetting;
import coffee.client.feature.module.Module;
import coffee.client.feature.module.ModuleType;
import coffee.client.helper.event.impl.PacketEvent;
import me.x150.jmessenger.MessageSubscription;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;

public class WorldTweaks extends Module {
    public WorldTweaks() {
        super("WorldTweaks", "Various world related tweaks", ModuleType.RENDER);
    }

    public final BooleanSetting fogModify = this.config.create(new BooleanSetting.Builder(true).name("Fog modify").description("Modifies the fog of the world.").get());
    public final DoubleSetting fogEnd = this.config.create(new DoubleSetting.Builder(64).name("Fog end").min(10).max(256).precision(1).get());
    public final DoubleSetting fogStart = this.config.create(new DoubleSetting.Builder(0).name("Fog start").min(0).max(256).precision(1).get());
    final BooleanSetting cTime = this.config.create(new BooleanSetting.Builder(false).name("Change time").description("Modifies the time of the world.").get());
    final DoubleSetting cTimeVal = this.config.create(new DoubleSetting.Builder(21).name("Time").min(0).max(23).precision(1).get());

    long oldTime;

    @Override
    public void enable() {
        oldTime = client.world.getTime();
    }

    @Override
    public void disable() {
        client.world.setTimeOfDay(oldTime);
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
    private void onPacketReceive(PacketEvent.Received event) {
        if (event.getPacket() instanceof WorldTimeUpdateS2CPacket && cTime.getValue()) {
            oldTime = ((WorldTimeUpdateS2CPacket) event.getPacket()).getTime();
            event.cancel();
        }
    }

    @Override
    public void tick() {
        if (cTime.getValue()) client.world.setTimeOfDay((int) (cTimeVal.getValue() * 1000));
    }
}
