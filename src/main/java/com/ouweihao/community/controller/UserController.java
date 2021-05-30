package com.ouweihao.community.controller;

import com.ouweihao.community.annotation.LoginRequired;
import com.ouweihao.community.dao.LoginTicketMapper;
import com.ouweihao.community.entity.User;
import com.ouweihao.community.service.FollowService;
import com.ouweihao.community.service.LikeService;
import com.ouweihao.community.service.UserService;
import com.ouweihao.community.util.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Controller
@RequestMapping(path = "/user")
public class UserController implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private LoginTicketMapper loginTicketMapper;

    @Autowired
    private LikeService likeService;

    @Autowired
    private FollowService followService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private MailClient mailClient;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @LoginRequired
    @RequestMapping(path = "/setting", method = RequestMethod.GET)
    public String getSettingPage(Model model) {
        model.addAttribute("currentUser", hostHolder.getUser());
        return "/site/setting";
    }

    @LoginRequired
    @RequestMapping(path = "/upload", method = RequestMethod.POST)
    public String uploadHeader(MultipartFile headerImage, Model model) {
        if (headerImage == null) {
            model.addAttribute("error", "您还未添加图片！");
            return "/site/setting";
        }

        // 得到初始的文件名
        String filename = headerImage.getOriginalFilename();

        // 得到文件的后缀名
        String suffix = null;
        if (filename.lastIndexOf('.') != -1) {
            suffix = filename.substring(filename.lastIndexOf('.'));
        }
        if (!StringUtils.isBlank(suffix)) {
            if (!".png".equals(suffix) && !".jpg".equals(suffix) && !".jpeg".equals(suffix)) {
                suffix = null;
                if (StringUtils.isBlank(suffix)) {
                    model.addAttribute("error", "文件格式不正确！！！");
                    return "/site/setting";
                }
            }
        } else {
            model.addAttribute("error", "文件格式不正确！！！");
            return "/site/setting";
        }

        // 生成随机文件名
        filename = CommunityUtil.generateUUID() + suffix;
        // 确定文件存在的路径
        File dest = new File(uploadPath + "/" + filename);

        try {
            headerImage.transferTo(dest);
        } catch (IOException e) {
            logger.error("上传文件失败：" + e.getMessage());
            throw new RuntimeException("上传文件失败！服务器发生异常！", e);
        }

        // 更新当前用户的头像的路径(web访问路径)
        // http://localhost:8080/community/user/header/xxx.png
        User user = hostHolder.getUser();
        String headerUrl = domain + contextPath + "/user/header/" + filename;
        userService.updateHeader(user.getId(), headerUrl);

        return "redirect:/index";
    }

//    @LoginRequired

    @RequestMapping(path = "/header/{fileName}", method = RequestMethod.GET)
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response) {
        // 服务器存放文件的路径
        fileName = uploadPath + "/" + fileName;

        // 获取文件后缀
        String suffix = fileName.substring(fileName.lastIndexOf('.'));

        // 响应图片
        response.setContentType("image/" + suffix);

        try (
                FileInputStream fis = new FileInputStream(fileName);
                OutputStream os = response.getOutputStream();
        ) {
            byte[] buffer = new byte[1024];
            int b = 0; // offset
            while ((b = fis.read(buffer)) != -1) {
                os.write(buffer, 0, b);
            }
        } catch (Exception e) {
            logger.error("读取头像失败：" + e.getMessage());
        }

    }

    @LoginRequired
    @RequestMapping(path = "/updatepassword", method = RequestMethod.POST)
    public String updatePassword(String password, @CookieValue("ticket") String ticket,
                                 String newPassword, String newPasswordConfirmed, Model model) {
        // 取出当前登录的用户
        User user = hostHolder.getUser();

        // 检查输入的原密码是否正确
        if (!user.getPassword().equals(CommunityUtil.MD5(password + user.getSalt()))) {
            model.addAttribute("updatePasswordMsg", "原密码错误");
            return "/site/setting";
        }

        // 检查两次输入的密码是否相同
        if (!newPassword.equals(newPasswordConfirmed)) {
            model.addAttribute("ConfirmedPasswordMsg", "两次输入的新密码不正确！");
            return "/site/setting";
        }

        // 检查新密码和原密码是否相同
        if (password.equals(newPasswordConfirmed)) {
            model.addAttribute("updatePasswordMsg", "原密码和新密码相同！");
            return "/site/setting";
        }

        // 更新用户密码
        userService.updatePassword(user.getId(), newPassword);

        // 将用户的登录凭设为过期，然后再到登录界面
        // 得到loginTicket
        // LoginTicket loginTicket = loginTicketMapper.selectLoginTicket(ticket);

        userService.logout(ticket);
        return "redirect:/login";

    }

    @RequestMapping(path = "/profile/{userId}", method = RequestMethod.GET)
    public String getProfilePage(@PathVariable("userId") int userId, Model model) {
        // 得到用户
        User user = userService.findUserById(userId);
        model.addAttribute("user", user);

        // 得到他获得了多少个赞
        int likeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount", likeCount);

        // 得到关注数量
        long followeeCount = followService.findFolloweeCount(userId, ENTITY_USER);
        model.addAttribute("followeeCount", followeeCount);

        // 得到粉丝数量
        long followerCount = followService.findFollowerCount(ENTITY_USER, userId);
        model.addAttribute("followerCount", followerCount);

        // 关注状态
        boolean hasFollowed = false;
        if (hostHolder.getUser() != null) {
            hasFollowed = followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_USER, userId);
        }
        model.addAttribute("hasFollowed", hasFollowed);


        return "/site/profile";
    }

    // 发送包含验证码的邮件

    @RequestMapping(path = "/updateVerifyCode", method = RequestMethod.GET)
    @ResponseBody
    public String getUpdateVerifyCode(String email) {
        Context context = new Context();
        String verifyCode = CommunityUtil.generateUUID().substring(0, 4);

        context.setVariable("email", email);
        context.setVariable("verifyCode", verifyCode);

        // 持久化到redis数据库，方便以后保存，保存时间为5分钟，
        String updateEmailKey = RedisKeyUtil.getUpdateEmailKey(email);
        redisTemplate.opsForValue().set(updateEmailKey, verifyCode, 5 * 60, TimeUnit.SECONDS);

        String content = templateEngine.process("/mail/updateEmailVerifyCode", context);
        mailClient.sendMail(email, "修改改邮箱验证码", content);

        return CommunityUtil.getJsonString(0, "成功发送验证码");
    }

    @RequestMapping(path = "/activateVerifyCode", method = RequestMethod.GET)
    @ResponseBody
    public String getActivateVerifyCode(String email) {
        Context context = new Context();
        String verifyCode = CommunityUtil.generateUUID().substring(0, 4);

        context.setVariable("email", email);
        context.setVariable("verifyCode", verifyCode);

        // 持久化到redis数据库，方便以后保存，保存时间为5分钟，
        String activateEmailKey = RedisKeyUtil.getActivateEmailKey(email);
        redisTemplate.opsForValue().set(activateEmailKey, verifyCode, 5 * 60, TimeUnit.SECONDS);

        String content = templateEngine.process("/mail/activateEmailVerifyCode", context);
        mailClient.sendMail(email, "激活新邮箱验证码", content);

        return CommunityUtil.getJsonString(0, "成功发送验证码");
    }

    @RequestMapping(path = "/updateemail", method = RequestMethod.POST)
    public String updateEmail(@RequestParam(name = "former-email") String formerEmail,
                              @RequestParam(name = "updateVerifyCode") String updateVerifyCode,
                              @RequestParam(name = "new-email") String newEmail,
                              @RequestParam(name = "activationVerifyCode") String activationVerifyCode,
                              Model model, RedirectAttributes attributes,
                              @CookieValue("ticket") String ticket) {

       /* System.out.println("formerEmail = " + formerEmail);
        System.out.println("updateVerifyCode = " + updateVerifyCode);
        System.out.println("newEmail = " + newEmail);
        System.out.println("activationVerifyCode = " + activationVerifyCode);*/

        Map<String, Object> updateEmailMsg = userService.updateEmail(formerEmail,
                updateVerifyCode, newEmail, activationVerifyCode);
        if (updateEmailMsg == null || updateEmailMsg.isEmpty()) {
            attributes.addFlashAttribute("forgetStatus", 1);
            attributes.addFlashAttribute("forgetMsg", "修改邮箱成功！！");
            userService.logout(ticket);
            return "redirect:/login";
        } else {
            model.addAttribute("formerEmailMsg", updateEmailMsg.get("formerEmailMsg"));
            model.addAttribute("updateVerifyCodeMsg", updateEmailMsg.get("updateVerifyCodeMsg"));
            model.addAttribute("newEmailMsg", updateEmailMsg.get("newEmailMsg"));
            model.addAttribute("activationVerifyCodeMsg", updateEmailMsg.get("activationVerifyCodeMsg"));
            return "/site/setting";
        }

    }

}
