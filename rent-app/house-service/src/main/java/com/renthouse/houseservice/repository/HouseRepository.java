package com.renthouse.houseservice.repository;

import com.renthouse.houseservice.model.House;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class HouseRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void save(House house) {
        String sql = "INSERT INTO house (title, price, location, user_id, status) VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, house.getTitle(), house.getPrice(), house.getLocation(), house.getUserId(), house.getStatus());
    }

    public List<House> findByLocationAndStatus(String location, String status) {
        String sql = "SELECT * FROM house WHERE location LIKE ? AND status = ?";
        return jdbcTemplate.query(sql, new Object[]{"%" + location + "%", status}, new HouseRowMapper());
    }

    public List<House> findByUserId(Integer userId) {
        String sql = "SELECT * FROM house WHERE user_id = ?";
        return jdbcTemplate.query(sql, new Object[]{userId}, new HouseRowMapper());
    }

    public List<House> findByStatus(String status) {
        String sql = "SELECT * FROM house WHERE status = ?";
        return jdbcTemplate.query(sql, new Object[]{status}, new HouseRowMapper());
    }

    public List<House> findAll() {
        String sql = "SELECT * FROM house";
        return jdbcTemplate.query(sql, new HouseRowMapper());
    }

    public void updateStatus(Integer id, String status, String reason) {
        String sql = "UPDATE house SET status = ?, reason = ? WHERE id = ?";
        jdbcTemplate.update(sql, status, reason, id);
    }

    private static class HouseRowMapper implements RowMapper<House> {
        @Override
        public House mapRow(ResultSet rs, int rowNum) throws SQLException {
            House house = new House();
            house.setId(rs.getInt("id"));
            house.setTitle(rs.getString("title"));
            house.setPrice(rs.getBigDecimal("price"));
            house.setLocation(rs.getString("location"));
            house.setUserId(rs.getInt("user_id"));
            house.setStatus(rs.getString("status"));
            house.setReason(rs.getString("reason"));
            return house;
        }
    }
}