package com.wang.gongzuoliu.entity.Demo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * Demo使用，角色实体
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Role {

    private Integer roleId;
    private String roleName;
    /**
     * 权限集合
     */
    private Set<Permissions> permissions;
}
