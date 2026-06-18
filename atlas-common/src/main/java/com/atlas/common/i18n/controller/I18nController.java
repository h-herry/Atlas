package com.atlas.common.i18n.controller;

import cn.hutool.core.collection.CollUtil;
import com.atlas.common.i18n.mapper.I18nLanguageMapper;
import com.atlas.common.i18n.mapper.I18nMessageMapper;
import com.atlas.common.i18n.service.I18nService;
import com.atlas.common.web.Result;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * I18n 管理 Controller — 提供语言列表查询、翻译增删改查、缓存刷新
 */
@Slf4j
@RestController
@RequestMapping("/api/i18n")
@RequiredArgsConstructor
public class I18nController {

    private final I18nService i18nService;
    private final I18nMessageMapper i18nMessageMapper;
    private final I18nLanguageMapper i18nLanguageMapper;

    // ==================== 语言管理 ====================

    /**
     * 获取所有启用的语言列表
     */
    @GetMapping("/languages")
    public Result<List<Map<String, Object>>> languages() {
        List<Map<String, Object>> languages = i18nMessageMapper.selectEnabledLanguages();
        return Result.ok(languages);
    }

    // ==================== 翻译 CRUD ====================

    /**
     * 查询单条翻译
     */
    @GetMapping("/messages/{key}")
    public Result<Map<String, String>> getMessage(@PathVariable String key,
                                                  @RequestParam(defaultValue = "zh-CN") String lang) {
        Map<String, String> data = i18nService.translateAll(null, Locale.forLanguageTag(lang));
        if (data.containsKey(key)) {
            data = Map.of(key, data.get(key));
        } else {
            data = Map.of();
        }
        return Result.ok(Map.copyOf(data));
    }

    /**
     * 按模块导出翻译
     */
    @GetMapping("/messages")
    public Result<Map<String, String>> exportMessages(@RequestParam String module,
                                                      @RequestParam(defaultValue = "zh-CN") String lang) {
        Map<String, String> messages = i18nService.translateAll(module, Locale.forLanguageTag(lang));
        return Result.ok(messages);
    }

    /**
     * 新增/更新单条翻译
     */
    @PostMapping("/messages")
    public Result<Void> saveMessage(@Valid @RequestBody I18nMessageRequest request) {
        i18nService.saveOrUpdate(request.getMessageKey(), request.getLanguageCode(), request.getMessageValue());
        log.info("翻译已保存: key={}, lang={}, value={}",
                request.getMessageKey(), request.getLanguageCode(), request.getMessageValue());
        return Result.ok();
    }

    /**
     * 批量导入翻译
     */
    @PostMapping("/messages/batch")
    public Result<Void> batchImport(@Valid @RequestBody List<I18nMessageRequest> messages) {
        if (CollUtil.isEmpty(messages)) {
            return Result.ok();
        }
        int count = 0;
        for (I18nMessageRequest msg : messages) {
            i18nService.saveOrUpdate(msg.getMessageKey(), msg.getLanguageCode(), msg.getMessageValue());
            count++;
        }
        log.info("批量导入翻译完成：{} 条", count);
        return Result.ok();
    }

    /**
     * 删除某消息键的所有语言翻译
     */
    @DeleteMapping("/messages/{key}")
    public Result<Void> deleteMessage(@PathVariable String key) {
        i18nService.deleteKey(key);
        log.info("删除翻译: key={}", key);
        return Result.ok();
    }

    /**
     * 刷新所有缓存
     */
    @PostMapping("/messages/refresh")
    public Result<Void> refreshCache() {
        i18nService.refreshCache();
        return Result.ok();
    }

    // ==================== DTO ====================

    @Data
    public static class I18nMessageRequest {
        @NotBlank(message = "消息键不能为空")
        private String messageKey;

        @NotBlank(message = "语言代码不能为空")
        private String languageCode;

        @NotBlank(message = "翻译文本不能为空")
        private String messageValue;

        private String module;
        private String description;
    }
}
