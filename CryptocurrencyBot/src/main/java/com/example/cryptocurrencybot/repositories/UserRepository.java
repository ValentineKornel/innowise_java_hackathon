package com.example.cryptocurrencybot.repositories;

import com.example.cryptocurrencybot.model.User;
import com.example.cryptocurrencybot.service.PriceUpdater;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.List;
import java.util.Optional;

@Repository
public class UserRepository {

    private final JdbcTemplate jdbc;


    public UserRepository(JdbcTemplate jdbc){
        this.jdbc = jdbc;
    }

    public void saveUser(User user) {
        String sql = "INSERT INTO users (id, username, rate) VALUES (?, ?, ?)";
        jdbc.update(sql,user.getId() , user.getUsername(), user.getRate());
    }

    public void updateUserRate(Long id, int newRate) {
        String sql = "UPDATE users SET rate = ? WHERE id = ?";
        jdbc.update(sql, newRate, id);
    }


    public Optional<User> findUserById(Long id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        return jdbc.query(sql, new Object[]{id}, rs -> {
            if (rs.next()) {
                User user = new User();
                user.setId(rs.getLong("id"));
                user.setUsername(rs.getString("username"));
                user.setRate(rs.getInt("rate"));
                return Optional.of(user);
            } else {
                return Optional.empty();
            }
        });
    }

    public List<User> findUserByRate(int rate) throws SQLException {
        String sql = "SELECT id, username, rate FROM users WHERE rate = ?";

        RowMapper<User> userRowMapper = (rs, rowNum) -> {
            User user = new User();
            user.setId(rs.getLong("id"));
            user.setUsername(rs.getString("username"));
            user.setRate(rs.getInt("rate"));
            return user;
        };

        return jdbc.query(sql, new Object[]{rate}, userRowMapper);
    }

}