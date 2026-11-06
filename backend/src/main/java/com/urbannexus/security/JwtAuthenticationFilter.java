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

import com.urbannexus.repository.AdminRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    // The only endpoints an account holding a temporary password may call.
    private static final Set<String> PASSWORD_CHANGE_PATHS = Set.of("/api/profile/me", "/api/profile/password");
    
    private static final String PASSWORD_CHANGE_REQUIRED_BODY =
        "{\"error\":\"You must change your temporary password before continuing.\","
            + "\"code\":\"PASSWORD_CHANGE_REQUIRED\"}";
    
    private final JwtTokenProvider tokenProvider;
    private final AdminRepository adminRepository;
    
    /**
     * Login is public and never needs the caller's token. Skipping it here means a stale token left in the
     * browser (e.g. one still flagged must-change-password) cannot block signing in again.
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return "/api/login".equals(request.getRequestURI());
    }
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        String path = request.getRequestURI();
        log.debug("Processing request: {} {}", request.getMethod(), path);
        
        String jwt = getJwtFromRequest(request);
        if (StringUtils.hasText(jwt)) {
            UserPrincipal principal = authenticate(jwt);
            if (principal != null) {
                if (principal.isMustChangePassword() && !PASSWORD_CHANGE_PATHS.contains(path)) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.getWriter().write(PASSWORD_CHANGE_REQUIRED_BODY);
                    return;
                }
                
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    principal, null, principal.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        
        filterChain.doFilter(request, response);
    }
    
    /**
     * Returns the principal for a token that is correctly signed, unexpired, and still current for its account,
     * or {@code null} otherwise. A token is no longer current once the account is deleted or its token version has
     * been bumped by a password change or reset.
     */
    private UserPrincipal authenticate(String jwt) {
        Claims claims;
        try {
            claims = tokenProvider.getClaimsFromJWT(jwt);
        } catch (JwtException | IllegalArgumentException ex) {
            log.warn("Rejected JWT: {}", ex.getMessage());
            return null;
        }
        
        Long id = extractLong(claims, "id");
        Long tokenVersion = extractLong(claims, "ver");
        if (id == null || tokenVersion == null) {
            log.warn("Rejected JWT for '{}': missing account id or token version", claims.getSubject());
            return null;
        }
        
        Optional<AccountState> state = adminRepository.findAccountState(id);
        if (state.isEmpty()) {
            log.warn("Rejected JWT for '{}': account no longer exists", claims.getSubject());
            return null;
        }
        if (tokenVersion.intValue() != state.get().tokenVersion()) {
            log.warn("Rejected JWT for '{}': token has been revoked", claims.getSubject());
            return null;
        }
        
        return new UserPrincipal(
            id,
            claims.getSubject(),
            null,
            claims.get("role", String.class),
            extractLong(claims, "resident_id"),
            extractLong(claims, "tech_id"),
            state.get().tokenVersion(),
            Boolean.TRUE.equals(state.get().mustChangePassword()));
    }
    
    private Long extractLong(Claims claims, String key) {
        Object val = claims.get(key);
        if (val == null)
            return null;
        if (val instanceof Number)
            return ((Number) val).longValue();
        return null;
    }
    
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
