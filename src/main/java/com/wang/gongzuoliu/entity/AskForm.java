package com.wang.gongzuoliu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AskForm implements Serializable {

//    private static final long serialVersionUID = 1L;

    //请假单ID
    @TableId(value="leave_id", type=IdType.AUTO)
    private Integer leaveId;
    //工号
    private String jobNumber;
    //请假天数
    private Integer leaveDays;
    //请假原因
    private String leaveReason;
    //请假类型
    private String leaveType;
    //请假人
    private String applyer;
    //审批人
    private String approver;
    //请假状态
    private Integer formStatus;
    //确认状态
    private boolean comfirmStatus = false; //默认值
    //已确认数量
    private Integer comfirmeds;
    //带确认数量
    private Integer needComfirms;

}
