package edu.kdkce.openelectivefcfs.src.model;

import jakarta.persistence.*;

import java.time.ZonedDateTime;

@Entity
public class PasswordResetToken {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;
    String token;
    @OneToOne
    @JoinColumn(name = "user_id")
    User user;
    ZonedDateTime expiryTime;

    public PasswordResetToken() {}

    public PasswordResetToken(String token, User user, ZonedDateTime zonedDateTime) {
        this.token = token;
        this.user = user;
        this.expiryTime = zonedDateTime;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public ZonedDateTime getExpiryTime() {
        return expiryTime;
    }

    public void setExpiryTime(ZonedDateTime expiryTime) {
        this.expiryTime = expiryTime;
    }
}
