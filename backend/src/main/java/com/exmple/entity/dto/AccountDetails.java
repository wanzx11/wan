package com.exmple.entity.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@TableName("account_details")
@NoArgsConstructor
public class AccountDetails {
    @TableId
    Integer id;
    int gender;
    String phone;
    String qq;
    String wx;

    String description;
}
