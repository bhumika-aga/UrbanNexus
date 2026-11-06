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

import com.urbannexus.model.Amenity;
import com.urbannexus.model.Pricing.PricingCategory;
import com.urbannexus.repository.AmenityMgmtRepository;
import com.urbannexus.repository.AmenityRepository;
import com.urbannexus.repository.PricingRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AmenityService {
    
    private final AmenityRepository amenityRepository;
    private final AmenityMgmtRepository amenityMgmtRepository;
    private final PricingRepository pricingRepository;
    private final AuditService auditService;
    private final EntityManager entityManager;
    private final Clock clock;
    
    // NULL is allowed and means "no headcount limit".
    private static void requireValidCapacity(Integer capacity) {
        if (capacity != null && capacity < 1) {
            throw Inputs.badRequest("capacity must be at least 1.");
        }
    }
    
    @Transactional
    public void addAmenity(Long amenityId, String name, Integer capacity) {
        long id = Inputs.requirePositiveId(amenityId, "amenity_id");
        String amenityName = Inputs.requireText(name, "name", 100);
        requireValidCapacity(capacity);
        
        if (amenityRepository.existsById(id)) {
            throw Inputs.conflict("Amenity ID " + id + " already exists.");
        }
        
        Amenity amenity = new Amenity();
        amenity.setAmenityId(id);
        amenity.setName(amenityName);
        amenity.setCapacity(capacity);
        // persist, not save(): with an admin-assigned id, save() merges and would silently
        // overwrite a row inserted since the check above; persist always INSERTs.
        try {
            entityManager.persist(amenity);
            entityManager.flush();
        } catch (PersistenceException e) {
            throw Inputs.conflict("Amenity ID " + id + " already exists.");
        }
    }
    
    public List<Amenity> getAllAmenities() {
        return amenityRepository.findAll();
    }
    
    @Transactional
    public void updateAmenity(Long id, String name, Integer capacity) {
        Amenity amenity = amenityRepository.findById(id)
                              .orElseThrow(() -> Inputs.notFound("Amenity not found."));
        requireValidCapacity(capacity);
        if (name != null) {
            String newName = Inputs.requireText(name, "name", 100);
            if (!newName.equals(amenity.getName())) {
                rename(amenity, newName);
            }
        }
        if (capacity != null) amenity.setCapacity(capacity);
        amenityRepository.save(amenity);
    }
    
    // Prices are keyed by amenity name, so a rename must carry the amenity's price over to the new name;
    // otherwise the amenity can no longer be booked ("No pricing found").
    private void rename(Amenity amenity, String newName) {
        if (amenityRepository.existsByNameAndAmenityIdNot(newName, amenity.getAmenityId())) {
            throw Inputs.conflict("Another amenity is already called " + newName + ".");
        }
        if (pricingRepository.findByItemNameAndCategory(newName, PricingCategory.Amenity).isPresent()) {
            throw Inputs.conflict("A price for \"" + newName + "\" already exists; choose a different name.");
        }
        pricingRepository.renameItem(amenity.getName(), newName, PricingCategory.Amenity.name());
        auditService.log("amenity", amenity.getAmenityId().toString(), "UPDATE",
            "Renamed " + amenity.getName() + " to " + newName);
        amenity.setName(newName);
    }
    
    @Transactional
    public void deleteAmenity(Long id) {
        Amenity amenity = amenityRepository.findById(id)
                              .orElseThrow(() -> Inputs.notFound("Amenity not found."));
        // Deleting the amenity deletes its bookings; residents' invoices survive (they carry
        // payment.resident_id), but upcoming reservations would silently disappear.
        long upcoming = amenityMgmtRepository.countByAmenity_AmenityIdAndStatusAndDateGreaterThanEqual(
            id, "Confirmed", LocalDate.now(clock));
        if (upcoming > 0) {
            throw Inputs.conflict(amenity.getName() + " has " + upcoming + " upcoming booking(s) and cannot be "
                                      + "deleted until they have taken place.");
        }
        auditService.log("amenity", id.toString(), "DELETE", "Deleted amenity: " + amenity.getName());
        amenityRepository.delete(amenity);
    }
}
