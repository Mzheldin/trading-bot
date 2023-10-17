package com.task.bot.dto;

/**
 * An object that represents the current bid of this bidder for /api/placeBid.
 */
public class BidDTO {

    private int bid;

    public BidDTO(int bid) {
        this.bid = bid;
    }

    public int getBid() {
        return bid;
    }

    public void setBid(int bid) {
        this.bid = bid;
    }
}
