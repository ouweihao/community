package com.ouweihao.community.controller;

import com.ouweihao.community.entity.Page;
import com.ouweihao.community.entity.User;
import com.ouweihao.community.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @RequestMapping(path = "/user/list", method = RequestMethod.GET)
    public String getUserListPage(Page page, Model model) {

        // 设置页面信息
        page.setLimit(20);
        page.setPath("/admin/user/list");
        page.setRows(userService.getUserCount());

        List<User> users = userService.findUsers(page.getOffSet(), page.getLimit());

        model.addAttribute("users", users);

        return "/site/admin/user-list";
    }

    // 升为版主

    @RequestMapping(path = "/moderator/{id}", method = RequestMethod.GET)
    public String moderator(@PathVariable(name = "id") int userId, RedirectAttributes attributes) {

        // 0-普通用户; 1-超级管理员; 2-版主; 3-禁言;

        userService.updateType(userId, 2);

        attributes.addFlashAttribute("message", "更新成功");

        return "redirect:/admin/user/list";
    }

    // 降为普通用户

    @RequestMapping(path = "/ordinary/{id}", method = RequestMethod.GET)
    public String ordinary(@PathVariable(name = "id") int userId, RedirectAttributes attributes) {

        // 0-普通用户; 1-超级管理员; 2-版主; 3-禁言;

        userService.updateType(userId, 0);

        attributes.addFlashAttribute("message", "更新成功");

        return "redirect:/admin/user/list";
    }

    // 踢出

    @RequestMapping(path = "/delete/{id}", method = RequestMethod.GET)
    public String deleteUser(@PathVariable(name = "id") int userId, RedirectAttributes attributes) {

        // 0-未激活; 1-已激活; 2-因违反规则被禁用;
        userService.updateStatus(userId, 2);

        attributes.addFlashAttribute("message", "更新成功");

        return "redirect:/admin/user/list";
    }

    // 解除踢出

    @RequestMapping(path = "/undelete/{id}", method = RequestMethod.GET)
    public String unDeleteUser(@PathVariable(name = "id") int userId, RedirectAttributes attributes) {

        // 0-未激活; 1-已激活; 2-因违反规则被禁用;
        userService.updateStatus(userId, 1);

        attributes.addFlashAttribute("message", "更新成功");

        return "redirect:/admin/user/list";
    }

    // 禁言

    @RequestMapping(path = "/forbid/{id}", method = RequestMethod.GET)
    public String forbidUser(@PathVariable(name = "id") int userId, RedirectAttributes attributes) {

        // 0-普通用户; 1-超级管理员; 2-版主; 3-禁言;
        userService.updateType(userId, 3);

        attributes.addFlashAttribute("message", "更新成功");

        return "redirect:/admin/user/list";
    }

    // 解除禁言

    @RequestMapping(path = "/unforbid/{id}", method = RequestMethod.GET)
    public String unForbidUser(@PathVariable(name = "id") int userId, RedirectAttributes attributes) {

        // 0-普通用户; 1-超级管理员; 2-版主; 3-禁言;
        userService.updateType(userId, 0);

        attributes.addFlashAttribute("message", "更新成功");

        return "redirect:/admin/user/list";
    }

}
