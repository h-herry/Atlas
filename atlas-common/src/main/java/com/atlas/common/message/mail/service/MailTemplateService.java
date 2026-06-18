package com.atlas.common.message.mail.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.File;
import java.util.Map;

/**
 * 邮件模板渲染 Service — 基于 Thymeleaf 模板引擎渲染 HTML 邮件 /
 * Mail template rendering Service — renders HTML mail based on Thymeleaf template engine
 *
 * <p>模板文件放置于 src/main/resources/templates/mail/ 目录下，后缀 .html /
 * Template files are placed under src/main/resources/templates/mail/ directory, with .html extension</p>
 *
 * @author Atlas Team
 * @since 1.2.10
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "mail", name = "enabled", havingValue = "true", matchIfMissing = false)
public class MailTemplateService {

    private final TemplateEngine templateEngine;

    /**
     * 渲染 HTML 邮件内容 / Render HTML mail content
     *
     * @param templateName 模板名称（不含 .html 后缀）/ Template name (without .html extension)
     * @param variables    模板变量 / Template variables
     * @return 渲染后的 HTML 字符串 / Rendered HTML string
     */
    public String render(String templateName, Map<String, Object> variables) {
        Context context = new Context();
        if (variables != null) {
            context.setVariables(variables);
        }
        String html = templateEngine.process("mail/" + templateName, context);
        log.debug("邮件模板渲染完成: template={}, varCount={}", templateName,
                variables != null ? variables.size() : 0);
        return html;
    }
}
