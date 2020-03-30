package com.wang.gongzuoliu.entity.Demo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * Demo使用，用户实体
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {

    private Integer userId;
    private String userName;
    private String password;
    /**
     * 用户角色集合
     */
    private Set<Role> roles;
}
