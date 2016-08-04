package com.web.butler.command;

interface Command {
    String GET_USER_BY_LOGIN_PASSWORD = "getUserByLoginPassword";
    String NEW_USER = "newUser";
    String GET_USER_BY_LOGIN = "getUserByLogin";
    String MESSAGE = "message";
    String NO_COMMAND = "";

    String execute(String request);
}
