package org.example.payment.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;


@Component
public class ExchangeRateClient {

    private final RestTemplate restTemplate;
    private final String appId;

    public ExchangeRateClient(
            RestTemplate restTemplate,
            @Value("${openexchangerates.app-id:}") String appId
    ) {
        this.restTemplate = restTemplate;
        Assert.hasText(appId, "Missing configuration: openexchangerates.app-id or OPENEXCHANGERATES_APP_ID");
        this.appId = appId;
    }

    /**
     * Returns the multiplier to convert 1 unit of {@code fromCurrency} into USD.
     * Returns {@link BigDecimal#ONE} when {@code fromCurrency} is already USD,
     * since the account balance table defaults to USD.
     */
    @SuppressWarnings("unchecked")
    public BigDecimal getRateToUsd(String fromCurrency) {
        if (fromCurrency == null || "USD".equalsIgnoreCase(fromCurrency)) {
            return BigDecimal.ONE;
        }

        String url = "https://openexchangerates.org/api/latest.json?app_id=" + appId;
        Map<String, Object> response = restTemplate.getForObject(url, Map.class);
        if (response == null || !response.containsKey("rates")) {
            throw new IllegalStateException("Unable to retrieve exchange rates from openexchangerates.org");
        }

        Map<String, Object> rates = (Map<String, Object>) response.get("rates");
        Object rateObj = rates.get(fromCurrency.toUpperCase());
        if (rateObj == null) {
            throw new IllegalStateException("No exchange rate found for currency: " + fromCurrency);
        }

        BigDecimal rateFromUsdToCurrency = new BigDecimal(rateObj.toString());
        // rates.json is expressed as USD -> currency; invert to get currency -> USD.
        return BigDecimal.ONE.divide(rateFromUsdToCurrency, 8, RoundingMode.HALF_UP);
    }
}

