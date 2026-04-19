package com.urbannexus.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.urbannexus.model.Amenity;
import com.urbannexus.repository.AmenityRepository;

@Service
public class AmenityService {

    @Autowired
    private AmenityRepository amenityRepository;

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
