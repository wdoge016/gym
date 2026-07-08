package com.gym.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gym.entity.WaitList;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface WaitListMapper extends BaseMapper<WaitList> {

    /**
     * 查询某个排课最早的候补记录
     */
    @Select("SELECT * FROM wait_list WHERE schedule_id = #{scheduleId} AND status = 'WAITING' AND deleted = 0 " +
            "ORDER BY create_time ASC LIMIT 1")
    WaitList selectEarliestWaiting(@Param("scheduleId") Long scheduleId);
}
