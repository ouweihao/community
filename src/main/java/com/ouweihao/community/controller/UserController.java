package com.ouweihao.community.controller;

import com.ouweihao.community.entity.User;
import com.ouweihao.community.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(path = "/User")
public class UserController {

    @Autowired
    private UserService userService;

    @RequestMapping(path = "/getUserById", method = RequestMethod.GET)
    @ResponseBody
    public User getUserById(int id) {
        return userService.findUserById(id);
    }

    @RequestMapping(path = "getUserByName", method = RequestMethod.GET)
    @ResponseBody
    public User getUserByName(String name) {
        return userService.findUserByName(name);
    }

}
