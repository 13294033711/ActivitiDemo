package com.wang.gongzuoliu.listener;

import com.alibaba.fastjson.JSONArray;
import com.wang.gongzuoliu.common.demo.LeaderUtils;
import com.wang.gongzuoliu.entity.AskForm;
import com.wang.gongzuoliu.service.IAskFormService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * 多任务完成后监听器
 */
@Service
public class MuliTaskCompleteListener implements TaskListener {

    @Transactional
    public void notify(DelegateTask delegateTask){
        System.err.println("1.子流程任务创建======delegateTask{}:" + delegateTask.getId());
        //获取子流程变量
        DelegateExecution execution =  delegateTask.getExecution();
        DelegateExecution parent = execution.getParent();
        String json = parent.getVariable("askForm").toString();
        AskForm askForm = JSONArray.parseObject(json, AskForm.class);
        String businessKey = askForm.getLeaveId().toString();
        Map<String, Object> variables = new HashMap<>();
        variables.put("askForm", askForm);
        variables.put("businessKey", businessKey);

        delegateTask.setVariables(variables);
        System.err.println(
                "2.获取子流程参数====="
                        + ";businessKey:" + businessKey);

    }

//    public void notify(DelegateTask delegateTask) {
//        System.out.println("多任务执行开始");
//        AskForm askForm = delegateTask.getVariable("askForm", AskForm.class);
//        askForm.setFormStatus(3);
//        if (askFormService.updateById(askForm)) {
//            askForm = askFormService.getById(askForm.getLeaveId());
//        }else {
//            throw new RuntimeException("保存表单错误");
//        }
//        delegateTask.setVariable("askForm", askForm);

//        System.out.println("多任务执行完毕");
//    }
}
