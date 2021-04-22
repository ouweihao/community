package com.ouweihao.community.controller;

import com.ouweihao.community.util.CommunityUtil;
import io.swagger.annotations.ApiOperation;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

@Controller
@RequestMapping("/alpha")
public class AlphaController {

    @ApiOperation("测试方法")
    @GetMapping("/test")
    public String test() {
        return "test";
    }

    @RequestMapping("/http")
    public void http(HttpServletRequest request, HttpServletResponse response) {
        // 获取请求数据

        System.out.println(request.getMethod());
        // 输出请求路径
        System.out.println(request.getContextPath());
        System.out.println(request.getServletPath());
        Enumeration<String> headers = request.getHeaderNames();
        while (headers.hasMoreElements()) {
            String key = headers.nextElement();
            String value = request.getHeader(key);
            System.out.println(key + " : " + value);
        }

        System.out.println(request.getParameter("code"));

        // 返回响应数据

        response.setContentType("text/html;charset=utf-8");
        try (
                // 这样写的话就不用自己关闭流，前提是该对象有close()方法
                PrintWriter writer = response.getWriter();
        ) {
            writer.println("<h1>Ouweihao</h1>");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    // GET请求

    @RequestMapping(path = "/getStudent", method = RequestMethod.GET)
    @ResponseBody
    public String queryStudents(
            @RequestParam(name = "currentPage", required = false, defaultValue = "1") int currentPage,
            @RequestParam(name = "limit", required = false, defaultValue = "10") int limit) {
        System.out.println(currentPage);
        System.out.println(limit);
        return "123";
    }

    @RequestMapping(path = "/student/{id}", method = RequestMethod.GET)
    @ResponseBody
    public String getStudent(@PathVariable(name = "id") int id) {
        System.out.println(id);
        return "123";
    }

    // Post请求

    @RequestMapping(path = "/student", method = RequestMethod.POST)
    @ResponseBody
    public String insertStudent(String name, int age) {
        System.out.println(name);
        System.out.println(age);
        return "success";
    }

    @RequestMapping(path = "/teacher", method = RequestMethod.GET)
    public ModelAndView teacher(@PathVariable(name = "name") String name, @PathVariable(name = "age") int age) {
        ModelAndView mav = new ModelAndView();
        mav.addObject("name", name);
        mav.addObject("age", age);
        mav.setViewName("/demo/view");
        return mav;
    }

    @RequestMapping(path = "/school", method = RequestMethod.GET)
    public String school(Model model) {
        model.addAttribute("name", "长沙理工大学");
        model.addAttribute("age", 100);
        return "/demo/view";
    }

    // 响应JSON数据（异步请求）

    @RequestMapping(path = "/emp", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> getEmp() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "ouweihao");
        map.put("age", 21);
        map.put("salary", 5000);
        return map;
    }

    @RequestMapping(path = "/emps", method = RequestMethod.GET)
    @ResponseBody
    public List<Map<String, Object>> getEmps() {
        ArrayList<Map<String, Object>> emps = new ArrayList<>();
        HashMap<String, Object> map = new HashMap<>();
        map.put("name", "张三");
        map.put("age", 23);
        map.put("salary", 8000.00);
        emps.add(map);

        map = new HashMap<>();
        map.put("name", "李四");
        map.put("age", 25);
        map.put("salary", 5000.00);
        emps.add(map);

        map = new HashMap<>();
        map.put("name", "王五");
        map.put("age", 34);
        map.put("salary", 10000.00);
        emps.add(map);

        return emps;
    }

    // cookie示例

    @RequestMapping(path = "/cookie/set", method = RequestMethod.GET)
    @ResponseBody
    public String setCookie(HttpServletResponse response) {

        Cookie code = new Cookie("code", CommunityUtil.generateUUID());

        // 设置cookie的生效范围
        code.setPath("/community/alpha");
        // 设置cookie的生存时间
        code.setMaxAge(60 * 10);

        response.addCookie(code);

        return "set cookie";
    }

    @RequestMapping(path = "/cookie/get", method = RequestMethod.GET)
    @ResponseBody
    public String getCookie(@CookieValue("code") String code) { // 从cookie数组中取得键为code的值
        System.out.println(code);
        return "get cookie";
    }

    // session示例

    @RequestMapping(path = "/session/set", method = RequestMethod.GET)
    @ResponseBody
    public String setSession(HttpSession session) {

        session.setAttribute("id", 1);
        session.setAttribute("name", "ouweihao");

        return "set session";
    }

    @RequestMapping(path = "/session/get", method = RequestMethod.GET)
    @ResponseBody
    public String getSession(HttpSession session) {

        System.out.println(session.getAttribute("id"));
        System.out.println(session.getAttribute("name"));

        return "get session";
    }

    // ajax示例

    @RequestMapping(path = "/ajax", method = RequestMethod.POST)
    @ResponseBody
    public String testAjax(String name, int age) {
        System.out.println(name);
        System.out.println(age);
        return CommunityUtil.getJsonString(0, "操作完成!");
    }

}
