package coffee.client.feature.module.impl.render;

import coffee.client.CoffeeMain;
import coffee.client.feature.module.Module;
import coffee.client.feature.module.ModuleType;
import coffee.client.helper.config.ConfigContainer;
import coffee.client.helper.event.EventSystem;
import coffee.client.helper.event.impl.ConfigSaveEvent;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import me.x150.jmessenger.MessageSubscription;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import org.apache.logging.log4j.Level;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Themes extends Module {
    public static List<Theme> themes = new ArrayList<>();
    @Getter @Setter
    static Theme currentTheme;
    public static ConfigContainer conf = new ConfigContainer(new File(CoffeeMain.BASE, "themes.coffee"));

    public static void init() {
        conf.reload();
        Config config1 = conf.get(Config.class);
        themes = config1 == null || config1.getThemes() == null ? new ArrayList<>() : new ArrayList<>(config1.getThemes());
        if (themes.isEmpty()) {
            CoffeeMain.log(Level.WARN, "No themes found! Generating default");
            Theme def = new Theme();
            def.setAccent(new Color(0x3AD99D));
            def.setModule(new Color(0xFF171E1F, true));
            def.setConfig(new Color(0xFF111A1A, true));
            def.setActive(new Color(21, 157, 204, 255));
            def.setInactive(new Color(20, 20, 20, 255));
            def.setSecondary(new Color(9, 162, 104));
            def.setName("Default");
            themes.add(def);
            currentTheme = def;
        } else {
            currentTheme = config1.getSelectedTheme();
        }
        EventSystem.manager.registerSubscribers(new Object() { // keep registered
            @MessageSubscription
            void onStop(ConfigSaveEvent ev) {
                CoffeeMain.log(Level.INFO, "Saving themes");
                Config c = new Config();
                c.setThemes(themes);
                c.setSelectedTheme(currentTheme);
                conf.set(c);
                conf.save();
            }
        });
    }

    public Themes() {
        super("Themes", "The theme of the client", ModuleType.RENDER);
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

    @Data
    public static class Theme {
        String name;
        Color accent;
        Color module;
        Color config;
        Color active;
        Color inactive;
        Color secondary;
    }

    @Data
    public static class Config {
        List<Theme> themes;
        Theme selectedTheme;
    }
}
