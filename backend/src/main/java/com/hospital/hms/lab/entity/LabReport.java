package com.hospital.hms.lab.entity;

import com.hospital.hms.common.entity.BaseIdEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * Laboratory report (final document). Generated after verification and authorization.
 * Immutable after release. Contains all results, interpretation, and supervisor signature.
 */
@Entity
@Table(
    name = "lab_reports",
    indexes = {
        @Index(name = "idx_lab_report_number", columnList = "report_number", unique = true),
        @Index(name = "idx_lab_report_order", columnList = "test_order_id"),
        @Index(name = "idx_lab_report_status", columnList = "status"),
        @Index(name = "idx_lab_report_released_at", columnList = "released_at")
    }
)
public class LabReport extends BaseIdEntity {

    @NotBlank
    @Size(max = 50)
    @Column(name = "report_number", nullable = false, unique = true, length = 50)
    private String reportNumber;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_order_id", nullable = false)
    private TestOrder testOrder;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private ReportStatus status = ReportStatus.DRAFT;

    @Column(name = "generated_at")
    private LocalDateTime generatedAt;

    @Size(max = 255)
    @Column(name = "generated_by", length = 255)
    private String generatedBy; // Lab technician username

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Size(max = 255)
    @Column(name = "verified_by", length = 255)
    private String verifiedBy; // Lab supervisor username

    @Column(name = "released_at")
    private LocalDateTime releasedAt;

    @Size(max = 255)
    @Column(name = "released_by", length = 255)
    private String releasedBy;

    @Size(max = 5000)
    @Column(name = "report_content", length = 5000)
    private String reportContent; // Formatted report text/HTML

    @Size(max = 1000)
    @Column(name = "interpretation", length = 1000)
    private String interpretation; // Doctor's interpretation notes

    @Size(max = 500)
    @Column(name = "supervisor_signature", length = 500)
    private String supervisorSignature; // Digital signature or name

    @Size(max = 1000)
    @Column(name = "correction_log", length = 1000)
    private String correctionLog; // JSON log of any corrections made

    @Column(name = "is_read_only", nullable = false)
    private Boolean isReadOnly = false; // True after release

    @Size(max = 500)
    @Column(name = "pdf_path", length = 500)
    private String pdfPath; // Path to generated PDF report

    public LabReport() {
    }

    public String getReportNumber() {
        return reportNumber;
    }

    public void setReportNumber(String reportNumber) {
        this.reportNumber = reportNumber;
    }

    public TestOrder getTestOrder() {
        return testOrder;
    }

    public void setTestOrder(TestOrder testOrder) {
        this.testOrder = testOrder;
    }

    public ReportStatus getStatus() {
        return status;
    }

    public void setStatus(ReportStatus status) {
        this.status = status;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }

    public String getGeneratedBy() {
        return generatedBy;
    }

    public void setGeneratedBy(String generatedBy) {
        this.generatedBy = generatedBy;
    }

    public LocalDateTime getVerifiedAt() {
        return verifiedAt;
    }

    public void setVerifiedAt(LocalDateTime verifiedAt) {
        this.verifiedAt = verifiedAt;
    }

    public String getVerifiedBy() {
        return verifiedBy;
    }

    public void setVerifiedBy(String verifiedBy) {
        this.verifiedBy = verifiedBy;
    }

    public LocalDateTime getReleasedAt() {
        return releasedAt;
    }

    public void setReleasedAt(LocalDateTime releasedAt) {
        this.releasedAt = releasedAt;
    }

    public String getReleasedBy() {
        return releasedBy;
    }

    public void setReleasedBy(String releasedBy) {
        this.releasedBy = releasedBy;
    }

    public String getReportContent() {
        return reportContent;
    }

    public void setReportContent(String reportContent) {
        this.reportContent = reportContent;
    }

    public String getInterpretation() {
        return interpretation;
    }

    public void setInterpretation(String interpretation) {
        this.interpretation = interpretation;
    }

    public String getSupervisorSignature() {
        return supervisorSignature;
    }

    public void setSupervisorSignature(String supervisorSignature) {
        this.supervisorSignature = supervisorSignature;
    }

    public String getCorrectionLog() {
        return correctionLog;
    }

    public void setCorrectionLog(String correctionLog) {
        this.correctionLog = correctionLog;
    }

    public Boolean getIsReadOnly() {
        return isReadOnly;
    }

    public void setIsReadOnly(Boolean isReadOnly) {
        this.isReadOnly = isReadOnly;
    }

    public String getPdfPath() {
        return pdfPath;
    }

    public void setPdfPath(String pdfPath) {
        this.pdfPath = pdfPath;
    }
}
