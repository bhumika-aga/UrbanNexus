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

import org.junit.jupiter.api.Test;

import java.time.*;

import static org.assertj.core.api.Assertions.assertThat;

class LoginAttemptServiceTest {
    
    private final MutableClock clock = new MutableClock();
    private final LoginAttemptService service = new LoginAttemptService(clock);
    
    @Test
    void locksAfterMaxFailuresAndUnlocksAfterTheLockDuration() {
        for (int i = 0; i < LoginAttemptService.MAX_FAILED_ATTEMPTS - 1; i++) {
            service.recordFailure("Jane_S");
        }
        assertThat(service.remainingLock("jane_s")).isZero();
        
        service.recordFailure("jane_s");
        assertThat(service.remainingLock("JANE_S")).isEqualTo(LoginAttemptService.LOCK_DURATION);
        
        clock.advance(LoginAttemptService.LOCK_DURATION);
        assertThat(service.remainingLock("jane_s")).isZero();
        
        // After the lock runs out, a single new failure must not re-lock immediately.
        service.recordFailure("jane_s");
        assertThat(service.remainingLock("jane_s")).isZero();
    }
    
    @Test
    void successClearsTheFailureCount() {
        for (int i = 0; i < LoginAttemptService.MAX_FAILED_ATTEMPTS - 1; i++) {
            service.recordFailure("tech_tom");
        }
        service.recordSuccess("tech_tom");
        service.recordFailure("tech_tom");
        assertThat(service.remainingLock("tech_tom")).isZero();
    }
    
    @Test
    void evictionDropsOnlyStaleEntries() {
        for (int i = 0; i < LoginAttemptService.MAX_FAILED_ATTEMPTS; i++) {
            service.recordFailure("locked_user");
        }
        clock.advance(Duration.ofMinutes(5));
        service.evictExpired();
        assertThat(service.remainingLock("locked_user")).isPositive();
        
        clock.advance(LoginAttemptService.LOCK_DURATION);
        service.evictExpired();
        assertThat(service.remainingLock("locked_user")).isZero();
    }
    
    private static final class MutableClock extends Clock {
        private Instant now = Instant.parse("2026-11-06T10:00:00Z");
        
        void advance(Duration duration) {
            now = now.plus(duration);
        }
        
        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }
        
        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }
        
        @Override
        public Instant instant() {
            return now;
        }
    }
}
