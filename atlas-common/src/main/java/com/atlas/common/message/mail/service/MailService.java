package com.atlas.common.message.mail.service;

import com.atlas.common.message.mail.config.MailConfig;
import com.atlas.common.message.mail.dto.MailSendRequest;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * 邮件发送 Service — 支持简单文本 / HTML模板 / 带附件 / 批量发送 /
 * Mail sending Service — supports plain text / HTML template / with attachment / batch sending
 *
 * @author Atlas Team
 * @since 1.2.10
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "mail", name = "enabled", havingValue = "true", matchIfMissing = false)
public class MailService {

    private final JavaMailSender mailSender;
    private final MailTemplateService templateService;
    private final MailConfig mailConfig;

    /**
     * 发送简单文本邮件 / Send simple plain text mail
     *
     * @param to      收件人 / Recipient
     * @param subject 主题 / Subject
     * @param content 正文 / Content
     */
    public void sendSimpleMail(String to, String subject, String content) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(mailConfig.getFrom());
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);
            mailSender.send(message);
            log.info("简单文本邮件发送成功: to={}, subject={}", to, subject);
        } catch (Exception e) {
            log.error("简单文本邮件发送失败: to={}, subject={}", to, subject, e);
            throw new RuntimeException("邮件发送失败 / Mail sending failed: " + e.getMessage(), e);
        }
    }

    /**
     * 发送 HTML 模板邮件（支持变量替换）/ Send HTML template mail (supports variable substitution)
     *
     * @param to           收件人 / Recipient
     * @param subject      主题 / Subject
     * @param templateName 模板名称（不含 .html 后缀）/ Template name (without .html extension)
     * @param variables    模板变量 / Template variables
     */
    public void sendTemplateMail(String to, String subject, String templateName, Map<String, Object> variables) {
        try {
            String htmlContent = templateService.render(templateName, variables);

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(mailConfig.getFrom());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);
            log.info("HTML模板邮件发送成功: to={}, subject={}, template={}", to, subject, templateName);
        } catch (MessagingException e) {
            log.error("HTML模板邮件发送失败: to={}, subject={}, template={}", to, subject, templateName, e);
            throw new RuntimeException("邮件发送失败 / Mail sending failed: " + e.getMessage(), e);
        }
    }

    /**
     * 批量发送 HTML 模板邮件（异步） / Batch send HTML template mail (async)
     *
     * @param toList       收件人列表 / Recipient list
     * @param subject      主题 / Subject
     * @param templateName 模板名称 / Template name
     * @param variables    模板变量 / Template variables
     */
    @Async
    public void sendBatchMail(List<String> toList, String subject, String templateName,
                              Map<String, Object> variables) {
        if (toList == null || toList.isEmpty()) {
            log.warn("批量邮件发送跳过，收件人列表为空 / Batch mail skipped, recipient list is empty");
            return;
        }

        int successCount = 0;
        int failCount = 0;

        for (String to : toList) {
            try {
                sendTemplateMail(to, subject, templateName, variables);
                successCount++;
            } catch (Exception e) {
                log.error("批量邮件发送单条失败: to={}, subject={}", to, subject, e);
                failCount++;
            }
        }

        log.info("批量邮件发送完成: total={}, success={}, fail={}", toList.size(), successCount, failCount);
    }

    /**
     * 带附件发送邮件 / Send mail with attachments
     *
     * @param to          收件人 / Recipient
     * @param subject     主题 / Subject
     * @param content     正文（支持 HTML）/ Content (supports HTML)
     * @param attachments 附件列表 / Attachment list
     */
    public void sendMailWithAttachment(String to, String subject, String content, List<File> attachments) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(mailConfig.getFrom());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);

            if (attachments != null) {
                for (File attachment : attachments) {
                    if (attachment != null && attachment.exists()) {
                        helper.addAttachment(attachment.getName(), attachment);
                    }
                }
            }

            mailSender.send(mimeMessage);
            log.info("带附件邮件发送成功: to={}, subject={}, attachments={}",
                    to, subject, attachments != null ? attachments.size() : 0);
        } catch (MessagingException e) {
            log.error("带附件邮件发送失败: to={}, subject={}", to, subject, e);
            throw new RuntimeException("邮件发送失败 / Mail sending failed: " + e.getMessage(), e);
        }
    }
}
