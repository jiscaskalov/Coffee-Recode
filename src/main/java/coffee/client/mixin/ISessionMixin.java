/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.mixin;

import net.minecraft.client.session.Session;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.UUID;

@Mixin(Session.class)
public interface ISessionMixin {
    @Mutable
    @Accessor("username")
    void setUsername(String username);

    @Mutable
    @Accessor("uuid")
    void setUuid(UUID uuid);

    @Mutable
    @Accessor("accessToken")
    void setAccessToken(String accessToken);

}
