package com.web.butler.util.json;

public class JsonMessage {
    private String command;
    private String username;
    private String content;

    private int to;
    private int from;

    public JsonMessage() {
    }

    public JsonMessage(String command, String username, String content) {
        this.command = command;
        this.username = username;
        this.content = content;
    }

    public void setTo(int to) {
        this.to = to;
    }

    public void setFrom(int from) {
        this.from = from;
    }

    public String getCommand() {
        return command;
    }

    public String getContent() {
        return content;
    }

    public String getUsername() {
        return username;
    }

    public int getTo() {
        return to;
    }

    public int getFrom() {
        return from;
    }


}
