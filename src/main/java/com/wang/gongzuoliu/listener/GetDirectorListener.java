package com.wang.gongzuoliu.listener;

import com.wang.gongzuoliu.common.demo.LeaderUtils;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;

/**
 * 获取主任监听器
 */
public class GetDirectorListener implements TaskListener {
    @Override
    public void notify(DelegateTask delegateTask) {
        //模拟获取主任，并设置办理人
        String assignee = LeaderUtils.getDirector();
        delegateTask.setAssignee(assignee);
        delegateTask.addCandidateUser(assignee);
    }

}
