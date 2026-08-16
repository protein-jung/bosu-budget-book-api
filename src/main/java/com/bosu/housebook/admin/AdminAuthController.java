package com.bosu.housebook.admin;

import com.bosu.housebook.admin.dto.AdminLoginRequest;
import com.bosu.housebook.admin.dto.AdminTokenResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/auth")
public class AdminAuthController {

    private final AdminService adminService;

    public AdminAuthController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/login")
    public AdminTokenResponse login(@Valid @RequestBody AdminLoginRequest request) {
        return new AdminTokenResponse(adminService.login(request));
    }
}
