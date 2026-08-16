package com.bosu.housebook.admin;

import com.bosu.housebook.admin.dto.AdminHouseholdResponse;
import com.bosu.housebook.admin.dto.AdminLoginRequest;
import com.bosu.housebook.admin.dto.AdminStatsResponse;
import com.bosu.housebook.admin.dto.AdminUserResponse;
import com.bosu.housebook.auth.JwtTokenProvider;
import com.bosu.housebook.common.ApiException;
import com.bosu.housebook.config.AdminProperties;
import com.bosu.housebook.household.Household;
import com.bosu.housebook.household.HouseholdMember;
import com.bosu.housebook.household.HouseholdMemberRepository;
import com.bosu.housebook.household.HouseholdRepository;
import com.bosu.housebook.transaction.TransactionRepository;
import com.bosu.housebook.user.User;
import com.bosu.housebook.user.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AdminService {

    private final AdminProperties adminProperties;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final HouseholdRepository householdRepository;
    private final HouseholdMemberRepository householdMemberRepository;
    private final TransactionRepository transactionRepository;

    public AdminService(AdminProperties adminProperties, PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider, UserRepository userRepository,
            HouseholdRepository householdRepository, HouseholdMemberRepository householdMemberRepository,
            TransactionRepository transactionRepository) {
        this.adminProperties = adminProperties;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
        this.householdRepository = householdRepository;
        this.householdMemberRepository = householdMemberRepository;
        this.transactionRepository = transactionRepository;
    }

    public String login(AdminLoginRequest request) {
        boolean usernameMatches = adminProperties.username().equals(request.username());
        boolean passwordMatches = !adminProperties.passwordHash().isBlank()
                && passwordEncoder.matches(request.password(), adminProperties.passwordHash());
        if (!usernameMatches || !passwordMatches) {
            throw ApiException.unauthorized("아이디 또는 비밀번호가 올바르지 않습니다.");
        }
        return jwtTokenProvider.generateAdminToken();
    }

    public List<AdminUserResponse> listUsers() {
        return userRepository.findAll().stream()
                .map(this::toAdminUserResponse)
                .toList();
    }

    public List<AdminHouseholdResponse> listHouseholds() {
        return householdRepository.findAll().stream()
                .map(this::toAdminHouseholdResponse)
                .toList();
    }

    public AdminStatsResponse stats() {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        return new AdminStatsResponse(
                userRepository.count(),
                householdRepository.count(),
                transactionRepository.count(),
                userRepository.countByCreatedAtAfter(sevenDaysAgo),
                householdRepository.countByCreatedAtAfter(sevenDaysAgo));
    }

    @Transactional
    public void setBlocked(Long userId, boolean blocked) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ApiException.notFound("사용자를 찾을 수 없습니다."));
        user.updateBlocked(blocked);
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ApiException.notFound("사용자를 찾을 수 없습니다."));
        userRepository.delete(user);
    }

    private AdminUserResponse toAdminUserResponse(User user) {
        var membership = householdMemberRepository.findByUserId(user.getId()).orElse(null);
        Household household = membership != null ? membership.getHousehold() : null;
        return new AdminUserResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getBirthDate(),
                user.isBlocked(),
                user.getCreatedAt(),
                household != null ? household.getId() : null,
                household != null ? household.getName() : null,
                membership != null ? membership.getRole().name() : null,
                transactionRepository.countByUserId(user.getId()));
    }

    private AdminHouseholdResponse toAdminHouseholdResponse(Household household) {
        List<String> memberNames = householdMemberRepository.findByHouseholdId(household.getId()).stream()
                .map(HouseholdMember::getUser)
                .map(User::getName)
                .toList();
        return new AdminHouseholdResponse(
                household.getId(),
                household.getName(),
                household.getInviteCode(),
                household.getCreatedAt(),
                memberNames,
                transactionRepository.countByHouseholdId(household.getId()));
    }
}
