package com.bosu.housebook.household;

import com.bosu.housebook.common.ApiException;
import com.bosu.housebook.household.dto.HouseholdCreateRequest;
import com.bosu.housebook.household.dto.HouseholdResponse;
import com.bosu.housebook.household.dto.JoinHouseholdRequest;
import com.bosu.housebook.household.dto.MemberResponse;
import com.bosu.housebook.user.User;
import com.bosu.housebook.user.UserRepository;
import java.security.SecureRandom;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class HouseholdService {

    private static final String INVITE_CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int INVITE_CODE_LENGTH = 8;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final HouseholdRepository householdRepository;
    private final HouseholdMemberRepository householdMemberRepository;
    private final UserRepository userRepository;

    public HouseholdService(HouseholdRepository householdRepository, HouseholdMemberRepository householdMemberRepository,
            UserRepository userRepository) {
        this.householdRepository = householdRepository;
        this.householdMemberRepository = householdMemberRepository;
        this.userRepository = userRepository;
    }

    /** 다른 도메인 서비스가 현재 로그인한 유저의 household id를 조회할 때 사용. */
    public Long getHouseholdIdForUser(Long userId) {
        return householdMemberRepository.findByUserId(userId)
                .map(member -> member.getHousehold().getId())
                .orElseThrow(() -> ApiException.badRequest("가입된 가계부가 없습니다. 가계부를 생성하거나 초대 코드로 참여하세요."));
    }

    @Transactional
    public HouseholdResponse create(Long userId, HouseholdCreateRequest request) {
        if (householdMemberRepository.findByUserId(userId).isPresent()) {
            throw ApiException.conflict("이미 가계부에 참여하고 있습니다.");
        }
        User user = getUser(userId);
        Household household = new Household(request.name(), generateUniqueInviteCode());
        householdRepository.save(household);
        householdMemberRepository.save(new HouseholdMember(household, user, HouseholdRole.OWNER));
        return toResponse(household);
    }

    public HouseholdResponse getMyHousehold(Long userId) {
        Long householdId = getHouseholdIdForUser(userId);
        Household household = householdRepository.findById(householdId)
                .orElseThrow(() -> ApiException.notFound("가계부를 찾을 수 없습니다."));
        return toResponse(household);
    }

    public String getInviteCode(Long userId) {
        Long householdId = getHouseholdIdForUser(userId);
        return householdRepository.findById(householdId)
                .orElseThrow(() -> ApiException.notFound("가계부를 찾을 수 없습니다."))
                .getInviteCode();
    }

    @Transactional
    public HouseholdResponse join(Long userId, JoinHouseholdRequest request) {
        if (householdMemberRepository.findByUserId(userId).isPresent()) {
            throw ApiException.conflict("이미 가계부에 참여하고 있습니다.");
        }
        Household household = householdRepository.findByInviteCode(request.inviteCode())
                .orElseThrow(() -> ApiException.notFound("유효하지 않은 초대 코드입니다."));
        User user = getUser(userId);
        householdMemberRepository.save(new HouseholdMember(household, user, HouseholdRole.MEMBER));
        return toResponse(household);
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> ApiException.notFound("사용자를 찾을 수 없습니다."));
    }

    private HouseholdResponse toResponse(Household household) {
        List<MemberResponse> members = householdMemberRepository.findByHouseholdId(household.getId()).stream()
                .map(MemberResponse::from)
                .toList();
        return HouseholdResponse.of(household, members);
    }

    private String generateUniqueInviteCode() {
        String code;
        do {
            code = generateInviteCode();
        } while (householdRepository.existsByInviteCode(code));
        return code;
    }

    private String generateInviteCode() {
        StringBuilder sb = new StringBuilder(INVITE_CODE_LENGTH);
        for (int i = 0; i < INVITE_CODE_LENGTH; i++) {
            sb.append(INVITE_CODE_CHARS.charAt(RANDOM.nextInt(INVITE_CODE_CHARS.length())));
        }
        return sb.toString();
    }
}
