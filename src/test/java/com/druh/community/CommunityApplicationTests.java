package com.druh.community;

import com.druh.community.entity.DiscussPost;
import com.druh.community.entity.LoginTicket;
import com.druh.community.mapper.DiscussPostMapper;
import com.druh.community.mapper.LoginTicketMapper;
import com.druh.community.utils.MailClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.List;

@SpringBootTest
class CommunityApplicationTests {

    @Test
    void contextLoads() {
    }

    @Autowired
    DiscussPostMapper discussPostMapper;
    @Test
    public void testSelectPosts() {
        List<DiscussPost> posts = discussPostMapper.selectDiscussPosts(101, 0, 10);
        for (DiscussPost post :
                posts) {
            System.out.println(post);
        }
    }

    @Autowired
    private MailClient mailClient;
    @Test
    public void testSendMail() {
        mailClient.sendMail("rushdjy@163.com", "TEST", "It's a spring mail test!");
    }

    @Autowired
    TemplateEngine templateEngine;

    @Test
    public void testHtmlMail() {
        Context context = new Context();
        context.setVariable("username", "Darren");
        String content = templateEngine.process("/mail/demo", context);

        mailClient.sendMail("rushdjy@163.com", "TestHTML", content);
    }


    @Autowired
    LoginTicketMapper loginTicketMapper;
    @Test
    public void testInsertLoginTicket() {
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setExpired(new Date(System.currentTimeMillis() + 1000 * 60 * 10));
        loginTicket.setTicket("abc");
        loginTicket.setUserId(101);
        loginTicket.setStatus(0);

        loginTicketMapper.insertLoginTicket(loginTicket);
    }

    @Test
    public void testSelectLoginTicket() {
        LoginTicket loginTicket = loginTicketMapper.selectByTicket("abc");
        System.out.println(loginTicket);

        loginTicketMapper.updateStatus("abc", 1);
        loginTicket = loginTicketMapper.selectByTicket("abc");
        System.out.println(loginTicket);
    }
}
