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

    // user里面有三个值，分别是用户名，密码和邮箱，是从前端传来的，在SpringMVC中，会自动装填到user中

    @RequestMapping(path = "/register", method = RequestMethod.POST)
    public String register(Model model, User user) {
        Map<String, Object> map = userService.register(user);
        if (map == null || map.isEmpty()) {
            model.addAttribute("msg", "注册成功,我们已经向您的邮箱发送了一封激活邮件,请尽快激活！且本系统只限激活用户登入！！！");
            model.addAttribute("target", "/login");
            return "/site/operate-result";
        } else {
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            model.addAttribute("emailMsg", map.get("emailMsg"));
            return "/site/register";
        }
    }

    // 拼接url http://localhost:8080/community/activation/101/code

    @RequestMapping(path = "/activation/{userId}/{code}", method = RequestMethod.GET)
    public String activation(Model model, @PathVariable("userId") int userId, @PathVariable("code") String code) {
        int activationCode = userService.activation(userId, code);
        if (activationCode == ACTIVATION_SUCCESS) {
            model.addAttribute("msg", "恭喜您，注册成功，现在您的账号可以正常使用了！");
            model.addAttribute("target", "/login");
        } else if (activationCode == ACTIVATION_REPEAT) {
            model.addAttribute("msg", "无效操作，该账号已被激活！");
            model.addAttribute("target", "/index");
        } else {
            model.addAttribute("msg", "激活失败，您提供的激活码不正确！");
            model.addAttribute("target", "/index");
        }
        return "/site/operate-result";
    }

    // 生成验证码的方法

    @RequestMapping(path = "/kaptcha", method = RequestMethod.GET)
    public void getKaptcha(HttpServletResponse response/*, HttpSession session*/) {
        // 生成验证码文本
        String text = kaptchaProducer.createText();
        // 生成图片
        BufferedImage image = kaptchaProducer.createImage(text);

        // 将文本存入session传给服务器以便后面进行比对
//        session.setAttribute("kaptcha", text);

        // 将文本存入redis数据库
        String owner = CommunityUtil.generateUUID();
        String kaptchaKey = RedisKeyUtil.getKaptchaKey(owner);
        redisTemplate.opsForValue().set(kaptchaKey, text, 60, TimeUnit.SECONDS);

        Cookie cookie = new Cookie("owner", owner);
        cookie.setMaxAge(60);
        cookie.setPath(contextPath);
        response.addCookie(cookie);

        // 通过cookie将随机生成的owner字符串传给客户端

        // 声明返回什么格式的数据
        response.setContentType("image/png");
        // 将图片显示
        try {
            OutputStream os = response.getOutputStream();
            ImageIO.write(image, "png", os);
        } catch (IOException e) {
            Logger.error("验证码响应失败：" + e.getMessage());
        }
    }

    @RequestMapping(path = "/login", method = RequestMethod.POST)
    public String login(String username, String password, String code, boolean rememberme,
                        Model model, /*HttpSession session,*/ HttpServletResponse response,
                        @CookieValue("owner") String owner) {

        // 得到并检查验证码
        String kaptcha = null;

        // 若cookie中没有owner这个键，则说明验证码已经失效，需要重新生成验证码
        if (StringUtils.isNotBlank(owner)) {
            String kaptchaKey = RedisKeyUtil.getKaptchaKey(owner);
            kaptcha = (String) redisTemplate.opsForValue().get(kaptchaKey);
        }

        if (StringUtils.isBlank(kaptcha) || StringUtils.isBlank(code) || !kaptcha.equalsIgnoreCase(code)) {
            model.addAttribute("codeMsg", "验证码不正确！");
            return "/site/login";
        }

        // 检查账号和密码
        int expiredTime = rememberme ? REMEMBER_EXPIRED_TIME : DEFAULT_EXPIRED_TIME;
        Map<String, Object> map = userService.login(username, password, expiredTime);
        if (map.containsKey("ticket")) {

            // 将ticket存到cookie中发送给服务器
            Cookie cookie = new Cookie("ticket", map.get("ticket").toString());
            cookie.setPath(contextPath);
            cookie.setMaxAge(expiredTime);
            response.addCookie(cookie);

            // 登陆成功，重定向到首页
            return "redirect:/index";
        } else {

            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));

            // 登陆失败，返回登录界面
            return "/site/login";
        }
    }


    @LoginRequired
    @RequestMapping(path = "/logout", method = RequestMethod.GET)
    public String logout(@CookieValue("ticket") String ticket) {
        userService.logout(ticket);

        // 清除权限
        SecurityContextHolder.clearContext();

        return "redirect:/login";
    }

    // 发送包含验证码的邮件

    @RequestMapping(path = "/forgetVerifyCode", method = RequestMethod.GET)
    @ResponseBody
    public String getVerifyCode(String email) {
        Context context = new Context();
        String verifyCode = CommunityUtil.generateUUID().substring(0, 4);

        context.setVariable("email", email);
        context.setVariable("verifyCode", verifyCode);

        // 持久化到redis数据库，方便以后保存，保存时间为5分钟，
        String forgetPasswordKey = RedisKeyUtil.getForgetPasswordKey(email);
        redisTemplate.opsForValue().set(forgetPasswordKey, verifyCode, 5 * 60, TimeUnit.SECONDS);

        String content = templateEngine.process("/mail/forget", context);
        mailClient.sendMail(email, "忘记密码验证码", content);

        return CommunityUtil.getJsonString(0, "成功发送验证码");
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
            attributes.addFlashAttribute("forgetMsg", "找回密码成功！");
            return "redirect:/login";
        } else {
            model.addAttribute("email", email);
            model.addAttribute("emailMsg", errorMsg.get("emailMsg"));
            model.addAttribute("codeMsg", errorMsg.get("codeMsg"));
            return "/site/forget";
        }
    }

}
