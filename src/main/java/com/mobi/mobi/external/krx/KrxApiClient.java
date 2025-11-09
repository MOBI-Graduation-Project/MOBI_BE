package com.mobi.mobi.external.krx;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
@Component
public class KrxApiClient {

    private final WebClient webClient;
    @Value("${api.krx.key}")
    private String serviceKey;

    public KrxApiClient() {

        ExchangeStrategies exchangeStrategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024)) // 10MB
                .build();

        this.webClient = WebClient.builder()
                .exchangeStrategies(exchangeStrategies)
                .build();
    }

    public List<KrxStockInfo> getStockInfo(String date) {
        URI uri = UriComponentsBuilder
                .fromUriString("https://data-dbg.krx.co.kr")
                .path("/svc/apis/sto/stk_bydd_trd")
                .queryParam("AUTH_KEY", serviceKey)
                .queryParam("basDd", date)
                .queryParam("resultType", "json")
                .encode()
                .build()
                .toUri();

        KrxApiResponse response = webClient.get()
                .uri(uri)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(KrxApiResponse.class)
                .block();


        // 응답이 없거나, 데이터 블록(OutBlock_1)이 비어있으면 실패로 간주
        if (response == null || response.getOutBlock1() == null) {
            System.out.println("KRX API 호출에 실패했거나 데이터가 없습니다.");
            return Collections.emptyList();
        }

        // 성공 시 데이터 리스트 반환
        return response.getOutBlock1();
    }
}