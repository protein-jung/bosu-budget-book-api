package com.bosu.housebook.transaction;

import com.bosu.housebook.card.Card;
import com.bosu.housebook.card.CardRepository;
import com.bosu.housebook.category.Category;
import com.bosu.housebook.category.CategoryRepository;
import com.bosu.housebook.common.ApiException;
import com.bosu.housebook.common.TransactionType;
import com.bosu.housebook.household.Household;
import com.bosu.housebook.household.HouseholdMember;
import com.bosu.housebook.household.HouseholdMemberRepository;
import com.bosu.housebook.household.HouseholdRepository;
import com.bosu.housebook.household.HouseholdService;
import com.bosu.housebook.notification.NotificationService;
import com.bosu.housebook.notification.NotificationType;
import com.bosu.housebook.transaction.dto.TransactionCommentResponse;
import com.bosu.housebook.transaction.dto.TransactionRequest;
import com.bosu.housebook.transaction.dto.TransactionResponse;
import com.bosu.housebook.user.User;
import com.bosu.housebook.user.UserRepository;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final TransactionCommentRepository transactionCommentRepository;
    private final HouseholdRepository householdRepository;
    private final HouseholdService householdService;
    private final HouseholdMemberRepository householdMemberRepository;
    private final CategoryRepository categoryRepository;
    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public TransactionService(TransactionRepository transactionRepository,
            TransactionCommentRepository transactionCommentRepository, HouseholdRepository householdRepository,
            HouseholdService householdService, HouseholdMemberRepository householdMemberRepository,
            CategoryRepository categoryRepository, CardRepository cardRepository, UserRepository userRepository,
            NotificationService notificationService) {
        this.transactionRepository = transactionRepository;
        this.transactionCommentRepository = transactionCommentRepository;
        this.householdRepository = householdRepository;
        this.householdService = householdService;
        this.householdMemberRepository = householdMemberRepository;
        this.categoryRepository = categoryRepository;
        this.cardRepository = cardRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    public List<TransactionResponse> getMonthly(Long userId, int year, int month) {
        Long householdId = householdService.getHouseholdIdForUser(userId);
        return getMonthlyByHousehold(householdId, year, month);
    }

    /** 관리자 화면에서 특정 가계부를 조회할 때처럼, 로그인한 유저 본인이 아니라 가계부 id를 직접
     * 아는 경우에 쓴다. */
    public List<TransactionResponse> getMonthlyByHousehold(Long householdId, int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate from = yearMonth.atDay(1);
        LocalDate to = yearMonth.atEndOfMonth();
        return transactionRepository
                .findByHouseholdIdAndTransactionDateBetweenAndIsBackfillFalseOrderByTransactionDateAscIdAsc(
                        householdId, from, to)
                .stream()
                .map(TransactionResponse::from)
                .toList();
    }

    @Transactional
    public TransactionResponse create(Long userId, TransactionRequest request) {
        Long householdId = householdService.getHouseholdIdForUser(userId);
        Household household = householdRepository.getReferenceById(householdId);
        Category category = getOwnedCategory(householdId, request.categoryId());
        Card card = getOwnedCard(householdId, request.cardId());
        User user = userRepository.getReferenceById(userId);

        Transaction transaction = new Transaction(household, request.type(), request.amount(),
                request.transactionDate(), category, card, user, request.memo(), request.note());
        transactionRepository.save(transaction);
        notifyOtherMembers(householdId, userId, user, request, category);
        return TransactionResponse.from(transaction);
    }

    /** 가계부를 같이 쓰는 배우자 등 나머지 구성원에게 "누가 얼마를 등록했는지" 알려준다. 본인에게는
     * 당연히 보낼 필요가 없어서 제외한다. */
    private void notifyOtherMembers(Long householdId, Long authorUserId, User author, TransactionRequest request,
            Category category) {
        String typeLabel = request.type() == TransactionType.INCOME ? "수입" : "지출";
        String title = "%s님이 %s을 등록했어요".formatted(author.getName(), typeLabel);
        String body = "%s · %,d원".formatted(category.getName(), request.amount().longValue());

        List<HouseholdMember> members = householdMemberRepository.findByHouseholdId(householdId);
        for (HouseholdMember member : members) {
            if (!member.getUser().getId().equals(authorUserId)) {
                notificationService.create(member.getUser(), NotificationType.TRANSACTION_CREATED, title, body,
                        "/calendar");
            }
        }
    }

    @Transactional
    public TransactionResponse update(Long userId, Long transactionId, TransactionRequest request) {
        Long householdId = householdService.getHouseholdIdForUser(userId);
        Transaction transaction = transactionRepository.findByIdAndHouseholdId(transactionId, householdId)
                .orElseThrow(() -> ApiException.notFound("거래 내역을 찾을 수 없습니다."));
        Category category = getOwnedCategory(householdId, request.categoryId());
        Card card = getOwnedCard(householdId, request.cardId());

        transaction.update(request.type(), request.amount(), request.transactionDate(), category, card,
                request.memo(), request.note());
        return TransactionResponse.from(transaction);
    }

    @Transactional
    public void delete(Long userId, Long transactionId) {
        Long householdId = householdService.getHouseholdIdForUser(userId);
        Transaction transaction = transactionRepository.findByIdAndHouseholdId(transactionId, householdId)
                .orElseThrow(() -> ApiException.notFound("거래 내역을 찾을 수 없습니다."));
        transactionRepository.delete(transaction);
    }

    public List<TransactionCommentResponse> getComments(Long userId, Long transactionId) {
        Transaction transaction = getOwnedTransaction(userId, transactionId);
        return transactionCommentRepository.findByTransactionIdOrderByCreatedAtAsc(transaction.getId()).stream()
                .map(TransactionCommentResponse::from)
                .toList();
    }

    @Transactional
    public TransactionCommentResponse addComment(Long userId, Long transactionId, String body) {
        Transaction transaction = getOwnedTransaction(userId, transactionId);
        User user = userRepository.getReferenceById(userId);
        TransactionComment saved = transactionCommentRepository
                .save(new TransactionComment(transaction, user, body.trim()));
        return TransactionCommentResponse.from(saved);
    }

    @Transactional
    public void deleteComment(Long userId, Long transactionId, Long commentId) {
        getOwnedTransaction(userId, transactionId);
        TransactionComment comment = transactionCommentRepository.findById(commentId)
                .orElseThrow(() -> ApiException.notFound("댓글을 찾을 수 없습니다."));
        if (!comment.getTransaction().getId().equals(transactionId)) {
            throw ApiException.notFound("댓글을 찾을 수 없습니다.");
        }
        if (comment.getUser() == null || !comment.getUser().getId().equals(userId)) {
            throw ApiException.forbidden("본인이 남긴 댓글만 지울 수 있습니다.");
        }
        transactionCommentRepository.delete(comment);
    }

    private Transaction getOwnedTransaction(Long userId, Long transactionId) {
        Long householdId = householdService.getHouseholdIdForUser(userId);
        return transactionRepository.findByIdAndHouseholdId(transactionId, householdId)
                .orElseThrow(() -> ApiException.notFound("거래 내역을 찾을 수 없습니다."));
    }

    private Category getOwnedCategory(Long householdId, Long categoryId) {
        return categoryRepository.findByIdAndHouseholdId(categoryId, householdId)
                .orElseThrow(() -> ApiException.badRequest("유효하지 않은 카테고리입니다."));
    }

    private Card getOwnedCard(Long householdId, Long cardId) {
        if (cardId == null) {
            return null;
        }
        return cardRepository.findByIdAndHouseholdId(cardId, householdId)
                .orElseThrow(() -> ApiException.badRequest("유효하지 않은 카드입니다."));
    }
}
