ALTER TABLE hospitals ADD COLUMN IF NOT EXISTS subdomain VARCHAR(100);
ALTER TABLE hospitals ADD UNIQUE INDEX IF NOT EXISTS idx_hospital_subdomain (subdomain);