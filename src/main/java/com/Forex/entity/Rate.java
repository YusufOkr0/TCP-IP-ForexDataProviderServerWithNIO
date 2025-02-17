package com.Forex.entity;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Rate {

    private String rateName;

    private BigDecimal bid;

    private BigDecimal ask;

    private Timestamp timestamp;

    public Rate(String rateName, BigDecimal bid, BigDecimal ask, Timestamp timestamp) {
        this.rateName = rateName;
        this.bid = bid;
        this.ask = ask;
        this.timestamp = timestamp;
    }

    public String getRateName() {
        return rateName;
    }

    public void setRateName(String rateName) {
        this.rateName = rateName;
    }

    public BigDecimal getBid() {
        return bid;
    }

    public void setBid(BigDecimal bid) {
        this.bid = bid;
    }

    public BigDecimal getAsk() {
        return ask;
    }

    public void setAsk(BigDecimal ask) {
        this.ask = ask;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }


}
