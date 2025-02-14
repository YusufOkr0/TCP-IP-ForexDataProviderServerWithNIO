package com.Forex.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ConfigLoader {

    private final Properties properties = new Properties();

    public ConfigLoader(String CONFIG_FILE) {
        loadConfigFile(CONFIG_FILE);
    }

    private void loadConfigFile(String CONFIG_FILE) {
        try{
            InputStream inputStream = ClassLoader.getSystemResourceAsStream(CONFIG_FILE);
            properties.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException("Error while reading config file: " + CONFIG_FILE, e);
        }
    }

    public int getPublishFrequency(){
        return Integer.parseInt(properties.getProperty("publish.frequency","5000"));
    }

    public int getPublishCount(){
        return Integer.parseInt(properties.getProperty("publish.count","100"));
    }

    public int getServerPort() {
        return Integer.parseInt(properties.getProperty("server.port","8081"));
    }

    public BigDecimal getUsdTryBid(){
        return new BigDecimal(properties.getProperty("TCP_USDTRY.bid"));
    }
    public BigDecimal getUsdTryAsk(){
        return new BigDecimal(properties.getProperty("TCP_USDTRY.ask"));
    }
    public BigDecimal getEurUsdBid(){
        return new BigDecimal(properties.getProperty("TCP_EURUSD.bid"));
    }
    public BigDecimal getEurUsdAsk(){
        return new BigDecimal(properties.getProperty("TCP_EURUSD.ask"));
    }
    public BigDecimal getGbpUsdBid(){
        return new BigDecimal(properties.getProperty("TCP_GBPUSD.bid"));
    }
    public BigDecimal getGbpUsdAsk(){
        return new BigDecimal(properties.getProperty("TCP_GBPUSD.ask"));
    }



    public Set<String> getCurrencyPairs() {
        String[] pairs = properties.getProperty("currency.pairs").split(",");
        Set<String> currencyPairs = new HashSet<>(Arrays.asList(pairs));
        if(currencyPairs.isEmpty()){
            currencyPairs.add("USDTRY");
        }
        return currencyPairs;
    }

    public Map<String, String> getUsersCredentials() {
        Map<String, String> authRepository = new HashMap<>();
        String[] credentials = properties.getProperty("user.credentials").split(",");
        for (String credential : credentials) {
            String[] user = credential.split("\\|");
            authRepository.put(user[0], user[1]);
        }
        if(authRepository.isEmpty()){
            authRepository.put("admin", "admin");
        }
        return authRepository;
    }

    public Map<String,Set<SocketChannel>> getSubscriptions() {
        Map<String, Set<SocketChannel>> subscriptions = new ConcurrentHashMap<>();
        String[] pairs = properties.getProperty("currency.pairs").split(",");
        for (String pair : pairs) {
            subscriptions.put(pair, ConcurrentHashMap.newKeySet());
        }
        return subscriptions;
    }




}
