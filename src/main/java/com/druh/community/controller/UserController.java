package com.druh.community.controller;

import com.druh.community.annotation.LoginRequired;
import com.druh.community.entity.User;
import com.druh.community.service.FollowService;
import com.druh.community.service.LikeService;
import com.druh.community.service.UserService;
import com.druh.community.utils.CommunityConstant;
import com.druh.community.utils.CommunityUtil;
import com.druh.community.utils.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author DJY
 * @date 2023/3/5 10:28
 * @apiNote
 */
@Controller
@RequestMapping("/user")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Value("${community.path.domain}")
    private String domain;

    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${server.servlet.context-path}")
    private  String contextPath;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private LikeService likeService;

    @Autowired
    private FollowService followService;

    // 返回账户设置页
    @LoginRequired
    @GetMapping("/setting")
    public String getSettingPage() {
        return "/site/setting";
    }

    /**
     * 处理修改头像请求
     * @param headerImage
     * @param model
     * @return
     */
    @LoginRequired
    @PostMapping("/upload")
    public String uploadHeader(MultipartFile headerImage, Model model) {
        // 如果没拿到头像，就报错
        if(headerImage == null){
            model.addAttribute("error", "您还没有选择图片！");
            return "/site/setting";
        }

        // 上传的头像的原始文件名称
        String filename = headerImage.getOriginalFilename();
        // 从原文件名中截取出后缀来
        String suffix = null;
        if (filename != null) {
            suffix = filename.substring(filename.lastIndexOf("."));
        }
        if (StringUtils.isBlank(suffix)) {
            model.addAttribute("error", "文件的格式不正确！");
            return "/site/setting";
        }
        // 生成随机文件名
        filename = CommunityUtil.generateUUID() + suffix;
        // 确定文件存放的路径
        File dest = new File(uploadPath + "/" +filename);
        try {
            // 存储文件
            headerImage.transferTo(dest);
        } catch (IOException e) {
            logger.error("上传文件失败：", e);
            throw new RuntimeException("上传文件失败！", e);
        }

        // 更新当前用户的头像路径（Web访问路径）
        // http:localhost:8080/community/user/header/xxx.png
        String headerUrl = domain + contextPath + "/user/header/" + filename;
        User user = hostHolder.getUser();
        userService.updateHeader(user.getId(), headerUrl);

        return "redirect:/index";
    }

    // 获取头像
    @GetMapping("/header/{fileName}")
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response) {
        // 头像在服务器的存放路径
        fileName = uploadPath + "/" + fileName;
        // 文件后缀
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        // 响应图片
        response.setContentType("image/" + suffix);
        try (
                FileInputStream fis = new FileInputStream(fileName);
                OutputStream os = response.getOutputStream();
        ) {
            byte[] buffer = new byte[1024];
            int b = 0;
            while ((b = fis.read(buffer)) != -1) {
                os.write(buffer, 0, b);
            }
        } catch (IOException e) {
            logger.error("读取头像失败: " + e.getMessage());
        }
    }

    // 个人主页
    @GetMapping("/profile/{userId}")
    public String getProfilePage(@PathVariable("userId") int userId, Model model) {
        // 用户
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在");
        }
        model.addAttribute("user", user);
        // 收到赞的数量
        int count = likeService.getUserLikeCount(userId);
        model.addAttribute("likeCount", count);

        // 关注数量
        long followeeCount = followService.getFolloweeCount(userId, CommunityConstant.ENTITY_TYPE_USER);
        model.addAttribute("followeeCount", followeeCount);
        // 粉丝数量
        long followerCount = followService.getFollowerCount(CommunityConstant.ENTITY_TYPE_USER, userId);
        model.addAttribute("followerCount", followerCount);
        // 是否已关注
        boolean hasFollowed = false;
        if (hostHolder.getUser() != null) {
            hasFollowed = followService.hasFollowed(hostHolder.getUser().getId(), CommunityConstant.ENTITY_TYPE_USER, userId);
        }
        model.addAttribute("hasFollowed", hasFollowed);

        return "/site/profile";
    }
}
