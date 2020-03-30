package com.wang.gongzuoliu.common;

import java.util.*;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.FlowNode;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;

/**
 * <p>Activiti工作流工具类</p>
 * @author
 * @time
 * @version 1.0
 */
public class ActivitiUtils {


    /**
     * 获取流程走过的线
     * @param bpmnModel 流程对象模型
     * @param processDefinitionEntity 流程定义对象
     * @param historicActivityInstances 历史流程已经执行的节点，并已经按执行的先后顺序排序
     * @return List<String> 流程走过的线
     * @version 1.0
     */
    public static List<String> getHighLightedFlows(BpmnModel bpmnModel, ProcessDefinitionEntity processDefinitionEntity, List<HistoricActivityInstance> historicActivityInstances) {
        // 用以保存高亮的线flowId
        List<String> highFlows = new ArrayList<String>();
        if(historicActivityInstances == null || historicActivityInstances.size() == 0) return highFlows;

        // 遍历历史节点
        for (int i = 0; i < historicActivityInstances.size() - 1; i++) {
            // 取出已执行的节点
            HistoricActivityInstance activityImpl_ = historicActivityInstances.get(i);

            // 用以保存后续开始时间相同的节点
            List<FlowNode> sameStartTimeNodes = new ArrayList<FlowNode>();

            // 获取下一个节点（用于连线）
//            List<FlowNode> nextFlowNodes = getNextFlowNodes(bpmnModel, historicActivityInstances, i, activityImpl_);
            FlowNode sameActivityImpl = getNextFlowNode(bpmnModel, historicActivityInstances, i, activityImpl_);

            // 将后面第一个节点放在时间相同节点的集合里
            if(sameActivityImpl != null) sameStartTimeNodes.add(sameActivityImpl);
//            if (null != nextFlowNodes && nextFlowNodes.size()>0) {
//                for (FlowNode node: nextFlowNodes) {
//                    sameStartTimeNodes.add(node);
//                }
//            }

            // 循环后面节点，看是否有与此后继节点开始时间相同的节点，有则添加到后继节点集合
            for (int j = i + 1; j < historicActivityInstances.size() - 1; j++) {
                HistoricActivityInstance activityImpl1 = historicActivityInstances.get(j);// 后续第一个节点
                HistoricActivityInstance activityImpl2 = historicActivityInstances.get(j + 1);// 后续第二个节点
                if (activityImpl1.getStartTime().getTime() != activityImpl2.getStartTime().getTime()) break;

                // 如果第一个节点和第二个节点开始时间相同保存
                FlowNode sameActivityImpl2 = (FlowNode) bpmnModel.getMainProcess().getFlowElement(activityImpl2.getActivityId());
                sameStartTimeNodes.add(sameActivityImpl2);
            }

            // 得到节点定义的详细信息
            FlowNode activityImpl = (FlowNode) bpmnModel.getMainProcess().getFlowElement(historicActivityInstances.get(i).getActivityId());

            if (null == activityImpl) {
                continue;
            }
            // 取出节点的所有出去的线，对所有的线进行遍历
            List<SequenceFlow> pvmTransitions = activityImpl.getOutgoingFlows();
            for (SequenceFlow pvmTransition : pvmTransitions) {
                // 获取节点
                FlowNode pvmActivityImpl = (FlowNode) bpmnModel.getMainProcess().getFlowElement(pvmTransition.getTargetRef());

                // 不是后继节点
                if(!sameStartTimeNodes.contains(pvmActivityImpl)) continue;

                // 如果取出的线的目标节点存在时间相同的节点里，保存该线的id，进行高亮显示
                highFlows.add(pvmTransition.getId());
            }
        }

        //返回高亮的线
        return highFlows;
    }



    /**
     * 获取下一个节点信息
     * @param bpmnModel 流程模型
     * @param historicActivityInstances 历史节点
     * @param i 当前已经遍历到的历史节点索引（找下一个节点从此节点后）
     * @param activityImpl_ 当前遍历到的历史节点实例
     * @return FlowNode 下一个节点信息
     * @version 1.0
     */
    private static FlowNode getNextFlowNode(BpmnModel bpmnModel, List<HistoricActivityInstance> historicActivityInstances, int i, HistoricActivityInstance activityImpl_) {
        // 保存后一个节点
        FlowNode sameActivityImpl = null;

        // 如果当前节点不是用户任务节点，则取排序的下一个节点为后续节点
        if(!"userTask".equals(activityImpl_.getActivityType())) {
            // 是最后一个节点，没有下一个节点
            if(i == historicActivityInstances.size()) return sameActivityImpl;
            // 不是最后一个节点，取下一个节点为后继节点
            sameActivityImpl = (FlowNode) bpmnModel.getMainProcess().getFlowElement(historicActivityInstances.get(i + 1).getActivityId());// 找到紧跟在后面的一个节点
            // 返回
            return sameActivityImpl;
        }

        // 遍历后续节点，获取当前节点后续节点
        for (int k = i + 1; k <= historicActivityInstances.size() - 1; k++) {
            // 后续节点
            HistoricActivityInstance activityImp2_ = historicActivityInstances.get(k);
            // 都是userTask，且主节点与后续节点的开始时间相同，说明不是真实的后继节点
            if("userTask".equals(activityImp2_.getActivityType()) && activityImpl_.getStartTime().getTime() == activityImp2_.getStartTime().getTime()) continue;
            // 找到紧跟在后面的一个节点
            sameActivityImpl = (FlowNode) bpmnModel.getMainProcess().getFlowElement(historicActivityInstances.get(k).getActivityId());
            break;
        }
        return sameActivityImpl;
    }

    private static List<FlowNode> getNextFlowNodes(BpmnModel bpmnModel, List<HistoricActivityInstance> historicActivityInstances, int i, HistoricActivityInstance activityImpl_) {
        // 保存后一个节点
        FlowNode sameActivityImpl = null;
        Set<FlowNode> sameActivityImpls = new HashSet<>();
        //所有执行过的节点Id
        ArrayList<String> historicActivityIds = new ArrayList<>();
        for (HistoricActivityInstance h: historicActivityInstances) {
            historicActivityIds.add(h.getActivityId());
        }

        // 如果当前节点不是用户任务节点，则取排序的下一个节点为后续节点
        if(!"userTask".equals(activityImpl_.getActivityType())) {
            // 是最后一个节点，没有下一个节点
            if(i == historicActivityInstances.size()) return new ArrayList<>(sameActivityImpls);
            // 不是最后一个节点，取下一个节点为后继节点
            sameActivityImpl = (FlowNode) bpmnModel.getMainProcess().getFlowElement(historicActivityInstances.get(i + 1).getActivityId());// 找到紧跟在后面的一个节点

            //是第一个节点，取下一个节点为后继节点
            if ("startEvent".equals(activityImpl_.getActivityType())) {
                sameActivityImpls.add(sameActivityImpl);
                return new ArrayList<>(sameActivityImpls);
            }
            if (null == sameActivityImpl) {
                return new ArrayList<>(sameActivityImpls);
            }
            List<SequenceFlow> incomingFlows = sameActivityImpl.getIncomingFlows();
            if (incomingFlows.size() ==0 || null == incomingFlows) {
                throw new RuntimeException("这里有问题");
            }
            for (SequenceFlow flowNode: incomingFlows) {
                FlowNode sourceFlowElement = (FlowNode)flowNode.getSourceFlowElement();
                List<SequenceFlow> outgoingFlows = sourceFlowElement.getOutgoingFlows();
                for (SequenceFlow out: outgoingFlows) {
                    sameActivityImpl = (FlowNode) bpmnModel.getMainProcess().getFlowElement(out.getTargetRef());
                    if (historicActivityIds.contains(sameActivityImpl.getId())) {
                        sameActivityImpls.add(sameActivityImpl);
                    }
                }
            }

            // 返回
            return new ArrayList<>(sameActivityImpls);
        }

        // 遍历后续节点，获取当前节点后续节点
        for (int k = i + 1; k <= historicActivityInstances.size() - 1; k++) {
            // 后续节点
            HistoricActivityInstance activityImp2_ = historicActivityInstances.get(k);
            // 都是userTask，且主节点与后续节点的开始时间相同(毫秒值相差5个单位级以下)，说明不是真实的后继节点
            if("userTask".equals(activityImp2_.getActivityType()) && Math.abs(activityImpl_.getStartTime().getTime() - activityImp2_.getStartTime().getTime())<=5) continue;
            // 找到紧跟在后面的一个节点
            sameActivityImpl = (FlowNode) bpmnModel.getMainProcess().getFlowElement(historicActivityInstances.get(k).getActivityId());
            sameActivityImpls.add(sameActivityImpl);
            break;
        }
        return new ArrayList<>(sameActivityImpls);
    }

    /**
     * 回溯当前节点的上个节点的所有参与运行的子节点集合
     * @param historicActivityIds 参与流程的节点Id
     * @param sameActivityImpl
     * @param bpmnModel
     * @return
     */
    private static Set<FlowNode> findSameLevelNodes(ArrayList<String> historicActivityIds, FlowNode sameActivityImpl, BpmnModel bpmnModel) {
        Set<FlowNode> results = new HashSet<>();
        List<SequenceFlow> incomingFlows = sameActivityImpl.getIncomingFlows();
        if (incomingFlows.size() ==0 || null == incomingFlows) {
            throw new RuntimeException("这里有问题");
        }
        for (SequenceFlow flowNode: incomingFlows) {
            FlowNode sourceFlowElement = (FlowNode)flowNode.getSourceFlowElement();
            List<SequenceFlow> outgoingFlows = sourceFlowElement.getOutgoingFlows();
            for (SequenceFlow out: outgoingFlows) {
                sameActivityImpl = (FlowNode) bpmnModel.getMainProcess().getFlowElement(out.getTargetRef());
                if (historicActivityIds.contains(sameActivityImpl.getId())) {
                    results.add(sameActivityImpl);
                }
            }
        }
        return results;
    }

}
