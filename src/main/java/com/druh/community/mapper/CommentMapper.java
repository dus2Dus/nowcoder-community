package com.druh.community.mapper;

import com.druh.community.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author DJY
 * @date 2023/4/5 14:58
 * @apiNote
 */
@Mapper
public interface CommentMapper {

    /**
     * 返回评论列表
     * @param entityType 回复的实体类型，回复的是帖子还是某人的评论
     * @param entityId 回复的实体id，回复的是哪个评论，哪个帖子
     * @param offset 开始
     * @param limit 长度
     * @return 评论列表
     */
    List<Comment> selectCommentsByEntity(int entityType, int entityId, int offset, int limit);

    /**
     * 计算总数，方便计算页数
     * @param entityType 实体类型
     * @param entityId 实体id
     * @return 总评论数
     */
    int selectCountByEntity(int entityType, int entityId);

    int insertComment(Comment comment);
}
