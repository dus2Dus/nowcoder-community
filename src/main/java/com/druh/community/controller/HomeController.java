package com.druh.community.controller;

import com.druh.community.entity.DiscussPost;
import com.druh.community.entity.Page;
import com.druh.community.entity.User;
import com.druh.community.service.DiscussPostService;
import com.druh.community.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController {

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserService userService;

    @RequestMapping("/index")
    public String getIndexPage(Model model, Page page) {    //这个page, springmvc会自动创建并传给我
        // 方法调用前，SpringMVC会自动实例化Model和Page，并将 Page注入Model ！
        // 所以，在Thymeleaf中可以直接访问Page对象中的数据
        // 不需要调用model.addAttribute来将Page对象放到Model中

        // 这儿两个属性一设置，咱们一个page的实例对象就完整了，page.current和page.limit都有初始值
        // 设置page的总记录数
        page.setRows(discussPostService.findDiscussPostRows(0));
        // 设置复用请求路径
        page.setPath("/index");

        List<DiscussPost> postList = discussPostService.findDiscussPosts(0, page.getOffset(), page.getLimit());
        List<Map<String, Object>> discussPosts = new ArrayList<>();
        for (DiscussPost post :
                postList) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("post", post);
            User user = userService.findUserById(post.getUserId());
            map.put("user", user);
            discussPosts.add(map);
        }
        model.addAttribute("discussPosts", discussPosts);
        return "/index";
    }
}
