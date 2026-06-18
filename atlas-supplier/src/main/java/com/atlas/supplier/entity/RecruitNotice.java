package com.atlas.supplier.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 招募公告实体 — 对应 recruit_notice 表 / Recruit notice entity — maps to recruit_notice table
 *
 * @author atlas
 */
@Data
@TableName("recruit_notice")
public class RecruitNotice {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 公告编号 / Notice number */
    private String noticeNo;

    /** 公告标题 / Notice title */
    private String title;

    /** 招募品类ID（JSON数组） / Category IDs (JSON array) */
    private String categoryIds;

    /** 公告内容/技术要求 / Content / technical requirements */
    private String content;

    /** 资质要求（JSON） / Qualification requirements (JSON) */
    private String qualificationRequirements;

    /** 发布时间 / Publish time */
    private LocalDateTime publishTime;

    /** 截止时间 / Deadline */
    private LocalDateTime deadline;

    /** 状态: 0草稿 1已发布 2已截止 3已关闭 / Status: 0=draft, 1=published, 2=closed, 3=archived */
    private Integer status;

    /** 发布人ID / Publisher ID */
    private Long publisherId;

    /** 发布人姓名 / Publisher name */
    private String publisherName;

    /** 创建时间 / Created time */
    private LocalDateTime createdAt;

    /** 更新时间 / Updated time */
    private LocalDateTime updatedAt;
}
