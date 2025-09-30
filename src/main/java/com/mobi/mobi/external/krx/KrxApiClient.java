package com.mobi.mobi.external.krx;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.Collections;
import java.util.List;

@Component
public class KrxApiClient {

    private final WebClient webClient;

    @Value("${api.krx.key}")
    private String serviceKey;

    public KrxApiClient() {
        this.webClient = WebClient.create("https://data-dbg.krx.co.kr");
    }

    public List<KrxStockInfo> getStockInfo(String date) {
        KrxApiResponse response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/svc/apis/sto/stk_bydd_trd")
                        .queryParam("serviceKey", serviceKey)
                        .queryParam("basDd", date)
                        .queryParam("resultType", "json") // 응답 타입을 JSON으로 명시
                        .build())
                .retrieve()
                .bodyToMono(KrxApiResponse.class)
                .block(); // 동기 방식으로 결과를 받아옴

        // API 호출 실패 또는 에러 응답 처리
        if (response == null || !"00".equals(response.getResponse().getHeader().getResultCode())) {
            // 실제로는 로깅을 하거나, 구체적인 예외를 던지는 것이 좋습니다.
            System.out.println("KRX API 호출에 실패했습니다.");
            return Collections.emptyList(); // 빈 리스트 반환
        }

        return response.getResponse().getBody().getItems().getItemList();
    }
}