package com.druh.community.controller;

import com.druh.community.entity.User;
import com.druh.community.service.UserService;
import com.druh.community.utils.CommunityConstant;
import com.google.code.kaptcha.Producer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;

@Controller
public class LoginController implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private Producer kaptchaProducer;

    @Autowired
    private UserService userService;

    /**
     * 点击注册按钮，去注册页面
     *
     * @return
     */
    @GetMapping("/register")
    public String getRegisterPage() {
        return "/site/register";
    }

    @GetMapping("/login")
    public String getLoginPage() {
        return "/site/login";
    }

    /**
     * 用户在注册页面填好表格，点击提交，所以是post不是get
     *
     * @param model
     * @param user
     * @return
     */
    @PostMapping("/register")
    public String register(Model model, User user) {
        Map<String, Object> map = userService.register(user);
        // 如果map为null或者为空，表示没有什么错误，已经添加成功了
        if (map == null || map.isEmpty()) {
            // 注册成功的页面提示信息
            model.addAttribute("msg", "注册成功,我们已经向您的邮箱发送了一封激活邮件,请尽快激活!");
            // 注册成功后，用户手动跳转的页面路径
            model.addAttribute("target", "/index");
            // 去结果页面
            return "/site/operate-result";
        } else {
            // map不为空，说明有问题，把问题拿出来放进model中去
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            model.addAttribute("emailMsg", map.get("emailMsg"));
            // 再重新回到register.html页面
            return "/site/register";
        }
    }

    /**
     * 用户在邮件中收到激活邮件，点击里面的激活链接，进行激活，调用UserService里的activate方法进行激活和判断
     * http://localhost:8080/community/activation/用户的id/用户的ActivationCode
     *
     * @param model
     * @param userId 用户id
     * @param code   用户自己带来的激活码(URL中的)
     * @return
     */
    @GetMapping("/activation/{userId}/{code}")
    public String activate(Model model, @PathVariable("userId") int userId, @PathVariable("code") String code) {
        // 激活结果
        int result = userService.activate(userId, code);
        if (result == ACTIVATION_SUCCESS) {
            // 激活成功
            model.addAttribute("msg", "激活成功,您的账号已经可以正常使用了!");
            model.addAttribute("target", "/login");
        } else if (result == ACTIVATION_REPETITION) {
            // 重复激活
            model.addAttribute("msg", "无效操作,该账号已经激活过了!");
            model.addAttribute("target", "/index");
        } else {
            // 激活失败
            model.addAttribute("msg", "激活失败,您提供的激活码不正确!");
            model.addAttribute("target", "/index");
        }
        return "/site/operate-result";
    }

    @GetMapping("/kaptcha")
    public void getKaptcha(HttpServletResponse response, HttpSession session) {
        // 生成验证码
        String text = kaptchaProducer.createText();
        BufferedImage image = kaptchaProducer.createImage(text);

        // 将验证码存入session
        session.setAttribute("kaptcha", text);

        // 将图片输出给浏览器
        response.setContentType("image/png");
        try {
            ServletOutputStream outputStream = response.getOutputStream();
            ImageIO.write(image, "png", outputStream);
        } catch (IOException e) {
            logger.error("响应验证码失败:" + e.getMessage());
        }

    }
}
