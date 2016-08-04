package com.web.butler.server;

import java.nio.channels.SocketChannel;

public class ChangeRequest {
    public static final int REGISTER = 1;
    public static final int CHANGER = 2;

    private SocketChannel socket;
    private int type;
    private int ops;

    public ChangeRequest(SocketChannel socket, int type, int ops) {
        this.socket = socket;
        this.type = type;
        this.ops = ops;
    }

    public SocketChannel getSocket() {
        return socket;
    }

    public int getType() {
        return type;
    }

    public int getOps() {
        return ops;
    }
}
