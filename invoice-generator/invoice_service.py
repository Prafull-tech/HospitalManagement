"""
Pharmacy Invoice PDF Generator - reportlab.platypus (MANDATORY)
Hospital-grade, NABH/GST compliant, audit-safe.
"""
import os
import json
import logging
from pathlib import Path
from datetime import datetime
from decimal import Decimal

from reportlab.lib import colors
from reportlab.lib.pagesizes import A4
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.lib.units import mm
from reportlab.platypus import (
    SimpleDocTemplate, Paragraph, Spacer, Table, TableStyle,
    Image, PageBreak
)
from reportlab.lib.enums import TA_CENTER, TA_LEFT, TA_RIGHT

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# Storage path - configurable via env. Default: ./data/invoices (dev) or /mnt/data/invoices (prod)
INVOICE_DIR = Path(
    os.environ.get("INVOICE_STORAGE_PATH")
    or os.environ.get("INVOICE_DIR")
    or os.path.join(os.getcwd(), "data", "invoices")
)
INVOICE_DIR.mkdir(parents=True, exist_ok=True)


def _safe_str(val):
    return str(val) if val is not None else ""


def _format_date(d):
    if not d:
        return "—"
    if isinstance(d, str):
        return d[:10] if len(d) >= 10 else d
    return str(d)


def _amount_in_words(amount):
    """Convert number to words (Indian style). Simplified for common amounts."""
    if amount is None or amount == 0:
        return "Zero Rupees Only"
    try:
        n = int(round(float(amount)))
    except (ValueError, TypeError):
        return "—"
    ones = ["", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine"]
    tens = ["", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety"]
    teens = ["Ten", "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen", "Seventeen", "Eighteen", "Nineteen"]

    def to_words(n):
        if n == 0:
            return ""
        if n < 10:
            return ones[n]
        if n < 20:
            return teens[n - 10]
        if n < 100:
            return (tens[n // 10] + " " + ones[n % 10]).strip()
        if n < 1000:
            return (ones[n // 100] + " Hundred " + to_words(n % 100)).strip()
        if n < 100000:
            return (to_words(n // 1000) + " Thousand " + to_words(n % 1000)).strip()
        if n < 10000000:
            return (to_words(n // 100000) + " Lakh " + to_words(n % 100000)).strip()
        return str(n)  # fallback for large numbers

    return (to_words(n) + " Rupees Only").strip()


def generate_invoice_pdf(invoice_number: str, data: dict) -> str:
    """
    Generate pharmacy invoice PDF using reportlab.platypus.
    Returns absolute path to saved file.
    """
    filename = f"{invoice_number}.pdf"
    filepath = INVOICE_DIR / filename

    hospital = data.get("hospital", {})
    sale = data.get("sale", {})
    patient = data.get("patient")  # linked or manual
    gst_enabled = data.get("gstEnabled", False)
    gst_percent = data.get("gstPercent", 0)
    line_items = sale.get("lineItems", [])

    # Build elements
    doc = SimpleDocTemplate(
        str(filepath),
        pagesize=A4,
        rightMargin=15 * mm,
        leftMargin=15 * mm,
        topMargin=15 * mm,
        bottomMargin=15 * mm,
    )
    styles = getSampleStyleSheet()
    elements = []

    # Header
    elements.append(Paragraph(
        hospital.get("name", "Hospital Name"),
        ParagraphStyle(name="Title", fontSize=18, alignment=TA_CENTER, spaceAfter=4)
    ))
    elements.append(Paragraph(
        hospital.get("address", "Address"),
        ParagraphStyle(name="Sub", fontSize=10, alignment=TA_CENTER, spaceAfter=2)
    ))
    elements.append(Paragraph(
        f"Phone: {hospital.get('phone', '—')}",
        ParagraphStyle(name="Sub", fontSize=10, alignment=TA_CENTER, spaceAfter=2)
    ))
    if hospital.get("gstNo"):
        elements.append(Paragraph(
            f"GST No: {hospital.get('gstNo')}",
            ParagraphStyle(name="Sub", fontSize=10, alignment=TA_CENTER, spaceAfter=2)
        ))
    elements.append(Spacer(1, 8))

    elements.append(Paragraph(
        "PHARMACY INVOICE",
        ParagraphStyle(name="InvoiceTitle", fontSize=14, alignment=TA_CENTER, spaceAfter=12)
    ))

    # Invoice details
    inv_date = _format_date(sale.get("transactionDate"))
    sold_by = _safe_str(sale.get("performedBy"))
    sale_type = sale.get("saleType", "MANUAL")
    sale_type_display = "IPD" if (sale_type == "PATIENT" and patient and patient.get("ipdLinked")) else (
        "OPD" if sale_type == "PATIENT" else "Manual"
    )

    inv_details = [
        ["Invoice No:", invoice_number, "Date:", inv_date],
        ["Sold By:", sold_by, "Sale Type:", sale_type_display],
    ]
    t_inv = Table(inv_details, colWidths=[80, 80, 80, 80])
    t_inv.setStyle(TableStyle([
        ("FONTNAME", (0, 0), (0, -1), "Helvetica-Bold"),
        ("FONTNAME", (2, 0), (2, -1), "Helvetica-Bold"),
    ]))
    elements.append(t_inv)
    elements.append(Spacer(1, 8))

    # Patient / Customer details
    if patient:
        if sale_type == "PATIENT":
            p_rows = [
                ["Patient Name:", _safe_str(patient.get("patientName"))],
                ["UHID:", _safe_str(patient.get("uhid"))],
                ["IPD No:", _safe_str(patient.get("ipdNo", "—"))],
                ["Ward/Bed:", _safe_str(patient.get("wardBed", "—"))],
                ["Phone:", _safe_str(patient.get("phone", "—"))],
            ]
        else:
            p_rows = [
                ["Customer Name:", _safe_str(patient.get("customerName"))],
                ["Phone:", _safe_str(patient.get("phone", "—"))],
            ]
        t_p = Table(p_rows, colWidths=[100, 300])
        t_p.setStyle(TableStyle([
            ("FONTNAME", (0, 0), (0, -1), "Helvetica-Bold"),
        ]))
        elements.append(t_p)
        elements.append(Spacer(1, 8))

    # Medicine table - support multi-line
    if not line_items:
        # Backward compat: single item from sale
        qty = sale.get("quantity", 0)
        rate = sale.get("rate") or 0
        amount = float(rate) * int(qty)
        med_name = _safe_str(sale.get("medicineName", "—"))
        batch = _safe_str(sale.get("batchNumber", "N/A"))
        expiry = _format_date(sale.get("expiryDate"))
        table_data = [
            ["Sl No", "Medicine Name", "Batch No", "Expiry", "Qty", "Rate", "Amount"],
            ["1", med_name, batch, expiry, str(qty), f"{float(rate):.2f}", f"{amount:.2f}"],
        ]
        subtotal_base = amount
    else:
        table_data = [["Sl No", "Medicine Name", "Batch No", "Expiry", "Qty", "Rate", "Amount"]]
        subtotal_base = 0
        for idx, line in enumerate(line_items, 1):
            med_name = _safe_str(line.get("medicineName", "—"))
            batch = _safe_str(line.get("batchNumber", "N/A"))
            expiry = _format_date(line.get("expiryDate"))
            qty = int(line.get("quantity", 0))
            rate = float(line.get("rate") or 0)
            amount = rate * qty
            subtotal_base += amount
            table_data.append([str(idx), med_name, batch, expiry, str(qty), f"{rate:.2f}", f"{amount:.2f}"])
    t_med = Table(table_data, colWidths=[40, 140, 60, 60, 40, 50, 60])
    t_med.setStyle(TableStyle([
        ("BACKGROUND", (0, 0), (-1, 0), colors.HexColor("#e0e0e0")),
        ("FONTNAME", (0, 0), (-1, 0), "Helvetica-Bold"),
        ("FONTSIZE", (0, 0), (-1, 0), 9),
        ("ALIGN", (0, 0), (-1, -1), "CENTER"),
        ("ALIGN", (1, 0), (1, -1), "LEFT"),
        ("ALIGN", (4, 0), (-1, -1), "RIGHT"),
        ("GRID", (0, 0), (-1, -1), 0.5, colors.grey),
        ("VALIGN", (0, 0), (-1, -1), "MIDDLE"),
    ]))
    elements.append(t_med)
    elements.append(Spacer(1, 12))

    # Totals
    subtotal = subtotal_base
    if gst_enabled and gst_percent > 0:
        gst_amount = subtotal * (gst_percent / 100)
        total = subtotal + gst_amount
        cgst = gst_amount / 2
        sgst = gst_amount / 2
        total_rows = [
            ["Subtotal:", f"₹ {subtotal:.2f}"],
            [f"CGST ({gst_percent/2}%):", f"₹ {cgst:.2f}"],
            [f"SGST ({gst_percent/2}%):", f"₹ {sgst:.2f}"],
            ["Total Amount:", f"₹ {total:.2f}"],
        ]
    else:
        total = subtotal
        total_rows = [
            ["Total Amount:", f"₹ {total:.2f}"],
        ]

    total_rows.append(["Amount in Words:", _amount_in_words(total)])
    t_tot = Table(total_rows, colWidths=[120, 120])
    t_tot.setStyle(TableStyle([
        ("FONTNAME", (0, 0), (0, -1), "Helvetica-Bold"),
        ("ALIGN", (1, 0), (1, -1), "RIGHT"),
        ("FONTSIZE", (0, -1), (-1, -1), 8),
    ]))
    elements.append(t_tot)
    elements.append(Spacer(1, 20))

    # Footer
    elements.append(Paragraph(
        "Authorized Signature",
        ParagraphStyle(name="F1", fontSize=9, alignment=TA_LEFT)
    ))
    elements.append(Spacer(1, 4))
    elements.append(Paragraph(
        "This is a system-generated document. No signature required for electronic records.",
        ParagraphStyle(name="F2", fontSize=8, textColor=colors.grey, alignment=TA_LEFT)
    ))
    elements.append(Paragraph(
        "Terms & Conditions: Medicines are non-returnable after sale.",
        ParagraphStyle(name="F3", fontSize=8, textColor=colors.grey, alignment=TA_LEFT)
    ))

    doc.build(elements)
    logger.info("Generated invoice PDF: %s", filepath)
    return str(filepath)


def main():
    """CLI entry for testing."""
    import sys
    if len(sys.argv) < 3:
        print("Usage: python invoice_service.py <invoice_number> <json_data_file>")
        sys.exit(1)
    inv_no = sys.argv[1]
    with open(sys.argv[2]) as f:
        data = json.load(f)
    path = generate_invoice_pdf(inv_no, data)
    print(f"Generated: {path}")


if __name__ == "__main__":
    main()
