package net.bingomc.partnersync.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Server {
    int id;
    int port;
    int players;

    String ip;
    ServerState state;
}
