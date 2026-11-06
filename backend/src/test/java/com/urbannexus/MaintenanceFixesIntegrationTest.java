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

import com.urbannexus.config.PaymentResidentBackfill;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Signing in over a stale session, deleting technicians and amenities without losing residents' invoices, amenity
 * renames, availability input, admin edits of a technician's skill, the overdue-scan result, and the invoice
 * resident backfill. Runs on its own in-memory database; tests use distinct seeded rows because that database is
 * shared across the tests in this class.
 */
@SpringBootTest(properties = {
    "app.jwt.secret=dGVzdC1zZWNyZXQtdGhhdC1pcy1hdC1sZWFzdC00OC1ieXRlcy1sb25nLWZvci1oczM4NA==",
    "spring.datasource.url=jdbc:h2:mem:urbannexus-maintenance-test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
    "spring.h2.console.enabled=false",
    "spring.jpa.show-sql=false"
})
@AutoConfigureMockMvc
class MaintenanceFixesIntegrationTest {

    private static final String SEED_PASSWORD = "pwd123#";

    private static String adminToken;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private PaymentResidentBackfill backfill;

    // ---------------------------------------------------------------- stale session at login

    @Test
    void staleTemporaryPasswordTokenDoesNotBlockSigningIn() throws Exception {
        // A token still flagged must-change-password, left in the browser.
        String stale = login("amy_k", SEED_PASSWORD, null).get("token").asString();

        login("amy_k", SEED_PASSWORD, stale);
        login("kevin_n", SEED_PASSWORD, stale);
    }

    // ---------------------------------------------------------------- deleting technicians

    @Test
    void technicianWithOpenJobsCannotBeDeletedAndResidentsKeepTheirInvoicesAfterwards() throws Exception {
        // Seed: tech 2 has assignment 2 (Assigned) for resident 3, invoiced as TXN-103 (Overdue).
        mvc.perform(withToken(delete("/api/technicians/2"), admin()))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.error").value(org.hamcrest.Matchers.containsString("1 open job")));

        updateTask(2, "Completed").andExpect(status().isOk());
        mvc.perform(withToken(delete("/api/technicians/2"), admin())).andExpect(status().isOk());

        // The resident still sees, and can settle, the invoice for the deleted technician's job.
        String michael = activate("michael_j");
        JsonNode dues = body(mvc.perform(withToken(get("/api/residents/me/dues"), michael))
                                 .andExpect(status().isOk()).andReturn());
        assertThat(dues.get("invoices").findValuesAsString("trans_no")).contains("TXN-103");
        mvc.perform(withToken(post("/api/payments/TXN-103/submit"), michael)
                        .contentType(MediaType.APPLICATION_JSON).content(json(Map.of("reference", "UPI-1"))))
            .andExpect(status().isOk());

        JsonNode ledger = body(mvc.perform(withToken(get("/api/admin/transactions"), admin())
                                               .param("resident_name", "michael"))
                                   .andExpect(status().isOk()).andReturn());
        assertThat(ledger.findValuesAsString("trans_no")).contains("TXN-103");
    }

    // ---------------------------------------------------------------- deleting amenities

    @Test
    void amenityWithUpcomingBookingsCannotBeDeleted() throws Exception {
        String sarah = activate("sarah_w");
        String nextWeek = LocalDate.now(ZoneId.of("Asia/Kolkata")).plusDays(7).toString();
        mvc.perform(withToken(post("/api/bookings/amenity"), sarah)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("amenity_id", 5, "date", nextWeek, "slot", 1, "capacity_booked", 2))))
            .andExpect(status().isCreated());

        mvc.perform(withToken(delete("/api/amenities/5"), admin()))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.error").value(org.hamcrest.Matchers.containsString("1 upcoming booking")));
    }

    @Test
    void deletingAnAmenityWithOnlyPastBookingsKeepsTheirInvoices() throws Exception {
        // Seed: amenity 1 (Paddock Club Lounge) has a past booking of resident 4, invoiced as TXN-109 (Pending).
        mvc.perform(withToken(delete("/api/amenities/1"), admin())).andExpect(status().isOk());

        String emily = activate("emily_d");
        JsonNode dues = body(mvc.perform(withToken(get("/api/residents/me/dues"), emily))
                                 .andExpect(status().isOk()).andReturn());
        assertThat(dues.get("invoices").findValuesAsString("trans_no")).contains("TXN-109");
    }

    // ---------------------------------------------------------------- renaming amenities

    @Test
    void renamedAmenityKeepsItsPriceAndStaysBookable() throws Exception {
        mvc.perform(withToken(put("/api/amenities/3"), admin())
                        .contentType(MediaType.APPLICATION_JSON).content(json(Map.of("name", "Fitness Studio"))))
            .andExpect(status().isOk());

        String token = activate("ryan_a");
        String tomorrow = LocalDate.now(ZoneId.of("Asia/Kolkata")).plusDays(1).toString();
        JsonNode booking = body(mvc.perform(withToken(post("/api/bookings/amenity"), token)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(json(Map.of("amenity_id", 3, "date", tomorrow, "slot", 2,
                                                    "capacity_booked", 1))))
                                    .andExpect(status().isCreated()).andReturn());
        assertThat(booking.get("amenity_name").asString()).isEqualTo("Fitness Studio");
        assertThat(booking.get("total_with_gst").decimalValue()).isEqualByComparingTo("354.00");

        // Another amenity already has this name.
        mvc.perform(withToken(put("/api/amenities/3"), admin())
                        .contentType(MediaType.APPLICATION_JSON).content(json(Map.of("name", "Community Theater"))))
            .andExpect(status().isConflict());
    }

    // ---------------------------------------------------------------- availability input

    @Test
    void availabilityRequiresTrueOrFalse() throws Exception {
        String tech = activate("tech_marc");
        for (String body : new String[]{"{}", "{\"available\":null}"}) {
            mvc.perform(withToken(put("/api/technicians/me/availability"), tech)
                            .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
        }
        mvc.perform(withToken(put("/api/technicians/me/availability"), tech)).andExpect(status().isBadRequest());
        mvc.perform(withToken(put("/api/technicians/me/availability"), tech)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"available\":false}"))
            .andExpect(status().isOk());
    }

    // ---------------------------------------------------------------- admin edits a technician's skill

    @Test
    void adminCanChangeATechniciansSkillToAPricedService() throws Exception {
        // Seed: tech 8 (Andre Lotterer) is a Painter.
        mvc.perform(withToken(put("/api/technicians/8"), admin())
                        .contentType(MediaType.APPLICATION_JSON).content(json(Map.of("skill", "Juggler"))))
            .andExpect(status().isBadRequest());
        mvc.perform(withToken(put("/api/technicians/8"), admin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("skill", "HVAC", "contact", "9000000888"))))
            .andExpect(status().isOk());
        assertThat(jdbc.queryForObject("SELECT skill FROM technician WHERE tech_id = 8", String.class)).isEqualTo("HVAC");

        mvc.perform(withToken(put("/api/technicians/8"), activate("tech_andre"))
                        .contentType(MediaType.APPLICATION_JSON).content(json(Map.of("skill", "Painter"))))
            .andExpect(status().isForbidden());

        // New technicians must also get a bookable skill.
        mvc.perform(withToken(post("/api/technicians"), admin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("tech_id", 90, "name", "No Skill", "contact", "9000000090",
                            "skill", "Juggler"))))
            .andExpect(status().isBadRequest());
    }

    // ---------------------------------------------------------------- overdue scan result

    @Test
    void overdueScanReportsHowManyInvoicesItMarked() throws Exception {
        // Seed: TXN-106 is Pending and recent; age it past the 30-day limit.
        jdbc.update("UPDATE payment SET payment_date = CURRENT_TIMESTAMP - INTERVAL '40' DAY WHERE trans_no = 'TXN-106'");

        JsonNode result = body(mvc.perform(withToken(post("/api/admin/process-overdue"), admin()))
                                   .andExpect(status().isOk()).andReturn());
        assertThat(result.get("marked_overdue").asInt()).isEqualTo(1);
        assertThat(result.get("message").asString()).contains("1 invoice(s)").doesNotContain("cursor");
        assertThat(jdbc.queryForObject("SELECT status FROM payment WHERE trans_no = 'TXN-106'", String.class))
            .isEqualTo("Overdue");
    }

    // ---------------------------------------------------------------- invoice resident backfill

    @Test
    void everyInvoiceIsLinkedToItsResidentAndTheBackfillIsRepeatable() throws Exception {
        // data.sql inserts invoices before their bookings, so these links come from the startup backfill.
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM payment WHERE resident_id IS NULL", Long.class)).isZero();
        assertThat(jdbc.queryForObject("SELECT resident_id FROM payment WHERE trans_no = 'TXN-102'", Long.class))
            .isEqualTo(2L);

        // An invoice from before the column existed gets linked; already-linked rows are left alone.
        jdbc.update("UPDATE payment SET resident_id = NULL WHERE trans_no = 'TXN-105'");
        backfill.run(new DefaultApplicationArguments());
        assertThat(jdbc.queryForObject("SELECT resident_id FROM payment WHERE trans_no = 'TXN-105'", Long.class))
            .isEqualTo(4L);
    }

    // ---------------------------------------------------------------- helpers

    private synchronized String admin() throws Exception {
        if (adminToken == null) {
            String temporary = login("admin", "Bhumika@1234", null).get("token").asString();
            adminToken = changePassword(temporary, "Bhumika@1234", "admin-test-secret");
        }
        return adminToken;
    }

    /** Logs a seeded account in and completes its forced password change; returns a usable token. */
    private String activate(String username) throws Exception {
        String temporary = login(username, SEED_PASSWORD, null).get("token").asString();
        return changePassword(temporary, SEED_PASSWORD, username + "-secret");
    }

    private JsonNode login(String username, String password, String bearer) throws Exception {
        MockHttpServletRequestBuilder request = post("/api/login")
                                                    .contentType(MediaType.APPLICATION_JSON)
                                                    .content(json(Map.of("username", username, "password", password)));
        if (bearer != null) {
            request.header("Authorization", "Bearer " + bearer);
        }
        return body(mvc.perform(request).andExpect(status().isOk()).andReturn());
    }

    private String changePassword(String token, String current, String next) throws Exception {
        return body(mvc.perform(withToken(put("/api/profile/password"), token)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(json(Map.of("currentPassword", current, "newPassword", next))))
                        .andExpect(status().isOk())
                        .andReturn()).get("token").asString();
    }

    private ResultActions updateTask(long id, String status) throws Exception {
        return mvc.perform(withToken(put("/api/technicians/tasks/" + id + "/status"), admin())
                               .contentType(MediaType.APPLICATION_JSON)
                               .content(json(Map.of("status", status))));
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
