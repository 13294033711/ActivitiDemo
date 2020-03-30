package com.wang.gongzuoliu.controller;

import com.wang.gongzuoliu.common.ActivitiUtils;
import com.wang.gongzuoliu.common.StreamUtils;
import com.wang.gongzuoliu.common.UserUtils;
import com.wang.gongzuoliu.entity.AskForm;
import com.wang.gongzuoliu.entity.TaskData;
import com.wang.gongzuoliu.service.WebSocketServer;
import org.activiti.api.process.runtime.ProcessRuntime;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.*;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricActivityInstanceQuery;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.runtime.ProcessInstanceQuery;
import org.activiti.engine.task.Task;
import org.activiti.image.ProcessDiagramGenerator;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("demo")
public class DemoController {


    private static final Logger logger = LoggerFactory.getLogger(DemoController.class);

    /** 流程定义和部署相关的存储服务 */
    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private ProcessRuntime processRuntime;

    /** 流程运行时相关的服务 */
    @Autowired
    private RuntimeService runtimeService;

    /** 节点任务相关操作接口 */
    @Autowired
    private TaskService taskService;

    /** 流程图生成器 */
    @Autowired
    private ProcessDiagramGenerator processDiagramGenerator;

    /** 历史记录相关服务接口 */
    @Autowired
    private HistoryService historyService;



    /**
     * 跳转请假申请单页面
     */
    @RequestMapping(value="/toLeave")
    public String employeeLeave(Model model) {
        model.addAttribute(new AskForm());
        return "employeeLeave";
    }



    /**
     * 启动请假流程
     */
    @RequestMapping(value="/start")
    @ResponseBody
    public String start() {
        //从配置文件中获取
        String instanceKey = "holiday";
        logger.info("开启请假流程...");

        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionKey(instanceKey).singleResult();
        if (null==processDefinition) {
            repositoryService.createDeployment().
        addClasspathResource("processes/holiday.bpmn").addClasspathResource("processes/holiday.png")
        .deploy();
        }

        /*
         *  设置流程参数，开启流程
         */
        ProcessInstance instance = runtimeService.startProcessInstanceByKey(instanceKey);//使用流程定义的key启动流程实例，key对应helloworld.bpmn文件中id的属性值，使用key值启动，默认是按照最新版本的流程定义启动

        logger.info("启动流程实例成功:{}", instance);
        logger.info("流程实例ID:{}", instance.getId());
        logger.info("流程定义ID:{}", instance.getProcessDefinitionId());

        //通过查询正在运行的流程实例来判断是否启动
        ProcessInstanceQuery processInstanceQuery = runtimeService.createProcessInstanceQuery();
        //根据流程实例ID来查询
        List<ProcessInstance> runningList = processInstanceQuery.processInstanceId(instance.getProcessInstanceId()).list();
        logger.info("根据流程ID查询条数:{}", runningList.size());
        //返回流程ID
        return instance.getId();
    }

    /**
     * 获取实例Id
     * @param request
     * @return
     */
//    @RequestMapping("getInstanceId")
//    @ResponseBody
//    public String getInstanceId(HttpServletRequest request) {
//        String instanceKey = request.getParameter("instanceKey");
//        System.out.println(instanceKey);
//        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processDefinitionKey(instanceKey).singleResult();
//        logger.info(processInstance.getId());
//        return processInstance.getId();
//    }

    /**
     * 查看任务详情
     */
    @ResponseBody
    @RequestMapping("getTaskByIdData")
    public Task getTaskById(String taskId) {
        Task task=taskService.createTaskQuery() // 创建任务查询
                .taskId(taskId) // 根据任务id查询
                .singleResult();
        return task;
    }



    /**
     * 查看当前流程图
     * @param instanceId 流程实例
     * @param response void 响应
     */
    @ResponseBody
    @RequestMapping(value="show/img")
    public void showImg(String instanceId, HttpServletResponse response) {

        //参数校验
        logger.info("查看完整流程图！流程实例ID:{}", instanceId);
        if(StringUtils.isBlank(instanceId)) return;

        //获取流程实例
        HistoricProcessInstance processInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(instanceId).singleResult();
        if(processInstance == null) {
            logger.error("流程实例ID:{}没查询到流程实例！", instanceId);
            return;
        }

        // 根据流程对象获取流程对象模型
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processInstance.getProcessDefinitionId());

        /*
         *  查看已执行的节点集合
         *  获取流程历史中已执行节点，并按照节点在流程中执行先后顺序排序
         */
        // 构造历史流程查询
        HistoricActivityInstanceQuery historyInstanceQuery = historyService.createHistoricActivityInstanceQuery().processInstanceId(instanceId);
        // 查询历史节点
        List<HistoricActivityInstance> historicActivityInstanceList = historyInstanceQuery.orderByHistoricActivityInstanceStartTime().asc().list();
        if(historicActivityInstanceList == null || historicActivityInstanceList.size() == 0) {
            logger.info("流程实例ID:{}没有历史节点信息！", instanceId);
            outputImg(response, bpmnModel, null, null);
            return;
        }
        // 已执行的节点ID集合(将historicActivityInstanceList中元素的activityId字段取出封装到executedActivityIdList)
        List<String> executedActivityIdList = historicActivityInstanceList.stream().map(item -> item.getActivityId()).collect(Collectors.toList());

        /*
         *  获取流程走过的线
         */
        // 获取流程定义
        ProcessDefinitionEntity processDefinition = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService).getDeployedProcessDefinition(processInstance.getProcessDefinitionId());
        List<String> flowIds = ActivitiUtils.getHighLightedFlows(bpmnModel, processDefinition, historicActivityInstanceList);


        /*
         * 输出图像，并设置高亮
         */
        outputImg(response, bpmnModel, flowIds, executedActivityIdList);
    }



    /**
     * 员工提交申请
     * @param request 请求
     * @return String 申请受理结果
     */
    @RequestMapping(value="employeeApply")
    public String employeeApply(AskForm askForm, HttpServletRequest request){
        start();

        //流转信息
        Map<String,Object> variable=new HashMap<>();
        variable.put("askForm",askForm);

        //获取填单任务，并完成
        Task task = taskService.createTaskQuery()
                .processDefinitionKey("holiday")
                .taskAssignee("wangchen")
                .singleResult();
        if(task == null) {
            logger.info("任务ID:{}查询到任务为空！", task.getId());
            return "fail";
        }
        taskService.complete(task.getId(),variable);
        logger.info("执行【填表】环节，流程推动到【上级审核】环节");

        /*
         * 返回成功
         */
        return "index";
    }

    /**
     * 审核
     */
    @RequestMapping("approve")
    public String approve(HttpServletRequest request) {
        String taskId = request.getParameter("taskId");
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        String processInstanceId = task.getProcessInstanceId();
        taskService.complete(taskId);
        if(isFinished(processInstanceId)) {
            try {
                WebSocketServer.sendInfo("您发起的流程结束了", "wangchen");
            }catch (IOException e) {
                e.printStackTrace();
                return "errer";
            }
        }
        logger.info("执行【审批】环节，流程结束");

        return "index";
    }

    /**
     * 获取流转参数（请假单）
     * @return
     */
    @RequestMapping(value="viewTaskDetail")
    @ResponseBody
    public Map<String, Object> toHigherAudit(HttpServletRequest request, Model model) {
        String taskId = request.getParameter("taskId");
        Map<String, Object> paramMap = new HashMap<>();

        logger.info("跳转到任务详情页面，任务ID:{}", taskId);
        if(StringUtils.isBlank(taskId)) {
            return paramMap;
        }

        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if(task == null) {
            logger.info("任务ID:{}不存在！", taskId);
            return paramMap;
        }

        paramMap = taskService.getVariables(taskId);
        return paramMap;
    }

    /**
     * 查看任务页面
     * @param request 请求
     * @return String  任务展示页面
     */
    @RequestMapping(value="/toShowTask")
    public String toShowTask(HttpServletRequest request, Model model) {

        UserDetails details = UserUtils.getCurrentPrincipal();
        String username = details.getUsername();

        List<Task> taskList = taskService.createTaskQuery().taskAssignee(username).list();
        List<TaskData> tasks = new ArrayList<>();
        if(taskList == null || taskList.size() == 0) {
            logger.info("查询任务列表为空！");
            model.addAttribute("tasks", tasks);
            return "/task";
        }

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
//            taskData.setCreateTime(task.getCreateTime());
//            taskData.setDefinitionId(task.getProcessDefinitionId());
            tasks.add(taskData);
        }

        logger.info("返回集合:{}", tasks.toString());
        model.addAttribute("tasks", tasks);
        return "task";
    }


    /**
     * 输出图像
     * @param response 响应实体
     * @param bpmnModel 图像对象
     * @param flowIds 已执行的线集合
     */
    private void outputImg(HttpServletResponse response, BpmnModel bpmnModel, List<String> flowIds, List<String> executedActivityIdList) {
        InputStream imageStream = null;
        try {
            imageStream = processDiagramGenerator.generateDiagram(bpmnModel, executedActivityIdList, flowIds, "宋体", "微软雅黑", "黑体", true);
            // 输出资源内容到相应对象
            byte[] b = new byte[1024];
            int len;
            while ((len = imageStream.read(b, 0, 1024)) != -1) {
                response.getOutputStream().write(b, 0, len);
            }

            response.getOutputStream().flush();
        }catch(Exception e) {
            logger.error("流程图输出异常！", e);
        } finally { // 流关闭
            StreamUtils.closeInputStream(imageStream);
        }
    }

    /**
     * 判断流程是否完成
     * @param processInstanceId 流程实例ID
     * @return boolean 已完成-true，未完成-false
     */
    public boolean isFinished(String processInstanceId) {
        return historyService.createHistoricProcessInstanceQuery().finished().processInstanceId(processInstanceId).count() > 0;
    }
}
