package com.wang.gongzuoliu.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 流程模型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProcessModel {

    /**
     * 模型编号
     */
    private String id;

    /**
     * 版本
     */
    private String version;

    /**
     * 模型名称
     */
    private String name;

    /**
     * 模型Key
     */
    private String key;
}
