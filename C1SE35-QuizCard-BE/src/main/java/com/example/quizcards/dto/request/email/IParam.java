package com.example.quizcards.dto.request.email;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = OtpParam.class, name = "OtpParam"),
        @JsonSubTypes.Type(value = ConfirmEmailParam.class, name = "ConfirmEmailParam"),
})
public interface IParam {
}
