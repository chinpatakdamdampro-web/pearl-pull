package com.ppull;

import com.ppull.mixin.MinecraftClientInvoker;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PPullMod implements ClientModInitializer {
    /** Strict vanilla-style system line: "<Name> p" (whole line, so nobody can inject a fake name). */
    private static final Pattern VANILLA_LINE = Pattern.compile("^<([^\\s<>]{1,32})> p$", Pattern.CASE_INSENSITIVE);

    @Override
    public void onInitializeClient() {
        PPullConfig.load();

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
            PPullCommand.register(dispatcher));

        // Normal player chat: sender comes from the server-signed profile, so it can't be spoofed by text.
        ClientReceiveMessageEvents.CHAT.register((message, signedMessage, sender, params, timestamp) -> {
            if (sender == null) return;
            handle(sender.getName(), signedMessage.getSignedContent());
        });

        // Servers that deliver chat as system messages (some plugins): accept only the strict "<Name> p" form.
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (overlay) return;
            Matcher m = VANILLA_LINE.matcher(message.getString().trim());
            if (m.matches()) handle(m.group(1), "p");
        });
    }

    private static void handle(String sender, String content) {
        if (content == null || !content.trim().equalsIgnoreCase("p")) return;
        if (!PPullConfig.isEnabled() || !PPullConfig.isAllowed(sender)) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.isOnThread()) rightClick(client);
        else client.execute(() -> rightClick(client));
    }

    private static void rightClick(MinecraftClient client) {
        if (client.player == null || client.world == null || client.interactionManager == null) return;
        ((MinecraftClientInvoker) client).ppull$doItemUse();
    }
}
