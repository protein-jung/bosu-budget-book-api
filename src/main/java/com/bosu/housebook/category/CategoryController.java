package com.bosu.housebook.category;

import com.bosu.housebook.auth.CurrentUserId;
import com.bosu.housebook.category.dto.CategoryMonthlyTargetRequest;
import com.bosu.housebook.category.dto.CategoryReorderRequest;
import com.bosu.housebook.category.dto.CategoryRequest;
import com.bosu.housebook.category.dto.CategoryResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public List<CategoryResponse> getAll(@CurrentUserId Long userId) {
        return categoryService.getAll(userId);
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> create(@CurrentUserId Long userId,
            @Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.create(userId, request));
    }

    @PutMapping("/{categoryId}")
    public CategoryResponse update(@CurrentUserId Long userId, @PathVariable Long categoryId,
            @Valid @RequestBody CategoryRequest request) {
        return categoryService.update(userId, categoryId, request);
    }

    @PutMapping("/reorder")
    public ResponseEntity<Void> reorder(@CurrentUserId Long userId,
            @Valid @RequestBody CategoryReorderRequest request) {
        categoryService.reorder(userId, request.categoryIds());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{categoryId}/monthly-target/{year}/{month}")
    public ResponseEntity<Void> setMonthlyTarget(@CurrentUserId Long userId, @PathVariable Long categoryId,
            @PathVariable int year, @PathVariable int month,
            @Valid @RequestBody CategoryMonthlyTargetRequest request) {
        categoryService.setMonthlyTarget(userId, categoryId, year, month, request.amount());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{categoryId}/monthly-target/{year}/{month}")
    public ResponseEntity<Void> clearMonthlyTarget(@CurrentUserId Long userId, @PathVariable Long categoryId,
            @PathVariable int year, @PathVariable int month) {
        categoryService.clearMonthlyTarget(userId, categoryId, year, month);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{categoryId}")
    public ResponseEntity<Void> delete(@CurrentUserId Long userId, @PathVariable Long categoryId) {
        categoryService.delete(userId, categoryId);
        return ResponseEntity.noContent().build();
    }
}
