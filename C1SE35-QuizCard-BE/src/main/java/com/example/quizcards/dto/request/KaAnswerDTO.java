package com.example.quizcards.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KaAnswerDTO {
    private Long answerId;
    private BigDecimal completionTime;
    private Boolean isTrue;
    private Long questionId;
    private String pinCode;

}
