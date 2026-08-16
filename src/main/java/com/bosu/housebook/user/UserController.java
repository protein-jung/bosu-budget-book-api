package com.bosu.housebook.user;

import com.bosu.housebook.auth.CurrentUserId;
import com.bosu.housebook.user.dto.AccountDeleteRequest;
import com.bosu.housebook.user.dto.PasswordChangeRequest;
import com.bosu.housebook.user.dto.UserResponse;
import com.bosu.housebook.user.dto.UserUpdateRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public UserResponse getMe(@CurrentUserId Long userId) {
        return userService.getMe(userId);
    }

    @PatchMapping("/me")
    public UserResponse updateMe(@CurrentUserId Long userId, @Valid @RequestBody UserUpdateRequest request) {
        return userService.updateMe(userId, request);
    }

    @PutMapping("/me/password")
    public ResponseEntity<Void> changePassword(@CurrentUserId Long userId,
            @Valid @RequestBody PasswordChangeRequest request) {
        userService.changePassword(userId, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMe(@CurrentUserId Long userId, @Valid @RequestBody AccountDeleteRequest request) {
        userService.deleteMe(userId, request);
        return ResponseEntity.noContent().build();
    }
}
