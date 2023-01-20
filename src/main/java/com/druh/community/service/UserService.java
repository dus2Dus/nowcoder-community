package com.druh.community.service;

import com.druh.community.entity.User;
import com.druh.community.mapper.UserMapper;
import com.druh.community.utils.CommunityConstant;
import com.druh.community.utils.CommunityUtil;
import com.druh.community.utils.MailClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class UserService implements CommunityConstant {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Value("${community.path.domain}")
    private String domain;

    /**
     * 根据id查询用户
     *
     * @param id
     * @return
     */
    public User findUserById(int id) {
        return userMapper.selectById(id);
    }

    /**
     * 注册用户，对应的controller为LoginController中的register方法
     * @param user
     * @return
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

        /**
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
     * @param userId
     * @param activationCode
     * @return
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
            return ACTIVATION_SUCCESS;
        } else {
            // 激活失败
            return ACTIVATION_FAILURE;
        }
    }


}

