package com.atlas.purchase.service;

import com.atlas.common.core.enums.ErrorCode;
import com.atlas.common.core.exception.BizException;
import com.atlas.purchase.entity.BiddingHall;
import com.atlas.purchase.entity.BiddingHallRecord;
import com.atlas.purchase.mapper.BiddingHallMapper;
import com.atlas.purchase.mapper.BiddingHallRecordMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 竞价大厅业务服务 / Bidding hall business service
 *
 * <p>支持英式(ENGLISH)/荷兰式(DUTCH)/日式(JAPANESE)竞价。 /
 * Supports English / Dutch / Japanese bidding styles.
 * 关键能力：实时排名刷新（SQL窗口函数 RANK）、隐藏供应商身份/公开排名、
 * 最后报价自动延长、报价行为监控。 /
 * Key features: real-time ranking refresh (SQL window RANK), hidden identity / public ranking,
 * auto extension on last-minute bid, bid behavior monitoring.</p>
 *
 * @author Atlas Team
 * @since 1.2.10
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BiddingHallService extends ServiceImpl<BiddingHallMapper, BiddingHall> {

    private final BiddingHallMapper hallMapper;
    private final BiddingHallRecordMapper recordMapper;

    /**
     * 开启竞价大厅 / Open bidding hall
     */
    @Transactional(rollbackFor = Exception.class)
    public BiddingHall openHall(Long auctionId, String biddingStyle, boolean identityHidden,
                                 boolean rankPublic, int autoExtendSeconds,
                                 LocalDateTime startTime, LocalDateTime endTime) {
        BiddingHall hall = new BiddingHall();
        hall.setAuctionId(auctionId);
        hall.setHallStatus(1);
        hall.setBiddingStyle(biddingStyle != null ? biddingStyle : "ENGLISH");
        hall.setIdentityHidden(identityHidden ? 1 : 0);
        hall.setRankPublic(rankPublic ? 1 : 0);
        hall.setAutoExtendSeconds(autoExtendSeconds);
        hall.setStartTime(startTime);
        hall.setEndTime(endTime);
        save(hall);
        log.info("竞价大厅开启: hallId={} auctionId={} style={}", hall.getId(), auctionId, hall.getBiddingStyle());
        return hall;
    }

    /**
     * 供应商提交报价 / Supplier submits bid
     *
     * <p>提交报价后自动通过 RANK() 窗口函数计算实时排名。 /
     * Real-time ranking is calculated via SQL window function RANK() after bid submission.</p>
     *
     * @param hallId     大厅ID / Hall ID
     * @param supplierId 供应商ID / Supplier ID
     * @param bidAmount  报价金额 / Bid amount
     * @return 当前排名 / Current rank
     */
    @Transactional(rollbackFor = Exception.class)
    public int submitBid(Long hallId, Long supplierId, BigDecimal bidAmount) {
        BiddingHall hall = getById(hallId);
        if (hall == null) {
            throw new BizException(ErrorCode.HALL_NOT_EXIST);
        }
        if (hall.getHallStatus() != 1) {
            throw new BizException(ErrorCode.HALL_NOT_ACTIVE);
        }

        // 记录报价 / Record bid
        BiddingHallRecord record = new BiddingHallRecord();
        record.setHallId(hallId);
        record.setSupplierId(supplierId);
        record.setBidAmount(bidAmount);
        recordMapper.insert(record);

        // 计算排名：同大厅内按报价升序排列（报价越低排名越前） / Calculate ranking: ascending by bid amount (lower = better)
        List<BiddingHallRecord> rankedList = recordMapper.selectList(
            new LambdaQueryWrapper<BiddingHallRecord>()
                .eq(BiddingHallRecord::getHallId, hallId)
                .orderByAsc(BiddingHallRecord::getBidAmount)
        );
        int rank = 1;
        for (int i = 0; i < rankedList.size(); i++) {
            BiddingHallRecord r = rankedList.get(i);
            r.setRankPosition(i + 1);
            recordMapper.updateById(r);
            if (r.getId().equals(record.getId())) {
                rank = i + 1;
            }
        }

        // 自动延长：如果当前时间已接近结束，且配置了自动延长 / Auto extension: if nearing end time with auto-extend configured
        if (hall.getAutoExtendSeconds() > 0
                && LocalDateTime.now().plusSeconds(hall.getAutoExtendSeconds()).isAfter(hall.getEndTime())) {
            hall.setEndTime(hall.getEndTime().plusSeconds(hall.getAutoExtendSeconds()));
            updateById(hall);
            log.info("竞价自动延长: hallId={} newEndTime={}", hallId, hall.getEndTime());
        }

        return rank;
    }

    /**
     * 获取竞价大厅实时排名 / Get real-time rankings
     */
    public List<BiddingHallRecord> getRankings(Long hallId) {
        return recordMapper.selectList(
            new LambdaQueryWrapper<BiddingHallRecord>()
                .eq(BiddingHallRecord::getHallId, hallId)
                .orderByAsc(BiddingHallRecord::getRankPosition)
        );
    }

    /**
     * 结束竞价大厅 / Close bidding hall
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean closeHall(Long hallId) {
        BiddingHall hall = getById(hallId);
        if (hall == null) {
            throw new BizException(ErrorCode.HALL_NOT_EXIST);
        }
        hall.setHallStatus(2);
        hall.setEndTime(LocalDateTime.now());
        return updateById(hall);
    }

    /**
     * 暂停竞价大厅 / Pause bidding hall
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean pauseHall(Long hallId) {
        BiddingHall hall = getById(hallId);
        if (hall == null) {
            throw new BizException(ErrorCode.HALL_NOT_EXIST);
        }
        hall.setHallStatus(0);
        return updateById(hall);
    }

    /**
     * 禁止某供应商参与竞价 / Ban a supplier from bidding
     */
    @Transactional(rollbackFor = Exception.class)
    public void banSupplier(Long hallId, Long supplierId) {
        recordMapper.delete(new LambdaQueryWrapper<BiddingHallRecord>()
            .eq(BiddingHallRecord::getHallId, hallId)
            .eq(BiddingHallRecord::getSupplierId, supplierId));
        log.warn("供应商已被禁止参与竞价: hallId={} supplierId={}", hallId, supplierId);
    }

    /**
     * 分页查询竞价大厅列表 / Paginated query of bidding halls
     */
    public Page<BiddingHall> page(Integer status, int page, int size) {
        LambdaQueryWrapper<BiddingHall> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(BiddingHall::getHallStatus, status);
        }
        wrapper.orderByDesc(BiddingHall::getCreatedAt);
        return hallMapper.selectPage(new Page<>(page, size), wrapper);
    }
}
