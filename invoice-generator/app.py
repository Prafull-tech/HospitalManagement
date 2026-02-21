"""
FastAPI wrapper for Pharmacy Invoice PDF generation.
Optional: run as standalone service for microservice deployment.
"""
import json
import os
import tempfile
from pathlib import Path

from fastapi import FastAPI, HTTPException
from fastapi.responses import FileResponse
from pydantic import BaseModel
from typing import Optional

from invoice_service import generate_invoice_pdf, INVOICE_DIR

app = FastAPI(title="Pharmacy Invoice Generator")


class InvoiceGenerateRequest(BaseModel):
    invoice_number: str
    hospital: dict
    sale: dict
    medicine: dict
    patient: Optional[dict] = None
    gst_enabled: bool = False
    gst_percent: float = 0


@app.post("/generate")
def generate(request: InvoiceGenerateRequest):
    """Generate invoice PDF. Returns path."""
    try:
        data = {
            "hospital": request.hospital,
            "sale": request.sale,
            "medicine": request.medicine,
            "patient": request.patient,
            "gstEnabled": request.gst_enabled,
            "gstPercent": request.gst_percent,
        }
        path = generate_invoice_pdf(request.invoice_number, data)
        return {"success": True, "path": path, "invoice_number": request.invoice_number}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@app.get("/invoice/{invoice_number}")
def download_invoice(invoice_number: str):
    """Serve PDF file."""
    path = INVOICE_DIR / f"{invoice_number}.pdf"
    if not path.exists():
        raise HTTPException(status_code=404, detail="Invoice not found")
    return FileResponse(path, media_type="application/pdf", filename=f"{invoice_number}.pdf")
