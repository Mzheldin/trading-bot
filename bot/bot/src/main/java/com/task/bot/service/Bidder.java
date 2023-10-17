package com.task.bot.service;

public interface Bidder {

    /**
     * Initializes the bidder with the production quantity and the allowed cash limit.
     * @param quantity the quantity
     * @param cash the cash limit
     */
    void init(int quantity, int cash);

    /**
     * Retrieves the next bid for the product, which may be zero.
     * @return the next bid
     */
    int placeBid();

    /**
     * Shows the bids of the two bidders.
     * @param own the bit of this bidder
     * @param other the bid of other bidder
     */
    void bids(Integer own, Integer other);

}
