package com.renthouse.houseservice.repository;

import com.renthouse.houseservice.model.Appointment;
import com.renthouse.houseservice.model.House;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class AppointmentRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void save(Appointment appointment) {
        String sql = "INSERT INTO appointment (user_id, house_id, landlord_id, appointment_time, contact_name, contact_phone, message, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, appointment.getUserId(), appointment.getHouseId(), appointment.getLandlordId(),
                appointment.getAppointmentTime(), appointment.getContactName(), appointment.getContactPhone(),
                appointment.getMessage(), "PENDING");
    }

    public boolean hasActiveAppointment(Integer userId, Integer houseId) {
        String sql = "SELECT COUNT(*) FROM appointment WHERE user_id = ? AND house_id = ? AND status IN ('PENDING', 'CONFIRMED')";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId, houseId);
        return count != null && count > 0;
    }

    public List<Appointment> findByUserId(Integer userId) {
        String sql = baseSelect() + " WHERE a.user_id = ? ORDER BY a.created_at DESC";
        return jdbcTemplate.query(sql, new Object[]{userId}, new AppointmentRowMapper());
    }

    public List<Appointment> findByUserId(Integer userId, Integer offset, Integer size) {
        String sql = baseSelect() + " WHERE a.user_id = ? ORDER BY a.created_at DESC LIMIT ? OFFSET ?";
        return jdbcTemplate.query(sql, new Object[]{userId, size, offset}, new AppointmentRowMapper());
    }

    public int countByUserId(Integer userId) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM appointment WHERE user_id = ?", Integer.class, userId);
        return count == null ? 0 : count;
    }

    public List<Appointment> findByLandlordId(Integer landlordId) {
        String sql = baseSelect() + " WHERE a.landlord_id = ? ORDER BY a.created_at DESC";
        return jdbcTemplate.query(sql, new Object[]{landlordId}, new AppointmentRowMapper());
    }

    public List<Appointment> findByLandlordId(Integer landlordId, Integer offset, Integer size) {
        String sql = baseSelect() + " WHERE a.landlord_id = ? ORDER BY a.created_at DESC LIMIT ? OFFSET ?";
        return jdbcTemplate.query(sql, new Object[]{landlordId, size, offset}, new AppointmentRowMapper());
    }

    public int countByLandlordId(Integer landlordId) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM appointment WHERE landlord_id = ?", Integer.class, landlordId);
        return count == null ? 0 : count;
    }

    public int confirm(Integer id, Integer landlordId, String reply) {
        String sql = "UPDATE appointment SET status = 'CONFIRMED', reply = ? WHERE id = ? AND landlord_id = ? AND status = 'PENDING'";
        return jdbcTemplate.update(sql, reply, id, landlordId);
    }

    public int reject(Integer id, Integer landlordId, String reply) {
        String sql = "UPDATE appointment SET status = 'CANCELLED', reply = ? WHERE id = ? AND landlord_id = ? AND status = 'PENDING'";
        return jdbcTemplate.update(sql, reply, id, landlordId);
    }

    public int finish(Integer id, Integer landlordId, String reply) {
        String sql = "UPDATE appointment SET status = 'FINISHED', reply = ? WHERE id = ? AND landlord_id = ? AND status = 'CONFIRMED'";
        return jdbcTemplate.update(sql, reply, id, landlordId);
    }

    public void cancel(Integer id, Integer userId) {
        String sql = "UPDATE appointment SET status = 'CANCELLED' WHERE id = ? AND user_id = ? AND status = 'PENDING'";
        jdbcTemplate.update(sql, id, userId);
    }

    private String baseSelect() {
        return """
                SELECT a.id AS appointment_id, a.user_id AS appointment_user_id, a.house_id AS appointment_house_id,
                       a.landlord_id AS appointment_landlord_id, a.appointment_time, a.contact_name AS appointment_contact_name,
                       a.contact_phone AS appointment_contact_phone, a.message AS appointment_message, a.status AS appointment_status,
                       a.reply AS appointment_reply, a.created_at AS appointment_created_at, a.updated_at AS appointment_updated_at,
                       h.*
                FROM appointment a
                JOIN house h ON a.house_id = h.id
                """;
    }

    private static class AppointmentRowMapper implements RowMapper<Appointment> {
        @Override
        public Appointment mapRow(ResultSet rs, int rowNum) throws SQLException {
            Appointment appointment = new Appointment();
            appointment.setId(rs.getInt("appointment_id"));
            appointment.setUserId(rs.getInt("appointment_user_id"));
            appointment.setHouseId(rs.getInt("appointment_house_id"));
            appointment.setLandlordId(rs.getInt("appointment_landlord_id"));
            appointment.setAppointmentTime(rs.getTimestamp("appointment_time").toLocalDateTime());
            appointment.setContactName(rs.getString("appointment_contact_name"));
            appointment.setContactPhone(rs.getString("appointment_contact_phone"));
            appointment.setMessage(rs.getString("appointment_message"));
            appointment.setStatus(rs.getString("appointment_status"));
            appointment.setReply(rs.getString("appointment_reply"));
            appointment.setCreatedAt(rs.getTimestamp("appointment_created_at").toLocalDateTime());
            appointment.setUpdatedAt(rs.getTimestamp("appointment_updated_at").toLocalDateTime());
            House house = HouseRepository.mapHouse(rs);
            appointment.setHouse(house);
            return appointment;
        }
    }
}
