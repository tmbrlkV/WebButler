package com.web.butler.server;

import java.nio.channels.SocketChannel;

public class ServerDataEvent {
    private NioServer server;
    private SocketChannel socket;
    private byte[] data;

    ServerDataEvent(NioServer server, SocketChannel socket, byte[] data) {
        this.server = server;
        this.socket = socket;
        this.data = data;
    }

    NioServer getServer() {
        return server;
    }

    public SocketChannel getSocket() {
        return socket;
    }

    public byte[] getData() {
        return data;
    }
}