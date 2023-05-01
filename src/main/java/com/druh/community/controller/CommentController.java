package com.druh.community.controller;

import com.druh.community.entity.Comment;
import com.druh.community.service.CommentService;
import com.druh.community.utils.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Date;

/**
 * @author DJY
 * @date 2023/4/9 16:37
 * @apiNote
 */
@Controller
@RequestMapping("/comment")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private HostHolder hostHolder;

    @PostMapping("/add/{discussPostId}")
    public String addComment(@PathVariable("discussPostId") int discussPostId, Comment comment) {
        comment.setCreateTime(new Date());
        comment.setStatus(0);
        comment.setUserId(hostHolder.getUser().getId());
        commentService.addComment(comment);

        return "redirect:/discuss/detail/" + discussPostId;
    }
}