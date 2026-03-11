package com.hospital.hms.lab.entity;

import com.hospital.hms.common.entity.BaseIdEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

/**
 * Lab order line item. Links to TestOrder for lab workflow.
 */
@Entity
@Table(
    name = "lab_order_items",
    indexes = {
        @Index(name = "idx_lab_order_item_order", columnList = "order_id"),
        @Index(name = "idx_lab_order_item_test", columnList = "test_master_id"),
        @Index(name = "idx_lab_order_item_test_order", columnList = "test_order_id")
    }
)
public class LabOrderItem extends BaseIdEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private LabOrder order;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_master_id", nullable = false)
    private TestMaster testMaster;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private LabOrderItemStatus status = LabOrderItemStatus.ORDERED;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "sample_status", nullable = false, length = 30)
    private LabOrderItemSampleStatus sampleStatus = LabOrderItemSampleStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_order_id")
    private TestOrder testOrder; // Links to TestOrder for lab workflow (collection, results)

    public LabOrderItem() {
    }

    public LabOrder getOrder() {
        return order;
    }

    public void setOrder(LabOrder order) {
        this.order = order;
    }

    public TestMaster getTestMaster() {
        return testMaster;
    }

    public void setTestMaster(TestMaster testMaster) {
        this.testMaster = testMaster;
    }

    public LabOrderItemStatus getStatus() {
        return status;
    }

    public void setStatus(LabOrderItemStatus status) {
        this.status = status;
    }

    public LabOrderItemSampleStatus getSampleStatus() {
        return sampleStatus;
    }

    public void setSampleStatus(LabOrderItemSampleStatus sampleStatus) {
        this.sampleStatus = sampleStatus;
    }

    public TestOrder getTestOrder() {
        return testOrder;
    }

    public void setTestOrder(TestOrder testOrder) {
        this.testOrder = testOrder;
    }
}
