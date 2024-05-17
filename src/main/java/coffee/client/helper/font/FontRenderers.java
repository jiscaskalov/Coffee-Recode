/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.helper.font;

import coffee.client.CoffeeMain;
import coffee.client.helper.font.adapter.FontAdapter;
import coffee.client.helper.font.adapter.impl.RendererFontAdapter;
import coffee.client.helper.font.renderer.FontRenderer;
import lombok.Getter;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FontRenderers {
    private static final List<RendererFontAdapter> fontRenderers = new ArrayList<>();
    private static FontAdapter normal;
    private static FontAdapter mono;
    @Getter public static FontRenderer renderer;

    public static FontAdapter getAdapter() {
        return normal;
    }

    public static void setAdapter(FontAdapter normal) {
        FontRenderers.normal = normal;
    }

    public static FontAdapter getMono() {
        if (mono == null) {
            int v = 9;
            try {
                mono = new RendererFontAdapter(Font.createFont(
                    Font.TRUETYPE_FONT,
                    Objects.requireNonNull(FontRenderers.class.getClassLoader().getResourceAsStream("Mono.ttf"))
                ).deriveFont(Font.PLAIN, v), v);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return mono;
    }

    public static RendererFontAdapter getCustomSize(float size) {
        float v = size / 2f; // assuming 2x scale
        for (RendererFontAdapter fontRenderer : fontRenderers) {
            if (fontRenderer.getSize() == v) {
                return fontRenderer;
            }
        }
        try {
            RendererFontAdapter bruhAdapter = new RendererFontAdapter(Font.createFont(
                Font.TRUETYPE_FONT,
                Objects.requireNonNull(FontRenderers.class.getClassLoader().getResourceAsStream("Font.ttf"))
            ).deriveFont(Font.PLAIN, v), v);
            fontRenderers.add(bruhAdapter);
            return bruhAdapter;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static @NotNull FontRenderer create(float size, String name) throws IOException, FontFormatException, NullPointerException {
        return new FontRenderer(Font.createFont(Font.TRUETYPE_FONT, CoffeeMain.class.getClassLoader().getResourceAsStream(name + ".ttf")).deriveFont(Font.PLAIN, size / 2f), size / 2f);
    }
}
