package com.renthouse.houseservice.repository;

import com.renthouse.houseservice.model.House;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.support.GeneratedKeyHolder;

@Repository
public class HouseRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public Integer save(House house) {
        String sql = "INSERT INTO house (title, price, location, latitude, longitude, description, layout, area, floor, orientation, image_url, contact_name, contact_phone, user_id, status, rent_status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            java.sql.PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            statement.setObject(1, house.getTitle());
            statement.setObject(2, house.getPrice());
            statement.setObject(3, house.getLocation());
            statement.setObject(4, house.getLatitude());
            statement.setObject(5, house.getLongitude());
            statement.setObject(6, house.getDescription());
            statement.setObject(7, house.getLayout());
            statement.setObject(8, house.getArea());
            statement.setObject(9, house.getFloor());
            statement.setObject(10, house.getOrientation());
            statement.setObject(11, house.getImageUrl());
            statement.setObject(12, house.getContactName());
            statement.setObject(13, house.getContactPhone());
            statement.setObject(14, house.getUserId());
            statement.setObject(15, house.getStatus());
            statement.setObject(16, house.getRentStatus());
            return statement;
        }, keyHolder);
        Number key = keyHolder.getKey();
        Integer id = key == null ? null : key.intValue();
        house.setId(id);
        return id;
    }

    public List<House> findByLocationAndStatus(String location, String status) {
        String sql = "SELECT * FROM house WHERE location LIKE ? AND status = ?";
        return jdbcTemplate.query(sql, new Object[]{"%" + location + "%", status}, new HouseRowMapper());
    }

    public List<House> search(String location, BigDecimal minPrice, BigDecimal maxPrice, String layout,
                              BigDecimal minArea, BigDecimal maxArea, String status) {
        StringBuilder sql = new StringBuilder("SELECT * FROM house WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (status != null && !status.trim().isEmpty()) {
            sql.append(" AND status = ?");
            params.add(status);
        }
        if (location != null && !location.trim().isEmpty()) {
            sql.append(" AND location LIKE ?");
            params.add("%" + location.trim() + "%");
        }
        if (minPrice != null) {
            sql.append(" AND price >= ?");
            params.add(minPrice);
        }
        if (maxPrice != null) {
            sql.append(" AND price <= ?");
            params.add(maxPrice);
        }
        if (layout != null && !layout.trim().isEmpty()) {
            sql.append(" AND layout LIKE ?");
            params.add("%" + layout.trim() + "%");
        }
        if (minArea != null) {
            sql.append(" AND area >= ?");
            params.add(minArea);
        }
        if (maxArea != null) {
            sql.append(" AND area <= ?");
            params.add(maxArea);
        }
        sql.append(" ORDER BY created_at DESC, id DESC");
        return jdbcTemplate.query(sql.toString(), params.toArray(), new HouseRowMapper());
    }

    public House findById(Integer id) {
        String sql = "SELECT * FROM house WHERE id = ?";
        List<House> houses = jdbcTemplate.query(sql, new Object[]{id}, new HouseRowMapper());
        return houses.isEmpty() ? null : houses.get(0);
    }

    public List<House> findByUserId(Integer userId) {
        String sql = "SELECT * FROM house WHERE user_id = ?";
        return jdbcTemplate.query(sql, new Object[]{userId}, new HouseRowMapper());
    }

    public List<House> findByUserId(Integer userId, Integer offset, Integer size) {
        String sql = "SELECT * FROM house WHERE user_id = ? ORDER BY created_at DESC, id DESC LIMIT ? OFFSET ?";
        return jdbcTemplate.query(sql, new Object[]{userId, size, offset}, new HouseRowMapper());
    }

    public int countByUserId(Integer userId) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM house WHERE user_id = ?", Integer.class, userId);
        return count == null ? 0 : count;
    }

    public List<House> findByStatus(String status) {
        String sql = "SELECT * FROM house WHERE status = ?";
        return jdbcTemplate.query(sql, new Object[]{status}, new HouseRowMapper());
    }

    public List<House> findByStatus(String status, Integer offset, Integer size) {
        String sql = "SELECT * FROM house WHERE status = ? ORDER BY created_at DESC, id DESC LIMIT ? OFFSET ?";
        return jdbcTemplate.query(sql, new Object[]{status, size, offset}, new HouseRowMapper());
    }

    public int countByStatus(String status) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM house WHERE status = ?", Integer.class, status);
        return count == null ? 0 : count;
    }

    public List<House> findAll() {
        String sql = "SELECT * FROM house ORDER BY created_at DESC, id DESC";
        return jdbcTemplate.query(sql, new HouseRowMapper());
    }

    public List<House> findAll(Integer offset, Integer size) {
        String sql = "SELECT * FROM house ORDER BY created_at DESC, id DESC LIMIT ? OFFSET ?";
        return jdbcTemplate.query(sql, new Object[]{size, offset}, new HouseRowMapper());
    }

    public int countAll() {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM house", Integer.class);
        return count == null ? 0 : count;
    }

    public void updateStatus(Integer id, String status, String reason) {
        String sql = "UPDATE house SET status = ?, reason = ? WHERE id = ?";
        jdbcTemplate.update(sql, status, reason, id);
    }

    public void update(House house) {
        String sql = "UPDATE house SET title = ?, price = ?, location = ?, latitude = ?, longitude = ?, description = ?, layout = ?, area = ?, floor = ?, orientation = ?, image_url = ?, contact_name = ?, contact_phone = ?, status = 'PENDING', reason = NULL WHERE id = ? AND user_id = ?";
        jdbcTemplate.update(sql, house.getTitle(), house.getPrice(), house.getLocation(), house.getLatitude(),
                house.getLongitude(), house.getDescription(), house.getLayout(), house.getArea(),
                house.getFloor(), house.getOrientation(), house.getImageUrl(), house.getContactName(), house.getContactPhone(),
                house.getId(), house.getUserId());
    }

    public void offline(Integer id, Integer userId) {
        String sql = "UPDATE house SET status = 'OFFLINE' WHERE id = ? AND user_id = ?";
        jdbcTemplate.update(sql, id, userId);
    }

    public void updateRentStatus(Integer id, String rentStatus) {
        String sql = "UPDATE house SET rent_status = ? WHERE id = ?";
        jdbcTemplate.update(sql, rentStatus, id);
    }

    public Map<String, Object> statistics() {
        String sql = """
                SELECT
                  (SELECT COUNT(*) FROM user) AS userCount,
                  (SELECT COUNT(*) FROM user WHERE role = 'LANDLORD') AS landlordCount,
                  (SELECT COUNT(*) FROM user WHERE landlord_apply_status = 'PENDING') AS pendingLandlordCount,
                  (SELECT COUNT(*) FROM house) AS houseCount,
                  (SELECT COUNT(*) FROM house WHERE status = 'PENDING') AS pendingHouseCount,
                  (SELECT COUNT(*) FROM favorite) AS favoriteCount,
                  (SELECT COUNT(*) FROM appointment) AS appointmentCount,
                  (SELECT COUNT(*) FROM rental_application) AS rentalApplicationCount,
                  (SELECT COUNT(*) FROM rental_application WHERE status = 'PENDING') AS pendingRentalApplicationCount
                """;
        return jdbcTemplate.queryForMap(sql);
    }

    public static House mapHouse(ResultSet rs) throws SQLException {
        House house = new House();
        house.setId(rs.getInt("id"));
        house.setTitle(rs.getString("title"));
        house.setPrice(rs.getBigDecimal("price"));
        house.setLocation(rs.getString("location"));
        house.setLatitude(rs.getBigDecimal("latitude"));
        house.setLongitude(rs.getBigDecimal("longitude"));
        house.setDescription(rs.getString("description"));
        house.setLayout(rs.getString("layout"));
        house.setArea(rs.getBigDecimal("area"));
        house.setFloor(rs.getString("floor"));
        house.setOrientation(rs.getString("orientation"));
        house.setImageUrl(rs.getString("image_url"));
        house.setContactName(rs.getString("contact_name"));
        house.setContactPhone(rs.getString("contact_phone"));
        house.setUserId(rs.getInt("user_id"));
        house.setStatus(rs.getString("status"));
        house.setRentStatus(rs.getString("rent_status"));
        house.setReason(rs.getString("reason"));
        house.setCreatedAt(rs.getTimestamp("created_at") == null ? null : rs.getTimestamp("created_at").toLocalDateTime());
        house.setUpdatedAt(rs.getTimestamp("updated_at") == null ? null : rs.getTimestamp("updated_at").toLocalDateTime());
        return house;
    }

    private static class HouseRowMapper implements RowMapper<House> {
        @Override
        public House mapRow(ResultSet rs, int rowNum) throws SQLException {
            return mapHouse(rs);
        }
    }
}
