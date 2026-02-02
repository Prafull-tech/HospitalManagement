package com.hospital.hms.system.entity;

import com.hospital.hms.common.entity.BaseIdEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Feature toggle: enable/disable features without redeploy. DB-agnostic (H2 & MySQL).
 */
@Entity
@Table(
    name = "feature_toggles",
    indexes = @Index(name = "idx_feature_toggle_key", columnList = "feature_key", unique = true)
)
public class FeatureToggle extends BaseIdEntity {

    @NotBlank
    @Size(max = 80)
    @Column(name = "feature_key", nullable = false, unique = true, length = 80)
    private String featureKey;

    @Size(max = 120)
    @Column(name = "name", length = 120)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "enabled", nullable = false)
    private boolean enabled = false;

    @Column(name = "sort_order")
    private Integer sortOrder;

    public FeatureToggle() {
    }

    public String getFeatureKey() {
        return featureKey;
    }

    public void setFeatureKey(String featureKey) {
        this.featureKey = featureKey;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
}
