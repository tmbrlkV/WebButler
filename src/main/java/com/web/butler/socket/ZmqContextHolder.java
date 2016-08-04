package com.web.butler.socket;

import org.zeromq.ZMQ;

final class ZmqContextHolder {
    private static ZMQ.Context context;

    private ZmqContextHolder() {}

    static ZMQ.Context getContext() {
        if (context == null) {
            synchronized (ZmqContextHolder.class) {
                if (context == null) {
                    context = ZMQ.context(1);
                }
            }
        }
        return context;
    }
}
