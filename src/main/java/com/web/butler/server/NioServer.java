package com.web.butler.server;

import com.web.butler.socket.ReceiverSocketHandler;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.*;

public class NioServer implements Runnable {
    private InetAddress hostAddress;
    private int port;
    private Selector selector;
    private ByteBuffer readBuffer = ByteBuffer.allocate(8192);
    private Worker worker;

    private final List<ChangeRequest> pendingChanges = new LinkedList<>();

    private final Map<SocketChannel, List<ByteBuffer>> pendingData = new HashMap<>();

    private ReceiverSocketHandler receiver;
    private final Thread receiverThread;

    private TimeoutManager timeoutManager;
    private final Thread timeoutManagerThread;

    public NioServer(InetAddress hostAddress, int port, Worker worker) throws IOException {
        this.hostAddress = hostAddress;
        this.port = port;
        selector = initSelector();
        this.worker = worker;
        receiver = new ReceiverSocketHandler(this);
        receiverThread = new Thread(receiver);
        receiverThread.start();
        timeoutManager = new TimeoutManager(receiver);
        timeoutManagerThread = new Thread(timeoutManager);
        timeoutManagerThread.start();
    }

    public void send(SocketChannel channel, byte[] data) {
        synchronized (pendingChanges) {
            pendingChanges.add(new ChangeRequest(channel, ChangeRequest.CHANGER, SelectionKey.OP_WRITE));
            queueWrite(channel, data);
        }
        selector.wakeup();
    }

    private void queueWrite(SocketChannel socket, byte[] data) {
        synchronized (pendingData) {
            List<ByteBuffer> queue = pendingData.get(socket);
            if (queue == null) {
                queue = new ArrayList<>();
                pendingData.put(socket, queue);
            }
            queue.add(ByteBuffer.wrap(data));
        }
    }

    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                synchronized (pendingChanges) {
                    for (ChangeRequest change : pendingChanges) {
                        switch (change.getType()) {
                            case ChangeRequest.CHANGER:
                                SelectionKey key = change.getSocket().keyFor(selector);
                                if (key != null && key.isValid()) {
                                    key.interestOps(change.getOps());
                                }
                        }
                    }
                    pendingChanges.clear();
                }
                selector.select();
                Iterator selectedKeys = selector.selectedKeys().iterator();
                while (selectedKeys.hasNext()) {
                    SelectionKey key = (SelectionKey) selectedKeys.next();
                    selectedKeys.remove();

                    if (!key.isValid()) {
                        continue;
                    }
                    if (key.isAcceptable()) {
                        accept(key);
                    } else if (key.isReadable()) {
                        read(key);
                    } else if (key.isWritable()) {
                        write(key);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                receiverThread.interrupt();
                timeoutManagerThread.interrupt();
            }
        }
    }

    private void accept(SelectionKey key) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        SocketChannel socketChannel = serverSocketChannel.accept();
        socketChannel.configureBlocking(false);
        receiver.addClient(socketChannel);
        socketChannel.register(selector, SelectionKey.OP_READ);
    }

    private void read(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        readBuffer.clear();
        int numRead;
        try {
            numRead = socketChannel.read(readBuffer);
        } catch (IOException e) {
            key.cancel();
            receiver.removeClient((SocketChannel) key.channel());
            socketChannel.close();
            return;
        }

        if (numRead == -1) {
            receiver.removeClient((SocketChannel) key.channel());
            key.channel().close();
            key.cancel();
            return;
        }
        worker.processData(this, socketChannel, readBuffer.array(), numRead);
        timeoutManager.addHandle(socketChannel);
    }

    private void write(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();

        synchronized (pendingData) {
            List<ByteBuffer> queue = pendingData.get(socketChannel);
            while (!queue.isEmpty()) {
                ByteBuffer buf = queue.get(0);
                try {
                    socketChannel.write(buf);
                } catch (IOException e) {
                    key.channel().close();
                    key.channel();
                }
                if (buf.remaining() > 0) {
                    break;
                }
                queue.remove(0);
            }

            if (queue.isEmpty()) {
                key.interestOps(SelectionKey.OP_READ);
            }
        }
    }

    private Selector initSelector() throws IOException {
        Selector socketSelector = SelectorProvider.provider().openSelector();
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);
        InetSocketAddress isa = new InetSocketAddress(hostAddress, port);
        serverChannel.socket().bind(isa);
        serverChannel.register(socketSelector, SelectionKey.OP_ACCEPT);

        return socketSelector;
    }
}