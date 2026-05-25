package com.renthouse.userservice.repository;

import com.renthouse.userservice.model.Notification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class NotificationRepository {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void create(Integer userId, String title, String content, String type) {
        String sql = "INSERT INTO notification (user_id, title, content, type) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, userId, title, content, type);
    }

    public List<Notification> findByUserId(Integer userId, Integer offset, Integer size) {
        String sql = "SELECT * FROM notification WHERE user_id = ? ORDER BY created_at DESC, id DESC LIMIT ? OFFSET ?";
        return jdbcTemplate.query(sql, new Object[]{userId, size, offset}, new NotificationRowMapper());
    }

    public int countByUserId(Integer userId) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM notification WHERE user_id = ?", Integer.class, userId);
        return count == null ? 0 : count;
    }

    public void markRead(Integer id, Integer userId) {
        jdbcTemplate.update("UPDATE notification SET read_status = 'READ' WHERE id = ? AND user_id = ?", id, userId);
    }

    private static class NotificationRowMapper implements RowMapper<Notification> {
        @Override
        public Notification mapRow(ResultSet rs, int rowNum) throws SQLException {
            Notification notification = new Notification();
            notification.setId(rs.getInt("id"));
            notification.setUserId(rs.getInt("user_id"));
            notification.setTitle(rs.getString("title"));
            notification.setContent(rs.getString("content"));
            notification.setType(rs.getString("type"));
            notification.setReadStatus(rs.getString("read_status"));
            notification.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            return notification;
        }
    }
}
