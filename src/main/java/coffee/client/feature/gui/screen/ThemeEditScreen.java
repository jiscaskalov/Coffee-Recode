/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.feature.gui.screen;

import coffee.client.feature.gui.HasSpecialCursor;
import coffee.client.feature.gui.element.Element;
import coffee.client.feature.gui.element.impl.ButtonElement;
import coffee.client.feature.gui.element.impl.ButtonGroupElement;
import coffee.client.feature.gui.element.impl.ColorEditorElement;
import coffee.client.feature.gui.element.impl.FlexLayoutElement;
import coffee.client.feature.gui.element.impl.TextFieldElement;
import coffee.client.feature.gui.element.impl.TexturedButtonElement;
import coffee.client.feature.gui.notifications.Notification;
import coffee.client.feature.gui.screen.base.AAScreen;
import coffee.client.feature.gui.screen.base.CenterOverlayScreen;
import coffee.client.feature.module.impl.render.Themes;
import coffee.client.feature.module.impl.render.Waypoints;
import coffee.client.helper.font.FontRenderers;
import coffee.client.helper.font.adapter.impl.RendererFontAdapter;
import coffee.client.helper.render.Cursor;
import coffee.client.helper.render.Renderer;
import coffee.client.helper.render.textures.Texture;
import coffee.client.helper.util.Transitions;
import coffee.client.helper.util.Utils;
import lombok.Getter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class ThemeEditScreen extends AAScreen {
    AddButton adb = new AddButton(0, 0, 60, 60, this::add);
    FlexLayoutElement el;
    boolean resized = false;

    void add() {
        Themes.Theme th = new Themes.Theme();
        th.setAccent(Color.WHITE);
        th.setModule(Color.WHITE);
        th.setConfig(Color.WHITE);
        th.setActive(Color.WHITE);
        th.setInactive(Color.WHITE);
        th.setSecondary(Color.WHITE);
        th.setName("New theme");
        Themes.themes.add(th);
        init();
    }

    @Override
    protected void initInternal() {
        List<Element> els = new ArrayList<>();
        for (Themes.Theme theme : Themes.themes) {
            els.add(new ThemeVis(0, 0, theme));
        }
        els.add(adb);
        if (el == null || resized) {
            el = new FlexLayoutElement(FlexLayoutElement.LayoutDirection.RIGHT, 5, 5, width - 10, height - 10, 5, els.toArray(Element[]::new));
        } else {
            el.setElements(els);
        }
        resized = false;
        addChild(el);
    }

    @Override
    public void resize(MinecraftClient client, int width, int height) {
        resized = false;
        super.resize(client, width, height);
    }

    @Override
    public void renderInternal(MatrixStack stack, int mouseX, int mouseY, float delta) {
        Renderer.R2D.renderQuad(stack, new Color(0, 0, 0, 150), 0, 0, width, height);
        super.renderInternal(stack, mouseX, mouseY, delta);
    }

    void renderPlusIcon(MatrixStack stack, double x, double y, double size) {
        Renderer.R2D.renderCircle(stack, new Color(11, 145, 225), x, y, size, 60);
        Renderer.R2D.renderRoundedQuad(stack, Color.WHITE, x - size / 2d, y - 1, x + size / 2d, y + 1, 1, 5);
        Renderer.R2D.renderRoundedQuad(stack, Color.WHITE, x - 1, y - size / 2d, x + 1, y + size / 2d, 1, 5);
    }

    static class EditScreen extends CenterOverlayScreen {
        Themes.Theme th;

        public EditScreen(Screen parent, Themes.Theme th) {
            super(parent, "Theme editor", "Edit themes");
            this.th = th;
        }

        @Override
        protected void initInternal() {
            super.initInternal();
            ButtonElement save = new ButtonElement(ButtonElement.SUCCESS, 0, 0, 200, 20, "Save", this::close);
            TextFieldElement textFieldElement = new TextFieldElement(0, 0, 200, 20, "Name");
            textFieldElement.set(th.getName());
            textFieldElement.setChangeListener(() -> th.setName(textFieldElement.get()));
            ColorEditorElement cea = new ColorEditorElement(0, 0, 180, 80, th.getAccent());
            ColorEditorElement cem = new ColorEditorElement(0, 0, 180, 80, th.getModule());
            ColorEditorElement cec = new ColorEditorElement(0, 0, 180, 80, th.getConfig());
            ColorEditorElement ceaa = new ColorEditorElement(0, 0, 180, 80, th.getActive());
            ColorEditorElement cei = new ColorEditorElement(0, 0, 180, 80, th.getInactive());
            ColorEditorElement ces = new ColorEditorElement(0, 0, 180, 80, th.getSecondary());
            cea.setOnChange(color -> th.setAccent(color));
            cem.setOnChange(color -> th.setModule(color));
            cec.setOnChange(color -> th.setConfig(color));
            ceaa.setOnChange(color -> th.setActive(color));
            cei.setOnChange(color -> th.setInactive(color));
            ces.setOnChange(color -> th.setSecondary(color));
            FlexLayoutElement topColors = new FlexLayoutElement(FlexLayoutElement.LayoutDirection.RIGHT, 0, 0, 5, cea, cem, cec);
            FlexLayoutElement bottomColors = new FlexLayoutElement(FlexLayoutElement.LayoutDirection.RIGHT, 0, 0, 5, ceaa, cei, ces);
            FlexLayoutElement bottom = new FlexLayoutElement(FlexLayoutElement.LayoutDirection.RIGHT, 0, 0, 5, textFieldElement, save);
            FlexLayoutElement total = new FlexLayoutElement(FlexLayoutElement.LayoutDirection.DOWN, 0, 0, 5, topColors, bottomColors, bottom);
            addChild(total);
        }
    }

    class AddButton extends Element implements HasSpecialCursor {
        double expand = 0;
        Runnable onClick;
        boolean mouseOver = false;

        public AddButton(double x, double y, double width, double height, Runnable onClick) {
            super(x, y, width, height);
            this.onClick = onClick;
        }

        @Override
        public void tickAnimations() {
            double delta = 0.04;
            if (!mouseOver) {
                delta *= -1;
            }
            expand += delta;
            expand = MathHelper.clamp(expand, 0, 1);
        }

        @Override
        public void render(MatrixStack stack, double mouseX, double mouseY) {
            mouseOver = inBounds(mouseX, mouseY);
            double expand = Transitions.easeOutExpo(this.expand);
            Renderer.R2D.renderRoundedQuad(
                    stack,
                    new Color(20, 20, 20),
                    getPositionX(),
                    getPositionY(),
                    getPositionX() + getWidth(),
                    getPositionY() + getHeight(),
                    5,
                    20
            );
            renderPlusIcon(stack, getPositionX() + getWidth() / 2d, getPositionY() + getHeight() / 2d, 10 + 2 * expand);
        }

        @Override
        public boolean mouseClicked(double x, double y, int button) {
            if (inBounds(x, y) && button == 0) {
                onClick.run();
                return true;
            }
            return false;
        }

        @Override
        public boolean mouseReleased(double x, double y, int button) {
            return false;
        }

        @Override
        public boolean mouseDragged(double x, double y, double xDelta, double yDelta, int button) {
            return false;
        }

        @Override
        public boolean charTyped(char c, int mods) {
            return false;
        }

        @Override
        public boolean keyPressed(int keyCode, int mods) {
            return false;
        }

        @Override
        public boolean keyReleased(int keyCode, int mods) {
            return false;
        }

        @Override
        public boolean mouseScrolled(double x, double y, double vAmount, double hAmount) {
            return false;
        }

        @Override
        public long getCursor() {
            return Cursor.CLICK;
        }

        @Override
        public boolean shouldApplyCustomCursor() {
            return mouseOver;
        }
    }

    class ThemeVis extends Element {
        Themes.Theme theme;
        ButtonGroupElement actions;
        TexturedButtonElement delete;

        public ThemeVis(double x, double y, Themes.Theme theme) {
            super(x, y, 120, 60);
            this.theme = theme;
            this.actions = new ButtonGroupElement(
                    0,
                    0,
                    getWidth() - 10,
                    20,
                    ButtonGroupElement.LayoutDirection.RIGHT,
                    new ButtonGroupElement.ButtonEntry("Edit", this::edit),
                    new ButtonGroupElement.ButtonEntry("Apply", () -> Themes.setCurrentTheme(theme))
            );
            this.delete = new TexturedButtonElement(
                    new Color(255, 70, 70),
                    0,
                    0,
                    16,
                    16,
                    this::delete,
                    TexturedButtonElement.IconRenderer.fromSpritesheet(Texture.MODULE_TYPES, "delete.png")
            );
        }

        void edit() {
            client.setScreen(new EditScreen(ThemeEditScreen.this, theme));
        }

        void delete() {
            Themes.themes.remove(theme);
            init();
        }

        @Override
        public void tickAnimations() {

        }

        @Override
        public void render(MatrixStack stack, double mouseX, double mouseY) {
            this.actions.setPositionX(getPositionX() + 5);
            this.actions.setPositionY(getPositionY() + getHeight() - 20 - 5);
            this.delete.setPositionX(getPositionX() + getWidth() - 5 - this.delete.getWidth());
            this.delete.setPositionY(getPositionY() + 5);
            Renderer.R2D.renderRoundedQuad(
                    stack,
                    new Color(20, 20, 20),
                    getPositionX(),
                    getPositionY(),
                    getPositionX() + getWidth(),
                    getPositionY() + getHeight(),
                    5,
                    20
            );
            RendererFontAdapter customSize = FontRenderers.getCustomSize(20);
            String t = Utils.capAtLength(theme.getName(), getWidth() - 10 - this.delete.getWidth(), customSize);
            customSize.drawString(stack, t, getPositionX() + 5, getPositionY() + 5, 0xFFFFFF);
            this.actions.render(stack, mouseX, mouseY);
            this.delete.render(stack, mouseX, mouseY);
        }

        @Override
        public boolean mouseClicked(double x, double y, int button) {
            return this.delete.mouseClicked(x, y, button) || this.actions.mouseClicked(x, y, button);
        }

        @Override
        public boolean mouseReleased(double x, double y, int button) {
            return false;
        }

        @Override
        public boolean mouseDragged(double x, double y, double xDelta, double yDelta, int button) {
            return false;
        }

        @Override
        public boolean charTyped(char c, int mods) {
            return false;
        }

        @Override
        public boolean keyPressed(int keyCode, int mods) {
            return false;
        }

        @Override
        public boolean keyReleased(int keyCode, int mods) {
            return false;
        }

        @Override
        public boolean mouseScrolled(double x, double y, double vAmount, double hAmount) {
            return false;
        }
    }
}