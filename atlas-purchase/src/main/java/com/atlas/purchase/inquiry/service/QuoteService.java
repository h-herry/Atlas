package com.atlas.purchase.inquiry.service;

import com.atlas.common.core.enums.ErrorCode;
import com.atlas.common.core.exception.BizException;
import com.atlas.purchase.inquiry.entity.Inquiry;
import com.atlas.purchase.inquiry.entity.Quote;
import com.atlas.purchase.inquiry.entity.QuoteItem;
import com.atlas.purchase.inquiry.mapper.QuoteMapper;
import com.atlas.purchase.inquiry.mapper.QuoteItemMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 报价业务服务 / Quote business service
 *
 * <p>供应商报价的提交、修改、查看。 /
 * Supplier quote submission, modification, and query.</p>
 *
 * @author Atlas Team
 * @since 2.1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QuoteService extends ServiceImpl<QuoteMapper, Quote> {

    private final QuoteMapper quoteMapper;
    private final QuoteItemMapper quoteItemMapper;
    private final InquiryService inquiryService;

    /**
     * 供应商提交报价 / Supplier submits quote
     */
    @Transactional(rollbackFor = Exception.class)
    public Quote submit(Quote quote, List<QuoteItem> items) {
        Inquiry inquiry = inquiryService.getById(quote.getInquiryId());
        if (inquiry == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "询价单不存在 / Inquiry not found");
        }
        if (!"PUBLISHED".equals(inquiry.getStatus()) && !"QUOTING".equals(inquiry.getStatus())) {
            throw new BizException(ErrorCode.ILLEGAL_STATE, "当前询价状态不可报价 / Inquiry status does not allow quoting");
        }

        // 计算总金额 / Calculate total amount
        BigDecimal total = items.stream()
            .map(i -> i.getUnitPrice().multiply(
                BigDecimal.valueOf(i.getDeliveryDays() > 0 ? i.getDeliveryDays() : 1)))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        quote.setTotalAmount(total);
        quote.setStatus("SUBMITTED");
        quote.setSubmitTime(LocalDateTime.now());
        save(quote);

        // 保存报价明细 / Save quote items
        for (QuoteItem item : items) {
            item.setQuoteId(quote.getId());
            quoteItemMapper.insert(item);
        }

        log.info("报价提交: inquiryId={} supplierId={} totalAmount={}", quote.getInquiryId(), quote.getSupplierId(), total);
        return quote;
    }

    /**
     * 修改报价 / Update quote
     */
    @Transactional(rollbackFor = Exception.class)
    public Quote updateQuote(Long quoteId, Quote updated, List<QuoteItem> items) {
        Quote existing = getById(quoteId);
        if (existing == null) {
            throw new BizException(ErrorCode.NOT_FOUND);
        }
        if (!"SUBMITTED".equals(existing.getStatus())) {
            throw new BizException(ErrorCode.ILLEGAL_STATE, "仅已提交报价可修改 / Only submitted quotes can be modified");
        }

        BigDecimal total = items.stream()
            .map(i -> i.getUnitPrice().multiply(
                BigDecimal.valueOf(i.getDeliveryDays() > 0 ? i.getDeliveryDays() : 1)))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        existing.setTotalAmount(total);
        existing.setSubmitTime(LocalDateTime.now());
        updateById(existing);

        // 删除旧明细，写入新明细 / Delete old items, insert new ones
        quoteItemMapper.delete(new LambdaQueryWrapper<QuoteItem>().eq(QuoteItem::getQuoteId, quoteId));
        for (QuoteItem item : items) {
            item.setQuoteId(quoteId);
            quoteItemMapper.insert(item);
        }

        log.info("报价修改: quoteId={}", quoteId);
        return existing;
    }

    /**
     * 查询某询价单下所有报价 / List all quotes for an inquiry
     */
    public List<Quote> listByInquiry(Long inquiryId) {
        return quoteMapper.selectList(
            new LambdaQueryWrapper<Quote>()
                .eq(Quote::getInquiryId, inquiryId)
                .orderByDesc(Quote::getSubmitTime)
        );
    }

    /**
     * 查询报价明细 / List quote items
     */
    public List<QuoteItem> listItems(Long quoteId) {
        return quoteItemMapper.selectList(
            new LambdaQueryWrapper<QuoteItem>()
                .eq(QuoteItem::getQuoteId, quoteId)
        );
    }

    /**
     * 撤回报价 / Withdraw quote
     */
    @Transactional(rollbackFor = Exception.class)
    public void withdraw(Long quoteId) {
        Quote quote = getById(quoteId);
        if (quote == null) {
            throw new BizException(ErrorCode.NOT_FOUND);
        }
        if (!"SUBMITTED".equals(quote.getStatus())) {
            throw new BizException(ErrorCode.ILLEGAL_STATE, "仅已提交报价可撤回 / Only submitted quotes can be withdrawn");
        }
        quote.setStatus("WITHDRAWN");
        updateById(quote);
        log.info("报价撤回: quoteId={}", quoteId);
    }
}
