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
import com.urbannexus.repository.AmenityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AmenityService {
    
    private final AmenityRepository amenityRepository;
    
    @Transactional
    public void addAmenity(Long amenityId, String name, Integer capacity) {
        Amenity amenity = new Amenity();
        amenity.setAmenityId(amenityId);
        amenity.setName(name);
        amenity.setCapacity(capacity);
        amenityRepository.save(amenity);
    }
    
    public List<Amenity> getAllAmenities() {
        return amenityRepository.findAll();
    }
    
    @Transactional
    public void updateAmenity(Long id, String name, Integer capacity) {
        Amenity amenity = amenityRepository.findById(id)
                              .orElseThrow(() -> new RuntimeException("Amenity not found."));
        if (name != null) amenity.setName(name);
        if (capacity != null) amenity.setCapacity(capacity);
        amenityRepository.save(amenity);
    }
    
    @Transactional
    public void deleteAmenity(Long id) {
        if (!amenityRepository.existsById(id)) {
            throw new RuntimeException("Amenity not found.");
        }
        amenityRepository.deleteById(id);
    }
}
