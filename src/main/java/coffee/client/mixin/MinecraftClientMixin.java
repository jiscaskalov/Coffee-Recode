/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.mixin;

import coffee.client.CoffeeMain;
import coffee.client.feature.command.impl.SelfDestruct;
import coffee.client.feature.gui.screen.HomeScreen;
import coffee.client.feature.gui.screen.LoadingScreen;
import coffee.client.feature.module.Module;
import coffee.client.feature.module.ModuleRegistry;
import coffee.client.feature.module.impl.world.FastUse;
import coffee.client.helper.event.EventSystem;
import coffee.client.helper.event.impl.OpenScreenEvent;
import coffee.client.helper.event.impl.ResourcePacksReloadedEvent;
import coffee.client.helper.manager.ConfigManager;
import coffee.client.helper.text.CoffeeClickEvent;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.util.crash.CrashReport;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
    @Shadow
    private int itemUseCooldown;

//    @Inject(method = "printCrashReport", at = @At("HEAD"))
//    private static void coffee_printCrash(CrashReport report, CallbackInfo ci) {
//        List<String> strings = ModuleRegistry.getModules().stream().filter(Module::isEnabled).map(Module::getName).toList();
//        report.addElement("Coffee client").add("Enabled modules", strings.isEmpty() ? "None" : String.join(", ", strings.toArray(String[]::new)));
//    }

    @Shadow private static MinecraftClient instance;

    @Inject(method = "stop", at = @At("HEAD"))
    void coffee_dispatchExit(CallbackInfo ci) {
        ConfigManager.saveState();
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    void coffee_postWindowInit(RunArgs args, CallbackInfo ci) {
        CoffeeMain.INSTANCE.postWindowInit();
    }

    @Inject(method = "setScreen", at = @At("HEAD"))
    void coffee_setScreenChange(Screen screen, CallbackInfo ci) {
        CoffeeMain.lastScreenChange = System.currentTimeMillis();
    }

    @Redirect(method = "handleInputEvents", at = @At(value = "FIELD", opcode = Opcodes.GETFIELD, target = "Lnet/minecraft/client/MinecraftClient;itemUseCooldown:I"))
    public int coffee_replaceItemUseCooldown(MinecraftClient minecraftClient) {
        FastUse fu = ModuleRegistry.getByClass(FastUse.class);
        if (fu != null && fu.isEnabled()) {
            return 0;
        } else {
            return this.itemUseCooldown;
        }
    }

    @ModifyReturnValue(method = "reloadResources(ZLnet/minecraft/client/MinecraftClient$LoadingContext;)Ljava/util/concurrent/CompletableFuture;", at = @At("RETURN"))
    private CompletableFuture<Void> onReloadResourcesNewCompletableFuture(CompletableFuture<Void> original) {
        return original.thenRun(() -> EventSystem.manager.send(new ResourcePacksReloadedEvent()));
    }

    @Inject(method = "getGameVersion", at = @At("HEAD"), cancellable = true)
    void coffee_replaceGameVersion(CallbackInfoReturnable<String> cir) {
        if (SelfDestruct.shouldSelfDestruct()) {
            cir.setReturnValue(SharedConstants.getGameVersion().getName());
        }
    }

    @Inject(method = "getVersionType", at = @At("HEAD"), cancellable = true)
    void coffee_replaceVersionType(CallbackInfoReturnable<String> cir) {
        if (SelfDestruct.shouldSelfDestruct()) {
            cir.setReturnValue("release");
        }
    }

    private Screen obtain(Screen original) {
        if (original instanceof TitleScreen && !SelfDestruct.shouldSelfDestruct()) {
            return LoadingScreen.instance();
        }
        return original;
    }

    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    void setScreen(Screen screen, CallbackInfo ci) {
        OpenScreenEvent se = new OpenScreenEvent(screen);
        EventSystem.manager.send(se);
        if (se.isCancelled()) ci.cancel();

        if (screen instanceof TitleScreen && CoffeeMain.client.currentScreen == HomeScreen.instance()) return;
        if (screen instanceof TitleScreen) {
            ci.cancel();
            CoffeeMain.client.setScreen(obtain(screen));
        }
    }

//    @ModifyArg(method = "method_45026", at = @At(value = "INVOKE",
//                                                 target = "Lnet/minecraft/client/gui/screen/ConnectScreen;connect(Lnet/minecraft/client/gui/screen/Screen;Lnet/minecraft/client/MinecraftClient;Lnet/minecraft/client/network/ServerAddress;Lnet/minecraft/client/network/ServerInfo;)V"),
//               index = 0)
//    Screen coffee_replaceTitleScreenDirectConnect(Screen screen) {
//        return obtain(screen);
//    }
//
//    @ModifyArg(method = "method_44648", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;setScreen(Lnet/minecraft/client/gui/screen/Screen;)V"), index = 0)
//    Screen coffee_replaceTitleScreenBannedNotice(Screen screen) {
//        return obtain(screen);
//    }

}
