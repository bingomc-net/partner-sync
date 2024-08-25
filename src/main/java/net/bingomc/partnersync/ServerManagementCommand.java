package net.bingomc.partnersync;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.VelocityBrigadierMessage;
import com.velocitypowered.api.proxy.ProxyServer;
import net.bingomc.partnersync.model.Server;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public final class ServerManagementCommand {

    public static BrigadierCommand createBrigadierCommand(final ProxyServer proxy) {
        LiteralCommandNode<CommandSource> helloNode = BrigadierCommand.literalArgumentBuilder("bingomc")
                .requires(source -> source.hasPermission("bingomc.manage"))
                .executes(context -> {
                    CommandSource source = context.getSource();

                    Component message = Component.text("BingoMC Partner Sync Plugin", NamedTextColor.AQUA);
                    source.sendMessage(message);
                    return Command.SINGLE_SUCCESS;
                })
                // Using the "then" method, you can add sub-arguments to the command.
                // For example, this subcommand will be executed when using the command "/test <some argument>"
                // A RequiredArgumentBuilder is a type of argument in which you can enter some undefined data
                // of some kind. For example, this example uses a StringArgumentType.word() that requires
                // a single word to be entered, but you can also use different ArgumentTypes provided by Brigadier
                // that return data of type Boolean, Integer, Float, other String types, etc
                .then(BrigadierCommand.literalArgumentBuilder("create")
                        .executes(context -> {
                            Component message;
                            try {
                                Server server = ServerHandler.requestServer();
                                message = Component.text(
                                        "Successfully requested server. New id = " + server.getId(),
                                        NamedTextColor.GREEN
                                );
                            } catch (Exception e) {
                                message = Component.text("Error: " + e.getMessage(), NamedTextColor.RED);
                            }
                            CommandSource source = context.getSource();
                            source.sendMessage(message);

                            return Command.SINGLE_SUCCESS;
                        })
                )
                .then(BrigadierCommand.literalArgumentBuilder("delete")
                        .then(BrigadierCommand.requiredArgumentBuilder("server_id", IntegerArgumentType.integer(0))
                        .suggests((ctx, builder) -> {
                            ServerHandler.getRegisteredServers().forEach(server -> builder.suggest(
                                    server.getId()
                            ));
                            return builder.buildFuture();
                        })
                        .executes(context -> {
                            Integer serverId = context.getArgument("server_id", Integer.class);
                            RestUtils.sendServerDeleteRequest(serverId);
                            Component message = Component.text(
                                    "Sent deletion signal for server " + serverId,
                                    NamedTextColor.GREEN
                            );
                            CommandSource source = context.getSource();
                            source.sendMessage(message);

                            return Command.SINGLE_SUCCESS;
                        })
                ))
                .build();

        // BrigadierCommand implements Command
        return new BrigadierCommand(helloNode);
    }
}