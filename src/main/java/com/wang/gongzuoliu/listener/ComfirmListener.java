package com.wang.gongzuoliu.listener;

import com.alibaba.fastjson.JSONObject;
import com.wang.gongzuoliu.common.demo.LeaderUtils;
import com.wang.gongzuoliu.entity.AskForm;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.delegate.TaskListener;

import java.util.*;

/**
 * 人事完成审批监听器
 */
public class ComfirmListener implements TaskListener {

    @Override
    public void notify(DelegateTask delegateTask) {
        System.out.println("执行获取【添加确认领导】监听器");

        Set<String> userIds=new LinkedHashSet<String>();
        userIds.add(LeaderUtils.getDirector());
        userIds.add(LeaderUtils.getDeputyDirector());
        userIds.add(LeaderUtils.getGeneralManagers().get(0));

        String json = delegateTask.getVariable("askForm").toString();
        AskForm askForm = new JSONObject().parseObject(json, AskForm.class);
        askForm.setNeedComfirms(userIds.size());
        askForm.setComfirmeds(0);
        delegateTask.setVariable("askForm", askForm);

//        Map<String, Object> variables = new HashMap<>();
//        variables.put("askForm", askForm);
//        variables.put("comfirmLeaders", new ArrayList<String>(userIds));
//        delegateTask.setVariables(variables);
        System.out.println("【添加确认领导】监听器执行结束");
    }
}
