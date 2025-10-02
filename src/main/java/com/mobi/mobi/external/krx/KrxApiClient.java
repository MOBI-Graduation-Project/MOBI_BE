package com.mobi.mobi.external.krx;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder; // ✨ UriComponentsBuilder 임포트
import java.net.URI; // ✨ URI 임포트
import java.util.Collections;
import java.util.List;

@Component
public class KrxApiClient {

    private final WebClient webClient;

    @Value("${api.krx.key}")
    private String serviceKey;

    public KrxApiClient() {
        // 이제 기본 URL은 필요 없습니다.
        this.webClient = WebClient.create();
    }

    public List<KrxStockInfo> getStockInfo(String date) {

        // ▼▼▼ [최종 수정] URL과 파라미터를 가장 안전한 방식으로 조립합니다. ▼▼▼
        URI uri = UriComponentsBuilder
                .fromUriString("https://data-dbg.krx.co.kr")
                .path("/svc/apis/sto/stk_bydd_trd")
                .queryParam("serviceKey", serviceKey) // 키를 여기서 전달하면, Spring이 표준 인코딩을 수행합니다.
                .queryParam("basDd", date)
                .queryParam("resultType", "json")
                .encode() // 인코딩을 수행하도록 명시
                .build()
                .toUri();

        KrxApiResponse response = webClient.get()
                .uri(uri) // 위에서 만든 최종 URI 사용
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(KrxApiResponse.class)
                .block();

        // API 호출 실패 또는 에러 응답 처리
        if (response == null || !"00".equals(response.getResponse().getHeader().getResultCode())) {
            System.out.println("KRX API 호출에 실패했습니다.");
            if (response != null) {
                System.out.println("Error Code: " + response.getResponse().getHeader().getResultCode());
                System.out.println("Error Message: " + response.getResponse().getHeader().getResultMsg());
            }
            return Collections.emptyList();
        }

        return response.getResponse().getBody().getItems().getItemList();
    }
}