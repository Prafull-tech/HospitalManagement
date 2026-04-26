package com.hospital.hms.superadmin.service;

import com.hospital.hms.auth.entity.AppUser;
import com.hospital.hms.auth.entity.UserRole;
import com.hospital.hms.auth.repository.AppUserRepository;
import com.hospital.hms.common.exception.ResourceNotFoundException;
import com.hospital.hms.hospital.entity.Hospital;
import com.hospital.hms.hospital.repository.HospitalRepository;
import com.hospital.hms.superadmin.dto.*;
import com.hospital.hms.system.entity.SystemModule;
import com.hospital.hms.system.repository.SystemModuleRepository;
import com.hospital.hms.superadmin.repository.HospitalModuleRepository;
import com.hospital.hms.superadmin.entity.*;
import com.hospital.hms.superadmin.repository.HospitalSubscriptionRepository;
import com.hospital.hms.superadmin.repository.SubscriptionPlanRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class SuperAdminService {

    private final HospitalRepository hospitalRepository;
    private final AppUserRepository appUserRepository;
    private final SubscriptionPlanRepository planRepository;
    private final HospitalSubscriptionRepository subscriptionRepository;
    private final HospitalModuleRepository hospitalModuleRepository;
    private final SystemModuleRepository systemModuleRepository;
    private final PasswordEncoder passwordEncoder;

    public SuperAdminService(HospitalRepository hospitalRepository,
                             AppUserRepository appUserRepository,
                             SubscriptionPlanRepository planRepository,
                             HospitalSubscriptionRepository subscriptionRepository,
                             HospitalModuleRepository hospitalModuleRepository,
                             SystemModuleRepository systemModuleRepository,
                             PasswordEncoder passwordEncoder) {
        this.hospitalRepository = hospitalRepository;
        this.appUserRepository = appUserRepository;
        this.planRepository = planRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.hospitalModuleRepository = hospitalModuleRepository;
        this.systemModuleRepository = systemModuleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ── Dashboard ──

    @Transactional(readOnly = true)
    public DashboardSummaryDto getDashboardSummary() {
        DashboardSummaryDto dto = new DashboardSummaryDto();
        long activeSubscriptions = subscriptionRepository.countByStatus(SubscriptionStatus.ACTIVE);
        long trialSubscriptions = subscriptionRepository.countByStatus(SubscriptionStatus.TRIAL);
        long expiredSubscriptions = subscriptionRepository.countByStatus(SubscriptionStatus.EXPIRED);

        dto.setTotalHospitals(hospitalRepository.count());
        dto.setActiveHospitals(hospitalRepository.findByDeletedFalseAndIsActiveTrueOrderByHospitalNameAsc().size());
        dto.setTotalUsers(appUserRepository.count());
        dto.setActiveSubscriptions(activeSubscriptions);
        dto.setTrialSubscriptions(trialSubscriptions);
        dto.setExpiredSubscriptions(expiredSubscriptions);
        dto.setTotalSubscriptions(activeSubscriptions + trialSubscriptions + expiredSubscriptions);

        Map<Long, DashboardPlanEarningDto> planBreakdown = new LinkedHashMap<>();
        BigDecimal estimatedMonthlyEarnings = BigDecimal.ZERO;

        for (HospitalSubscription subscription : subscriptionRepository.findAll()) {
            SubscriptionPlan plan = subscription.getPlan();
            if (plan == null) {
                continue;
            }
            Long planId = plan.getId();
            DashboardPlanEarningDto row = planBreakdown.computeIfAbsent(planId, id -> {
                DashboardPlanEarningDto created = new DashboardPlanEarningDto();
                created.setPlanId(planId);
                created.setPlanCode(plan.getPlanCode());
                created.setPlanName(plan.getPlanName());
                created.setEstimatedMonthlyEarnings(BigDecimal.ZERO);
                return created;
            });

            if (subscription.getStatus() == SubscriptionStatus.ACTIVE) {
                row.setActiveSubscriptions(row.getActiveSubscriptions() + 1);
                BigDecimal monthlyEquivalent = resolveMonthlyEquivalent(plan, subscription.getBillingCycle());
                row.setEstimatedMonthlyEarnings(row.getEstimatedMonthlyEarnings().add(monthlyEquivalent));
                estimatedMonthlyEarnings = estimatedMonthlyEarnings.add(monthlyEquivalent);
            } else if (subscription.getStatus() == SubscriptionStatus.TRIAL) {
                row.setTrialSubscriptions(row.getTrialSubscriptions() + 1);
            }
        }

        List<DashboardPlanEarningDto> planRows = planBreakdown.values().stream()
                .peek(row -> row.setEstimatedMonthlyEarnings(row.getEstimatedMonthlyEarnings().setScale(2, RoundingMode.HALF_UP)))
                .sorted(Comparator.comparing(DashboardPlanEarningDto::getEstimatedMonthlyEarnings).reversed()
                        .thenComparing(DashboardPlanEarningDto::getPlanName, Comparator.nullsLast(String::compareToIgnoreCase)))
                .collect(Collectors.toList());

        estimatedMonthlyEarnings = estimatedMonthlyEarnings.setScale(2, RoundingMode.HALF_UP);
        dto.setEstimatedMonthlyEarnings(estimatedMonthlyEarnings);
        dto.setEstimatedCurrentMonthEarnings(estimatedMonthlyEarnings);
        dto.setPlanBreakdown(planRows);
        return dto;
    }

    private BigDecimal resolveMonthlyEquivalent(SubscriptionPlan plan, BillingCycle billingCycle) {
        BigDecimal monthly = plan.getMonthlyPrice() != null ? plan.getMonthlyPrice() : BigDecimal.ZERO;
        if (billingCycle == null || billingCycle == BillingCycle.MONTHLY) {
            return monthly;
        }
        if (billingCycle == BillingCycle.QUARTERLY) {
            BigDecimal quarterly = plan.getQuarterlyPrice() != null ? plan.getQuarterlyPrice() : monthly.multiply(BigDecimal.valueOf(3));
            return quarterly.divide(BigDecimal.valueOf(3), 2, RoundingMode.HALF_UP);
        }
        BigDecimal yearly = plan.getYearlyPrice() != null ? plan.getYearlyPrice() : monthly.multiply(BigDecimal.valueOf(12));
        return yearly.divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
    }

    // ── Hospital Users ──

    @Transactional(readOnly = true)
    public List<HospitalUserDto> getHospitalUsers(Long hospitalId) {
        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Hospital not found: " + hospitalId));
        List<AppUser> users = appUserRepository.findByHospitalId(hospitalId);
        return users.stream().map(u -> toUserDto(u, hospital)).collect(Collectors.toList());
    }

    @Transactional
    public HospitalUserDto createHospitalUser(Long hospitalId, CreateHospitalUserRequest req) {
        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Hospital not found: " + hospitalId));
        if (appUserRepository.findByUsernameIgnoreCase(req.getUsername().trim()).isPresent()) {
            throw new IllegalArgumentException("Username already exists: " + req.getUsername());
        }
        UserRole role;
        try {
            role = UserRole.valueOf(req.getRole());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid role: " + req.getRole());
        }
        AppUser user = new AppUser();
        user.setUsername(req.getUsername().trim());
        user.setFullName(req.getFullName().trim());
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        user.setRole(role);
        user.setHospital(hospital);
        user.setEmail(req.getEmail() != null ? req.getEmail().trim() : null);
        user.setPhone(req.getPhone() != null ? req.getPhone().trim() : null);
        user.setActive(true);
        user.setMustChangePassword(true);
        user = appUserRepository.save(user);
        return toUserDto(user, hospital);
    }

    @Transactional(readOnly = true)
    public HospitalModuleConfigResponseDto getHospitalModuleConfiguration(Long hospitalId) {
        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Hospital not found: " + hospitalId));
        HospitalSubscription currentSubscription = getCurrentSubscription(hospitalId);
        if (currentSubscription != null) {
            initializeHospitalModules(hospital, currentSubscription.getPlan());
        }
        return buildHospitalModuleConfig(hospital, currentSubscription);
    }

    @Transactional
    public HospitalModuleConfigResponseDto updateHospitalModules(Long hospitalId, HospitalModuleUpdateRequestDto req) {
        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Hospital not found: " + hospitalId));
        HospitalSubscription currentSubscription = getCurrentSubscription(hospitalId);
        if (currentSubscription != null) {
            initializeHospitalModules(hospital, currentSubscription.getPlan());
        }
        Map<String, HospitalModule> existingModules = hospitalModuleRepository.findByHospitalIdOrderByModuleCodeAsc(hospitalId)
                .stream()
                .collect(Collectors.toMap(
                        item -> normalizeModuleCode(item.getModuleCode()),
                        Function.identity(),
                        (left, right) -> left
                ));

        for (HospitalModuleItemRequestDto item : req.getModules()) {
            String moduleCode = normalizeModuleCode(item.getModuleCode());
            HospitalModule hospitalModule = existingModules.get(moduleCode);
            if (hospitalModule == null) {
                hospitalModule = new HospitalModule();
                hospitalModule.setHospital(hospital);
                hospitalModule.setModuleCode(moduleCode);
            }
            hospitalModule.setEnabled(item.getEnabled() == null || item.getEnabled());
            hospitalModuleRepository.save(hospitalModule);
        }

        return buildHospitalModuleConfig(hospital, currentSubscription);
    }

    @Transactional
    public void toggleUserStatus(Long hospitalId, Long userId, boolean active) {
        hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Hospital not found: " + hospitalId));
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        if (user.getHospital() == null || !user.getHospital().getId().equals(hospitalId)) {
            throw new IllegalArgumentException("User does not belong to this hospital");
        }
        user.setActive(active);
        appUserRepository.save(user);
    }

    // ── Subscription Plans ──

    @Transactional(readOnly = true)
    public List<SubscriptionPlanResponseDto> listPlans(boolean activeOnly) {
        List<SubscriptionPlan> plans = activeOnly
                ? planRepository.findByIsActiveTrueOrderByPlanNameAsc()
                : planRepository.findAllByOrderByPlanNameAsc();
        return plans.stream().map(this::toPlanDto).collect(Collectors.toList());
    }

    @Transactional
    public SubscriptionPlanResponseDto createPlan(SubscriptionPlanRequestDto req) {
        String code = req.getPlanCode().trim();
        if (planRepository.existsByPlanCode(code)) {
            throw new IllegalArgumentException("Plan code already exists: " + code);
        }
        SubscriptionPlan plan = new SubscriptionPlan();
        applyPlanFields(plan, req);
        plan = planRepository.save(plan);
        return toPlanDto(plan);
    }

    @Transactional
    public SubscriptionPlanResponseDto updatePlan(Long id, SubscriptionPlanRequestDto req) {
        SubscriptionPlan plan = planRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found: " + id));
        String code = req.getPlanCode().trim();
        if (planRepository.existsByPlanCodeAndIdNot(code, id)) {
            throw new IllegalArgumentException("Plan code already exists: " + code);
        }
        applyPlanFields(plan, req);
        plan = planRepository.save(plan);
        return toPlanDto(plan);
    }

    // ── Hospital Subscriptions ──

    @Transactional(readOnly = true)
    public List<HospitalSubscriptionResponseDto> listSubscriptions() {
        return subscriptionRepository.findAllByOrderByStartDateDesc()
                .stream().map(this::toSubDto).collect(Collectors.toList());
    }

    @Transactional
    public HospitalSubscriptionResponseDto createSubscription(HospitalSubscriptionRequestDto req) {
        Hospital hospital = hospitalRepository.findById(req.getHospitalId())
                .orElseThrow(() -> new ResourceNotFoundException("Hospital not found: " + req.getHospitalId()));
        SubscriptionPlan plan = planRepository.findById(req.getPlanId())
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found: " + req.getPlanId()));

        HospitalSubscription sub = new HospitalSubscription();
        sub.setHospital(hospital);
        sub.setPlan(plan);
        sub.setStatus(req.getStatus() != null ? SubscriptionStatus.valueOf(req.getStatus()) : SubscriptionStatus.TRIAL);
        sub.setStartDate(req.getStartDate() != null ? req.getStartDate() : LocalDate.now());
        sub.setEndDate(req.getEndDate());
        sub.setTrialEndDate(req.getTrialEndDate() != null ? req.getTrialEndDate()
                : (plan.getTrialDays() != null ? sub.getStartDate().plusDays(plan.getTrialDays()) : null));
        sub.setBillingCycle(req.getBillingCycle() != null ? BillingCycle.valueOf(req.getBillingCycle()) : BillingCycle.MONTHLY);
        sub.setNotes(req.getNotes());
        sub = subscriptionRepository.save(sub);
        initializeHospitalModules(hospital, plan);
        return toSubDto(sub);
    }

    @Transactional
    public HospitalSubscriptionResponseDto updateSubscriptionStatus(Long id, String status) {
        HospitalSubscription sub = subscriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found: " + id));
        sub.setStatus(SubscriptionStatus.valueOf(status));
        sub = subscriptionRepository.save(sub);
        return toSubDto(sub);
    }

    // ── Mappers ──

    private void applyPlanFields(SubscriptionPlan plan, SubscriptionPlanRequestDto req) {
        plan.setPlanCode(req.getPlanCode().trim());
        plan.setPlanName(req.getPlanName().trim());
        plan.setDescription(req.getDescription());
        plan.setMonthlyPrice(req.getMonthlyPrice());
        plan.setQuarterlyPrice(req.getQuarterlyPrice());
        plan.setYearlyPrice(req.getYearlyPrice());
        plan.setMaxUsers(req.getMaxUsers());
        plan.setMaxBeds(req.getMaxBeds());
        plan.setEnabledModules(req.getEnabledModules());
        plan.setIsActive(req.getActive() != null ? req.getActive() : true);
        plan.setTrialDays(req.getTrialDays());
    }

    private HospitalModuleConfigResponseDto buildHospitalModuleConfig(Hospital hospital, HospitalSubscription currentSubscription) {
        HospitalModuleConfigResponseDto dto = new HospitalModuleConfigResponseDto();
        dto.setHospitalId(hospital.getId());

        List<SystemModule> systemModules = systemModuleRepository.findAllByOrderByModuleCategoryAscSortOrderAscCodeAsc();
        Map<String, HospitalModule> hospitalOverrides = hospitalModuleRepository.findByHospitalIdOrderByModuleCodeAsc(hospital.getId())
            .stream()
            .collect(Collectors.toMap(
                item -> normalizeModuleCode(item.getModuleCode()),
                Function.identity(),
                (left, right) -> left
            ));
        if (currentSubscription == null) {
            dto.setHasActivePlan(false);
            dto.setModules(systemModules.stream()
                .map(module -> {
                String moduleCode = normalizeModuleCode(module.getCode());
                boolean enabled = hospitalOverrides.containsKey(moduleCode)
                    ? hospitalOverrides.get(moduleCode).isEnabled()
                    : module.isEnabled();
                return toHospitalModuleDto(module, false, enabled);
                })
                .collect(Collectors.toList()));
            return dto;
        }

        SubscriptionPlan plan = currentSubscription.getPlan();
        Set<String> planModules = parsePlanModules(plan.getEnabledModules());

        dto.setHasActivePlan(true);
        dto.setPlanId(plan.getId());
        dto.setPlanCode(plan.getPlanCode());
        dto.setPlanName(plan.getPlanName());
        dto.setModules(systemModules.stream()
                .map(module -> {
                    String moduleCode = normalizeModuleCode(module.getCode());
                    boolean inCurrentPlan = planModules.contains(moduleCode);
                boolean enabled = hospitalOverrides.containsKey(moduleCode)
                    ? hospitalOverrides.get(moduleCode).isEnabled()
                    : module.isEnabled();
                    return toHospitalModuleDto(module, inCurrentPlan, enabled);
                })
                .collect(Collectors.toList()));
        return dto;
    }

    private HospitalModuleResponseDto toHospitalModuleDto(SystemModule module, boolean inCurrentPlan, boolean enabled) {
        HospitalModuleResponseDto dto = new HospitalModuleResponseDto();
        dto.setModuleCode(module.getCode());
        dto.setModuleName(module.getName());
        dto.setModuleCategory(module.getModuleCategory().name());
        dto.setInCurrentPlan(inCurrentPlan);
        dto.setEnabled(enabled);
        return dto;
    }

    private HospitalSubscription getCurrentSubscription(Long hospitalId) {
        List<HospitalSubscription> subscriptions = subscriptionRepository.findByHospitalIdAndStatusInOrderByStartDateDescIdDesc(
                hospitalId,
                List.of(SubscriptionStatus.ACTIVE, SubscriptionStatus.TRIAL)
        );
        if (subscriptions.isEmpty()) {
            return null;
        }
        return subscriptions.stream()
                .max(Comparator
                        .comparing(HospitalSubscription::getStartDate, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(HospitalSubscription::getId, Comparator.nullsLast(Comparator.naturalOrder())))
                .orElse(null);
    }

    private void initializeHospitalModules(Hospital hospital, SubscriptionPlan plan) {
        Set<String> planModules = parsePlanModules(plan.getEnabledModules());
        if (planModules.isEmpty()) {
            return;
        }

        Map<String, HospitalModule> existingModules = hospitalModuleRepository.findByHospitalIdOrderByModuleCodeAsc(hospital.getId())
                .stream()
                .collect(Collectors.toMap(
                        item -> normalizeModuleCode(item.getModuleCode()),
                        Function.identity(),
                        (left, right) -> left
                ));

        List<HospitalModule> missingModules = new ArrayList<>();
        for (String moduleCode : planModules) {
            if (!existingModules.containsKey(moduleCode)) {
                HospitalModule hospitalModule = new HospitalModule();
                hospitalModule.setHospital(hospital);
                hospitalModule.setModuleCode(moduleCode);
                hospitalModule.setEnabled(true);
                missingModules.add(hospitalModule);
            }
        }

        if (!missingModules.isEmpty()) {
            hospitalModuleRepository.saveAll(missingModules);
        }
    }

    private Set<String> parsePlanModules(String enabledModules) {
        if (enabledModules == null || enabledModules.isBlank()) {
            return Collections.emptySet();
        }
        return Arrays.stream(enabledModules.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .map(this::normalizeModuleCode)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private String normalizeModuleCode(String moduleCode) {
        return moduleCode == null ? "" : moduleCode.trim().toUpperCase(Locale.ROOT);
    }

    private SubscriptionPlanResponseDto toPlanDto(SubscriptionPlan p) {
        SubscriptionPlanResponseDto dto = new SubscriptionPlanResponseDto();
        dto.setId(p.getId());
        dto.setPlanCode(p.getPlanCode());
        dto.setPlanName(p.getPlanName());
        dto.setDescription(p.getDescription());
        dto.setMonthlyPrice(p.getMonthlyPrice());
        dto.setQuarterlyPrice(p.getQuarterlyPrice());
        dto.setYearlyPrice(p.getYearlyPrice());
        dto.setMaxUsers(p.getMaxUsers());
        dto.setMaxBeds(p.getMaxBeds());
        dto.setEnabledModules(p.getEnabledModules());
        dto.setActive(p.getIsActive());
        dto.setTrialDays(p.getTrialDays());
        return dto;
    }

    private HospitalSubscriptionResponseDto toSubDto(HospitalSubscription s) {
        HospitalSubscriptionResponseDto dto = new HospitalSubscriptionResponseDto();
        dto.setId(s.getId());
        dto.setHospitalId(s.getHospital().getId());
        dto.setHospitalName(s.getHospital().getHospitalName());
        dto.setHospitalCode(s.getHospital().getHospitalCode());
        dto.setPlanId(s.getPlan().getId());
        dto.setPlanName(s.getPlan().getPlanName());
        dto.setPlanCode(s.getPlan().getPlanCode());
        dto.setStatus(s.getStatus().name());
        dto.setStartDate(s.getStartDate());
        dto.setEndDate(s.getEndDate());
        dto.setTrialEndDate(s.getTrialEndDate());
        dto.setBillingCycle(s.getBillingCycle() != null ? s.getBillingCycle().name() : null);
        dto.setNotes(s.getNotes());
        return dto;
    }

    private HospitalUserDto toUserDto(AppUser u, Hospital h) {
        HospitalUserDto dto = new HospitalUserDto();
        dto.setId(u.getId());
        dto.setUsername(u.getUsername());
        dto.setFullName(u.getFullName());
        dto.setRole(u.getRole().name());
        dto.setEmail(u.getEmail());
        dto.setPhone(u.getPhone());
        dto.setActive(u.getActive());
        dto.setMustChangePassword(u.getMustChangePassword());
        dto.setHospitalId(h.getId());
        dto.setHospitalName(h.getHospitalName());
        return dto;
    }
}
