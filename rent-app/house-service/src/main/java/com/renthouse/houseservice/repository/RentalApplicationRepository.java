package com.renthouse.houseservice.repository;

import com.renthouse.houseservice.model.House;
import com.renthouse.houseservice.model.RentalApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class RentalApplicationRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void save(RentalApplication application) {
        String sql = "INSERT INTO rental_application (user_id, house_id, landlord_id, applicant_name, applicant_phone, move_in_date, lease_months, message, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, application.getUserId(), application.getHouseId(), application.getLandlordId(),
                application.getApplicantName(), application.getApplicantPhone(), application.getMoveInDate(),
                application.getLeaseMonths(), application.getMessage(), "PENDING");
    }

    public boolean hasPendingApplication(Integer userId, Integer houseId) {
        String sql = "SELECT COUNT(*) FROM rental_application WHERE user_id = ? AND house_id = ? AND status = 'PENDING'";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId, houseId);
        return count != null && count > 0;
    }

    public List<RentalApplication> findByUserId(Integer userId) {
        String sql = baseSelect() + " WHERE r.user_id = ? ORDER BY r.created_at DESC";
        return jdbcTemplate.query(sql, new Object[]{userId}, new RentalApplicationRowMapper());
    }

    public List<RentalApplication> findByUserId(Integer userId, Integer offset, Integer size) {
        String sql = baseSelect() + " WHERE r.user_id = ? ORDER BY r.created_at DESC LIMIT ? OFFSET ?";
        return jdbcTemplate.query(sql, new Object[]{userId, size, offset}, new RentalApplicationRowMapper());
    }

    public int countByUserId(Integer userId) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM rental_application WHERE user_id = ?", Integer.class, userId);
        return count == null ? 0 : count;
    }

    public List<RentalApplication> findByLandlordId(Integer landlordId) {
        String sql = baseSelect() + " WHERE r.landlord_id = ? ORDER BY r.created_at DESC";
        return jdbcTemplate.query(sql, new Object[]{landlordId}, new RentalApplicationRowMapper());
    }

    public List<RentalApplication> findByLandlordId(Integer landlordId, Integer offset, Integer size) {
        String sql = baseSelect() + " WHERE r.landlord_id = ? ORDER BY r.created_at DESC LIMIT ? OFFSET ?";
        return jdbcTemplate.query(sql, new Object[]{landlordId, size, offset}, new RentalApplicationRowMapper());
    }

    public int countByLandlordId(Integer landlordId) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM rental_application WHERE landlord_id = ?", Integer.class, landlordId);
        return count == null ? 0 : count;
    }

    public RentalApplication findByIdAndLandlordId(Integer id, Integer landlordId) {
        String sql = baseSelect() + " WHERE r.id = ? AND r.landlord_id = ?";
        List<RentalApplication> applications = jdbcTemplate.query(sql, new Object[]{id, landlordId}, new RentalApplicationRowMapper());
        return applications.isEmpty() ? null : applications.get(0);
    }

    public List<RentalApplication> findAll() {
        String sql = baseSelect() + " ORDER BY r.created_at DESC";
        return jdbcTemplate.query(sql, new RentalApplicationRowMapper());
    }

    public List<RentalApplication> findAll(Integer offset, Integer size) {
        String sql = baseSelect() + " ORDER BY r.created_at DESC LIMIT ? OFFSET ?";
        return jdbcTemplate.query(sql, new Object[]{size, offset}, new RentalApplicationRowMapper());
    }

    public int countAll() {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM rental_application", Integer.class);
        return count == null ? 0 : count;
    }

    public int updatePendingStatus(Integer id, Integer landlordId, String status, String reply) {
        String sql = "UPDATE rental_application SET status = ?, reply = ? WHERE id = ? AND landlord_id = ? AND status = 'PENDING'";
        return jdbcTemplate.update(sql, status, reply, id, landlordId);
    }

    public void cancel(Integer id, Integer userId) {
        String sql = "UPDATE rental_application SET status = 'CANCELLED' WHERE id = ? AND user_id = ? AND status = 'PENDING'";
        jdbcTemplate.update(sql, id, userId);
    }

    private String baseSelect() {
        return """
                SELECT r.id AS rental_id, r.user_id AS rental_user_id, r.house_id AS rental_house_id,
                       r.landlord_id AS rental_landlord_id, r.applicant_name, r.applicant_phone, r.move_in_date,
                       r.lease_months, r.message AS rental_message, r.status AS rental_status, r.reply AS rental_reply,
                       r.created_at AS rental_created_at, r.updated_at AS rental_updated_at,
                       h.*
                FROM rental_application r
                JOIN house h ON r.house_id = h.id
                """;
    }

    private static class RentalApplicationRowMapper implements RowMapper<RentalApplication> {
        @Override
        public RentalApplication mapRow(ResultSet rs, int rowNum) throws SQLException {
            RentalApplication application = new RentalApplication();
            application.setId(rs.getInt("rental_id"));
            application.setUserId(rs.getInt("rental_user_id"));
            application.setHouseId(rs.getInt("rental_house_id"));
            application.setLandlordId(rs.getInt("rental_landlord_id"));
            application.setApplicantName(rs.getString("applicant_name"));
            application.setApplicantPhone(rs.getString("applicant_phone"));
            application.setMoveInDate(rs.getDate("move_in_date") == null ? null : rs.getDate("move_in_date").toLocalDate());
            application.setLeaseMonths(rs.getInt("lease_months"));
            application.setMessage(rs.getString("rental_message"));
            application.setStatus(rs.getString("rental_status"));
            application.setReply(rs.getString("rental_reply"));
            application.setCreatedAt(rs.getTimestamp("rental_created_at").toLocalDateTime());
            application.setUpdatedAt(rs.getTimestamp("rental_updated_at").toLocalDateTime());
            House house = HouseRepository.mapHouse(rs);
            application.setHouse(house);
            return application;
        }
    }
}
