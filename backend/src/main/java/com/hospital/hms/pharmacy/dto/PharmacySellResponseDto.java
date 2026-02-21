package com.hospital.hms.pharmacy.dto;

/**
 * Response after successful pharmacy sell. Includes invoice info when PDF generated.
 */
public class PharmacySellResponseDto {

    private boolean success = true;
    private String invoiceNumber;
    private String pdfUrl;
    private StockTransactionResponseDto transaction;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public String getPdfUrl() {
        return pdfUrl;
    }

    public void setPdfUrl(String pdfUrl) {
        this.pdfUrl = pdfUrl;
    }

    public StockTransactionResponseDto getTransaction() {
        return transaction;
    }

    public void setTransaction(StockTransactionResponseDto transaction) {
        this.transaction = transaction;
    }
}
