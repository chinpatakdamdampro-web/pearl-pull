package com.ppull.mixin;

import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Exposes vanilla's private right-click handler so we trigger exactly what a real right click does. */
@Mixin(MinecraftClient.class)
public interface MinecraftClientInvoker {
    @Invoker("doItemUse")
    void ppull$doItemUse();
}
