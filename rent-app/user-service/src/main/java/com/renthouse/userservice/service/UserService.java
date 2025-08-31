package com.renthouse.userservice.service;

import com.renthouse.userservice.model.User;
import com.renthouse.userservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

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
    }

    public void approveLandlord(Integer userId) {
        userRepository.approveLandlord(userId);
    }

    public void updateUser(User user) {
        userRepository.updateUser(user);
    }

    public List<User> findAllUsers() {
        return userRepository.findAll();
    }
}