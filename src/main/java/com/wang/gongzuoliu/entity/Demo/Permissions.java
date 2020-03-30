package com.wang.gongzuoliu.entity.Demo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Demo使用，权限实体类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Permissions {

    private Integer permissionsId;
    private String permissionsName;
}
