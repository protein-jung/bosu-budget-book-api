package com.bosu.housebook.imports;

import com.bosu.housebook.card.Card;
import com.bosu.housebook.household.Household;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "import_batches")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ImportBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "household_id", nullable = false)
    private Household household;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ImportProvider provider;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id")
    private Card card;

    @Column(name = "file_checksum", nullable = false)
    private String fileChecksum;

    @Column(name = "imported_count", nullable = false)
    private int importedCount;

    @Column(name = "skipped_count", nullable = false)
    private int skippedCount;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public ImportBatch(Household household, ImportProvider provider, Card card, String fileChecksum,
            int importedCount, int skippedCount) {
        this.household = household;
        this.provider = provider;
        this.card = card;
        this.fileChecksum = fileChecksum;
        this.importedCount = importedCount;
        this.skippedCount = skippedCount;
        this.createdAt = LocalDateTime.now();
    }
}
