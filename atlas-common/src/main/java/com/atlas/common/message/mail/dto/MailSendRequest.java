package com.atlas.common.message.mail.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * 邮件发送请求 DTO — 封装邮件发送所需全部参数 /
 * Mail send request DTO — encapsulates all parameters needed for mail sending
 *
 * @author Atlas Team
 * @since 1.2.10
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MailSendRequest {

    /** 收件人（单个）/ Recipient (single) */
    private String to;

    /** 收件人列表（批量）/ Recipient list (batch) */
    private List<String> toList;

    /** 抄送 / CC */
    private List<String> cc;

    /** 密送 / BCC */
    private List<String> bcc;

    /** 邮件主题 / Mail subject */
    private String subject;

    /** 纯文本内容 / Plain text content */
    private String content;

    /** HTML 模板名称（不含 .html 后缀）/ HTML template name (without .html extension) */
    private String templateName;

    /** 模板变量替换 / Template variable substitution */
    private Map<String, Object> variables;

    /** 附件列表 / Attachment list */
    private List<File> attachments;
}
