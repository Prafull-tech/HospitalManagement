# Pharmacy Invoice PDF Generator

Uses **reportlab.platypus** (MANDATORY) for hospital-grade, NABH/GST compliant invoices.

## Setup

```bash
pip install -r requirements.txt
```

## Usage

### 1. Called by Spring Boot (automatic)

After a successful pharmacy sale, the backend spawns this script:

```
python invoice_service.py INV-2026-0001 /tmp/pharmacy-inv-xxx.json
```

### 2. Standalone test

Create `sample_data.json`:

```json
{
  "hospital": {
    "name": "Sample Hospital",
    "address": "123 Hospital Road",
    "phone": "+91-1234567890",
    "gstNo": "29XXXXX1234X1XX"
  },
  "sale": {
    "transactionDate": "2026-02-05",
    "performedBy": "PHARMACIST",
    "saleType": "PATIENT",
    "quantity": 2,
    "batchNumber": "BATCH001",
    "expiryDate": "2026-12-31",
    "rate": 150.50,
    "medicineName": "Paracetamol 500mg"
  },
  "medicine": {
    "medicineName": "Paracetamol 500mg",
    "medicineCode": "PARA500"
  },
  "patient": {
    "patientName": "John Doe",
    "uhid": "UHID001",
    "phone": "9876543210",
    "ipdNo": "IPD-2026-001",
    "wardBed": "Ward A / Bed 5",
    "ipdLinked": true
  },
  "gstEnabled": true,
  "gstPercent": 12
}
```

Run:

```bash
python invoice_service.py INV-2026-0001 sample_data.json
```

Output: `./data/invoices/INV-2026-0001.pdf` (or `INVOICE_STORAGE_PATH`)

### 3. FastAPI service (optional)

```bash
uvicorn app:app --reload --port 8001
```

POST `/generate` with JSON body.
