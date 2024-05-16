package coffee.client.helper.world;

import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.world.GameMode;

import static coffee.client.CoffeeMain.client;

public class PlayerUtils {
    public static Dimension getDimension() {
        if (client.world == null) return Dimension.Overworld;

        return switch (client.world.getRegistryKey().getValue().getPath()) {
            case "the_nether" -> Dimension.Nether;
            case "the_end" -> Dimension.End;
            default -> Dimension.Overworld;
        };
    }
    public static GameMode getGameMode() {
        PlayerListEntry playerListEntry = client.getNetworkHandler().getPlayerListEntry(client.player.getUuid());
        if (playerListEntry == null) return GameMode.SPECTATOR;
        return playerListEntry.getGameMode();
    }
}
