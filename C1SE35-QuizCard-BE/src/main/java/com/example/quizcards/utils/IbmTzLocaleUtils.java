package com.example.quizcards.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.Locale;

@Component
@Slf4j
public class IbmTzLocaleUtils {
    public Locale getLocale(String localeCode) {
        if (!StringUtils.hasText(localeCode)) {
            return null;
        }

        com.ibm.icu.util.ULocale input = new com.ibm.icu.util.ULocale(localeCode);
        com.ibm.icu.util.ULocale fullLocale = com.ibm.icu.util.ULocale.addLikelySubtags(input);

        if ("".equalsIgnoreCase(fullLocale.getCountry())) {
            return Locale.forLanguageTag("en-US"); // default language
        } else {
            return fullLocale.toLocale();
        }
    }

    // Returnn value: Hours - Minutes
    public int[] getOffsetHoursBaseOnLocale(String localeCode) {
        Locale locale = getLocale(localeCode);
        if (locale == null) {
            return new int[] {0, 0}; // default offset
        }

        String[] timeZoneIds = com.ibm.icu.util.TimeZone.getAvailableIDs(locale.getCountry());
        if (timeZoneIds.length == 0) {
            log.warn("WARN: Cannot find timezone for country: {}, default value (0) returned", locale.getCountry());
            return new int[] {0, 0}; // default offset
        }

        // Select the first timezone (usually the most common one)
        com.ibm.icu.util.TimeZone timeZone = com.ibm.icu.util.TimeZone.getTimeZone(timeZoneIds[0]);

        // Convert timezone to offset in milliseconds
        int rawOffsetMillis = timeZone.getOffset(new Date().getTime());

        // convert to hours
        int hours = rawOffsetMillis / (1000 * 60 * 60);
        // convert to minutes
        int minutes = Math.abs((rawOffsetMillis / (1000 * 60)) % 60);

        return new int[] {hours, minutes};
    }
}
