package com.hospital.hms.tenant.context;

import java.util.Optional;

public final class TenantContextHolder {

    private static final ThreadLocal<TenantRequestContext> HOLDER = new ThreadLocal<>();

    private TenantContextHolder() {
    }

    public static void set(TenantRequestContext context) {
        HOLDER.set(context);
    }

    public static Optional<TenantRequestContext> get() {
        return Optional.ofNullable(HOLDER.get());
    }

    public static void clear() {
        HOLDER.remove();
    }
}