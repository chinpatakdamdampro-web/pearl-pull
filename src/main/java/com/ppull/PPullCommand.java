package com.ppull;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public final class PPullCommand {
    private PPullCommand() {}

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(ClientCommandManager.literal("ppull")
            .executes(ctx -> {
                ctx.getSource().sendFeedback(Text.literal(
                    "/ppull add <player> | remove <player> | list | toggle").formatted(Formatting.GRAY));
                return 1;
            })
            .then(ClientCommandManager.literal("add")
                .then(ClientCommandManager.argument("player", StringArgumentType.word())
                    .suggests((ctx, builder) -> CommandSource.suggestMatching(
                        ctx.getSource().getPlayerNames().stream().filter(n -> !PPullConfig.isAllowed(n)),
                        builder))
                    .executes(ctx -> {
                        String name = StringArgumentType.getString(ctx, "player");
                        if (PPullConfig.add(name)) {
                            ctx.getSource().sendFeedback(Text.literal("Added " + name + " to the pearl-pull whitelist.")
                                .formatted(Formatting.GREEN));
                        } else {
                            ctx.getSource().sendFeedback(Text.literal(name + " is already whitelisted.")
                                .formatted(Formatting.YELLOW));
                        }
                        return 1;
                    })))
            .then(ClientCommandManager.literal("remove")
                .then(ClientCommandManager.argument("player", StringArgumentType.word())
                    .suggests((ctx, builder) -> CommandSource.suggestMatching(PPullConfig.names(), builder))
                    .executes(ctx -> {
                        String name = StringArgumentType.getString(ctx, "player");
                        if (PPullConfig.remove(name)) {
                            ctx.getSource().sendFeedback(Text.literal("Removed " + name + " from the whitelist.")
                                .formatted(Formatting.GREEN));
                        } else {
                            ctx.getSource().sendFeedback(Text.literal(name + " is not on the whitelist.")
                                .formatted(Formatting.YELLOW));
                        }
                        return 1;
                    })))
            .then(ClientCommandManager.literal("list")
                .executes(ctx -> {
                    List<String> names = PPullConfig.names();
                    String state = PPullConfig.isEnabled() ? "ON" : "OFF";
                    ctx.getSource().sendFeedback(Text.literal(
                        "Pearl puller is " + state + ". Whitelist (" + names.size() + "): "
                            + (names.isEmpty() ? "none" : String.join(", ", names))).formatted(Formatting.AQUA));
                    return 1;
                }))
            .then(ClientCommandManager.literal("toggle")
                .executes(ctx -> {
                    boolean now = !PPullConfig.isEnabled();
                    PPullConfig.setEnabled(now);
                    ctx.getSource().sendFeedback(Text.literal("Pearl puller " + (now ? "enabled." : "disabled."))
                        .formatted(now ? Formatting.GREEN : Formatting.RED));
                    return 1;
                })));
    }
}
