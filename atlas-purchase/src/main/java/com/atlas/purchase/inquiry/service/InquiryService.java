package com.atlas.purchase.inquiry.service;

import com.atlas.common.core.enums.ErrorCode;
import com.atlas.common.core.exception.BizException;
import com.atlas.purchase.inquiry.entity.Inquiry;
import com.atlas.purchase.inquiry.entity.InquiryItem;
import com.atlas.purchase.inquiry.mapper.InquiryMapper;
import com.atlas.purchase.inquiry.mapper.InquiryItemMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 询价单业务服务 / Inquiry business service
 *
 * <p>管理询价单全生命周期：创建、编辑、发布、关闭。 /
 * Manages full inquiry lifecycle: create, edit, publish, close.</p>
 *
 * @author Atlas Team
 * @since 2.1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InquiryService extends ServiceImpl<InquiryMapper, Inquiry> {

    private final InquiryMapper inquiryMapper;
    private final InquiryItemMapper itemMapper;

    /**
     * 创建询价单 / Create inquiry
     */
    @Transactional(rollbackFor = Exception.class)
    public Inquiry create(Inquiry inquiry) {
        inquiry.setStatus("DRAFT");
        save(inquiry);
        log.info("询价单创建: id={} title={}", inquiry.getId(), inquiry.getTitle());
        return inquiry;
    }

    /**
     * 编辑询价单（仅草稿状态可编辑） / Edit inquiry (draft only)
     */
    @Transactional(rollbackFor = Exception.class)
    public Inquiry update(Long id, Inquiry updated) {
        Inquiry existing = getById(id);
        if (existing == null) {
            throw new BizException(ErrorCode.NOT_FOUND);
        }
        if (!"DRAFT".equals(existing.getStatus())) {
            throw new BizException(ErrorCode.ILLEGAL_STATE, "非草稿状态不可编辑 / Only draft can be edited");
        }
        updated.setId(id);
        updateById(updated);
        log.info("询价单编辑: id={}", id);
        return getById(id);
    }

    /**
     * 发布询价单 / Publish inquiry
     */
    @Transactional(rollbackFor = Exception.class)
    public Inquiry publish(Long id) {
        Inquiry inquiry = getById(id);
        if (inquiry == null) {
            throw new BizException(ErrorCode.NOT_FOUND);
        }
        if (!"DRAFT".equals(inquiry.getStatus())) {
            throw new BizException(ErrorCode.ILLEGAL_STATE, "仅草稿状态可发布 / Only draft can be published");
        }
        inquiry.setStatus("PUBLISHED");
        inquiry.setPublishTime(LocalDateTime.now());
        updateById(inquiry);
        log.info("询价单发布: id={}", id);
        return inquiry;
    }

    /**
     * 关闭询价单 / Close inquiry
     */
    @Transactional(rollbackFor = Exception.class)
    public void close(Long id) {
        Inquiry inquiry = getById(id);
        if (inquiry == null) {
            throw new BizException(ErrorCode.NOT_FOUND);
        }
        if ("CLOSED".equals(inquiry.getStatus())) {
            throw new BizException(ErrorCode.ILLEGAL_STATE, "询价单已关闭 / Inquiry already closed");
        }
        inquiry.setStatus("CLOSED");
        inquiry.setCloseTime(LocalDateTime.now());
        updateById(inquiry);
        log.info("询价单关闭: id={}", id);
    }

    /**
     * 询价单详情 / Inquiry detail
     */
    public Inquiry detail(Long id) {
        Inquiry inquiry = getById(id);
        if (inquiry == null) {
            throw new BizException(ErrorCode.NOT_FOUND);
        }
        return inquiry;
    }

    /**
     * 分页列表 / Paginated list
     */
    public Page<Inquiry> page(String status, int page, int size) {
        LambdaQueryWrapper<Inquiry> wrapper = new LambdaQueryWrapper<>();
        if (status != null && !status.isEmpty()) {
            wrapper.eq(Inquiry::getStatus, status);
        }
        wrapper.orderByDesc(Inquiry::getCreatedAt);
        return inquiryMapper.selectPage(new Page<>(page, size), wrapper);
    }

    /**
     * 添加询价行项目 / Add inquiry item
     */
    @Transactional(rollbackFor = Exception.class)
    public InquiryItem addItem(Long inquiryId, InquiryItem item) {
        Inquiry inquiry = getById(inquiryId);
        if (inquiry == null) {
            throw new BizException(ErrorCode.NOT_FOUND);
        }
        if (!"DRAFT".equals(inquiry.getStatus())) {
            throw new BizException(ErrorCode.ILLEGAL_STATE, "仅草稿状态可添加行项目 / Items can only be added in draft");
        }
        item.setInquiryId(inquiryId);
        itemMapper.insert(item);
        return item;
    }

    /**
     * 查询询价单所有行项目 / List all items of an inquiry
     */
    public List<InquiryItem> listItems(Long inquiryId) {
        return itemMapper.selectList(
            new LambdaQueryWrapper<InquiryItem>()
                .eq(InquiryItem::getInquiryId, inquiryId)
        );
    }
}
