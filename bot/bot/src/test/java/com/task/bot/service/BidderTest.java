package com.task.bot.service;

import com.task.bot.service.impl.BidderImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

public class BidderTest {

    private final Bidder service = new BidderImpl();

    @Test
    public void initWithZeroQuantityTest() {
        service.init(0, 1);
        assert (service.placeBid() == 0);
    }

    @Test
    public void initWithZeroCashTest() {
        service.init(2, 0);
        assert (service.placeBid() == 0);
    }

    @Test
    public void bidTo2QuantityTest() {
        int cash = 10;
        service.init(2, cash);
        assert (service.placeBid() == cash);
    }

    @Test
    public void bidAfterAuctionIsOverTest() {
        int cash = 10;
        service.init(2, cash);
        service.bids(service.placeBid(), cash);
        assert (service.placeBid() == 0);
    }

    @Test
    public void bidAfterWonTest() {
        int cash = 10;
        service.init(3, cash);
        service.bids(service.placeBid(), 5);
        assert (service.placeBid() == 0);
    }

    @Test
    public void bidChangeUnitTest() {
        int quantity = 6;
        int cash = 15;
        int bidChangeUnit = (cash / (quantity / 2)) / 2;
        service.init(6, 15);
        int firstBid = service.placeBid();
        service.bids(firstBid, 8);
        int secondBid = service.placeBid();
        assert (secondBid == firstBid + bidChangeUnit);
        service.bids(secondBid, 5);
        int thirdBid = service.placeBid();
        service.bids(thirdBid, 1);
        assert (thirdBid == firstBid - bidChangeUnit);
    }

    @Test
    public void economicalBidTest() {
        int cash = 10;
        service.init(4, cash);
        int firstBid = service.placeBid();
        service.bids(firstBid, cash);
        int secondBid = service.placeBid();
        assert (secondBid == 1);
        assert (firstBid + secondBid < cash);
    }

    @Test
    public void notEnoughCashTest() {
        service.init(6, 2);
        int firstBid = service.placeBid();
        service.bids(firstBid, 1);
        assert (firstBid == 1);
        int secondBid = service.placeBid();
        service.bids(secondBid, 0);
        assert (secondBid == 1);
        int thirdBid = service.placeBid();
        service.bids(thirdBid, 1);
        assert (thirdBid == 0);
    }

    @ParameterizedTest
    @MethodSource("provideBidsCases")
    public void testAgainstBids(int quantity, int cash, List<Integer> otherBids) {
        service.init(quantity, cash);
        assert (otherBids.stream().mapToInt(Integer::intValue).sum() == 40);
        int amountWon = 0;
        int ownCash =  cash;
        for (int otherBid : otherBids) {
            Integer ownBid = service.placeBid();
            service.bids(ownBid, otherBid);
            amountWon += ownBid.compareTo(otherBid) + 1;
            ownCash -= ownBid;
        }
        assert (amountWon >= quantity / 2);
        assert (ownCash >= 0);
    }
    private static Stream<Arguments> provideBidsCases() {
        return Stream.of(
                Arguments.of(20, 40, List.of(6, 7, 8, 8, 7, 4, 0, 0, 0, 0)), //aggressive bids
                Arguments.of(20, 40, List.of(1, 1, 1, 1, 6, 6, 7, 7, 7, 3)), //passive bids
                Arguments.of(20, 40, List.of(3, 3, 4, 4, 5, 5, 5, 4, 4, 3))); //middle bids
    }
}
