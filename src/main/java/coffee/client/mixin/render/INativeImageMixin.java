package coffee.client.mixin.render;

import net.minecraft.client.texture.NativeImage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(NativeImage.class)
public interface INativeImageMixin {
    @Accessor("pointer")
    long getPointer();
}
