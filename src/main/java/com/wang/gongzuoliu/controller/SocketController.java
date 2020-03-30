package com.wang.gongzuoliu.controller;

import com.wang.gongzuoliu.service.WebSocketServer;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;

@Controller
public class SocketController {

    @GetMapping("/socket/{uid}")
    public ModelAndView socket(@PathVariable String uid) {
        ModelAndView modelAndView = new ModelAndView("/index");
        modelAndView.addObject("uid", uid);
        return modelAndView;
    }

    @ResponseBody
    @RequestMapping("/socket/push/{uid}")
    public String pushToWeb(@PathVariable String uid, String message) {
        try {
            WebSocketServer.sendInfo(message, uid);
        }catch (IOException e) {
            e.printStackTrace();
            return "fail";
        }
        return "scesses";
    }
}
