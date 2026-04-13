ALTER TABLE patients ADD COLUMN IF NOT EXISTS hospital_id BIGINT;
ALTER TABLE appointments ADD COLUMN IF NOT EXISTS hospital_id BIGINT;
ALTER TABLE wards ADD COLUMN IF NOT EXISTS hospital_id BIGINT;
ALTER TABLE ipd_admissions ADD COLUMN IF NOT EXISTS hospital_id BIGINT;

UPDATE patients
SET hospital_id = (
    SELECT seeded_hospital.id
    FROM (
        SELECT id FROM hospitals ORDER BY id LIMIT 1
    ) seeded_hospital
)
WHERE hospital_id IS NULL
  AND EXISTS (SELECT 1 FROM hospitals);

UPDATE appointments a
JOIN patients p ON p.id = a.patient_id
SET a.hospital_id = p.hospital_id
WHERE a.hospital_id IS NULL
  AND p.hospital_id IS NOT NULL;

UPDATE wards
SET hospital_id = (
    SELECT seeded_hospital.id
    FROM (
        SELECT id FROM hospitals ORDER BY id LIMIT 1
    ) seeded_hospital
)
WHERE hospital_id IS NULL
  AND EXISTS (SELECT 1 FROM hospitals);

UPDATE ipd_admissions a
JOIN patients p ON p.id = a.patient_id
SET a.hospital_id = p.hospital_id
WHERE a.hospital_id IS NULL
  AND p.hospital_id IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_patient_hospital ON patients (hospital_id);
CREATE INDEX IF NOT EXISTS idx_appointment_hospital ON appointments (hospital_id);
CREATE INDEX IF NOT EXISTS idx_ward_hospital ON wards (hospital_id);
CREATE INDEX IF NOT EXISTS idx_ipd_admission_hospital ON ipd_admissions (hospital_id);

ALTER TABLE patients ADD CONSTRAINT fk_patient_hospital
    FOREIGN KEY (hospital_id) REFERENCES hospitals(id);
ALTER TABLE appointments ADD CONSTRAINT fk_appointment_hospital
    FOREIGN KEY (hospital_id) REFERENCES hospitals(id);
ALTER TABLE wards ADD CONSTRAINT fk_ward_hospital
    FOREIGN KEY (hospital_id) REFERENCES hospitals(id);
ALTER TABLE ipd_admissions ADD CONSTRAINT fk_ipd_admission_hospital
    FOREIGN KEY (hospital_id) REFERENCES hospitals(id);