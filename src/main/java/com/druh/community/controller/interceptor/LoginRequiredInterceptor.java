package com.druh.community.controller.interceptor;

import com.druh.community.annotation.LoginRequired;
import com.druh.community.utils.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

/**
 * @author DJY
 * @date 2023/3/5 15:51
 * @apiNote
 */
@Component
public class LoginRequiredInterceptor implements HandlerInterceptor {

    @Autowired
    private HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 如果我们处理的目标，也就是handler，他是个方法的话
        if (handler instanceof HandlerMethod) {
            // 把object对象强转为handlermethod，方便后面获取方法等
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            // 获取方法
            Method method = handlerMethod.getMethod();
            // 获取方法上的LoginRequired注解，当然可能没有这个注解
            LoginRequired loginRequired = method.getAnnotation(LoginRequired.class);
            // 如果方法上有这个注解，而且HostHolder中没有用户信息
            if (loginRequired != null && hostHolder.getUser() == null) {
                response.sendRedirect(request.getContextPath() + "/login");
                return false;
            }
        }
        return true;
    }
}
