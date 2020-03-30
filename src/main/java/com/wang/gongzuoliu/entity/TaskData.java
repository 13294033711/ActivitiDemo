package com.wang.gongzuoliu.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskData {
    //任务ID
    private String taskId;
    private String taskName;
    private String executionId;
    private String instanceId;
    //执行人
    private String assignee;
}
