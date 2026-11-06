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

package com.urbannexus.service;

import com.urbannexus.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.Locale;

/**
 * Derives login usernames as {@code <name><id>} (e.g. {@code jane_smith42}). Residents and technicians share one
 * username space and their ids come from separate sequences, so a candidate already taken gets a {@code _2},
 * {@code _3}, ... suffix. The unique constraint on {@code admin.username} remains the final guard against a
 * concurrent insert.
 */
@Component
@RequiredArgsConstructor
public class UsernameGenerator {

    // admin.username is VARCHAR(50).
    static final int MAX_LENGTH = 50;
    private static final int MAX_ATTEMPTS = 1000;

    private final AdminRepository adminRepository;

    public String generate(String name, long id) {
        String base = slug(name);
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            String suffix = attempt == 1 ? String.valueOf(id) : id + "_" + attempt;
            String candidate = join(base, suffix);
            if (!adminRepository.existsByUsername(candidate)) {
                return candidate;
            }
        }
        throw new IllegalStateException("Could not find a free username for id " + id);
    }

    /** Lower-case ASCII letters and digits joined by single underscores; accented letters lose the accent (é → e). */
    static String slug(String name) {
        String ascii = Normalizer.normalize(name, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        String slug = ascii.toLowerCase(Locale.ROOT)
                          .replaceAll("[^a-z0-9]+", "_")
                          .replaceAll("^_+|_+$", "");
        return slug.isEmpty() ? "user" : slug;
    }

    private static String join(String base, String suffix) {
        int room = MAX_LENGTH - suffix.length() - 1; // keep room for a separator
        String trimmed = base.length() > room ? base.substring(0, room).replaceAll("_+$", "") : base;
        // A name ending in a digit gets a separator, so "Ann1" #2 (ann1_2) can't equal "Ann" #12 (ann12).
        boolean needsSeparator = !trimmed.isEmpty() && Character.isDigit(trimmed.charAt(trimmed.length() - 1));
        return trimmed + (needsSeparator ? "_" : "") + suffix;
    }
}
