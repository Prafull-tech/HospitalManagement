package com.hospital.hms.pharmacy.entity;

import com.hospital.hms.common.entity.BaseIdEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * Pharmacy invoice record. Links sale (StockTransaction) to generated PDF.
 * Audit-safe, supports regeneration.
 */
@Entity
@Table(
        name = "pharmacy_invoice",
        indexes = {
                @Index(name = "idx_ph_inv_number", columnList = "invoice_number", unique = true),
                @Index(name = "idx_ph_inv_sale", columnList = "sale_id")
        }
)
public class PharmacyInvoice extends BaseIdEntity {

    @Column(name = "invoice_number", nullable = false, unique = true, length = 30)
    private String invoiceNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_id", nullable = false)
    private StockTransaction sale;

    @Column(name = "pdf_path", nullable = false, length = 500)
    private String pdfPath;

    @Column(name = "generated_by", nullable = false, length = 255)
    private String generatedBy;

    @Column(name = "generated_at", nullable = false)
    private Instant generatedAt;

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public StockTransaction getSale() {
        return sale;
    }

    public void setSale(StockTransaction sale) {
        this.sale = sale;
    }

    public String getPdfPath() {
        return pdfPath;
    }

    public void setPdfPath(String pdfPath) {
        this.pdfPath = pdfPath;
    }

    public String getGeneratedBy() {
        return generatedBy;
    }

    public void setGeneratedBy(String generatedBy) {
        this.generatedBy = generatedBy;
    }

    public Instant getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(Instant generatedAt) {
        this.generatedAt = generatedAt;
    }
}
