package com.renthouse.userservice.service;

import com.renthouse.userservice.model.User;
import com.renthouse.userservice.model.Notification;
import com.renthouse.userservice.repository.NotificationRepository;
import com.renthouse.userservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private NotificationRepository notificationRepository;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User findById(Integer id) {
        return userRepository.findById(id);
    }

    public void registerUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
    }

    public boolean checkPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    public void applyForLandlord(Integer userId) {
        userRepository.updateLandlordApplyStatus(userId, "PENDING");
        notificationRepository.create(userId, "房东申请已提交", "您的房东申请已提交，请等待管理员审核。", "LANDLORD_APPLY");
    }

    public void approveLandlord(Integer userId) {
        userRepository.approveLandlord(userId);
        notificationRepository.create(userId, "房东申请已通过", "您的房东申请已通过，请重新登录进入房东中心。", "LANDLORD_APPROVED");
    }

    public void rejectLandlord(Integer userId, String reason) {
        userRepository.rejectLandlord(userId, reason);
        notificationRepository.create(userId, "房东申请被拒绝", reason == null || reason.isBlank() ? "您的房东申请未通过审核。" : reason, "LANDLORD_REJECTED");
    }

    public void updateUser(User user) {
        userRepository.updateUser(user);
    }

    public boolean changePassword(Integer userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId);
        if (user == null || !checkPassword(oldPassword, user.getPassword())) {
            return false;
        }
        userRepository.updatePassword(userId, passwordEncoder.encode(newPassword));
        return true;
    }

    public void updateAvatarFileId(Integer userId, String avatarFileId) {
        userRepository.updateAvatarFileId(userId, avatarFileId);
    }

    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    public List<User> findUsersPage(Integer page, Integer size) {
        return userRepository.findPage((page - 1) * size, size);
    }

    public int countUsers() {
        return userRepository.countAll();
    }

    public List<Notification> findNotifications(Integer userId, Integer page, Integer size) {
        return notificationRepository.findByUserId(userId, (page - 1) * size, size);
    }

    public int countNotifications(Integer userId) {
        return notificationRepository.countByUserId(userId);
    }

    public void markNotificationRead(Integer userId, Integer id) {
        notificationRepository.markRead(id, userId);
    }
}
