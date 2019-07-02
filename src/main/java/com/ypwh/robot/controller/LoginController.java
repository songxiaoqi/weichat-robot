package com.ypwh.robot.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.ypwh.robot.model.User;
import com.ypwh.robot.service.UserService;

/**
 * 登陆
 */
@Controller
public class LoginController {

    @Autowired
    private UserService userService;

    @RequestMapping(value = {"/loginPage","/"},method = {RequestMethod.GET})
    public ModelAndView loginPage(HttpServletRequest request, HttpServletResponse response){
        return new ModelAndView("login");
    }

    @RequestMapping(value = {"/mainPage"},method = {RequestMethod.GET})
    public ModelAndView mainPage(HttpServletRequest request, HttpServletResponse response){
        return new ModelAndView("main");
    }

    @RequestMapping(value = {"/mainPage1"},method = {RequestMethod.GET})
    public ModelAndView mainPage1(HttpServletRequest request, HttpServletResponse response){
        return new ModelAndView("main1");
    }

    @RequestMapping(value = "/login",method = {RequestMethod.GET})
    @ResponseBody
    public User login(HttpServletRequest request, HttpServletResponse response, User user){
        User user1 = userService.selectUserByUsernameAndPassword(user);
        if(null==user1){
            return null;
        }
        request.getSession().setAttribute("token_user",user1);
        return user1;
    }


}
