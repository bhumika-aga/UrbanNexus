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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end checks for account takeover, task-status IDOR, self-approved payments and token revocation, run against
 * the seeded H2 dataset. Each test works on its own seeded accounts and invoices because the context (and database)
 * is shared across tests.
 */
@SpringBootTest(properties = {
    "app.jwt.secret=dGVzdC1zZWNyZXQtdGhhdC1pcy1hdC1sZWFzdC00OC1ieXRlcy1sb25nLWZvci1oczM4NA==",
    "spring.datasource.url=jdbc:h2:mem:urbannexus-test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
    "spring.h2.console.enabled=false",
    "spring.jpa.show-sql=false"
})
@AutoConfigureMockMvc
class SecurityFixesIntegrationTest {
    
    private static final String SEED_PASSWORD = "pwd123#";
    
    // The seeded admin must rotate its password before doing anything, and the context is shared,
    // so every test reuses one rotated admin session.
    private static String adminToken;
    
    @Autowired
    private MockMvc mvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    // ---------------------------------------------------------------- 1. default passwords
    
    private static String statusOfAssignment(JsonNode assignments, long id) {
        for (JsonNode assignment : assignments) {
            if (assignment.get("assignment_id").asLong() == id) {
                return assignment.get("status").asText();
            }
        }
        return null;
    }
    
    private static MockHttpServletRequestBuilder withToken(MockHttpServletRequestBuilder request, String token) {
        return request.header("Authorization", "Bearer " + token);
    }
    
    @Test
    void seededAccountCanOnlyChangeItsPasswordUntilItDoes() throws Exception {
        JsonNode login = login("jane_s", SEED_PASSWORD, 200);
        assertThat(login.at("/admin/mustChangePassword").asBoolean()).isTrue();
        String temporaryToken = login.get("token").asText();
        
        mvc.perform(withToken(get("/api/residents/me/dues"), temporaryToken))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.code").value("PASSWORD_CHANGE_REQUIRED"));
        mvc.perform(withToken(get("/api/profile/me"), temporaryToken)).andExpect(status().isOk());
        
        String token = changePassword(temporaryToken, SEED_PASSWORD, "jane-new-secret");
        mvc.perform(withToken(get("/api/residents/me/dues"), token)).andExpect(status().isOk());
        assertThat(login("jane_s", "jane-new-secret", 200).at("/admin/mustChangePassword").asBoolean()).isFalse();
    }
    
    @Test
    void newPasswordMustDifferFromTheTemporaryOne() throws Exception {
        String token = login("amy_k", SEED_PASSWORD, 200).get("token").asText();
        mvc.perform(withToken(put("/api/profile/password"), token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("currentPassword", SEED_PASSWORD, "newPassword", SEED_PASSWORD))))
            .andExpect(status().isBadRequest());
    }
    
    @Test
    void loginLocksAfterFiveFailuresEvenForTheRightPassword() throws Exception {
        for (int i = 0; i < 5; i++) {
            login("michael_j", "wrong-guess-" + i, 401);
        }
        JsonNode locked = login("michael_j", SEED_PASSWORD, 429);
        assertThat(locked.get("error").asText()).contains("Too many failed login attempts");
    }
    
    @Test
    void unknownUsernameIsIndistinguishableFromWrongPassword() throws Exception {
        String unknown = login("nobody_here", "whatever", 401).get("error").asText();
        String wrong = login("ryan_a", "whatever", 401).get("error").asText();
        assertThat(unknown).isEqualTo(wrong);
    }
    
    @Test
    void newAccountsGetUniqueRandomTemporaryPasswords() throws Exception {
        Set<String> passwords = new HashSet<>();
        for (int i = 0; i < 3; i++) {
            JsonNode created = createResident("Temp Person " + i);
            String password = created.get("password").asText();
            assertThat(password).isNotEqualTo(SEED_PASSWORD).hasSize(12);
            passwords.add(password);
            
            JsonNode login = login(created.get("username").asText(), password, 200);
            assertThat(login.at("/admin/mustChangePassword").asBoolean()).isTrue();
        }
        assertThat(passwords).hasSize(3);
    }
    
    // ---------------------------------------------------------------- 2. task-status IDOR
    
    @Test
    void adminResetIssuesNewTemporaryPasswordAndRevokesSessions() throws Exception {
        JsonNode created = createResident("Reset Subject");
        String username = created.get("username").asText();
        String token = changePassword(login(username, created.get("password").asText(), 200).get("token").asText(),
            created.get("password").asText(), "chosen-by-resident");
        mvc.perform(withToken(get("/api/residents/me/dues"), token)).andExpect(status().isOk());
        
        long residentId = residentIdOf("Reset Subject");
        JsonNode reset = body(mvc.perform(withToken(post("/api/residents/" + residentId + "/reset-password"), admin()))
                                  .andExpect(status().isOk()).andReturn());
        
        mvc.perform(withToken(get("/api/residents/me/dues"), token)).andExpect(status().isUnauthorized());
        login(username, "chosen-by-resident", 401);
        assertThat(login(username, reset.get("password").asText(), 200).at("/admin/mustChangePassword").asBoolean())
            .isTrue();
    }
    
    @Test
    void onlyTheAdminCanResetPasswords() throws Exception {
        String technician = activate("tech_alex");
        mvc.perform(withToken(post("/api/residents/1/reset-password"), technician)).andExpect(status().isForbidden());
        mvc.perform(withToken(post("/api/technicians/1/reset-password"), technician)).andExpect(status().isForbidden());
    }
    
    // ---------------------------------------------------------------- 3. self-approved payments
    
    @Test
    void technicianCanOnlyCompleteTheirOwnAssignments() throws Exception {
        // Seed: assignment 1 belongs to tech 1 (tech_tom), assignment 2 to tech 2.
        String tom = activate("tech_tom");
        
        updateTask(tom, 2, "Completed").andExpect(status().isForbidden());
        updateTask(tom, 1, "Assigned").andExpect(status().isForbidden());
        updateTask(tom, 1, "Bogus").andExpect(status().isBadRequest());
        updateTask(tom, 99999, "Completed").andExpect(status().isNotFound());
        updateTask(tom, 1, "Completed").andExpect(status().isOk());
        
        JsonNode assignments = body(mvc.perform(withToken(get("/api/admin/assignments"), admin()))
                                        .andExpect(status().isOk()).andReturn());
        assertThat(statusOfAssignment(assignments, 1)).isEqualTo("Completed");
        assertThat(statusOfAssignment(assignments, 2)).isEqualTo("Assigned");
    }
    
    @Test
    void adminCanReopenAnAssignment() throws Exception {
        // Seed: assignment 3 is Completed.
        updateTask(admin(), 3, "Assigned").andExpect(status().isOk());
        updateTask(admin(), 3, "Done").andExpect(status().isBadRequest());
    }
    
    // ---------------------------------------------------------------- 4. token revocation
    
    @Test
    void residentSubmissionStaysUnpaidUntilAdminConfirms() throws Exception {
        // Seed: TXN-109 is a Pending invoice of resident 4 (emily_d); TXN-106 belongs to resident 5.
        String emily = activate("emily_d");
        
        submit(emily, "TXN-109", null).andExpect(status().isBadRequest());
        submit(emily, "TXN-106", "UPI-1").andExpect(status().isBadRequest());
        submit(emily, "TXN-109", "UPI-REF-123").andExpect(status().isOk());
        submit(emily, "TXN-109", "UPI-REF-123").andExpect(status().isBadRequest());
        
        JsonNode invoice = dueInvoice(emily, "TXN-109");
        assertThat(invoice.get("status").asText()).isEqualTo("Under Review");
        assertThat(invoice.get("payment_reference").asText()).isEqualTo("UPI-REF-123");
        
        mvc.perform(withToken(post("/api/payments/TXN-109/confirm"), emily)).andExpect(status().isForbidden());
        mvc.perform(withToken(post("/api/payments/TXN-109/pay"), emily))
            .andExpect(result -> assertThat(result.getResponse().getStatus()).isNotEqualTo(200));
        
        mvc.perform(withToken(post("/api/payments/TXN-109/confirm"), admin())).andExpect(status().isOk());
        assertThat(dueInvoice(emily, "TXN-109")).isNull();
        mvc.perform(withToken(post("/api/payments/TXN-109/confirm"), admin())).andExpect(status().isBadRequest());
    }
    
    @Test
    void rejectedSubmissionReturnsToUnpaid() throws Exception {
        String chris = activate("chris_b");
        submit(chris, "TXN-106", "CHQ-555").andExpect(status().isOk());
        
        mvc.perform(withToken(post("/api/payments/TXN-106/reject"), chris)).andExpect(status().isForbidden());
        mvc.perform(withToken(post("/api/payments/TXN-106/reject"), admin())).andExpect(status().isOk());
        
        JsonNode invoice = dueInvoice(chris, "TXN-106");
        assertThat(invoice.get("status").asText()).isEqualTo("Pending");
        assertThat(invoice.get("payment_reference").isNull()).isTrue();
        mvc.perform(withToken(post("/api/payments/TXN-106/reject"), admin())).andExpect(status().isBadRequest());
    }
    
    @Test
    void passwordChangeRevokesEarlierTokens() throws Exception {
        String first = activate("david_m");
        String second = login("david_m", "david_m-secret", 200).get("token").asText();
        
        String latest = changePassword(second, "david_m-secret", "david_m-rotated");
        mvc.perform(withToken(get("/api/residents/me/bookings"), first)).andExpect(status().isUnauthorized());
        mvc.perform(withToken(get("/api/residents/me/bookings"), second)).andExpect(status().isUnauthorized());
        mvc.perform(withToken(get("/api/residents/me/bookings"), latest)).andExpect(status().isOk());
    }
    
    // ---------------------------------------------------------------- helpers
    
    @Test
    void deletingAnAccountRevokesItsTokens() throws Exception {
        JsonNode created = createResident("Soon Gone");
        String username = created.get("username").asText();
        String token = changePassword(login(username, created.get("password").asText(), 200).get("token").asText(),
            created.get("password").asText(), "soon-gone-secret");
        mvc.perform(withToken(get("/api/profile/me"), token)).andExpect(status().isOk());
        
        mvc.perform(withToken(delete("/api/residents/" + residentIdOf("Soon Gone")), admin())).andExpect(status().isOk());
        mvc.perform(withToken(get("/api/profile/me"), token)).andExpect(status().isUnauthorized());
    }
    
    @Test
    void tokenWithoutVersionClaimIsRejected() throws Exception {
        // The pre-fix token format had no "ver" claim; such tokens are refused outright.
        String legacy = "eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJhZG1pbiIsImlkIjoxLCJyb2xlIjoiU3VwZXJBZG1pbiJ9.invalid";
        mvc.perform(withToken(get("/api/profile/me"), legacy)).andExpect(status().isUnauthorized());
    }
    
    private synchronized String admin() throws Exception {
        if (adminToken == null) {
            String temporary = login("admin", "Bhumika@1234", 200).get("token").asText();
            adminToken = changePassword(temporary, "Bhumika@1234", "admin-test-secret");
        }
        return adminToken;
    }
    
    /**
     * Logs a seeded account in and completes its forced password change; returns a usable token.
     */
    private String activate(String username) throws Exception {
        String temporary = login(username, SEED_PASSWORD, 200).get("token").asText();
        return changePassword(temporary, SEED_PASSWORD, username + "-secret");
    }
    
    private JsonNode login(String username, String password, int expectedStatus) throws Exception {
        MvcResult result = mvc.perform(post("/api/login")
                                           .contentType(MediaType.APPLICATION_JSON)
                                           .content(json(Map.of("username", username, "password", password))))
                               .andExpect(status().is(expectedStatus))
                               .andReturn();
        return body(result);
    }
    
    private String changePassword(String token, String current, String next) throws Exception {
        MvcResult result = mvc.perform(withToken(put("/api/profile/password"), token)
                                           .contentType(MediaType.APPLICATION_JSON)
                                           .content(json(Map.of("currentPassword", current, "newPassword", next))))
                               .andExpect(status().isOk())
                               .andReturn();
        JsonNode response = body(result);
        assertThat(response.at("/admin/mustChangePassword").asBoolean()).isFalse();
        return response.get("token").asText();
    }
    
    private JsonNode createResident(String name) throws Exception {
        Map<String, Object> payload = Map.of(
            "name", name, "house_block", "Z", "house_floor", "1", "house_unit", "101",
            "ownership_status", "Owner", "contact", "9000000000", "no_of_members", 1);
        return body(mvc.perform(withToken(post("/api/residents"), admin())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(json(payload)))
                        .andExpect(status().isCreated())
                        .andReturn());
    }
    
    private long residentIdOf(String name) throws Exception {
        JsonNode matches = body(mvc.perform(withToken(get("/api/admin/residents/search"), admin()).param("name", name))
                                    .andExpect(status().isOk()).andReturn());
        assertThat(matches).hasSize(1);
        return matches.get(0).get("residentId").asLong();
    }
    
    private org.springframework.test.web.servlet.ResultActions updateTask(String token, long id, String status)
        throws Exception {
        return mvc.perform(withToken(put("/api/technicians/tasks/" + id + "/status"), token)
                               .contentType(MediaType.APPLICATION_JSON)
                               .content(json(Map.of("status", status))));
    }
    
    private org.springframework.test.web.servlet.ResultActions submit(String token, String transNo, String reference)
        throws Exception {
        MockHttpServletRequestBuilder request = withToken(post("/api/payments/" + transNo + "/submit"), token)
                                                    .contentType(MediaType.APPLICATION_JSON);
        if (reference != null) {
            request.content(json(Map.of("reference", reference)));
        }
        return mvc.perform(request);
    }
    
    private JsonNode dueInvoice(String token, String transNo) throws Exception {
        JsonNode dues = body(mvc.perform(withToken(get("/api/residents/me/dues"), token))
                                 .andExpect(status().isOk()).andReturn());
        for (JsonNode invoice : dues.get("invoices")) {
            if (transNo.equals(invoice.get("trans_no").asText())) {
                return invoice;
            }
        }
        return null;
    }
    
    private JsonNode body(MvcResult result) throws Exception {
        String content = result.getResponse().getContentAsString();
        return content.isEmpty() ? objectMapper.nullNode() : objectMapper.readTree(content);
    }
    
    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }
}
