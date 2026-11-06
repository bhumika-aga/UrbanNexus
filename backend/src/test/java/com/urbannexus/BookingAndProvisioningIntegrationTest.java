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

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.urbannexus.service.BookingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Checks booking capacity and slot exclusivity (including under concurrent requests), duplicate-ID rejection when
 * adding technicians and amenities, and username generation. Runs on its own in-memory database; tests use distinct
 * seeded accounts, amenities and dates because that database is shared across the tests in this class.
 */
@SpringBootTest(properties = {
    "app.jwt.secret=dGVzdC1zZWNyZXQtdGhhdC1pcy1hdC1sZWFzdC00OC1ieXRlcy1sb25nLWZvci1oczM4NA==",
    // A generous lock timeout: concurrent bookings queue on row locks, as they would on PostgreSQL.
    "spring.datasource.url=jdbc:h2:mem:urbannexus-booking-test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=10000",
    "spring.h2.console.enabled=false",
    "spring.jpa.show-sql=false"
})
@AutoConfigureMockMvc
class BookingAndProvisioningIntegrationTest {
    
    private static final String SEED_PASSWORD = "pwd123#";
    
    private static String adminToken;
    
    @Autowired
    private MockMvc mvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private BookingService bookingService;
    
    @Autowired
    private JdbcTemplate jdbc;
    
    // ---------------------------------------------------------------- 5. amenity capacity
    
    private static Map<String, Object> amenityBooking(long amenityId, String date, int slot, Integer headcount) {
        Map<String, Object> request = new HashMap<>(Map.of("amenity_id", amenityId, "date", date, "slot", slot));
        if (headcount != null) {
            request.put("capacity_booked", headcount);
        }
        return request;
    }
    
    private static Map<String, Object> residentPayload(String name) {
        return Map.of("name", name, "house_block", "Z", "house_floor", "1", "house_unit", "101",
            "ownership_status", "Owner", "contact", "9000000000", "no_of_members", 1);
    }
    
    /**
     * Starts every task at the same instant and returns each one's result, or the exception it threw.
     */
    private static List<Object> runConcurrently(List<Callable<Object>> tasks) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(tasks.size());
        CountDownLatch start = new CountDownLatch(1);
        try {
            List<Future<Object>> futures = new ArrayList<>();
            for (Callable<Object> task : tasks) {
                futures.add(pool.submit(() -> {
                    start.await();
                    try {
                        return task.call();
                    } catch (Exception e) {
                        return e;
                    }
                }));
            }
            start.countDown();
            List<Object> outcomes = new ArrayList<>();
            for (Future<Object> future : futures) {
                outcomes.add(future.get(30, TimeUnit.SECONDS));
            }
            return outcomes;
        } finally {
            pool.shutdownNow();
        }
    }
    
    private static HttpStatus statusOf(Object outcome) {
        assertThat(outcome).as("unexpected failure: %s", outcome).isInstanceOf(ResponseStatusException.class);
        return HttpStatus.valueOf(((ResponseStatusException) outcome).getStatusCode().value());
    }
    
    // ---------------------------------------------------------------- 6. technician double booking
    
    private static MockHttpServletRequestBuilder withToken(MockHttpServletRequestBuilder request, String token) {
        return request.header("Authorization", "Bearer " + token);
    }
    
    @Test
    void amenityBookingRejectsMissingOrInvalidFields() throws Exception {
        String token = activate("ashley_w");
        
        bookAmenity(token, amenityBooking(3, "2031-01-05", 1, 0)).andExpect(status().isBadRequest());
        bookAmenity(token, amenityBooking(3, "2031-01-05", 1, -2)).andExpect(status().isBadRequest());
        bookAmenity(token, amenityBooking(3, "2031-01-05", 1, null)).andExpect(status().isBadRequest());
        bookAmenity(token, amenityBooking(3, "2031-01-05", 4, 1)).andExpect(status().isBadRequest());
        bookAmenity(token, amenityBooking(3, "05/01/2031", 1, 1)).andExpect(status().isBadRequest());
        bookAmenity(token, amenityBooking(999, "2031-01-05", 1, 1)).andExpect(status().isNotFound());
    }
    
    @Test
    void amenityCapacityCountsEarlierBookingsInTheSameSlot() throws Exception {
        // Seed: amenity 3 (Parc Fermé Gym) has capacity 10.
        String token = activate("robert_m");
        
        bookAmenity(token, amenityBooking(3, "2031-01-10", 1, 6)).andExpect(status().isCreated());
        JsonNode tooMany = body(bookAmenity(token, amenityBooking(3, "2031-01-10", 1, 5))
                                    .andExpect(status().isConflict()).andReturn());
        assertThat(tooMany.get("error").asString()).contains("Only 4 place(s) left");
        
        bookAmenity(token, amenityBooking(3, "2031-01-10", 1, 4)).andExpect(status().isCreated());
        JsonNode full = body(bookAmenity(token, amenityBooking(3, "2031-01-10", 1, 1))
                                 .andExpect(status().isConflict()).andReturn());
        assertThat(full.get("error").asString()).contains("fully booked");
        
        // Other slots and dates have their own capacity.
        bookAmenity(token, amenityBooking(3, "2031-01-10", 2, 10)).andExpect(status().isCreated());
        bookAmenity(token, amenityBooking(3, "2031-01-11", 1, 10)).andExpect(status().isCreated());
    }
    
    // ---------------------------------------------------------------- 7. add must not overwrite
    
    @Test
    void amenityWithoutCapacityHasNoHeadcountLimit() throws Exception {
        mvc.perform(withToken(post("/api/amenities"), admin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("amenity_id", 50, "name", "Open Lawn"))))
            .andExpect(status().isCreated());
        jdbc.update("INSERT INTO pricing (item_name, category, base_price) VALUES ('Open Lawn', 'Amenity', 100.00)");
        
        String token = activate("kevin_n");
        bookAmenity(token, amenityBooking(50, "2031-01-15", 1, 500)).andExpect(status().isCreated());
        bookAmenity(token, amenityBooking(50, "2031-01-15", 1, 500)).andExpect(status().isCreated());
    }
    
    @Test
    void concurrentAmenityBookingsNeverExceedCapacity() throws Exception {
        // Seed: amenity 4 (Spa & Wellness Center) has capacity 8, so only two bookings of 3 fit.
        List<Long> residents = List.of(12L, 13L, 14L, 15L, 16L, 17L, 18L, 19L);
        List<Object> outcomes = runConcurrently(residents.stream()
                                                    .<Callable<Object>>map(id -> () -> bookingService.bookAmenity(
                                                        id, 4L, "2031-02-01", 1, 3))
                                                    .toList());
        
        assertThat(outcomes.stream().filter(o -> o instanceof Map).count()).isEqualTo(2);
        assertThat(outcomes.stream().filter(o -> !(o instanceof Map)))
            .allSatisfy(o -> assertThat(statusOf(o)).isEqualTo(HttpStatus.CONFLICT));
        assertThat(jdbc.queryForObject(
            "SELECT SUM(capacity_booked) FROM amenity_mgmt WHERE amenity_id = 4 AND date = DATE '2031-02-01' AND slot = 1",
            Long.class)).isEqualTo(6L);
    }
    
    @Test
    void technicianSlotCanOnlyBeBookedOnce() throws Exception {
        // Seed: tech 9 is the only Mason.
        String token = activate("daniel_l");
        Map<String, Object> request = Map.of("skill", "Mason", "assign_date", "2031-03-01", "slot", 1);
        
        bookTechnician(token, request).andExpect(status().isCreated());
        bookTechnician(token, request).andExpect(status().isConflict());
        bookTechnician(token, Map.of("skill", "Mason", "assign_date", "2031-03-01", "slot", 2))
            .andExpect(status().isCreated());
        bookTechnician(token, Map.of("skill", "Mason", "assign_date", "2031-03-01", "slot", 9))
            .andExpect(status().isBadRequest());
    }
    
    // ---------------------------------------------------------------- 8. username collisions
    
    @Test
    void concurrentTechnicianBookingsGiveEachTechnicianOneJobPerSlot() throws Exception {
        // Seed: techs 1, 5 and 15 are the Plumbers, so exactly three of six simultaneous requests can succeed.
        // (No other test in this class adds a Plumber.)
        List<Long> residents = List.of(20L, 21L, 22L, 23L, 24L, 25L);
        List<Object> outcomes = runConcurrently(residents.stream()
                                                    .<Callable<Object>>map(id -> () -> bookingService.bookTechnician(
                                                        id, "Plumber", 2, "2031-04-01"))
                                                    .toList());
        
        assertThat(outcomes.stream().filter(o -> o instanceof Map).count()).isEqualTo(3);
        assertThat(outcomes.stream().filter(o -> !(o instanceof Map)))
            .allSatisfy(o -> assertThat(statusOf(o)).isEqualTo(HttpStatus.CONFLICT));
        assertThat(jdbc.queryForList(
            "SELECT tech_id FROM technician_management WHERE assign_date = DATE '2031-04-01' AND slot = 2 ORDER BY tech_id",
            Long.class)).containsExactly(1L, 5L, 15L);
    }
    
    @Test
    void databaseRejectsASecondAssignmentForTheSameTechnicianAndSlot() {
        String insert = "INSERT INTO technician_management (resident_id, tech_id, slot, assign_date, status) "
                            + "VALUES (2, 14, 3, DATE '2031-05-01', 'Assigned')";
        jdbc.update(insert);
        assertThatThrownBy(() -> jdbc.update(insert)).isInstanceOf(DataIntegrityViolationException.class);
    }
    
    @Test
    void addingTechnicianWithExistingIdIsRejectedAndChangesNothing() throws Exception {
        mvc.perform(withToken(post("/api/technicians"), admin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("tech_id", 2, "name", "Impostor", "contact", "9999999999",
                            "skill", "Plumber"))))
            .andExpect(status().isConflict());
        
        assertThat(jdbc.queryForObject("SELECT name FROM technician WHERE tech_id = 2", String.class))
            .isEqualTo("Jacky Ickx");
        assertThat(jdbc.queryForObject("SELECT skill FROM technician WHERE tech_id = 2", String.class))
            .isEqualTo("Electrician");
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM admin WHERE tech_id = 2", Long.class)).isEqualTo(1L);
    }
    
    // ---------------------------------------------------------------- helpers
    
    @Test
    void addingAmenityWithExistingIdIsRejectedAndChangesNothing() throws Exception {
        mvc.perform(withToken(post("/api/amenities"), admin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("amenity_id", 1, "name", "Renamed", "capacity", 1))))
            .andExpect(status().isConflict());
        
        Map<String, Object> amenity = jdbc.queryForMap("SELECT name, capacity FROM amenity WHERE amenity_id = 1");
        assertThat(amenity.get("NAME")).isEqualTo("Paddock Club Lounge");
        assertThat(amenity.get("CAPACITY")).isEqualTo(20);
    }
    
    @Test
    void technicianAndAmenityFieldsAreValidated() throws Exception {
        addTechnician(Map.of("tech_id", 60, "name", " ", "contact", "9000000060", "skill", "Plumber"))
            .andExpect(status().isBadRequest());
        addTechnician(Map.of("tech_id", 60, "name", "No Contact", "skill", "Plumber"))
            .andExpect(status().isBadRequest());
        addTechnician(Map.of("tech_id", "abc", "name", "Bad Id", "contact", "9000000060", "skill", "Plumber"))
            .andExpect(status().isBadRequest());
        addTechnician(Map.of("tech_id", -1, "name", "Negative", "contact", "9000000060", "skill", "Plumber"))
            .andExpect(status().isBadRequest());
        
        for (Map<String, Object> amenity : List.<Map<String, Object>>of(
            Map.of("amenity_id", 61, "name", "Zero Room", "capacity", 0),
            Map.of("amenity_id", 61, "name", ""))) {
            mvc.perform(withToken(post("/api/amenities"), admin())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json(amenity)))
                .andExpect(status().isBadRequest());
        }
        mvc.perform(withToken(put("/api/amenities/1"), admin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("capacity", -5))))
            .andExpect(status().isBadRequest());
    }
    
    @Test
    void residentAndTechnicianWithSameNameAndIdGetDistinctUsernames() throws Exception {
        JsonNode resident = body(addResident(residentPayload("Max Power")).andExpect(status().isCreated()).andReturn());
        long residentId = jdbc.queryForObject("SELECT resident_id FROM resident WHERE name = 'Max Power'", Long.class);
        
        JsonNode technician = body(addTechnician(Map.of("tech_id", residentId, "name", "Max Power",
            "contact", "9000000001", "skill", "Carpenter")).andExpect(status().isCreated()).andReturn());
        
        assertThat(resident.get("username").asString()).isEqualTo("max_power" + residentId);
        assertThat(technician.get("username").asString()).isEqualTo("max_power" + residentId + "_2");
        login(resident.get("username").asString(), resident.get("password").asString(), 200);
        login(technician.get("username").asString(), technician.get("password").asString(), 200);
    }
    
    @Test
    void namesEndingInDigitsCannotCollideWithLongerIds() throws Exception {
        JsonNode first = body(addTechnician(Map.of("tech_id", 3456, "name", "Ann", "contact", "9000003456",
            "skill", "Painter")).andExpect(status().isCreated()).andReturn());
        JsonNode second = body(addTechnician(Map.of("tech_id", 56, "name", "Ann34", "contact", "9000000056",
            "skill", "Painter")).andExpect(status().isCreated()).andReturn());
        
        assertThat(first.get("username").asString()).isEqualTo("ann3456");
        assertThat(second.get("username").asString()).isEqualTo("ann34_56");
    }
    
    @Test
    void usernamesAreSanitisedAndFitTheColumn() throws Exception {
        JsonNode accented = body(addResident(residentPayload("  Zoë Ångström-Smith ")).andExpect(status().isCreated())
                                     .andReturn());
        assertThat(accented.get("username").asString()).matches("zoe_angstrom_smith\\d+");
        
        String longName = "a".repeat(100);
        JsonNode longest = body(addResident(residentPayload(longName)).andExpect(status().isCreated()).andReturn());
        assertThat(longest.get("username").asString()).hasSizeLessThanOrEqualTo(50).startsWith("aaaa");
        login(longest.get("username").asString(), longest.get("password").asString(), 200);
        
        addResident(residentPayload("a".repeat(101))).andExpect(status().isBadRequest());
        addResident(residentPayload("")).andExpect(status().isBadRequest());
        
        Map<String, Object> missingBlock = new HashMap<>(residentPayload("No Block"));
        missingBlock.remove("house_block");
        addResident(missingBlock).andExpect(status().isBadRequest());
        
        Map<String, Object> tooLongUnit = new HashMap<>(residentPayload("Long Unit"));
        tooLongUnit.put("house_unit", "12345678901");
        addResident(tooLongUnit).andExpect(status().isBadRequest());
    }
    
    private synchronized String admin() throws Exception {
        if (adminToken == null) {
            String temporary = login("admin", "Bhumika@1234", 200).get("token").asString();
            adminToken = changePassword(temporary, "Bhumika@1234", "admin-test-secret");
        }
        return adminToken;
    }
    
    /**
     * Logs a seeded account in and completes its forced password change; returns a usable token.
     */
    private String activate(String username) throws Exception {
        String temporary = login(username, SEED_PASSWORD, 200).get("token").asString();
        return changePassword(temporary, SEED_PASSWORD, username + "-secret");
    }
    
    private JsonNode login(String username, String password, int expectedStatus) throws Exception {
        return body(mvc.perform(post("/api/login")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(json(Map.of("username", username, "password", password))))
                        .andExpect(status().is(expectedStatus))
                        .andReturn());
    }
    
    private String changePassword(String token, String current, String next) throws Exception {
        JsonNode response = body(mvc.perform(withToken(put("/api/profile/password"), token)
                                                 .contentType(MediaType.APPLICATION_JSON)
                                                 .content(json(Map.of("currentPassword", current,
                                                     "newPassword", next))))
                                     .andExpect(status().isOk())
                                     .andReturn());
        return response.get("token").asString();
    }
    
    private ResultActions bookAmenity(String token, Map<String, Object> request) throws Exception {
        return mvc.perform(withToken(post("/api/bookings/amenity"), token)
                               .contentType(MediaType.APPLICATION_JSON)
                               .content(json(request)));
    }
    
    private ResultActions bookTechnician(String token, Map<String, Object> request) throws Exception {
        return mvc.perform(withToken(post("/api/bookings/technician"), token)
                               .contentType(MediaType.APPLICATION_JSON)
                               .content(json(request)));
    }
    
    private ResultActions addTechnician(Map<String, Object> payload) throws Exception {
        return mvc.perform(withToken(post("/api/technicians"), admin())
                               .contentType(MediaType.APPLICATION_JSON)
                               .content(json(payload)));
    }
    
    private ResultActions addResident(Map<String, Object> payload) throws Exception {
        return mvc.perform(withToken(post("/api/residents"), admin())
                               .contentType(MediaType.APPLICATION_JSON)
                               .content(json(payload)));
    }
    
    private JsonNode body(MvcResult result) throws Exception {
        String content = result.getResponse().getContentAsString();
        return content.isEmpty() ? objectMapper.nullNode() : objectMapper.readTree(content);
    }
    
    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }
}
