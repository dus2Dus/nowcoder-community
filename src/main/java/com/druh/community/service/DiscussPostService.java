package com.druh.community.service;

import com.druh.community.entity.DiscussPost;
import com.druh.community.mapper.DiscussPostMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DiscussPostService {

    @Autowired
    DiscussPostMapper discussPostMapper;

    /**
     * 根据userId, offset, limit来查询帖子
     * 如果userId==0，则查询所有的帖子，不为0则查询这个userId的帖子
     * @param userId 用户id
     * @param offset 偏移，即这一页从哪条帖子开始，由Page对象传进来
     * @param limit 每页限制展示帖子条数的上限，由Page对象传进来
     * @return
     */
    public List<DiscussPost> findDiscussPosts(int userId, int offset, int limit) {
        return discussPostMapper.selectDiscussPosts(userId, offset, limit);
    }

    /**
     * 根据userId来查询这个用户的总帖子数
     * @param userId
     * @return
     */
    public int findDiscussPostRows(int userId) {
        return discussPostMapper.selectDiscussPostRows(userId);
    }


}
