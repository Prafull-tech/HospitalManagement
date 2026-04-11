package com.hospital.hms.system.entity;

import com.hospital.hms.common.entity.BaseIdEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "company_profile")
public class CompanyProfile extends BaseIdEntity {

    @Size(max = 255)
    @Column(name = "company_name", nullable = false, length = 255)
    private String companyName;

    @Size(max = 100)
    @Column(name = "brand_name", nullable = false, length = 100)
    private String brandName;

    @Size(max = 255)
    @Column(name = "logo_text", length = 255)
    private String logoText;

    @Size(max = 1000)
    @Column(name = "logo_url", length = 1000)
    private String logoUrl;

    @Size(max = 255)
    @Column(name = "support_email", length = 255)
    private String supportEmail;

    @Size(max = 30)
    @Column(name = "support_phone", length = 30)
    private String supportPhone;

    @Size(max = 1000)
    @Column(name = "address_text", length = 1000)
    private String addressText;

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getBrandName() { return brandName; }
    public void setBrandName(String brandName) { this.brandName = brandName; }

    public String getLogoText() { return logoText; }
    public void setLogoText(String logoText) { this.logoText = logoText; }

    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }

    public String getSupportEmail() { return supportEmail; }
    public void setSupportEmail(String supportEmail) { this.supportEmail = supportEmail; }

    public String getSupportPhone() { return supportPhone; }
    public void setSupportPhone(String supportPhone) { this.supportPhone = supportPhone; }

    public String getAddressText() { return addressText; }
    public void setAddressText(String addressText) { this.addressText = addressText; }
}