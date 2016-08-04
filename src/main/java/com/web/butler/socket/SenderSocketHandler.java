package com.web.butler.socket;

import org.zeromq.ZMQ;

public class SenderSocketHandler implements AutoCloseable {
    private ZMQ.Socket sender;

    public SenderSocketHandler() {
        sender = ZmqContextHolder.getContext().socket(ZMQ.PUSH);
        sender.connect(ConnectionProperties.getProperties().getProperty("chat_sender_address"));
    }

    public void send(String message) {
        sender.send(message);
    }

    @Override
    public void close() throws Exception {
        sender.close();
    }
}
