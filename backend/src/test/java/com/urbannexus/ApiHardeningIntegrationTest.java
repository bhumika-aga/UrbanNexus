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

package com.urbannexus;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Error responses, CORS, admin username changes, dashboard figures, open assignments, payment dates, seed data and
 * the bookable-skill list. Runs on its own in-memory database; tests use distinct seeded accounts and invoices because
 * that database is shared across the tests in this class.
 */
@SpringBootTest(properties = {
    "app.jwt.secret=dGVzdC1zZWNyZXQtdGhhdC1pcy1hdC1sZWFzdC00OC1ieXRlcy1sb25nLWZvci1oczM4NA==",
    "spring.datasource.url=jdbc:h2:mem:urbannexus-hardening-test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
    "spring.h2.console.enabled=false",
    "spring.jpa.show-sql=false",
    "app.cors.allowed-origins=https://urbannexus.example.com, http://localhost:*"
})
@AutoConfigureMockMvc
class ApiHardeningIntegrationTest {

    private static final String SEED_PASSWORD = "pwd123#";

    private static String adminToken;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbc;

    // ---------------------------------------------------------------- 17. no internal error text

    @Test
    void errorResponsesCarryOnlyDeliberateMessages() throws Exception {
        String token = activate("emily_d");

        // A malformed number used to come back as the raw NumberFormatException text ("For input string: ...").
        JsonNode badNumber = body(mvc.perform(withToken(post("/api/bookings/amenity"), token)
                                                  .contentType(MediaType.APPLICATION_JSON)
                                                  .content(json(Map.of("amenity_id", "abc", "date", "2031-01-01",
                                                      "slot", 1, "capacity_booked", 1))))
                                      .andExpect(status().isBadRequest()).andReturn());
        assertThat(badNumber.get("error").asText()).isEqualTo("Invalid booking request.");

        mvc.perform(withToken(delete("/api/technicians/9999"), admin()))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").value("Technician not found."));
        mvc.perform(withToken(post("/api/residents/9999/reset-password"), admin()))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").value("Resident not found."));
        mvc.perform(withToken(post("/api/payments/NO-SUCH-TXN/confirm"), admin()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Invoice not found or already paid."));

        mvc.perform(post("/api/login").contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("username", "ghost", "password", "x"))))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error").value("Invalid username or password"));
    }

    // ---------------------------------------------------------------- 18. CORS and the H2 console

    @Test
    void corsAllowsOnlyConfiguredOriginsWithoutCredentials() throws Exception {
        mvc.perform(options("/api/login")
                        .header(HttpHeaders.ORIGIN, "https://urbannexus.example.com")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
            .andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "https://urbannexus.example.com"))
            .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS));
        mvc.perform(options("/api/login")
                        .header(HttpHeaders.ORIGIN, "http://localhost:5173")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
            .andExpect(status().isOk());

        // Any other Render app (or any other site) used to be allowed.
        mvc.perform(options("/api/login")
                        .header(HttpHeaders.ORIGIN, "https://attacker.onrender.com")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
            .andExpect(status().isForbidden())
            .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    @Test
    void h2ConsoleIsNotPublicWhenDisabled() throws Exception {
        mvc.perform(get("/h2-console/")).andExpect(status().isUnauthorized());
    }

    // ---------------------------------------------------------------- 19. admin username changes

    @Test
    void adminUsernameChangesAreValidated() throws Exception {
        updateProfile(admin(), Map.of("username", "jane_s")).andExpect(status().isConflict());
        updateProfile(admin(), Map.of("username", "  ")).andExpect(status().isBadRequest());
        updateProfile(admin(), Map.of("username", "not allowed")).andExpect(status().isBadRequest());
        updateProfile(admin(), Map.of("username", "x".repeat(51))).andExpect(status().isBadRequest());

        updateProfile(admin(), Map.of("username", "estate.manager")).andExpect(status().isOk());
        mvc.perform(withToken(get("/api/profile/me"), admin())).andExpect(jsonPath("$.username").value("estate.manager"));
        login("estate.manager", "admin-test-secret");

        updateProfile(admin(), Map.of("username", "admin")).andExpect(status().isOk());
    }

    // ---------------------------------------------------------------- 20 / 21. dashboard and open assignments

    @Test
    void dashboardCountsOnlyRealOpenWork() throws Exception {
        // Seed: 3 Assigned technician jobs; every seeded amenity booking is in the past.
        JsonNode before = stats();
        assertThat(before.has("gridUptime")).isFalse();
        assertThat(before.get("pendingTickets").asLong()).isEqualTo(3);

        String token = activate("sarah_w");
        String tomorrow = LocalDate.now(ZoneId.of("Asia/Kolkata")).plusDays(1).toString();
        mvc.perform(withToken(post("/api/bookings/amenity"), token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("amenity_id", 5, "date", tomorrow, "slot", 2, "capacity_booked", 2))))
            .andExpect(status().isCreated());
        assertThat(stats().get("pendingTickets").asLong()).isEqualTo(4);

        long reviewsBefore = stats().get("paymentsToReview").asLong();
        String chris = activate("chris_b");
        mvc.perform(withToken(post("/api/payments/TXN-106/submit"), chris)
                        .contentType(MediaType.APPLICATION_JSON).content(json(Map.of("reference", "UPI-77"))))
            .andExpect(status().isOk());
        assertThat(stats().get("paymentsToReview").asLong()).isEqualTo(reviewsBefore + 1);
    }

    @Test
    void openAssignmentsExcludeCompletedJobs() throws Exception {
        JsonNode open = body(mvc.perform(withToken(get("/api/admin/assignments"), admin()))
                                 .andExpect(status().isOk()).andReturn());
        assertThat(open).isNotEmpty();
        assertThat(open.findValuesAsText("status")).doesNotContain("Completed");
    }

    // ---------------------------------------------------------------- 22. payment dates

    @Test
    void confirmingAPaymentRecordsWhenItWasPaid() throws Exception {
        // Seed: TXN-109 is Pending.
        assertThat(jdbc.queryForObject("SELECT paid_at FROM payment WHERE trans_no = 'TXN-109'", Object.class)).isNull();
        mvc.perform(withToken(post("/api/payments/TXN-109/confirm"), admin())).andExpect(status().isOk());

        JsonNode ledger = body(mvc.perform(withToken(get("/api/admin/transactions"), admin()).param("status", "Paid"))
                                   .andExpect(status().isOk()).andReturn());
        assertThat(ledger).allSatisfy(tx -> {
            assertThat(tx.get("paid_at").isNull()).as("paid_at of %s", tx.get("trans_no")).isFalse();
            assertThat(tx.get("issued_at").isNull()).isFalse();
        });
        assertThat(ledger.findValuesAsText("trans_no")).contains("TXN-109");
    }

    // ---------------------------------------------------------------- 23. seed data

    @Test
    void seedInvoicesMatchThePriceListWithGst() {
        List<Map<String, Object>> mismatches = jdbc.queryForList("""
            SELECT p.trans_no, p.cost, pr.base_price FROM payment p
            JOIN technician_management tm ON tm.trans_no = p.trans_no
            JOIN technician t ON t.tech_id = tm.tech_id
            JOIN pricing pr ON pr.item_name = t.skill AND pr.category = 'Technician'
            WHERE p.trans_no LIKE 'TXN-1__' AND p.cost <> ROUND(pr.base_price * 1.18, 2)
            UNION ALL
            SELECT p.trans_no, p.cost, pr.base_price FROM payment p
            JOIN amenity_mgmt am ON am.trans_no = p.trans_no
            JOIN amenity a ON a.amenity_id = am.amenity_id
            JOIN pricing pr ON pr.item_name = a.name AND pr.category = 'Amenity'
            WHERE p.trans_no LIKE 'TXN-1__' AND p.cost <> ROUND(pr.base_price * 1.18, 2)
            """);
        assertThat(mismatches).isEmpty();

        // Seeded statuses agree with the 30-day overdue rule.
        assertThat(jdbc.queryForObject("""
            SELECT COUNT(*) FROM payment WHERE trans_no LIKE 'TXN-1__' AND (
                (status = 'Overdue' AND payment_date >= NOW() - INTERVAL '30' DAY)
                OR (status = 'Pending' AND payment_date < NOW() - INTERVAL '30' DAY))
            """, Long.class)).isZero();
    }

    // ---------------------------------------------------------------- 25. bookable skills, task dates

    @Test
    void bookableSkillsComeFromThePriceList() throws Exception {
        String token = activate("ashley_w");
        JsonNode skills = body(mvc.perform(withToken(get("/api/bookings/technician-skills"), token))
                                   .andExpect(status().isOk()).andReturn());

        assertThat(skills.findValuesAsText("skill")).contains("Plumber", "Landscaper").doesNotContain("Spa & Wellness Center");
        for (JsonNode skill : skills) {
            if ("Plumber".equals(skill.get("skill").asText())) {
                assertThat(skill.get("price_with_gst").decimalValue()).isEqualByComparingTo(new BigDecimal("590.00"));
            }
        }

        mvc.perform(withToken(get("/api/bookings/technician-skills"), activate("tech_derek")))
            .andExpect(status().isForbidden());
    }

    @Test
    void taskDatesAreSentAsPlainDates() throws Exception {
        // The technician dashboard compares these to today's date as YYYY-MM-DD strings.
        JsonNode tasks = body(mvc.perform(withToken(get("/api/technicians/me/tasks"), activate("tech_frank")))
                                  .andExpect(status().isOk()).andReturn());
        assertThat(tasks).isNotEmpty();
        assertThat(tasks.findValuesAsText("assign_date")).allMatch(date -> date.matches("\\d{4}-\\d{2}-\\d{2}"));
    }

    // ---------------------------------------------------------------- helpers

    private synchronized String admin() throws Exception {
        if (adminToken == null) {
            String temporary = login("admin", "Bhumika@1234").get("token").asText();
            adminToken = changePassword(temporary, "Bhumika@1234", "admin-test-secret");
        }
        return adminToken;
    }

    /** Logs a seeded account in and completes its forced password change; returns a usable token. */
    private String activate(String username) throws Exception {
        String temporary = login(username, SEED_PASSWORD).get("token").asText();
        return changePassword(temporary, SEED_PASSWORD, username + "-secret");
    }

    private JsonNode login(String username, String password) throws Exception {
        return body(mvc.perform(post("/api/login")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(json(Map.of("username", username, "password", password))))
                        .andExpect(status().isOk())
                        .andReturn());
    }

    private String changePassword(String token, String current, String next) throws Exception {
        return body(mvc.perform(withToken(put("/api/profile/password"), token)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(json(Map.of("currentPassword", current, "newPassword", next))))
                        .andExpect(status().isOk())
                        .andReturn()).get("token").asText();
    }

    private ResultActions updateProfile(String token, Map<String, Object> updates) throws Exception {
        return mvc.perform(withToken(put("/api/profile/update"), token)
                               .contentType(MediaType.APPLICATION_JSON)
                               .content(json(updates)));
    }

    private JsonNode stats() throws Exception {
        return body(mvc.perform(withToken(get("/api/admin/stats"), admin())).andExpect(status().isOk()).andReturn());
    }

    private static MockHttpServletRequestBuilder withToken(MockHttpServletRequestBuilder request, String token) {
        return request.header("Authorization", "Bearer " + token);
    }

    private JsonNode body(MvcResult result) throws Exception {
        String content = result.getResponse().getContentAsString();
        return content.isEmpty() ? objectMapper.nullNode() : objectMapper.readTree(content);
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }
}
