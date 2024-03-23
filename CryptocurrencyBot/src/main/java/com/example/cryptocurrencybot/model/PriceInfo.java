package com.example.cryptocurrencybot.model;

public class PriceInfo {
    private final String symbol;
    private final String price;

    public PriceInfo(String symbol, String price) {
        this.symbol = symbol;
        this.price = price;
    }

    @Override
    public String toString() {
        return "PriceInfo{" +
                "symbol='" + symbol + '\'' +
                ", price='" + price + '\'' +
                '}';
    }
}