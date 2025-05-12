package com.example.quizcards.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public class PreserveOffsetDateTimeDeserializerUtils extends JsonDeserializer<OffsetDateTime> {
    // Sử dụng luôn ISO_OFFSET_DATE_TIME để parse nano và timezone
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    @Override
    public OffsetDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String text = p.getText().trim();
        return OffsetDateTime.parse(text, FORMATTER);
    }
}