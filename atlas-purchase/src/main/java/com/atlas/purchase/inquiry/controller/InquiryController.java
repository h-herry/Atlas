package com.atlas.purchase.inquiry.controller;

import com.atlas.common.security.annotation.RequirePermission;
import com.atlas.common.web.Result;
import com.atlas.purchase.inquiry.entity.Inquiry;
import com.atlas.purchase.inquiry.entity.InquiryItem;
import com.atlas.purchase.inquiry.service.InquiryService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 询价单 REST API / Inquiry REST API
 *
 * @author Atlas Team
 * @since 2.1.0
 */
@RestController
@RequestMapping("/api/inquiry")
@RequiredArgsConstructor
@Tag(name = "询价单管理 / Inquiry Management")
public class InquiryController {

    private final InquiryService inquiryService;

    /**
     * 创建询价单 / Create inquiry
     */
    @PostMapping
    @RequirePermission("purchase:add")
    public Result<Inquiry> create(@RequestBody Inquiry inquiry) {
        return Result.ok(inquiryService.create(inquiry));
    }

    /**
     * 编辑询价单 / Edit inquiry
     */
    @PutMapping("/{id}")
    @RequirePermission("purchase:edit")
    public Result<Inquiry> update(@PathVariable Long id, @RequestBody Inquiry inquiry) {
        return Result.ok(inquiryService.update(id, inquiry));
    }

    /**
     * 发布询价单 / Publish inquiry
     */
    @PostMapping("/{id}/publish")
    @RequirePermission("purchase:edit")
    public Result<Inquiry> publish(@PathVariable Long id) {
        return Result.ok(inquiryService.publish(id));
    }

    /**
     * 询价单详情 / Inquiry detail
     */
    @GetMapping("/{id}")
    @RequirePermission("purchase:view")
    public Result<Inquiry> detail(@PathVariable Long id) {
        return Result.ok(inquiryService.detail(id));
    }

    /**
     * 询价单列表 / Inquiry list
     */
    @GetMapping("/list")
    @RequirePermission("purchase:view")
    public Result<Page<Inquiry>> list(@RequestParam(required = false) String status,
                                       @RequestParam(defaultValue = "1") int page,
                                       @RequestParam(defaultValue = "10") int size) {
        return Result.ok(inquiryService.page(status, page, size));
    }

    /**
     * 关闭询价单 / Close inquiry
     */
    @PutMapping("/{id}/close")
    @RequirePermission("purchase:edit")
    public Result<Void> close(@PathVariable Long id) {
        inquiryService.close(id);
        return Result.ok();
    }

    /**
     * 添加询价行项目 / Add inquiry item
     */
    @PostMapping("/{inquiryId}/items")
    @RequirePermission("purchase:add")
    public Result<InquiryItem> addItem(@PathVariable Long inquiryId, @RequestBody InquiryItem item) {
        return Result.ok(inquiryService.addItem(inquiryId, item));
    }

    /**
     * 查询询价单行项目 / List inquiry items
     */
    @GetMapping("/{inquiryId}/items")
    @RequirePermission("purchase:view")
    public Result<List<InquiryItem>> listItems(@PathVariable Long inquiryId) {
        return Result.ok(inquiryService.listItems(inquiryId));
    }
}
