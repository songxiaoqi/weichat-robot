package com.ypwh.robot.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import wechat.WeChatDemo;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * author woson
 * date 2019/7/3
 * description
 */
@Controller
public class ChatRoomController {

    @RequestMapping(value = "/addAccount",method = {RequestMethod.POST})
    @ResponseBody
    public String addAccount(HttpServletRequest request, HttpServletResponse response){

        return "";
    }

}
