package com.bosu.housebook.card;

import com.bosu.housebook.card.dto.CardRequest;
import com.bosu.housebook.card.dto.CardResponse;
import com.bosu.housebook.common.ApiException;
import com.bosu.housebook.household.Household;
import com.bosu.housebook.household.HouseholdMemberRepository;
import com.bosu.housebook.household.HouseholdRepository;
import com.bosu.housebook.household.HouseholdService;
import com.bosu.housebook.user.User;
import com.bosu.housebook.user.UserRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CardService {

    private final CardRepository cardRepository;
    private final HouseholdRepository householdRepository;
    private final HouseholdMemberRepository householdMemberRepository;
    private final HouseholdService householdService;
    private final UserRepository userRepository;

    public CardService(CardRepository cardRepository, HouseholdRepository householdRepository,
            HouseholdMemberRepository householdMemberRepository, HouseholdService householdService,
            UserRepository userRepository) {
        this.cardRepository = cardRepository;
        this.householdRepository = householdRepository;
        this.householdMemberRepository = householdMemberRepository;
        this.householdService = householdService;
        this.userRepository = userRepository;
    }

    public List<CardResponse> getAll(Long userId) {
        Long householdId = householdService.getHouseholdIdForUser(userId);
        return cardRepository.findByHouseholdIdOrderByIdAsc(householdId).stream()
                .map(CardResponse::from)
                .toList();
    }

    @Transactional
    public CardResponse create(Long userId, CardRequest request) {
        Long householdId = householdService.getHouseholdIdForUser(userId);
        Household household = householdRepository.getReferenceById(householdId);
        User owner = resolveOwner(householdId, request.ownerUserId());
        Card card = new Card(household, request.name(), request.type(), owner);
        cardRepository.save(card);
        return CardResponse.from(card);
    }

    @Transactional
    public CardResponse update(Long userId, Long cardId, CardRequest request) {
        Long householdId = householdService.getHouseholdIdForUser(userId);
        Card card = cardRepository.findByIdAndHouseholdId(cardId, householdId)
                .orElseThrow(() -> ApiException.notFound("카드를 찾을 수 없습니다."));
        User owner = resolveOwner(householdId, request.ownerUserId());
        card.update(request.name(), request.type(), owner);
        return CardResponse.from(card);
    }

    @Transactional
    public void delete(Long userId, Long cardId) {
        Long householdId = householdService.getHouseholdIdForUser(userId);
        Card card = cardRepository.findByIdAndHouseholdId(cardId, householdId)
                .orElseThrow(() -> ApiException.notFound("카드를 찾을 수 없습니다."));
        cardRepository.delete(card);
    }

    private User resolveOwner(Long householdId, Long ownerUserId) {
        if (ownerUserId == null) {
            return null;
        }
        if (!householdMemberRepository.existsByHouseholdIdAndUserId(householdId, ownerUserId)) {
            throw ApiException.badRequest("가계부 구성원만 카드 소유자로 지정할 수 있습니다.");
        }
        return userRepository.getReferenceById(ownerUserId);
    }
}
