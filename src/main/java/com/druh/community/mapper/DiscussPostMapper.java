package com.druh.community.mapper;

import com.druh.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

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
    // @Param注解用于给参数取别名，当这个方法中只有一个参数时并且这个参数在mapper.xml文件中被使用在<if>标签中时，就必须要使用@Param注解来给这个参数取个别名
    int selectDiscussPostRows(@Param("userId") int userId);

}
