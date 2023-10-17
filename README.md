# trading-bot
Test task Trading Bot

The task was performed using the Spring Boot framework, because there were no requirements for the application 
and I assumed that it could be a web application (http://localhost:8081/) with api and swagger for more convenience (/swagger-ui/index.html ). 
The application runs on port 8081. There are three endpoints that call the interface methods of the same name from the task statement: PUT/api/init, POST/api/bids, GET/api/placeBid. 
The main strategy chosen is the planning of bets for all rounds at the beginning based on the arithmetic mean of the total available amount of monetary units and the total quantity units. 
Bids are made based on this plan, as well as the deviation of the opponent's last bid from the average and the amount of his remaining cash. 
The idea is quite simple - if the opponent has spent more money, then the probability increases that in the remaining rounds his bid will be less than the average, and vice versa. 
Depending on this, bidder can either increase the current bid at the expense of one and future bids, or by reducing the current bid, increase the future bid. 
At the same time, zero bets are not provided because the strategy is designed to win the average bet on the distance. 
This is not the most creative approach, but when tested, it showed better results than others that came to my mind.
