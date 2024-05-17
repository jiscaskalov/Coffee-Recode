package coffee.client.helper.render;

import coffee.client.helper.util.AccurateFrameRateCounter;
import coffee.client.helper.util.Utils;
import net.minecraft.client.MinecraftClient;

public class AnimationUtils {
    public static double deltaTime() {
        return MinecraftClient.getInstance().getCurrentFps() > 0 ? (1f / AccurateFrameRateCounter.globalInstance.getFps()) : 0.016;
    }

    public static float fast(float end, float start, float multiple) {
        return (1 - Utils.clamp((float) (deltaTime() * multiple), 0, 1)) * end + Utils.clamp((float) (deltaTime() * multiple), 0, 1) * start;
    }
}
