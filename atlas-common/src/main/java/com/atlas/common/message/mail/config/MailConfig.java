package com.atlas.common.message.mail.config;

import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

/**
 * 邮件配置 — JavaMailSender Bean 定义 / Mail configuration — JavaMailSender Bean definition
 *
 * <p>基于 application.yml 中 mail.* 配置项，构建 Spring JavaMailSender 实例 /
 * Based on mail.* configuration items in application.yml, builds Spring JavaMailSender instance</p>
 *
 * @author Atlas Team
 * @since 1.2.10
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "mail")
@ConditionalOnProperty(prefix = "mail", name = "enabled", havingValue = "true", matchIfMissing = false)
public class MailConfig {

    /** SMTP 服务器地址 / SMTP server host */
    private String host;

    /** SMTP 端口 / SMTP port */
    private int port;

    /** 发件人用户名 / Sender username */
    private String username;

    /** 发件人密码 / Sender password */
    private String password;

    /** 协议: smtp / smtps / Protocol: smtp / smtps */
    private String protocol;

    /** 发件人显示名称 / Sender display name */
    private String from;

    /** 额外 JavaMail 属性 / Additional JavaMail properties */
    private Properties properties = new Properties();

    /**
     * 创建 JavaMailSender Bean / Create JavaMailSender Bean
     */
    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(host);
        mailSender.setPort(port);
        mailSender.setUsername(username);
        mailSender.setPassword(password);
        mailSender.setProtocol(protocol != null ? protocol : "smtp");
        mailSender.setDefaultEncoding("UTF-8");

        Properties props = mailSender.getJavaMailProperties();
        props.putAll(this.properties);

        // 默认开启认证和 SSL / Enable auth and SSL by default
        props.putIfAbsent("mail.smtp.auth", "true");
        if ("smtps".equalsIgnoreCase(protocol)) {
            props.putIfAbsent("mail.smtp.ssl.enable", "true");
        }

        return mailSender;
    }
}
