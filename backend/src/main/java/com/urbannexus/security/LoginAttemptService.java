/*
 * Copyright (c) 2026 Bhumika Agarwal
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.urbannexus.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Locks a username out of login for {@link #LOCK_DURATION} after {@link #MAX_FAILED_ATTEMPTS} consecutive failures.
 * State is in memory, so it is per instance and resets on restart — enough to make online password guessing
 * impractical for a single-instance deployment.
 */
@Slf4j
@Component
public class LoginAttemptService {
    
    static final int MAX_FAILED_ATTEMPTS = 5;
    static final Duration LOCK_DURATION = Duration.ofMinutes(15);
    
    private final Map<String, Attempts> attempts = new ConcurrentHashMap<>();
    private final Clock clock;
    
    public LoginAttemptService() {
        this(Clock.systemUTC());
    }
    
    LoginAttemptService(Clock clock) {
        this.clock = clock;
    }
    
    private static String key(String username) {
        return username == null ? "" : username.trim().toLowerCase(Locale.ROOT);
    }
    
    /**
     * Returns how long the username remains locked, or {@link Duration#ZERO} if it may attempt a login.
     */
    public Duration remainingLock(String username) {
        Attempts entry = attempts.get(key(username));
        if (entry == null || entry.lockedUntil == null) {
            return Duration.ZERO;
        }
        Duration remaining = Duration.between(clock.instant(), entry.lockedUntil);
        return remaining.isNegative() ? Duration.ZERO : remaining;
    }
    
    public void recordFailure(String username) {
        Instant now = clock.instant();
        attempts.compute(key(username), (k, entry) -> {
            // A lock that has run out starts a fresh window of attempts.
            if (entry == null || (entry.lockedUntil != null && !now.isBefore(entry.lockedUntil))) {
                entry = new Attempts();
            }
            entry.failures++;
            entry.lastFailure = now;
            if (entry.failures >= MAX_FAILED_ATTEMPTS) {
                entry.lockedUntil = now.plus(LOCK_DURATION);
                log.warn("Login locked for '{}' after {} failed attempts", k, entry.failures);
            }
            return entry;
        });
    }
    
    public void recordSuccess(String username) {
        attempts.remove(key(username));
    }
    
    // Attempts are keyed on any submitted username, so drop stale entries to keep the map bounded.
    @Scheduled(fixedRate = 10 * 60 * 1000)
    public void evictExpired() {
        Instant cutoff = clock.instant().minus(LOCK_DURATION);
        attempts.entrySet().removeIf(e -> e.getValue().lastFailure.isBefore(cutoff)
                                              && (e.getValue().lockedUntil == null
                                                      || e.getValue().lockedUntil.isBefore(clock.instant())));
    }
    
    private static final class Attempts {
        private int failures;
        private Instant lastFailure;
        private Instant lockedUntil;
    }
}
