package com.task.bot.controller;

import com.task.bot.service.Bidder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static java.lang.String.valueOf;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BidderController.class)
public class BidderControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private Bidder service;

    @Test
    public void initTest() throws Exception {
        int quantity = 10;
        int cash = 20;
        mvc.perform(put("/api/init")
                .param("quantity", valueOf(quantity))
                .param("cash", valueOf(cash)))
                .andExpect(status().isOk());
        verify(service, only()).init(quantity, cash);
    }

    @Test
    public void placeBidTest() throws Exception {
        int bid = 10;
        when(service.placeBid()).thenReturn(bid);
        mvc.perform(get("/api/placeBid")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bid").exists())
                .andExpect(jsonPath("$.bid", is(bid)));
        verify(service, only()).placeBid();
    }

    @Test
    public void showBidTest() throws Exception {
        int own = 5;
        int other = 6;
        mvc.perform(post("/api/bids")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("own", valueOf(own))
                        .param("other", valueOf(other)))
                .andExpect(status().isOk());
        verify(service, only()).bids(own, other);
    }
}
