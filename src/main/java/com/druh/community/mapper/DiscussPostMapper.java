package com.druh.community.mapper;

import com.druh.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DiscussPostMapper {

    /**
     * discusspost是发布的帖子
     * @param userId id如果为0则全查，id不为0，则根据id查询
     * @param offset 这一页从哪一行开始
     * @param limit 这一页展示多少行
     * @return
     */
    List<DiscussPost> selectDiscussPosts(int userId, int offset, int limit);

    /**
     * 查询userId来查询这个用户共发了多少条帖子
     * @param userId
     * @return
     */
    int selectDiscussPostRows(@Param("userId") int userId);

}
