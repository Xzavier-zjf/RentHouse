package com.renthouse.houseservice.repository;

import com.renthouse.houseservice.model.Favorite;
import com.renthouse.houseservice.model.House;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class FavoriteRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void add(Integer userId, Integer houseId) {
        String sql = "INSERT IGNORE INTO favorite (user_id, house_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, userId, houseId);
    }

    public void remove(Integer userId, Integer houseId) {
        String sql = "DELETE FROM favorite WHERE user_id = ? AND house_id = ?";
        jdbcTemplate.update(sql, userId, houseId);
    }

    public boolean exists(Integer userId, Integer houseId) {
        String sql = "SELECT COUNT(*) FROM favorite WHERE user_id = ? AND house_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId, houseId);
        return count != null && count > 0;
    }

    public List<Favorite> findByUserId(Integer userId) {
        String sql = """
                SELECT f.id AS favorite_id, f.user_id AS favorite_user_id, f.house_id AS favorite_house_id, f.created_at AS favorite_created_at,
                       h.*
                FROM favorite f
                JOIN house h ON f.house_id = h.id
                WHERE f.user_id = ?
                ORDER BY f.created_at DESC
                """;
        return jdbcTemplate.query(sql, new Object[]{userId}, new FavoriteRowMapper());
    }

    private static class FavoriteRowMapper implements RowMapper<Favorite> {
        @Override
        public Favorite mapRow(ResultSet rs, int rowNum) throws SQLException {
            Favorite favorite = new Favorite();
            favorite.setId(rs.getInt("favorite_id"));
            favorite.setUserId(rs.getInt("favorite_user_id"));
            favorite.setHouseId(rs.getInt("favorite_house_id"));
            favorite.setCreatedAt(rs.getTimestamp("favorite_created_at").toLocalDateTime());

            House house = HouseRepository.mapHouse(rs);
            favorite.setHouse(house);
            return favorite;
        }
    }
}
