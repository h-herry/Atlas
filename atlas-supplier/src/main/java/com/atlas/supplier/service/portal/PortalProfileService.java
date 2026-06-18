package com.atlas.supplier.service.portal;

import com.atlas.common.core.enums.ErrorCode;
import com.atlas.common.core.exception.BizException;
import com.atlas.supplier.config.SupplierSecurityConfig;
import com.atlas.supplier.entity.Supplier;
import com.atlas.supplier.entity.SupplierQualification;
import com.atlas.supplier.entity.SupplierRegister;
import com.atlas.supplier.mapper.SupplierMapper;
import com.atlas.supplier.mapper.SupplierQualificationMapper;
import com.atlas.supplier.mapper.SupplierRegisterMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * 供应商档案服务（供应商端） — 供应商查看/编辑自家信息、资质上传与管理 /
 * Supplier profile service (portal) — supplier views/edits own info, qualification upload and management
 *
 * <p>所有操作严格限定当前登录供应商的 supplier_id，确保数据隔离。 /
 * All operations strictly scoped to the current logged-in supplier's supplier_id for data isolation.</p>
 *
 * @author Atlas Team
 * @since 2.1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PortalProfileService {

    private final SupplierMapper supplierMapper;
    private final SupplierRegisterMapper supplierRegisterMapper;
    private final SupplierQualificationMapper qualificationMapper;

    /**
     * 查看自家企业信息 / View own enterprise information
     *
     * @return 供应商实体 / Supplier entity
     */
    public Supplier getProfile() {
        Long supplierId = SupplierSecurityConfig.getCurrentSupplierId();
        Supplier supplier = supplierMapper.selectById(supplierId);
        if (supplier == null) {
            throw new BizException(ErrorCode.USER_NOT_FOUND,
                    "供应商档案不存在 / Supplier profile not found");
        }
        return supplier;
    }

    /**
     * 编辑企业信息（联系人、电话、地址等） / Edit enterprise info (contact person, phone, address, etc.)
     *
     * @param profile 更新后的供应商信息 / Updated supplier info
     */
    public void updateProfile(Supplier profile) {
        Long supplierId = SupplierSecurityConfig.getCurrentSupplierId();

        // 确保只更新自己的档案 / Ensure only own profile is updated
        profile.setId(supplierId);
        // 不可修改的关键字段保护 / Protect immutable key fields
        profile.setSupplierNo(null);
        profile.setStatus(null);
        profile.setGrade(null);
        profile.setQualificationLevel(null);

        int rows = supplierMapper.updateById(profile);
        if (rows == 0) {
            throw new BizException(ErrorCode.UPDATE_FAILED,
                    "更新档案失败 / Profile update failed");
        }
        log.info("供应商档案已更新: supplierId={}", supplierId);
    }

    /**
     * 上传资质文件（营业执照、ISO 证书等） / Upload qualification file (business license, ISO certificate, etc.)
     *
     * @param qualification 资质信息 / Qualification info
     */
    public void uploadCertificate(SupplierQualification qualification) {
        Long supplierId = SupplierSecurityConfig.getCurrentSupplierId();
        qualification.setSupplierId(supplierId);

        int rows = qualificationMapper.insert(qualification);
        if (rows == 0) {
            throw new BizException(ErrorCode.INSERT_FAILED,
                    "上传资质失败 / Certificate upload failed");
        }
        log.info("供应商资质已上传: supplierId={}, qualType={}", supplierId, qualification.getQualType());
    }

    /**
     * 查看已上传资质列表 / View uploaded qualification list
     *
     * @return 资质列表 / Qualification list
     */
    public List<SupplierQualification> getCertificates() {
        Long supplierId = SupplierSecurityConfig.getCurrentSupplierId();
        return qualificationMapper.selectList(
                new LambdaQueryWrapper<SupplierQualification>()
                        .eq(SupplierQualification::getSupplierId, supplierId)
                        .orderByDesc(SupplierQualification::getCreatedAt)
        );
    }

    /**
     * 即将过期的资质预警（30 天内到期） / Expiring qualification alert (within 30 days)
     *
     * @return 即将过期的资质列表 / Expiring qualifications list
     */
    public List<SupplierQualification> getExpiringCertificates() {
        Long supplierId = SupplierSecurityConfig.getCurrentSupplierId();
        LocalDate threshold = LocalDate.now().plusDays(30);

        return qualificationMapper.selectList(
                new LambdaQueryWrapper<SupplierQualification>()
                        .eq(SupplierQualification::getSupplierId, supplierId)
                        .le(SupplierQualification::getExpireDate, threshold)
                        .ge(SupplierQualification::getExpireDate, LocalDate.now())
                        .orderByAsc(SupplierQualification::getExpireDate)
        );
    }
}
