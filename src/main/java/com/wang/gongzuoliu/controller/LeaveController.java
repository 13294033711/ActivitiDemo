package com.wang.gongzuoliu.controller;

import com.wang.gongzuoliu.common.StrFormatter;
import com.wang.gongzuoliu.common.StringUtils;
import com.wang.gongzuoliu.common.UserUtils;
import com.wang.gongzuoliu.common.demo.LeaderUtils;
import com.wang.gongzuoliu.entity.*;
import com.wang.gongzuoliu.service.IAskFormService;
import com.wang.gongzuoliu.service.WebSocketServer;
import lombok.extern.slf4j.Slf4j;
import org.activiti.bpmn.model.*;
import org.activiti.engine.*;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Task;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.baomidou.mybatisplus.core.toolkit.IdWorker.getId;

/**
 * 请假流程控制器
 */
@Slf4j
@EnableTransactionManagement
@RequestMapping("leave")
@Controller
public class LeaveController {

    @Value("${activiti.activiti_names.leave_name}")
    private String leaveName;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private IAskFormService askFormService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private ProcessEngine processEngine;

    /**
     * 跳转请假申请单页面
     */
    @RequestMapping(value="/toLeave")
    public String employeeLeave(Model model) {
        model.addAttribute(new AskForm());
        return "employeeLeave";
    }

    /**
     * 请假表单提交
     */
    @PostMapping("employeeApply")
    public String  employeeApply(AskForm askForm) {
        //1 保存业务表单
        askForm.setApplyer(UserUtils.getCurrentPrincipal().getUsername());
        askFormService.save(askForm);
        askForm = askFormService.getById(askForm);

        //2 开启流程
        List<Deployment> deployments = repositoryService.createDeploymentQuery().deploymentName(leaveName).orderByDeploymenTime().asc().list();
        String deploymentId = deployments.get(0).getId();
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().deploymentId(deploymentId).singleResult();
        if (null==processDefinition) {
            throw new RuntimeException("流程启动失败");
        }
        Map<String, Object> variables = new HashMap<>();
        variables.put("askForm", askForm);
        runtimeService.startProcessInstanceByKey(processDefinition.getKey(), String.valueOf(askForm.getLeaveId()), variables);

        return "index";
    }

    /**
     * 员工完成提交流程任务
     */
    @RequestMapping("submit")
    public String submit(String leaveId) {
        //1 更新业务状态
        AskForm askForm = askFormService.getById(leaveId);
        askForm.setFormStatus(2);
        boolean flag = askFormService.updateById(askForm);
        if (!flag) {
            throw new RuntimeException("员工提交失败");
        }
        askForm = askFormService.getById(leaveId);

        //2 根据业务Id，完成任务
        Execution execution = runtimeService.createExecutionQuery().processInstanceBusinessKey(leaveId).singleResult();
        String pI = execution.getProcessInstanceId();
        Task task = taskService.createTaskQuery().processInstanceId(pI).taskAssignee(askForm.getApplyer()).singleResult();

        //3 设置通过条件
//        Map<String, Object> variables = taskService.getVariables(task.getExecutionId());
//        variables.put("askForm", askForm);
//        taskService.setVariables(task.getId(), "askForm", askForm);
        taskService.setVariable(task.getId(), "askForm", askForm);

        //4 完成并传递
        completeTask(task.getId(), task.getProcessInstanceId(),null, askForm);

        return "index";
    }

    /**
     * 用户废止任务
     * @param askForm
     * @return
     */
    @RequestMapping("delete")
    public String delete(String leaveId) {
        AskForm af = askFormService.getById(leaveId);

        //0 拿到流程变量
        Execution execution = runtimeService.createExecutionQuery().processInstanceBusinessKey(String.valueOf(af.getLeaveId())).singleResult();
        String pI = execution.getProcessInstanceId();
        Task task = taskService.createTaskQuery().processInstanceId(pI).taskAssignee(af.getApplyer()).singleResult();

        AskForm askForm = askFormService.getById(af);
        askForm.setFormStatus(4);
        boolean flag = askFormService.updateById(askForm);

        if (!flag) {
            throw new RuntimeException("员工废止失败");
        }
        askForm = askFormService.getById(af);
        taskService.setVariable(task.getId(), "askForm", askForm);

        //2 根据业务Id，完成任务
        completeTask(task.getId(), task.getProcessInstanceId(), null, askForm);
        return "index";
    }

    /**
     * 查看任务页面
     * @param request 请求
     * @return String  任务展示页面
     */
    @RequestMapping("toShowTask")
    public String toShowTask(Model model) {

        UserDetails details = UserUtils.getCurrentPrincipal();
        String username = details.getUsername();

        //从处理人做查询参数
        List<Task> taskList = taskService.createTaskQuery().taskAssignee(username).list();
        List<TaskData> tasks = new ArrayList<>();
        List<Task> list = taskService.createTaskQuery().taskCandidateUser(username).list();

        taskList.addAll(list);
        /*
         * 查询所有任务，并封装
         */
        for(Task task : taskList) {
            TaskData taskData = new TaskData();
            taskData.setTaskId(task.getId());
            taskData.setAssignee(task.getAssignee());
            taskData.setExecutionId(task.getExecutionId());
            taskData.setTaskName(task.getName());
            taskData.setInstanceId(task.getProcessInstanceId());
            tasks.add(taskData);
        }

        log.info("返回集合:{}", tasks.toString());
        model.addAttribute("tasks", tasks);
        return "task";
    }

    /**
     * 查询已处理任务列表。
     *
     */
    @RequestMapping("queryDoneTasks")
    public String queryDoneTasks(Model model) {
        String cuurUser = UserUtils.getCurrentUserAuthentication().getName();
        List<HistoricTaskInstance> taskList  = historyService.createHistoricTaskInstanceQuery()
                .taskAssignee(cuurUser)
                .finished()
                .list();

        List<TaskData> tasks = new ArrayList<>();
        /*
         * 查询所有任务，并封装
         */
        for(HistoricTaskInstance task : taskList) {
            TaskData taskData = new TaskData();
            taskData.setTaskId(task.getId());
            taskData.setAssignee(task.getAssignee());
            taskData.setExecutionId(task.getExecutionId());
            taskData.setTaskName(task.getName());
            taskData.setInstanceId(task.getProcessInstanceId());
            tasks.add(taskData);
        }
        model.addAttribute("tasks", tasks);
        return "doneTask";
    }

    /**
     * 获取请假单详情
     * @return
     */
    @RequestMapping(value="viewTaskDetail")
    @ResponseBody
    public Map<String, Object> viewTaskDetail(HttpServletRequest request, Model model) {
        String taskId = request.getParameter("taskId");
        AskForm askForm = getAskFormByTaskId(taskId);

        Map<String, Object> paramMap = new HashMap<>();

        paramMap.put("askForm", askForm);
        return paramMap;
    }

    /**
     * 审批通过
     */
    @RequestMapping("approve")
    public String approve(HttpServletRequest request) {
        //拿到业务表单
        String taskId = request.getParameter("taskId");
        String comment = request.getParameter("comment");
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        ProcessInstance pi = runtimeService.createProcessInstanceQuery().processInstanceId(task.getProcessInstanceId()).singleResult();
        String businessKey = pi.getBusinessKey();
        AskForm askForm = askFormService.getById(businessKey);

        if(null==task.getAssignee()) {
            //这是一个候选人任务，要先拾取，再完成，针对总经理们
            taskService.claim(taskId, UserUtils.getCurrentPrincipal().getUsername());
            //查询最新任务
            task = taskService.createTaskQuery().taskId(taskId).singleResult();
        }else {
            //否则这是一个有处理人的任务
            String assignee = task.getAssignee();
            if(assignee.equals(LeaderUtils.getProjectManager()) || assignee.equals(LeaderUtils.getPersonnelMatters())) {
                //如果处理人是项目经理或人事，变更业务表状态置为3
                askForm.setFormStatus(3);
            }else {
                //否则处理人是其他，不需要变更状态为2
                askForm.setFormStatus(2);
            }
        }
        askForm.setApprover(task.getAssignee());
        //保存业务表单
        boolean flag = askFormService.updateById(askForm);
        if(!flag) {
            throw new RuntimeException(StrFormatter.format("{}【审批】失败", task.getAssignee()));
        }

        //重设流程变量
        taskService.setVariable(taskId, "askForm", askForm);

        //完成并传递
        completeTask(taskId, task.getProcessInstanceId(), comment, askForm);

        String processInstanceId = task.getProcessInstanceId();
        if(isFinished(processInstanceId)) {
            try {
                WebSocketServer.sendInfo("您发起的流程结束了", askForm.getApplyer());
            }catch (IOException e) {
                e.printStackTrace();
                return "errer";
            }
        }
        log.info("执行【审批】环节");
        return "index";
    }

    /**
     * 统一审批方法
     */
    @RequestMapping("approveTy")
    public String approveTy(HttpServletRequest request) {
        //拿到业务表单
        String taskId = request.getParameter("taskId");
        String comment = request.getParameter("comment");
        String formStatus = request.getParameter("formStatus");
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        String executionId = task.getExecutionId();
        ProcessInstance pi = runtimeService.createProcessInstanceQuery().processInstanceId(task.getProcessInstanceId()).singleResult();
        String key = pi.getBusinessKey();
        String businessKey = StringUtils.isEmpty(key)?taskService.getVariable(taskId,"businessKey").toString():key;
        AskForm askForm = askFormService.getById(businessKey);

//        if(null==task.getAssignee()) {
//            //这是一个候选人任务，要先拾取，再完成，针对总经理们
//            taskService.claim(taskId, UserUtils.getCurrentPrincipal().getUsername());
//            //查询最新任务
//            task = taskService.createTaskQuery().taskId(taskId).singleResult();
//        }
        taskService.claim(taskId, UserUtils.getCurrentPrincipal().getUsername());
        //查询最新任务
        task = taskService.createTaskQuery().taskId(taskId).singleResult();

        if(!StringUtils.isEmpty(formStatus)) {
            askForm.setFormStatus(Integer.valueOf(formStatus));
        }

        //子流程设置出去条件
        if (StringUtils.isEmpty(key)) {
            askForm.setFormStatus(3);
        }

        askForm.setApprover(task.getAssignee());
        //保存业务表单
        boolean flag = askFormService.updateById(askForm);
        if(!flag) {
            throw new RuntimeException(StrFormatter.format("{}【审批】失败", task.getAssignee()));
        }


        //重设流程变量
        taskService.setVariable(taskId, "askForm", askForm);    //这个方法是构造了一个新的map，把变量放进去；
        runtimeService.setVariable(executionId, "askForm", askForm);
        //完成并传递
        completeTask(taskId, task.getProcessInstanceId(), comment, askForm);

        String processInstanceId = task.getProcessInstanceId();
        if(isFinished(processInstanceId)) {
            try {
                WebSocketServer.sendInfo("您发起的流程结束了", askForm.getApplyer());
            }catch (IOException e) {
                e.printStackTrace();
                return "errer";
            }
        }
        log.info("执行【审批】环节");
        return "index";
    }

    /**
     * 审批退回(项目经理或总经理执行)
     */
    @RequestMapping("backOff")
    public String backOff(HttpServletRequest request) {
        String taskId = request.getParameter("taskId");
        String comment = request.getParameter("comment");
        //拿到业务表单
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        ProcessInstance pi = runtimeService.createProcessInstanceQuery().processInstanceId(task.getProcessInstanceId()).singleResult();

        String businessKey = pi.getBusinessKey();
        AskForm askForm = askFormService.getById(businessKey);

        if (null==task.getAssignee()) {
            //这是一个候选人任务，需要先拾取
            taskService.claim(taskId, UserUtils.getCurrentPrincipal().getUsername());
        }
        //变更业务表单状态为1
        askForm.setFormStatus(1);
        boolean flag = askFormService.updateById(askForm);
        if (!flag) {
            throw new RuntimeException(StrFormatter.format("{}【退回】失败", task.getAssignee()));
        }
        askForm = askFormService.getById(askForm);

        //设置任务通过条件
        taskService.setVariable(taskId, "askForm", askForm);

        //完成并传递
        completeTask(task.getId(), task.getProcessInstanceId(), comment, askForm);

        return "index";
    }

    /**
     * 根据任务Id taskId 获取业务表单
     */
    private AskForm getAskFormByTaskId(String taskId) {
        //1  获取任务对象
        Task task  =  taskService.createTaskQuery().taskId(taskId).singleResult();

        //2  通过任务对象获取流程实例
        ProcessInstance pi = runtimeService.createProcessInstanceQuery().processInstanceId(task.getProcessInstanceId()).singleResult();

        //3 通过流程实例获取“业务键”
        String businessKey = StringUtils.isEmpty(pi.getBusinessKey())?taskService.getVariable(taskId,"businessKey").toString():pi.getBusinessKey();

        AskForm askForm = askFormService.getById(businessKey);
        return askForm;
    }

    /**
     * 统一完成任务方法
     * @param taskId
     * @param askForm
     */
    private void completeTask(String taskId, String processInstanceId, String comment, AskForm askForm) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("askForm", askForm);
        // 添加批注时候的审核人
        Authentication.setAuthenticatedUserId(UserUtils.getCurrentPrincipal().getUsername());
        taskService.addComment(taskId,processInstanceId, comment==null?"":comment);
        taskService.complete(taskId, variables);
    }
//    private void completeTask(String taskId, String processInstanceId, String comment, Map<String, Object> variables) {
//        // 添加批注时候的审核人
//        Authentication.setAuthenticatedUserId(UserUtils.getCurrentPrincipal().getUsername());
//        taskService.addComment(taskId,processInstanceId, comment==null?"":comment);
//        taskService.complete(taskId, variables);
//    }

    /**
     * 判断流程是否完成
     * @param processInstanceId 流程实例ID
     * @return boolean 已完成-true，未完成-false
     */
    public boolean isFinished(String processInstanceId) {
        return historyService.createHistoricProcessInstanceQuery().finished().processInstanceId(processInstanceId).count() > 0;
    }

    /**
     * 根据任务Id查询批注信息
     */
    @RequestMapping("queryCommentByTaskId")
    @ResponseBody
    public List<CommentData> queryCommentByTaskId(String taskId) {
        //1 根据任务ID查询任务实例
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();

        //2 从任务里面取出流程实例ID
        String processInstanceId = task.getProcessInstanceId();

        List<Comment> comments = taskService.getProcessInstanceComments(processInstanceId);
        List<CommentData> result = new ArrayList<>();
        if(null!=comments && comments.size()>0) {
            for (Comment comment: comments) {
                CommentData data = new CommentData();
                BeanUtils.copyProperties(comment, data);
                result.add(data);
            }
        }
        return result;
    }

    /**
     * 根据任务ID查询连线（出）信息
     *
     */
    @RequestMapping("findOutComeListByTaskId")
    @ResponseBody
//    public List<OutFlowInfo> findOutComeListByTaskId(String taskId) {
    public List<String> findOutComeListByTaskId(String taskId) {
        List<String> strs = new ArrayList<>();
        List<OutFlowInfo> outFlowInfos = new ArrayList<>();
        //返回存放连线的名称集合
        List<Map<String, String>> list = new ArrayList<>();
        //1:使用任务ID，查询任务对象
        Task task = taskService.createTaskQuery()
                .taskId(taskId)
                .singleResult();
        //2:获取流程定义ID
        String processDefinitionId = task.getProcessDefinitionId();
        //获取流程实例ID
        String processInstanceId = task.getProcessInstanceId();
        //获取当前活动ID,并行会有多个Id
        List<String> activeActivityIds = runtimeService.getActiveActivityIds(processInstanceId);

        BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinitionId);
        List<SequenceFlow> outs = new ArrayList<>();
        for (String activityId: activeActivityIds) {
            //符合当前任务,并发条件下会有同一个活动节点，会有多个执行Id，所以要用list接收
            List<Execution> executions = runtimeService.createExecutionQuery().activityId(activityId).list();
            String currentExeId = null;
            for(Execution execution: executions) {
                Task tempTask = taskService.createTaskQuery().executionId(execution.getId()).singleResult();
                //没有找到这个任务，执行下一个
                if (null == tempTask) {
                    continue;
                }
                if (taskId.equals(tempTask.getId())) {
                    currentExeId = execution.getId();
                }
            }
            //如果没有找到，则进入下个循环
            if (null == currentExeId) {
                continue;
            }
            FlowNode flowElement = (FlowNode) bpmnModel.getFlowElement(activityId);
            outs = flowElement.getOutgoingFlows();
            if (null != outs && outs.size()>0) {
                for(SequenceFlow out: outs) {
                    String name = StringUtils.isEmpty(out.getName())?"通过":out.getName();
                    String conditionExpression = out.getConditionExpression();
                    List<String> strings = dealConditionExpression(conditionExpression);
                    String join = StringUtils.join(strings.toArray(), "&");
                    String str = "<a class=\"btn btn-sm btn-primary\" onclick=\"javascript: shenpi('"+join+"')\">"+name+"</a>";
                    strs.add(str);
//                        OutFlowInfo outFlowInfo = new OutFlowInfo(name, strs);
//                        outFlowInfos.add(outFlowInfo);
                }
            }
        }

//        if (null != outs && outs.size()>0) {
//            Map<String, String> result = new HashMap<>();
//            for(FlowElement flow: outs) {
//                FlowNode f = (FlowNode) flow;
//                result.put("conditionExpression", " ");
//                result.put("name", f.getName());
//                list.add(result);
//            }
//        }



        //5:获取当前活动完成之后连线的名称
//        List<PvmTransition> pvmList = activityImpl.getOutgoingTransitions();
//        if(pvmList != null && pvmList.size()>0) {
//            for(PvmTransition pvm:pvmList) {
//                String name = (String) pvm.getProperty("name");
//                if(StringUtils.isNotBlank(name)) {
//                    list.add(name);
//                }else {
//                    list.add("默认提交");
//                }
//            }
//        }
//        return outFlowInfos;
        return strs;
    }

    /**
     * 处理条件表达式
     * @param conditionExpression
     * @return
     */
    public List<String> dealConditionExpression(String conditionExpression) {
        List<String> conditions = new ArrayList<>();
        //截取花括号中的内容
        if (StringUtils.isEmpty(conditionExpression)) {
            return conditions;
        }
        String substring = StringUtils.substring(conditionExpression, conditionExpression.indexOf("{") + 1, conditionExpression.indexOf("}"));
        String[] split = substring.split("&&");
        for (String s : split) {
            s = s.trim();
            conditions.add(s);
        }
        return conditions;
    }
}
