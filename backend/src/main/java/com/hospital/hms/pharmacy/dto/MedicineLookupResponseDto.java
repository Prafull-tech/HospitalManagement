package com.hospital.hms.pharmacy.dto;

/**
 * Response for medicine lookup by barcode/GTIN.
 * source=local: from Medicine Master; source=external: from 3rd party API.
 */
public class MedicineLookupResponseDto {

    public enum LookupSource {
        LOCAL,
        EXTERNAL
    }

    private LookupSource source;
    private MedicineMasterResponseDto data;

    public LookupSource getSource() {
        return source;
    }

    public void setSource(LookupSource source) {
        this.source = source;
    }

    public MedicineMasterResponseDto getData() {
        return data;
    }

    public void setData(MedicineMasterResponseDto data) {
        this.data = data;
    }
}
