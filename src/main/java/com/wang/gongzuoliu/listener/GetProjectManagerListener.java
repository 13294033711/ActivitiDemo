package com.wang.gongzuoliu.listener;

import com.wang.gongzuoliu.common.demo.LeaderUtils;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;

/**
 * 获取项目经理监听器
 */
public class GetProjectManagerListener implements TaskListener {
    @Override
    public void notify(DelegateTask delegateTask) {
        //模拟获取项目经理，并设置处理人
        String assignee = LeaderUtils.getProjectManager();
//        delegateTask.setAssignee(assignee);
        delegateTask.addCandidateUser(assignee);
    }
}
