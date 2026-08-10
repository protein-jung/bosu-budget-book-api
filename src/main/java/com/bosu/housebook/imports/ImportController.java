package com.bosu.housebook.imports;

import com.bosu.housebook.auth.CurrentUserId;
import com.bosu.housebook.imports.dto.ImportResultResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/imports")
public class ImportController {

    private final ImportService importService;

    public ImportController(ImportService importService) {
        this.importService = importService;
    }

    @PostMapping
    public ImportResultResponse importStatement(
            @CurrentUserId Long userId,
            @RequestParam ImportProvider provider,
            @RequestParam(required = false) Long cardId,
            @RequestParam(required = false) String cardName,
            @RequestPart MultipartFile file) {
        return importService.importStatement(userId, provider, cardId, cardName, file);
    }
}
