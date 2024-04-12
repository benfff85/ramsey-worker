package com.setminusx.ramsey.worker.utility;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class TimeUtility {

    private TimeUtility() {
        // Masking default public constructor
    }

    public static LocalDateTime now() {
        return LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC);
    }

}
