package com.example.cryptocurrencybot.service;


import org.springframework.context.ApplicationEvent;

public class PriceIncreaseEvent extends ApplicationEvent {
    private final String symbol;
    private final double oldPrice;
    private final double newPrice;
    private final int percent;

    public PriceIncreaseEvent(Object source, String symbol, double oldPrice, double newPrice, int percent) {
        super(source);
        this.symbol = symbol;
        this.oldPrice = oldPrice;
        this.newPrice = newPrice;
        this.percent = percent;
    }

    public String getSymbol() {
        return symbol;
    }

    public double getOldPrice() {
        return oldPrice;
    }

    public double getNewPrice() {
        return newPrice;
    }

    public int getPercent(){return percent;}
}