package com.druh.community.controller;

import com.druh.community.entity.Page;
import com.druh.community.entity.User;
import com.druh.community.service.FollowService;
import com.druh.community.service.UserService;
import com.druh.community.utils.CommunityConstant;
import com.druh.community.utils.CommunityUtil;
import com.druh.community.utils.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
public class FollowController {

    @Autowired
    private FollowService followService;
    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private UserService userService;

    @PostMapping("/follow")
    @ResponseBody
    public String follow(int entityType, int entityId) {
        User user = hostHolder.getUser();
        followService.follow(user.getId(), entityType, entityId);
        return CommunityUtil.getJSONString(0, "已关注");
    }

    @PostMapping("/unfollow")
    @ResponseBody
    public String unfollow(int entityType, int entityId) {
        User user = hostHolder.getUser();
        followService.unfollow(user.getId(), entityType, entityId);
        return CommunityUtil.getJSONString(0, "已取消关注");
    }

    /**
     * 查询当前用户关注的用户
     * @param userId
     * @param page
     * @param model
     * @return
     */
    @GetMapping("/followees/{userId}")
    public String getFollowees(@PathVariable("userId") int userId, Page page, Model model) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在！");
        }
        model.addAttribute("user", user);

        page.setLimit(5);
        page.setPath("/followees/" + userId);
        page.setRows((int) followService.getFolloweeCount(userId, CommunityConstant.ENTITY_TYPE_USER));

        List<Map<String, Object>> followeesList = followService.getFollowees(userId, page.getOffset(), page.getLimit());
        if (followeesList != null) {
            for (Map<String, Object> map :
                    followeesList) {
                User u = (User) map.get("user");
                map.put("hasFollowed", hasFollowed(u.getId()));
            }
        }
        model.addAttribute("users", followeesList);

        return "/site/followee";
    }

    /**
     * 获取当前用户的粉丝列表
     * @param userId
     * @param page
     * @param model
     * @return
     */
    @GetMapping("/followers/{userId}")
    public String getFollowers(@PathVariable("userId") int userId, Page page, Model model) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在！");
        }
        model.addAttribute("user", user);

        page.setLimit(5);
        page.setPath("/followers/" + userId);
        page.setRows((int) followService.getFollowerCount(CommunityConstant.ENTITY_TYPE_USER, userId));

        List<Map<String, Object>> followersList = followService.getFollowers(userId, page.getOffset(), page.getLimit());
        if (followersList != null) {
            for (Map<String, Object> map :
                    followersList) {
                User u = (User) map.get("user");
                map.put("hasFollowed", hasFollowed(u.getId()));
            }
        }
        model.addAttribute("users", followersList);

        return "/site/follower";
    }

    // 当前用户是否follow了userId这个用户
    private boolean hasFollowed(int userId) {
        if (hostHolder.getUser() == null) {
            return false;
        }
        return followService.hasFollowed(hostHolder.getUser().getId(), CommunityConstant.ENTITY_TYPE_USER, userId);
    }
}
