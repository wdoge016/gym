package com.gym.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gym.entity.Schedule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ScheduleMapper extends BaseMapper<Schedule> {

    /**
     * 乐观锁扣减名额：booked_count + 1（仅当 booked_count < max_members 时生效）
     * @return 影响行数，0 表示已满员
     */
    @Update("UPDATE `schedule` SET booked_count = booked_count + 1 " +
            "WHERE id = #{id} AND booked_count < max_members AND deleted = 0")
    int incrementBookedCount(@Param("id") Long id);

    /**
     * 回退名额：booked_count - 1（仅当 booked_count > 0 时生效）
     */
    @Update("UPDATE `schedule` SET booked_count = booked_count - 1 " +
            "WHERE id = #{id} AND booked_count > 0 AND deleted = 0")
    int decrementBookedCount(@Param("id") Long id);

    /**
     * 逻辑删除所有已过期的排课（end_time < now）
     * @return 影响行数
     */
    @Update("UPDATE `schedule` SET deleted = 1, update_time = NOW() " +
            "WHERE end_time < NOW() AND deleted = 0")
    int deleteExpired();
}
