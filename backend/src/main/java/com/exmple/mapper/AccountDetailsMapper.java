package com.exmple.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.exmple.entity.dto.AccountDetails;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AccountDetailsMapper extends BaseMapper<AccountDetails> {
}
