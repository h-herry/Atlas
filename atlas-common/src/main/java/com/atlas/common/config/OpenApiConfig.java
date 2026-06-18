package com.atlas.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI 3 文档配置 / OpenAPI 3 documentation configuration
 *
 * <p>接入 Knife4j (springdoc-openapi)，自动生成所有 Controller 的 API 文档。 /
 * Integrates Knife4j (springdoc-openapi) to auto-generate API docs for all Controllers.</p>
 *
 * <p>Swagger UI 访问路径: /doc.html / Swagger UI path: /doc.html</p>
 *
 * <p>扫描分组 / Scan groups:</p>
 * <ul>
 *   <li>atlas-supplier — 供应商管理 / Supplier management</li>
 *   <li>atlas-purchase — 采购管理 / Purchase management</li>
 *   <li>atlas-contract — 合同管理 / Contract management</li>
 *   <li>atlas-inventory — 库存管理 / Inventory management</li>
 *   <li>atlas-user — 用户管理 / User management</li>
 * </ul>
 *
 * @author Atlas Team
 * @since 1.2.21
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI atlasOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Atlas 企业采购管理系统 API / Atlas Enterprise Procurement API")
                        .description("基于 Spring Boot 3.2 + Spring Cloud 2023 的企业采购管理系统 API 文档 / "
                                   + "Enterprise procurement management system API documentation")
                        .version("1.2.21")
                        .contact(new Contact()
                                .name("Atlas Team")
                                .email("atlas@example.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}
