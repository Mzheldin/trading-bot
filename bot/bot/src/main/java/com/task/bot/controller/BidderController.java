package com.task.bot.controller;

import com.task.bot.dto.BidDTO;
import com.task.bot.service.Bidder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class BidderController {

    private final Bidder bidder;

    public BidderController(Bidder bidder) {
        this.bidder = bidder;
    }

    @Operation(
            summary = "Initializes the bidder with the production quantity and the allowed cash limit.",
            parameters = {
                    @Parameter(in = ParameterIn.QUERY, required = true, name = "quantity", description = "the quantity"),
                    @Parameter(in = ParameterIn.QUERY, required = true, name = "cash", description = "the cash limit")},
            responses = {@ApiResponse(responseCode = "200", description = "Bidder successfully initialized")})
    @PutMapping("/init")
    public ResponseEntity<Void> init(@RequestParam("quantity") Integer quantity, @RequestParam("cash") Integer cash) {
        bidder.init(quantity, cash);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Retrieves the next bid for the product, which may be zero.",
            responses = {@ApiResponse(responseCode = "200", description = "Bid successfully placed",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = BidDTO.class)))})
    @GetMapping("/placeBid")
    public BidDTO placeBid() {
        return new BidDTO(bidder.placeBid());
    }

    @Operation(
            summary = "Shows the bids of the two bidders.",
            parameters = {
                    @Parameter(in = ParameterIn.QUERY, required = true, name = "own", description = "the bit of this bidder"),
                    @Parameter(in = ParameterIn.QUERY, required = true, name = "other", description = "the bid of other bidder")},
            responses = {@ApiResponse(responseCode = "200", description = "Bids successfully published")})
    @PostMapping("/bids")
    public void bids(@RequestParam("own") Integer own, @RequestParam("other") Integer other) {
        bidder.bids(own, other);
    }
}
