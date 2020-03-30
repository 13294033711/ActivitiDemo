package com.wang.gongzuoliu.controller;


import com.wang.gongzuoliu.common.UserUtils;
import com.wang.gongzuoliu.entity.AskForm;
import com.wang.gongzuoliu.service.IAskFormService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 请假单 前端控制器
 * </p>
 *
 * @author wangchen
 * @since 2020-03-16
 */
@Controller
@RequestMapping("/askForm")
public class AskFormController {

    @Autowired
    private IAskFormService askFormService;

    /**
     * 查询当前用户的申请单列表
     * @return
     */
    @RequestMapping("list")
    public String list(Model model) {
        String currentUser = UserUtils.getCurrentPrincipal().getUsername();
        Map<String, Object> colsMap = new HashMap<>();
        colsMap.put("applyer", currentUser);
        model.addAttribute("askForms",new ArrayList<>(askFormService.listByMap(colsMap)));
        return "askFormList";
    }

    @RequestMapping("add")
    public String add(AskForm askForm) {
        if(askFormService.save(askForm)) {
            return "index";
        }else {
            return "error";
        }
    }

    @RequestMapping("update")
    public String update(AskForm askForm) {
        if(askFormService.updateById(askForm)) {
            return "index";
        }else {
            return "error";
        }
    }

    @RequestMapping("delete")
    public String delete(AskForm askForm) {
        if(askFormService.removeById(askForm.getLeaveId())) {
            return "index";
        }else {
            return "error";
        }
    }

    @RequestMapping("viewTaskDetail")
    @ResponseBody
    public Map<String, Object> viewTaskDetail(String leaveId) {
        Map<String, Object> paramMap = new HashMap<>();

        paramMap.put("askForm", askFormService.getById(leaveId));
        return paramMap;
    }
}
