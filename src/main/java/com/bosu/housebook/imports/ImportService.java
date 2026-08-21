package com.bosu.housebook.imports;

import com.bosu.housebook.card.Card;
import com.bosu.housebook.card.CardRepository;
import com.bosu.housebook.card.CardType;
import com.bosu.housebook.category.Category;
import com.bosu.housebook.category.CategoryRepository;
import com.bosu.housebook.common.ApiException;
import com.bosu.housebook.common.TransactionType;
import com.bosu.housebook.household.Household;
import com.bosu.housebook.household.HouseholdRepository;
import com.bosu.housebook.household.HouseholdService;
import com.bosu.housebook.imports.dto.ImportResultResponse;
import com.bosu.housebook.transaction.Transaction;
import com.bosu.housebook.transaction.TransactionRepository;
import com.bosu.housebook.user.User;
import com.bosu.housebook.user.UserRepository;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional(readOnly = true)
public class ImportService {

    private static final String UNCATEGORIZED_NAME = "미분류";
    private static final String UNCATEGORIZED_COLOR = "#94a3b8";
    private static final String UNCATEGORIZED_ICON = "❓";
    private static final String COUPANG_CORP_MERCHANT_NAME = "쿠팡 주식회사";

    private final Map<ImportProvider, StatementParser> parsers;
    private final HouseholdService householdService;
    private final HouseholdRepository householdRepository;
    private final CardRepository cardRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final ImportBatchRepository importBatchRepository;
    private final UserRepository userRepository;
    private final MerchantCategoryClassifier categoryClassifier;
    private final OfficeFileDecryptor officeFileDecryptor;

    public ImportService(List<StatementParser> parsers, HouseholdService householdService,
            HouseholdRepository householdRepository, CardRepository cardRepository,
            CategoryRepository categoryRepository, TransactionRepository transactionRepository,
            ImportBatchRepository importBatchRepository, UserRepository userRepository,
            MerchantCategoryClassifier categoryClassifier,
            OfficeFileDecryptor officeFileDecryptor) {
        this.parsers = parsers.stream().collect(java.util.stream.Collectors.toMap(StatementParser::provider,
                Function.identity()));
        this.householdService = householdService;
        this.householdRepository = householdRepository;
        this.cardRepository = cardRepository;
        this.categoryRepository = categoryRepository;
        this.transactionRepository = transactionRepository;
        this.importBatchRepository = importBatchRepository;
        this.userRepository = userRepository;
        this.categoryClassifier = categoryClassifier;
        this.officeFileDecryptor = officeFileDecryptor;
    }

    @Transactional
    public ImportResultResponse importStatement(Long userId, ImportProvider provider, Long cardId, String cardName,
            MultipartFile file) {
        Long householdId = householdService.getHouseholdIdForUser(userId);
        User uploader = userRepository.findById(userId)
                .orElseThrow(() -> ApiException.notFound("사용자를 찾을 수 없습니다."));
        byte[] bytes = readBytes(file);
        String checksum = sha256(bytes);

        if (importBatchRepository.existsByHouseholdIdAndFileChecksum(householdId, checksum)) {
            throw ApiException.conflict("이미 가져온 파일입니다.");
        }

        StatementParser parser = parsers.get(provider);
        if (parser == null) {
            throw ApiException.badRequest("지원하지 않는 명세서 형식입니다.");
        }

        byte[] readableBytes = officeFileDecryptor.decryptIfNeeded(bytes, uploader);
        List<ParsedTransaction> parsed = parser.parse(new ByteArrayInputStream(readableBytes));
        if (parsed.isEmpty()) {
            throw ApiException.badRequest("가져올 거래 내역을 찾지 못했습니다.");
        }

        Household household = householdRepository.getReferenceById(householdId);
        Card card = resolveCard(household, cardId, cardName, provider);
        ExistingCounts existingCounts = countExistingByKey(householdId, card.getId(), parsed);

        Function<String, Optional<MerchantCategoryClassifier.CategorySuggestion>> classify = provider == ImportProvider.COUPANG
                ? categoryClassifier::classifyProduct
                : categoryClassifier::classify;

        Map<String, Category> categoryCache = new LinkedHashMap<>();
        Map<Long, BreakdownAccumulator> breakdown = new LinkedHashMap<>();
        BigDecimal total = BigDecimal.ZERO;
        int skippedCount = 0;
        for (ParsedTransaction row : parsed) {
            if (row.merchantName().contains(COUPANG_CORP_MERCHANT_NAME)) {
                // 쿠팡 주문내역은 COUPANG 명세서로 상품 단위로 따로 들어오므로, 카드/계좌 명세서의
                // "쿠팡 주식회사" 결제 내역은 중복이라 가져오지 않는다.
                skippedCount++;
                continue;
            }
            boolean duplicate = row.looseDuplicateCheck()
                    ? consumeIfPresent(existingCounts.loose(),
                            looseDuplicateKey(row.transactionDate(), row.amount(), row.type()))
                    : consumeIfPresent(existingCounts.exact(),
                            duplicateKey(row.transactionDate(), row.amount(), row.merchantName(), row.type()));
            if (duplicate) {
                skippedCount++;
                continue;
            }

            Category category = resolveCategory(household, row.merchantName(), row.type(), classify, categoryCache);
            Transaction transaction = new Transaction(household, row.type(), row.amount(),
                    row.transactionDate(), category, card, uploader, row.merchantName());
            transactionRepository.save(transaction);
            total = total.add(row.amount());
            breakdown.computeIfAbsent(category.getId(), id -> new BreakdownAccumulator(category.getName()))
                    .add(row.amount());
        }

        int importedCount = parsed.size() - skippedCount;
        importBatchRepository.save(new ImportBatch(household, provider, card, checksum, importedCount, skippedCount));

        List<ImportResultResponse.CategoryBreakdown> categoryBreakdown = breakdown.entrySet().stream()
                .map(entry -> new ImportResultResponse.CategoryBreakdown(entry.getKey(), entry.getValue().name,
                        entry.getValue().count, entry.getValue().amount))
                .sorted((a, b) -> b.amount().compareTo(a.amount()))
                .toList();

        return new ImportResultResponse(importedCount, skippedCount, total, card.getId(), card.getName(),
                categoryBreakdown);
    }

    /** exact: 같은 카드·날짜·금액·메모·수입지출까지 완전히 같은 거래. loose: 메모는 무시하고 카드·
     * 날짜·금액·수입지출만 같은 거래 — 같은 지출이 다른 명세서엔 다른 표기의 메모로 이미 들어있을
     * 수 있는 행({@link ParsedTransaction#looseDuplicateCheck()}가 true인 행)에만 쓴다. */
    private record ExistingCounts(Map<String, Integer> exact, Map<String, Integer> loose) {
    }

    /**
     * 이미 저장된 거래 개수를 두 기준(exact/loose)으로 세어 둔다. 한 번 가져올 때 같은 조합이 여러
     * 건 있어도(예: 같은 날 같은 카페 두 번) 실제로 이미 들어간 건수만큼만 중복으로 건너뛰고, 그
     * 이상은 새 거래로 취급한다.
     */
    private ExistingCounts countExistingByKey(Long householdId, Long cardId, List<ParsedTransaction> parsed) {
        LocalDate from = parsed.stream().map(ParsedTransaction::transactionDate).min(Comparator.naturalOrder())
                .orElseThrow();
        LocalDate to = parsed.stream().map(ParsedTransaction::transactionDate).max(Comparator.naturalOrder())
                .orElseThrow();
        Map<String, Integer> exact = new LinkedHashMap<>();
        Map<String, Integer> loose = new LinkedHashMap<>();
        for (Transaction existing : transactionRepository
                .findByHouseholdIdAndCardIdAndTransactionDateBetween(householdId, cardId, from, to)) {
            exact.merge(duplicateKey(existing.getTransactionDate(), existing.getAmount(), existing.getMemo(),
                    existing.getType()), 1, Integer::sum);
            loose.merge(looseDuplicateKey(existing.getTransactionDate(), existing.getAmount(), existing.getType()),
                    1, Integer::sum);
        }
        return new ExistingCounts(exact, loose);
    }

    private boolean consumeIfPresent(Map<String, Integer> counts, String key) {
        int remaining = counts.getOrDefault(key, 0);
        if (remaining <= 0) {
            return false;
        }
        counts.put(key, remaining - 1);
        return true;
    }

    private String duplicateKey(LocalDate date, BigDecimal amount, String memo, TransactionType type) {
        return date + "|" + amount.stripTrailingZeros().toPlainString() + "|" + memo + "|" + type;
    }

    private String looseDuplicateKey(LocalDate date, BigDecimal amount, TransactionType type) {
        return date + "|" + amount.stripTrailingZeros().toPlainString() + "|" + type;
    }

    private static final class BreakdownAccumulator {
        private final String name;
        private int count;
        private BigDecimal amount = BigDecimal.ZERO;

        private BreakdownAccumulator(String name) {
            this.name = name;
        }

        private void add(BigDecimal delta) {
            count++;
            amount = amount.add(delta);
        }
    }

    /**
     * 분류기가 제안한 카테고리는 이 가계부에 이미 있는 이름일 때만 사용한다. 제안된 이름이 이
     * 가계부에 없으면(=아직 안 쓰는 카테고리) 새로 만들지 않고 미분류로 저장한다.
     */
    private Category resolveCategory(Household household, String merchantName, TransactionType type,
            Function<String, Optional<MerchantCategoryClassifier.CategorySuggestion>> classify,
            Map<String, Category> cache) {
        Optional<String> suggestedName = classify.apply(merchantName)
                .map(MerchantCategoryClassifier.CategorySuggestion::categoryName);
        if (suggestedName.isPresent()) {
            Category existing = findExistingCategory(household, suggestedName.get(), type, cache);
            if (existing != null) {
                return existing;
            }
        }
        return uncategorized(household, type, cache);
    }

    private Category findExistingCategory(Household household, String name, TransactionType type,
            Map<String, Category> cache) {
        String cacheKey = type + ":" + name;
        Category cached = cache.get(cacheKey);
        if (cached != null) {
            return cached;
        }
        Optional<Category> found = categoryRepository
                .findByHouseholdIdAndNameAndType(household.getId(), name, type)
                .stream()
                .findFirst();
        found.ifPresent(category -> cache.put(cacheKey, category));
        return found.orElse(null);
    }

    private Category uncategorized(Household household, TransactionType type, Map<String, Category> cache) {
        String cacheKey = type + ":" + UNCATEGORIZED_NAME;
        return cache.computeIfAbsent(cacheKey, key -> categoryRepository
                .findByHouseholdIdAndNameAndType(household.getId(), UNCATEGORIZED_NAME, type)
                .stream()
                .findFirst()
                .orElseGet(() -> categoryRepository
                        .save(new Category(household, UNCATEGORIZED_NAME, type, UNCATEGORIZED_COLOR,
                                UNCATEGORIZED_ICON))));
    }

    private Card resolveCard(Household household, Long cardId, String cardName, ImportProvider provider) {
        if (cardId != null) {
            return cardRepository.findByIdAndHouseholdId(cardId, household.getId())
                    .orElseThrow(() -> ApiException.badRequest("유효하지 않은 카드입니다."));
        }
        String name = (cardName == null || cardName.isBlank()) ? defaultCardName(provider) : cardName.trim();
        return cardRepository.findByHouseholdIdAndNameIgnoreCase(household.getId(), name)
                .orElseGet(() -> cardRepository.save(new Card(household, name, CardType.CREDIT, null)));
    }

    private String defaultCardName(ImportProvider provider) {
        return switch (provider) {
            case SAMSUNG_CARD -> "삼성카드";
            case GYEONGGI_LOCAL_CURRENCY -> "경기지역화폐";
            case COUPANG -> "쿠팡";
            case KBANK -> "케이뱅크";
        };
    }

    private byte[] readBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException e) {
            throw ApiException.badRequest("파일을 읽을 수 없습니다.");
        }
    }

    private String sha256(byte[] bytes) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(bytes));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
