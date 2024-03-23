package com.example.cryptocurrencybot.service;

import com.example.cryptocurrencybot.model.PriceInfo;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class PriceUpdater {

    private final ApplicationEventPublisher eventPublisher;

    private Map<String, Double> currentPrices = new HashMap<>();
    private Map<String, Double> newPrices = new HashMap<>();

    public PriceUpdater(ApplicationEventPublisher eventPublisher){
        this.eventPublisher = eventPublisher;
    }

    @Scheduled(fixedRate = 20000)
    public void updatePrices(){

        newPrices = getNewPrices();
        compareAndNotify(currentPrices, newPrices);
    }

    private Map<String, Double> getNewPrices() {
        Map<String, Double> prices = new HashMap<>();

        try {
            URL url = new URL("https://api.mexc.com/api/v3/ticker/price");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            JSONArray jsonArray = new JSONArray(response.toString());
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String symbol = jsonObject.getString("symbol");
                double price = jsonObject.getDouble("price");
                prices.put(symbol, price);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return prices;
    }

    private void compareAndNotify(Map<String, Double> oldPrices, Map<String, Double> newPrices) {
        for (Map.Entry<String, Double> entry : oldPrices.entrySet()) {
            String symbol = entry.getKey();
            if (newPrices.containsKey(symbol)) {
                double oldPrice = entry.getValue();
                double newPrice = newPrices.get(symbol);
                // Если новая цена больше старой на 10%
                if (newPrice != oldPrice) {
                    double priceDifference = newPrice - oldPrice;
                    int percentageChange = (int) ((priceDifference / oldPrice) * 100);

                    PriceIncreaseEvent event = new PriceIncreaseEvent(this, symbol, oldPrice, newPrice, percentageChange);
                    eventPublisher.publishEvent(event);
                }
            }
        }
        currentPrices = newPrices;
    }

    public Map<String, Double> getPrices(){
        return currentPrices;
    }


}
