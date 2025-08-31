package com.renthouse.houseservice.service;

import com.renthouse.houseservice.model.House;
import com.renthouse.houseservice.repository.HouseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HouseService {

    @Autowired
    private HouseRepository houseRepository;

    public void uploadHouse(House house) {
        houseRepository.save(house);
    }

    public List<House> searchHouses(String location) {
        return houseRepository.findByLocationAndStatus(location, "APPROVED");
    }

    public List<House> getMyHouses(Integer userId) {
        return houseRepository.findByUserId(userId);
    }

    public List<House> getHousesByStatus(String status) {
        return houseRepository.findByStatus(status);
    }

    public List<House> getAllHouses() {
        return houseRepository.findAll();
    }

    public void approveHouse(Integer id, String status, String reason) {
        houseRepository.updateStatus(id, status, reason);
    }
}