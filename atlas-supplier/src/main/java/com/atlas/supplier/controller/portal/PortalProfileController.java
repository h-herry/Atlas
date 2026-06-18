package com.atlas.supplier.controller.portal;

import com.atlas.common.security.annotation.RequirePermission;
import com.atlas.common.web.Result;
import com.atlas.supplier.entity.Supplier;
import com.atlas.supplier.entity.SupplierQualification;
import com.atlas.supplier.service.portal.PortalProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 供应商档案管理控制器（供应商端） — 查看/编辑自家信息、资质上传与管理 /
 * Supplier profile management controller (portal) — view/edit own info, qualification upload and management
 *
 * @author Atlas Team
 * @since 2.1.0
 */
@RestController
@RequestMapping("/portal/profile")
@RequiredArgsConstructor
public class PortalProfileController {

    private final PortalProfileService portalProfileService;

    /**
     * 查看自家企业信息（只能看自己的 supplier_id） / View own enterprise info (only own supplier_id)
     */
    @GetMapping
    @RequirePermission("supplier:portal:profile:view")
    public Result<Supplier> getProfile() {
        return Result.ok(portalProfileService.getProfile());
    }

    /**
     * 编辑企业信息（联系人、电话、地址等） / Edit enterprise info (contact, phone, address, etc.)
     */
    @PutMapping
    @RequirePermission("supplier:portal:profile:edit")
    public Result<Void> updateProfile(@Valid @RequestBody Supplier profile) {
        portalProfileService.updateProfile(profile);
        return Result.ok();
    }

    /**
     * 上传资质文件（营业执照、ISO 证书等） / Upload qualification file (business license, ISO certificate, etc.)
     */
    @PostMapping("/certificate")
    @RequirePermission("supplier:portal:certificate:upload")
    public Result<Void> uploadCertificate(@Valid @RequestBody SupplierQualification qualification) {
        portalProfileService.uploadCertificate(qualification);
        return Result.ok();
    }

    /**
     * 查看已上传资质列表 / View uploaded qualification list
     */
    @GetMapping("/certificates")
    @RequirePermission("supplier:portal:certificate:view")
    public Result<List<SupplierQualification>> getCertificates() {
        return Result.ok(portalProfileService.getCertificates());
    }

    /**
     * 即将过期的资质预警（30 天内到期） / Expiring qualification alerts (within 30 days)
     */
    @GetMapping("/certificates/expiring")
    @RequirePermission("supplier:portal:certificate:view")
    public Result<List<SupplierQualification>> getExpiringCertificates() {
        return Result.ok(portalProfileService.getExpiringCertificates());
    }
}
