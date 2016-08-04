package com.web.butler.socket;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class ConnectionProperties {
    private ConnectionProperties() {
    }

    public static Properties getProperties() {
        Properties properties = new Properties();
        try (InputStream open = ConnectionProperties.class.getClassLoader().getResourceAsStream("server.properties")) {
            properties.load(open);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties;
    }
}