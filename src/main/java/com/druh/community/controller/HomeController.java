package com.druh.community.controller;

import com.druh.community.entity.DiscussPost;
import com.druh.community.entity.Page;
import com.druh.community.entity.User;
import com.druh.community.service.DiscussPostService;
import com.druh.community.service.LikeService;
import com.druh.community.service.UserService;
import com.druh.community.utils.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController implements CommunityConstant {

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

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
        if (postList != null) {
            for (DiscussPost post :
                    postList) {
                HashMap<String, Object> map = new HashMap<>();
                map.put("post", post);
                User user = userService.findUserById(post.getUserId());
                map.put("user", user);

                long likeCount = likeService.getEntityLikeCount(ENTITY_TYPE_POST, post.getId());
                map.put("likeCount", likeCount);

                discussPosts.add(map);
            }
        }
        model.addAttribute("discussPosts", discussPosts);
        return "/index";
    }

    /**
     * 返回500错误页面
     * @return
     */
    @GetMapping("/error")
    public String getErrorPage() {
        return "/error/500";
    }

    @GetMapping("/denied")
    public String getDeniedPage() {
        return "/error/404";
    }
}
