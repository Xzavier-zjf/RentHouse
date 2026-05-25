package com.renthouse.houseservice.controller;

import com.renthouse.houseservice.model.House;
import com.renthouse.houseservice.model.Appointment;
import com.renthouse.houseservice.model.RentalApplication;
import com.renthouse.houseservice.service.HouseService;
import com.renthouse.houseservice.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/house")
public class HouseController {

    @Autowired
    private HouseService houseService;
    
    @Autowired
    private JwtUtil jwtUtil;

    // 搜索房源（无需登录）
    @GetMapping("/search")
    public ResponseEntity<?> searchHouses(@RequestParam(required = false) String location,
                                          @RequestParam(required = false) BigDecimal minPrice,
                                          @RequestParam(required = false) BigDecimal maxPrice,
                                          @RequestParam(required = false) String layout,
                                          @RequestParam(required = false) BigDecimal minArea,
                                          @RequestParam(required = false) BigDecimal maxArea) {
        List<House> houses = houseService.searchHouses(location, minPrice, maxPrice, layout, minArea, maxArea, "APPROVED");
        return ResponseEntity.ok(houses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getHouseDetail(@PathVariable Integer id) {
        House house = houseService.getPublicHouseDetail(id);
        if (house == null) {
            return ResponseEntity.badRequest().body("房源不存在");
        }
        return ResponseEntity.ok(house);
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
        
        String validationMessage = houseService.validateHouse(house);
        if (validationMessage != null) {
            return ResponseEntity.badRequest().body(validationMessage);
        }
        
        houseService.uploadHouse(house);
        return ResponseEntity.ok("房源上传成功，等待管理员审核");
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateHouse(HttpServletRequest request, @PathVariable Integer id, @RequestBody House house) {
        if (!hasRole(request, "LANDLORD")) {
            return ResponseEntity.status(403).body("权限不足，只有房东可以编辑房源");
        }
        Integer userId = getUserId(request);
        house.setId(id);
        String validationMessage = houseService.validateHouse(house);
        if (validationMessage != null) {
            return ResponseEntity.badRequest().body(validationMessage);
        }
        boolean updated = houseService.updateHouse(userId, house);
        if (!updated) {
            return ResponseEntity.status(403).body("房源不存在或无权编辑");
        }
        return ResponseEntity.ok("房源已更新，等待管理员重新审核");
    }

    @PostMapping("/{id}/offline")
    public ResponseEntity<?> offlineHouse(HttpServletRequest request, @PathVariable Integer id) {
        if (!hasRole(request, "LANDLORD")) {
            return ResponseEntity.status(403).body("权限不足，只有房东可以下架房源");
        }
        boolean updated = houseService.offlineHouse(getUserId(request), id);
        if (!updated) {
            return ResponseEntity.status(403).body("房源不存在或无权下架");
        }
        return ResponseEntity.ok("房源已下架");
    }

    // 房东查看自己的房源
    @GetMapping("/my-houses")
    public ResponseEntity<?> getMyHouses(HttpServletRequest request,
                                         @RequestParam(required = false) Integer page,
                                         @RequestParam(required = false) Integer size) {
        if (!hasRole(request, "LANDLORD")) {
            return ResponseEntity.status(403).body("权限不足，只有房东可以查看自己的房源");
        }
        
        String token = request.getHeader("Authorization").substring(7);
        Integer userId = jwtUtil.extractUserId(token);

        if (page == null || size == null) {
            List<House> houses = houseService.getMyHouses(userId);
            return ResponseEntity.ok(houses);
        }
        int safePage = safePage(page);
        int safeSize = safeSize(size);
        return ResponseEntity.ok(pageResult(
                houseService.getMyHouses(userId, safePage, safeSize),
                houseService.countMyHouses(userId),
                safePage,
                safeSize
        ));
    }

    // 管理员查看所有房源
    @GetMapping("/admin/houses")
    public ResponseEntity<?> getAllHouses(HttpServletRequest request,
                                          @RequestParam(required = false) String status,
                                          @RequestParam(required = false) Integer page,
                                          @RequestParam(required = false) Integer size) {
        if (!hasRole(request, "ADMIN")) {
            return ResponseEntity.status(403).body("权限不足");
        }

        if (page != null && size != null) {
            int safePage = safePage(page);
            int safeSize = safeSize(size);
            if (status != null && !status.isBlank()) {
                return ResponseEntity.ok(pageResult(
                        houseService.getHousesByStatus(status, safePage, safeSize),
                        houseService.countHousesByStatus(status),
                        safePage,
                        safeSize
                ));
            }
            return ResponseEntity.ok(pageResult(
                    houseService.getAllHouses(safePage, safeSize),
                    houseService.countAllHouses(),
                    safePage,
                    safeSize
            ));
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
        
        try {
            houseService.approveHouse(id, status, reason);
            return ResponseEntity.ok("房源审核状态已更新");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/favorite")
    public ResponseEntity<?> addFavorite(HttpServletRequest request, @PathVariable Integer id) {
        if (!hasRole(request, "USER")) {
            return ResponseEntity.status(403).body("只有普通用户可以收藏房源");
        }
        try {
            houseService.addFavorite(getUserId(request), id);
            return ResponseEntity.ok("收藏成功");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}/favorite")
    public ResponseEntity<?> removeFavorite(HttpServletRequest request, @PathVariable Integer id) {
        if (!hasRole(request, "USER")) {
            return ResponseEntity.status(403).body("只有普通用户可以取消收藏");
        }
        houseService.removeFavorite(getUserId(request), id);
        return ResponseEntity.ok("已取消收藏");
    }

    @GetMapping("/{id}/favorite")
    public ResponseEntity<?> isFavorite(HttpServletRequest request, @PathVariable Integer id) {
        if (!hasRole(request, "USER")) {
            return ResponseEntity.status(403).body("只有普通用户可以查看收藏状态");
        }
        return ResponseEntity.ok(Map.of("favorite", houseService.isFavorite(getUserId(request), id)));
    }

    @GetMapping("/favorites")
    public ResponseEntity<?> getFavorites(HttpServletRequest request) {
        if (!hasRole(request, "USER")) {
            return ResponseEntity.status(403).body("只有普通用户可以查看收藏");
        }
        return ResponseEntity.ok(houseService.getFavorites(getUserId(request)));
    }

    @PostMapping("/appointments")
    public ResponseEntity<?> createAppointment(HttpServletRequest request, @RequestBody Appointment appointment) {
        if (!hasRole(request, "USER")) {
            return ResponseEntity.status(403).body("只有普通用户可以预约看房");
        }
        try {
            houseService.createAppointment(getUserId(request), appointment);
            return ResponseEntity.ok("预约已提交，等待房东确认");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/appointments/my")
    public ResponseEntity<?> getMyAppointments(HttpServletRequest request,
                                               @RequestParam(required = false) Integer page,
                                               @RequestParam(required = false) Integer size) {
        if (!hasRole(request, "USER")) {
            return ResponseEntity.status(403).body("只有普通用户可以查看我的预约");
        }
        if (page != null && size != null) {
            int safePage = safePage(page);
            int safeSize = safeSize(size);
            Integer userId = getUserId(request);
            return ResponseEntity.ok(pageResult(
                    houseService.getMyAppointments(userId, safePage, safeSize),
                    houseService.countMyAppointments(userId),
                    safePage,
                    safeSize
            ));
        }
        return ResponseEntity.ok(houseService.getMyAppointments(getUserId(request)));
    }

    @GetMapping("/appointments/landlord")
    public ResponseEntity<?> getLandlordAppointments(HttpServletRequest request,
                                                     @RequestParam(required = false) Integer page,
                                                     @RequestParam(required = false) Integer size) {
        if (!hasRole(request, "LANDLORD")) {
            return ResponseEntity.status(403).body("只有房东可以查看预约管理");
        }
        if (page != null && size != null) {
            int safePage = safePage(page);
            int safeSize = safeSize(size);
            Integer landlordId = getUserId(request);
            return ResponseEntity.ok(pageResult(
                    houseService.getLandlordAppointments(landlordId, safePage, safeSize),
                    houseService.countLandlordAppointments(landlordId),
                    safePage,
                    safeSize
            ));
        }
        return ResponseEntity.ok(houseService.getLandlordAppointments(getUserId(request)));
    }

    @PostMapping("/appointments/{id}/status")
    public ResponseEntity<?> updateAppointmentStatus(HttpServletRequest request, @PathVariable Integer id,
                                                     @RequestParam String status,
                                                     @RequestParam(required = false) String reply) {
        if (!hasRole(request, "LANDLORD")) {
            return ResponseEntity.status(403).body("只有房东可以处理预约");
        }
        try {
            houseService.updateAppointmentStatus(getUserId(request), id, status, reply);
            return ResponseEntity.ok("预约状态已更新");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/appointments/{id}/cancel")
    public ResponseEntity<?> cancelAppointment(HttpServletRequest request, @PathVariable Integer id) {
        if (!hasRole(request, "USER")) {
            return ResponseEntity.status(403).body("只有普通用户可以取消预约");
        }
        houseService.cancelAppointment(getUserId(request), id);
        return ResponseEntity.ok("预约已取消");
    }

    @PostMapping("/rental-applications")
    public ResponseEntity<?> createRentalApplication(HttpServletRequest request, @RequestBody RentalApplication application) {
        if (!hasRole(request, "USER")) {
            return ResponseEntity.status(403).body("只有普通用户可以提交租赁申请");
        }
        try {
            houseService.createRentalApplication(getUserId(request), application);
            return ResponseEntity.ok("租赁申请已提交");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/rental-applications/my")
    public ResponseEntity<?> getMyRentalApplications(HttpServletRequest request,
                                                     @RequestParam(required = false) Integer page,
                                                     @RequestParam(required = false) Integer size) {
        if (!hasRole(request, "USER")) {
            return ResponseEntity.status(403).body("只有普通用户可以查看我的申请");
        }
        if (page != null && size != null) {
            int safePage = safePage(page);
            int safeSize = safeSize(size);
            Integer userId = getUserId(request);
            return ResponseEntity.ok(pageResult(
                    houseService.getMyRentalApplications(userId, safePage, safeSize),
                    houseService.countMyRentalApplications(userId),
                    safePage,
                    safeSize
            ));
        }
        return ResponseEntity.ok(houseService.getMyRentalApplications(getUserId(request)));
    }

    @GetMapping("/rental-applications/landlord")
    public ResponseEntity<?> getLandlordRentalApplications(HttpServletRequest request,
                                                           @RequestParam(required = false) Integer page,
                                                           @RequestParam(required = false) Integer size) {
        if (!hasRole(request, "LANDLORD")) {
            return ResponseEntity.status(403).body("只有房东可以查看申请管理");
        }
        if (page != null && size != null) {
            int safePage = safePage(page);
            int safeSize = safeSize(size);
            Integer landlordId = getUserId(request);
            return ResponseEntity.ok(pageResult(
                    houseService.getLandlordRentalApplications(landlordId, safePage, safeSize),
                    houseService.countLandlordRentalApplications(landlordId),
                    safePage,
                    safeSize
            ));
        }
        return ResponseEntity.ok(houseService.getLandlordRentalApplications(getUserId(request)));
    }

    @GetMapping("/admin/rental-applications")
    public ResponseEntity<?> getAllRentalApplications(HttpServletRequest request,
                                                      @RequestParam(required = false) Integer page,
                                                      @RequestParam(required = false) Integer size) {
        if (!hasRole(request, "ADMIN")) {
            return ResponseEntity.status(403).body("权限不足");
        }
        if (page != null && size != null) {
            int safePage = safePage(page);
            int safeSize = safeSize(size);
            return ResponseEntity.ok(pageResult(
                    houseService.getAllRentalApplications(safePage, safeSize),
                    houseService.countAllRentalApplications(),
                    safePage,
                    safeSize
            ));
        }
        return ResponseEntity.ok(houseService.getAllRentalApplications());
    }

    @PostMapping("/rental-applications/{id}/status")
    public ResponseEntity<?> updateRentalApplicationStatus(HttpServletRequest request, @PathVariable Integer id,
                                                           @RequestParam String status,
                                                           @RequestParam(required = false) String reply) {
        if (!hasRole(request, "LANDLORD")) {
            return ResponseEntity.status(403).body("只有房东可以处理租赁申请");
        }
        try {
            houseService.updateRentalApplicationStatus(getUserId(request), id, status, reply);
            return ResponseEntity.ok("租赁申请状态已更新");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/rental-applications/{id}/cancel")
    public ResponseEntity<?> cancelRentalApplication(HttpServletRequest request, @PathVariable Integer id) {
        if (!hasRole(request, "USER")) {
            return ResponseEntity.status(403).body("只有普通用户可以取消租赁申请");
        }
        houseService.cancelRentalApplication(getUserId(request), id);
        return ResponseEntity.ok("租赁申请已取消");
    }

    @GetMapping("/admin/statistics")
    public ResponseEntity<?> statistics(HttpServletRequest request) {
        if (!hasRole(request, "ADMIN")) {
            return ResponseEntity.status(403).body("权限不足");
        }
        return ResponseEntity.ok(houseService.statistics());
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
        String token = request.getHeader("Authorization").substring(7);
        return jwtUtil.extractUserId(token);
    }

    private int safePage(Integer page) {
        return Math.max(page == null ? 1 : page, 1);
    }

    private int safeSize(Integer size) {
        return Math.min(Math.max(size == null ? 10 : size, 1), 100);
    }

    private Map<String, Object> pageResult(Object items, Integer total, Integer page, Integer size) {
        return Map.of("items", items, "total", total, "page", page, "size", size);
    }
}
