package coffee.client.mixin.render;

import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(GameRenderer.class)
public interface IGameRendererMixin {
    @Invoker("renderHand")
    void irenderHand(Camera camera, float tickDelta, Matrix4f matrix4f);
}
