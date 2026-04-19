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

package com.urbannexus.controller;

import java.util.Map;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.urbannexus.security.UserPrincipal;
import com.urbannexus.service.TechnicianService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/technicians")
@RequiredArgsConstructor
public class TechnicianController {

    private final TechnicianService technicianService;

    @PostMapping
    public ResponseEntity<?> addTechnician(@AuthenticationPrincipal UserPrincipal currentUser,
            @RequestBody Map<String, Object> payload) {
        if (!"SuperAdmin".equals(currentUser.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Admin only."));
        }

        try {
            Long techId = payload.get("tech_id") != null ? Long.parseLong(payload.get("tech_id").toString()) : null;
            if (techId == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Tech ID must be a number."));
            }

            String name = (String) payload.get("name");
            String contact = (String) payload.get("contact");
            String skill = (String) payload.get("skill");

            Map<String, Object> res = technicianService.addTechnician(techId, name, contact, skill);
            return ResponseEntity.status(HttpStatus.CREATED).body(res);
        } catch (DuplicateKeyException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "This Tech ID or Username is already taken on the grid."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to add technician to the grid."));
        }
    }

    @GetMapping("/me/tasks")
    public ResponseEntity<?> getTasks(@AuthenticationPrincipal UserPrincipal currentUser) {
        if (!"Technician".equals(currentUser.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Access denied."));
        }

        try {
            return ResponseEntity.ok(technicianService.getTasks(currentUser.getTechId()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch tasks."));
        }
    }

    @PutMapping("/tasks/{id}/status")
    public ResponseEntity<?> updateTaskStatus(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        try {
            String status = payload.get("status");
            return ResponseEntity.ok(technicianService.updateTaskStatus(id, status));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update status"));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTechnician(@AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable Long id) {
        if (!"SuperAdmin".equals(currentUser.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Only Admin can delete technicians."));
        }

        try {
            technicianService.deleteTechnician(id);
            return ResponseEntity.ok(Map.of("message", "Technician removed successfully."));
        } catch (RuntimeException e) {
            if (e.getMessage().equals("Technician not found.")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to delete technician."));
        }
    }
}
