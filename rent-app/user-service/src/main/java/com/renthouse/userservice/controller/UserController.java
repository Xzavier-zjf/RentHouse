package com.renthouse.userservice.controller;

import com.renthouse.userservice.model.User;
import com.renthouse.userservice.service.ImageStorageService;
import com.renthouse.userservice.service.UserService;
import com.renthouse.userservice.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private ImageStorageService imageStorageService;

    // 用户注册
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        if (userService.findByUsername(user.getUsername()) != null) {
            return ResponseEntity.badRequest().body("用户名已存在");
        }
        
        // 确保新用户角色设置为USER
        user.setRole("USER");
        user.setLandlordApplyStatus("NOT_APPLIED");
        System.out.println("注册新用户 - 用户名: " + user.getUsername() + ", 角色: " + user.getRole());
        
        userService.registerUser(user);
        return ResponseEntity.ok("注册成功");
    }

    // 用户登录
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User loginUser) {
        User user = userService.findByUsername(loginUser.getUsername());
        if (user == null || !userService.checkPassword(loginUser.getPassword(), user.getPassword())) {
            return ResponseEntity.badRequest().body("用户名或密码错误");
        }
        
        System.out.println("登录用户信息 - 用户名: " + user.getUsername() + ", 角色: " + user.getRole() + ", ID: " + user.getId());
        
        // 确保角色不为null
        String userRole = user.getRole();
        if (userRole == null) {
            System.out.println("警告：用户角色为null，设置为默认USER");
            userRole = "USER";
            user.setRole("USER");
            userService.updateUser(user); // 更新数据库中的角色
        }
        
        String token = jwtUtil.generateToken(user.getUsername(), userRole, user.getId());
        System.out.println("生成的JWT令牌: " + token);
        return ResponseEntity.ok(new LoginResponse(token, userRole));
    }

    // 申请成为房东
    @PostMapping("/apply-landlord")
    public ResponseEntity<?> applyLandlord(HttpServletRequest request) {
        System.out.println("收到房东申请请求");
        
        String token = request.getHeader("Authorization");
        System.out.println("Authorization头: " + token);
        
        if (token == null || !token.startsWith("Bearer ")) {
            System.out.println("Authorization头格式错误");
            return ResponseEntity.status(401).body("未授权");
        }
        
        token = token.substring(7);
        System.out.println("提取的令牌: " + token);
        
        if (!jwtUtil.isTokenValid(token)) {
            System.out.println("JWT令牌验证失败");
            return ResponseEntity.status(401).body("令牌无效");
        }
        
        String username = jwtUtil.extractUsername(token);
        System.out.println("从令牌提取的用户名: " + username);
        
        User user = userService.findByUsername(username);
        if (user == null) {
            System.out.println("数据库中找不到用户: " + username);
            return ResponseEntity.status(401).body("用户不存在");
        }
        
        // 修复数据库中角色为null的问题
        if (user.getRole() == null) {
            System.out.println("发现用户角色为null，修复为USER");
            user.setRole("USER");
            if (user.getLandlordApplyStatus() == null) {
                user.setLandlordApplyStatus("NOT_APPLIED");
            }
            userService.updateUser(user);
            System.out.println("用户角色已修复 - 用户名: " + username + ", 新角色: " + user.getRole());
        }
        
        System.out.println("用户角色检查 - 用户名: " + username + ", 角色: " + user.getRole() + ", 申请状态: " + user.getLandlordApplyStatus());
        
        // 检查是否已经申请过
        if ("PENDING".equals(user.getLandlordApplyStatus())) {
            return ResponseEntity.badRequest().body("您已经提交过申请，请等待管理员审核");
        }
        if ("APPROVED".equals(user.getLandlordApplyStatus()) || "LANDLORD".equals(user.getRole())) {
            return ResponseEntity.badRequest().body("您已经是房东了");
        }
        
        if (user.getRole() == null || !"USER".equals(user.getRole())) {
            System.out.println("角色检查失败 - 期望: USER, 实际: " + user.getRole());
            return ResponseEntity.badRequest().body("只有普通用户可以申请成为房东，当前角色: " + user.getRole());
        }
        
        System.out.println("开始处理房东申请...");
        userService.applyForLandlord(user.getId());
        System.out.println("房东申请处理完成");
        return ResponseEntity.ok("申请已提交，等待管理员审核");
    }

    // 管理员审核房东申请
    @PostMapping("/admin/approve-landlord")
    public ResponseEntity<?> approveLandlord(HttpServletRequest request, @RequestParam Integer userId) {
        if (!hasRole(request, "ADMIN")) {
            return ResponseEntity.status(403).body("权限不足");
        }
        
        User user = userService.findById(userId);
        if (user == null) {
            return ResponseEntity.badRequest().body("用户不存在");
        }
        
        userService.approveLandlord(userId);
        return ResponseEntity.ok("房东申请已批准");
    }

    @PostMapping("/admin/reject-landlord")
    public ResponseEntity<?> rejectLandlord(HttpServletRequest request,
                                            @RequestParam Integer userId,
                                            @RequestParam(required = false) String reason) {
        if (!hasRole(request, "ADMIN")) {
            return ResponseEntity.status(403).body("权限不足");
        }

        User user = userService.findById(userId);
        if (user == null) {
            return ResponseEntity.badRequest().body("用户不存在");
        }

        userService.rejectLandlord(userId, reason);
        return ResponseEntity.ok("房东申请已拒绝");
    }

    // 获取个人信息
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(HttpServletRequest request) {
        System.out.println("收到获取个人信息请求");
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            System.out.println("Authorization头缺失或格式错误: " + token);
            return ResponseEntity.status(401).body("未授权");
        }
        
        token = token.substring(7);
        if (!jwtUtil.isTokenValid(token)) {
            System.out.println("JWT令牌无效");
            return ResponseEntity.status(401).body("令牌无效");
        }
        
        String username = jwtUtil.extractUsername(token);
        System.out.println("从JWT提取的用户名: " + username);
        User user = userService.findByUsername(username);
        if (user == null) {
            System.out.println("用户不存在: " + username);
            return ResponseEntity.status(401).body("用户不存在");
        }
        
        System.out.println("返回用户信息: " + user.getUsername() + ", 角色: " + user.getRole());
        return ResponseEntity.ok(new UserProfileResponse(user));
    }

    // 更新个人信息
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(HttpServletRequest request, @RequestBody UpdateProfileRequest updateRequest) {
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("未授权");
        }
        
        token = token.substring(7);
        if (!jwtUtil.isTokenValid(token)) {
            return ResponseEntity.status(401).body("令牌无效");
        }
        
        String username = jwtUtil.extractUsername(token);
        User user = userService.findByUsername(username);
        if (user == null) {
            return ResponseEntity.status(401).body("用户不存在");
        }
        
        // 更新邮箱
        if (updateRequest.getEmail() != null && !updateRequest.getEmail().trim().isEmpty()) {
            user.setEmail(updateRequest.getEmail());
        }
        
        userService.updateUser(user);
        return ResponseEntity.ok("个人信息更新成功");
    }

    @PostMapping("/avatar")
    public ResponseEntity<?> uploadAvatar(HttpServletRequest request, @RequestParam("file") MultipartFile file) {
        Integer userId = getUserId(request);
        if (userId == null) {
            return ResponseEntity.status(401).body("未授权");
        }
        User user = userService.findById(userId);
        if (user == null) {
            return ResponseEntity.status(401).body("用户不存在");
        }
        try {
            String fileId = imageStorageService.replaceUserAvatar(userId, user.getAvatarFileId(), file);
            userService.updateAvatarFileId(userId, fileId);
            return ResponseEntity.ok(Map.of(
                    "fileId", fileId,
                    "avatarUrl", "/api/user/avatar/" + fileId
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("头像上传失败");
        }
    }

    @GetMapping("/avatar/{fileId}")
    public ResponseEntity<?> getAvatar(@PathVariable String fileId) {
        try {
            ImageStorageService.StoredImage image = imageStorageService.read(fileId);
            if (image == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(image.contentType()))
                    .body(image.data());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("图片ID无效");
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("图片读取失败");
        }
    }

    @PutMapping("/password")
    public ResponseEntity<?> changePassword(HttpServletRequest request, @RequestBody ChangePasswordRequest passwordRequest) {
        Integer userId = getUserId(request);
        if (userId == null) {
            return ResponseEntity.status(401).body("未授权");
        }
        if (passwordRequest.getOldPassword() == null || passwordRequest.getOldPassword().isBlank()
                || passwordRequest.getNewPassword() == null || passwordRequest.getNewPassword().length() < 6) {
            return ResponseEntity.badRequest().body("请输入旧密码，新密码长度至少6位");
        }
        boolean changed = userService.changePassword(userId, passwordRequest.getOldPassword(), passwordRequest.getNewPassword());
        if (!changed) {
            return ResponseEntity.badRequest().body("旧密码错误");
        }
        return ResponseEntity.ok("密码修改成功");
    }

    // 管理员查看所有用户
    @GetMapping("/admin/users")
    public ResponseEntity<?> getAllUsers(HttpServletRequest request,
                                         @RequestParam(required = false) Integer page,
                                         @RequestParam(required = false) Integer size) {
        if (!hasRole(request, "ADMIN")) {
            return ResponseEntity.status(403).body("权限不足");
        }

        if (page == null || size == null) {
            List<User> users = userService.findAllUsers();
            return ResponseEntity.ok(users.stream().map(UserProfileResponse::new).toList());
        }

        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(size, 1), 100);
        List<UserProfileResponse> users = userService.findUsersPage(safePage, safeSize)
                .stream()
                .map(UserProfileResponse::new)
                .toList();
        return ResponseEntity.ok(Map.of(
                "items", users,
                "total", userService.countUsers(),
                "page", safePage,
                "size", safeSize
        ));
    }

    @GetMapping("/notifications")
    public ResponseEntity<?> getNotifications(HttpServletRequest request,
                                              @RequestParam(defaultValue = "1") Integer page,
                                              @RequestParam(defaultValue = "10") Integer size) {
        Integer userId = getUserId(request);
        if (userId == null) {
            return ResponseEntity.status(401).body("未授权");
        }

        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(size, 1), 100);
        return ResponseEntity.ok(Map.of(
                "items", userService.findNotifications(userId, safePage, safeSize),
                "total", userService.countNotifications(userId),
                "page", safePage,
                "size", safeSize
        ));
    }

    @PostMapping("/notifications/{id}/read")
    public ResponseEntity<?> markNotificationRead(HttpServletRequest request, @PathVariable Integer id) {
        Integer userId = getUserId(request);
        if (userId == null) {
            return ResponseEntity.status(401).body("未授权");
        }
        userService.markNotificationRead(userId, id);
        return ResponseEntity.ok("通知已读");
    }

    // 检查用户角色
    private boolean hasRole(HttpServletRequest request, String requiredRole) {
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            return false;
        }
        
        token = token.substring(7);
        if (!jwtUtil.isTokenValid(token)) {
            return false;
        }
        
        String role = jwtUtil.extractRole(token);
        return requiredRole.equals(role);
    }

    private Integer getUserId(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            return null;
        }
        token = token.substring(7);
        if (!jwtUtil.isTokenValid(token)) {
            return null;
        }
        return jwtUtil.extractUserId(token);
    }
    
    // 登录响应类
    public static class LoginResponse {
        private String token;
        private String role;
        
        public LoginResponse(String token, String role) {
            this.token = token;
            this.role = role;
        }
        
        public String getToken() {
            return token;
        }
        
        public void setToken(String token) {
            this.token = token;
        }
        
        public String getRole() {
            return role;
        }
        
        public void setRole(String role) {
            this.role = role;
        }
    }
    
    // 用户个人信息响应类
    public static class UserProfileResponse {
        private Integer id;
        private String username;
        private String email;
        private String role;
        private String avatarUrl;
        private String landlordApplyStatus;
        private String landlordApplyReason;
        
        public UserProfileResponse(User user) {
            this.id = user.getId();
            this.username = user.getUsername();
            this.email = user.getEmail();
            this.role = user.getRole();
            this.avatarUrl = user.getAvatarFileId() == null || user.getAvatarFileId().isBlank() ? null : "/api/user/avatar/" + user.getAvatarFileId();
            this.landlordApplyStatus = user.getLandlordApplyStatus();
            this.landlordApplyReason = user.getLandlordApplyReason();
        }
        
        // Getters and Setters
        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }

        public String getAvatarUrl() { return avatarUrl; }
        public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
        
        public String getLandlordApplyStatus() { return landlordApplyStatus; }
        public void setLandlordApplyStatus(String landlordApplyStatus) { this.landlordApplyStatus = landlordApplyStatus; }

        public String getLandlordApplyReason() { return landlordApplyReason; }
        public void setLandlordApplyReason(String landlordApplyReason) { this.landlordApplyReason = landlordApplyReason; }
    }
    
    // 更新个人信息请求类
    public static class UpdateProfileRequest {
        private String email;
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }

    public static class ChangePasswordRequest {
        private String oldPassword;
        private String newPassword;

        public String getOldPassword() { return oldPassword; }
        public void setOldPassword(String oldPassword) { this.oldPassword = oldPassword; }

        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    }
}
