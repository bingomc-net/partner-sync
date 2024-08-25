package net.bingomc.partnersync;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import lombok.Getter;
import net.bingomc.partnersync.model.Server;
import net.bingomc.partnersync.model.ServerState;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ServerHandler {
    @Getter
    private static final List<Server> servers = new ArrayList<>();
    @Getter
    private static final List<Server> registeredServers = new ArrayList<>();

    public static void sendPlayerToServer(Server bingoServer, UUID uuid) {
        ProxyServer proxyServer = PartnerSyncPlugin.getServer();
        Optional<Player> player = proxyServer.getPlayer(uuid);
        if (player.isEmpty()) return;

        Optional<RegisteredServer> server = proxyServer.getServer(prefixServer(bingoServer.getId()));

        proxyServer.getScheduler()
                .buildTask(PartnerSyncPlugin.getInstance(), scheduledTask -> {
                    server.ifPresent(registeredServer -> {
                        player.get().createConnectionRequest(registeredServer).connect();

                        Optional<ServerConnection> playerServer = player.get().getCurrentServer();
                        if (playerServer.isPresent()) {
                            if (playerServer.get().getServer().equals(registeredServer)) {
                                scheduledTask.cancel();
                            }
                        }
                    });
                })
                .repeat(Duration.ofSeconds(1L))
                .schedule();
    }

    public static void updateServers() {
        synchronized (servers) {
            List<Server> receivedServers;

            try {
                receivedServers = RestUtils.sendServerGetRequest();
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }

            servers.clear();
            registeredServers.clear();
            servers.addAll(receivedServers);

            for (Server server : servers) {
                ServerState state = server.getState();

                switch (state) {
                    case WAITING, RUNNING -> {
                        if (!registeredServers.contains(server)) {
                            registeredServers.add(server);
                            registerServer(server);
                        }
                    }
                    case DONE -> unregisterServer(server);
                }
            }
        }
    }

    public static Server requestServer() {
        Server server;
        server = RestUtils.sendServerPostRequest();
        if (server != null) {
            registerServer(server);
        }
        return server;
    }

    public static void registerServer(Server server) {
        InetAddress address = ipToAddress(server.getIp());
        ServerInfo serverInfo = new ServerInfo(prefixServer(server.getId()), new InetSocketAddress(address, server.getPort()));
        PartnerSyncPlugin.getServer().registerServer(serverInfo);
        if (!registeredServers.contains(server)) {
            registeredServers.add(server);
        }
    }

    public static void unregisterServer(Server server) {
        Optional<RegisteredServer> registeredServer = PartnerSyncPlugin.getServer().getServer(prefixServer(server.getId()));
        registeredServer.ifPresent(deletedServer -> PartnerSyncPlugin.getServer().unregisterServer(deletedServer.getServerInfo()));
        registeredServers.remove(server);
    }

    public static String prefixServer(int id) {
        return "bingo_" + id;
    }

    private static InetAddress ipToAddress(String ip) {
        try {
            return InetAddress.getByName(ip);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }
}