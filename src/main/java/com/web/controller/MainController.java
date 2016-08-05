package com.web.controller;

import com.web.config.SocketConfig;
import com.web.entity.Greeting;
import com.web.entity.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class MainController {
    private static Thread thread;
    private static SocketConfig instance;


    @RequestMapping("/init")
    public ModelAndView init() throws Exception {
        instance = SocketConfig.getInstance();
        startReceivingThread();
        return new ModelAndView("redirect:/");
    }

    @RequestMapping("/login")
    public ModelAndView logout() throws Exception {
        if (thread != null) {
            thread.interrupt();
            thread = null;
            instance.close();
        }
        return new ModelAndView("login");
    }

    @MessageMapping("/hello")
    @SendTo("/topic/greetings")
    public Greeting greeting(Message message) throws Exception {
        instance.send("message", message);
        return new Greeting("");
    }


    @Autowired
    private SimpMessagingTemplate template;

    private void startReceivingThread() {
        if (thread == null) {
            thread = new Thread(() -> {
                while (!Thread.currentThread().isInterrupted()) {
                    instance.receive(template);
                }
            });
            thread.start();
        }
    }
}
