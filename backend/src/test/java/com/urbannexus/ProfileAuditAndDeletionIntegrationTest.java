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
import com.urbannexus.service.AdminService;
import com.urbannexus.service.AuditService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Profile updates, audit-log consistency and attribution, resident deletion with unpaid invoices, case-insensitive
 * search, and past-date booking checks. Runs on its own in-memory database; tests use distinct seeded accounts and
 * invoices because that database is shared across the tests in this class.
 */
@SpringBootTest(properties = {
    "app.jwt.secret=dGVzdC1zZWNyZXQtdGhhdC1pcy1hdC1sZWFzdC00OC1ieXRlcy1sb25nLWZvci1oczM4NA==",
    "spring.datasource.url=jdbc:h2:mem:urbannexus-profile-test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
    "spring.h2.console.enabled=false",
    "spring.jpa.show-sql=false"
})
@AutoConfigureMockMvc
class ProfileAuditAndDeletionIntegrationTest {
    
    private static final String SEED_PASSWORD = "pwd123#";
    
    private static String adminToken;
    
    @Autowired
    private MockMvc mvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private JdbcTemplate jdbc;
    
    @Autowired
    private AuditService auditService;
    
    @Autowired
    private AdminService adminService;
    
    @Autowired
    private TransactionTemplate transactionTemplate;
    
    // ---------------------------------------------------------------- 9. resident profile: members
    
    private static MockHttpServletRequestBuilder withToken(MockHttpServletRequestBuilder request, String token) {
        return request.header("Authorization", "Bearer " + token);
    }
    
    // ---------------------------------------------------------------- 12. technician skill
    
    @Test
    void residentCanUpdateMembersFromTheProfileForm() throws Exception {
        String token = activate("ashley_w");
        
        // HTML number inputs send strings.
        updateProfile(token, Map.of("name", "Ashley White", "contact", "9123456788", "no_of_members", "4"))
            .andExpect(status().isOk());
        mvc.perform(withToken(get("/api/profile/me"), token)).andExpect(jsonPath("$.noOfMembers").value(4));
        
        updateProfile(token, Map.of("no_of_members", 5)).andExpect(status().isOk());
        mvc.perform(withToken(get("/api/profile/me"), token)).andExpect(jsonPath("$.noOfMembers").value(5));
        
        updateProfile(token, Map.of("no_of_members", "lots")).andExpect(status().isBadRequest());
        updateProfile(token, Map.of("no_of_members", 0)).andExpect(status().isBadRequest());
        updateProfile(token, Map.of("name", " ")).andExpect(status().isBadRequest());
        mvc.perform(withToken(get("/api/profile/me"), token))
            .andExpect(jsonPath("$.noOfMembers").value(5))
            .andExpect(jsonPath("$.name").value("Ashley White"));
    }
    
    // ---------------------------------------------------------------- 10. audit log
    
    @Test
    void technicianCannotChangeTheirOwnSkill() throws Exception {
        // Seed: tech 2 (tech_jacky) is an Electrician.
        String token = activate("tech_jacky");
        
        updateProfile(token, Map.of("skill", "HVAC")).andExpect(status().isForbidden());
        assertThat(jdbc.queryForObject("SELECT skill FROM technician WHERE tech_id = 2", String.class))
            .isEqualTo("Electrician");
        
        // The profile form echoes the unchanged skill back along with real edits.
        updateProfile(token, Map.of("skill", "Electrician", "contact", "9000000222", "available", false))
            .andExpect(status().isOk());
        assertThat(jdbc.queryForObject("SELECT contact FROM technician WHERE tech_id = 2", String.class))
            .isEqualTo("9000000222");
        updateProfile(token, Map.of("available", "nope")).andExpect(status().isBadRequest());
    }
    
    @Test
    void auditEntryIsRolledBackWithTheChangeItDescribes() {
        assertThatThrownBy(() -> transactionTemplate.executeWithoutResult(tx -> {
            auditService.log("resident", "rollback-probe", "DELETE", "Should never be visible");
            throw new IllegalStateException("operation failed after logging");
        })).isInstanceOf(IllegalStateException.class);
        
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM audit_log WHERE record_id = 'rollback-probe'",
            Long.class)).isZero();
    }
    
    // ---------------------------------------------------------------- 11. deleting a resident who owes money
    
    @Test
    void auditEntriesRecordWhoMadeTheChange() throws Exception {
        String token = activate("nicole_h");
        updateProfile(token, Map.of("contact", "9000000020")).andExpect(status().isOk());
        assertThat(latestAuditActor("resident", "20")).isEqualTo("nicole_h");
        
        // Work without a signed-in user (e.g. scheduled jobs) is attributed to "system".
        adminService.processOverduePayments();
        assertThat(latestAuditActor("payment", "ALL")).isEqualTo("system");
        
        JsonNode logs = body(mvc.perform(withToken(get("/api/admin/audit-logs"), admin()))
                                 .andExpect(status().isOk()).andReturn());
        assertThat(logs.get(0).has("changed_by")).isTrue();
    }
    
    @Test
    void deletingAResidentWithUnpaidInvoicesNeedsAnExplicitWriteOff() throws Exception {
        // Seed: resident 2 (Jane Smith) has TXN-101 Pending (590.00 incl. GST) and TXN-102 Paid.
        BigDecimal pendingBefore = pendingLedger();
        
        JsonNode refused = body(mvc.perform(withToken(delete("/api/residents/2"), admin()))
                                    .andExpect(status().isConflict()).andReturn());
        assertThat(refused.get("code").asText()).isEqualTo("UNPAID_INVOICES");
        assertThat(refused.get("unpaid_count").asLong()).isEqualTo(1);
        assertThat(refused.get("unpaid_total").decimalValue()).isEqualByComparingTo("590.00");
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM resident WHERE resident_id = 2", Long.class)).isOne();
        
        mvc.perform(withToken(delete("/api/residents/2"), admin()).param("write_off_unpaid", "true"))
            .andExpect(status().isOk());
        
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM resident WHERE resident_id = 2", Long.class)).isZero();
        assertThat(paymentStatus("TXN-101")).isEqualTo("Written Off");
        assertThat(paymentStatus("TXN-102")).isEqualTo("Paid");
        assertThat(pendingLedger()).isEqualByComparingTo(pendingBefore.subtract(new BigDecimal("590.00")));
        
        // The ledger still shows who was billed, marked as a former resident (no unit).
        JsonNode ledger = body(mvc.perform(withToken(get("/api/admin/transactions"), admin())
                                               .param("resident_name", "jane smith"))
                                   .andExpect(status().isOk()).andReturn());
        assertThat(ledger).hasSize(2).allSatisfy(tx -> {
            assertThat(tx.get("resident_name").asText()).isEqualTo("Jane Smith");
            assertThat(tx.get("house_block").isNull()).isTrue();
        });
        
        // A written-off invoice cannot be confirmed as paid afterwards.
        mvc.perform(withToken(post("/api/payments/TXN-101/confirm"), admin())).andExpect(status().isBadRequest());
    }
    
    // ---------------------------------------------------------------- 13. search is case-insensitive
    
    @Test
    void deletingAResidentWhoseInvoicesArePaidNeedsNoWriteOff() throws Exception {
        // Seed: resident 8 (Jessica Taylor) has only TXN-110, which is Paid.
        mvc.perform(withToken(delete("/api/residents/8"), admin())).andExpect(status().isOk());
        assertThat(jdbc.queryForObject("SELECT billed_to FROM payment WHERE trans_no = 'TXN-110'", String.class))
            .isEqualTo("Jessica Taylor");
        assertThat(paymentStatus("TXN-110")).isEqualTo("Paid");
    }
    
    // ---------------------------------------------------------------- 15. bookings in the past
    
    @Test
    void residentSearchIgnoresCase() throws Exception {
        JsonNode byName = searchResidents(Map.of("name", "john"));
        assertThat(byName.findValuesAsText("name")).contains("John Doe", "Michael Johnson");
        
        JsonNode byBlock = searchResidents(Map.of("name", "JOHN", "block", "a"));
        assertThat(byBlock.findValuesAsText("name")).contains("John Doe", "Michael Johnson");
        
        // An empty filter means "no filter", not "block equals ''".
        assertThat(searchResidents(Map.of("name", "john", "block", ""))).hasSize(byName.size());
    }
    
    // ---------------------------------------------------------------- helpers
    
    @Test
    void bookingsCannotBeMadeForPastDates() throws Exception {
        String token = activate("ryan_a");
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Kolkata"));
        
        JsonNode past = body(mvc.perform(withToken(post("/api/bookings/amenity"), token)
                                             .contentType(MediaType.APPLICATION_JSON)
                                             .content(json(Map.of("amenity_id", 5, "date", today.minusDays(1).toString(),
                                                 "slot", 1, "capacity_booked", 1))))
                                 .andExpect(status().isBadRequest()).andReturn());
        assertThat(past.get("error").asText()).contains("cannot be in the past");
        
        mvc.perform(withToken(post("/api/bookings/technician"), token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("skill", "HVAC", "assign_date", today.minusDays(3).toString(), "slot", 1))))
            .andExpect(status().isBadRequest());
        
        mvc.perform(withToken(post("/api/bookings/amenity"), token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("amenity_id", 5, "date", today.toString(), "slot", 3,
                            "capacity_booked", 1))))
            .andExpect(status().isCreated());
    }
    
    private synchronized String admin() throws Exception {
        if (adminToken == null) {
            String temporary = login("admin", "Bhumika@1234").get("token").asText();
            adminToken = changePassword(temporary, "Bhumika@1234", "admin-test-secret");
        }
        return adminToken;
    }
    
    /**
     * Logs a seeded account in and completes its forced password change; returns a usable token.
     */
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
    
    private JsonNode searchResidents(Map<String, String> params) throws Exception {
        MockHttpServletRequestBuilder request = withToken(get("/api/admin/residents/search"), admin());
        new HashMap<>(params).forEach(request::param);
        return body(mvc.perform(request).andExpect(status().isOk()).andReturn());
    }
    
    private BigDecimal pendingLedger() throws Exception {
        return body(mvc.perform(withToken(get("/api/admin/stats"), admin())).andExpect(status().isOk()).andReturn())
                   .get("pendingLedger").decimalValue();
    }
    
    private String paymentStatus(String transNo) {
        return jdbc.queryForObject("SELECT status FROM payment WHERE trans_no = ?", String.class, transNo);
    }
    
    private String latestAuditActor(String table, String recordId) {
        return jdbc.queryForObject("SELECT changed_by FROM audit_log WHERE table_affected = ? AND record_id = ? "
                                       + "ORDER BY log_id DESC LIMIT 1", String.class, table, recordId);
    }
    
    private JsonNode body(MvcResult result) throws Exception {
        String content = result.getResponse().getContentAsString();
        return content.isEmpty() ? objectMapper.nullNode() : objectMapper.readTree(content);
    }
    
    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }
}
