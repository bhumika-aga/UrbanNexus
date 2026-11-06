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
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Runs the real schema.sql, data.sql and native queries against PostgreSQL (production runs on Supabase PostgreSQL;
 * every other test uses H2 in PostgreSQL mode, which accepts some SQL that PostgreSQL rejects). Skipped when Docker
 * is not available.
 */
@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(properties = {
    "app.jwt.secret=dGVzdC1zZWNyZXQtdGhhdC1pcy1hdC1sZWFzdC00OC1ieXRlcy1sb25nLWZvci1oczM4NA==",
    "spring.datasource.driver-class-name=org.postgresql.Driver",
    "spring.sql.init.mode=always",
    "spring.h2.console.enabled=false",
    "spring.jpa.show-sql=false"
})
@AutoConfigureMockMvc
class PostgresCompatibilityIntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    private static String adminToken;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void transactionLedgerWorksWithAndWithoutFilters() throws Exception {
        // The admin Financial Ledger calls this with no filters at all.
        assertThat(transactions("")).hasSizeGreaterThanOrEqualTo(10);
        assertThat(transactions("?status=Paid")).allSatisfy(tx -> assertThat(tx.get("status").asText()).isEqualTo("Paid"));
        assertThat(transactions("?block=A")).allSatisfy(tx -> assertThat(tx.get("house_block").asText()).isEqualTo("A"));
        assertThat(transactions("?resident_name=Jane")).isNotEmpty()
            .allSatisfy(tx -> assertThat(tx.get("resident_name").asText()).contains("Jane"));
    }

    @Test
    void adminQueriesRunOnPostgres() throws Exception {
        for (String path : new String[]{"/api/admin/stats", "/api/admin/audit-logs", "/api/admin/assignments",
            "/api/admin/amenities/bookings", "/api/admin/technicians/bookings", "/api/admin/technicians"}) {
            mvc.perform(withToken(get(path), admin())).andExpect(status().isOk());
        }
        mvc.perform(withToken(post("/api/admin/process-overdue"), admin())).andExpect(status().isOk());
        
        JsonNode residents = body(mvc.perform(withToken(get("/api/admin/residents/search"), admin())
                                                  .param("name", "john").param("block", "a"))
                                      .andExpect(status().isOk()).andReturn());
        assertThat(residents.findValuesAsText("name")).contains("John Doe", "Michael Johnson");
    }
    
    @Test
    void residentBookingAndPaymentFlowRunsOnPostgres() throws Exception {
        String token = activate("chris_b");
        
        // Row locks (SELECT ... FOR UPDATE) and the capacity sum.
        mvc.perform(withToken(post("/api/bookings/amenity"), token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("amenity_id", 3, "date", "2031-06-01", "slot", 1, "capacity_booked", 10))))
            .andExpect(status().isCreated());
        mvc.perform(withToken(post("/api/bookings/amenity"), token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("amenity_id", 3, "date", "2031-06-01", "slot", 1, "capacity_booked", 1))))
            .andExpect(status().isConflict());
        Map<String, Object> mason = Map.of("skill", "Mason", "assign_date", "2031-06-01", "slot", 2);
        mvc.perform(withToken(post("/api/bookings/technician"), token)
                        .contentType(MediaType.APPLICATION_JSON).content(json(mason)))
            .andExpect(status().isCreated());
        mvc.perform(withToken(post("/api/bookings/technician"), token)
                        .contentType(MediaType.APPLICATION_JSON).content(json(mason)))
            .andExpect(status().isConflict());
        
        mvc.perform(withToken(get("/api/residents/me/bookings"), token)).andExpect(status().isOk());
        JsonNode dues = body(mvc.perform(withToken(get("/api/residents/me/dues"), token))
                                 .andExpect(status().isOk()).andReturn());
        assertThat(dues.get("total_unpaid_invoices").asInt()).isGreaterThanOrEqualTo(3);
        
        // Seed: TXN-106 is a Pending invoice of resident 5 (chris_b).
        mvc.perform(withToken(post("/api/payments/TXN-106/submit"), token)
                        .contentType(MediaType.APPLICATION_JSON).content(json(Map.of("reference", "UPI-PG-1"))))
            .andExpect(status().isOk());
        mvc.perform(withToken(post("/api/payments/TXN-106/reject"), admin())).andExpect(status().isOk());
        mvc.perform(withToken(post("/api/payments/TXN-106/submit"), token)
                        .contentType(MediaType.APPLICATION_JSON).content(json(Map.of("reference", "UPI-PG-2"))))
            .andExpect(status().isOk());
        mvc.perform(withToken(post("/api/payments/TXN-106/confirm"), admin())).andExpect(status().isOk());
        
        mvc.perform(withToken(put("/api/profile/update"), token)
                        .contentType(MediaType.APPLICATION_JSON).content(json(Map.of("no_of_members", "3"))))
            .andExpect(status().isOk());
    }
    
    @Test
    void residentDeletionWithWriteOffRunsOnPostgres() throws Exception {
        // Seed: resident 3 (Michael Johnson) has TXN-103 Overdue and TXN-104 Pending.
        mvc.perform(withToken(delete("/api/residents/3"), admin())).andExpect(status().isConflict());
        mvc.perform(withToken(delete("/api/residents/3"), admin()).param("write_off_unpaid", "true"))
            .andExpect(status().isOk());
        
        JsonNode ledger = transactions("?resident_name=michael");
        assertThat(ledger).hasSize(2).allSatisfy(tx -> {
            assertThat(tx.get("status").asText()).isEqualTo("Written Off");
            assertThat(tx.get("resident_name").asText()).isEqualTo("Michael Johnson");
        });
    }
    
    // ---------------------------------------------------------------- helpers
    
    private String activate(String username) throws Exception {
        String temporary = login(username, "pwd123#").get("token").asText();
        return changePassword(temporary, "pwd123#", username + "-secret");
    }

    private JsonNode transactions(String query) throws Exception {
        return body(mvc.perform(withToken(get("/api/admin/transactions" + query), admin()))
                        .andExpect(status().isOk()).andReturn());
    }

    private synchronized String admin() throws Exception {
        if (adminToken == null) {
            String temporary = login("admin", "Bhumika@1234").get("token").asText();
            adminToken = changePassword(temporary, "Bhumika@1234", "admin-test-secret");
        }
        return adminToken;
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
