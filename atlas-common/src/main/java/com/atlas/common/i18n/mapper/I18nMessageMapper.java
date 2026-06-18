package com.atlas.common.i18n.mapper;

import com.atlas.common.i18n.entity.I18nMessage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 翻译消息 Mapper
 */
@Mapper
public interface I18nMessageMapper extends BaseMapper<I18nMessage> {

    /**
     * 按语言代码和消息键列表批量查询
     */
    @Select("<script>" +
            "SELECT message_key, message_value FROM i18n_message " +
            "WHERE language_code = #{languageCode} " +
            "AND message_key IN " +
            "<foreach collection='keys' item='key' open='(' separator=',' close=')'>#{key}</foreach>" +
            "</script>")
    List<Map<String, String>> selectByKeys(@Param("languageCode") String languageCode,
                                            @Param("keys") List<String> keys);

    /**
     * 按模块和语言查询所有翻译
     */
    @Select("SELECT message_key, message_value FROM i18n_message " +
            "WHERE module = #{module} AND language_code = #{languageCode}")
    List<Map<String, String>> selectByModule(@Param("module") String module,
                                              @Param("languageCode") String languageCode);

    /**
     * 查询所有启用的语言
     */
    @Select("SELECT code, name, native_name, sort_order FROM i18n_language " +
            "WHERE enabled = 1 ORDER BY sort_order ASC")
    List<Map<String, Object>> selectEnabledLanguages();
}
