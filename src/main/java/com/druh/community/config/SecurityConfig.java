package com.druh.community.config;

import com.druh.community.utils.CommunityConstant;
import com.druh.community.utils.CommunityUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter implements CommunityConstant {

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/resources/**");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // 授权
        http.authorizeRequests()
                .antMatchers(
                        "/user/setting",
                        "/user/upload",
                        "/discuss/add",
                        "/comment/add/**",
                        "/letter/**",
                        "/notice/**",
                        "/like",
                        "/follow",
                        "/unfollow"
                )
                .hasAnyAuthority(
                        AUTHORITY_USER,
                        AUTHORITY_ADMIN,
                        AUTHORITY_MODERATOR
                )
                .antMatchers(
                        "/discuss/top",
                        "/discuss/wonderful"
                )
                .hasAnyAuthority(
                        AUTHORITY_MODERATOR
                )
                .antMatchers(
                        "/discuss/delete",
                        "/data/**"
                )
                .hasAnyAuthority(
                        AUTHORITY_ADMIN
                )
                .anyRequest().permitAll()
                        .and().csrf().disable();

        // 权限不够时的处理
        http.exceptionHandling()
                .authenticationEntryPoint(new AuthenticationEntryPoint() {
                    // 针对没有登陆的处理
                    @Override
                    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException e) throws IOException, ServletException {
                        String xRequestedWith = request.getHeader("x-requested-with");
                        // 同步请求
                        if (xRequestedWith == null) {
                            response.sendRedirect(request.getContextPath() + "/login");
                        }else {
                            if (xRequestedWith.equals("XMLHttpRequest")) {
                                // 异步请求,不能返回html页面回去，要返回JSON格式的数据
                                response.setContentType("application/plain;charset=utf-8");
                                response.getWriter().write(CommunityUtil.getJSONString(403, "你还没有登录哦!"));
                            }else {
                                // 同步请求
                                response.sendRedirect(request.getContextPath() + "/login");
                            }
                        }
                    }
                })
                .accessDeniedHandler(new AccessDeniedHandler() {
                    // 针对权限不足的处理
                    @Override
                    public void handle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AccessDeniedException e) throws IOException, ServletException {
                        String xRequestedWith = httpServletRequest.getHeader("x-requested-with");
                        if (xRequestedWith.equals("XMLHttpRequest")) {
                            // 异步请求,不能返回html页面回去，要返回JSON格式的数据
                            httpServletResponse.setContentType("application/plain;charset=utf-8");
                            httpServletResponse.getWriter().write(CommunityUtil.getJSONString(403, "你没有访问此功能的权限!"));
                        }else {
                            // 同步请求
                            httpServletResponse.sendRedirect(httpServletRequest.getContextPath() + "/denied");
                        }
                    }
                });

        // Security底层默认会拦截/logout请求,进行退出处理
        // 这里覆盖它默认的逻辑,才能执行我们自己的退出代码
        // 这里设置的logout路径是假的，目的是让Security的这个功能失效
        http.logout().logoutUrl("/securitylogout");
    }
}
