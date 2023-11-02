package com.druh.community.controller;

import com.druh.community.service.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

@Controller
public class DataController {

    @Autowired
    private DataService dataService;

    // 打开统计页面
    @RequestMapping(value = "/data", method = {RequestMethod.GET, RequestMethod.POST})
    public String getDataPage() {
        return "/site/admin/data";
    }

    // 统计网站UV
    @PostMapping("/data/uv")
    public String getUV(@DateTimeFormat(pattern = "yyyy-MM-dd") Date start,
                        @DateTimeFormat(pattern = "yyyy-MM-dd") Date end, Model model) {
        long uv = dataService.calculateUV(start, end);
        model.addAttribute("uvResult", uv);
        model.addAttribute("uvStartDate", start);
        model.addAttribute("uvEndDate", end);

        // forward 和 直接return "/site/admin/data"的区别：
        // forward是转发到另一个controller方法，意思就是这里的/data其实是上面那个打开统计页面方法，所以这里效果和return模板是一样的
        // 但是要是/data方法里面还有其他的处理逻辑，那就不一样了
        return "forward:/data";
    }

    // 统计活跃用户
    @PostMapping("/data/dau")
    public String getDAU(@DateTimeFormat(pattern = "yyyy-MM-dd") Date start,
                        @DateTimeFormat(pattern = "yyyy-MM-dd") Date end, Model model) {
        long dau = dataService.calculateUV(start, end);
        model.addAttribute("dauResult", dau);
        model.addAttribute("dauStartDate", start);
        model.addAttribute("dauEndDate", end);

        // forward 和 直接return "/site/admin/data"的区别：
        // forward是转发到另一个controller方法，意思就是这里的/data其实是上面那个打开统计页面方法，所以这里效果和return模板是一样的
        // 但是要是/data方法里面还有其他的处理逻辑，那就不一样了
        return "forward:/data";
    }
}