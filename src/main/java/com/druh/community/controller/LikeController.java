package com.druh.community.controller;

import com.druh.community.entity.User;
import com.druh.community.service.LikeService;
import com.druh.community.utils.CommunityUtil;
import com.druh.community.utils.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;

@Controller
public class LikeController {

    @Autowired
    private LikeService likeService;
    @Autowired
    private HostHolder hostHolder;

    @PostMapping("/like")
    @ResponseBody
    public String like(int entityType, int entityId, int entityUserId) {
        User user = hostHolder.getUser();
        // 点赞
        likeService.like(user.getId(), entityType, entityId, entityUserId);
        // 数量
        long likeCount = likeService.getEntityLikeCount(entityType, entityId);
        // 状态
        int likeStatus = likeService.getEntityLikeStatus(user.getId(), entityType, entityId);
        // 返回的结果
        HashMap<String, Object> map = new HashMap<>();
        map.put("likeCount", likeCount);
        map.put("likeStatus", likeStatus);
        return CommunityUtil.getJSONString(0, null, map);
    }
}
