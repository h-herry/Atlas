package com.atlas.message.mapper;

import com.atlas.message.model.Message;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 消息记录 Mapper / Message record Mapper
 *
 * @author Atlas Team
 * @since 1.2.10
 */
@Mapper
public interface MessageMapper extends BaseMapper<Message> {

    /**
     * 查询供应商未读消息列表 / Query supplier's unread messages
     */
    @Select("SELECT * FROM msg_record WHERE supplier_id = #{supplierId} AND is_read = 0 ORDER BY created_at DESC LIMIT #{limit}")
    List<Message> findUnreadBySupplier(@Param("supplierId") Long supplierId, @Param("limit") int limit);

    /**
     * 按类型统计未读数 / Count unread by type
     */
    @Select("SELECT type, COUNT(*) as cnt FROM msg_record " +
            "WHERE supplier_id = #{supplierId} AND is_read = 0 " +
            "GROUP BY type")
    List<java.util.Map<String, Object>> countUnreadByType(@Param("supplierId") Long supplierId);

    /**
     * 统计供应商未读总数 / Count total unread for supplier
     */
    @Select("SELECT COUNT(*) FROM msg_record WHERE supplier_id = #{supplierId} AND is_read = 0")
    Long countUnread(@Param("supplierId") Long supplierId);

    /**
     * 批量标记已读 / Batch mark as read
     */
    @Update("<script>" +
            "UPDATE msg_record SET is_read = 1, read_at = NOW() " +
            "WHERE supplier_id = #{supplierId} AND is_read = 0 " +
            "<if test='type != null'>AND type = #{type}</if>" +
            "</script>")
    int markAsRead(@Param("supplierId") Long supplierId, @Param("type") String type);
}
