package com.toyota.broadcast;

import com.toyota.entity.Rate;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class FXDataPublisher {

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final Map<String, Set<SocketChannel>> subscriptions;
    private final List<Rate> rates;
    private final int PUBLISH_FREQUENCY;


    public FXDataPublisher(
            Map<String, Set<SocketChannel>> subscriptions,
            List<Rate> initial_rates,
            int publishFrequency) {
        this.subscriptions = subscriptions;
        this.rates = initial_rates;
        this.PUBLISH_FREQUENCY = publishFrequency;
    }


    public void startBroadcast() {
        scheduler.scheduleWithFixedDelay(this::publishRates, 0, PUBLISH_FREQUENCY, TimeUnit.MILLISECONDS);
    }

    private void publishRates() {
        for (Rate rate : rates) {
            updateRate(rate);
            String message = formatRateMessage(rate);

            Set<SocketChannel> clients = subscriptions.get(rate.getRateName());
            if (clients != null) {
                for (SocketChannel client : clients) {
                    sendToClient(client, message);
                }
            }
        }
    }

    private void updateRate(Rate rate) {
        BigDecimal minChange = new BigDecimal("0.0005");
        BigDecimal maxChange = new BigDecimal("0.002");

        BigDecimal spread = rate.getAsk().subtract(rate.getBid());

        BigDecimal changePercentage = BigDecimal.valueOf(Math.random())
                .multiply(maxChange.subtract(minChange))
                .add(minChange);

        if (Math.random() < 0.5) {
            changePercentage = changePercentage.negate();
        }

        BigDecimal newBid = rate.getBid().multiply(BigDecimal.ONE.add(changePercentage));
        BigDecimal newAsk = newBid.add(spread);

        rate.setBid(newBid.setScale(16,RoundingMode.HALF_UP));
        rate.setAsk(newAsk.setScale(16,RoundingMode.HALF_UP));
        rate.setTimestamp(Timestamp.from(Instant.now()));
    }


    private String formatRateMessage(Rate rate) {
        return String.format("%s|%s|%s|%s",
                rate.getRateName(),
                rate.getBid(),
                rate.getAsk(),
                rate.getTimestamp()
        );
    }

    private void sendToClient(SocketChannel client, String message) {
        try {
            ByteBuffer buffer = ByteBuffer.wrap((message + "\r\n").getBytes());
            client.write(buffer);
        } catch (IOException e) {
            System.err.println("Error while sending message to client: " + e.getMessage());
        }
    }
}
