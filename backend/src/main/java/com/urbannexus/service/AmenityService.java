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

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.urbannexus.model.Amenity;
import com.urbannexus.repository.AmenityRepository;

@Service
public class AmenityService {

    private final AmenityRepository amenityRepository;

    public AmenityService(AmenityRepository amenityRepository) {
        this.amenityRepository = amenityRepository;
    }

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

    public void updateAmenity(Long id, String name, Integer capacity) {
        Optional<Amenity> amenityOpt = amenityRepository.findById(id);
        if (amenityOpt.isEmpty()) {
            throw new RuntimeException("Amenity not found.");
        }
        Amenity amenity = amenityOpt.get();
        if (name != null)
            amenity.setName(name);
        if (capacity != null)
            amenity.setCapacity(capacity);
        amenityRepository.save(amenity);
    }

    public void deleteAmenity(Long id) {
        if (!amenityRepository.existsById(id)) {
            throw new RuntimeException("Amenity not found.");
        }
        amenityRepository.deleteById(id);
    }
}
