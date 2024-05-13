/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.feature.module.impl.movement;

import coffee.client.feature.config.BooleanSetting;
import coffee.client.feature.config.DoubleSetting;
import coffee.client.feature.config.EnumSetting;
import coffee.client.feature.config.annotation.VisibilitySpecifier;
import coffee.client.feature.module.Module;
import coffee.client.feature.module.ModuleType;
import coffee.client.helper.util.DamageUtils;
import com.google.common.collect.Streams;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.decoration.EndCrystalEntity;

import java.util.OptionalDouble;

public class Step extends Module {

    final DoubleSetting height = this.config.create(new DoubleSetting.Builder(3).name("Height").description("How high to step").min(1).max(50).precision(0).get());
    final EnumSetting activeWhen = this.config.create(new EnumSetting.Builder<ActiveWhen>(ActiveWhen.Always).name("active-when").description("Step is active when you meet these requirements.").get());
    final BooleanSetting safeStep = this.config.create(new BooleanSetting.Builder(false).name("safe-step").description("Doesn't let you step out of a hole if you are low on health or there is a crystal nearby.").get());
    @VisibilitySpecifier("safe-step")
    final DoubleSetting stepHealth = this.config.create(new DoubleSetting.Builder(5).name("step-health").description("The health you stop being able to step at.").min(1).max(36).get());

    public Step() {
        super("Step", "Allows you to step up full blocks", ModuleType.MOVEMENT);
    }

    private float prevStepHeight;
    private boolean prevPathManagerStep;

    @Override
    public void tick() {
        boolean work = (activeWhen.getValue() == ActiveWhen.Always) || (activeWhen.getValue() == ActiveWhen.Sneaking && client.player.isSneaking()) || (activeWhen.getValue() == ActiveWhen.NotSneaking && !client.player.isSneaking());
        client.player.setBoundingBox(client.player.getBoundingBox().offset(0, 1, 0));
        if (work && (!safeStep.getValue() || (getHealth() > stepHealth.getValue() && getHealth() - getExplosionDamage() > stepHealth.getValue()))){
            client.player.getAttributeInstance(EntityAttributes.GENERIC_STEP_HEIGHT).setBaseValue(height.getValue());
        } else {
            client.player.getAttributeInstance(EntityAttributes.GENERIC_STEP_HEIGHT).setBaseValue(prevStepHeight);
        }
        client.player.setBoundingBox(client.player.getBoundingBox().offset(0, -1, 0));
    }

    @Override
    public void enable() {
        prevStepHeight = client.player.getStepHeight();
    }

    @Override
    public void disable() {
        client.player.getAttributeInstance(EntityAttributes.GENERIC_STEP_HEIGHT).setBaseValue(prevStepHeight);
    }

    @Override
    public String getContext() {
        return null;
    }

    @Override
    public void onWorldRender(MatrixStack matrices) {

    }

    @Override
    public void onHudRender() {

    }

    private float getHealth(){
        return client.player.getHealth() + client.player.getAbsorptionAmount();
    }

    private double getExplosionDamage() {
        OptionalDouble crystalDamage = Streams.stream(client.world.getEntities())
                .filter(entity -> entity instanceof EndCrystalEntity)
                .filter(Entity::isAlive)
                .mapToDouble(entity -> DamageUtils.crystalDamage(client.player, entity.getPos()))
                .max();
        return crystalDamage.orElse(0.0);
    }

    public enum ActiveWhen {
        Always,
        Sneaking,
        NotSneaking
    }
}
