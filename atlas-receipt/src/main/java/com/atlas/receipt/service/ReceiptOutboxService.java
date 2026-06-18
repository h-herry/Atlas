package com.atlas.receipt.service;

import com.atlas.common.mq.producer.AbstractMessageProducer;
import com.atlas.receipt.entity.ReceiptOutbox;
import com.atlas.receipt.mapper.ReceiptOutboxMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 本地消息表补偿服务 — 定时扫描未发送消息并重试 /
 * Local message table compensation service — periodically scans unsent messages and retries
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReceiptOutboxService {

    private final ReceiptOutboxMapper outboxMapper;
    private final ReceiptService.ReceiptConfirmProducer receiptConfirmProducer;

    private static final int MAX_RETRY = 5;

    /**
     * 写入本地消息表记录（独立事务，不受外部事务回滚影响） /
     * Write outbox record (independent transaction, not affected by external rollback)
     * <p>
     * 在收货确认事务中先通过此方法持久化 outbox，再发送 MQ。
     * 若 MQ 发送失败导致外部事务回滚，outbox 记录仍保留，
     * 由 {@link #retryPendingMessages()} 定时补偿重试。 /
     * Persist outbox first before sending MQ in confirm transaction.
     * If MQ send fails and triggers external rollback, the outbox record remains
     * for scheduled retry via {@link #retryPendingMessages()}.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void saveOutbox(ReceiptOutbox outbox) {
        outboxMapper.insert(outbox);
    }

    /**
     * 每分钟扫描一次，重发 status=0 且到达重试时间的消息 /
     * Scan every minute, resend messages with status=0 that have reached retry time
     */
    @Scheduled(cron = "0 * * * * ?")
    public void retryPendingMessages() {
        List<ReceiptOutbox> pendingList = outboxMapper.selectPage(
                new Page<>(0, 100),
                new LambdaQueryWrapper<ReceiptOutbox>()
                        .eq(ReceiptOutbox::getStatus, 0)
                        .le(ReceiptOutbox::getNextRetryTime, LocalDateTime.now())
        ).getRecords();

        for (ReceiptOutbox outbox : pendingList) {
            try {
                receiptConfirmProducer.sendAsync(String.valueOf(outbox.getReceiptId()), outbox.getMessageBody());
                outbox.setStatus(1);
                log.info("消息补发成功: outboxId={} receiptId={}", outbox.getId(), outbox.getReceiptId());
            } catch (Exception e) {
                int newRetryCount = outbox.getRetryCount() + 1;
                outbox.setRetryCount(newRetryCount);
                outbox.setErrorMsg(e.getMessage());
                if (newRetryCount >= MAX_RETRY) {
                    outbox.setStatus(2);
                    log.error("消息补发失败已达上限: outboxId={} receiptId={} retry={}",
                            outbox.getId(), outbox.getReceiptId(), newRetryCount, e);
                } else {
                    outbox.setNextRetryTime(LocalDateTime.now().plusMinutes(1));
                    log.warn("消息补发失败将重试: outboxId={} receiptId={} retry={}",
                            outbox.getId(), outbox.getReceiptId(), newRetryCount);
                }
            }
            outboxMapper.updateById(outbox);
        }
    }
}
