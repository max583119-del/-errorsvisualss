package com.example.ias.account;

public class Account {
    public String name;
    public String uuid;
    public String accessToken;
    public String refreshToken;

    public Account() {}

    public Account(String name, String uuid, String accessToken, String refreshToken) {
        this.name = name;
        this.uuid = uuid;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
}