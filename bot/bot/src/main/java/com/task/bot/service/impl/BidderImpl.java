package com.task.bot.service.impl;

import com.task.bot.service.Bidder;
import org.springframework.stereotype.Service;

@Service
public class BidderImpl implements Bidder {

    private Integer totalQuantity = 0;
    private Integer ownCashLeft = 0;
    private Integer otherCashLeft = 0;
    private Integer amountWon = 0;
    private int[] ownBidsPerRound;
    private Integer average = 0;
    private Integer round = 0;
    private Integer prevOtherBid = 0;
    private Integer bidMovingRound = 0;
    private Integer bidChangeUnit = 0;
    private Integer totalCash = 0;

    /**
     * Initialization of trades with total amount of quantity units and available monetary units for both bidders.
     * Bidder parameters that are initialized:
     * - totalCash: amount of available monetary units at start of trades,
     * - totalQuantity: amount of quantity units for trades, which are exhibited in batches of 2 pieces,
     * - ownCashLeft: amount of available monetary units for this bidder,
     * - otherCashLeft: amount of available monetary units for other bidder,
     * - amountWon: amount of quantity units won by bidder,
     * - rounds: number of rounds of trades which is an average of totalQuantity and size of quantity unit batch,
     * that is rounded to nearest or bigger integer
     * - ownBidsPerRound: bidders plan of bids for trades,
     * - average: an average of total cash and total quantity, that is rounded to nearest or bigger integer,
     * - bidMovingRound: round of trades which considered as possible to moving a part of bid to/from current bid,
     * - bidChangeUnit: accepted amount of monetary units for changing bids due to current situation, at least 1.
     * There is no initialization in case of zero quantity units.
     * Re-initialization of the same instance of a bidder sets up a new auction.
     * @param quantity the quantity
     * @param cash the cash limit
     */
    @Override
    public void init(int quantity, int cash) {
        checkNegativeInitParams(quantity, cash);
        totalCash = cash;
        totalQuantity = quantity;
        ownCashLeft = cash;
        otherCashLeft = cash;
        amountWon = 0;
        int rounds = int rounds = calculateRoundsNumber(quantity);
        ownBidsPerRound = new int[rounds];
        average = calculateAverage(cash, rounds);
        fillRoundsByPossibleBids(ownBidsPerRound, average, cash);
        bidMovingRound = rounds - 1;
        bidChangeUnit = Math.max(average / 2, 1);
    }

    /**
     * Placing an own bid depending on state of trading.
     * Bid is 0 in case of:
     * - there is no quantity units to trade,
     * - bidder has no cash or bidder has spent his available cash,
     * - the auction is over
     * - bidder has amount of quantity units more than half of total, so he has won already and can save remaining cash.
     * Bid is 1 in case of other bidder has spent all of his available cash but this bidder still have cash and needs
     * to win current round.
     * Otherwise, bidder takes his planned bid, change it if needed and checks for possibility to make this bid.
     * @return the bit of this bidder
     */
    @Override
    public int placeBid() {
        if (totalQuantity == 0
                || ownCashLeft <= 0
                || round >= ownBidsPerRound.length
                || amountWon > (totalQuantity - amountWon))
            return 0;
        if (otherCashLeft == 0)
            return 1;
        return Math.min(getOwnPossibleBid(), ownCashLeft);
    }

    /**
     * Publishing of other bid for current party of quantity units.
     * Changing of remain cashes.
     * Calculating amount won depends on bigger bid and number of quantity units in party.
     * Refreshing previous other bid to current other bid.
     * Increasing of bid rounds counter.
     * @param own the bit of this bidder
     * @param other the bid of other bidder
     */
    @Override
    public void bids(Integer own, Integer other) {
        ownCashLeft -= own;
        otherCashLeft -= other;
        amountWon += own.compareTo(other) + 1;
        prevOtherBid = other;
        round++;
    }

    /**
     * Making own bid depends on the array of planned bids from beginning of trading with changes depending on current
     * cashes of both bidders, an average value of total cash, an amount of monetary units for changing of bids,
     * and total quantity and previous other bid.
     * In case of previous bid is bigger than the average, assume that other bidder can do bid in the future less than
     * average, so bidder can move some monetary amount from one of his possible bids in later rounds to current bid,
     * but keep some value for them also in assuming to take or tie last rounds.
     * If there is no enough cash planned for last rounds, bidder change position for cash moving to nearest one from the
     * last round and so on up to current round.
     * At the same time, if there is less than first half of total rounds and other bidder has spent most of his cash,
     * then probably there is an attempt to win by taking first rounds with higher bids. If so, probably bidder can not
     * win first rounds and there is no sense to increase current bid, but bidder can keep some cash for our last round
     * bids and try to win or tie by them.
     * In the case of previous bid is less than the average and current cash of other bidder is lower than ours,
     * it seems like bidder have won first rounds but other bidder keeps bigger bids for second part or rounds, so
     * currently bidder can put less, than he has planned to strengthen last rounds, but still keep some amount for
     * current bid  for better chances to win current round.
     * @return planned bid amount, probably changed to the accepted value of the bid change.
     */
    private int getOwnPossibleBid() {
        if (prevOtherBid > average && !(round < ownBidsPerRound.length / 2 && otherCashLeft < totalCash / 2)) {
            while (bidMovingRound > round && ownBidsPerRound[bidMovingRound] <= bidChangeUnit)
                bidMovingRound--;
            if (bidMovingRound > round && ownBidsPerRound[bidMovingRound] >= bidChangeUnit + 1) {
                ownBidsPerRound[bidMovingRound] -= bidChangeUnit;
                ownBidsPerRound[round] += bidChangeUnit;
            }
        }
        if (prevOtherBid < average && otherCashLeft > ownCashLeft && ownBidsPerRound[round] >= bidChangeUnit + 1) {
            ownBidsPerRound[round] -= bidChangeUnit;
            ownBidsPerRound[bidMovingRound] += bidChangeUnit;
        }
        return ownBidsPerRound[round];
    }

     /**
     * Calculation number of auction rounds depending on total amount of quantity units and one batch size (2).
     * @param quantity total amount of quantity units
     * @return number of auction rounds
     */
    private int calculateRoundsNumber(int quantity) {
        if (quantity > 0)
            return quantity % 2 == 0 ? quantity / 2 : quantity / 2 + 1;
        return 0;
    }

    /**
     * Calculating an average of total cash and total quantity, that is rounded to nearest or bigger integer.
     * The case of quantity <= 2 and single round of bids is separated due to the calculation of division remainder.
     * @param cash amount of monetary units available at the time of the start of trading
     * @param rounds rounds of bidding for each party of quantity units
     * @return an average of total cash and total quantity, that is rounded to nearest or bigger integer
     */
    private int calculateAverage(int cash, int rounds) {
        if (rounds < 2)
            return rounds * cash;
        return cash % rounds >= rounds / 2 ? cash / rounds + 1 : cash / rounds;
    }

    /**
     * Filling an array of rounds by own possible bids.
     * Each of available monetary unit is put in cell right to left until it reaches the average value.
     * Otherwise, the order changes and units which more that average are put from tail to head of the array.
     * In case, when total quantity equals 3, first of two bid rounds is more valuable and bidder put all of his cash.
     * @param rounds rounds of bidding for each party of two (or last one?) quantity units
     * @param average an average of total cash and total quantity, that is rounded to nearest or bigger integer
     * @param cash amount of monetary units available at the time of the start of trading
     */
    private void fillRoundsByPossibleBids(int[] rounds, int average, int cash) {
        if (totalQuantity == 3) {
            rounds[0] = cash;
            return;
        }
        int position = 0;
        while (cash > 0) {
            if (rounds[position] < average ||
                    (position == rounds.length - 1 && rounds[position] == average) ||
                    (position < rounds.length - 1 && rounds[position + 1] > average)) {
                cash--;
                rounds[position] += 1;
            }
            position++;
            if (position == rounds.length)
                position = 0;
        }
    }

    /**
     * Checking signs of quantity and cash. Can not be less than 0.
     * @param quantity amount of quantity units for the auction
     * @param cash amount of available monetary units
     */
    private void checkNegativeInitParams(int quantity, int cash) {
        if (quantity < 0)
            throw new IllegalArgumentException("Total amount of quantity units can not be less than 0");
        if (cash < 0)
            throw new IllegalArgumentException("Initial amount of monetary units can not be less than 0");
    }    
}
