package com.microservices.progressservice.client;

import com.microservices.progressservice.dto.response.SetProgressSettingResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import reactor.core.publisher.Mono;

import java.util.Map;

public interface SettingProgressReactiveClient {

    /**
     * GET /get-setting?setId={setId}
     * Trả về Mono<ResponseEntity<SetProgressSettingResponse>>
     */
    @GetExchange("/get-setting")
    Mono<ResponseEntity<SetProgressSettingResponse>> getDefaultSetting(
            @RequestParam("setId") Long setId,
            @RequestHeader Map<String, String> headers
    );

    /**
     * GET /get-effective-setting?setId={setId}
     */
    @GetExchange("/get-effective-setting")
    Mono<ResponseEntity<SetProgressSettingResponse>> getEffectiveSetting(
            @RequestParam("setId") Long setId,
            @RequestHeader Map<String, String> headers
    );

    /**
     * GET /get-current-simple-mode-version?cardId={cardId}
     */
    @GetExchange("/get-current-simple-mode-version")
    Mono<ResponseEntity<Map<String, Long>>> getCurrentSimpleModeVersion(
            @RequestParam("cardId") Long cardId,
            @RequestHeader Map<String, String> headers
    );
}
