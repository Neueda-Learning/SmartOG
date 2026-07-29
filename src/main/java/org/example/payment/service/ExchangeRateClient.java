package org.example.payment.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

/**
 * Thin client around the openexchangerates.org "latest" endpoint.
 * The free tier always returns rates with USD as the base currency
 * (i.e. "1 USD = rate * targetCurrency"), so to convert an amount that is
 * denominated in a non-USD currency into USD we invert the published rate.
 */
@Component
public class ExchangeRateClient {

    private final RestTemplate restTemplate;

    @Value("${openexchangerates.app-id:05b55aac70a14b4480dd902f060350b0}")
    private String appId;

    public ExchangeRateClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
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

