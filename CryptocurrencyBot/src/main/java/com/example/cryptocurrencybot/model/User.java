package com.example.cryptocurrencybot.model;


public class User {
    private Long id;
    private String username;
    private int rate;

    public User(Long id, String username, int rate) {
        this.id = id;
        this.username = username;
        this.rate = rate;
    }

    public User(){

    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setRate(int rate) {
        this.rate = rate;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public int getRate() {
        return rate;
    }

    @Override
    public String toString() {
        return "userid: " + id + " " + username + " " + rate;
    }
}
