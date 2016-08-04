package com.web.butler.socket;

import org.zeromq.ZMQ;

public class DatabaseSocketHandler implements AutoCloseable {
    private ZMQ.Socket requester;

    public DatabaseSocketHandler() {
        requester = ZmqContextHolder.getContext().socket(ZMQ.REQ);
        requester.connect(ConnectionProperties.getProperties().getProperty("database_address"));
    }

    public void send(String message) {
        requester.send(message);
    }

    public String receive() {
        return requester.recvStr();
    }

    @Override
    public void close() {
        requester.close();
    }
}
