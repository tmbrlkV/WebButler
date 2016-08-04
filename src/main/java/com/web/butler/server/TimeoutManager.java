package com.web.butler.server;


import com.web.butler.socket.ConnectionProperties;
import com.web.butler.socket.ReceiverSocketHandler;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

class TimeoutManager implements Runnable {
    private Map<SocketChannel, Instant> channels = new ConcurrentHashMap<>();
    private ReceiverSocketHandler receiverSocketHandler;
    private final int threshold;
    private final int threadCheckDelay;
    private static long timeout;

    private Consumer<SocketChannel> handler = channel -> {
        if (Duration.between(channels.get(channel), Instant.now()).getSeconds() > timeout) {
            removeHandle(channel);
            receiverSocketHandler.removeClient(channel);
        }
    };

    TimeoutManager(ReceiverSocketHandler receiverSocketHandler) {
        this.receiverSocketHandler = receiverSocketHandler;
        Properties properties = ConnectionProperties.getProperties();
        threshold = Integer.parseInt(properties.getProperty("connections_threshold"));
        threadCheckDelay = Integer.parseInt(properties.getProperty("thread_check_delay_ms"));
        timeout = Long.parseLong(properties.getProperty("timeout_sec"));
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Thread.sleep(threadCheckDelay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (channels.isEmpty()) {
                continue;
            }

            if (channels.size() > threshold) {
                channels.keySet().parallelStream().forEach(handler);
            } else {
                channels.keySet().forEach(handler);
            }
        }
    }

    void addHandle(SocketChannel channel) {
        channels.put(channel, Instant.now());
    }

    private void removeHandle(SocketChannel channel) {
        channels.remove(channel);
        try {
            channel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
