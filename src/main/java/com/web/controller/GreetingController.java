package com.web.controller;

import com.web.butler.util.json.JsonMessage;
import com.web.butler.util.json.JsonObjectFactory;
import com.web.entity.Greeting;
import com.web.entity.Message;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

@Controller
public class GreetingController {
    private Socket socket;
    private String toSend;

    {
        read();
    }

    public GreetingController() {
    }

    @MessageMapping("/hello")
    @SendTo("/topic/greetings")
    public Greeting greeting(Message message) throws Exception {
        if (toSend == null) {
            PrintWriter printWriter = new PrintWriter(socket.getOutputStream());
            JsonMessage jsonMessage = new JsonMessage("message", "kek", message.getName());
            String string = JsonObjectFactory.getJsonString(jsonMessage);
            printWriter.println(string);
            printWriter.flush();

            return new Greeting(message.getName());
        } else {
            JsonMessage objectFromJson = JsonObjectFactory.getObjectFromJson(toSend, JsonMessage.class);
            if (objectFromJson != null) {
                Greeting greeting = new Greeting(objectFromJson.getUsername() + ": " + objectFromJson.getContent());
                toSend = null;
                return greeting;
            } else {
                return new Greeting("");
            }
        }
    }

    private void read() {
        new Thread(() -> {
            if (socket == null) {
                try {
                    socket = new Socket("10.66.160.89", 14000);
                    Scanner scanner = new Scanner(socket.getInputStream());
                    while (!Thread.currentThread().isInterrupted()) {
                        toSend = scanner.nextLine().trim();
                        receive();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private String receive() throws Exception {
        System.out.println(toSend + " RECEIVE");
        return toSend;
    }
}
