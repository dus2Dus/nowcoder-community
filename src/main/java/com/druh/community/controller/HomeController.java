package com.druh.community.controller;

import com.druh.community.entity.DiscussPost;
import com.druh.community.entity.User;
import com.druh.community.mapper.DiscussPostMapper;
import com.druh.community.mapper.UserMapper;
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
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private UserMapper userMapper;

    @RequestMapping("/index")
    public String getIndexPage(Model model) {
        List<DiscussPost> postList = discussPostMapper.selectDiscussPosts(0, 0, 10);
        List<Map<String, Object>> discussPosts = new ArrayList<>();
        for (DiscussPost post :
                postList) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("post", post);
            User user = userMapper.selectById(post.getUserId());
            map.put("user", user);
            discussPosts.add(map);
        }
        model.addAttribute("discussPosts", discussPosts);
        return "/index";
    }
}
