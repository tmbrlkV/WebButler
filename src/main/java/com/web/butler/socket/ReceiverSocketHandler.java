package com.web.butler.socket;

import com.web.butler.server.NioServer;
import org.zeromq.ZMQ;

import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class ReceiverSocketHandler implements Runnable {
    private static int CUTOFF;
    private ZMQ.Socket receiver;
    private NioServer server;
    private ZMQ.Poller poller;
    private List<SocketChannel> clients = new CopyOnWriteArrayList<>();

    public ReceiverSocketHandler(NioServer server) {
        Properties properties = ConnectionProperties.getProperties();
        CUTOFF = Integer.parseInt(properties.getProperty("connections_threshold"));
        this.server = server;
        receiver = ZmqContextHolder.getContext().socket(ZMQ.SUB);
        receiver.connect(properties.getProperty("chat_receiver_address"));
        receiver.subscribe("".getBytes());
        poller = new ZMQ.Poller(0);
        poller.register(receiver, ZMQ.Poller.POLLIN);
    }

    public void addClient(SocketChannel channel) {
        if (!clients.contains(channel)) {
            clients.add(channel);
        }
    }

    public void removeClient(SocketChannel channel) {
        int i = clients.indexOf(channel);
        if (i >= 0) {
            clients.remove(i);
        }
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            int events = poller.poll();
            if (events > 0) {
                String reply = receiver.recvStr();
                Consumer<SocketChannel> handler =  channel -> {
                    server.send(channel, (reply + "\n").getBytes());
                };

                if (clients.size() > CUTOFF) {
                    clients.parallelStream().forEach(handler);
                } else {
                    clients.forEach(handler);
                }
            }
        }
    }
}
