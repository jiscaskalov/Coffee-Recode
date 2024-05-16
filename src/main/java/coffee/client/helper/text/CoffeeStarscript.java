package coffee.client.helper.text;

import coffee.client.CoffeeMain;
import coffee.client.feature.module.Module;
import coffee.client.helper.util.AccurateFrameRateCounter;
import coffee.client.helper.util.Names;
import coffee.client.helper.util.Utils;
import coffee.client.helper.world.*;
import coffee.client.mixin.IClientPlayerInteractionManagerMixin;
import meteordevelopment.starscript.Script;
import meteordevelopment.starscript.Section;
import meteordevelopment.starscript.StandardLib;
import meteordevelopment.starscript.Starscript;
import meteordevelopment.starscript.compiler.Compiler;
import meteordevelopment.starscript.compiler.Parser;
import meteordevelopment.starscript.utils.Error;
import meteordevelopment.starscript.utils.StarscriptError;
import meteordevelopment.starscript.value.Value;
import meteordevelopment.starscript.value.ValueMap;
import net.minecraft.SharedConstants;
import net.minecraft.block.BlockState;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientStatusC2SPacket;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.stat.Stat;
import net.minecraft.stat.Stats;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static coffee.client.CoffeeMain.client;

public class CoffeeStarscript {
    public static Starscript ss = new Starscript();

    private static final BlockPos.Mutable BP = new BlockPos.Mutable();
    private static final StringBuilder SB = new StringBuilder();

    public static void init() {
        StandardLib.init(ss);

        // General
        ss.set("client_version", SharedConstants.getGameVersion().getName());
        ss.set("fps", () -> Value.number(AccurateFrameRateCounter.globalInstance.getFps()));
        ss.set("ping", CoffeeStarscript::ping);
        ss.set("time", () -> Value.string(LocalTime.now().format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))));
        ss.set("cps", () -> Value.number(CPSUtils.getCpsAverage()));

        // Camera
        ss.set("camera", new ValueMap()
                .set("pos", new ValueMap()
                        .set("_toString", () -> posString(false, true))
                        .set("x", () -> Value.number(client.gameRenderer.getCamera().getPos().x))
                        .set("y", () -> Value.number(client.gameRenderer.getCamera().getPos().y))
                        .set("z", () -> Value.number(client.gameRenderer.getCamera().getPos().z))
                )
                .set("opposite_dim_pos", new ValueMap()
                        .set("_toString", () -> posString(true, true))
                        .set("x", () -> oppositeX(true))
                        .set("y", () -> Value.number(client.gameRenderer.getCamera().getPos().y))
                        .set("z", () -> oppositeZ(true))
                )

                .set("yaw", () -> yaw(true))
                .set("pitch", () -> pitch(true))
                .set("direction", () -> direction(true))
        );

        // Player
        ss.set("player", new ValueMap()
                .set("_toString", () -> Value.string(client.getSession().getUsername()))
                .set("health", () -> Value.number(client.player != null ? client.player.getHealth() : 0))
                .set("absorption", () -> Value.number(client.player != null ? client.player.getAbsorptionAmount() : 0))
                .set("hunger", () -> Value.number(client.player != null ? client.player.getHungerManager().getFoodLevel() : 0))

                .set("speed", () -> Value.number(Utils.getPlayerSpeed().horizontalLength()))
                .set("speed_all", new ValueMap()
                        .set("_toString", () -> Value.string(client.player != null ? Utils.getPlayerSpeed().toString() : ""))
                        .set("x", () -> Value.number(client.player != null ? Utils.getPlayerSpeed().x : 0))
                        .set("y", () -> Value.number(client.player != null ? Utils.getPlayerSpeed().y : 0))
                        .set("z", () -> Value.number(client.player != null ? Utils.getPlayerSpeed().z : 0))
                )

                .set("breaking_progress", () -> Value.number(client.interactionManager != null ? ((IClientPlayerInteractionManagerMixin) client.interactionManager).getBreakingProgress() : 0))
                .set("biome", CoffeeStarscript::biome)

                .set("dimension", () -> Value.string(PlayerUtils.getDimension().name()))
                .set("opposite_dimension", () -> Value.string(PlayerUtils.getDimension().opposite().name()))

                .set("gamemode", () -> client.player != null ? Value.string(StringUtils.capitalize(PlayerUtils.getGameMode().getName())) : Value.null_())

                .set("pos", new ValueMap()
                        .set("_toString", () -> posString(false, false))
                        .set("x", () -> Value.number(client.player != null ? client.player.getX() : 0))
                        .set("y", () -> Value.number(client.player != null ? client.player.getY() : 0))
                        .set("z", () -> Value.number(client.player != null ? client.player.getZ() : 0))
                )
                .set("opposite_dim_pos", new ValueMap()
                        .set("_toString", () -> posString(true, false))
                        .set("x", () -> oppositeX(false))
                        .set("y", () -> Value.number(client.player != null ? client.player.getY() : 0))
                        .set("z", () -> oppositeZ(false))
                )

                .set("yaw", () -> yaw(false))
                .set("pitch", () -> pitch(false))
                .set("direction", () -> direction(false))

                .set("hand", () -> client.player != null ? wrap(client.player.getMainHandStack()) : Value.null_())
                .set("offhand", () -> client.player != null ? wrap(client.player.getOffHandStack()) : Value.null_())
                .set("hand_or_offhand", CoffeeStarscript::handOrOffhand)
                .set("get_item", CoffeeStarscript::getItem)
                .set("count_items", CoffeeStarscript::countItems)

                .set("xp", new ValueMap()
                        .set("level", () -> Value.number(client.player != null ? client.player.experienceLevel : 0))
                        .set("progress", () -> Value.number(client.player != null ? client.player.experienceProgress : 0))
                        .set("total", () -> Value.number(client.player != null ? client.player.totalExperience : 0))
                )

                .set("has_potion_effect", CoffeeStarscript::hasPotionEffect)
                .set("get_potion_effect", CoffeeStarscript::getPotionEffect)

                .set("get_stat", CoffeeStarscript::getStat)
        );

        // Crosshair target
        ss.set("crosshair_target", new ValueMap()
                .set("type", CoffeeStarscript::crosshairType)
                .set("value", CoffeeStarscript::crosshairValue)
        );

        // Server
        ss.set("server", new ValueMap()
                .set("_toString", () -> Value.string(Utils.getWorldName()))
                .set("tps", () -> Value.number(TickRate.INSTANCE.getTickRate()))
                .set("time", () -> Value.string(Utils.getWorldTime()))
                .set("player_count", () -> Value.number(client.getNetworkHandler() != null ? client.getNetworkHandler().getPlayerList().size() : 0))
                .set("difficulty", () -> Value.string(client.world != null ? client.world.getDifficulty().getName() : ""))
        );
    }

    // Helpers

    public static Script compile(String source) {
        Parser.Result result = Parser.parse(source);

        if (result.hasErrors()) {
            for (Error error : result.errors) printChatError(error);
            return null;
        }

        return Compiler.compile(result);
    }

    public static Section runSection(Script script, StringBuilder sb) {
        try {
            return ss.run(script, sb);
        }
        catch (StarscriptError error) {
            printChatError(error);
            return null;
        }
    }
    public static String run(Script script, StringBuilder sb) {
        Section section = runSection(script, sb);
        return section != null ? section.toString() : null;
    }

    public static Section runSection(Script script) {
        return runSection(script, new StringBuilder());
    }
    public static String run(Script script) {
        return run(script, new StringBuilder());
    }

    // Errors

    public static void printChatError(int i, Error error) {
        String caller = getCallerName();

        if (caller != null) {
//            if (i != -1) ChatUtils.errorPrefix("Starscript", "%d, %d '%c': %s (from %s)", i, error.character, error.ch, error.message, caller);
//            else ChatUtils.errorPrefix("Starscript", "%d '%c': %s (from %s)", error.character, error.ch, error.message, caller);
        }
        else {
//            if (i != -1) ChatUtils.errorPrefix("Starscript", "%d, %d '%c': %s", i, error.character, error.ch, error.message);
//            else ChatUtils.errorPrefix("Starscript", "%d '%c': %s", error.character, error.ch, error.message);
        }
    }

    public static void printChatError(Error error) {
        printChatError(-1, error);
    }

    public static void printChatError(StarscriptError e) {
        String caller = getCallerName();

//        if (caller != null) ChatUtils.errorPrefix("Starscript", "%s (from %s)", e.getMessage(), caller);
//        else ChatUtils.errorPrefix("Starscript", "%s", e.getMessage());
    }

    private static String getCallerName() {
        StackTraceElement[] elements = Thread.currentThread().getStackTrace();
        if (elements.length == 0) return null;

        for (int i = 1; i < elements.length; i++) {
            String name = elements[i].getClassName();

            if (name.startsWith(Starscript.class.getPackageName())) continue;
            if (name.equals(CoffeeStarscript.class.getName())) continue;

            return name.substring(name.lastIndexOf('.') + 1);
        }

        return null;
    }

    // Functions

    private static long lastRequestedStatsTime = 0;

    private static Value hasPotionEffect(Starscript ss, int argCount) {
        if (argCount < 1) ss.error("player.has_potion_effect() requires 1 argument, got %d.", argCount);
        if (client.player == null) return Value.bool(false);

        Identifier name = popIdentifier(ss, "First argument to player.has_potion_effect() needs to a string.");

        Optional<RegistryEntry.Reference<StatusEffect>> effect = Registries.STATUS_EFFECT.getEntry(name);
        if (effect.isEmpty()) return Value.bool(false);

        StatusEffectInstance effectInstance = client.player.getStatusEffect(effect.get());
        return Value.bool(effectInstance != null);
    }

    private static Value getPotionEffect(Starscript ss, int argCount) {
        if (argCount < 1) ss.error("player.get_potion_effect() requires 1 argument, got %d.", argCount);
        if (client.player == null) return Value.null_();

        Identifier name = popIdentifier(ss, "First argument to player.get_potion_effect() needs to a string.");

        Optional<RegistryEntry.Reference<StatusEffect>> effect = Registries.STATUS_EFFECT.getEntry(name);
        if (effect.isEmpty()) return Value.null_();

        StatusEffectInstance effectInstance = client.player.getStatusEffect(effect.get());
        if (effectInstance == null) return Value.null_();

        return wrap(effectInstance);
    }

    private static Value getStat(Starscript ss, int argCount) {
        if (argCount < 1) ss.error("player.get_stat() requires 1 argument, got %d.", argCount);
        if (client.player == null) return Value.number(0);

        long time = System.currentTimeMillis();
        if ((time - lastRequestedStatsTime) / 1000.0 >= 1 && client.getNetworkHandler() != null) {
            client.getNetworkHandler().sendPacket(new ClientStatusC2SPacket(ClientStatusC2SPacket.Mode.REQUEST_STATS));
            lastRequestedStatsTime = time;
        }

        String type = argCount > 1 ? ss.popString("First argument to player.get_stat() needs to be a string.") : "custom";
        Identifier name = popIdentifier(ss, (argCount > 1 ? "Second" : "First") + " argument to player.get_stat() needs to be a string.");

        Stat<?> stat = switch (type) {
            case "mined" -> Stats.MINED.getOrCreateStat(Registries.BLOCK.get(name));
            case "crafted" -> Stats.CRAFTED.getOrCreateStat(Registries.ITEM.get(name));
            case "used" -> Stats.USED.getOrCreateStat(Registries.ITEM.get(name));
            case "broken" -> Stats.BROKEN.getOrCreateStat(Registries.ITEM.get(name));
            case "picked_up" -> Stats.PICKED_UP.getOrCreateStat(Registries.ITEM.get(name));
            case "dropped" -> Stats.DROPPED.getOrCreateStat(Registries.ITEM.get(name));
            case "killed" -> Stats.KILLED.getOrCreateStat(Registries.ENTITY_TYPE.get(name));
            case "killed_by" -> Stats.KILLED_BY.getOrCreateStat(Registries.ENTITY_TYPE.get(name));
            case "custom" -> {
                name = Registries.CUSTOM_STAT.get(name);
                yield name != null ? Stats.CUSTOM.getOrCreateStat(name) : null;
            }
            default -> null;
        };

        return Value.number(stat != null ? client.player.getStatHandler().getStat(stat) : 0);
    }

    private static Value getItem(Starscript ss, int argCount) {
        if (argCount != 1) ss.error("player.get_item() requires 1 argument, got %d.", argCount);

        int i = (int) ss.popNumber("First argument to player.get_item() needs to be a number.");
        return client.player != null ? wrap(client.player.getInventory().getStack(i)) : Value.null_();
    }

    private static Value countItems(Starscript ss, int argCount) {
        if (argCount != 1) ss.error("player.count_items() requires 1 argument, got %d.", argCount);

        String idRaw = ss.popString("First argument to player.count_items() needs to be a string.");
        Identifier id = Identifier.tryParse(idRaw);
        if (id == null) return Value.number(0);

        Item item = Registries.ITEM.get(id);
        if (item == Items.AIR || client.player == null) return Value.number(0);

        int count = 0;
        for (int i = 0; i < client.player.getInventory().size(); i++) {
            ItemStack itemStack = client.player.getInventory().getStack(i);
            if (itemStack.getItem() == item) count += itemStack.getCount();
        }

        return Value.number(count);
    }

    private static Value oppositeX(boolean camera) {
        double x = camera ? client.gameRenderer.getCamera().getPos().x : (client.player != null ? client.player.getX() : 0);
        Dimension dimension = PlayerUtils.getDimension();

        if (dimension == Dimension.Overworld) x /= 8;
        else if (dimension == Dimension.Nether) x *= 8;

        return Value.number(x);
    }

    private static Value oppositeZ(boolean camera) {
        double z = camera ? client.gameRenderer.getCamera().getPos().z : (client.player != null ? client.player.getZ() : 0);
        Dimension dimension = PlayerUtils.getDimension();

        if (dimension == Dimension.Overworld) z /= 8;
        else if (dimension == Dimension.Nether) z *= 8;

        return Value.number(z);
    }

    private static Value yaw(boolean camera) {
        float yaw;
        if (camera) yaw = client.gameRenderer.getCamera().getYaw();
        else yaw = client.player != null ? client.player.getYaw() : 0;
        yaw %= 360;

        if (yaw < 0) yaw += 360;
        if (yaw > 180) yaw -= 360;

        return Value.number(yaw);
    }

    private static Value pitch(boolean camera) {
        float pitch;
        if (camera) pitch = client.gameRenderer.getCamera().getPitch();
        else pitch = client.player != null ? client.player.getPitch() : 0;
        pitch %= 360;

        if (pitch < 0) pitch += 360;
        if (pitch > 180) pitch -= 360;

        return Value.number(pitch);
    }

    private static Value direction(boolean camera) {
        float yaw;
        if (camera) yaw = client.gameRenderer.getCamera().getYaw();
        else yaw = client.player != null ? client.player.getYaw() : 0;

        return wrap(HorizontalDirection.get(yaw));
    }

    private static Value biome() {
        if (client.player == null || client.world == null) return Value.string("");

        BP.set(client.player.getX(), client.player.getY(), client.player.getZ());
        Identifier id = client.world.getRegistryManager().get(RegistryKeys.BIOME).getId(client.world.getBiome(BP).value());
        if (id == null) return Value.string("Unknown");

        return Value.string(Arrays.stream(id.getPath().split("_")).map(StringUtils::capitalize).collect(Collectors.joining(" ")));
    }

    private static Value handOrOffhand() {
        if (client.player == null) return Value.null_();

        ItemStack itemStack = client.player.getMainHandStack();
        if (itemStack.isEmpty()) itemStack = client.player.getOffHandStack();

        return itemStack != null ? wrap(itemStack) : Value.null_();
    }

    private static Value ping() {
        if (client.getNetworkHandler() == null || client.player == null) return Value.number(0);

        PlayerListEntry playerListEntry = client.getNetworkHandler().getPlayerListEntry(client.player.getUuid());
        return Value.number(playerListEntry != null ? playerListEntry.getLatency() : 0);
    }

    private static Value posString(boolean opposite, boolean camera) {
        Vec3d pos;
        if (camera) pos = client.gameRenderer.getCamera().getPos();
        else pos = client.player != null ? client.player.getPos() : Vec3d.ZERO;

        double x = pos.x;
        double z = pos.z;

        if (opposite) {
            Dimension dimension = PlayerUtils.getDimension();

            if (dimension == Dimension.Overworld) {
                x /= 8;
                z /= 8;
            }
            else if (dimension == Dimension.Nether) {
                x *= 8;
                z *= 8;
            }
        }

        return posString(x, pos.y, z);
    }

    private static Value posString(double x, double y, double z) {
        return Value.string(String.format("X: %.0f Y: %.0f Z: %.0f", x, y, z));
    }

    private static Value crosshairType() {
        if (client.crosshairTarget == null) return Value.string("miss");

        return Value.string(switch (client.crosshairTarget.getType()) {
            case MISS -> "miss";
            case BLOCK -> "block";
            case ENTITY -> "entity";
        });
    }

    private static Value crosshairValue() {
        if (client.world == null || client.crosshairTarget == null) return Value.null_();

        if (client.crosshairTarget.getType() == HitResult.Type.MISS) return Value.string("");
        if (client.crosshairTarget instanceof BlockHitResult hit) return wrap(hit.getBlockPos(), client.world.getBlockState(hit.getBlockPos()));
        return wrap(((EntityHitResult) client.crosshairTarget).getEntity());
    }

    // Utility

    public static Identifier popIdentifier(Starscript ss, String errorMessage) {
        try {
            return new Identifier(ss.popString(errorMessage));
        }
        catch (InvalidIdentifierException e) {
            ss.error(e.getMessage());
            return null;
        }
    }

    // Wrapping

    public static Value wrap(ItemStack itemStack) {
        String name = itemStack.isEmpty() ? "" : Names.get(itemStack.getItem());

        int durability = 0;
        if (!itemStack.isEmpty() && itemStack.isDamageable()) durability = itemStack.getMaxDamage() - itemStack.getDamage();

        return Value.map(new ValueMap()
                .set("_toString", Value.string(itemStack.getCount() <= 1 ? name : String.format("%s %dx", name, itemStack.getCount())))
                .set("name", Value.string(name))
                .set("id", Value.string(Registries.ITEM.getId(itemStack.getItem()).toString()))
                .set("count", Value.number(itemStack.getCount()))
                .set("durability", Value.number(durability))
                .set("max_durability", Value.number(itemStack.getMaxDamage()))
        );
    }

    public static Value wrap(BlockPos blockPos, BlockState blockState) {
        return Value.map(new ValueMap()
                .set("_toString", Value.string(Names.get(blockState.getBlock())))
                .set("id", Value.string(Registries.BLOCK.getId(blockState.getBlock()).toString()))
                .set("pos", Value.map(new ValueMap()
                        .set("_toString", posString(blockPos.getX(), blockPos.getY(), blockPos.getZ()))
                        .set("x", Value.number(blockPos.getX()))
                        .set("y", Value.number(blockPos.getY()))
                        .set("z", Value.number(blockPos.getZ()))
                ))
        );
    }

    public static Value wrap(Entity entity) {
        return Value.map(new ValueMap()
                .set("_toString", Value.string(entity.getName().getString()))
                .set("id", Value.string(Registries.ENTITY_TYPE.getId(entity.getType()).toString()))
                .set("health", Value.number(entity instanceof LivingEntity e ? e.getHealth(): 0))
                .set("absorption", Value.number(entity instanceof LivingEntity e ? e.getAbsorptionAmount() : 0))
                .set("pos", Value.map(new ValueMap()
                        .set("_toString", posString(entity.getX(), entity.getY(), entity.getZ()))
                        .set("x", Value.number(entity.getX()))
                        .set("y", Value.number(entity.getY()))
                        .set("z", Value.number(entity.getZ()))
                ))
        );
    }

    public static Value wrap(HorizontalDirection dir) {
        return Value.map(new ValueMap()
                .set("_toString", Value.string(dir.name + " " + dir.axis))
                .set("name", Value.string(dir.name))
                .set("axis", Value.string(dir.axis))
        );
    }

    public static Value wrap(StatusEffectInstance effectInstance) {
        return Value.map(new ValueMap()
                .set("duration", effectInstance.getDuration())
                .set("level", effectInstance.getAmplifier() + 1)
        );
    }
}