package com.toyota;

import com.toyota.broadcast.FXDataPublisher;
import com.toyota.config.ConfigLoader;
import com.toyota.entity.Rate;
import com.toyota.server.FXDataServer;

import java.math.BigDecimal;
import java.nio.channels.SocketChannel;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Main {
    public static void main(String[] args) {

        final String CONFIG_FILE = "application.properties";
        final ConfigLoader configLoader = new ConfigLoader(CONFIG_FILE);

        final int SERVER_PORT = configLoader.getServerPort();
        final Set<String> CURRENCY_PAIRS = configLoader.getCurrencyPairs();
        final Map<String,String> USER_CREDENTIALS = configLoader.getUsersCredentials();
        final Map<String,Set<SocketChannel>> SUBSCRIPTIONS = configLoader.getSubscriptions();


        FXDataServer server = new FXDataServer(
                SERVER_PORT,
                CURRENCY_PAIRS,
                SUBSCRIPTIONS,
                USER_CREDENTIALS
        );

        new Thread(server::startServer, "FXDataServer-Thread").start();


        BigDecimal usdTryBid = configLoader.getUsdTryBid();
        BigDecimal usdTryAsk = configLoader.getUsdTryAsk();

        BigDecimal eurUsdBid = configLoader.getEurUsdBid();
        BigDecimal eurUsdAsk = configLoader.getEurUsdAsk();

        BigDecimal gbpUsdBid = configLoader.getGbpUsdBid();
        BigDecimal gbpUsdAsk = configLoader.getGbpUsdAsk();

        Rate USD_TRY = new Rate("TCP_USDTRY", usdTryBid, usdTryAsk, Timestamp.from(Instant.now()));

        Rate EUR_USD = new Rate("TCP_EURUSD", eurUsdBid, eurUsdAsk, Timestamp.from(Instant.now()));

        Rate GBP_USD = new Rate("TCP_GBPUSD", gbpUsdBid, gbpUsdAsk, Timestamp.from(Instant.now()));


        final List<Rate> INITIAL_RATES = new ArrayList<>();
        INITIAL_RATES.add(USD_TRY);
        INITIAL_RATES.add(EUR_USD);
        INITIAL_RATES.add(GBP_USD);

        final int PUBLISH_FREQUENCY = configLoader.getPublishFrequency();

        FXDataPublisher publisher = new FXDataPublisher(
                SUBSCRIPTIONS,
                INITIAL_RATES,
                PUBLISH_FREQUENCY
        );

        publisher.startBroadcast();



    }
}