package com.druh.community.service;

import com.druh.community.entity.DiscussPost;
import com.druh.community.mapper.DiscussPostMapper;
import com.druh.community.utils.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class DiscussPostService {

    @Autowired
    private DiscussPostMapper discussPostMapper;
    @Autowired
    private SensitiveFilter sensitiveFilter;

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

    /**
     * 添加一条帖子
     * @param post 要发布的帖子对象
     * @return 范围插入后受影响的行数
     */
    public int addDiscussionPost(DiscussPost post) {
        if (post == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        // 转义HTML标签
        post.setTitle(HtmlUtils.htmlEscape(post.getTitle()));
        post.setContent((HtmlUtils.htmlEscape(post.getContent())));

        // 过滤敏感词
        post.setTitle((sensitiveFilter.filter(post.getTitle())));
        post.setContent(sensitiveFilter.filter(post.getContent()));

        return discussPostMapper.insertDiscussPost(post);
    }

    /**
     * 根据帖子id查询帖子
     * @param id 帖子的id
     * @return DiscussPost对象
     */
    public DiscussPost findDiscussPostById(int id) {
        return discussPostMapper.selectDiscussPostById(id);
    }
}
