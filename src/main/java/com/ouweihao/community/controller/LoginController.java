package com.ouweihao.community.controller;

import com.google.code.kaptcha.Producer;
import com.ouweihao.community.annotation.LoginRequired;
import com.ouweihao.community.entity.User;
import com.ouweihao.community.service.UserService;
import com.ouweihao.community.util.CommunityConstant;
import com.ouweihao.community.util.CommunityUtil;
import com.ouweihao.community.util.MailClient;
import com.ouweihao.community.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.imageio.ImageIO;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Controller
public class LoginController implements CommunityConstant {

    private static final Logger Logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private Producer kaptchaProducer;

    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private MailClient mailClient;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @RequestMapping(path = "/register", method = RequestMethod.GET)
    public String getRegisterPage() {
        return "/site/register";
    }

    @RequestMapping(path = "/login", method = RequestMethod.GET)
    public String getLoginPage() {
        return "/site/login";
    }

    // user???????????????????????????????????????????????????????????????????????????????????????SpringMVC????????????????????????user???

    @RequestMapping(path = "/register", method = RequestMethod.POST)
    public String register(Model model, User user) {
        Map<String, Object> map = userService.register(user);
        if (map == null || map.isEmpty()) {
            model.addAttribute("msg", "????????????,??????????????????????????????????????????????????????,???????????????????????????????????????????????????????????????");
            model.addAttribute("target", "/login");
            return "/site/operate-result";
        } else {
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            model.addAttribute("emailMsg", map.get("emailMsg"));
            return "/site/register";
        }
    }

    // ??????url http://localhost:8080/community/activation/101/code

    @RequestMapping(path = "/activation/{userId}/{code}", method = RequestMethod.GET)
    public String activation(Model model, @PathVariable("userId") int userId, @PathVariable("code") String code) {
        int activationCode = userService.activation(userId, code);
        if (activationCode == ACTIVATION_SUCCESS) {
            model.addAttribute("msg", "?????????????????????????????????????????????????????????????????????");
            model.addAttribute("target", "/login");
        } else if (activationCode == ACTIVATION_REPEAT) {
            model.addAttribute("msg", "???????????????????????????????????????");
            model.addAttribute("target", "/index");
        } else {
            model.addAttribute("msg", "????????????????????????????????????????????????");
            model.addAttribute("target", "/index");
        }
        return "/site/operate-result";
    }

    // ????????????????????????

    @RequestMapping(path = "/kaptcha", method = RequestMethod.GET)
    public void getKaptcha(HttpServletResponse response/*, HttpSession session*/) {
        // ?????????????????????
        String text = kaptchaProducer.createText();
        // ????????????
        BufferedImage image = kaptchaProducer.createImage(text);

        // ???????????????session???????????????????????????????????????
//        session.setAttribute("kaptcha", text);

        // ???????????????redis?????????
        String owner = CommunityUtil.generateUUID();
        String kaptchaKey = RedisKeyUtil.getKaptchaKey(owner);
        redisTemplate.opsForValue().set(kaptchaKey, text, 60, TimeUnit.SECONDS);

        Cookie cookie = new Cookie("owner", owner);
        cookie.setMaxAge(60);
        cookie.setPath(contextPath);
        response.addCookie(cookie);

        // ??????cookie??????????????????owner????????????????????????

        // ?????????????????????????????????
        response.setContentType("image/png");
        // ???????????????
        try {
            OutputStream os = response.getOutputStream();
            ImageIO.write(image, "png", os);
        } catch (IOException e) {
            Logger.error("????????????????????????" + e.getMessage());
        }
    }

    @RequestMapping(path = "/login", method = RequestMethod.POST)
    public String login(String username, String password, String code, boolean rememberme,
                        Model model, /*HttpSession session,*/ HttpServletResponse response,
                        @CookieValue("owner") String owner) {

        // ????????????????????????
        String kaptcha = null;

        // ???cookie?????????owner????????????????????????????????????????????????????????????????????????
        if (StringUtils.isNotBlank(owner)) {
            String kaptchaKey = RedisKeyUtil.getKaptchaKey(owner);
            kaptcha = (String) redisTemplate.opsForValue().get(kaptchaKey);
        }

        if (StringUtils.isBlank(kaptcha) || StringUtils.isBlank(code) || !kaptcha.equalsIgnoreCase(code)) {
            model.addAttribute("codeMsg", "?????????????????????");
            return "/site/login";
        }

        // ?????????????????????
        int expiredTime = rememberme ? REMEMBER_EXPIRED_TIME : DEFAULT_EXPIRED_TIME;
        Map<String, Object> map = userService.login(username, password, expiredTime);
        if (map.containsKey("ticket")) {

            // ???ticket??????cookie?????????????????????
            Cookie cookie = new Cookie("ticket", map.get("ticket").toString());
            cookie.setPath(contextPath);
            cookie.setMaxAge(expiredTime);
            response.addCookie(cookie);

            // ?????????????????????????????????
            return "redirect:/index";
        } else {

            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));

            // ?????????????????????????????????
            return "/site/login";
        }
    }


    @LoginRequired
    @RequestMapping(path = "/logout", method = RequestMethod.GET)
    public String logout(@CookieValue("ticket") String ticket) {
        userService.logout(ticket);

        // ????????????
        SecurityContextHolder.clearContext();

        return "redirect:/login";
    }

    // ??????????????????????????????

    @RequestMapping(path = "/forgetVerifyCode", method = RequestMethod.GET)
    @ResponseBody
    public String getVerifyCode(String email) {
        Context context = new Context();
        String verifyCode = CommunityUtil.generateUUID().substring(0, 4);

        context.setVariable("email", email);
        context.setVariable("verifyCode", verifyCode);

        // ????????????redis????????????????????????????????????????????????5?????????
        String forgetPasswordKey = RedisKeyUtil.getForgetPasswordKey(email);
        redisTemplate.opsForValue().set(forgetPasswordKey, verifyCode, 5 * 60, TimeUnit.SECONDS);

        String content = templateEngine.process("/mail/forget", context);
        mailClient.sendMail(email, "?????????????????????", content);

        return CommunityUtil.getJsonString(0, "?????????????????????");
    }

    @RequestMapping(path = "/forget", method = RequestMethod.GET)
    public String getForgetPage() {
        return "/site/forget";
    }

    @RequestMapping(path = "/forget", method = RequestMethod.POST)
    public String forget(@RequestParam(name = "your-email") String email,
                         @RequestParam(name = "verifycode") String verifyCode,
                         @RequestParam(name = "your-password") String newPassword,
                         Model model, RedirectAttributes attributes) {

//        System.out.println("email = " + email);
//        System.out.println("verifyCode = " + verifyCode);
//        System.out.println("newPassword = " + newPassword);

        Map<String, Object> errorMsg = userService.forgetPassword(email, verifyCode, newPassword);
        if (errorMsg == null || errorMsg.isEmpty()) {
            attributes.addFlashAttribute("forgetStatus", 1);
            attributes.addFlashAttribute("forgetMsg", "?????????????????????");
            return "redirect:/login";
        } else {
            model.addAttribute("email", email);
            model.addAttribute("emailMsg", errorMsg.get("emailMsg"));
            model.addAttribute("codeMsg", errorMsg.get("codeMsg"));
            return "/site/forget";
        }
    }

}
