package com.druh.community.controller;

import com.druh.community.entity.DiscussPost;
import com.druh.community.entity.User;
import com.druh.community.service.DiscussPostService;
import com.druh.community.utils.CommunityUtil;
import com.druh.community.utils.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;

/**
 * @author DJY
 * @date 2023/3/26 17:28
 * @apiNote
 */
@Controller
@RequestMapping("/discuss")
public class DiscussPostController {

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private HostHolder hostHolder;

    // 发布帖子
    @PostMapping("/add")
    @ResponseBody
    public String addDiscussPost(String title, String content) {
        User user = hostHolder.getUser();
        if (user == null) {
            return CommunityUtil.getJSONString(403, "你还没有登录！");
        }
        DiscussPost post = new DiscussPost();
        post.setUserId(user.getId());
        post.setTitle(title);
        post.setContent(content);
        post.setCreateTime(new Date());
        discussPostService.addDiscussionPost(post);

        // 报错的情况将来统一处理
        return CommunityUtil.getJSONString(0, "发布成功！");
    }
}