package com.flashsalex.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.flashsalex.entity.SeckillStock;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface SeckillStockMapper extends BaseMapper<SeckillStock> {

    /**
     * 乐观锁扣减库存（数据库兜底）
     */
    @Update("UPDATE t_seckill_stock SET available_stock = available_stock - #{quantity}, " +
            "sold_stock = sold_stock + #{quantity}, version = version + 1, updated_at = NOW() " +
            "WHERE activity_id = #{activityId} AND available_stock >= #{quantity}")
    int deductStock(@Param("activityId") Long activityId, @Param("quantity") Integer quantity);

    /**
     * 回补库存
     */
    @Update("UPDATE t_seckill_stock SET available_stock = available_stock + #{quantity}, " +
            "sold_stock = sold_stock - #{quantity}, version = version + 1, updated_at = NOW() " +
            "WHERE activity_id = #{activityId} AND sold_stock >= #{quantity}")
    int restoreStock(@Param("activityId") Long activityId, @Param("quantity") Integer quantity);
}
