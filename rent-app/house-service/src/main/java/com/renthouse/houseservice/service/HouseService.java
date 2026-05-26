package com.renthouse.houseservice.service;

import com.renthouse.houseservice.model.House;
import com.renthouse.houseservice.model.HouseImage;
import com.renthouse.houseservice.model.Favorite;
import com.renthouse.houseservice.model.Appointment;
import com.renthouse.houseservice.model.RentalApplication;
import com.renthouse.houseservice.repository.AppointmentRepository;
import com.renthouse.houseservice.repository.FavoriteRepository;
import com.renthouse.houseservice.repository.HouseRepository;
import com.renthouse.houseservice.repository.NotificationRepository;
import com.renthouse.houseservice.repository.RentalApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Set;
import java.util.List;
import java.util.Map;

@Service
public class HouseService {
    private static final Set<String> HOUSE_REVIEW_STATUSES = Set.of("APPROVED", "REJECTED");
    private static final Set<String> APPOINTMENT_STATUSES = Set.of("CONFIRMED", "CANCELLED", "FINISHED");
    private static final Set<String> RENTAL_APPLICATION_STATUSES = Set.of("APPROVED", "REJECTED");

    @Autowired
    private HouseRepository houseRepository;
    @Autowired
    private FavoriteRepository favoriteRepository;
    @Autowired
    private AppointmentRepository appointmentRepository;
    @Autowired
    private RentalApplicationRepository rentalApplicationRepository;
    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private HouseImageStorageService houseImageStorageService;

    public Integer uploadHouse(House house) {
        if (house.getStatus() == null) {
            house.setStatus("PENDING");
        }
        if (house.getRentStatus() == null) {
            house.setRentStatus("AVAILABLE");
        }
        return houseRepository.save(house);
    }

    public List<House> searchHouses(String location) {
        return enrichHouses(houseRepository.findByLocationAndStatus(location, "APPROVED"));
    }

    public List<House> searchHouses(String location, BigDecimal minPrice, BigDecimal maxPrice, String layout,
                                    BigDecimal minArea, BigDecimal maxArea, String status) {
        return enrichHouses(houseRepository.search(location, minPrice, maxPrice, layout, minArea, maxArea, status));
    }

    public House getHouseDetail(Integer id) {
        return enrichHouse(houseRepository.findById(id));
    }

    public House getPublicHouseDetail(Integer id) {
        House house = houseRepository.findById(id);
        if (house == null || !"APPROVED".equals(house.getStatus())) {
            return null;
        }
        return enrichHouse(house);
    }

    public List<House> getMyHouses(Integer userId) {
        return enrichHouses(houseRepository.findByUserId(userId));
    }

    public List<House> getMyHouses(Integer userId, Integer page, Integer size) {
        return enrichHouses(houseRepository.findByUserId(userId, (page - 1) * size, size));
    }

    public int countMyHouses(Integer userId) {
        return houseRepository.countByUserId(userId);
    }

    public List<House> getHousesByStatus(String status) {
        return enrichHouses(houseRepository.findByStatus(status));
    }

    public List<House> getHousesByStatus(String status, Integer page, Integer size) {
        return enrichHouses(houseRepository.findByStatus(status, (page - 1) * size, size));
    }

    public int countHousesByStatus(String status) {
        return houseRepository.countByStatus(status);
    }

    public List<House> getAllHouses() {
        return enrichHouses(houseRepository.findAll());
    }

    public List<House> getAllHouses(Integer page, Integer size) {
        return enrichHouses(houseRepository.findAll((page - 1) * size, size));
    }

    public int countAllHouses() {
        return houseRepository.countAll();
    }

    public void approveHouse(Integer id, String status, String reason) {
        if (!HOUSE_REVIEW_STATUSES.contains(status)) {
            throw new IllegalArgumentException("不支持的房源状态");
        }
        House house = houseRepository.findById(id);
        if (house == null) {
            throw new IllegalArgumentException("房源不存在");
        }
        if (!"PENDING".equals(house.getStatus())) {
            throw new IllegalArgumentException("只能审核待审核房源");
        }
        houseRepository.updateStatus(id, status, reason);
        String title = "APPROVED".equals(status) ? "房源审核已通过" : "房源审核未通过";
        String content = "APPROVED".equals(status)
                ? "您的房源《" + house.getTitle() + "》已通过审核。"
                : "您的房源《" + house.getTitle() + "》未通过审核，原因：" + (reason == null || reason.isBlank() ? "不符合发布要求" : reason);
        notificationRepository.create(house.getUserId(), title, content, "HOUSE_REVIEW");
    }

    public String validateHouse(House house) {
        if (house.getTitle() == null || house.getTitle().trim().isEmpty()) {
            return "房源标题不能为空";
        }
        if (house.getPrice() == null || house.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            return "房源价格必须大于0";
        }
        if (house.getLocation() == null || house.getLocation().trim().isEmpty()) {
            return "房源位置不能为空";
        }
        return null;
    }

    public boolean updateHouse(Integer userId, House house) {
        House existing = houseRepository.findById(house.getId());
        if (existing == null || !userId.equals(existing.getUserId())) {
            return false;
        }
        house.setUserId(userId);
        houseRepository.update(house);
        return true;
    }

    public boolean offlineHouse(Integer userId, Integer houseId) {
        House existing = houseRepository.findById(houseId);
        if (existing == null || !userId.equals(existing.getUserId())) {
            return false;
        }
        houseRepository.offline(houseId, userId);
        return true;
    }

    public void addFavorite(Integer userId, Integer houseId) {
        House house = requireApprovedHouse(houseId);
        favoriteRepository.add(userId, house.getId());
    }

    public void removeFavorite(Integer userId, Integer houseId) {
        favoriteRepository.remove(userId, houseId);
    }

    public boolean isFavorite(Integer userId, Integer houseId) {
        return favoriteRepository.exists(userId, houseId);
    }

    public List<Favorite> getFavorites(Integer userId) {
        List<Favorite> favorites = favoriteRepository.findByUserId(userId);
        favorites.forEach(favorite -> enrichHouse(favorite.getHouse()));
        return favorites;
    }

    public void createAppointment(Integer userId, Appointment appointment) {
        House house = requireAvailableApprovedHouse(appointment.getHouseId());
        if (appointmentRepository.hasActiveAppointment(userId, appointment.getHouseId())) {
            throw new IllegalArgumentException("您已提交过该房源的有效预约");
        }
        appointment.setUserId(userId);
        appointment.setLandlordId(house.getUserId());
        appointmentRepository.save(appointment);
        notificationRepository.create(house.getUserId(), "收到新的看房预约", "房源《" + house.getTitle() + "》收到新的看房预约，请及时处理。", "APPOINTMENT_CREATED");
    }

    public List<Appointment> getMyAppointments(Integer userId) {
        return appointmentRepository.findByUserId(userId);
    }

    public List<Appointment> getMyAppointments(Integer userId, Integer page, Integer size) {
        return appointmentRepository.findByUserId(userId, (page - 1) * size, size);
    }

    public int countMyAppointments(Integer userId) {
        return appointmentRepository.countByUserId(userId);
    }

    public List<Appointment> getLandlordAppointments(Integer landlordId) {
        return appointmentRepository.findByLandlordId(landlordId);
    }

    public List<Appointment> getLandlordAppointments(Integer landlordId, Integer page, Integer size) {
        return appointmentRepository.findByLandlordId(landlordId, (page - 1) * size, size);
    }

    public int countLandlordAppointments(Integer landlordId) {
        return appointmentRepository.countByLandlordId(landlordId);
    }

    public void updateAppointmentStatus(Integer landlordId, Integer id, String status, String reply) {
        if (!APPOINTMENT_STATUSES.contains(status)) {
            throw new IllegalArgumentException("不支持的预约状态");
        }
        int updated;
        if ("CONFIRMED".equals(status)) {
            updated = appointmentRepository.confirm(id, landlordId, reply);
        } else if ("CANCELLED".equals(status)) {
            updated = appointmentRepository.reject(id, landlordId, reply);
        } else {
            updated = appointmentRepository.finish(id, landlordId, reply);
        }
        if (updated == 0) {
            throw new IllegalArgumentException("预约不存在、无权处理或当前状态不允许该操作");
        }
        // 查询列表成本较低，课程项目中用于通知目标用户。
        appointmentRepository.findByLandlordId(landlordId).stream()
                .filter(item -> id.equals(item.getId()))
                .findFirst()
                .ifPresent(item -> notificationRepository.create(item.getUserId(), "看房预约状态更新",
                        "您对房源《" + item.getHouse().getTitle() + "》的预约状态已更新为：" + status, "APPOINTMENT_UPDATED"));
    }

    public void cancelAppointment(Integer userId, Integer id) {
        appointmentRepository.cancel(id, userId);
    }

    public void createRentalApplication(Integer userId, RentalApplication application) {
        House house = requireAvailableApprovedHouse(application.getHouseId());
        if (rentalApplicationRepository.hasPendingApplication(userId, application.getHouseId())) {
            throw new IllegalArgumentException("您已提交过该房源的待处理租赁申请");
        }
        application.setUserId(userId);
        application.setLandlordId(house.getUserId());
        rentalApplicationRepository.save(application);
        notificationRepository.create(house.getUserId(), "收到新的租赁申请", "房源《" + house.getTitle() + "》收到新的租赁申请，请及时处理。", "RENTAL_APPLICATION_CREATED");
    }

    public List<RentalApplication> getMyRentalApplications(Integer userId) {
        return rentalApplicationRepository.findByUserId(userId);
    }

    public List<RentalApplication> getMyRentalApplications(Integer userId, Integer page, Integer size) {
        return rentalApplicationRepository.findByUserId(userId, (page - 1) * size, size);
    }

    public int countMyRentalApplications(Integer userId) {
        return rentalApplicationRepository.countByUserId(userId);
    }

    public List<RentalApplication> getLandlordRentalApplications(Integer landlordId) {
        return rentalApplicationRepository.findByLandlordId(landlordId);
    }

    public List<RentalApplication> getLandlordRentalApplications(Integer landlordId, Integer page, Integer size) {
        return rentalApplicationRepository.findByLandlordId(landlordId, (page - 1) * size, size);
    }

    public int countLandlordRentalApplications(Integer landlordId) {
        return rentalApplicationRepository.countByLandlordId(landlordId);
    }

    public List<RentalApplication> getAllRentalApplications() {
        return rentalApplicationRepository.findAll();
    }

    public List<RentalApplication> getAllRentalApplications(Integer page, Integer size) {
        return rentalApplicationRepository.findAll((page - 1) * size, size);
    }

    public int countAllRentalApplications() {
        return rentalApplicationRepository.countAll();
    }

    public void updateRentalApplicationStatus(Integer landlordId, Integer id, String status, String reply) {
        if (!RENTAL_APPLICATION_STATUSES.contains(status)) {
            throw new IllegalArgumentException("不支持的租赁申请状态");
        }
        RentalApplication application = rentalApplicationRepository.findByIdAndLandlordId(id, landlordId);
        if (application == null) {
            throw new IllegalArgumentException("租赁申请不存在或无权处理");
        }
        int updated = rentalApplicationRepository.updatePendingStatus(id, landlordId, status, reply);
        if (updated == 0) {
            throw new IllegalArgumentException("租赁申请当前状态不允许该操作");
        }
        if ("APPROVED".equals(status)) {
            houseRepository.updateRentStatus(application.getHouseId(), "RENTED");
        }
        notificationRepository.create(application.getUserId(), "租赁申请状态更新",
                "您对房源《" + application.getHouse().getTitle() + "》的租赁申请状态已更新为：" + status, "RENTAL_APPLICATION_UPDATED");
    }

    public void cancelRentalApplication(Integer userId, Integer id) {
        rentalApplicationRepository.cancel(id, userId);
    }

    public Map<String, Object> statistics() {
        return houseRepository.statistics();
    }

    public HouseImage addHouseImage(Integer userId, Integer houseId, org.springframework.web.multipart.MultipartFile file) throws java.io.IOException {
        if (!canManageHouseImages(userId, houseId)) {
            throw new IllegalArgumentException("房源不存在或无权管理图片");
        }
        HouseImage image = houseImageStorageService.addHouseImage(houseId, file);
        houseRepository.resetReviewStatus(houseId, userId);
        return image;
    }

    public List<HouseImage> getHouseImages(Integer houseId) {
        return houseImageStorageService.listHouseImages(houseId);
    }

    public void updateHouseImage(Integer userId, Integer houseId, String fileId, Integer sortOrder, Boolean cover) {
        if (!canManageHouseImages(userId, houseId)) {
            throw new IllegalArgumentException("房源不存在或无权管理图片");
        }
        houseImageStorageService.updateHouseImage(houseId, fileId, sortOrder, cover);
        houseRepository.resetReviewStatus(houseId, userId);
    }

    public void deleteHouseImage(Integer userId, Integer houseId, String fileId) {
        if (!canManageHouseImages(userId, houseId)) {
            throw new IllegalArgumentException("房源不存在或无权管理图片");
        }
        houseImageStorageService.deleteHouseImage(houseId, fileId);
        houseRepository.resetReviewStatus(houseId, userId);
    }

    public HouseImageStorageService.StoredImage readHouseImage(String fileId) throws java.io.IOException {
        return houseImageStorageService.read(fileId);
    }

    private House requireApprovedHouse(Integer houseId) {
        House house = houseRepository.findById(houseId);
        if (house == null) {
            throw new IllegalArgumentException("房源不存在");
        }
        if (!"APPROVED".equals(house.getStatus())) {
            throw new IllegalArgumentException("只能操作已审核通过的房源");
        }
        return house;
    }

    private boolean canManageHouseImages(Integer userId, Integer houseId) {
        House existing = houseRepository.findById(houseId);
        return existing != null && userId.equals(existing.getUserId());
    }

    private List<House> enrichHouses(List<House> houses) {
        List<Integer> houseIds = houses.stream()
                .map(House::getId)
                .filter(id -> id != null)
                .toList();
        Map<Integer, List<HouseImage>> imagesByHouseId = houseImageStorageService.listHouseImagesByHouseIds(houseIds);
        houses.forEach(house -> applyHouseImages(house, imagesByHouseId.getOrDefault(house.getId(), List.of())));
        return houses;
    }

    private House enrichHouse(House house) {
        if (house == null || house.getId() == null) {
            return house;
        }
        List<HouseImage> images = houseImageStorageService.listHouseImages(house.getId());
        applyHouseImages(house, images);
        return house;
    }

    private void applyHouseImages(House house, List<HouseImage> images) {
        house.setImages(images);
        String coverImageUrl = images.stream()
                .filter(image -> Boolean.TRUE.equals(image.getCover()))
                .findFirst()
                .or(() -> images.stream().findFirst())
                .map(HouseImage::getUrl)
                .orElse(house.getImageUrl());
        house.setCoverImageUrl(coverImageUrl);
    }

    private House requireAvailableApprovedHouse(Integer houseId) {
        House house = requireApprovedHouse(houseId);
        if (!"AVAILABLE".equals(house.getRentStatus())) {
            throw new IllegalArgumentException("该房源当前不可预约或申请");
        }
        return house;
    }
}
