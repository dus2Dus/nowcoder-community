package com.druh.community.mapper;

import com.druh.community.entity.LoginTicket;
import org.apache.ibatis.annotations.*;

@Mapper
@Deprecated
public interface LoginTicketMapper {

    /**
     * 插入一条登录凭证
     * @param loginTicket
     * @return
     */
    @Insert({
            "insert into login_ticket(user_id, ticket, status, expired) ",
            "values(#{userId}, #{ticket}, #{status}, #{expired})"
    })
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertLoginTicket(LoginTicket loginTicket);

    /**
     * 找一条登陆凭证的数据
     * @param ticket
     * @return
     */
    @Select({
            "select * from login_ticket ",
            "where ticket = #{ticket}"
    })
    LoginTicket selectByTicket(String ticket);

    /**
     * 更新状态
     * @param ticket
     * @param status
     * @return
     */
    @Update({
            "update login_ticket set status = #{status} where ticket = #{ticket}"
    })
    int updateStatus(String ticket, int status);

}
