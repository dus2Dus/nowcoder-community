package com.druh.community.service;

import com.druh.community.entity.LoginTicket;
import com.druh.community.entity.User;
import com.druh.community.mapper.LoginTicketMapper;
import com.druh.community.mapper.UserMapper;
import com.druh.community.utils.CommunityConstant;
import com.druh.community.utils.CommunityUtil;
import com.druh.community.utils.MailClient;
import com.druh.community.utils.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class UserService implements CommunityConstant {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private RedisTemplate redisTemplate;

//    @Autowired
//    private LoginTicketMapper loginTicketMapper;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Value("${community.path.domain}")
    private String domain;

    /**
     * 根据id查询用户
     *
     * @param id id
     * @return 返回User
     */
    public User findUserById(int id) {
//        return userMapper.selectById(id);
        User user = getCache(id);
        if (user == null) {
            user = initCache(id);
        }
        return user;
    }

    /**
     * 注册用户，对应的controller为LoginController中的register方法
     *
     * @param user 前端表单传入的User对象
     * @return 返回存了多种处理情况的map
     */
    public Map<String, Object> register(User user) {
        Map<String, Object> map = new HashMap<>();

        // 空值处理
        if (user == null) {
            throw new IllegalArgumentException("参数不能为空！");
        }
        if (StringUtils.isBlank(user.getUsername())) {
            map.put("usernameMsg", "账号不能为空！");
            return map;
        }
        if (StringUtils.isBlank(user.getPassword())) {
            map.put("passwordMsg", "密码不能为空！");
            return map;
        }
        if (StringUtils.isBlank(user.getEmail())) {
            map.put("emailMsg", "邮箱不能为空！");
            return map;
        }

        // 验证账号
        User u = userMapper.selectByName(user.getUsername());
        if (u != null) {
            map.put("usernameMsg", "该账号已存在！");
            return map;
        }

        // 验证邮箱
        u = userMapper.selectByEmail(user.getEmail());
        if (u != null) {
            map.put("emailMsg", "该邮箱已被注册");
            return map;
        }

        // 注册用户
        user.setSalt(CommunityUtil.generateUUID().substring(0, 5));
        user.setStatus(0);
        user.setType(0);
        user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt()));
        user.setActivationCode(CommunityUtil.generateUUID());
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        user.setCreateTime(new Date());
        userMapper.insertUser(user);

        /*
         * 发送激活邮件
         */
        /* context就是用来携带参数的，看templateEngine.process那一行，模板引擎的process方法就是用来处理模板的，
            模板里面有一些地方是挖空的，不是写死的，需要用户用${}来取值并填入，context就是这个作用，带一批参数过去给用户取，
            换句话说，String content就是我要发送给用户的邮件的内容，
            而content就等于 一个有些地方需要填入内容的html模板 + 用来填入的参数
        */
        Context context = new Context();
        context.setVariable("email", user.getEmail());

        /* 用户激活账号的连接：http://localhost:8080/community/activation/用户的id/用户的ActivationCode
         *   用户点了这个链接，就发送了这个链接对应的请求，我们对应的controller方法就能去处理这个请求，做一些跳转*/

        // 用户的id为什么可以直接user.getId()?不是没有设置吗？因为我用userMapper.insertUser(user)后，mysql自动生成id值，自增的嘛，回过来就有了
        // 为什么mysql自增id值？因为我们配置了 mybatis.configuration.useGeneratedKeys=true
        String url = domain + contextPath + "/activation" + "/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url", url);
        String content = templateEngine.process("/mail/activation", context);
        //发送邮件
        mailClient.sendMail(user.getEmail(), "激活您的账户！", content);


        return map;
    }


    /**
     * 激活用户的账户
     * 根据用户id和传进来的激活码，去数据库中查找并比对
     *
     * @param userId         userId
     * @param activationCode 激活码
     * @return 返回对应处理结果的数字
     */
    public int activate(int userId, String activationCode) {
        User user = userMapper.selectById(userId);
        // 已经激活过了
        if (user.getStatus() == 1) {
            userMapper.updateStatus(userId, 1);
            return ACTIVATION_REPETITION;
        } else if (user.getActivationCode().equals(activationCode)) {
            // 数据库中的激活码和你带来的激活码(URL中的)对上了，就激活成功了
            userMapper.updateStatus(userId, 1);
            clearCache(userId);
            return ACTIVATION_SUCCESS;
        } else {
            // 激活失败
            return ACTIVATION_FAILURE;
        }
    }

    /**
     * 处理登录请求
     *
     * @param username      用户名
     * @param password      密码
     * @param expireSeconds 过期时间
     * @return 返回一个包含多种情况的map，key错误名字，value是错误信息
     */
    public Map<String, Object> login(String username, String password, int expireSeconds) {
        HashMap<String, Object> map = new HashMap<>();
        // 空值处理
        if (StringUtils.isBlank(username)) {
            map.put("usernameMsg", "账号不能为空！");
            return map;
        }
        if (StringUtils.isBlank(password)) {
            map.put("passwordMsg", "密码不能为空！");
            return map;
        }

        User user = userMapper.selectByName(username);
        if (user == null) {
            map.put("usernameMsg", "账号不存在！");
            return map;
        }
        // 验证状态
        if (user.getStatus() == 0) {
            map.put("usernameMsg", "账号未激活！");
            return map;
        }

        password = CommunityUtil.md5(password + user.getSalt());
        if (!user.getPassword().equals(password)) {
            map.put("passwordMsg", "密码错误！");
            return map;
        }

        // 生成登录凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expireSeconds * 1000));
        loginTicket.setStatus(0);
//        loginTicketMapper.insertLoginTicket(loginTicket);

        String key = RedisKeyUtil.getTicketKey(loginTicket.getTicket());
        redisTemplate.opsForValue().set(key, loginTicket);

        map.put("ticket", loginTicket.getTicket());
        return map;
    }

    /**
     * 退出登录
     *
     * @param ticket 登录凭证
     */
    public void logout(String ticket) {
//        loginTicketMapper.updateStatus(ticket, 1);
        String key = RedisKeyUtil.getTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(key);
        loginTicket.setStatus(1);
        redisTemplate.opsForValue().set(key, loginTicket);
    }


    /**
     * 根据ticket查找LoginTicket对象
     *
     * @param ticket 登录凭证ticket
     * @return 返回LoginTicket对象
     */
    public LoginTicket getLoginTicket(String ticket) {
//        return loginTicketMapper.selectByTicket(ticket);
        String key = RedisKeyUtil.getTicketKey(ticket);
        return (LoginTicket) redisTemplate.opsForValue().get(key);
    }

    /**
     * 更新用户头像
     *
     * @param userId    用户id
     * @param headerUrl 头像url
     * @return 返回受影响的行数
     */
    public int updateHeader(int userId, String headerUrl) {
//        return userMapper.updateHeader(userId, headerUrl);
        int rows = userMapper.updateHeader(userId, headerUrl);
        clearCache(userId);
        return rows;
    }

    public User findUserByName(String username) {
        return userMapper.selectByName(username);
    }

    // 1.优先从缓存中取值
    private User getCache(int userId) {
        String userKey = RedisKeyUtil.getUserKey(userId);
        return (User) redisTemplate.opsForValue().get(userKey);
    }
    // 2.取不到时初始化缓存数据
    private User initCache(int userId) {
        User user = userMapper.selectById(userId);
        String userKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.opsForValue().set(userKey, user, 3600, TimeUnit.SECONDS);
        return user;
    }

    // 3.数据变更时清除缓存数据
    private void clearCache(int userId) {
        String userKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.delete(userKey);
    }
}

