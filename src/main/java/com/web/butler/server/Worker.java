package com.web.butler.server;


import com.web.butler.command.CommandManager;

import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.List;

public class Worker implements Runnable {
    private final List<ServerDataEvent> queue = new LinkedList<>();
    private CommandManager commandManager = new CommandManager();

    void processData(NioServer server, SocketChannel socket, byte[] data, int count) {
        byte[] dataCopy = new byte[count];
        System.arraycopy(data, 0, dataCopy, 0, count);
        synchronized (queue) {
            queue.add(new ServerDataEvent(server, socket, dataCopy));
            queue.notify();
        }
    }

    public void run() {
        ServerDataEvent dataEvent;
        while (!Thread.currentThread().isInterrupted()) {
            synchronized (queue) {
                while (queue.isEmpty()) {
                    try {
                        queue.wait();
                    } catch (InterruptedException ignored) {
                    }
                }
                dataEvent = queue.remove(0);
            }
            String data = commandManager.execute(dataEvent);
            dataEvent.getServer().send(dataEvent.getSocket(), data.getBytes());
        }
    }
}