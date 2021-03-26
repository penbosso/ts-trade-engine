package com.example.tstradeengine;

import com.example.tstradeengine.model.*;
import com.example.tstradeengine.service.TradeEngineService;
import com.example.tstradeengine.stratedy.TradeEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootApplication
@RestController
public class TsTradeEngineApplication {
    @Value("${spring.redis.host}")
    private String HOST;
    @Value("${spring.redis.port}")
    private Integer PORT;
    @Value("${spring.redis.password}")
    private String PASSWORD;
    private final Logger log = LoggerFactory.getLogger(TsTradeEngineApplication.class);

    @Bean
    public RestTemplate getRestTemplate() {
        return new RestTemplate();
    }

    @Bean
    public JedisPool getJedisPool() {
        return new JedisPool(new JedisPoolConfig(), HOST, PORT, 10000, PASSWORD);
    }

    @Autowired
    private RestTemplate restTemplate ;
    @Autowired
    private EngineQueueItemRepository engineQueueItemRepository;
    @Autowired
    private TradeEngineService tradeEngineService;

    public static void main(String[] args) {
        SpringApplication.run(TsTradeEngineApplication.class, args);
    }

    static int requestNumber = 0;
    // when ever there is data check queue
    @PostMapping("/mdsubscription1")
    public void marketDataSubscribtion1(@RequestBody List<MarketData> request) {
        requestNumber = requestNumber + 1;
        String exchangeDataUrl ="https://exchange.matraining.com";
        log.info("Data from Ex1 ->" + requestNumber + ": " + request.toString());
        updateMarketList(request, exchangeDataUrl);
    }

    @PostMapping("/mdsubscription2")
    public void marketDataSubscribtion2(@RequestBody List<MarketData> request) {
        requestNumber = requestNumber + 1;
        log.info("Data from Ex2 ->" + requestNumber + ": " + request.toString());
        String exchangeDataUrl ="https://exchange2.matraining.com";
        // when ever there is data
        updateMarketList(request, exchangeDataUrl);
    }
    @PostMapping("/mdsubscription3")
    public void marketDataSubscribtion3(@RequestBody List<MarketData> request) {
        requestNumber = requestNumber + 1;
        log.info("Data from Ex3 ->" + requestNumber + ": " + request.toString());
        String exchangeDataUrl ="https://exchange1.matraining.com";
        // when ever there is data
        updateMarketList(request, exchangeDataUrl);
    }

    private void updateMarketList(@RequestBody List<MarketData> request, String exchangeDataUrl) {
        System.out.println("+====In update market list fnx ===================================");
        // when ever there is data
        List<MarketData> mdList = request.stream().map(marketData -> {
            marketData.setExchangeUrl(exchangeDataUrl);
            // calculating change in price
            MarketData prevMarketData = MarketDataList.getMarketDataListByTickerAndUrl(marketData.getTICKER(), exchangeDataUrl);
            marketData.setChangeInPrice( marketData.getLAST_TRADED_PRICE() - prevMarketData.getLAST_TRADED_PRICE());
            System.out.println("+====Change in price for "+marketData.getTICKER()+" : ==="+marketData.getChangeInPrice()+"========");
            return marketData;
        }).collect(Collectors.toList());
        MarketDataList.merchData(mdList, exchangeDataUrl);
        // process queue
        tradeEngineService.processTradeQueue();
    }

    @PostConstruct
    public void onStartApp() {
        // sample
        List<Exchange> exchangeList = new ArrayList<Exchange>();
        exchangeList.add(new Exchange(1, "https://exchange.matraining.com", true));
        exchangeList.add(new Exchange(2, "https://exchange2.matraining.com", true));
        log.info("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
        log.info("^^^^Del subscription^^^^");
        log.info("^^^^subscription^^^^");
        String subscriberUrl = "https://41a5f5e26552.ngrok.io/mdsubscription";
        log.info("^^^^get initial deata^^^^");
        for (Exchange exchange : exchangeList) {
            if (exchange.isEnable()) {
                MarketDataList.merchData(getMarketDataList(exchange.getUrl()), exchange.getUrl());
                try {
                    subscribeToMarketData(exchange.getUrl()+"/md/subscription", subscriberUrl+exchange.getId());
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }
        }

        // get uncompleted orders from db to queue register
        EngineQueueRegister.mergeEngineQueueRegister(engineQueueItemRepository.findByStatus("incomplete"));

        log.info("^^" + MarketDataList.getMarketDataListByTicker("IBM").get(0).toString() + "^^");
        log.info("^^" + MarketDataList.getMarketDataListByTicker("IBM").get(1).toString() + "^^");
        log.info("^^^^^^^^^^^^" + "^^^^^^^^^^^^^^^^^^");
    }

    private void subscribeToMarketData(String exchangeUrl, String subscriberUrl) throws URISyntaxException {
        URI uri = new URI(exchangeUrl);;

        HttpHeaders headers = new HttpHeaders();
        Charset utf8 = Charset.forName("UTF-8");
        MediaType mediaType = new MediaType("text", "plain", utf8);
        headers.setContentType(mediaType);
        HttpEntity<String> request = new HttpEntity<>(subscriberUrl, headers);

        ResponseEntity<String> result = restTemplate.postForEntity(uri, request, String.class);

      log.info("$ Results of sub.: -> "+ result.getStatusCode());
      log.info("$ Results of sub.: -> "+ result.getBody());
    }

    private List<MarketData> getMarketDataList(String exchangeDataUrl) {

        //access to exchange list in exchange dow
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<List<MarketData>> responseEntity =
                restTemplate.exchange(
                        exchangeDataUrl + "/md",
                        HttpMethod.GET, null,
                        new ParameterizedTypeReference<List<MarketData>>() {
                        }
                );
        List<MarketData> MarketDataList = responseEntity.getBody();
        return MarketDataList.stream().map(marketData -> {
            marketData.setExchangeUrl(exchangeDataUrl);
            return marketData;
        }).collect(Collectors.toList());
    }


}
