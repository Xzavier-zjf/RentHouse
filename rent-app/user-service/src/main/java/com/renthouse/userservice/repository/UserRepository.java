package com.renthouse.userservice.repository;

import com.renthouse.userservice.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class UserRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public User findByUsername(String username) {
        String sql = "SELECT * FROM user WHERE username = ?";
        List<User> users = jdbcTemplate.query(sql, new Object[]{username}, new UserRowMapper());
        return users.isEmpty() ? null : users.get(0);
    }

    public User findById(Integer id) {
        String sql = "SELECT * FROM user WHERE id = ?";
        List<User> users = jdbcTemplate.query(sql, new Object[]{id}, new UserRowMapper());
        return users.isEmpty() ? null : users.get(0);
    }

    public void save(User user) {
        String sql = "INSERT INTO user (username, password, email, avatar_file_id, role, landlord_apply_status, landlord_apply_reason) VALUES (?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, user.getUsername(), user.getPassword(), user.getEmail(), user.getAvatarFileId(), user.getRole(), user.getLandlordApplyStatus(), user.getLandlordApplyReason());
    }

    public void updateRole(Integer userId, String role) {
        String sql = "UPDATE user SET role = ? WHERE id = ?";
        jdbcTemplate.update(sql, role, userId);
    }

    public void updateLandlordApplyStatus(Integer userId, String status) {
        String sql = "UPDATE user SET landlord_apply_status = ?, landlord_apply_reason = NULL WHERE id = ?";
        jdbcTemplate.update(sql, status, userId);
    }

    public void approveLandlord(Integer userId) {
        String sql = "UPDATE user SET role = 'LANDLORD', landlord_apply_status = 'APPROVED', landlord_apply_reason = NULL WHERE id = ?";
        jdbcTemplate.update(sql, userId);
    }

    public void rejectLandlord(Integer userId, String reason) {
        String sql = "UPDATE user SET role = 'USER', landlord_apply_status = 'REJECTED', landlord_apply_reason = ? WHERE id = ?";
        jdbcTemplate.update(sql, reason, userId);
    }

    public void updateUser(User user) {
        String sql = "UPDATE user SET email = ?, avatar_file_id = ?, role = ?, landlord_apply_status = ?, landlord_apply_reason = ? WHERE id = ?";
        jdbcTemplate.update(sql, user.getEmail(), user.getAvatarFileId(), user.getRole(), user.getLandlordApplyStatus(), user.getLandlordApplyReason(), user.getId());
    }

    public void updatePassword(Integer userId, String encodedPassword) {
        String sql = "UPDATE user SET password = ? WHERE id = ?";
        jdbcTemplate.update(sql, encodedPassword, userId);
    }

    public void updateAvatarFileId(Integer userId, String avatarFileId) {
        String sql = "UPDATE user SET avatar_file_id = ? WHERE id = ?";
        jdbcTemplate.update(sql, avatarFileId, userId);
    }

    public List<User> findAll() {
        String sql = "SELECT * FROM user";
        return jdbcTemplate.query(sql, new UserRowMapper());
    }

    public List<User> findPage(Integer offset, Integer size) {
        String sql = "SELECT * FROM user ORDER BY id DESC LIMIT ? OFFSET ?";
        return jdbcTemplate.query(sql, new Object[]{size, offset}, new UserRowMapper());
    }

    public int countAll() {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM user", Integer.class);
        return count == null ? 0 : count;
    }

    private static class UserRowMapper implements RowMapper<User> {
        @Override
        public User mapRow(ResultSet rs, int rowNum) throws SQLException {
            User user = new User();
            user.setId(rs.getInt("id"));
            user.setUsername(rs.getString("username"));
            user.setPassword(rs.getString("password"));
            user.setEmail(rs.getString("email"));
            user.setAvatarFileId(rs.getString("avatar_file_id"));
            user.setRole(rs.getString("role"));
            user.setLandlordApplyStatus(rs.getString("landlord_apply_status"));
            user.setLandlordApplyReason(rs.getString("landlord_apply_reason"));
            return user;
        }
    }
}
