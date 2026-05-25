package com.renthouse.houseservice.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class NotificationRepository {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void create(Integer userId, String title, String content, String type) {
        String sql = "INSERT INTO notification (user_id, title, content, type) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, userId, title, content, type);
    }
}
