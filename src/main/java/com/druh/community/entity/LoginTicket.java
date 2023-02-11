package com.druh.community.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 登陆凭证实体类，对应数据库中的login_ticket表
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginTicket {

    private int id;

    private int userId;

    private String ticket;

    private int status;

    private Date expired;

}
