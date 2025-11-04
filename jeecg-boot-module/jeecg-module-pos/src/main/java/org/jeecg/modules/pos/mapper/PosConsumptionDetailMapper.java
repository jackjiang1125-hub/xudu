package org.jeecg.modules.pos.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.jeecg.modules.pos.entity.PosConsumptionDetail;

import java.util.List;

/**
 * 消费记录明细Mapper
 */
@Mapper
public interface PosConsumptionDetailMapper extends BaseMapper<PosConsumptionDetail> {

    /**
     * 根据消费记录ID查询明细
     */
    @Select("SELECT id, record_id, sku_code, product_name, unit_price, quantity, total_amount, " +
            "create_by, create_time, update_by, update_time " +
            "FROM pos_consumption_detail " +
            "WHERE record_id = #{recordId} " +
            "ORDER BY create_time ASC")
    List<PosConsumptionDetail> selectByRecordId(@Param("recordId") String recordId);

    /**
     * 根据消费记录ID批量删除明细
     */
    @Delete("DELETE FROM pos_consumption_detail WHERE record_id IN (${recordIds})")
    void deleteByRecordIds(@Param("recordIds") String recordIds);
}