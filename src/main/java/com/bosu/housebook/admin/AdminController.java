package com.bosu.housebook.admin;

import com.bosu.housebook.admin.dto.AdminHouseholdResponse;
import com.bosu.housebook.admin.dto.AdminStatsResponse;
import com.bosu.housebook.admin.dto.AdminUserResponse;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/stats")
    public AdminStatsResponse stats() {
        return adminService.stats();
    }

    @GetMapping("/users")
    public List<AdminUserResponse> users() {
        return adminService.listUsers();
    }

    @GetMapping("/households")
    public List<AdminHouseholdResponse> households() {
        return adminService.listHouseholds();
    }

    @PostMapping("/users/{userId}/block")
    public ResponseEntity<Void> block(@PathVariable Long userId) {
        adminService.setBlocked(userId, true);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/users/{userId}/unblock")
    public ResponseEntity<Void> unblock(@PathVariable Long userId) {
        adminService.setBlocked(userId, false);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        adminService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}
