package com.renthouse.houseservice.controller;

import com.renthouse.houseservice.model.House;
import com.renthouse.houseservice.service.HouseService;
import com.renthouse.houseservice.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/house")
public class HouseController {

    @Autowired
    private HouseService houseService;
    
    @Autowired
    private JwtUtil jwtUtil;

    // 搜索房源（无需登录）
    @GetMapping("/search")
    public ResponseEntity<?> searchHouses(@RequestParam String location) {
        List<House> houses = houseService.searchHouses(location);
        return ResponseEntity.ok(houses);
    }

    // 房东上传房源
    @PostMapping("/upload")
    public ResponseEntity<?> uploadHouse(HttpServletRequest request, @RequestBody House house) {
        if (!hasRole(request, "LANDLORD")) {
            return ResponseEntity.status(403).body("权限不足，只有房东可以上传房源");
        }
        
        String token = request.getHeader("Authorization").substring(7);
        Integer userId = jwtUtil.extractUserId(token);
        house.setUserId(userId);
        house.setStatus("PENDING"); // 确保状态正确设置
        
        // 验证必填字段
        if (house.getTitle() == null || house.getTitle().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("房源标题不能为空");
        }
        if (house.getPrice() == null || house.getPrice().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            return ResponseEntity.badRequest().body("房源价格必须大于0");
        }
        if (house.getLocation() == null || house.getLocation().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("房源位置不能为空");
        }
        
        houseService.uploadHouse(house);
        return ResponseEntity.ok("房源上传成功，等待管理员审核");
    }

    // 房东查看自己的房源
    @GetMapping("/my-houses")
    public ResponseEntity<?> getMyHouses(HttpServletRequest request) {
        if (!hasRole(request, "LANDLORD")) {
            return ResponseEntity.status(403).body("权限不足，只有房东可以查看自己的房源");
        }
        
        String token = request.getHeader("Authorization").substring(7);
        Integer userId = jwtUtil.extractUserId(token);
        
        List<House> houses = houseService.getMyHouses(userId);
        return ResponseEntity.ok(houses);
    }

    // 管理员查看所有房源
    @GetMapping("/admin/houses")
    public ResponseEntity<?> getAllHouses(HttpServletRequest request, @RequestParam(required = false) String status) {
        if (!hasRole(request, "ADMIN")) {
            return ResponseEntity.status(403).body("权限不足");
        }
        
        List<House> houses;
        if (status != null) {
            houses = houseService.getHousesByStatus(status);
        } else {
            houses = houseService.getAllHouses();
        }
        return ResponseEntity.ok(houses);
    }

    // 管理员审核房源
    @PostMapping("/admin/approve-house")
    public ResponseEntity<?> approveHouse(HttpServletRequest request, 
                                          @RequestParam Integer id,
                                          @RequestParam String status,
                                          @RequestParam(required = false) String reason) {
        if (!hasRole(request, "ADMIN")) {
            return ResponseEntity.status(403).body("权限不足");
        }
        
        houseService.approveHouse(id, status, reason);
        return ResponseEntity.ok("房源审核状态已更新");
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
}