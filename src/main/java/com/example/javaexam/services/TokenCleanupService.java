package com.example.javaexam.services;

import com.example.javaexam.repositories.AuthTokenRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Periodically purges expired auth tokens so the token table doesn't grow
 * without bound. Interval is {@code app.cleanup.interval-ms}; the
 * first run is delayed by that same interval so it doesn't fire at startup.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TokenCleanupService {

    private final AuthTokenRepository authTokenRepository;

    @Scheduled(initialDelayString = "${app.cleanup.interval-ms}", fixedRateString = "${app.cleanup.interval-ms}")
    @Transactional
    public void purgeExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        int deleted = authTokenRepository.deleteAllExpired(now);

        if (deleted > 0) {
            log.info("Token cleanup removed {} expired auth tokens", deleted);
        }
    }
}
