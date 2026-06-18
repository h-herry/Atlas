package com.atlas.supplier.dto.portal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 供应商 Token 响应 DTO / Supplier token response DTO
 *
 * @author Atlas Team
 * @since 2.1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplierTokenResponse {

    /** JWT 访问令牌 / JWT access token */
    private String accessToken;

    /** 令牌类型 / Token type */
    private String tokenType;

    /** 过期时间（秒） / Expiration time (seconds) */
    private Long expiresIn;

    /** 供应商ID / Supplier ID */
    private Long supplierId;

    /** 供应商名称 / Supplier name */
    private String supplierName;

    /** 刷新令牌 / Refresh token */
    private String refreshToken;
}
