package com.bosu.housebook.category;

import com.bosu.housebook.auth.CurrentUserId;
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

    @DeleteMapping("/{categoryId}")
    public ResponseEntity<Void> delete(@CurrentUserId Long userId, @PathVariable Long categoryId) {
        categoryService.delete(userId, categoryId);
        return ResponseEntity.noContent().build();
    }
}
