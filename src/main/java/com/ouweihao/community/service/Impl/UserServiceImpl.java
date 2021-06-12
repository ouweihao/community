package com.ouweihao.community.service.Impl;

import com.ouweihao.community.dao.UserMapper;
import com.ouweihao.community.entity.LoginTicket;
import com.ouweihao.community.entity.User;
import com.ouweihao.community.service.UserService;
import com.ouweihao.community.util.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements UserService, CommunityConstant {

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

//    @Autowired
//    private LoginTicketMapper loginTicketMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Autowired
    private RedisTemplate redisTemplate;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Override
    public User findUserById(int id) {
//        return userMapper.selectById(id);
        User user = getCacheUser(id);
        if (user == null) {
            user = initCacheUser(id);
        }
        return user;
    }

    // 1. 优先从缓存中取值

    private User getCacheUser(int userId) {
        String userKey = RedisKeyUtil.getUserKey(userId);
        return (User) redisTemplate.opsForValue().get(userKey);
    }

    // 2. 若缓存中没有则初始化存入缓存

    private User initCacheUser(int userId) {
        User user = userMapper.selectById(userId);
        String userKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.opsForValue().set(userKey, user, 1, TimeUnit.HOURS);
        return user;
    }

    // 3. 变更时删除缓存

    private void clearCacheUser(int userId) {
        String userKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.delete(userKey);
    }


    @Override
    public User findUserByName(String username) {
        return userMapper.selectByName(username);
    }

    @Override
    public Map<String, Object> register(User user) {
        Map<String, Object> map = new HashMap<>();

        // 空值处理
        if (user == null) {
            throw new IllegalArgumentException("参数不能为空！");
        }
        if (user.getUsername() == null) {
            map.put("usernameMsg", "账号不能为空！");
            return map;
        }
        if (user.getPassword() == null) {
            map.put("passwordMsg", "密码不能为空！");
            return map;
        }
        if (user.getEmail() == null) {
            map.put("emailMsg", "邮箱不能为空！");
            return map;
        }

        // 验证账号
        User u = userMapper.selectByName(user.getUsername());
        if (u != null) {
            map.put("usernameMsg", "该用户名已被占用！");
            return map;
        }

        // 验证邮箱
        u = userMapper.selectByEmail(user.getEmail());
        if (u != null) {
            map.put("emailMsg", "该邮箱已被注册！");
            return map;
        }

        // 过滤
        user.setUsername(sensitiveFilter.filter(user.getUsername()));

        // 注册用户，补全用户中缺失的信息
        user.setSalt(CommunityUtil.generateUUID().substring(0, 5));
        user.setPassword(CommunityUtil.MD5(user.getPassword() + user.getSalt()));
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(CommunityUtil.generateUUID());
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        user.setCreateTime(new Date());
        userMapper.insertUser(user);

        // 发送激活邮件
        Context context = new Context();
        context.setVariable("email", user.getEmail());
        // 拼接url http://localhost:8080/community/activation/101/code
        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url", url);
        String content = templateEngine.process("/mail/activation", context);
        mailClient.sendMail(user.getEmail(), "激活邮件", content);

        return map;
    }

    @Override
    public Map<String, Object> forgetPassword(String email, String verifyCode, String newPassword) {

//        System.out.println("email = " + email);
//        System.out.println("verifyCode = " + verifyCode);
//        System.out.println("newPassword = " + newPassword);

        Map<String, Object> errorMsg = new HashMap<>();

        User user = userMapper.selectByEmail(email);
        if (user == null) {
            errorMsg.put("emailMsg", "不存在存在该邮箱的用户！");
            return errorMsg;
        }

        if (user.getStatus() == 0) {
            errorMsg.put("emailMsg", "该账号尚未激活！无法修改密码！");
            return errorMsg;
        }

        if (user.getStatus() == 3) {
            errorMsg.put("emailMsg", "该账号处于封禁中，无法修改该密码！！");
            return errorMsg;
        }

        String forgetPasswordKey = RedisKeyUtil.getForgetPasswordKey(email);

        if (!redisTemplate.hasKey(forgetPasswordKey)) {
            errorMsg.put("emailMsg", "验证码超时或输入的验证码并非本账号的验证码！！！");
            return errorMsg;
        }

        String redisVerifyCode = (String) redisTemplate.opsForValue().get(forgetPasswordKey);
        if (!redisVerifyCode.equalsIgnoreCase(verifyCode)) {
            errorMsg.put("codeMsg", "验证码错误！！");
            return errorMsg;
        }

        this.updatePassword(user.getId(), newPassword);

        clearCacheUser(user.getId());

        return errorMsg;
    }

    @Override
    public int activation(int userId, String activationCode) {
        User user = userMapper.selectById(userId);
        if (user.getStatus() == 1) {
            return ACTIVATION_REPEAT;
        } else if (user.getActivationCode().equals(activationCode)) {
            userMapper.updateStatus(userId, 1);
            clearCacheUser(userId);
            return ACTIVATION_SUCCESS;
        } else {
            return ACTIVATION_FAILURE;
        }
    }

    @Override
    public Map<String, Object> login(String username, String password, int expiredSeconds) {

        Map<String, Object> map = new HashMap<>();

        // 空值处理
        if (StringUtils.isBlank(username)) {
            map.put("usernameMsg", "账号不能为空！");
            return map;
        }
        if (StringUtils.isBlank(password)) {
            map.put("passwordMsg", "密码不能为空！");
            return map;
        }

        // 验证账号
        User user = userMapper.selectByName(username);
        if (user == null) {
            map.put("usernameMsg", "该账号不存在！");
            return map;
        }
        // 检测用户账号的状态
        if (user.getStatus() == 0) {
            map.put("usernameMsg", "该账号未激活！");
            return map;
        }
        if (user.getStatus() == 2) {
            map.put("usernameMsg", "该账号已被禁用！");
            return map;
        }

        password = CommunityUtil.MD5(password + user.getSalt());
        if (!user.getPassword().equals(password)) {
            map.put("passwordMsg", "密码不正确！");
            return map;
        }

        // 若能到此，则说明账号和密码均正确
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setStatus(0);
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000));
//        loginTicketMapper.insertLoginTicket(loginTicket);

        String ticketKey = RedisKeyUtil.getTicketKey(loginTicket.getTicket());
        // 直接存对象，在redis中会序列化成JSON对象
        redisTemplate.opsForValue().set(ticketKey, loginTicket);

        // 将登陆凭证存进map
        map.put("ticket", loginTicket.getTicket());

        return map;
    }

    @Override
    public void logout(String ticket) {
//        loginTicketMapper.updateStatus(ticket, 1);
        String ticketKey = RedisKeyUtil.getTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(ticketKey);
        loginTicket.setStatus(1);
        redisTemplate.opsForValue().set(ticketKey, loginTicket);
    }

    @Override
    public LoginTicket findLoginTicket(String ticket) {
//        return loginTicketMapper.selectLoginTicket(ticket);
        String ticketKey = RedisKeyUtil.getTicketKey(ticket);
        return ((LoginTicket) redisTemplate.opsForValue().get(ticketKey));
    }

    @Override
    public int updateHeader(int userId, String headerUrl) {
        int rows = userMapper.updateHeader(userId, headerUrl);
        clearCacheUser(userId);
        return rows;
    }

    @Override
    public int updatePassword(int userId, String password) {

        // 得到登录用户
        User user = userMapper.selectById(userId);

        password = CommunityUtil.MD5(password + user.getSalt());

        int rows = userMapper.updatePassword(userId, password);

        clearCacheUser(userId);

        return rows;
    }

    @Override
    public Map<String, Object> updateEmail(String formerEmail, String updateVerifyCode,
                                           String newEmail, String activationVerifyCode) {

        User currentUser = hostHolder.getUser();

        Map<String, Object> updateEmailMsg = new HashMap<>();

        String updateEmailKey = RedisKeyUtil.getUpdateEmailKey(formerEmail);
        String activateEmailKey = RedisKeyUtil.getActivateEmailKey(newEmail);

        if (!redisTemplate.hasKey(updateEmailKey)) {
            updateEmailMsg.put("updateVerifyCodeMsg", "验证码超时或非本账号验证码！！");
            return updateEmailMsg;
        }

        if (!redisTemplate.hasKey(activateEmailKey)) {
            updateEmailMsg.put("activationVerifyCodeMsg", "验证码超时或非本账号验证码！！");
            return updateEmailMsg;
        }

        String updateCode = (String) redisTemplate.opsForValue().get(updateEmailKey);
        String activateCode = (String) redisTemplate.opsForValue().get(activateEmailKey);

        if (!formerEmail.equals(currentUser.getEmail())) {
            updateEmailMsg.put("formerEmailMsg", "只能修改自己的邮箱哦！！亲~");
            return updateEmailMsg;
        }

        if (!updateCode.equalsIgnoreCase(updateVerifyCode)) {
            updateEmailMsg.put("updateVerifyCodeMsg", "验证码错误！！");
            return updateEmailMsg;
        }

        User newEmailUser = userMapper.selectByEmail(newEmail);

        if (newEmailUser != null) {
            updateEmailMsg.put("newEmailMsg", "该邮箱已被注册！！");
            return updateEmailMsg;
        }

        if (!activateCode.equalsIgnoreCase(activationVerifyCode)) {
            updateEmailMsg.put("activationVerifyCodeMsg", "验证码错误！");
            return updateEmailMsg;
        }

        // 进行修改邮箱的操作
        userMapper.updateEmail(currentUser.getId(), newEmail);

        // 数据发生变化，清除缓存中的数据
        clearCacheUser(currentUser.getId());

        return updateEmailMsg;
    }

    // 验证邮箱格式

    /*private boolean isEmail(String email) {

        String emailReg = "^[A-Za-z\\d]+([-_.][A-Za-z\\d]+)*@([A-Za-z\\d]+[-.])+[A-Za-z\\d]{2,4}$";

        Pattern emailPattern = Pattern.compile(emailReg);
        Matcher m = emailPattern.matcher(email);
        return m.matches();
//        return false;

    }*/

    @Override
    public int updateType(int userId, int type) {
        clearCacheUser(userId);
        return userMapper.updateType(userId, type);
    }

    @Override
    public int updateStatus(int userId, int newStatus) {
        clearCacheUser(userId);
        return userMapper.updateStatus(userId, newStatus);
    }

    /**
     * 查询用户的权限
     *
     * @param userId 所要查询的用户的id
     * @return 权限
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities(int userId) {

        User user = this.findUserById(userId);

        List<GrantedAuthority> list = new ArrayList<>();

        list.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                switch (user.getType()) {
                    case 1:
                        return AUTHORITY_ADMIN;
                    case 2:
                        return AUTHORITY_MODERATOR;
                    case 3:
                        return AUTHORITY_FORBIDDEN;
                    default:
                        return AUTHORITY_USER;
                }
            }
        });

        return list;
    }
}
