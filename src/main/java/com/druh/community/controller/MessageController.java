package com.druh.community.controller;

import com.alibaba.fastjson.JSONObject;
import com.druh.community.entity.Message;
import com.druh.community.entity.Page;
import com.druh.community.entity.User;
import com.druh.community.service.MessageService;
import com.druh.community.service.UserService;
import com.druh.community.utils.CommunityConstant;
import com.druh.community.utils.CommunityUtil;
import com.druh.community.utils.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.HtmlUtils;

import java.util.*;

/**
 * @author DJY
 * @date 2023/4/16 19:24
 * @apiNote
 */
@Controller
public class MessageController implements CommunityConstant {

    @Autowired
    private MessageService messageService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    /**
     * 获取会话列表，传给前端
     *
     * @param model model
     * @param page  page
     * @return return
     */
    @GetMapping("/letter/list")
    public String getLetterList(Model model, Page page) {

        User user = hostHolder.getUser();

        // 分页信息
        page.setLimit(5);
        page.setPath("/letter/list");
        page.setRows(messageService.findConversationCount(user.getId()));

        // 会话列表
        // page的offset和limit哪里来的？？
        List<Message> conversationList = messageService.findConversations(user.getId(), page.getOffset(), page.getLimit());
        // 因为我们现在只要显示用户的所有会话列表，不需要点进去看所有消息，所以在这个页面上我们只要显示每个会话的最新一条消息，就可以表示一个会话了
        // 所以可以直接把查出来的最新的那条Message当作一个会话，所以conversationList中存的对象是Message

        // 存每条会话的view object。conversations中的每个map存的都是一条会话要显示的那些信息
        List<Map<String, Object>> conversations = new ArrayList<>();
        if (conversationList != null) {
            for (Message message : conversationList) {
                HashMap<String, Object> map = new HashMap<>();
                // 因为我们在外面只显示最新的那条消息，所以最新的那条消息就可以代表这个会话，所以取名conversation
                map.put("conversation", message);
                map.put("letterCount", messageService.findLetterCount(message.getConversationId()));
                map.put("unreadCount", messageService.findLetterUnreadCount(user.getId(), message.getConversationId()));
                // 这条会话前面显示的头像应该是对方，而不是我（当前用户）
                int targetId = (user.getId() == message.getFromId() ? message.getToId() : message.getFromId());
                map.put("target", userService.findUserById(targetId));

                conversations.add(map);
            }
        }
        model.addAttribute("conversations", conversations);

        // 查询该账户内的所有未读消息数量，所以不用指定conversationId
        int letterUnreadCount = messageService.findLetterUnreadCount(user.getId(), null);
        model.addAttribute("letterUnreadCount", letterUnreadCount);

        return "/site/letter";
    }

    /**
     * 获取某个会话的详细信息，就是查询聊天框里面的聊天记录
     *
     * @param conversationId
     * @param page
     * @param model
     * @return
     */
    @GetMapping("/letter/detail/{conversationId}")
    public String getLetterDetail(@PathVariable("conversationId") String conversationId, Page page, Model model) {
        // 分页信息
        page.setLimit(5);
        page.setPath("/letter/detail/" + conversationId);
        page.setRows(messageService.findLetterCount(conversationId));

        // 私信列表
        List<Message> letterList = messageService.findLetters(conversationId, page.getOffset(), page.getLimit());
        List<Map<String, Object>> letters = new ArrayList<>();
        if (letterList != null) {
            for (Message message : letterList) {
                Map<String, Object> map = new HashMap<>();
                map.put("letter", message);
                map.put("fromUser", userService.findUserById(message.getFromId()));
                letters.add(map);
            }
        }
        model.addAttribute("letters", letters);

        // 私信目标
        model.addAttribute("target", getLetterTarget(conversationId));

        // 设置已读
        List<Integer> ids = getLetterIds(letterList);
        if (!ids.isEmpty()) {
            messageService.readMessage(ids);
        }

        return "/site/letter-detail";
    }

    /**
     * 获取要被设置为已读的私信的集合，我点进聊天框，那些未读的消息就要被设置为已读
     * 哪些是未读的呢，就是toId是我，然后status==0
     * @param letterList 聊天框里所有私信的集合
     * @return 返回id的集合
     */
    private List<Integer> getLetterIds(List<Message> letterList) {
        List<Integer> ids = new ArrayList<>();

        if (letterList != null) {
            for (Message message : letterList) {
                if (hostHolder.getUser().getId() == message.getToId() && message.getStatus() == 0) {
                    ids.add(message.getId());
                }
            }
        }

        return ids;
    }

    /**
     * 获取私信的目标用户
     *
     * @param conversationId 会话id
     * @return 返回发送人User
     */
    private User getLetterTarget(String conversationId) {
        String[] ids = conversationId.split("_");
        int id0 = Integer.parseInt(ids[0]);
        int id1 = Integer.parseInt(ids[1]);

        if (hostHolder.getUser().getId() == id0) {
            return userService.findUserById(id1);
        } else {
            return userService.findUserById(id0);
        }
    }

    @PostMapping("/letter/send")
    @ResponseBody
    public String sendLetter(String toName, String content) {
        User target = userService.findUserByName(toName);
        if (target == null) {
            return CommunityUtil.getJSONString(1, "目标用户不存在！");
        }

        Message message = new Message();
        message.setFromId(hostHolder.getUser().getId());
        message.setToId(target.getId());
        if (message.getFromId() < message.getToId()) {
            message.setConversationId(message.getFromId() + "_" + message.getToId());
        } else {
            message.setConversationId(message.getToId() + "_" + message.getFromId());
        }
        message.setContent(content);
        message.setCreateTime(new Date());
        messageService.addMessage(message);

        return CommunityUtil.getJSONString(0);
    }

    @RequestMapping(path = "/notice/list", method = RequestMethod.GET)
    public String getNoticeList(Model model){
        User user = hostHolder.getUser();

        // 查询评论类通知
        Message message = messageService.findLatestNotice(user.getId(), TOPIC_COMMENT);
        if (message != null) {
            HashMap<String, Object> messageVO = new HashMap<>();
            messageVO.put("message", message);
            // 把转义字符再转回来
            String content = HtmlUtils.htmlUnescape(message.getContent());
            // 把JSON字符串变成HashMap形式
            HashMap<String, Object> data = JSONObject.parseObject(content, HashMap.class);

            messageVO.put("user", userService.findUserById((Integer)data.get("userId")));
            messageVO.put("entityType", data.get("entityType"));
            messageVO.put("entityId", data.get("entityId"));
            messageVO.put("postId", data.get("postId"));

            int count = messageService.findNoticeCount(user.getId(), TOPIC_COMMENT);
            messageVO.put("count", count);

            int unread = messageService.findNoticeUnreadCount(user.getId(), TOPIC_COMMENT);
            messageVO.put("unread", unread);
            model.addAttribute("commentNotice", messageVO);
        }

        // 查询点赞类通知
        message = messageService.findLatestNotice(user.getId(), TOPIC_LIKE);
        if (message != null) {
            HashMap<String, Object> messageVO = new HashMap<>();
            messageVO.put("message", message);
            // 把转义字符再转回来
            String content = HtmlUtils.htmlUnescape(message.getContent());
            // 把JSON字符串变成HashMap形式
            HashMap<String, Object> data = JSONObject.parseObject(content, HashMap.class);

            messageVO.put("user", userService.findUserById((Integer)data.get("userId")));
            messageVO.put("entityType", data.get("entityType"));
            messageVO.put("entityId", data.get("entityId"));
            messageVO.put("postId", data.get("postId"));

            int count = messageService.findNoticeCount(user.getId(), TOPIC_LIKE);
            messageVO.put("count", count);

            int unread = messageService.findNoticeUnreadCount(user.getId(), TOPIC_LIKE);
            messageVO.put("unread", unread);
            model.addAttribute("likeNotice", messageVO);
        }

        // 查询关注类通知
        message = messageService.findLatestNotice(user.getId(), TOPIC_FOLLOW);
        if (message != null) {
            HashMap<String, Object> messageVO = new HashMap<>();
            messageVO.put("message", message);
            // 把转义字符再转回来
            String content = HtmlUtils.htmlUnescape(message.getContent());
            // 把JSON字符串变成HashMap形式
            HashMap<String, Object> data = JSONObject.parseObject(content, HashMap.class);

            messageVO.put("user", userService.findUserById((Integer)data.get("userId")));
            messageVO.put("entityType", data.get("entityType"));
            messageVO.put("entityId", data.get("entityId"));

            int count = messageService.findNoticeCount(user.getId(), TOPIC_FOLLOW);
            messageVO.put("count", count);

            int unread = messageService.findNoticeUnreadCount(user.getId(), TOPIC_FOLLOW);
            messageVO.put("unread", unread);
            model.addAttribute("followNotice", messageVO);
        }

        // 查询未读消息数量
        int letterUnreadCount = messageService.findLetterUnreadCount(user.getId(), null);
        model.addAttribute("letterUnreadCount", letterUnreadCount);
        int noticeUnreadCount = messageService.findNoticeUnreadCount(user.getId(), null);
        model.addAttribute("noticeUnreadCount", noticeUnreadCount);

        return "/site/notice";
    }

    @GetMapping(path = "/notice/detail/{topic}")
    public String getNoticeDetail(@PathVariable("topic") String topic, Page page, Model model) {
        User user = hostHolder.getUser();

        page.setLimit(5);
        page.setPath("/notice/detail/" + topic);
        page.setRows(messageService.findNoticeCount(user.getId(), topic));

        List<Message> noticeList = messageService.findNotices(user.getId(), topic, page.getOffset(), page.getLimit());
        List<Map<String, Object>> noticeVOList = new ArrayList<>();

        if (noticeList != null) {
            for (Message notice : noticeList) {
                HashMap<String, Object> map = new HashMap<>();
                // 通知
                map.put("notice", notice);
                // 内容
                String content = HtmlUtils.htmlUnescape(notice.getContent());
                HashMap<String ,Object> data = JSONObject.parseObject(content, HashMap.class);
                map.put("user", userService.findUserById((Integer)data.get("userId")));
                map.put("entityType", data.get("entityType"));
                map.put("entityId", data.get("entityId"));
                map.put("postId", data.get("postId"));
                // 通知的作者
                map.put("fromUser", userService.findUserById(notice.getFromId()));

                noticeVOList.add(map);
            }
        }
        model.addAttribute("notices", noticeVOList);

        // 设置已读
        List<Integer> ids = getLetterIds(noticeList);
        if (!ids.isEmpty()) {
            messageService.readMessage(ids);
        }

        return "/site/notice-detail";
    }
}
