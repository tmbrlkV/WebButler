package com.web.config;

import com.web.butler.socket.ConnectionProperties;
import com.web.butler.util.json.JsonMessage;
import com.web.butler.util.json.JsonObjectFactory;
import com.web.entity.Greeting;
import com.web.entity.Message;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Optional;
import java.util.Properties;
import java.util.Scanner;

public class SocketConfig {
    private Socket socket;
    private PrintWriter printWriter;
    private Scanner scanner;
    private static SocketConfig instance;

    public static SocketConfig getInstance() {
        if (instance == null) {
            instance = new SocketConfig();
        }
        return instance;
    }

    private SocketConfig() {
        try {
            Properties properties = ConnectionProperties.getProperties();
            int port = Integer.parseInt(properties.getProperty("butler_port"));
            String address = properties.getProperty("butler_address");
            socket = new Socket(address, port);
            printWriter = new PrintWriter(socket.getOutputStream());
            scanner = new Scanner(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void send(String command, Message message) {
        JsonMessage jsonMessage = new JsonMessage(command, message.getUser(), message.getMessage());
        String string = JsonObjectFactory.getJsonString(jsonMessage);
        printWriter.println(string);
        printWriter.flush();
    }

    public void send(String json) {
        printWriter.println(json);
        printWriter.flush();
    }

    public String receive() {
        try {
            InputStream inputStream = socket.getInputStream();
            byte[] message = new byte[8000];
            int read = inputStream.read(message);
            if (read > 0) {
                return new String(message).trim();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public void receive(SimpMessagingTemplate template) {
        try {
            scanner = new Scanner(socket.getInputStream());
            if (scanner.hasNextLine()) {
                String json = scanner.nextLine();
                JsonMessage objectFromJson = Optional.ofNullable(JsonObjectFactory.getObjectFromJson(json, JsonMessage.class)).orElseGet(JsonMessage::new);
                template.convertAndSend("/topic/greetings", new Greeting(objectFromJson.getUsername() + ": " + objectFromJson.getContent()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void close() {
        try {
            socket.close();
            instance = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
